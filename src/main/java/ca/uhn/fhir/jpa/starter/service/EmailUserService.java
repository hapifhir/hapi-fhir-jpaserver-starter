package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.model.IndicatorColumn;
import ca.uhn.fhir.jpa.starter.model.OrgHierarchy;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

		List<OrgHierarchy> allHierarchies;
		try {
			allHierarchies = helperService.getAllOrgHierarchies();
		} catch (Exception e) {
			logger.error("Failed to fetch Organization: {}", ExceptionUtils.getStackTrace(e));
			return;
		}

		if (allHierarchies.isEmpty()) {
			logger.error("No Organization found in FHIR");
			return;
		}

		List<OrgHierarchy> facilityHierarchies = allHierarchies.stream()
			.filter(h -> "facility".equals(h.getLevel()))
			.collect(Collectors.toList());

		if (facilityHierarchies.isEmpty()) {
			logger.error("No facilities found in FHIR");
			return;
		}

		List<ReportEntry> reportEntries = new ArrayList<>();
		StringBuilder messageBody = new StringBuilder();
		messageBody.append(String.format("Weekly Facility Summary Report from %s to %s", startDate, endDate));

		for (OrgHierarchy facilityHierarchy : facilityHierarchies) {
			String facilityId = facilityHierarchy.getOrgId();
			String facilityName = getValidOrganizationName(facilityId, "facility");
			String wardName = getValidOrganizationName(facilityHierarchy.getWardParent(), "ward");
			String lgaName = getValidOrganizationName(facilityHierarchy.getLgaParent(), "lga");
			String stateName = getValidOrganizationName(facilityHierarchy.getStateParent(), "state");

			if (!isValidName(facilityName) || !isValidName(wardName) || !isValidName(lgaName) || stateName == null || stateName.trim().isEmpty()) {
				logger.warn("Skipping facility {} due to invalid organization names: facility={}, ward={}, lga={}, state={}",
					facilityId, facilityName, wardName, lgaName, stateName);
				continue;
			}

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

				if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof List) {
					@SuppressWarnings("unchecked")
					List<ScoreCardItem> scoreCardItems = (List<ScoreCardItem>) response.getBody();
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

			reportEntries.add(new ReportEntry(dateRange, stateName, lgaName, wardName, facilityName, indicatorValues));
		}

		byte[] csvAttachment = csvConverter.convertReportToCSV(reportEntries);

		String emailSubject = emailSubjectTemplate.replace("{startDate}", startDate).replace("{endDate}", endDate);
		String attachmentName = emailAttachmentNameTemplate.replace("{startDate}", startDate).replace("{endDate}", endDate);

		if (reportEmails == null || reportEmails.isEmpty()) {
			logger.error("No valid email addresses configured in reportEmails");
			return;
		}

		for (String email : reportEmails) {
			if (!StringUtils.hasText(email)) {
				logger.warn("Skipping invalid email address: {}", email);
				continue;
			}
			try {
				rabbitTemplate.convertAndSend(
					emailExchange,
					emailRoutingKey,
					new EmailService.EmailDetails(
						email,
						messageBody.toString(),
						emailSubject,
						csvAttachment,
						attachmentName
					)
				);
				logger.info("Weekly summary email with CSV attachment sent to: {} for {} facilities", email, reportEntries.size());
			} catch (Exception e) {
				logger.error("Failed to send summary email to {}: {}", email, ExceptionUtils.getStackTrace(e));
			}
		}
	}

	private String getValidOrganizationName(String orgId, String level) {
		if (orgId == null) {
			logger.warn("Null {} ID provided", level);
			return null;
		}
		String name = helperService.getOrganizationName(orgId);
		if (name == null || name.trim().isEmpty()) {
			logger.warn("No valid name found for {} ID: {}", level, orgId);
			return null;
		}
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