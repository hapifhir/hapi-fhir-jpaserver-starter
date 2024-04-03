package ch.ahdis.matchbox;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructureDefinitionResourceProvider extends
		ConformancePackageResourceProvider<StructureDefinition, org.hl7.fhir.r4b.model.StructureDefinition, org.hl7.fhir.r5.model.StructureDefinition> {

	private static final Logger ourLog = LoggerFactory.getLogger(StructureDefinitionResourceProvider.class);

	public StructureDefinitionResourceProvider() {
		super(StructureDefinition.class, org.hl7.fhir.r4b.model.StructureDefinition.class,
				org.hl7.fhir.r5.model.StructureDefinition.class);
	}

	private MethodOutcome createSnapshot(IBaseResource resource) {
		if (classR4.isInstance(resource)) {
			StructureDefinition theResource = (StructureDefinition) classR4.cast(resource);
			if (theResource.getSnapshot().isEmpty()) {
				MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(theResource.getBaseDefinition(),
						null, false, false);
				try {
					StructureDefinition theSnapShotResource = matchboxEngine.createSnapshot(theResource);
					theResource.setSnapshot(theSnapShotResource.getSnapshot());
				} catch (FHIRException | IOException e) {
					ourLog.error("Error creating snapshot for StructureDefinition " + theResource.getUrl(), e);
					MethodOutcome outcome = new MethodOutcome();
					outcome.setResponseStatusCode(400);
					outcome.setCreated(false);
					OperationOutcome operationOutcome = new OperationOutcome();
					operationOutcome.addIssue().setDiagnostics(e.getMessage());
					outcome.setOperationOutcome(operationOutcome);
					return outcome;
				}
			}
		}
		if (classR5.isInstance(resource)) {
			org.hl7.fhir.r5.model.StructureDefinition theResource = (org.hl7.fhir.r5.model.StructureDefinition) resource;
			if (theResource.getSnapshot().isEmpty()) {
				MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(theResource.getBaseDefinition(),
						null, false, false);
				try {
					org.hl7.fhir.r5.model.StructureDefinition theSnapShotResource = matchboxEngine.createSnapshot(theResource);
					theResource.setSnapshot(theSnapShotResource.getSnapshot());
				} catch (FHIRException | IOException e) {
					ourLog.error("Error creating snapshot for StructureDefinition " + theResource.getUrl(), e);
					MethodOutcome outcome = new MethodOutcome();
					outcome.setResponseStatusCode(400);
					outcome.setCreated(false);
					OperationOutcome operationOutcome = new OperationOutcome();
					operationOutcome.addIssue().setDiagnostics(e.getMessage());
					outcome.setOperationOutcome(operationOutcome);
					return outcome;
				}
			}
		}
		return null;
	}

	@Override
	public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam IBaseResource theResource,
			@ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
		MethodOutcome outcome = createSnapshot(theResource);
		return outcome == null ? super.create(theRequest, theResource, theConditional, theRequestDetails) : outcome;
	}

	@Override
	public MethodOutcome update(HttpServletRequest theRequest, IDomainResource theResource, IIdType theId,
			String theConditional, RequestDetails theRequestDetails) {
		MethodOutcome outcome = createSnapshot(theResource);
		return outcome == null ? super.update(theRequest, theResource, theId, theConditional, theRequestDetails)
				: outcome;
	}

}
