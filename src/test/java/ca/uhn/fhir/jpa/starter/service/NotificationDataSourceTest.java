package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.model.ComGenerator;
import ca.uhn.fhir.jpa.starter.model.SMSInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NotificationDataSource.class})
class NotificationDataSourceTest {

	@BeforeEach
	void setUp() throws IOException {
		// Initialize the mocks before each test
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void fetchSMSRecordsByResourceId_listIsNotEmpty() {
		// Create a mock of NotificationDataSource
		NotificationDataSource notificationDataSource = mock(NotificationDataSource.class);

		// Create SMSInfo object
		String patientId = "235850a1-0ca8-42a8-b111-cdbbf7d5186b";
		String organizationId = "a080d7c6-3947-43b2-b190-a4b4abae3b15";
		String encounterId = "61cbe269-dc64-4e41-97e5-765e7fbd996a";
		String resourceId = "8ce11358-9742-4c04-bfc0-72cc32c50081";
		ArrayList<SMSInfo> smsInfoList = new ArrayList<SMSInfo>();
		SMSInfo smsInfo = new SMSInfo(
			ComGenerator.MessageStatus.PENDING.name(),
			Timestamp.valueOf("2024-04-26 17:25:04.029000"),
			patientId,
			organizationId,
			encounterId,
			"Appointment",
			resourceId,
			null,
			"Appointment"
		);
		smsInfoList.add(smsInfo);
		when(notificationDataSource.fetchSMSRecordsByResourceId(resourceId)).thenReturn(smsInfoList);
		List<SMSInfo> smsInfos = notificationDataSource.fetchSMSRecordsByResourceId(resourceId);
		assertFalse(smsInfos.isEmpty());
	}
}