package ca.uhn.fhir.jpa.starter.interceptors;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Substance;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.ImagingStudy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Consent Enforcement Interceptor
 */
@Configuration
@ConfigurationProperties(prefix = "hapi.policy")
public class ConsentProperties {
    /**
     * Organization ID for KEMENKES managing organization
     */
    private String environment = "dev";

    /**
     * Header name for administrator access
     */
    private String administratorRequestHeader = "X-Administrator-Access";

    /**
     * Header name for organization ID
     */
    private String organizationRequestHeader = "X-Organization-ID";

    /**
     * Security system name for organization ID
     */
    private String organizationSecuritySystemName = "http://terminology.kemkes.go.id/org-id";

    /**
     * Organization ID for KEMENKES managing organization
     */
    private String organizationManagingIdKemenkes = "9900000000";

    /**
     * URL for consent registry service
     */
    private String urlConsentRegistry = "http://localhost:7272/api/consent";

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public String getAdministratorRequestHeader() {
        return administratorRequestHeader;
    }

    public void setAdministratorRequestHeader(String administratorRequestHeader) {
        this.administratorRequestHeader = administratorRequestHeader;
    }

    public String getOrganizationRequestHeader() {
        return organizationRequestHeader;
    }

    public void setOrganizationRequestHeader(String organizationRequestHeader) {
        this.organizationRequestHeader = organizationRequestHeader;
    }

    public String getOrganizationSecuritySystemName() {
        return organizationSecuritySystemName;
    }

    public void setOrganizationSecuritySystemName(String organizationSecuritySystemName) {
        this.organizationSecuritySystemName = organizationSecuritySystemName;
    }

    public String getOrganizationManagingIdKemenkes() {
        return organizationManagingIdKemenkes;
    }

    public void setOrganizationManagingIdKemenkes(String organizationManagingIdKemenkes) {
        this.organizationManagingIdKemenkes = organizationManagingIdKemenkes;
    }

    public String getUrlConsentRegistry() {
        return urlConsentRegistry;
    }

    public void setUrlConsentRegistry(String urlConsentRegistry) {
        this.urlConsentRegistry = urlConsentRegistry;
    }

    /**
     * Get FHIR resource whitelist for read access
     */
    public List<Class<? extends DomainResource>> getFhirResourceWhitelist() {
        List<Class<? extends DomainResource>> whitelist = new ArrayList<>();
        whitelist.add(AllergyIntolerance.class);
        whitelist.add(FamilyMemberHistory.class);
        whitelist.add(Substance.class);
        return whitelist;
    }

    public List<Class<? extends DomainResource>> getFhirResourceHasEncounter() {
        List<Class<? extends DomainResource>> resources = new ArrayList<>();
        resources.add(Condition.class);
        resources.add(Procedure.class);
        resources.add(QuestionnaireResponse.class);
        resources.add(Immunization.class);
        resources.add(ClinicalImpression.class);
        resources.add(CarePlan.class);
        resources.add(NutritionOrder.class);
        resources.add(ServiceRequest.class);
        resources.add(DiagnosticReport.class);
        resources.add(Observation.class);
        resources.add(MedicationRequest.class);
        resources.add(RiskAssessment.class);
        resources.add(ImagingStudy.class);
        return resources;
    }
}
