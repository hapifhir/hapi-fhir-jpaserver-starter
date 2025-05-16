package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailUserService {

	private static final Logger logger = LoggerFactory.getLogger(EmailUserService.class);

	private final RabbitTemplate rabbitTemplate;
	private final HelperService helperService;
	private final CSVConverter csvConverter;

	@Value("${rabbitmq.exchange.email.name}")
	private String emailExchange;

	@Value("${rabbitmq.binding.email.name}")
	private String emailRoutingKey;

	@Value("${report.email}")
	private List<String> reportEmails;

	@Value("${report.env}")
	private String reportEnv;

	@Value("${report.email-subject}")
	private String emailSubjectTemplate;

	@Value("${report.email-attachment-name}")
	private String emailAttachmentNameTemplate;

	@Value("${report.top-level-org-id}")
	private List<String> topLevelOrgIds;

	@Autowired
	private Map<String, DashboardConfigContainer> dashboardEnvToConfigMap;

	@Scheduled(cron = "${report.cron}")
	public void sendWeeklyFacilitySummary() {
		LocalDate today = LocalDate.now();
		LocalDate previousMonday = today.minusWeeks(1).with(DayOfWeek.MONDAY);
		LocalDate previousSunday = previousMonday.with(DayOfWeek.SUNDAY);

		String startDate = previousMonday.format(DateTimeFormatter.ISO_LOCAL_DATE);
		String endDate = previousSunday.format(DateTimeFormatter.ISO_LOCAL_DATE);
		String dateRange = startDate + " to " + endDate;

		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.put("type", "weekly");
		ReportType type = ReportType.weekly;
		boolean isAnonymizationEnabled = false;

		DashboardConfigContainer configContainer = dashboardEnvToConfigMap.getOrDefault(reportEnv, new DashboardConfigContainer());
		List<IndicatorColumn> indicatorColumns = configContainer.getIndicatorColumns();
		if (indicatorColumns == null || indicatorColumns.isEmpty()) {
			logger.error("No indicator columns found for environment: {}", reportEnv);
			return;
		}

		Map<Integer, String> indicatorMap = new LinkedHashMap<>();
		for (IndicatorColumn indicator : indicatorColumns) {
			indicatorMap.put(indicator.getId(), indicator.getName());
		}

		if (topLevelOrgIds == null || topLevelOrgIds.isEmpty()) {
			logger.error("No top-level organization IDs configured in report.top-level-org-id");
			return;
		}

		for (String topLevelOrgId : topLevelOrgIds) {
			Pair<List<String>, LinkedHashMap<String, List<String>>> facilityData;
			try {
				facilityData = helperService.getFacilityIdsAndOrgIdToChildrenMapPair(topLevelOrgId);
			} catch (Exception e) {
				logger.error("Failed to fetch facility IDs and hierarchy for org ID {}: {}", topLevelOrgId, ExceptionUtils.getStackTrace(e));
				continue;
			}

			List<String> facilityIds = facilityData.first;
			LinkedHashMap<String, List<String>> orgIdToChildrenMap = facilityData.second;

			if (facilityIds.isEmpty()) {
				logger.error("No facilities found in FHIR for top-level org ID: {}", topLevelOrgId);
				continue;
			}

			Map<String, String> orgIdToNameCache = new HashMap<>();
			List<ReportEntry> reportEntries = new ArrayList<>();
			String stateName = getValidOrganizationName(topLevelOrgId, "state", orgIdToNameCache);
			if (!isValidName(stateName)) {
				logger.error("Invalid state name for org ID: {}", topLevelOrgId);
				continue;
			}

			StringBuilder messageBody = new StringBuilder();
			messageBody.append(String.format("Weekly Facility Summary Report for %s from %s to %s", stateName, startDate, endDate));

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

				logger.info("Facility {} hierarchy - State: {}, LGA: {}, Ward: {}",
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
						startDate, endDate, type, cleanedFilters, reportEnv, facilityId, isAnonymizationEnabled);
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
						logger.error("Error response from processDataForReport for facility {}: {}", facilityName, response.getBody());
						indicatorMap.values().forEach(name -> indicatorValues.put(name, "Error: " + (response.getBody() != null ? response.getBody().toString() : "Unknown error")));
					}
				} catch (Exception e) {
					logger.error("Failed to process facility {}: {}", facilityName, ExceptionUtils.getStackTrace(e));
					indicatorMap.values().forEach(name -> indicatorValues.put(name, "Error: " + e.getMessage()));
				}

				reportEntries.add(new ReportEntry(dateRange, stateNameHierarchy, lgaName, wardName, facilityName, indicatorValues));
			}

			if (reportEntries.isEmpty()) {
				logger.warn("No report entries generated for state: {}", stateName);
				continue;
			}

			byte[] csvAttachment = csvConverter.convertReportToCSV(reportEntries);

			String emailSubject = emailSubjectTemplate
				.replace("{startDate}", startDate)
				.replace("{endDate}", endDate)
				.replace("{state}", stateName);
			String attachmentName = emailAttachmentNameTemplate
				.replace("{state}", stateName)
				.replace("{startDate}", startDate)
				.replace("{endDate}", endDate);

			if (reportEmails == null || reportEmails.isEmpty()) {
				logger.error("No valid email addresses configured in reportEmails for state: {}", stateName);
				continue;
			}

			for (String email : reportEmails) {
				if (!StringUtils.hasText(email)) {
					logger.warn("Skipping invalid email address: {} for state: {}", email, stateName);
					continue;
				}
				try {
					rabbitTemplate.convertAndSend(
						emailExchange,
						emailRoutingKey,
						new EmailListener.EmailDetails(
							email,
							messageBody.toString(),
							emailSubject,
							csvAttachment,
							attachmentName
						)
					);
				} catch (Exception e) {
					logger.error("Failed to send summary email to {} for state {}: {}", email, stateName, ExceptionUtils.getStackTrace(e));
				}
			}
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