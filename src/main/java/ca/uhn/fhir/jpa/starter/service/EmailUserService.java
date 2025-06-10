package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.RabbitMQProperties;
import ca.uhn.fhir.jpa.starter.ReportProperties;
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

/**
 * Service class responsible for generating and sending weekly facility summary reports via email.
 * This service retrieves facility data, processes it into reports, converts the reports to CSV format,
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

	@Autowired
	private ReportProperties reportProperties;

	@Autowired
	private RabbitMQProperties rabbitMQProperties;

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

		DashboardConfigContainer configContainer = dashboardEnvToConfigMap.getOrDefault(reportProperties.getEnv(), new DashboardConfigContainer());
		List<IndicatorColumn> indicatorColumns = configContainer.getIndicatorColumns();
		if (indicatorColumns == null || indicatorColumns.isEmpty()) {
			logger.error("No indicator columns found for environment: {}", reportProperties.getEnv());
			return;
		}

		Map<Integer, String> indicatorMap = new LinkedHashMap<>();
		for (IndicatorColumn indicator : indicatorColumns) {
			indicatorMap.put(indicator.getId(), indicator.getName());
		}

		List<String> topLevelOrgIds = reportProperties.getTopLevelOrgId();
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
						startDate, endDate, type, cleanedFilters, reportProperties.getEnv(), facilityId, isAnonymizationEnabled);
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

			String emailSubject = reportProperties.getEmailSubject()
				.replace("{startDate}", startDate)
				.replace("{endDate}", endDate)
				.replace("{state}", stateName);
			String attachmentName = reportProperties.getEmailAttachmentName()
				.replace("{state}", stateName)
				.replace("{startDate}", startDate)
				.replace("{endDate}", endDate);

			List<String> reportEmails = reportProperties.getEmail();
			if (reportEmails == null || reportEmails.isEmpty()) {
				logger.error("No valid email addresses configured in report.email for state: {}", stateName);
				continue;
			}

			for (String email : reportEmails) {
				if (!StringUtils.hasText(email)) {
					logger.warn("Skipping invalid email address: {} for state: {}", email, stateName);
					continue;
				}
				try {
					rabbitTemplate.convertAndSend(
						rabbitMQProperties.getExchange().getEmail().getName(),
						rabbitMQProperties.getBinding().getEmail().getName(),
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

	/**
	 * Finds the parent hierarchy for a given facility ID, mapping parent organization IDs to their levels (state, LGA, ward).
	 *
	 * @param facilityId         the ID of the facility to find the hierarchy for
	 * @param orgIdToChildrenMap a map of organization IDs to their child organization IDs
	 * @return a map containing the hierarchy levels (state, lga, ward) mapped to their respective organization IDs
	 */
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

	/**
	 * Maps an organization type to its corresponding hierarchy level.
	 *
	 * @param orgType the organization type (e.g., state, lga, ward)
	 * @return the hierarchy level (state, lga, ward) or null if the type is not recognized
	 */
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

	/**
	 * Retrieves a valid organization name for a given organization ID, caching the result.
	 *
	 * @param orgId      the ID of the organization
	 * @param level      the hierarchy level of the organization (e.g., state, lga, ward)
	 * @param nameCache  a cache mapping organization IDs to their names
	 * @return the organization name, or null if the name is invalid or not found
	 */
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

	/**
	 * Updates indicator values for a facility based on score card items.
	 *
	 * @param scoreCardItems   the list of score card items containing indicator data
	 * @param indicatorMap     a map of indicator IDs to their names
	 * @param indicatorValues  a map to store indicator names and their values
	 * @param facilityName     the name of the facility
	 */
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