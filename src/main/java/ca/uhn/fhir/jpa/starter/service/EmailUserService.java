package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.model.OrgHierarchy;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import com.iprd.report.model.data.ScoreCardItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailUserService {

	private final EmailService emailService;
	private final RabbitTemplate rabbitTemplate;
	private final HelperService helperService;

	@Value("${rabbitmq.exchange.email.name}")
	private String emailExchange;

	@Value("${rabbitmq.binding.email.name}")
	private String emailRoutingKey;

	@Value("${report.email}")
	private List<String> reportEmails;

	@Value("${report.env}")
	private String reportEnv;

	@Scheduled(cron = "${report.cron}")
	public void sendWeeklyFacilitySummary() {
		LocalDate today = LocalDate.now();
		LocalDate previousMonday = today.minusWeeks(1).with(DayOfWeek.MONDAY);
		LocalDate previousSunday = previousMonday.with(DayOfWeek.SUNDAY);

		String startDate = previousMonday.format(DateTimeFormatter.ISO_LOCAL_DATE);
		String endDate = previousSunday.format(DateTimeFormatter.ISO_LOCAL_DATE);

//		String startDate = "2025-04-07";
//		String endDate = "2025-04-13";
		String dateRange = startDate + " to " + endDate;

		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.put("type", "weekly");
		ReportType type = ReportType.weekly;
		boolean isAnonymizationEnabled = false;


		List<OrgHierarchy> allHierarchies;
		try {
			allHierarchies = helperService.getAllOrgHierarchies();
		} catch (Exception e) {
			log.error("Failed to fetch hierarchies: {}", ExceptionUtils.getStackTrace(e));
			return;
		}

		if (allHierarchies.isEmpty()) {
			log.error("No hierarchies found in FHIR");
			return;
		}


		List<OrgHierarchy> facilityHierarchies = allHierarchies.stream()
			.filter(h -> "facility".equals(h.getLevel()))
			.collect(Collectors.toList());

		if (facilityHierarchies.isEmpty()) {
			log.error("No facilities found in FHIR");
			return;
		}

		Map<Integer, String> indicatorMap = new LinkedHashMap<>();
		indicatorMap.put(20, "Patient Registration");
		indicatorMap.put(21, "ANC Registration");
		indicatorMap.put(22, "Schedule Appointment");
		indicatorMap.put(23, "Postnatal");
		indicatorMap.put(24, "Antenatal");
		indicatorMap.put(25, "Labour and Delivery");
		indicatorMap.put(26, "Birth Register");
		indicatorMap.put(27, "Child Immunization");
		indicatorMap.put(28, "Family Planning");
		indicatorMap.put(29, "Tetanus Diphtheria");
		indicatorMap.put(30, "Child Growth Monitoring");
		indicatorMap.put(31, "In-Patients");
		indicatorMap.put(32, "Community Immunization");
		indicatorMap.put(33, "Out-Patients");


		StringBuilder messageBody = new StringBuilder();
		List<ReportEntry> reportEntries = new ArrayList<>();
		messageBody.append(String.format("Weekly Facility Summary Report from %s to %s", startDate, endDate));

		for (OrgHierarchy facilityHierarchy : facilityHierarchies) {
			String facilityId = facilityHierarchy.getOrgId();
			String facilityName = helperService.getOrganizationName(facilityId);
			if (!StringUtils.hasText(facilityName)) {
				facilityName = facilityId;
				log.warn("No name found for facility orgId: {}", facilityId);
			}

			String wardId = facilityHierarchy.getWardParent();
			String wardName = wardId != null ? helperService.getOrganizationName(wardId) : "Unknown";
			if (!StringUtils.hasText(wardName)) {
				wardName = wardId != null ? wardId : "Unknown";
				log.warn("No name found for ward orgId: {}", wardId);
			}

			String lgaId = facilityHierarchy.getLgaParent();
			String lgaName = lgaId != null ? helperService.getOrganizationName(lgaId) : "Unknown";
			if (!StringUtils.hasText(lgaName)) {
				lgaName = lgaId != null ? lgaId : "Unknown";
				log.warn("No name found for LGA orgId: {}", lgaId);
			}

			String stateId = facilityHierarchy.getStateParent();
			String stateName = stateId != null ? helperService.getOrganizationName(stateId) : "Unknown";
			if (!StringUtils.hasText(stateName)) {
				stateName = stateId != null ? stateId : "Unknown";
				log.warn("No name found for state orgId: {}", stateId);
			}


			Map<String, String> indicatorValues = new LinkedHashMap<>();
			for (String indicatorName : indicatorMap.values()) {
				indicatorValues.put(indicatorName, "0");
			}

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
								log.warn("No data for Indicator {} ({}) found for Facility: {}", indicatorId, indicatorName, facilityName);
							}
						}
					} else {
						log.warn("No ScoreCardItems found for Facility: {}", facilityName);
					}
				} else {
					log.error("Error response from processDataForReport for Facility {}: {}", facilityName, response.getBody());
					for (String indicatorName : indicatorMap.values()) {
						indicatorValues.put(indicatorName, "Error: " + (response.getBody() != null ? response.getBody().toString() : "Unknown error"));
					}
				}
			} catch (Exception e) {
				log.error("Failed to process Facility {}: {}", facilityName, ExceptionUtils.getStackTrace(e));
				for (String indicatorName : indicatorMap.values()) {
					indicatorValues.put(indicatorName, "Error: " + e.getMessage());
				}
			}

			reportEntries.add(new ReportEntry(dateRange, stateName, lgaName, wardName, facilityName, indicatorValues));
		}

		byte[] csvAttachment = CSVConverter.convertReportToCSV(reportEntries);

		if (reportEmails == null || reportEmails.isEmpty()) {
			log.error("No valid email addresses configured in reportEmails");
			return;
		}

		for (String email : reportEmails) {
			if (!StringUtils.hasText(email)) {
				log.warn("Skipping invalid email address: {}", email);
				continue;
			}
			try {
				rabbitTemplate.convertAndSend(
					emailExchange,
					emailRoutingKey,
					new EmailService.EmailDetails(
						email,
						messageBody.toString(),
						"Weekly Facility Summary Report",
						csvAttachment,
						"Weekly_Facility_Summary_" + startDate + "_to_" + endDate + ".csv"
					)
				);
				log.info("Weekly summary email with CSV attachment sent to: {} for {} facilities", email, facilityHierarchies.size());
			} catch (Exception e) {
				log.error("Failed to send summary email to {}: {}", email, ExceptionUtils.getStackTrace(e));
			}
		}
	}
}