package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.model.EncounterIdEntity;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.FhirUtils;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ResourceMapperService {
	private static final Logger logger = LoggerFactory.getLogger(ResourceMapperService.class);
	private static final long DELAY = 10 * 60000;

	/**
	 * Maps the resources of temporary patient to actual patient when the patient not found on client and service is provided with the OCL-ID
	 */
	@Scheduled(fixedDelay = DELAY, initialDelay = DELAY)
	public void mapResourcesToPatient() {
		//Searching for patient created with OCL-ID
		Bundle tempPatientBundle = new Bundle();
		FhirUtils.getBundleBySearchUrl(tempPatientBundle, FhirClientAuthenticatorService.serverBase + "/Patient?identifier=patient_with_ocl");

		for (Bundle.BundleEntryComponent entry : tempPatientBundle.getEntry()) {
			// per patient loop.
			Patient tempPatient = (Patient) entry.getResource();
			String tempPatientId = tempPatient.getIdElement().getIdPart();
			String oclId = tempPatient.getIdentifier().get(0).getValue();
			//Searching for actual patient with OCL-ID
			String actualPatientId = getActualPatientId(oclId);
			Reference patientReference = new Reference("Patient/"+actualPatientId);
			if (actualPatientId == null) {
				continue;
			}

			Bundle questionnaireResponseBundle =
				FhirClientAuthenticatorService.getFhirClient()
					.search()
					.forResource(QuestionnaireResponse.class)
					.where(QuestionnaireResponse.PATIENT.hasId(tempPatientId))
					.returnBundle(Bundle.class)
					.execute();
			if (questionnaireResponseBundle.hasEntry() && questionnaireResponseBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: questionnaireResponseBundle.getEntry()) {
					QuestionnaireResponse questionnaireResponse = (QuestionnaireResponse) entryComponent.getResource();
					questionnaireResponse.setSubject(patientReference);
					FhirClientAuthenticatorService.getFhirClient()
						.update().resource(questionnaireResponse).execute();
				}
			}

			Bundle encounterBundle =
				FhirClientAuthenticatorService.getFhirClient()
					.search()
					.forResource(Encounter.class)
					.where(Encounter.SUBJECT.hasId(tempPatientId))
					.returnBundle(Bundle.class)
					.execute();
			if (encounterBundle.hasEntry() && encounterBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: encounterBundle.getEntry()) {
					Encounter encounter = (Encounter) entryComponent.getResource();
					encounter.setSubject(patientReference);
					FhirClientAuthenticatorService.getFhirClient().update().resource(encounter).execute();
				}
			}

			Bundle procedureBundle =
				FhirClientAuthenticatorService.getFhirClient()
					.search()
					.forResource(Procedure.class)
					.where(Procedure.SUBJECT.hasId(tempPatientId))
					.returnBundle(Bundle.class)
					.execute();
			if (procedureBundle.hasEntry() && procedureBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: procedureBundle.getEntry()) {
					Procedure procedure = (Procedure) entryComponent.getResource();
					procedure.setSubject(patientReference);
					FhirClientAuthenticatorService.getFhirClient().update().resource(procedure).execute();
				}
			}

			Bundle observationBundle =
				FhirClientAuthenticatorService.getFhirClient()
					.search()
					.forResource(Observation.class)
					.where(Observation.SUBJECT.hasId(tempPatientId))
					.returnBundle(Bundle.class)
					.execute();
			if (observationBundle.hasEntry() && observationBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: observationBundle.getEntry()) {
					Observation observation = (Observation) entryComponent.getResource();
					observation.setSubject(patientReference);
					FhirClientAuthenticatorService.getFhirClient().update().resource(observation).execute();
				}
			}

			Bundle conditionBundle =
				FhirClientAuthenticatorService.getFhirClient()
					.search()
					.forResource(Condition.class)
					.where(Observation.SUBJECT.hasId(tempPatientId))
					.returnBundle(Bundle.class)
					.execute();
			if (conditionBundle.hasEntry() && conditionBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: conditionBundle.getEntry()) {
					Condition condition = (Condition) entryComponent.getResource();
					condition.setSubject(patientReference);
					FhirClientAuthenticatorService.getFhirClient().update().resource(condition).execute();
				}
			}

			Bundle immunizationBundle = FhirClientAuthenticatorService.getFhirClient()
				.search()
				.forResource(Immunization.class)
				.where(Immunization.PATIENT.hasId(tempPatientId))
				.returnBundle(Bundle.class)
				.execute();
			if (immunizationBundle.hasEntry() && immunizationBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: immunizationBundle.getEntry()) {
					Immunization immunization = (Immunization) entryComponent.getResource();
					immunization.setPatient(patientReference);
					FhirClientAuthenticatorService.getFhirClient().update().resource(immunization).execute();
				}
			}

			Bundle appointmentBundle = FhirClientAuthenticatorService.getFhirClient()
				.search()
				.forResource(Appointment.class)
				.where(Appointment.PATIENT.hasId(tempPatientId))
				.returnBundle(Bundle.class)
				.execute();
			if (appointmentBundle.hasEntry() && appointmentBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: appointmentBundle.getEntry()) {
					Appointment appointment = (Appointment) entryComponent.getResource();
					appointment.getParticipant().get(0).getActor().setReference("Patient/"+actualPatientId);
					FhirClientAuthenticatorService.getFhirClient().update().resource(appointment).execute();
				}
			}

			Bundle documentReferenceBundle =
				FhirClientAuthenticatorService.getFhirClient()
					.search()
					.forResource(DocumentReference.class)
					.where(DocumentReference.SUBJECT.hasId(tempPatientId))
					.returnBundle(Bundle.class)
					.execute();
			if (documentReferenceBundle.hasEntry() && documentReferenceBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: documentReferenceBundle.getEntry()) {
					DocumentReference documentReference = (DocumentReference) entryComponent.getResource();
					documentReference.setSubject(patientReference);
					FhirClientAuthenticatorService.getFhirClient().update().resource(documentReference).execute();
				}
			}

			Bundle mediaBundle =
				FhirClientAuthenticatorService.getFhirClient()
					.search()
					.forResource(Media.class)
					.where(Media.SUBJECT.hasId(tempPatientId))
					.returnBundle(Bundle.class)
					.execute();
			if (mediaBundle.hasEntry() && mediaBundle.getEntry().size() > 0) {
				for (Bundle.BundleEntryComponent entryComponent: mediaBundle.getEntry()) {
					Media media = (Media) entryComponent.getResource();
					media.setSubject(patientReference);
					FhirClientAuthenticatorService.getFhirClient()
						.update().resource(media).execute();
				}
			}
		}
	}

	/**
	 *  Updates partOf reference in the encounter if multiple encounters created for a patient on same day.
	*/
	@Scheduled(fixedDelay = DELAY, initialDelay = DELAY)
	public void mapEncounters() {
		NotificationDataSource notificationDataSource = NotificationDataSource.getInstance();
		List<EncounterIdEntity> encounterIdEntityList = notificationDataSource.fetchAllFromEncounterIdEntity();
		if (encounterIdEntityList.isEmpty()) {
			return;
		}
		for (EncounterIdEntity encounterIdEntity : encounterIdEntityList) {
			try {
				Encounter encounterCreated = FhirClientAuthenticatorService.getFhirClient()
					.read()
					.resource(Encounter.class)
					.withId(encounterIdEntity.getEncounterId())
					.execute();
				Bundle encounterBundle = FhirClientAuthenticatorService.getFhirClient()
					.search()
					.forResource(Encounter.class)
					.where(
						Encounter.DATE.exactly()
							.day(DateUtilityHelper.toDateString(encounterCreated.getPeriod().getStart(), "yyyy-MM-dd"))
					).and(Encounter.PATIENT.hasId(encounterCreated.getSubject().getReferenceElement().getIdPart()))
					.returnBundle(Bundle.class)
					.execute();
				if (!encounterBundle.hasEntry() || encounterBundle.getEntry().size() <= 1) {
					notificationDataSource.delete(encounterIdEntity);
					continue;
				}
				List<Encounter> encountersOfPatient =
					encounterBundle.getEntry().stream()
						.map(Bundle.BundleEntryComponent::getResource)
						.map(resource -> (Encounter) resource)
						.sorted(Comparator.comparing(encounter -> encounter.getPeriod().getStart()))
						.collect(Collectors.toList());

				Encounter parentEncounter = encountersOfPatient.get(0);
				String parentEncounterId = parentEncounter.getIdElement().getIdPart();
				Reference parentEncounterReference = new Reference("Encounter/" + parentEncounterId);
				if (parentEncounter.getPartOf().getReference() != null) {
					parentEncounter.setPartOf(null);
					FhirClientAuthenticatorService.getFhirClient().update().resource(parentEncounter).execute();
				}
				for (int i = 1; i < encountersOfPatient.size(); i++) {
					Encounter encounter = encountersOfPatient.get(i);
					if (
						!parentEncounterId
							.equals(encounter.getPartOf().getReferenceElement().getIdPart())
					) {
						encounter.setPartOf(parentEncounterReference);
						FhirClientAuthenticatorService.getFhirClient().update().resource(encounter).execute();
					}
				}

				notificationDataSource.delete(encounterIdEntity);

			} catch (ResourceNotFoundException ex) {
				ex.printStackTrace();
			} catch (FhirClientConnectionException ex) {
				// FhirClientConnectionException internally throws SocketTimeoutException: Read timeout
				ex.printStackTrace();
			}
		}
	}

	private String getActualPatientId(String oclId) {
		Bundle patientBundle = new Bundle();
		String queryPath = "/Patient?";
		queryPath += "identifierPartial:contains=" + oclId + "&";
		queryPath += "identifier:not=patient_with_ocl";
		FhirUtils.getBundleBySearchUrl(patientBundle, FhirClientAuthenticatorService.serverBase + queryPath);
		if (patientBundle.hasEntry() && patientBundle.getEntry().size() > 0) {
			Patient patient = (Patient) patientBundle.getEntry().get(0).getResource();
			return patient.getIdElement().getIdPart();
		}

		for (Bundle.BundleEntryComponent entry : patientBundle.getEntry()) {
			Patient patient = (Patient) entry.getResource();
			if (isActualPatient(patient, oclId))
				return patient.getIdElement().getIdPart();
		}
		return null;
	}

	private boolean isActualPatient(Patient patient, String oclId) {
		for (Identifier identifier : patient.getIdentifier()) {
			if (identifier.getValue().equals("patient_with_ocl")) {
				return false;
			}
		}
		return true;
	}
}
