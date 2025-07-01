package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.RabbitMQProperties;
import ca.uhn.fhir.jpa.starter.ReportProperties;
import ca.uhn.fhir.jpa.starter.model.EmailScheduleData;
import ca.uhn.fhir.jpa.starter.model.EmailScheduleEntity;
import ca.uhn.fhir.jpa.starter.model.IndicatorColumn;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import com.iprd.report.model.data.ScoreCardItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import android.util.Pair;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * Service class responsible for generating and sending facility summaries via email.
 * This service retrieves facility data, processes it into reports, converts reports to CSV format,
 * and sends them as email attachments using a RabbitMQ message queue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailUserService {

	private static final Logger logger = LoggerFactory.getLogger(EmailUserService.class);

	private final RabbitTemplate rabbitTemplate;
	private final HelperService helperService;
	private final CSVConverter csvConverter;
	private final TaskScheduler taskScheduler;

	@Autowired
	private ReportProperties reportProperties;

	@Autowired
	private RabbitMQProperties rabbitMQProperties;

	@Autowired
	private Map<String, DashboardConfigContainer> dashboardEnvToConfigMap;

	// Field to store cached email schedules
	private List<EmailScheduleData> emailSchedulesForProcessing = new ArrayList<>();

	// Map to track scheduled tasks
	private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

	// Map to track previous schedules for comparison
	private final Map<Integer, EmailScheduleData> previousSchedules = new ConcurrentHashMap<>();

	// Map to store cron expressions by schedule ID
	private final Map<Integer, String> scheduleCronExpressions = new ConcurrentHashMap<>();

	@Scheduled(cron = "0 0 * * * *") // Run every hour
	private void cacheEmailSchedulesForProcessing() {
		try {
			List<EmailScheduleEntity> schedules = NotificationDataSource.getInstance().getAllEmailSchedules();
			List<EmailScheduleData> dataList = schedules.stream()
				.map(s -> {
					String scheduleType = s.getScheduleType() != null ? s.getScheduleType().toLowerCase() : "";
					String cronExpression;
					switch (scheduleType) {
						case "daily":
							cronExpression = "0 0 2 * * *"; // 2:00 AM daily
							break;
						case "weekly":
							cronExpression = "0 30 2 * * MON"; // Monday 2:30 AM
							break;
						case "monthly":
							cronExpression = "0 45 2 1 * *"; // At 2:45 AM, on day 1 of the month
							break;
						default:
							logger.warn("Invalid schedule type {} for ID {}, skipping schedule", scheduleType, s.getId());
							return null;
					}

					// Store cron expression in map
					scheduleCronExpressions.put(s.getId(), cronExpression);
					return new EmailScheduleData(
						s.getId(),
						s.getOrgId(),
						s.getRecipientEmail(),
						s.getEmailSubject(),
						s.getScheduleType(),
						s.getUpdatedAt()
					);
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

			synchronized (this) {
				// Create a map of new schedules by ID
				Map<Integer, EmailScheduleData> newSchedules = dataList.stream()
					.collect(Collectors.toMap(EmailScheduleData::getId, schedule -> schedule));

				// Identify deleted schedules and cancel their tasks
				Set<Integer> deletedIds = new HashSet<>(previousSchedules.keySet());
				deletedIds.removeAll(newSchedules.keySet());
				for (Integer id : deletedIds) {
					ScheduledFuture<?> future = scheduledTasks.remove(id);
					if (future != null) {
						future.cancel(false);
						logger.info("Canceled task for deleted schedule ID: {}", id);
					}
					previousSchedules.remove(id);
					scheduleCronExpressions.remove(id); // Clean up cron expression
				}

				// Process new or updated schedules
				for (EmailScheduleData schedule : dataList) {
					Integer id = schedule.getId();
					EmailScheduleData prevSchedule = previousSchedules.get(id);
					boolean shouldSchedule = prevSchedule == null || !prevSchedule.equals(schedule) || prevSchedule.getUpdatedAt().before(schedule.getUpdatedAt());

					String cronExpression = scheduleCronExpressions.get(id);
					if (shouldSchedule && StringUtils.hasText(cronExpression)) {
						try {
							// Cancel existing task if it exists
							ScheduledFuture<?> existingFuture = scheduledTasks.remove(id);
							if (existingFuture != null) {
								existingFuture.cancel(false);
								logger.info("Canceled existing task for updated schedule ID: {}", id);
							}

							// Schedule new task
							CronTrigger trigger = new CronTrigger(cronExpression);
							ScheduledFuture<?> future = taskScheduler.schedule(
								() -> sendFacilitySummaryForSchedule(schedule), trigger
							);
							scheduledTasks.put(id, future);
							previousSchedules.put(id, schedule);
							logger.info("Scheduled report for ID {} with cron: {}", id, cronExpression);
						} catch (IllegalArgumentException e) {
							logger.warn("Invalid cron expression for schedule ID {}: {}", id, cronExpression);
						}
					} else if (!StringUtils.hasText(cronExpression)) {
						logger.warn("No cron expression for schedule ID: {}", id);
					}
				}

				// Update cached schedules
				this.emailSchedulesForProcessing = new ArrayList<>(newSchedules.values());
				logger.info("Cached {} email schedules for future processing", dataList.size());
			}
		} catch (Exception e) {
			logger.warn("Email schedule caching task failed: {}", ExceptionUtils.getStackTrace(e));
		}
	}

	private void sendFacilitySummaryForSchedule(EmailScheduleData schedule) {
		LocalDate today = LocalDate.now();
		LocalDate startDate;
		LocalDate endDate;
		String dateRange;
		// Determine date range based on schedule type
		String scheduleType = schedule.getScheduleType().toLowerCase();
		switch (scheduleType) {
			case "daily":
				startDate = today.minusDays(1);
				endDate = startDate;
				break;
			case "monthly":
				startDate = today.minusMonths(1).withDayOfMonth(1);
				endDate = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth());
				break;
			case "weekly":
			default:
				startDate = today.minusWeeks(1).with(DayOfWeek.MONDAY);
				endDate = startDate.with(DayOfWeek.SUNDAY);
				break;
		}

		String formattedStartDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
		String formattedEndDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
		dateRange = formattedStartDate + " to " + formattedEndDate;

		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.put("type", scheduleType);
		ReportType type = ReportType.valueOf(scheduleType.toLowerCase());
		boolean isAnonymizationEnabled = false;

		DashboardConfigContainer configContainer = dashboardEnvToConfigMap.getOrDefault(reportProperties.getEnv(), new DashboardConfigContainer());
		List<IndicatorColumn> indicatorColumns = configContainer.getIndicatorColumns();
		if (indicatorColumns == null || indicatorColumns.isEmpty()) {
			logger.warn("No indicator columns found for environment: {}", reportProperties.getEnv());
			return;
		}

		Map<Integer, String> indicatorMap = new LinkedHashMap<>();
		for (IndicatorColumn indicator : indicatorColumns) {
			indicatorMap.put(indicator.getId(), indicator.getName());
		}

		String orgId = schedule.getOrgId();
		String recipientEmail = schedule.getRecipientEmail();
		String emailSubjectTemplate = schedule.getEmailSubject();

		Pair<List<String>, LinkedHashMap<String, List<String>>> facilityData;
		try {
			facilityData = helperService.getFacilityIdsAndOrgIdToChildrenMapPair(orgId);
		} catch (Exception e) {
			logger.warn("Failed to fetch facility IDs and hierarchy for org ID {}: {}", orgId, ExceptionUtils.getStackTrace(e));
			return;
		}

		List<String> facilityIds = facilityData.first;
		LinkedHashMap<String, List<String>> orgIdToChildrenMap = facilityData.second;

		if (facilityIds.isEmpty()) {
			logger.warn("No facilities found in FHIR for org ID: {}", orgId);
			return;
		}

		Map<String, String> orgIdToNameCache = new HashMap<>();
		List<ReportEntry> reportEntries = new ArrayList<>();
		String stateName = getValidOrganizationName(orgId, "state", orgIdToNameCache);
		if (!isValidName(stateName)) {
			logger.warn("Invalid state name for org ID: {}", orgId);
			return;
		}

		StringBuilder messageBody = new StringBuilder();
		messageBody.append(String.format("%s Facility Summary Report for %s from %s to %s",
			scheduleType.substring(0, 1).toUpperCase() + scheduleType.substring(1), stateName, formattedStartDate, formattedEndDate));

		for (String facilityId : facilityIds) {
			String facilityName = getValidOrganizationName(facilityId, "facility", orgIdToNameCache);
			if (!isValidName(facilityName)) {
				logger.warn("Skipping facility {} due to invalid name", facilityId);
				continue;
			}

			Map<String, String> hierarchy = findParentHierarchy(facilityId, orgIdToChildrenMap);
			String stateId = hierarchy.get("state");
			String lgaId = hierarchy.get("lga");
			String wardId = hierarchy.get("ward");

			String stateNameHierarchy = getValidOrganizationName(stateId, "state", orgIdToNameCache);
			String lgaName = getValidOrganizationName(lgaId, "lga", orgIdToNameCache);
			String wardName = getValidOrganizationName(wardId, "ward", orgIdToNameCache);

			if (stateNameHierarchy == null) {
				stateNameHierarchy = "N/A";
				logger.warn("No valid state name for facility ID: {}", facilityId);
			}
			if (lgaName == null) {
				lgaName = "N/A";
				logger.debug("No valid LGA name for facility ID: {}", facilityId);
			}
			if (wardName == null) {
				wardName = "N/A";
				logger.debug("No valid ward name for facility ID: {}", facilityId);
			}

			logger.warn("Facility {} hierarchy - State: {}, LGA: {}, Ward: {}",
				facilityName, stateNameHierarchy, lgaName, wardName);

			Map<String, String> indicatorValues = new LinkedHashMap<>();
			indicatorMap.values().forEach(name -> indicatorValues.put(name, "0"));

			try {
				LinkedHashMap<String, String> cleanedFilters = new LinkedHashMap<>(filters);
				cleanedFilters.remove("from");
				cleanedFilters.remove("to");
				cleanedFilters.remove("lga");
				cleanedFilters.remove("env");
				cleanedFilters.remove("type");

				ResponseEntity<?> response = helperService.processDataForReport(
					formattedStartDate, formattedEndDate, type, cleanedFilters, reportProperties.getEnv(), facilityId, isAnonymizationEnabled);
				logger.debug("Report response for facility {}: Status={}, Body={}",
					facilityName, response.getStatusCode(), response.getBody());

				if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof List) {
					@SuppressWarnings("unchecked")
					List<ScoreCardItem> scoreCardItems = (List<ScoreCardItem>) response.getBody();
					logger.debug("ScoreCardItems for facility {}: {}", facilityName, scoreCardItems);
					if (scoreCardItems != null && !scoreCardItems.isEmpty()) {
						updateIndicatorValues(scoreCardItems, indicatorMap, indicatorValues, facilityName);
					} else {
						logger.warn("No ScoreCardItems found for facility: {}", facilityName);
					}
				} else {
					logger.warn("Error response from processDataForReport for facility {}: {}", facilityName, response.getBody());
					indicatorMap.values().forEach(name -> indicatorValues.put(name, "Error: " + (response.getBody() != null ? response.getBody().toString() : "Unknown error")));
				}
			} catch (Exception e) {
				logger.warn("Failed to process facility {}: {}", facilityName, ExceptionUtils.getStackTrace(e));
				indicatorMap.values().forEach(name -> indicatorValues.put(name, "Error: " + e.getMessage()));
			}

			reportEntries.add(new ReportEntry(dateRange, stateNameHierarchy, lgaName, wardName, facilityName, indicatorValues));
		}

		if (reportEntries.isEmpty()) {
			logger.warn("No report entries generated for state: {}", stateName);
			return;
		}

		byte[] csvAttachment = csvConverter.convertReportToCSV(reportEntries);

		String emailSubject = emailSubjectTemplate
			.replace("{startDate}", formattedStartDate)
			.replace("{endDate}", formattedEndDate)
			.replace("{state}", stateName);
		String attachmentName = reportProperties.getEmailAttachmentName()
			.replace("{state}", stateName)
			.replace("{startDate}", formattedStartDate)
			.replace("{endDate}", formattedEndDate);

		if (!StringUtils.hasText(recipientEmail)) {
			logger.warn("Skipping invalid email address: {} for state: {}", recipientEmail, stateName);
			return;
		}

		try {
			rabbitTemplate.convertAndSend(
				rabbitMQProperties.getExchange().getEmail().getName(),
				rabbitMQProperties.getBinding().getEmail().getName(),
				new EmailListener.EmailDetails(
					recipientEmail,
					messageBody.toString(),
					emailSubject,
					csvAttachment,
					attachmentName
				)
			);
			logger.warn("Sent report email to {} for org ID: {}", recipientEmail, orgId);
		} catch (Exception e) {
			logger.warn("Failed to send summary email to {} for state {}: {}", recipientEmail, stateName, ExceptionUtils.getStackTrace(e));
		}
	}

	private Map<String, String> findParentHierarchy(String facilityId, LinkedHashMap<String, List<String>> orgIdToChildrenMap) {
		Map<String, String> hierarchy = new HashMap<>();
		String currentId = facilityId;
		int maxIterations = 10;

		for (int i = 0; i < maxIterations && currentId != null; i++) {
			String parentId = null;
			for (Map.Entry<String, List<String>> entry : orgIdToChildrenMap.entrySet()) {
				if (entry.getValue().contains(currentId)) {
					parentId = entry.getKey();
					break;
				}
			}
			if (parentId == null) {
				logger.debug("No parent found for ID: {}", currentId);
				break;
			}

			String parentType = helperService.getOrganizationType(parentId);
			logger.debug("Parent ID: {}, Type: {}", parentId, parentType);

			String level = mapOrgTypeToLevel(parentType);
			if (level != null) {
				hierarchy.put(level, parentId);
				logger.debug("Mapped parent ID {} to level: {}", parentId, level);
			} else {
				logger.debug("No level mapped for type: {}", parentType);
			}

			currentId = parentId;
		}

		if (!hierarchy.containsKey("state")) {
			logger.warn("No state found in hierarchy for facility ID: {}", facilityId);
		}
		if (!hierarchy.containsKey("lga")) {
			logger.warn("No LGA found in hierarchy for facility ID: {}", facilityId);
		}
		if (!hierarchy.containsKey("ward")) {
			logger.warn("No ward found in hierarchy for facility ID: {}", facilityId);
		}
		logger.debug("Hierarchy for facility {}: {}", facilityId, hierarchy);
		return hierarchy;
	}

	private String mapOrgTypeToLevel(String orgType) {
		if (orgType == null) {
			return null;
		}
		switch (orgType.toLowerCase()) {
			case "state":
			case "govt":
				return "state";
			case "lga":
				return "lga";
			case "ward":
				return "ward";
			default:
				logger.debug("Skipping organization type: {}", orgType);
				return null;
		}
	}

	private String getValidOrganizationName(String orgId, String level, Map<String, String> nameCache) {
		if (orgId == null) {
			logger.warn("Null {} ID provided", level);
			return null;
		}
		if (nameCache.containsKey(orgId)) {
			return nameCache.get(orgId);
		}
		String name = helperService.getOrganizationName(orgId);
		if (name == null || name.trim().isEmpty()) {
			logger.warn("No valid name found for {} ID: {}", level, orgId);
			return null;
		}
		nameCache.put(orgId, name);
		return name;
	}

	private boolean isValidName(String name) {
		return StringUtils.hasText(name) && !"Unknown".equalsIgnoreCase(name);
	}

	private void updateIndicatorValues(List<ScoreCardItem> scoreCardItems, Map<Integer, String> indicatorMap,
												  Map<String, String> indicatorValues, String facilityName) {
		for (Map.Entry<Integer, String> entry : indicatorMap.entrySet()) {
			int indicatorId = entry.getKey();
			String indicatorName = entry.getValue();

			ScoreCardItem item = scoreCardItems.stream()
				.filter(scoreItem -> scoreItem.getIndicatorId() == indicatorId)
				.findFirst()
				.orElse(null);

			if (item != null) {
				indicatorValues.put(indicatorName, item.getValue());
			} else {
				logger.warn("No data for indicator {} ({}) found for facility: {}", indicatorId, indicatorName, facilityName);
			}
		}
	}
}