package ch.ahdis.matchbox.terminology;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * The HAPI FHIR provider for the CodeSystem/$validate-code operation.
 *
 * @author Quentin Ligier
 **/
public class CodeSystemCodeValidationProvider implements IResourceProvider {
	private static final Logger log = LoggerFactory.getLogger(CodeSystemCodeValidationProvider.class);

	/**
	 *
	 */
	@Operation(name = "$validate-code", idempotent = true)
	public IAnyResource validateCode(@ResourceParam final Parameters request,
												final HttpServletResponse servletResponse) {
		if (request.hasParameter("coding") && request.getParameterValue("coding") instanceof final Coding coding) {
			log.debug("Validating code in CS: {}|{}", coding.getCode(), coding.getSystem());
			return TerminologyUtils.mapCodingToSuccessfulParameters(coding);
		}

		servletResponse.setStatus(422);
		final var oo = new OperationOutcome();
		oo.addIssue()
			.setSeverity(OperationOutcome.IssueSeverity.ERROR)
			.setCode(OperationOutcome.IssueType.NOTFOUND)
			.setDetails(new CodeableConcept().setText("Unable to find code to validate (looked for coding)"));
		// Original error message is:
		// Unable to find code to validate (looked for coding | codeableConcept | code)
		return oo;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return CodeSystem.class;
	}
}
