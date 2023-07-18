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
import java.util.Objects;

import android.util.Base64;

import ca.uhn.fhir.jpa.starter.model.*;

import com.iprd.fhir.utils.FhirUtils;
import com.iprd.fhir.utils.PatientIdentifierStatus;

import kotlin.Triple;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
			Encounter encounter = (Encounter) theResource;
			String encounterId = encounter.getIdElement().getIdPart();
			String patientId = encounter.getSubject().getReferenceElement().getIdPart();
			Date currentDate = DateUtilityHelper.getCurrentSqlDate();
			String messageStatus = ComGenerator.MessageStatus.PENDING.name();

			if (!isEncounterMigrated(encounter)) {
				try {
					EncounterIdEntity encounterIdEntity = new EncounterIdEntity(encounterId);	
					notificationDataSource.persist(encounterIdEntity);
					ComGenerator comGen = new ComGenerator(
						"Encounter",
						encounterId,
						currentDate,
						messageStatus,
						patientId,
						null
					);

					notificationDataSource.persist(comGen);
	
				}catch(Exception e) {
					logger.warn(ExceptionUtils.getStackTrace(e));
				}
			}
		}
		else if(theResource.fhirType().equals("Appointment")) {
			Appointment appointment = (Appointment) theResource;
			String appointmentId = appointment.getIdElement().getIdPart();
			String patientId = appointment.getParticipant().get(0).getActor().getReferenceElement().getIdPart();
			Timestamp appointmentScheduledDateTime = DateUtilityHelper.utilDateToTimestamp(appointment.getStart());

			Date currentDate = DateUtilityHelper.getCurrentSqlDate();
			Date previousDate = DateUtilityHelper.getPreviousDay(appointmentScheduledDateTime);
			Date appointmentDate = DateUtilityHelper.timeStampToDate(appointmentScheduledDateTime);
			
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
					previousDate,
					messageStatus,
					patientId,
					appointmentScheduledDateTime
				);

			ComGenerator secondReminder = new ComGenerator(
					"Appointment",
					appointmentId,
					appointmentDate,
					messageStatus,
					patientId,
					appointmentScheduledDateTime
				);

			notificationDataSource.insert(comGen);
			if(currentDate!=previousDate) {
				notificationDataSource.insert(firstReminder);	
			}
			if(currentDate!=appointmentDate) {
				notificationDataSource.insert(secondReminder);	
			}
		}
		else if (theResource.fhirType().equals("QuestionnaireResponse")) {
			processQuestionnaireResponse((QuestionnaireResponse) theResource);
		} else if (theResource.fhirType().equals("Patient")) {
			processPatientInsert(theResource);
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
			processPatientUpdate(theOldResource, theResource);
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
			File image = new File(imagePath,md5Hash+".jpeg");
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

	private void processPatientInsert(IBaseResource theResource) {
		Patient patient = (Patient) theResource;
		String patientId = patient.getIdElement().getIdPart();
		Triple<String, String, String> patientOclId = FhirUtils.getOclIdFromIdentifier(patient.getIdentifier());
		String patientTelecom = patient.getTelecomFirstRep().getValue();
		String patientCardNumber = FhirUtils.getPatientCardNumber(patient.getIdentifier());
		Long currentEpochTime = System.currentTimeMillis();

		if (FhirUtils.isOclPatient(patient.getIdentifier())) {
			return;
		}

		if (patientOclId != null && patientOclId.getFirst() != null) {
			PatientIdentifierEntity patientIdentifierEntityOcl = new PatientIdentifierEntity(
				patientId,
				patientOclId.getFirst(),
				PatientIdentifierEntity.PatientIdentifierType.OCL_ID.name(),
				patientOclId.getSecond(),
				patientOclId.getThird(),
				(notificationDataSource.getPatientIdWithIdentifier(patientId, patientOclId.getFirst()).size() >= 1) ? PatientIdentifierStatus.DUPLICATE.name() : PatientIdentifierStatus.OK.name(),
				currentEpochTime,
				currentEpochTime
			);
			notificationDataSource.persist(patientIdentifierEntityOcl);
		}

		if (patientCardNumber != null) {
			PatientIdentifierEntity patientIdentifierEntityCardNumber = new PatientIdentifierEntity(
				patientId,
				patientCardNumber,
				PatientIdentifierEntity.PatientIdentifierType.PATIENT_CARD_NUM.name(),
				null,
				null,
				(notificationDataSource.getPatientIdWithIdentifier(patientId, patientCardNumber).size() >= 1) ? PatientIdentifierStatus.DUPLICATE.name() : PatientIdentifierStatus.OK.name(),
				currentEpochTime,
				currentEpochTime
			);
			notificationDataSource.persist(patientIdentifierEntityCardNumber);
		}

		if (patientTelecom != null) {
			PatientIdentifierEntity patientIdentifierEntityPhoneNumber = new PatientIdentifierEntity(
				patientId,
				patientTelecom,
				PatientIdentifierEntity.PatientIdentifierType.PHONE_NUM.name(),
				null,
				null,
				(notificationDataSource.getPatientIdWithIdentifier(patientId, patientTelecom).size() >= 1) ? PatientIdentifierStatus.DUPLICATE.name() : PatientIdentifierStatus.OK.name(),
				currentEpochTime,
				currentEpochTime
			);
			notificationDataSource.persist(patientIdentifierEntityPhoneNumber);
		}

	}

	private void processPatientUpdate(IBaseResource theOldResource, IBaseResource theResource) {
		Patient oldPatient = (Patient) theOldResource;
		Patient updatedPatient = (Patient) theResource;

		if (FhirUtils.isOclPatient(oldPatient.getIdentifier()) && !FhirUtils.isOclPatient(updatedPatient.getIdentifier())) {
			// If the use updates the temporary patient from the mobile, the identifier will be removed. So adding it back
			Identifier oclPatientIdentifier = new Identifier().setSystem("http://iprdgroup.com/identifiers/patientWithOcl").setValue("patient_with_ocl");
			updatedPatient.addIdentifier(oclPatientIdentifier);
			// Returning form this block because for temporary patient no need to keep track of duplicate identifier.
			return;
		}

		String patientId = updatedPatient.getIdElement().getIdPart();

		Triple<String, String, String> oldPatientOclId = FhirUtils.getOclIdFromIdentifier(oldPatient.getIdentifier());
		Triple<String, String, String> updatedPatientOclId = FhirUtils.getOclIdFromIdentifier(updatedPatient.getIdentifier());

		String oldPatientCardNumber = FhirUtils.getPatientCardNumber(oldPatient.getIdentifier());
		String updatedPatientCardNumber = FhirUtils.getPatientCardNumber(updatedPatient.getIdentifier());

		String oldTelecom = oldPatient.getTelecomFirstRep().getValue();
		String updatedTelecom = updatedPatient.getTelecomFirstRep().getValue();

		if (!Objects.equals(oldPatientOclId, updatedPatientOclId)) {
			List<PatientIdentifierEntity> patientIdentifierEntityList = (oldPatientOclId == null) ? new ArrayList() : notificationDataSource.getPatientIdentifierEntityByPatientIdAndIdentifier(patientId, oldPatientOclId.getFirst());
			PatientIdentifierEntity patientIdentifierEntity = (!patientIdentifierEntityList.isEmpty()) ? patientIdentifierEntityList.get(0) : null;

			if (patientIdentifierEntity != null) {
				patientIdentifierEntity.setStatus(PatientIdentifierStatus.DELETE.name());
				patientIdentifierEntity.setUpdatedTime(System.currentTimeMillis());
				notificationDataSource.update(patientIdentifierEntity);

				if (PatientIdentifierStatus.OK.name().equals(patientIdentifierEntity.getStatus())) {
					PatientIdentifierEntity entryWithDuplicateStatus = notificationDataSource.getPatientIdentifierEntityWithDuplicateStatus(patientId, oldPatientOclId.getFirst());

					if (entryWithDuplicateStatus != null) {
						entryWithDuplicateStatus.setStatus(PatientIdentifierStatus.OK.name());
						entryWithDuplicateStatus.setUpdatedTime(System.currentTimeMillis());
						notificationDataSource.update(entryWithDuplicateStatus);
					}
				}
			}

			if (updatedPatientOclId != null) {
				long currentTime = System.currentTimeMillis();

				PatientIdentifierEntity newPatientIdentifierEntity = new PatientIdentifierEntity(
					patientId,
					updatedPatientOclId.getFirst(),
					PatientIdentifierEntity.PatientIdentifierType.OCL_ID.name(),
					updatedPatientOclId.getSecond(),
					updatedPatientOclId.getThird(),
					PatientIdentifierStatus.OK.name(),
					currentTime,
					currentTime
				);
				notificationDataSource.persist(newPatientIdentifierEntity);
			}
		}

		if (!Objects.equals(oldPatientCardNumber, updatedPatientCardNumber)) {
			List<PatientIdentifierEntity> patientIdentifierEntityList = notificationDataSource.getPatientIdentifierEntityByPatientIdAndIdentifier(patientId, oldPatientCardNumber);
			PatientIdentifierEntity patientIdentifierEntity = (!patientIdentifierEntityList.isEmpty()) ? patientIdentifierEntityList.get(0) : null;

			if (patientIdentifierEntity != null) {
				patientIdentifierEntity.setStatus(PatientIdentifierStatus.DELETE.name());
				patientIdentifierEntity.setUpdatedTime(System.currentTimeMillis());
				notificationDataSource.update(patientIdentifierEntity);

				if (PatientIdentifierStatus.OK.name().equals(patientIdentifierEntity.getStatus())) {
					PatientIdentifierEntity entryWithDuplicateStatus = notificationDataSource.getPatientIdentifierEntityWithDuplicateStatus(patientId, oldPatientCardNumber);
					if (entryWithDuplicateStatus != null) {
						entryWithDuplicateStatus.setStatus(PatientIdentifierStatus.OK.name());
						entryWithDuplicateStatus.setUpdatedTime(System.currentTimeMillis());
						notificationDataSource.update(entryWithDuplicateStatus);
					}
				}
			}
			if (updatedPatientCardNumber != null) {
				long currentTime = System.currentTimeMillis();

				PatientIdentifierEntity newPatientIdentifierEntity = new PatientIdentifierEntity(
					patientId,
					updatedPatientCardNumber,
					PatientIdentifierEntity.PatientIdentifierType.PATIENT_CARD_NUM.name(),
					null,
					null,
					PatientIdentifierStatus.OK.name(),
					currentTime,
					currentTime
				);
				notificationDataSource.persist(newPatientIdentifierEntity);
			}
		}

		if (!Objects.equals(oldTelecom, updatedTelecom)) {
			List<PatientIdentifierEntity> patientIdentifierEntityList = notificationDataSource.getPatientIdentifierEntityByPatientIdAndIdentifier(patientId, oldTelecom);
			PatientIdentifierEntity patientIdentifierEntity = (!patientIdentifierEntityList.isEmpty()) ? patientIdentifierEntityList.get(0) : null;

			if (patientIdentifierEntity != null) {
				patientIdentifierEntity.setStatus(PatientIdentifierStatus.DELETE.name());
				patientIdentifierEntity.setUpdatedTime(System.currentTimeMillis());
				notificationDataSource.update(patientIdentifierEntity);

				if (PatientIdentifierStatus.OK.name().equals(patientIdentifierEntity.getStatus())) {
					PatientIdentifierEntity entryWithDuplicateStatus = notificationDataSource.getPatientIdentifierEntityWithDuplicateStatus(patientId, oldTelecom);

					if (!patientIdentifierEntityList.isEmpty()) {
						entryWithDuplicateStatus.setStatus(PatientIdentifierStatus.OK.name());
						entryWithDuplicateStatus.setUpdatedTime(System.currentTimeMillis());
						notificationDataSource.update(entryWithDuplicateStatus);
					}
				}
			}

			if (updatedTelecom != null) {
				long currentTime = System.currentTimeMillis();

				PatientIdentifierEntity newPatientIdentifierEntity = new PatientIdentifierEntity(
					patientId,
					updatedTelecom,
					PatientIdentifierEntity.PatientIdentifierType.PHONE_NUM.name(),
					null,
					null,
					PatientIdentifierStatus.OK.name(),
					currentTime,
					currentTime
				);
				notificationDataSource.persist(newPatientIdentifierEntity);
			}
		}
	}

	private void processQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) throws IOException {
		if(questionnaireResponse == null) return ;
		if(questionnaireResponse.getQuestionnaire() == null) return ;
		if (questionnaireResponse.getQuestionnaire().equals("Questionnaire/labour")) {
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

		if(questionnaireResponse.getQuestionnaire().equals("Questionnaire/childBirth-registration")){
			byte[] bitmapdata = null;
			for(QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()){
				if(item.getLinkId().equals("birth-certificate-details")){
					for(QuestionnaireResponse.QuestionnaireResponseItemComponent parentGroupItem : item.getItem()){
						if(parentGroupItem.getLinkId().equals("15.0")){
							for(QuestionnaireResponse.QuestionnaireResponseItemComponent childGroupItem : parentGroupItem.getItem()){
								if(childGroupItem.getLinkId().equals("15.1")){
									for (QuestionnaireResponse.QuestionnaireResponseItemComponent answerItem : childGroupItem.getItem()){
										if(answerItem.getLinkId().equals("15.1.1")){
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

			if(imageSize == byteSize){
				for(QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()){
					if(item.getLinkId().equals("birth-certificate-details")){
						for(QuestionnaireResponse.QuestionnaireResponseItemComponent parentGroupItem : item.getItem()){
							if(parentGroupItem.getLinkId().equals("15.0")){
								for(QuestionnaireResponse.QuestionnaireResponseItemComponent childGroupItem : parentGroupItem.getItem()){
									if(childGroupItem.getLinkId().equals("15.1")){
										for (QuestionnaireResponse.QuestionnaireResponseItemComponent answerItem : childGroupItem.getItem()){
											if(answerItem.getLinkId().equals("15.1.1")){
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
						break;
					}
				}
			}
			else {
				logger.warn("Image Not Proper");
			}
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
			logger.warn(ExceptionUtils.getStackTrace(ex));
		}
		return digest;
	}
}