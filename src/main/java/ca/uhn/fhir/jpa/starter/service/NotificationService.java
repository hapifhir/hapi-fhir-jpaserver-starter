package ca.uhn.fhir.jpa.starter.service;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import autovalue.shaded.kotlin.Pair;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.model.SMSInfo;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.FhirUtils;
import autovalue.shaded.kotlin.Triple;
import ca.uhn.fhir.jpa.starter.model.ComGenerator;
import ca.uhn.fhir.jpa.starter.model.ComGenerator.MessageStatus;
import okhttp3.OkHttpClient;
import java.sql.Timestamp;
import java.util.Objects;

@Service
public class NotificationService {
	
	@Autowired
	FhirClientAuthenticatorService fhirClientAuthenticatorService;
	@Autowired
	AppProperties appProperties;
	private static final long DELAY = 3 * 60000;
	private static final long DELETE_WORK_DELAY = 24 * 3600000;

	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	
	@Scheduled(fixedDelay = DELAY, initialDelay = DELAY)
	private void prepareMessage() throws IOException {
		NotificationDataSource datasource = NotificationDataSource.getInstance();
		List<ComGenerator> records = datasource.fetchRecordsByScheduledDateAndStatus(DateUtilityHelper.getCurrentSqlDate(), MessageStatus.PENDING);
		// TODO: Read and process the records in a batch.

		for (ComGenerator record: records) {
			String patientId = record.getPatientId();

			try {
				Patient patient = fhirClientAuthenticatorService.getFhirClient().read().resource(Patient.class).withId(patientId).execute();
				Triple<String, String, String> oclDetails = FhirUtils.getOclIdFromIdentifier(patient.getIdentifier());
				String mobile = null;

				if (!patient.getTelecom().isEmpty()) {
					mobile = patient.getTelecom().get(0).getValue();
				}
				if (oclDetails == null || mobile == null) {
					throw new IllegalStateException("Either oclId or mobile number is missing from the patient");
				}

				String patientOclId = oclDetails.getFirst().replaceAll(".(?!$)", "$0 ").replaceAll(".{8}", "$0\n");
				String patientName = patient.getName().get(0).getNameAsSingleString();
				String patientOclLink = FhirUtils.getOclLink(patient.getIdentifier());
				String date = DateUtilityHelper.sqlTimestampToFormattedDateString(record.getCreatedAt());

				if (mobile.startsWith("+234-") && mobile.length() > 6) {
					mobile = mobile.substring(5);
					if (mobile.startsWith("0")) {
						mobile = mobile.substring(1);
					}

					String patientDetailsMessage = "";
					if (record.getResourceType().equals("Encounter")) {
						patientDetailsMessage += "Thanks for visiting!\nHere are the details of your visit: \nName: " + patientName + " \nDate: " + date + " \nYour OCL Id is:\n" + patientOclId + "";
						String oclLinkMessage = "The QR image for OCL code:\n" + patientOclId + "\nis here:\n" + patientOclLink + "";
						sendSmsAndUpdateStatus(patientDetailsMessage, mobile, record);
						sendSmsAndUpdateStatus(oclLinkMessage, mobile, record);
						updateSMSInfoRecord(record);
					} else if (record.getResourceType().equals("Appointment")) {
						Timestamp nextVisitDate = record.getNextVisitDate();
						patientDetailsMessage += "Your next visit details are: \nName: " + patientName + (nextVisitDate != null ? "\nDate: " + nextVisitDate : "") + "\nYour OCL Id is:\n" + patientOclId;
						sendSmsAndUpdateStatus(patientDetailsMessage, mobile, record);
						updateSMSInfoRecord(record);
					}
				}
			} catch (ResourceNotFoundException | IllegalStateException | ResourceGoneException ex) {
				logger.warn(ExceptionUtils.getStackTrace(ex));
				logger.warn("Deleting the record from the database!");
				datasource.delete(record);
			}
		}
	}
	
	@Scheduled(fixedDelay = DELETE_WORK_DELAY, initialDelay = DELAY)
	public void deletePreviousRecords() {
		NotificationDataSource datasource = NotificationDataSource.getInstance();
		Date previousDate = DateUtilityHelper.getPreviousDateByDays(DateUtilityHelper.getCurrentSqlDate(), 15);
		datasource.deleteRecordsByTimePeriod(previousDate);
	}

	private void updateSMSInfoRecord(ComGenerator comGenerator){
		IGenericClient fhirClient = FhirClientAuthenticatorService.getFhirClient();
		String resourceId = comGenerator.getResourceId();
		String patientId = comGenerator.getPatientId();
		String patientCardNumber = FhirUtils.getPatientCardNumberByPatientId(patientId, fhirClient);
		NotificationDataSource dataSource = NotificationDataSource.getInstance();
		List<SMSInfo> smsInfo = dataSource.fetchSMSRecordsByResourceId(resourceId);
		Pair<String, String> encIdAndOrgIdPair = FhirUtils.getEncounterIdAndOrganizationIdForAppointment(resourceId, fhirClient);
		if (!smsInfo.isEmpty() && encIdAndOrgIdPair != null){
			SMSInfo smsInfoRecord = smsInfo.get(0);
			smsInfoRecord.setStatus(comGenerator.getCommunicationStatus());
			smsInfoRecord.setPatientCardNumber(patientCardNumber);
			if (Objects.equals(comGenerator.getCommunicationStatus(), MessageStatus.SENT.name()))
				smsInfoRecord.setSentAt(comGenerator.getUpdatedAt());
			if (comGenerator.getResourceType().equals(ResourceType.Appointment.name())){
				smsInfoRecord.setEncounterId(encIdAndOrgIdPair.getFirst());
				smsInfoRecord.setOrganizationId(encIdAndOrgIdPair.getSecond());
			}
			dataSource.update(smsInfoRecord);
		}
	}

	private void sendSmsAndUpdateStatus(String message, String mobileNumber, ComGenerator comGeneratorEntity) throws IOException {
		NotificationDataSource dataSource = NotificationDataSource.getInstance();
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		okhttp3.MediaType mediaType = okhttp3.MediaType.parse("text/plain");

		// TODO: Can query parameters be assigned dynamically through app properties instead of hardcoding?
		String smsPassword = appProperties.getSms_password();
		String smsUrl = "https://portal.nigeriabulksms.com/api/?username=impacthealth@hacey.org" + "&password=" + smsPassword + "&message=" + message + "&sender=HACEY-IPRD&mobiles=" + mobileNumber;
		Request smsRequest = new Request.Builder().url(smsUrl).build();
		okhttp3.Response smsResponse = client.newCall(smsRequest).execute();
		try {
			String smsStatus = getSMSResponseStatus(smsResponse);
			if (smsStatus != null && smsStatus.equals("OK")){
				System.out.println(message);
				comGeneratorEntity.setCommunicationStatus(MessageStatus.SENT.name());
				dataSource.update(comGeneratorEntity);
			}
		} catch (Exception e) {
			comGeneratorEntity.setCommunicationStatus(MessageStatus.FAILED.name());
			dataSource.update(comGeneratorEntity);
			logger.warn(ExceptionUtils.getStackTrace(e));
		} finally {
			if (smsResponse.body() != null) smsResponse.body().close();
		}
	}

	private String getSMSResponseStatus(okhttp3.Response smsResponse) throws IOException {
		if (smsResponse.body() != null){
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode smsResponseBody = objectMapper.readTree(smsResponse.body().string());
			return smsResponseBody.get("status").asText();
		}
		return null;
	}
}
