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
import autovalue.shaded.kotlin.Triple;
import ca.uhn.fhir.jpa.starter.model.*;

import com.iprd.fhir.utils.FhirUtils;
import com.iprd.fhir.utils.PatientIdentifierStatus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	FhirClientAuthenticatorService fhirClientAuthenticatorService;

	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
	public void insert(IBaseResource theResource) throws IOException {
		notificationDataSource = NotificationDataSource.getInstance();
		if (theResource.fhirType().equals("Media")) {
			processMedia((Media) theResource);
		} else if (theResource.fhirType().equals("Encounter")) {
			Encounter encounter = (Encounter) theResource;
			if(encounter.getMeta().hasTag() && encounter.getMeta().getTag().get(0).getCode().equals("patient-registration")){
				String serviceProviderId = encounter.getServiceProvider().getReferenceElement().getIdPart();
				String patientId = encounter.getSubject().getReferenceElement().getIdPart();
				try {
					List<PatientIdentifierEntity> getExistingRowsToUpdateOrgId = notificationDataSource.getExistingEntriesWithPatientId(patientId);
					ArrayList<PatientIdentifierEntity> PatientIdentifierEntitiesToUpdate = new ArrayList<PatientIdentifierEntity>();
					for (PatientIdentifierEntity item : getExistingRowsToUpdateOrgId) {
						item.setOrgId(serviceProviderId);
						PatientIdentifierEntitiesToUpdate.add(item);
					}
					notificationDataSource.updateObjects(PatientIdentifierEntitiesToUpdate);
				} catch (HibernateException e) {
					logger.warn(ExceptionUtils.getStackTrace(e));
				}
			}
			String encounterId = encounter.getIdElement().getIdPart();
			String patientId = encounter.getSubject().getReferenceElement().getIdPart();
			Date currentDate = DateUtilityHelper.getCurrentSqlDate();
			String messageStatus = ComGenerator.MessageStatus.PENDING.name();

			if (!isEncounterMigrated(encounter)) {
				try {
					EncounterIdEntity encounterIdEntity = new EncounterIdEntity(encounterId);
					notificationDataSource.persist(encounterIdEntity);
					ComGenerator comGen = new ComGenerator("Encounter", encounterId, currentDate, messageStatus,
							patientId, null);

					notificationDataSource.persist(comGen);

				} catch (Exception e) {
					logger.warn(ExceptionUtils.getStackTrace(e));
				}
			}
		} else if (theResource.fhirType().equals("Appointment")) {
			generateAndInsertAppointmentNotifications((Appointment) theResource);
		} else if (theResource.fhirType().equals("QuestionnaireResponse")) {
			processQuestionnaireResponse((QuestionnaireResponse) theResource);
		} else if (theResource.fhirType().equals("Patient")) {
			processPatientInsert(theResource);
		}
	}

	public void generateAndInsertAppointmentNotifications(Appointment appointment){
		String appointmentId = appointment.getIdElement().getIdPart();
		String patientId = appointment.getParticipant().get(0).getActor().getReferenceElement().getIdPart();
		Timestamp appointmentScheduledDateTime = DateUtilityHelper.utilDateToTimestamp(appointment.getStart());

		Date currentDate = DateUtilityHelper.getCurrentSqlDate();
		Date previousDate = DateUtilityHelper.getPreviousDay(appointmentScheduledDateTime);
		Date appointmentDate = DateUtilityHelper.timeStampToDate(appointmentScheduledDateTime);

		String messageStatus = ComGenerator.MessageStatus.PENDING.name();

		ComGenerator comGen = new ComGenerator("Appointment", appointmentId, currentDate, messageStatus, patientId,
			appointmentScheduledDateTime);

		ComGenerator firstReminder = new ComGenerator("Appointment", appointmentId, previousDate, messageStatus,
			patientId, appointmentScheduledDateTime);

		ComGenerator secondReminder = new ComGenerator("Appointment", appointmentId, appointmentDate, messageStatus,
			patientId, appointmentScheduledDateTime);

		notificationDataSource.persist(comGen);
		if (currentDate != previousDate) {
			notificationDataSource.persist(firstReminder);
		}
		if (currentDate != appointmentDate) {
			notificationDataSource.persist(secondReminder);
		}
	}
	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
	public void update(IBaseResource theOldResource, IBaseResource theResource) throws IOException {
		notificationDataSource = NotificationDataSource.getInstance();
		if (theResource.fhirType().equals("Media")) {
			processMedia((Media) theResource);
		} else if (theResource.fhirType().equals("Encounter")) {
			Encounter encounter = (Encounter) theResource;
			String encounterId = encounter.getIdElement().getIdPart();

			if (!isEncounterMigrated(encounter)) {
				EncounterIdEntity encounterIdEntity = new EncounterIdEntity(encounterId);
				// Using persist to add entry only if it is not exists
				notificationDataSource.persist(encounterIdEntity);
			}
		} else if (theResource.fhirType().equals("Appointment")) {
			Appointment oldAppointment = (Appointment) theOldResource;
			Appointment updatedAppointment = (Appointment) theResource;
			if(!oldAppointment.getStart().equals(updatedAppointment.getStart())){
				String oldAppointmentId = oldAppointment.getIdElement().getIdPart();
				try {
					List<ComGenerator> records = notificationDataSource.fetchRecordsByResourceId(oldAppointmentId, ComGenerator.MessageStatus.PENDING);
					ArrayList<ComGenerator> comGeneratorEntitiesToUpdate = new ArrayList<ComGenerator>();
					for (ComGenerator record: records) {
						record.setCommunicationStatus(ComGenerator.MessageStatus.DELETED.name());
						comGeneratorEntitiesToUpdate.add(record);
					}
					if (!comGeneratorEntitiesToUpdate.isEmpty()) {
						notificationDataSource.updateObjectsWithSession(comGeneratorEntitiesToUpdate);
					}
				} catch (HibernateException e) {
					logger.warn(ExceptionUtils.getStackTrace(e));
				}
				generateAndInsertAppointmentNotifications(updatedAppointment);
			}

		} else if (theResource.fhirType().equals("QuestionnaireResponse")) {
			processQuestionnaireResponse((QuestionnaireResponse) theResource);
		} else if (theResource.fhirType().equals("Patient")) {
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
		for (Identifier identifier : encounter.getIdentifier()) {
			if (identifier.hasSystem() && identifier.getSystem().equals(ENCOUNTER_MIGRATED_SYSTEM)
					&& identifier.hasValue() && identifier.getValue().equals(ENCOUNTER_MIGRATED_VALUE)) {
				return true;
			}
		}
		return false;
	}

	private void processMedia(Media media) throws IOException {
		if(media.getContent().hasData() && media.getType().getCodingFirstRep().getCode().equals("image")) {
			byte[] bitmapdata = media.getContent().getDataElement().getValue();
			byte[] base64 = Base64.decode(bitmapdata, Base64.DEFAULT);
			String md5Hash = md5Bytes(base64);
			if (md5Hash == null) {
				return;
			}
			File image = new File(imagePath, md5Hash + ".jpeg");
			FileUtils.writeByteArrayToFile(image, base64);
			String imagePath = image.getAbsolutePath();
			long imageSize = Files.size(Paths.get(imagePath));
			long byteSize = base64.length;
			if (imageSize == byteSize) {
				media.getContent().setDataElement(null);
				media.getContent().setUrl(imagePath);
			} else {
				logger.warn("Image Not Proper");
			}
		}
	}

	private void processPatientInsert(IBaseResource theResource) {
		Patient patient = (Patient) theResource;
		String patientId = patient.getIdElement().getIdPart();
		if (FhirUtils.isOclPatient(patient.getIdentifier())) {
			return;
		}

		Triple<String, String, String> patientOclId = FhirUtils.getOclIdFromIdentifier(patient.getIdentifier());
		String patientCardNumber = FhirUtils.getPatientCardNumber(patient.getIdentifier());
		Long currentEpochTime = System.currentTimeMillis();

		if (patientOclId != null && patientOclId.getFirst() != null) {
			PatientIdentifierEntity patientIdentifierEntityOcl = new PatientIdentifierEntity(patientId,
					patientOclId.getFirst(), PatientIdentifierEntity.PatientIdentifierType.OCL_ID.name(),
					patientOclId.getSecond(), patientOclId.getThird(),
					(notificationDataSource.getPatientIdWithIdentifier(patientId, patientOclId.getFirst()).size() >= 1)
							? PatientIdentifierStatus.DUPLICATE.name()
							: PatientIdentifierStatus.OK.name(),
					currentEpochTime, currentEpochTime, null);
			notificationDataSource.persist(patientIdentifierEntityOcl);
		}

		if (patientCardNumber != null) {
			PatientIdentifierEntity patientIdentifierEntityCardNumber = new PatientIdentifierEntity(patientId,
					patientCardNumber, PatientIdentifierEntity.PatientIdentifierType.PATIENT_CARD_NUM.name(), null,
					null,
					(notificationDataSource.getPatientIdWithIdentifier(patientId, patientCardNumber).size() >= 1)
							? PatientIdentifierStatus.DUPLICATE.name()
							: PatientIdentifierStatus.OK.name(),
					currentEpochTime, currentEpochTime, null);
			notificationDataSource.persist(patientIdentifierEntityCardNumber);
		}
	}

	private void processPatientUpdate(IBaseResource theOldResource, IBaseResource theResource) {
		Patient oldPatient = (Patient) theOldResource;
		Patient updatedPatient = (Patient) theResource;
		String organizationId = null;
		if (FhirUtils.isOclPatient(oldPatient.getIdentifier())
				&& !FhirUtils.isOclPatient(updatedPatient.getIdentifier())) {
			// If the use updates the temporary patient from the mobile, the identifier will
			// be removed. So adding it back
			Identifier oclPatientIdentifier = new Identifier()
					.setSystem("http://iprdgroup.com/identifiers/patientWithOcl").setValue("patient_with_ocl");
			updatedPatient.addIdentifier(oclPatientIdentifier);
			// Returning form this block because for temporary patient no need to keep track
			// of duplicate identifier.
			return;
		}

		String patientId = updatedPatient.getIdElement().getIdPart();

		Bundle searchBundle = fhirClientAuthenticatorService.getFhirClient().search()
			.byUrl("Encounter?subject=" +patientId )
			.returnBundle(Bundle.class)
			.execute();
		List<Bundle.BundleEntryComponent> entries = searchBundle.getEntry();
		if (entries != null && !entries.isEmpty()) {
			Encounter encounterResource = (Encounter) entries.get(0).getResource();
			if (encounterResource != null) {
				organizationId = encounterResource.getServiceProvider().getReferenceElement().getIdPart();
			}
		}

		Triple<String, String, String> oldPatientOclId = FhirUtils.getOclIdFromIdentifier(oldPatient.getIdentifier());
		Triple<String, String, String> updatedPatientOclId = FhirUtils
				.getOclIdFromIdentifier(updatedPatient.getIdentifier());

		String oldPatientCardNumber = FhirUtils.getPatientCardNumber(oldPatient.getIdentifier());
		String updatedPatientCardNumber = FhirUtils.getPatientCardNumber(updatedPatient.getIdentifier());

		if (!Objects.equals(oldPatientOclId, updatedPatientOclId)) {
			List<PatientIdentifierEntity> patientIdentifierEntityList = (oldPatientOclId == null) ? new ArrayList()
					: notificationDataSource.getPatientIdentifierEntityByPatientIdAndIdentifier(patientId,
							oldPatientOclId.getFirst());
			PatientIdentifierEntity patientIdentifierEntity = (!patientIdentifierEntityList.isEmpty())
					? patientIdentifierEntityList.get(0)
					: null;

			if (patientIdentifierEntity != null) {
				patientIdentifierEntity.setStatus(PatientIdentifierStatus.DELETE.name());
				patientIdentifierEntity.setUpdatedTime(System.currentTimeMillis());
				notificationDataSource.update(patientIdentifierEntity);

				if (PatientIdentifierStatus.OK.name().equals(patientIdentifierEntity.getStatus())) {
					PatientIdentifierEntity entryWithDuplicateStatus = notificationDataSource
							.getPatientIdentifierEntityWithDuplicateStatus(patientId, oldPatientOclId.getFirst());

					if (entryWithDuplicateStatus != null) {
						entryWithDuplicateStatus.setStatus(PatientIdentifierStatus.OK.name());
						entryWithDuplicateStatus.setUpdatedTime(System.currentTimeMillis());
						notificationDataSource.update(entryWithDuplicateStatus);
					}
				}
			}

			if (updatedPatientOclId != null) {
				long currentTime = System.currentTimeMillis();

				PatientIdentifierEntity newPatientIdentifierEntity = new PatientIdentifierEntity(patientId,
						updatedPatientOclId.getFirst(), PatientIdentifierEntity.PatientIdentifierType.OCL_ID.name(),
						updatedPatientOclId.getSecond(), updatedPatientOclId.getThird(),
						PatientIdentifierStatus.OK.name(), currentTime, currentTime, organizationId);
				notificationDataSource.persist(newPatientIdentifierEntity);
			}
		}

		if (!Objects.equals(oldPatientCardNumber, updatedPatientCardNumber)) {
			List<PatientIdentifierEntity> patientIdentifierEntityList = notificationDataSource
					.getPatientIdentifierEntityByPatientIdAndIdentifier(patientId, oldPatientCardNumber);
			PatientIdentifierEntity patientIdentifierEntity = (!patientIdentifierEntityList.isEmpty())
					? patientIdentifierEntityList.get(0)
					: null;

			if (patientIdentifierEntity != null) {
				patientIdentifierEntity.setStatus(PatientIdentifierStatus.DELETE.name());
				patientIdentifierEntity.setUpdatedTime(System.currentTimeMillis());
				notificationDataSource.update(patientIdentifierEntity);

				if (PatientIdentifierStatus.OK.name().equals(patientIdentifierEntity.getStatus())) {
					PatientIdentifierEntity entryWithDuplicateStatus = notificationDataSource
							.getPatientIdentifierEntityWithDuplicateStatus(patientId, oldPatientCardNumber);
					if (entryWithDuplicateStatus != null) {
						entryWithDuplicateStatus.setStatus(PatientIdentifierStatus.OK.name());
						entryWithDuplicateStatus.setUpdatedTime(System.currentTimeMillis());
						notificationDataSource.update(entryWithDuplicateStatus);
					}
				}
			}
			if (updatedPatientCardNumber != null) {
				long currentTime = System.currentTimeMillis();

				PatientIdentifierEntity newPatientIdentifierEntity = new PatientIdentifierEntity(patientId,
						updatedPatientCardNumber, PatientIdentifierEntity.PatientIdentifierType.PATIENT_CARD_NUM.name(),
						null, null, PatientIdentifierStatus.OK.name(), currentTime, currentTime, organizationId);
				notificationDataSource.persist(newPatientIdentifierEntity);
			}
		}
	}

	private void processQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) throws IOException {
		if (questionnaireResponse == null)
			return;
		if (questionnaireResponse.getQuestionnaire() == null)
			return;
		if (questionnaireResponse.getQuestionnaire().equals("Questionnaire/labour")) {
			byte[] bitmapdata = null;

			for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
				if (item.getLinkId().equals("8.0")) {
					for (QuestionnaireResponse.QuestionnaireResponseItemComponent groupItem : item.getItem()) {
						if (groupItem.getLinkId().equals("8.2")) {
							for (QuestionnaireResponse.QuestionnaireResponseItemComponent answerItem : groupItem
									.getItem()) {
								if (answerItem.getLinkId().equals("8.2.1")) {
									List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answers = answerItem
											.getAnswer();
									if (answers != null && !answers.isEmpty()) {
										QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = answers
												.get(0);
										if (answer.getValueAttachment() != null
												&& answer.getValueAttachment().getData() != null) {
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

			File image = new File(imagePath + "//" + md5Hash + ".jpeg");
			FileUtils.writeByteArrayToFile(image, base64);
			String imagePath = image.getAbsolutePath();
			long imageSize = Files.size(Paths.get(imagePath));
			long byteSize = base64.length;
			if (imageSize == byteSize) {
				for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
					if (item.getLinkId().equals("8.0")) {
						for (QuestionnaireResponse.QuestionnaireResponseItemComponent groupItem : item.getItem()) {
							if (groupItem.getLinkId().equals("8.2")) {
								for (QuestionnaireResponse.QuestionnaireResponseItemComponent answerItem : groupItem
										.getItem()) {
									if (answerItem.getLinkId().equals("8.2.1")) {
										List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answers = answerItem
												.getAnswer();
										if (answers != null && !answers.isEmpty()) {
											QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = answers
													.get(0);
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
			} else {
				logger.warn("Image Not Proper");
			}
		}

		if (questionnaireResponse.getQuestionnaire().equals("Questionnaire/childBirth-registration")) {
			byte[] bitmapdata = null;
			for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
				if (item.getLinkId().equals("birth-certificate-details")) {
					for (QuestionnaireResponse.QuestionnaireResponseItemComponent parentGroupItem : item.getItem()) {
						if (parentGroupItem.getLinkId().equals("15.0")) {
							for (QuestionnaireResponse.QuestionnaireResponseItemComponent childGroupItem : parentGroupItem
									.getItem()) {
								if (childGroupItem.getLinkId().equals("15.1")) {
									for (QuestionnaireResponse.QuestionnaireResponseItemComponent answerItem : childGroupItem
											.getItem()) {
										if (answerItem.getLinkId().equals("15.1.1")) {
											List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answers = answerItem
													.getAnswer();
											if (answers != null && !answers.isEmpty()) {
												QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = answers
														.get(0);
												if (answer.getValueAttachment() != null
														&& answer.getValueAttachment().getData() != null) {
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

			File image = new File(imagePath + "//" + md5Hash + ".jpeg");
			FileUtils.writeByteArrayToFile(image, base64);
			String imagePath = image.getAbsolutePath();
			long imageSize = Files.size(Paths.get(imagePath));
			long byteSize = base64.length;

			if (imageSize == byteSize) {
				for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
					if (item.getLinkId().equals("birth-certificate-details")) {
						for (QuestionnaireResponse.QuestionnaireResponseItemComponent parentGroupItem : item
								.getItem()) {
							if (parentGroupItem.getLinkId().equals("15.0")) {
								for (QuestionnaireResponse.QuestionnaireResponseItemComponent childGroupItem : parentGroupItem
										.getItem()) {
									if (childGroupItem.getLinkId().equals("15.1")) {
										for (QuestionnaireResponse.QuestionnaireResponseItemComponent answerItem : childGroupItem
												.getItem()) {
											if (answerItem.getLinkId().equals("15.1.1")) {
												List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answers = answerItem
														.getAnswer();
												if (answers != null && !answers.isEmpty()) {
													QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = answers
															.get(0);
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
			} else {
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
			for (byte b : hash) {
				stringBuilder.append(String.format("%02x", b));
			}
			digest = stringBuilder.toString();
		} catch (NoSuchAlgorithmException ex) {
			logger.warn(ExceptionUtils.getStackTrace(ex));
		}
		return digest;
	}
}