package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.RabbitMQProperties;
import ca.uhn.fhir.jpa.starter.ReportProperties;
import ca.uhn.fhir.jpa.starter.model.EmailScheduleData;
import ca.uhn.fhir.jpa.starter.model.IndicatorColumn;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import com.iprd.report.model.data.ScoreCardItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;
import android.util.Pair;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailUserServiceTest {

	@InjectMocks
	private EmailUserService emailUserService;

	@Mock private RabbitTemplate rabbitTemplate;
	@Mock private HelperService helperService;
	@Mock private CSVConverter csvConverter;
	@Mock private TaskScheduler taskScheduler; // Mocked but not used in this specific test
	@Mock private ReportProperties reportProperties;
	@Mock private RabbitMQProperties rabbitMQProperties;
	@Mock private Map<String, DashboardConfigContainer> dashboardEnvToConfigMap;
	@Mock private DashboardConfigContainer dashboardConfigContainer;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(emailUserService, "reportProperties", reportProperties);
		ReflectionTestUtils.setField(emailUserService, "rabbitMQProperties", rabbitMQProperties);
		ReflectionTestUtils.setField(emailUserService, "dashboardEnvToConfigMap", dashboardEnvToConfigMap);
	}

	@Test
	void testSendFacilitySummaryForSchedule_SuccessPath() {
		EmailScheduleData schedule = new EmailScheduleData(1, "0f0428b1-ed9e-43b0-b556-0ff952ddd294", "test1@example.com", "Daily Report for {state}", "daily", new Timestamp(new Date().getTime()));
		when(reportProperties.getEnv()).thenReturn("test-env");
		when(reportProperties.getEmailAttachmentName()).thenReturn("report-{state}-{startDate}-to-{endDate}.csv");

		RabbitMQProperties.Exchange exchangeMock = mock(RabbitMQProperties.Exchange.class);
		RabbitMQProperties.Exchange.Email emailExchangeMock = mock(RabbitMQProperties.Exchange.Email.class);
		RabbitMQProperties.Binding bindingMock = mock(RabbitMQProperties.Binding.class);
		RabbitMQProperties.Binding.Email emailBindingMock = mock(RabbitMQProperties.Binding.Email.class);

		when(emailExchangeMock.getName()).thenReturn("test-exchange");
		when(exchangeMock.getEmail()).thenReturn(emailExchangeMock);
		when(emailBindingMock.getName()).thenReturn("test-binding");
		when(bindingMock.getEmail()).thenReturn(emailBindingMock);
		when(rabbitMQProperties.getExchange()).thenReturn(exchangeMock);
		when(rabbitMQProperties.getBinding()).thenReturn(bindingMock);

		when(dashboardEnvToConfigMap.getOrDefault(eq("test-env"), any())).thenReturn(dashboardConfigContainer);
		List<IndicatorColumn> indicatorColumns = Arrays.asList(
			new IndicatorColumn(20, "Patient Registration"),
			new IndicatorColumn(21, "ANC Registration"),
			new IndicatorColumn(33, "Out-Patients")
		);
		when(dashboardConfigContainer.getIndicatorColumns()).thenReturn(indicatorColumns);

		String stateId = "0f0428b1-ed9e-43b0-b556-0ff952ddd294";
		String lgaId = "5fc25a72-5b81-48a8-ae8e-8d781963c9ad";
		String wardId = "e7728af9-a5c3-40e8-8abd-505534e69b64";
		String facilityId = "8b37ee67-6702-434b-8005-e90e2ef94b8d";

		List<String> facilityIds = Collections.singletonList(facilityId);
		LinkedHashMap<String, List<String>> orgIdToChildrenMap = new LinkedHashMap<>();
		orgIdToChildrenMap.put(wardId, Collections.singletonList(facilityId));
		orgIdToChildrenMap.put(lgaId, Collections.singletonList(wardId));
		orgIdToChildrenMap.put(stateId, Collections.singletonList(lgaId));

		Pair<List<String>, LinkedHashMap<String, List<String>>> facilityData = new Pair<>(facilityIds, orgIdToChildrenMap);
		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(stateId)).thenReturn(facilityData);

		when(helperService.getOrganizationName(stateId)).thenReturn("Karnataka");
		when(helperService.getOrganizationName(lgaId)).thenReturn("Bangalore Urban");
		when(helperService.getOrganizationName(wardId)).thenReturn("Whitefield");
		when(helperService.getOrganizationName(facilityId)).thenReturn("City Health Hub");

		when(helperService.getOrganizationType(stateId)).thenReturn("state");
		when(helperService.getOrganizationType(lgaId)).thenReturn("lga");
		when(helperService.getOrganizationType(wardId)).thenReturn("ward");

		List<ScoreCardItem> scoreCardItems = Arrays.asList(
			new ScoreCardItem(facilityId, 20, "55", null, null),
			new ScoreCardItem(facilityId, 21, "12", null, null),
			new ScoreCardItem(facilityId, 33, "88", null, null)
		);
		ResponseEntity<List<ScoreCardItem>> reportResponse = new ResponseEntity<>(scoreCardItems, HttpStatus.OK);
		when(helperService.processDataForReport(anyString(), anyString(), eq(ReportType.daily), any(LinkedHashMap.class), eq("test-env"), eq(facilityId), anyBoolean()))
			.thenReturn(reportResponse);

		byte[] csvBytes = "csv,data".getBytes();
		when(csvConverter.convertReportToCSV(anyList())).thenReturn(csvBytes);

		assertDoesNotThrow(() -> {
			ReflectionTestUtils.invokeMethod(emailUserService, "sendFacilitySummaryForSchedule", schedule);
		});

		ArgumentCaptor<EmailListener.EmailDetails> emailDetailsCaptor = ArgumentCaptor.forClass(EmailListener.EmailDetails.class);
		verify(rabbitTemplate, times(1)).convertAndSend(
			eq("test-exchange"),
			eq("test-binding"),
			emailDetailsCaptor.capture()
		);

		EmailListener.EmailDetails capturedDetails = emailDetailsCaptor.getValue();
		assertEquals("test1@example.com", capturedDetails.getRecipient());
		assertEquals("Daily Report for Karnataka", capturedDetails.getSubject());
	}

	@Test
	void testSendFacilitySummary_NoFacilitiesFound() {
		EmailScheduleData schedule = new EmailScheduleData(2, "state-org-2", "test2@example.com", "Daily Report", "daily", new Timestamp(new Date().getTime()));

		Pair<List<String>, LinkedHashMap<String, List<String>>> emptyFacilityData = new Pair<>(Collections.emptyList(), new LinkedHashMap<>());
		when(helperService.getFacilityIdsAndOrgIdToChildrenMapPair(schedule.getOrgId())).thenReturn(emptyFacilityData);

		when(reportProperties.getEnv()).thenReturn("test-env");
		when(dashboardEnvToConfigMap.getOrDefault(anyString(), any())).thenReturn(dashboardConfigContainer);
		when(dashboardConfigContainer.getIndicatorColumns()).thenReturn(Collections.singletonList(new IndicatorColumn(1, "Test")));

		assertDoesNotThrow(() -> {
			ReflectionTestUtils.invokeMethod(emailUserService, "sendFacilitySummaryForSchedule", schedule);
		});

		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(EmailListener.EmailDetails.class));
		verify(csvConverter, never()).convertReportToCSV(anyList());
	}
}
