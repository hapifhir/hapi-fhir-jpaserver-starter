package ca.uhn.fhir.jpa.starter.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;

import android.util.Base64;

import ca.uhn.fhir.jpa.starter.model.*;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;

import com.iprd.fhir.utils.DateUtilityHelper;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.AppProperties;

@Import(AppProperties.class)
@Interceptor
public class ServerInterceptor {
		
	String imagePath;
	
	NotificationDataSource notificationDataSource;
	
	
	private static final Logger logger = LoggerFactory.getLogger(ServerInterceptor.class);
	
	public ServerInterceptor(String path) {
		imagePath = path;
	}
	
	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
	public void insert(IBaseResource theResource) throws IOException {
		notificationDataSource = NotificationDataSource.getInstance();
		if(theResource.fhirType().equals("Media")) {
			if(((Media) theResource).getContent().hasData()) {
				byte[] bitmapdata = ((Media) theResource).getContent().getDataElement().getValue();
				String mediaId = ((Media) theResource).getIdElement().getIdPart();
				byte[] base64 = Base64.decode(bitmapdata, Base64.DEFAULT);
				File image = new File(imagePath+"//"+mediaId+".jpeg");
				FileUtils.writeByteArrayToFile(image, base64);
				String imagePath = image.getAbsolutePath();
				long imageSize = Files.size(Paths.get(imagePath));
				long byteSize = base64.length;
				if(imageSize == byteSize) {
					((Media) theResource).getContent().setDataElement(null);
		 			((Media) theResource).getContent().setUrl(imagePath);
				}
				else {
					System.out.println("Image Not Proper");
				}
			}
		}
		else if (theResource.fhirType().equals("Encounter")) {
			Encounter encounter = (Encounter) theResource;
			String encounterId = encounter.getIdElement().getIdPart();
			String patientId = encounter.getSubject().getReferenceElement().getIdPart();
			Date currentDate = DateUtilityHelper.getCurrentSqlDate();
			String messageStatus = ComGenerator.MessageStatus.PENDING.name();

			EncounterIdEntity encounterIdEntity = new EncounterIdEntity(encounterId);
			notificationDataSource.insert(encounterIdEntity);

			ComGenerator comGen = new ComGenerator(
				"Encounter",
				encounterId,
				currentDate,
				messageStatus,
				patientId,
				null
			);

			notificationDataSource.insert(comGen);
		}
		else if(theResource.fhirType().equals("Appointment")) {
			Appointment appointment = (Appointment) theResource;
			String appointmentId = appointment.getIdElement().getIdPart();
			String patientId = appointment.getParticipant().get(0).getActor().getReferenceElement().getIdPart();

			Date currentDate = DateUtilityHelper.getCurrentSqlDate();
			Timestamp appointmentScheduledDateTime = DateUtilityHelper.utilDateToTimestamp(appointment.getStart());
			String messageStatus = ComGenerator.MessageStatus.PENDING.name();

			ComGenerator comGen = new ComGenerator(
					"Appointment",
					appointmentId,
					currentDate,
					messageStatus,
					patientId,
					appointmentScheduledDateTime
				);

			ComGenerator firstReminder = new ComGenerator(
					"Appointment",
					appointmentId,
					DateUtilityHelper.getPreviousDay(appointmentScheduledDateTime),
					messageStatus,
					patientId,
					appointmentScheduledDateTime
				);

			ComGenerator secondReminder = new ComGenerator(
					"Appointment",
					appointmentId,
					DateUtilityHelper.timeStampToDate(appointmentScheduledDateTime),
					messageStatus,
					patientId,
					appointmentScheduledDateTime
				);

			notificationDataSource.insert(comGen);
			notificationDataSource.insert(firstReminder);
			notificationDataSource.insert(secondReminder);
		}
	}
	
	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
	   public void update(IBaseResource theOldResource, IBaseResource theResource) throws IOException {
		notificationDataSource = NotificationDataSource.getInstance();
		if(theResource.fhirType().equals("Media")) {
			if(((Media) theResource).getContent().hasData()) {
				byte[] bitmapdata = ((Media) theResource).getContent().getDataElement().getValue();
				String mediaId = ((Media) theResource).getIdElement().getIdPart();
				byte[] base64 = Base64.decode(bitmapdata, Base64.DEFAULT);
				File image = new File(imagePath+"//"+mediaId+".jpeg");
				FileUtils.writeByteArrayToFile(image, base64);
				String imagePath = image.getAbsolutePath();
				long imageSize = Files.size(Paths.get(imagePath));
				long byteSize = base64.length;
				if(imageSize == byteSize) {
					((Media) theResource).getContent().setDataElement(null);
		 			((Media) theResource).getContent().setUrl(imagePath);
				}
				else {
					System.out.println("Image Not Proper");
				}	
			}
		} else if (theResource.fhirType().equals("Encounter")) {
			Encounter encounter = (Encounter) theResource;
			String encounterId = encounter.getIdElement().getIdPart();

			EncounterIdEntity encounterIdEntity = new EncounterIdEntity(encounterId);
			// Using persist to add entry only if it is not exists
			notificationDataSource.persist(encounterIdEntity);
		}
	}

	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_DELETED)
	public void delete(IBaseResource theResource) {
		notificationDataSource = NotificationDataSource.getInstance();
		if (theResource.fhirType().equals("Encounter")) {
			Encounter encounter = (Encounter) theResource;
			String encounterId = encounter.getIdElement().getIdPart();
			notificationDataSource.deleteFromEncounterIdEntityByEncounterId(encounterId);
		}
	}
}