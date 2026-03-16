package ca.uhn.fhir.jpa.starter.interceptors;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.EventDefinition;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.ResearchDefinition;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.model.Substance;
import org.hl7.fhir.r4.model.TerminologyCapabilities;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.GraphDefinition;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationDefinition;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.SearchParameter;
import org.hl7.fhir.r4.model.ImagingStudy;

public final class Constants {
    public final static String ADMINISTRATOR_REQUEST_HEADER = "X-Administrator-Access";
    public final static String ORGANIZATION_REQUEST_HEADER = "X-Organization-ID";
    public final static String ORGANIZATION_SECURITY_SYSTEM_NAME = "http://terminology.kemkes.go.id/org-id";
    public final static String ORGANIZATION_MANAGING_ID_KEMENKES = "9900000000";
    public final static String PATIENT_SECURITY_SYSTEM_NAME = "http://terminology.kemkes.go.id/patient-id";

    public final static String URLCONSENT_REGISTRY = "http://localhost:7272/api/consent";

    public final static List<Class<? extends DomainResource>> FHIR_RESOURCE_CONFORMANNCE = new ArrayList<Class<? extends DomainResource>>() {
        {
            add(CapabilityStatement.class);
            add(GraphDefinition.class);
            add(StructureDefinition.class);
            add(ImplementationGuide.class);
            add(OperationDefinition.class);
            add(SearchParameter.class);
            add(StructureMap.class);
        }
    };

    public final static List<Class<? extends DomainResource>> FHIR_RESOURCE_TERMINOLOGY = new ArrayList<Class<? extends DomainResource>>() {
        {
            add(CodeSystem.class);
            add(ValueSet.class);
            add(ConceptMap.class);
            add(NamingSystem.class);
            add(TerminologyCapabilities.class);
        }
    };

    public final static List<Class<? extends DomainResource>> FHIR_RESOURCE_CLINICAL_REASONING = new ArrayList<Class<? extends DomainResource>>() {
        {
            add(PlanDefinition.class);
            add(EventDefinition.class);
            add(ResearchDefinition.class);
        }
    };

    // Boleh 'read' melalui pencarian atau get detail oleh semua organisasi
    public final static List<Class<? extends DomainResource>> FHIR_RESOURCE_WHITELIST = new ArrayList<Class<? extends DomainResource>>() {
        {
            add(AllergyIntolerance.class);
            add(FamilyMemberHistory.class);

            // Karena keterbatasan DaoRegistry untuk mencari ServiceRequest di sub-sub-parameter processing.aditive, 
            // maka sementara Substance masuk WHITELIST sampai ditemukan metode yang efisien untuk mencari ServiceRequest
            add(Substance.class);
        }
    };

    // Resource yang memiliki reference langsung ke Encounter
    public final static List<Class<? extends DomainResource>> FHIR_RESOURCE_HAS_ENCOUNTER = new ArrayList<Class<? extends DomainResource>>() {
        {
            add(Condition.class);
            add(Procedure.class);
            add(QuestionnaireResponse.class);
            add(Immunization.class);
            add(ClinicalImpression.class);
            add(CarePlan.class);
            add(NutritionOrder.class);
            add(ServiceRequest.class);
            add(DiagnosticReport.class);
            add(Observation.class);
            add(MedicationRequest.class);
            add(MedicationStatement.class);
            add(MedicationAdministration.class);
            add(MedicationDispense.class);
            add(RiskAssessment.class);
            add(Composition.class);
            add(ImagingStudy.class);
        }
    };
}
