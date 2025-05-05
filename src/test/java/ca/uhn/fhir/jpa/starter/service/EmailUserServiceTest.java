package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.model.IndicatorColumn;
import ca.uhn.fhir.jpa.starter.model.OrgHierarchy;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import com.iprd.report.model.data.ScoreCardItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Optional;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyBoolean;

@ExtendWith(MockitoExtension.class)
class EmailUserServiceTest {

	private static final String REPORT_ENV = "V2";
	private static final String EMAIL_EXCHANGE = "email_exchange";
	private static final String EMAIL_ROUTING_KEY = "email_routing_key";
	private static final List<String> REPORT_EMAILS = Arrays.asList("sushantk@apra.in","sushant@apra.in");
	private static final String EMAIL_SUBJECT_TEMPLATE = "Weekly Facility Summary Report ({startDate} to {endDate})";
	private static final String EMAIL_ATTACHMENT_NAME_TEMPLATE = "Weekly_Facility_Summary_{startDate}_to_{endDate}.csv";
	private static final int[] INDICATOR_IDS = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33};

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Mock
	private HelperService helperService;

	@Mock
	private CSVConverter csvConverter;

	@Mock
	private Map<String, DashboardConfigContainer> dashboardEnvToConfigMap;

	@InjectMocks
	private EmailUserService emailUserService;

	private String startDate;
	private String endDate;
	private DashboardConfigContainer configContainer;

	@BeforeEach
	void setUp() {
		LocalDate today = LocalDate.now();
		LocalDate previousMonday = today.minusWeeks(1).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
		LocalDate previousSunday = previousMonday.plusDays(6);
		startDate = previousMonday.format(DateTimeFormatter.ISO_LOCAL_DATE);
		endDate = previousSunday.format(DateTimeFormatter.ISO_LOCAL_DATE);

		ReflectionTestUtils.setField(emailUserService, "emailExchange", EMAIL_EXCHANGE);
		ReflectionTestUtils.setField(emailUserService, "emailRoutingKey", EMAIL_ROUTING_KEY);
		ReflectionTestUtils.setField(emailUserService, "reportEmails", REPORT_EMAILS);
		ReflectionTestUtils.setField(emailUserService, "reportEnv", REPORT_ENV);
		ReflectionTestUtils.setField(emailUserService, "emailSubjectTemplate", EMAIL_SUBJECT_TEMPLATE);
		ReflectionTestUtils.setField(emailUserService, "emailAttachmentNameTemplate", EMAIL_ATTACHMENT_NAME_TEMPLATE);
		ReflectionTestUtils.setField(emailUserService, "dashboardEnvToConfigMap", dashboardEnvToConfigMap);

		configContainer = new DashboardConfigContainer();
		List<IndicatorColumn> indicatorColumns = new ArrayList<>();
		for (int id : INDICATOR_IDS) {
			IndicatorColumn column = new IndicatorColumn();
			column.setId(id);
			column.setName("Indicator_" + id);
			indicatorColumns.add(column);
		}
		configContainer.setIndicatorColumns(indicatorColumns);
		when(dashboardEnvToConfigMap.getOrDefault(eq(REPORT_ENV), any())).thenReturn(configContainer);
	}

	@Test
	void sendWeeklyFacilitySummary_successfulExecution_sendsEmails() throws Exception {
		List<OrgHierarchy> orgHierarchies = createOrgHierarchies();
		when(helperService.getAllOrgHierarchies()).thenReturn(orgHierarchies);
		setupOrganizationNameMocks(orgHierarchies);
		setupProcessDataForReportMocks(orgHierarchies, true);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(1)).getAllOrgHierarchies();
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false));
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic2"), eq(false));
		verify(csvConverter, times(1)).convertReportToCSV(anyList());
		verify(rabbitTemplate, times(2)).convertAndSend(
			eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailService.EmailDetails.class));
	}

	@Test
	void sendWeeklyFacilitySummary_emptyOrgHierarchies_noEmailsSent() throws Exception {
		when(helperService.getAllOrgHierarchies()).thenReturn(Collections.emptyList());

		assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(1)).getAllOrgHierarchies();
		verify(helperService, never()).processDataForReport(anyString(), anyString(), any(), any(), anyString(), anyString(), anyBoolean());
		verify(csvConverter, never()).convertReportToCSV(anyList());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
	}

	@Test
	void sendWeeklyFacilitySummary_singleFacility_sendsEmails() throws Exception {
		List<OrgHierarchy> orgHierarchies = Collections.singletonList(createOrgHierarchies().get(0));
		when(helperService.getAllOrgHierarchies()).thenReturn(orgHierarchies);
		setupOrganizationNameMocks(orgHierarchies);
		setupProcessDataForReportMocks(orgHierarchies, true);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(1)).getAllOrgHierarchies();
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false));
		verify(helperService, never()).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic2"), eq(false));
		verify(csvConverter, times(1)).convertReportToCSV(anyList());
		verify(rabbitTemplate, times(2)).convertAndSend(
			eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailService.EmailDetails.class));
	}

	@Test
	void sendWeeklyFacilitySummary_emptyScoreCardItems_sendsEmailsWithDefaultValues() throws Exception {
		List<OrgHierarchy> orgHierarchies = createOrgHierarchies();
		when(helperService.getAllOrgHierarchies()).thenReturn(orgHierarchies);
		setupOrganizationNameMocks(orgHierarchies);
		setupProcessDataForReportMocks(orgHierarchies, false);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(1)).getAllOrgHierarchies();
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false));
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic2"), eq(false));
		verify(csvConverter, times(1)).convertReportToCSV(anyList());
		verify(rabbitTemplate, times(2)).convertAndSend(
			eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailService.EmailDetails.class));
	}

	@Test
	void sendWeeklyFacilitySummary_processDataForReportFails_logsErrorAndContinues() throws Exception {
		List<OrgHierarchy> orgHierarchies = createOrgHierarchies();
		when(helperService.getAllOrgHierarchies()).thenReturn(orgHierarchies);
		setupOrganizationNameMocks(orgHierarchies);
		when(helperService.processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false)))
			.thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
		when(helperService.processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic2"), eq(false)))
			.thenReturn(new ResponseEntity<>(createScoreCardItems("lab_test_clinic2", getIndicatorValuesForFacility("lab_test_clinic2", true)), HttpStatus.OK));
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(1)).getAllOrgHierarchies();
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false));
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic2"), eq(false));
		verify(csvConverter, times(1)).convertReportToCSV(anyList());
		verify(rabbitTemplate, times(2)).convertAndSend(
			eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailService.EmailDetails.class));
	}

	@Test
	void sendWeeklyFacilitySummary_emptyReportEmails_noEmailsSent() throws Exception {
		ReflectionTestUtils.setField(emailUserService, "reportEmails", Collections.emptyList());
		List<OrgHierarchy> orgHierarchies = createOrgHierarchies();
		when(helperService.getAllOrgHierarchies()).thenReturn(orgHierarchies);
		setupOrganizationNameMocks(orgHierarchies);
		setupProcessDataForReportMocks(orgHierarchies, true);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(1)).getAllOrgHierarchies();
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false));
		verify(helperService, times(1)).processDataForReport(
			eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic2"), eq(false));
		verify(csvConverter, times(1)).convertReportToCSV(anyList());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
	}

	private List<OrgHierarchy> createOrgHierarchies() {
		OrgHierarchy org1 = new OrgHierarchy();
		org1.setOrgId("lab_test_clinic1");
		org1.setLevel("facility");
		org1.setWardParent("Test_ward1");
		org1.setLgaParent("Test_lga1");
		org1.setStateParent("Test_state");

		OrgHierarchy org2 = new OrgHierarchy();
		org2.setOrgId("lab_test_clinic2");
		org2.setLevel("facility");
		org2.setWardParent("Test_ward2");
		org2.setLgaParent("Test_lga2");
		org2.setStateParent("Test_state");

		return Arrays.asList(org1, org2);
	}

	private void setupOrganizationNameMocks(List<OrgHierarchy> orgHierarchies) {
		for (OrgHierarchy org : orgHierarchies) {
			when(helperService.getOrganizationName(org.getOrgId())).thenReturn("Test Clinic " + org.getOrgId().substring(org.getOrgId().length() - 1));
			when(helperService.getOrganizationName(org.getWardParent())).thenReturn("Test Ward " + org.getWardParent().substring(org.getWardParent().length() - 1));
			when(helperService.getOrganizationName(org.getLgaParent())).thenReturn("Test LGA " + org.getLgaParent().substring(org.getLgaParent().length() - 1));
			when(helperService.getOrganizationName(org.getStateParent())).thenReturn("Test State");
		}
	}

	private void setupProcessDataForReportMocks(List<OrgHierarchy> orgHierarchies, boolean useCsvValues) {
		for (OrgHierarchy org : orgHierarchies) {
			String orgId = org.getOrgId();
			Map<Integer, String> indicatorValues = getIndicatorValuesForFacility(orgId, useCsvValues);
			List<ScoreCardItem> items = useCsvValues ? createScoreCardItems(orgId, indicatorValues) : Collections.emptyList();
			when(helperService.processDataForReport(
				eq(startDate), eq(endDate), eq(ReportType.weekly), any(LinkedHashMap.class),
				eq(REPORT_ENV), eq(orgId), eq(false)))
				.thenReturn(new ResponseEntity<>(items, HttpStatus.OK));
		}
	}

	private Map<Integer, String> getIndicatorValuesForFacility(String orgId, boolean useCsvValues) {
		Map<Integer, String> values = new HashMap<>();
		if (useCsvValues && "lab_test_clinic1".equals(orgId)) {
			String[] csvValues = {"4.0", "3.0", "2.0", "2.0", "4.0", "2.0", "1.0", "2.0", "1.0", "5.0", "2.0", "1.0", "0.0", "0.0"};
			for (int i = 0; i < INDICATOR_IDS.length; i++) {
				values.put(INDICATOR_IDS[i], csvValues[i]);
			}
		} else {
			for (int id : INDICATOR_IDS) {
				values.put(id, "0");
			}
		}
		return values;
	}

	private List<ScoreCardItem> createScoreCardItems(String orgId, Map<Integer, String> indicatorValues) {
		List<ScoreCardItem> items = new ArrayList<>();
		for (Map.Entry<Integer, String> entry : indicatorValues.entrySet()) {
			items.add(new ScoreCardItem(orgId, entry.getKey(), entry.getValue(), null, null));
		}
		return items;
	}
}