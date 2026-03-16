package ca.uhn.fhir.jpa.starter.interceptors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.ImagingStudy;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Substance;

public final class Utils {
    public static String getPatientId(IBaseResource theResource) {
        if (theResource == null) {
            return null;
        }

        Reference patientRef = null;

        if (theResource instanceof Encounter) {
            patientRef = ((Encounter) theResource).getSubject();
        } else if (theResource instanceof Condition) {
            patientRef = ((Condition) theResource).getSubject();
        } else if (theResource instanceof Procedure) {
            patientRef = ((Procedure) theResource).getSubject();
        } else if (theResource instanceof QuestionnaireResponse) {
            patientRef = ((QuestionnaireResponse) theResource).getSubject();
        } else if (theResource instanceof Immunization) {
            patientRef = ((Immunization) theResource).getPatient();
        } else if (theResource instanceof AllergyIntolerance) {
            patientRef = ((AllergyIntolerance) theResource).getPatient();
        } else if (theResource instanceof ClinicalImpression) {
            patientRef = ((ClinicalImpression) theResource).getSubject();
        } else if (theResource instanceof CarePlan) {
            patientRef = ((CarePlan) theResource).getSubject();
        } else if (theResource instanceof Goal) {
            patientRef = ((Goal) theResource).getSubject();
        } else if (theResource instanceof NutritionOrder) {
            patientRef = ((NutritionOrder) theResource).getPatient();
        } else if (theResource instanceof ServiceRequest) {
            patientRef = ((ServiceRequest) theResource).getSubject();
        } else if (theResource instanceof Specimen) {
            patientRef = ((Specimen) theResource).getSubject();
        } else if (theResource instanceof Substance) {
            // patientRef = ((Substance) theResource).getPatient();
        } else if (theResource instanceof DiagnosticReport) {
            patientRef = ((DiagnosticReport) theResource).getSubject();
        } else if (theResource instanceof Observation) {
            patientRef = ((Observation) theResource).getSubject();
        } else if (theResource instanceof Composition) {
            patientRef = ((Composition) theResource).getSubject();
        } else if (theResource instanceof Medication) {
            // patientRef = ((Medication) theResource).getSubject();
        } else if (theResource instanceof MedicationRequest) {
            patientRef = ((MedicationRequest) theResource).getSubject();
        } else if (theResource instanceof MedicationStatement) {
            patientRef = ((MedicationStatement) theResource).getSubject();
        } else if (theResource instanceof MedicationAdministration) {
            patientRef = ((MedicationAdministration) theResource).getSubject();
        } else if (theResource instanceof MedicationDispense) {
            patientRef = ((MedicationDispense) theResource).getSubject();
        } else if (theResource instanceof RiskAssessment) {
            patientRef = ((RiskAssessment) theResource).getSubject();
        } else if (theResource instanceof ImagingStudy) {
            patientRef = ((ImagingStudy) theResource).getSubject();
        } else if (theResource instanceof FamilyMemberHistory) {
            patientRef = ((FamilyMemberHistory) theResource).getPatient();
        }

        if (patientRef != null && patientRef.hasReference()) {
            String reference = patientRef.getReference();
            if (reference.startsWith("Patient/")) {
                return reference.substring("Patient/".length());
            }
            return reference;
        }

        return null;
    }

    public static String getEncounterId(IBaseResource theResource) {
        if (theResource == null) {
            return null;
        }

        Reference encounterRef = null;

        if (theResource instanceof Condition) {
            encounterRef = ((Condition) theResource).getEncounter();
        } else if (theResource instanceof Procedure) {
            encounterRef = ((Procedure) theResource).getEncounter();
        } else if (theResource instanceof QuestionnaireResponse) {
            encounterRef = ((QuestionnaireResponse) theResource).getEncounter();
        } else if (theResource instanceof Immunization) {
            encounterRef = ((Immunization) theResource).getEncounter();
        } else if (theResource instanceof AllergyIntolerance) {
            encounterRef = ((AllergyIntolerance) theResource).getEncounter();
        } else if (theResource instanceof ClinicalImpression) {
            encounterRef = ((ClinicalImpression) theResource).getEncounter();
        } else if (theResource instanceof CarePlan) {
            encounterRef = ((CarePlan) theResource).getEncounter();
        } else if (theResource instanceof NutritionOrder) {
            encounterRef = ((NutritionOrder) theResource).getEncounter();
        } else if (theResource instanceof ServiceRequest) {
            encounterRef = ((ServiceRequest) theResource).getEncounter();
        } else if (theResource instanceof DiagnosticReport) {
            encounterRef = ((DiagnosticReport) theResource).getEncounter();
        } else if (theResource instanceof Observation) {
            encounterRef = ((Observation) theResource).getEncounter();
        } else if (theResource instanceof MedicationRequest) {
            encounterRef = ((MedicationRequest) theResource).getEncounter();
        } else if (theResource instanceof MedicationStatement) {
            Reference ref = ((MedicationStatement) theResource).getContext();
            if (ref != null && ref.hasReference()  && ref.getReference().startsWith("Encounter/")) {
                encounterRef = ref;
            }
        } else if (theResource instanceof MedicationAdministration) {
            Reference ref = ((MedicationAdministration) theResource).getContext();
            if (ref != null && ref.hasReference()  && ref.getReference().startsWith("Encounter/")) {
                encounterRef = ref;
            }
        } else if (theResource instanceof MedicationDispense) {
            Reference ref = ((MedicationDispense) theResource).getContext();
            if (ref != null && ref.hasReference() && ref.getReference().startsWith("Encounter/")) {
                encounterRef = ref;
            }
        } else if (theResource instanceof RiskAssessment) {
            encounterRef = ((RiskAssessment) theResource).getEncounter();
        } else if (theResource instanceof ImagingStudy) {
            encounterRef = ((ImagingStudy) theResource).getEncounter();
        }

        if (encounterRef != null && encounterRef.hasReference()) {
            String reference = encounterRef.getReference();
            // Extract ID from reference (e.g., "Encounter/123" -> "123")
            if (reference.startsWith("Encounter/")) {
                return reference.substring("Encounter/".length());
            }
            return reference;
        }

        return null;
    }
}
