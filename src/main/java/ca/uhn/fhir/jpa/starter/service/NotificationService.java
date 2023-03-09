package ca.uhn.fhir.jpa.starter.service;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import org.hl7.fhir.r4.model.Patient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.FhirUtils;

import ca.uhn.fhir.jpa.starter.model.ComGenerator;
import ca.uhn.fhir.jpa.starter.model.ComGenerator.MessageStatus;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Service
public class NotificationService {
	
	private static final long DELAY = 3 * 60000;
	private static final long DELETE_WORK_DELAY = 24 * 3600000;
	
//	@Scheduled(fixedDelay = DELAY, initialDelay = DELAY)
	private void sendSms() throws IOException {
		NotificationDataSource datasource = NotificationDataSource.getInstance(); 
		List<ComGenerator> records = datasource.fetchRecordsByScheduledDateAndStatus(DateUtilityHelper.getCurrentSqlDate(), MessageStatus.PENDING);
		for (ComGenerator record: records) {
			
			String patientId = record.getPatientId();
			Patient patient = FhirClientAuthenticatorService.getFhirClient().read().resource(Patient.class).withId(patientId).execute();
			String patientOclId = FhirUtils.getOclIdentifier(patient.getIdentifier()).replaceAll(".(?!$)", "$0 ").replaceAll(".{8}", "$0\n");
			String patientName = patient.getName().get(0).getNameAsSingleString();
			String patientOclLink = FhirUtils.getOclLink(patient.getIdentifier());
			String mobile = patient.getTelecom().get(0).getValue();
			String date = DateUtilityHelper.sqlTimestampToFormattedDateString(record.getCreatedAt());
			
			if (mobile.startsWith("+234-") && mobile.length() > 6) {
				mobile = mobile.substring(5);
				if (mobile.startsWith("0")) {
					mobile = mobile.substring(1);
				}
				OkHttpClient client = new OkHttpClient().newBuilder().build();
				okhttp3.MediaType mediaType = okhttp3.MediaType.parse("text/plain");
				String patientDetailsMessage = "";
				
				if (record.getResourceType().equals("Encounter")) {
					patientDetailsMessage += "Thanks for visiting!\nHere are the details of your visit: \nName: " + patientName + " \nDate: " + date + " \nYour OCL Id is:\n" + patientOclId + "";
					String oclLinkMessage = "The QR image for OCL code:\n" + patientOclId + "\nis here:\n" + patientOclLink + "";
					String messageVisitDetails = "https://portal.nigeriabulksms.com/api/?username=impacthealth@hacey.org&password=IPRDHACEY123&message=" + patientDetailsMessage + "&sender=HACEY-IPRD&mobiles=" + mobile;
					String messageQrImage = "https://portal.nigeriabulksms.com/api/?username=impacthealth@hacey.org&password=IPRDHACEY123&message=" + oclLinkMessage + "&sender=HACEY-IPRD&mobiles=" + mobile;
					Request requestVisitDetails = new Request.Builder().url(messageVisitDetails).build();
					Request requestQrImage = new Request.Builder().url(messageQrImage).build();
					okhttp3.Response responseVisitDetails = client.newCall(requestVisitDetails).execute();
					okhttp3.Response responseQrImage = client.newCall(requestQrImage).execute();
					try {
						if (responseVisitDetails.isSuccessful() && responseQrImage.isSuccessful()) {
							System.out.println(patientDetailsMessage);
							record.setCommunicationStatus(MessageStatus.SENT.name());
							datasource.update(record);
						}
					} catch (Exception e) {
						record.setCommunicationStatus(MessageStatus.FAILED.name());
						datasource.update(record);
						e.printStackTrace();
					} finally {
						responseVisitDetails.body().close();
						responseQrImage.body().close();
					}
				} else if (record.getResourceType().equals("Appointment")) {
					patientDetailsMessage += "Your next visit details are: \nName: " + patientName + " \nDate: " + date + " \nYour OCL Id is:\n" + patientOclId + "";
					String messageVisitDetails = "https://portal.nigeriabulksms.com/api/?username=impacthealth@hacey.org&password=IPRDHACEY123&message=" + patientDetailsMessage + "&sender=HACEY-IPRD&mobiles=" + mobile;
					Request requestVisitDetails = new Request.Builder().url(messageVisitDetails).build();
					okhttp3.Response responseVisitDetails = client.newCall(requestVisitDetails).execute();
					try {
						if (responseVisitDetails.isSuccessful()) {
							System.out.println(patientDetailsMessage);
							record.setCommunicationStatus(MessageStatus.SENT.name());
							datasource.update(record);
						}
					} catch (Exception e) {
						record.setCommunicationStatus(MessageStatus.FAILED.name());
						datasource.update(record);
						e.printStackTrace();
					} finally {
						responseVisitDetails.body().close();
					}
				}
			}
		}
	}
	
//	@Scheduled(fixedDelay = DELETE_WORK_DELAY, initialDelay = DELAY)
	public void deletePerviousRecords() {
		NotificationDataSource datasource = NotificationDataSource.getInstance();
		Date previousDate = DateUtilityHelper.getPreviousDateByDays(DateUtilityHelper.getCurrentSqlDate(), 15);
		datasource.deleteRecordsByTimePeriod(previousDate);
	}

}
