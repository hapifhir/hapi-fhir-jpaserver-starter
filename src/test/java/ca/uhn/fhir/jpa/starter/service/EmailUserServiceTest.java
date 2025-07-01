package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.model.IndicatorColumn;
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
import android.util.Pair;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailUserServiceTest {

	private static final String REPORT_ENV = "V2";
	private static final String EMAIL_EXCHANGE = "email_exchange";
	private static final String EMAIL_ROUTING_KEY = "email_routing_key";
	private static final List<String> REPORT_EMAILS = Arrays.asList("sushantk@apra.in", "test@gmail.com");
	private static final String EMAIL_SUBJECT_TEMPLATE = "Weekly Facility Summary Report ({startDate} to {endDate})";
	private static final String EMAIL_ATTACHMENT_NAME_TEMPLATE = "Weekly_Facility_Summary_{startDate}_to_{endDate}.csv";
	private static final List<String> TOP_LEVEL_ORG_IDS = Arrays.asList(
		"a22e71c0-b58f-48fb-a675-93b3121899fd",
		"b33f82d1-c690-49gc-b786-04c42329a0ge"
	);
	private static final int[] INDICATOR_IDS = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33};
	private static final String START_DATE = "2025-04-21";
	private static final String END_DATE = "2025-04-26";

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

	private DashboardConfigContainer configContainer;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(emailUserService, "emailExchange", EMAIL_EXCHANGE);
		ReflectionTestUtils.setField(emailUserService, "emailRoutingKey", EMAIL_ROUTING_KEY);
		ReflectionTestUtils.setField(emailUserService, "reportEmails", REPORT_EMAILS);
		ReflectionTestUtils.setField(emailUserService, "reportEnv", REPORT_ENV);
		ReflectionTestUtils.setField(emailUserService, "emailSubjectTemplate", EMAIL_SUBJECT_TEMPLATE);
		ReflectionTestUtils.setField(emailUserService, "emailAttachmentNameTemplate", EMAIL_ATTACHMENT_NAME_TEMPLATE);
		ReflectionTestUtils.setField(emailUserService, "topLevelOrgIds", TOP_LEVEL_ORG_IDS);
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
		lenient().when(dashboardEnvToConfigMap.getOrDefault(eq(REPORT_ENV), any())).thenReturn(configContainer);
	}

	@Test
	void sendWeeklyFacilitySummary_emptyFacilityIds_noEmailsSent() {
		for (String orgId : TOP_LEVEL_ORG_IDS) {
			when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(orgId))
				.thenReturn(new Pair<>(Collections.emptyList(), new LinkedHashMap<>()));
			lenient().when(helperService.getOrganizationName(orgId)).thenReturn("Test State " + (TOP_LEVEL_ORG_IDS.indexOf(orgId) + 1));
			lenient().when(helperService.getOrganizationType(orgId)).thenReturn("state");
		}

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(2)).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, never()).processDataForReport(any(), any(), any(), any(), any(), any(), anyBoolean());
		verify(csvConverter, never()).convertReportToCSV(anyList());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
	}

	@Test
	void sendWeeklyFacilitySummary_singleFacilityInOneState_sendsEmails() {
		List<String> facilityIds = Collections.singletonList("lab_test_clinic1");
		LinkedHashMap<String, List<String>> orgIdToChildrenMap = new LinkedHashMap<>();
		orgIdToChildrenMap.put("lab_test_clinic1", Collections.emptyList());
		orgIdToChildrenMap.put("ward1", Collections.singletonList("lab_test_clinic1"));
		orgIdToChildrenMap.put("lga1", Collections.singletonList("ward1"));
		orgIdToChildrenMap.put(TOP_LEVEL_ORG_IDS.get(0), Collections.singletonList("lga1"));

		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(TOP_LEVEL_ORG_IDS.get(0)))
			.thenReturn(new Pair<>(facilityIds, orgIdToChildrenMap));
		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(TOP_LEVEL_ORG_IDS.get(1)))
			.thenReturn(new Pair<>(Collections.emptyList(), new LinkedHashMap<>()));
		setupOrganizationMocks();

		setupProcessDataForReportMocks(true, facilityIds);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(2)).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, times(1)).processDataForReport(
			eq(START_DATE), eq(END_DATE), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false));
		verify(csvConverter, times(1)).convertReportToCSV(anyList());
		verify(rabbitTemplate, times(2)).convertAndSend(
			eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailListener.EmailDetails.class));
	}

	@Test
	void sendWeeklyFacilitySummary_emptyScoreCardItems_sendsEmailsWithDefaultValues() {
		List<String> facilityIds = Arrays.asList("lab_test_clinic1", "lab_test_clinic2", "lab_test_clinic3");
		setupFacilityAndHierarchyMocks(false, TOP_LEVEL_ORG_IDS, facilityIds);
		setupProcessDataForReportMocks(false, facilityIds);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(2)).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, times(3)).processDataForReport(
			eq(START_DATE), eq(END_DATE), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), anyString(), eq(false));
		verify(csvConverter, times(2)).convertReportToCSV(anyList());
		verify(rabbitTemplate, times(4)).convertAndSend(
			eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailListener.EmailDetails.class));
	}

	@Test
	void sendWeeklyFacilitySummary_emptyReportEmails_noEmailsSent() {
		ReflectionTestUtils.setField(emailUserService, "reportEmails", Collections.emptyList());
		List<String> facilityIds = Arrays.asList("lab_test_clinic1", "lab_test_clinic2", "lab_test_clinic3");
		setupFacilityAndHierarchyMocks(true, TOP_LEVEL_ORG_IDS, facilityIds);
		setupProcessDataForReportMocks(true, facilityIds);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(2)).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, times(3)).processDataForReport(
			eq(START_DATE), eq(END_DATE), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), anyString(), eq(false));
		verify(csvConverter, times(2)).convertReportToCSV(anyList());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
	}

	@Test
	void sendWeeklyFacilitySummary_emptyTopLevelOrgIds_noProcessing() {
		ReflectionTestUtils.setField(emailUserService, "topLevelOrgIds", Collections.emptyList());

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, never()).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, never()).processDataForReport(any(), any(), any(), any(), any(), any(), anyBoolean());
		verify(csvConverter, never()).convertReportToCSV(anyList());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
	}

	@Test
	void sendWeeklyFacilitySummary_invalidStateName_skipsState() {
		List<String> facilityIds = Arrays.asList("lab_test_clinic1", "lab_test_clinic2");
		LinkedHashMap<String, List<String>> orgIdToChildrenMap = new LinkedHashMap<>();
		orgIdToChildrenMap.put("lab_test_clinic1", Collections.emptyList());
		orgIdToChildrenMap.put("lab_test_clinic2", Collections.emptyList());
		orgIdToChildrenMap.put("ward1", Collections.singletonList("lab_test_clinic1"));
		orgIdToChildrenMap.put("ward2", Collections.singletonList("lab_test_clinic2"));
		orgIdToChildrenMap.put("lga1", Arrays.asList("ward1", "ward2"));
		orgIdToChildrenMap.put(TOP_LEVEL_ORG_IDS.get(0), Collections.singletonList("lga1"));

		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(TOP_LEVEL_ORG_IDS.get(0)))
			.thenReturn(new Pair<>(facilityIds, orgIdToChildrenMap));
		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(TOP_LEVEL_ORG_IDS.get(1)))
			.thenReturn(new Pair<>(Collections.emptyList(), new LinkedHashMap<>()));
		lenient().when(helperService.getOrganizationName("lab_test_clinic1")).thenReturn("Test Clinic 1");
		lenient().when(helperService.getOrganizationType("lab_test_clinic1")).thenReturn("facility");
		lenient().when(helperService.getOrganizationName("lab_test_clinic2")).thenReturn("Test Clinic 2");
		lenient().when(helperService.getOrganizationType("lab_test_clinic2")).thenReturn("facility");
		lenient().when(helperService.getOrganizationName("ward1")).thenReturn("Test Ward 1");
		lenient().when(helperService.getOrganizationType("ward1")).thenReturn("ward");
		lenient().when(helperService.getOrganizationName("ward2")).thenReturn("Test Ward 2");
		lenient().when(helperService.getOrganizationType("ward2")).thenReturn("ward");
		lenient().when(helperService.getOrganizationName("lga1")).thenReturn("Test LGA 1");
		lenient().when(helperService.getOrganizationType("lga1")).thenReturn("lga");
		lenient().when(helperService.getOrganizationName(TOP_LEVEL_ORG_IDS.get(0))).thenReturn("Unknown");
		lenient().when(helperService.getOrganizationType(TOP_LEVEL_ORG_IDS.get(0))).thenReturn("state");
		lenient().when(helperService.getOrganizationName(TOP_LEVEL_ORG_IDS.get(1))).thenReturn("Test State 2");
		lenient().when(helperService.getOrganizationType(TOP_LEVEL_ORG_IDS.get(1))).thenReturn("state");

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(2)).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, never()).processDataForReport(any(), any(), any(), any(), any(), any(), anyBoolean());
		verify(csvConverter, never()).convertReportToCSV(anyList());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
	}


	@Test
	void sendWeeklyFacilitySummary_noIndicatorColumns_noProcessing() {
		configContainer.setIndicatorColumns(Collections.emptyList());
		when(dashboardEnvToConfigMap.getOrDefault(eq(REPORT_ENV), any())).thenReturn(configContainer);

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, never()).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, never()).processDataForReport(any(), any(), any(), any(), any(), any(), anyBoolean());
		verify(csvConverter, never()).convertReportToCSV(anyList());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
	}

	@Test
	void sendWeeklyFacilitySummary_invalidEmailAddress_skipsEmail() {
		List<String> facilityIds = Collections.singletonList("lab_test_clinic1");
		LinkedHashMap<String, List<String>> orgIdToChildrenMap = new LinkedHashMap<>();
		orgIdToChildrenMap.put("lab_test_clinic1", Collections.emptyList());
		orgIdToChildrenMap.put("ward1", Collections.singletonList("lab_test_clinic1"));
		orgIdToChildrenMap.put("lga1", Collections.singletonList("ward1"));
		orgIdToChildrenMap.put(TOP_LEVEL_ORG_IDS.get(0), Collections.singletonList("lga1"));

		ReflectionTestUtils.setField(emailUserService, "reportEmails", Arrays.asList("sushantk@apra.in", ""));

		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(TOP_LEVEL_ORG_IDS.get(0)))
			.thenReturn(new Pair<>(facilityIds, orgIdToChildrenMap));
		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(TOP_LEVEL_ORG_IDS.get(1)))
			.thenReturn(new Pair<>(Collections.emptyList(), new LinkedHashMap<>()));
		setupOrganizationMocks();

		setupProcessDataForReportMocks(true, facilityIds);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(2)).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, times(1)).processDataForReport(
			eq(START_DATE), eq(END_DATE), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false));
		verify(csvConverter, times(1)).convertReportToCSV(anyList());
		verify(rabbitTemplate, times(1)).convertAndSend(
			eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailListener.EmailDetails.class));
	}

	@Test
	void sendWeeklyFacilitySummary_nullHierarchyLevels_usesDefaultNA() {
		List<String> facilityIds = Collections.singletonList("lab_test_clinic1");
		LinkedHashMap<String, List<String>> orgIdToChildrenMap = new LinkedHashMap<>();
		orgIdToChildrenMap.put("lab_test_clinic1", Collections.emptyList());
		orgIdToChildrenMap.put(TOP_LEVEL_ORG_IDS.get(0), Collections.singletonList("lab_test_clinic1"));

		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(TOP_LEVEL_ORG_IDS.get(0)))
			.thenReturn(new Pair<>(facilityIds, orgIdToChildrenMap));
		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(TOP_LEVEL_ORG_IDS.get(1)))
			.thenReturn(new Pair<>(Collections.emptyList(), new LinkedHashMap<>()));
		lenient().when(helperService.getOrganizationName("lab_test_clinic1")).thenReturn("Test Clinic 1");
		lenient().when(helperService.getOrganizationType("lab_test_clinic1")).thenReturn("facility");
		lenient().when(helperService.getOrganizationName(TOP_LEVEL_ORG_IDS.get(0))).thenReturn("Test State 1");
		lenient().when(helperService.getOrganizationType(TOP_LEVEL_ORG_IDS.get(0))).thenReturn("state");
		lenient().when(helperService.getOrganizationName(TOP_LEVEL_ORG_IDS.get(1))).thenReturn("Test State 2");
		lenient().when(helperService.getOrganizationType(TOP_LEVEL_ORG_IDS.get(1))).thenReturn("state");

		setupProcessDataForReportMocks(true, facilityIds);
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(new byte[]{1, 2, 3});

		//assertDoesNotThrow(() -> emailUserService.sendWeeklyFacilitySummary());

		verify(helperService, times(2)).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		verify(helperService, times(1)).processDataForReport(
			eq(START_DATE), eq(END_DATE), eq(ReportType.weekly), any(LinkedHashMap.class),
			eq(REPORT_ENV), eq("lab_test_clinic1"), eq(false));
		verify(csvConverter, times(1)).convertReportToCSV(anyList());
		verify(rabbitTemplate, times(2)).convertAndSend(
			eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailListener.EmailDetails.class));
	}

	private void setupOrganizationMocks() {
		lenient().when(helperService.getOrganizationName("lab_test_clinic1")).thenReturn("Test Clinic 1");
		lenient().when(helperService.getOrganizationType("lab_test_clinic1")).thenReturn("facility");
		lenient().when(helperService.getOrganizationName("lab_test_clinic2")).thenReturn("Test Clinic 2");
		lenient().when(helperService.getOrganizationType("lab_test_clinic2")).thenReturn("facility");
		lenient().when(helperService.getOrganizationName("lab_test_clinic3")).thenReturn("Test Clinic 3");
		lenient().when(helperService.getOrganizationType("lab_test_clinic3")).thenReturn("facility");
		lenient().when(helperService.getOrganizationName("ward1")).thenReturn("Test Ward 1");
		lenient().when(helperService.getOrganizationType("ward1")).thenReturn("ward");
		lenient().when(helperService.getOrganizationName("ward2")).thenReturn("Test Ward 2");
		lenient().when(helperService.getOrganizationType("ward2")).thenReturn("ward");
		lenient().when(helperService.getOrganizationName("ward3")).thenReturn("Test Ward 3");
		lenient().when(helperService.getOrganizationType("ward3")).thenReturn("ward");
		lenient().when(helperService.getOrganizationName("lga1")).thenReturn("Test LGA 1");
		lenient().when(helperService.getOrganizationType("lga1")).thenReturn("lga");
		lenient().when(helperService.getOrganizationName("lga2")).thenReturn("Test LGA 2");
		lenient().when(helperService.getOrganizationType("lga2")).thenReturn("lga");
		lenient().when(helperService.getOrganizationName(TOP_LEVEL_ORG_IDS.get(0))).thenReturn("Test State 1");
		lenient().when(helperService.getOrganizationType(TOP_LEVEL_ORG_IDS.get(0))).thenReturn("state");
		lenient().when(helperService.getOrganizationName(TOP_LEVEL_ORG_IDS.get(1))).thenReturn("Test State 2");
		lenient().when(helperService.getOrganizationType(TOP_LEVEL_ORG_IDS.get(1))).thenReturn("state");
	}

	private void setupFacilityAndHierarchyMocks(boolean useCsvValues, List<String> topLevelOrgIds, List<String> facilityIds) {
		for (int i = 0; i < topLevelOrgIds.size(); i++) {
			String orgId = topLevelOrgIds.get(i);
			List<String> stateFacilityIds = i == 0 ?
				facilityIds.subList(0, Math.min(2, facilityIds.size())) :
				facilityIds.subList(Math.min(2, facilityIds.size()), facilityIds.size());
			LinkedHashMap<String, List<String>> orgIdToChildrenMap = new LinkedHashMap<>();
			List<String> wardIds = new ArrayList<>();
			for (String facilityId : stateFacilityIds) {
				String wardId = "ward" + (facilityIds.indexOf(facilityId) + 1);
				orgIdToChildrenMap.put(facilityId, Collections.emptyList());
				orgIdToChildrenMap.put(wardId, Collections.singletonList(facilityId));
				wardIds.add(wardId);
			}
			String lgaId = "lga" + (i + 1);
			orgIdToChildrenMap.put(lgaId, wardIds);
			orgIdToChildrenMap.put(orgId, Collections.singletonList(lgaId));
			when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(orgId))
				.thenReturn(new Pair<>(stateFacilityIds, orgIdToChildrenMap));
		}
		setupOrganizationMocks();
	}

	private void setupProcessDataForReportMocks(boolean useCsvValues, List<String> facilityIds) {
		for (String orgId : facilityIds) {
			Map<Integer, String> indicatorValues = getIndicatorValuesForFacility(orgId, useCsvValues);
			List<ScoreCardItem> items = useCsvValues ? createScoreCardItems(orgId, indicatorValues) : Collections.emptyList();
			lenient().when(helperService.processDataForReport(
					eq(START_DATE), eq(END_DATE), eq(ReportType.weekly), any(LinkedHashMap.class),
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