package ca.uhn.fhir.jpa.starter.interceptors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;

import java.util.ArrayList;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsentEnforcementService implements IConsentService {
    private LoaderWithCache loader;
    private String orgId;
    private PolicyEvaluator policyEvaluator;

    @Autowired
    private DaoRegistry daoRegistry;

    @Autowired
    private ConsentProperties consentProperties;

    @Autowired
    private FhirContext fhirContext;

    @Override
    public ConsentOutcome startOperation(RequestDetails theRequestDetails, IConsentContextServices theContextServices) {
        // Authrization using Organization ID
        String userOrgId = theRequestDetails.getHeader(consentProperties.getOrganizationRequestHeader());
        if (userOrgId == null || userOrgId.isEmpty()) {
            // TOLAK Jika tidak ada ID organisasi di header
            return ConsentOutcome.REJECT;
        }

        // Front Service hanya mengirimkan X-Organization-ID yang benar, jadi tidak perlu dicek
        this.orgId = userOrgId;

        this.loader = new LoaderWithCache(consentProperties);
        this.loader.setFhirContext(fhirContext);

        // load daftar consent berdasarkan Organisasi
        this.policyEvaluator = new PolicyEvaluator(PolicyEvaluator.EVALUATION_SCOPE_ORGANIZATION, loader.getConsentList(consentProperties.getEnvironment(), orgId));

        return ConsentOutcome.PROCEED;
    }

    @Override
    public ConsentOutcome canSeeResource(RequestDetails theRequestDetails, IBaseResource theResource,
            IConsentContextServices theContextServices) {
        String patientId = Utils.getPatientId(theResource);
        policyEvaluator.setPatientId(patientId);
        return policyEvaluator.evaluate(theResource, ActionEnum.ACCESS, new ArrayList<String>());
    }

    @Override
    public ConsentOutcome willSeeResource(RequestDetails theRequestDetails, IBaseResource theResource,
            IConsentContextServices theContextServices) {
        String patientId = Utils.getPatientId(theResource);
        policyEvaluator.setPatientId(patientId);
        return policyEvaluator.evaluate(theResource, ActionEnum.READ, new ArrayList<String>());
    }

    @Override
    public void completeOperationFailure(RequestDetails theRequestDetails, BaseServerResponseException theException,
            IConsentContextServices theContextServices) {
        try {
            IFhirResourceDao<AuditEvent> auditEventDao = daoRegistry.getResourceDao(AuditEvent.class);

            AuditEventLogger logger = new AuditEventLogger(auditEventDao, orgId, consentProperties);
            logger.createForFailedOperation(theRequestDetails, theException, theContextServices);
        } catch (Exception e) {
            // Log error but don't throw to avoid interfering with the original exception
            e.printStackTrace();
        }
    }
}
