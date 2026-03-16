package ca.uhn.fhir.jpa.starter.interceptors;

import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAction;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAgentComponent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventOutcome;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;

public class AuditEventLogger {
    private IFhirResourceDao<AuditEvent> auditEventDao;
    private String orgId;
    private ConsentProperties consentProperties;

    public AuditEventLogger(IFhirResourceDao<AuditEvent> auditEventDao, String orgId, ConsentProperties consentProperties) {
        this.auditEventDao = auditEventDao;
        this.orgId = orgId;
        this.consentProperties = consentProperties;
    }

    public void createForFailedOperation(RequestDetails theRequestDetails, BaseServerResponseException theException,
            IConsentContextServices theContextServices) {
        AuditEvent auditEvent = new AuditEvent();
            auditEvent.setAction(AuditEventAction.E); // Action: Execute (E)
            auditEvent.setRecorded(new java.util.Date());

            // Outcome: Minor Failure (4) atau Serious Failure (8)
            auditEvent.setOutcome(AuditEventOutcome._4);

            Coding eventType = new Coding();
            eventType.setSystem("http://terminology.hl7.org/CodeSystem/audit-event-type");
            eventType.setCode("rest");
            eventType.setDisplay("RESTful Operation");
            auditEvent.setType(eventType);

            Coding eventSubType = new Coding();
            eventSubType.setSystem("http://hl7.org/fhir/restful-interaction");
            eventSubType.setCode(theRequestDetails.getRestOperationType() != null ? 
                theRequestDetails.getRestOperationType().getCode() : "unknown");
            eventSubType.setDisplay(theRequestDetails.getRestOperationType() != null ? 
                theRequestDetails.getRestOperationType().name() : "Unknown");
            auditEvent.addSubtype(eventSubType);

            if (theException != null) {
                auditEvent.setOutcomeDesc(theException.getMessage());
            }

            CodeableConcept purposeOfUse = new CodeableConcept();
            Coding purposeCoding = new Coding();
            purposeCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActReason");
            purposeCoding.setCode("HREAT");
            purposeCoding.setDisplay("healthcare treatment");
            purposeOfUse.addCoding(purposeCoding);
            auditEvent.addPurposeOfEvent(purposeOfUse);

            if (orgId != null) {
                AuditEventAgentComponent participant = new AuditEventAgentComponent();
                participant.setWho(new Reference("Organization/" + orgId));
                participant.setRequestor(true);
                CodeableConcept participantRole = new CodeableConcept();
                Coding roleCoding = new Coding();
                roleCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
                roleCoding.setCode("IRCP");
                roleCoding.setDisplay("information recipient");
                participantRole.addCoding(roleCoding);
                participant.addRole(participantRole);
                auditEvent.addAgent(participant);
            }

            AuditEventAgentComponent serverParticipant = new AuditEventAgentComponent();
            serverParticipant.setWho(new Reference("Device/hapi-fhir-server"));
            serverParticipant.setRequestor(false);
            CodeableConcept serverRole = new CodeableConcept();
            Coding serverRoleCoding = new Coding();
            serverRoleCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
            serverRoleCoding.setCode("AUT");
            serverRoleCoding.setDisplay("author");
            serverRole.addCoding(serverRoleCoding);
            serverParticipant.addRole(serverRole);
            auditEvent.addAgent(serverParticipant);

            Coding securityLabel = new Coding();
            securityLabel.setSystem(consentProperties.getOrganizationSecuritySystemName());
            securityLabel.setCode(orgId != null ? orgId : "unknown");
            auditEvent.getMeta().addSecurity(securityLabel);

            auditEventDao.create(auditEvent, theRequestDetails);
    }
}
