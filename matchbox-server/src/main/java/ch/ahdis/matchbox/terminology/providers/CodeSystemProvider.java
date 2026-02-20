package ch.ahdis.matchbox.terminology.providers;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.StringParam;
import ch.ahdis.matchbox.config.MatchboxFhirVersion;
import ch.ahdis.matchbox.providers.AbstractMatchboxResourceProvider;
import ch.ahdis.matchbox.terminology.TerminologyUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static ch.ahdis.matchbox.terminology.TerminologyUtils.mapErrorToOperationOutcome;

public class CodeSystemProvider extends AbstractMatchboxResourceProvider {
	private static final Logger log = LoggerFactory.getLogger(CodeSystemProvider.class);

	public CodeSystemProvider(final MatchboxFhirVersion matchboxFhirVersion) {
		super(matchboxFhirVersion,
				org.hl7.fhir.r4.model.CodeSystem.class,
				org.hl7.fhir.r4b.model.CodeSystem.class,
				org.hl7.fhir.r5.model.CodeSystem.class);
	}

	@Operation(name = "$validate-code",
		canonicalUrl = "http://hl7.org/fhir/OperationDefinition/CodeSystem-validate-code",
		idempotent = true)
	public IBaseResource validateCode(@ResourceParam final IBaseParameters baseParameters,
												 final HttpServletResponse servletResponse) {
		return this.fhirVersion.applyOnR5(
			baseParameters,
			r5 -> this.doValidateR5Code(r5, servletResponse),
			Parameters.class
		);
	}

	@Operation(name = "$lookup",
		canonicalUrl = "http://hl7.org/fhir/OperationDefinition/CodeSystem-lookup",
		idempotent = true)
	public IBaseResource lookupCodeSystem(@ResourceParam final IBaseParameters baseParameters,
													  final HttpServletResponse servletResponse) {
		return null;
	}

	@Read
	public IBaseResource readCodeSystem(@IdParam final IIdType id,
									 			   final HttpServletResponse servletResponse) {
		return null;
	}

	@Search
	public List<CodeSystem> searchCodeSystem(
		@OptionalParam(name = CodeSystem.SP_URL) final StringParam url,
		@OptionalParam(name = CodeSystem.SP_VERSION) final StringParam version
	) {
		return Collections.emptyList();
	}

	private Resource doValidateR5Code(final Parameters request,
												 final HttpServletResponse servletResponse) {
		if (request.hasParameter("coding") && request.getParameterValue("coding") instanceof final Coding coding) {
			log.debug("Validating code in CS: {}|{}", coding.getCode(), coding.getSystem());
			return TerminologyUtils.createSuccessfulResponseParameters(coding);
		}

		if (request.hasParameter("codeableConcept") && request.getParameterValue("codeableConcept") instanceof final CodeableConcept codeableConcept) {
			log.debug("Validating code in CS: {}|{}", codeableConcept.getCodingFirstRep().getCode(), codeableConcept.getCodingFirstRep().getSystem());
			return TerminologyUtils.createSuccessfulResponseParameters(codeableConcept);
		}

		servletResponse.setStatus(422);
		// The original error message is:
		// Unable to find code to validate (looked for coding | codeableConcept | code)
		return mapErrorToOperationOutcome("Unable to find code to validate (looked for coding)");
	}
}
