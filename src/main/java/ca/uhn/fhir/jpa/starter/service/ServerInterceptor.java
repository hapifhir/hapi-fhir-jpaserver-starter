package ca.uhn.fhir.jpa.starter.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import android.util.Base64;

import ca.uhn.fhir.jpa.starter.model.*;
import kotlin.Pair;

import com.iprd.fhir.utils.FhirUtils;
import com.iprd.fhir.utils.PatientIdentifierStatus;

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

	private static final String ENCOUNTER_MIGRATED_SYSTEM = "https://iprdgroup.com/identifier";
	private static final String ENCOUNTER_MIGRATED_VALUE = "argusoft-migrated";
	
	public ServerInterceptor(String path) {
		imagePath = path;
	}
	
	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
	public void insert(IBaseResource theResource) throws IOException {
		notificationDataSource = NotificationDataSource.getInstance();
		if(theResource.fhirType().equals("Media")) {
			processMedia((Media) theResource);
		}
		else if (theResource.fhirType().equals("Encounter")) {
			try {
				Encounter encounter = (Encounter) theResource;
				String encounterId = encounter.getIdElement().getIdPart();
				String patientId = encounter.getSubject().getReferenceElement().getIdPart();
				Date currentDate = DateUtilityHelper.getCurrentSqlDate();
				String messageStatus = ComGenerator.MessageStatus.PENDING.name();

				if (!isEncounterMigrated(encounter)) {
					try {
						EncounterIdEntity encounterIdEntity = new EncounterIdEntity(encounterId);
						notificationDataSource.insert(encounterIdEntity);
					} catch(Exception ex) {
						System.out.println("Duplicate encounter id found");
						ex.printStackTrace();
					}

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
			} catch(PersistenceException ex) {
				System.out.println("Duplicate encounter entry found");
				ex.printStackTrace();
			}
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
		else if (theResource.fhirType().equals("QuestionnaireResponse")) {
			processQuestionnaireResponse((QuestionnaireResponse) theResource);
		}
		else if(theResource.fhirType().equals("Patient")){
			Patient patient = (Patient) theResource;
			String patientId = patient.getIdElement().getIdPart();
			String patientOclId = FhirUtils.getOclIdentifier(patient.getIdentifier());
			String patientOclUrlLink = FhirUtils.getOclLink(patient.getIdentifier());
			String patientTelecom = patient.getTelecomFirstRep().getValue();
			String patientCardNumber = FhirUtils.getPatientCardNumber(patient.getIdentifier());
			String currentDate = String.valueOf(System.currentTimeMillis());
			List<PatientIdentifierEntity> entitiesToSave = new ArrayList<>();
			if(!FhirUtils.isOclPatient(patient.getIdentifier())) {
				PatientIdentifierEntity patientInfoResourceEntityOCL = new PatientIdentifierEntity(
						patientId,
						patientOclId,
						patientOclUrlLink,
						(notificationDataSource.getPatientIdWithIdentifier(patientOclId).size() >= 1) ?  PatientIdentifierStatus.DUPLICATE.name():PatientIdentifierStatus.OK.name(),
						theResource.fhirType(),
						currentDate.toString()
					);
				PatientIdentifierEntity patientInfoResourceEntityCardNumber = new PatientIdentifierEntity(
						patientId,
						patientCardNumber,
						"http://iprdgroup.com/identifiers/patient-card",
						(notificationDataSource.getPatientIdWithIdentifier(patientCardNumber).size() >= 1) ?  PatientIdentifierStatus.DUPLICATE.name(): PatientIdentifierStatus.OK.name(),
						theResource.fhirType(),
						currentDate.toString()
					);
				PatientIdentifierEntity patientInfoResourceEntityTelecom = new PatientIdentifierEntity(
						patientId,
						patientTelecom,
						"phone",
						(notificationDataSource.getPatientIdWithIdentifier(patientTelecom).size() >= 1) ?  PatientIdentifierStatus.DUPLICATE.name(): PatientIdentifierStatus.OK.name(),
						theResource.fhirType(),
						currentDate.toString()
					);
					notificationDataSource.insert(patientInfoResourceEntityOCL);
					notificationDataSource.insert(patientInfoResourceEntityCardNumber);
					notificationDataSource.insert(patientInfoResourceEntityTelecom);
			}
		}
	}

	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
	   public void update(IBaseResource theOldResource, IBaseResource theResource) throws IOException {
		notificationDataSource = NotificationDataSource.getInstance();
		if(theResource.fhirType().equals("Media")) {
			processMedia((Media) theResource);
		} else if (theResource.fhirType().equals("Encounter")) {
			Encounter encounter = (Encounter) theResource;
			String encounterId = encounter.getIdElement().getIdPart();

			if (!isEncounterMigrated(encounter)) {
				EncounterIdEntity encounterIdEntity = new EncounterIdEntity(encounterId);
				// Using persist to add entry only if it is not exists
				notificationDataSource.persist(encounterIdEntity);
			}
		}
		else if (theResource.fhirType().equals("QuestionnaireResponse")) {
			processQuestionnaireResponse((QuestionnaireResponse) theResource);
		}
		else if(theResource.fhirType().equals("Patient")){
			Patient patient = (Patient) theResource;
			String patientId = patient.getIdElement().getIdPart();
			String patientOclId = FhirUtils.getOclIdentifier(patient.getIdentifier());
			String patientOclUrlLink = FhirUtils.getOclLink(patient.getIdentifier());
			String patientTelecom = patient.getTelecomFirstRep().getValue();
			String patientCardNumber = FhirUtils.getPatientCardNumber(patient.getIdentifier());
			String currentDate = String.valueOf(System.currentTimeMillis());
			List<PatientIdentifierEntity> entitiesToSave = new ArrayList<>();
			Pair<List<String>, List<Identifier>> missingIdentifierNewIdentifier = FhirUtils.getMissingIdentifierAndNewIdentifier(((Patient)theOldResource).getIdentifier(),patient.getIdentifier());
			// Handle Identifiers that are deleted
			if(!missingIdentifierNewIdentifier.getFirst().isEmpty()) {
				for(String identifierValue : missingIdentifierNewIdentifier.getFirst()) {
					List<PatientIdentifierEntity> patientIdentifierEntitys =  notificationDataSource.getPatientIdentifierEntity(identifierValue,patientId);
					if(patientIdentifierEntitys.size()>0) {
						PatientIdentifierEntity patientEntity =  patientIdentifierEntitys.get(0);
						patientEntity.setStatus(PatientIdentifierStatus.DELETE.name());
						notificationDataSource.update(patientEntity);
					}
				}	
			}
			// Handle Updated / New identifiers
			if(!missingIdentifierNewIdentifier.getSecond().isEmpty()) {
				for(Identifier identifier : missingIdentifierNewIdentifier.getSecond()) {
					List<PatientIdentifierEntity> patientIdentifierEntitys =  notificationDataSource.getPatientIdentifierEntity(identifier.getValue(),patientId);
					if(patientIdentifierEntitys.size()>0) {
						PatientIdentifierEntity patientEntity =  patientIdentifierEntitys.get(0);
						patientEntity.setStatus(PatientIdentifierStatus.DELETE.name());
						notificationDataSource.update(patientEntity);
					}else{
						PatientIdentifierEntity patientInfoResourceEntityCardNumber = new PatientIdentifierEntity(
							patientId,
							identifier.getValue(),
							identifier.getSystem(),
							(notificationDataSource.getPatientIdWithIdentifier(patientCardNumber).size() >= 1) ?   PatientIdentifierStatus.DUPLICATE.name(): PatientIdentifierStatus.OK.name(),
							theResource.fhirType(),
							currentDate
						);
						notificationDataSource.insert(patientInfoResourceEntityCardNumber);
					}
				}
			}
			if(!FhirUtils.isOclPatient(patient.getIdentifier())) {
				
				PatientIdentifierEntity patientInfoResourceEntityOCL = new PatientIdentifierEntity(
						patientId,
						patientOclId,
						patientOclUrlLink,
						(notificationDataSource.getPatientIdWithIdentifier(patientOclId).size() >= 1) ?  PatientIdentifierStatus.DUPLICATE.name(): PatientIdentifierStatus.OK.name(),
						theResource.fhirType(),
						currentDate
					);
				PatientIdentifierEntity patientInfoResourceEntityCardNumber = new PatientIdentifierEntity(
						patientId,
						patientCardNumber,
						"http://iprdgroup.com/identifiers/patient-card",
						(notificationDataSource.getPatientIdWithIdentifier(patientCardNumber).size() >= 1) ?   PatientIdentifierStatus.DUPLICATE.name(): PatientIdentifierStatus.OK.name(),
						theResource.fhirType(),
						currentDate
					);
				PatientIdentifierEntity patientInfoResourceEntityTelecom = new PatientIdentifierEntity(
						patientId,
						patientTelecom,
						"phone",
						(notificationDataSource.getPatientIdWithIdentifier(patientTelecom).size() >= 1) ?   PatientIdentifierStatus.DUPLICATE.name(): PatientIdentifierStatus.OK.name(),
						theResource.fhirType(),
						currentDate
					);
					notificationDataSource.insert(patientInfoResourceEntityOCL);
					notificationDataSource.insert(patientInfoResourceEntityCardNumber);
					notificationDataSource.insert(patientInfoResourceEntityTelecom);
			}
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

	private boolean isEncounterMigrated(Encounter encounter) {
		for (Identifier identifier: encounter.getIdentifier()) {
			if (
				identifier.hasSystem() && identifier.getSystem().equals(ENCOUNTER_MIGRATED_SYSTEM) &&
					identifier.hasValue() && identifier.getValue().equals(ENCOUNTER_MIGRATED_VALUE)
			) {
				return true;
			}
		}
		return false;
	}

	private void processMedia(Media media) throws IOException {
		if(media.getContent().hasData()) {
			byte[] bitmapdata = media.getContent().getDataElement().getValue();
			byte[] base64 = Base64.decode(bitmapdata, Base64.DEFAULT);
			String md5Hash = md5Bytes(base64);
			if (md5Hash == null) {
				return;
			}
			File image = new File(imagePath+"//"+md5Hash+".jpeg");
			FileUtils.writeByteArrayToFile(image, base64);
			String imagePath = image.getAbsolutePath();
			long imageSize = Files.size(Paths.get(imagePath));
			long byteSize = base64.length;
			if(imageSize == byteSize) {
				media.getContent().setDataElement(null);
				media.getContent().setUrl(imagePath);
			}
			else {
				logger.warn("Image Not Proper");
			}
		}
	}

	private void processQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) throws IOException {
		if (!questionnaireResponse.getQuestionnaire().equals("Questionnaire/labour")) {
			return;
		}
		byte[] bitmapdata = null;

		for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
			if (item.getLinkId().equals("8.0")) {
				for (QuestionnaireResponse.QuestionnaireResponseItemComponent groupItem : item.getItem()) {
					if (groupItem.getLinkId().equals("8.2")) {
						for (QuestionnaireResponse.QuestionnaireResponseItemComponent answerItem : groupItem.getItem()) {
							if (answerItem.getLinkId().equals("8.2.1")) {
								List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answers = answerItem.getAnswer();
								if (answers != null && !answers.isEmpty()) {
									QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = answers.get(0);
									if (answer.getValueAttachment() != null && answer.getValueAttachment().getData() != null) {
										bitmapdata = answer.getValueAttachment().getData();
										break;
									}
								}
							}
						}
						break;
					}
				}
				break;
			}
		}

		if (bitmapdata == null) {
			return;
		}

		byte[] base64 = bitmapdata;
		String md5Hash = md5Bytes(base64);

		if (md5Hash == null) {
			return;
		}

		File image = new File(imagePath+"//"+md5Hash+".jpeg");
		FileUtils.writeByteArrayToFile(image, base64);
		String imagePath = image.getAbsolutePath();
		long imageSize = Files.size(Paths.get(imagePath));
		long byteSize = base64.length;
		if(imageSize == byteSize) {
			for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
				if (item.getLinkId().equals("8.0")) {
					for (QuestionnaireResponse.QuestionnaireResponseItemComponent groupItem : item.getItem()) {
						if (groupItem.getLinkId().equals("8.2")) {
							for (QuestionnaireResponse.QuestionnaireResponseItemComponent answerItem : groupItem.getItem()) {
								if (answerItem.getLinkId().equals("8.2.1")) {
									List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answers = answerItem.getAnswer();
									if (answers != null && !answers.isEmpty()) {
										QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = answers.get(0);
										Attachment valueAttachment = answer.getValueAttachment();
										if (valueAttachment != null) {
											valueAttachment.setData(null);
											valueAttachment.setUrl(imagePath);
										} else {
											valueAttachment = new Attachment();
											valueAttachment.setData(null);
											valueAttachment.setUrl(imagePath);
											answer.setValue(valueAttachment);
										}
									} else {
										QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
										Attachment valueAttachment = new Attachment();
										valueAttachment.setData(null);
										valueAttachment.setUrl(imagePath);
										answer.setValue(valueAttachment);
										answerItem.addAnswer(answer);
									}
								}
							}
							break;
						}
					}
					break;
				}
			}
		}
		else {
			logger.warn("Image Not Proper");
		}
	}

	private String md5Bytes(byte[] bytes) {
		String digest = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] hash = md5.digest(bytes);
			StringBuilder stringBuilder = new StringBuilder(2 * hash.length);
			for (byte b: hash) {
				stringBuilder.append(String.format("%02x", b));
			}
			digest = stringBuilder.toString();
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return digest;
	}
}