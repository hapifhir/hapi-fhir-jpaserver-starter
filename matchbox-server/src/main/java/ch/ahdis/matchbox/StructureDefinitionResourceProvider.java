package ch.ahdis.matchbox;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.quartz.DisallowConcurrentExecution;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class StructureDefinitionResourceProvider extends ConformanceResourceProvider<StructureDefinition> {

	private static final Logger ourLog = LoggerFactory.getLogger(StructureDefinitionResourceProvider.class);

	public StructureDefinitionResourceProvider() {
		super("StructureDefinition");
	}

	@Override
	public Class<StructureDefinition> getResourceType() {
		return StructureDefinition.class;
	}

	private MethodOutcome createSnapshot(StructureDefinition theResource) {
		if (theResource.getSnapshot().isEmpty()) {
			MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(theResource.getBaseDefinition(),
					null, false, false);
			try {
				StructureDefinition theSnapShotResource = matchboxEngine.createSnapshot(theResource);
				theResource.setSnapshot(theSnapShotResource.getSnapshot());
			} catch (FHIRException | IOException e) {
				ourLog.error("Error creating snapshot for StructureDefinition " + theResource.getUrl(), e);
				MethodOutcome outcome = new MethodOutcome();
				outcome.setStatusCode(400);
				outcome.setCreated(false);
				OperationOutcome operationOutcome = new OperationOutcome();
				operationOutcome.addIssue().setDiagnostics(e.getMessage());
				outcome.setOperationOutcome(operationOutcome);
				return outcome;
			}
		}
		return null;
	}

	@Override
	public MethodOutcome create(HttpServletRequest theRequest, StructureDefinition theResource, String theConditional,
			RequestDetails theRequestDetails) {
		MethodOutcome outcome = createSnapshot(theResource);
		return outcome==null ? super.create(theRequest, theResource, theConditional, theRequestDetails) : outcome;
	}

	@Override
	public MethodOutcome update(HttpServletRequest theRequest, StructureDefinition theResource, IIdType theId,
			String theConditional, RequestDetails theRequestDetails) {
		MethodOutcome outcome = createSnapshot(theResource);
		return outcome==null ? super.update(theRequest, theResource, theId, theConditional, theRequestDetails) : outcome;
	}

}
