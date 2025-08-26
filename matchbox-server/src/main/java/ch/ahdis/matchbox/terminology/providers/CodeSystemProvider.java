package ch.ahdis.matchbox.terminology.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import ch.ahdis.matchbox.terminology.TerminologyUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static ch.ahdis.matchbox.terminology.TerminologyUtils.mapErrorToOperationOutcome;

public class CodeSystemProvider implements IResourceProvider {
	private static final Logger log = LoggerFactory.getLogger(CodeSystemProvider.class);

	private final FhirVersionEnum fhirVersion;

	public CodeSystemProvider(final FhirContext fhirContext) {
		this.fhirVersion = fhirContext.getVersion().getVersion();
	}

	@Operation(name = "$validate-code",
		canonicalUrl = "http://hl7.org/fhir/OperationDefinition/CodeSystem-validate-code",
		idempotent = true)
	public IAnyResource validateCode(@ResourceParam final IBaseParameters baseParameters,
												final HttpServletResponse servletResponse) {
		final Parameters request;
		if (baseParameters instanceof final Parameters parametersR5) {
			request = parametersR5;
		} else if (baseParameters instanceof final org.hl7.fhir.r4.model.Parameters parametersR4) {
			request = (Parameters) VersionConvertorFactory_40_50.convertResource(parametersR4);
		} else if (baseParameters instanceof final org.hl7.fhir.r4b.model.Parameters parametersR4B) {
			request = (Parameters) VersionConvertorFactory_43_50.convertResource(parametersR4B);
		} else {
			throw new MatchboxUnsupportedFhirVersionException("CodeSystemCodeValidationProvider",
																			  this.fhirVersion);
		}

		final var response = this.doValidateR5Code(request, servletResponse);

		return switch (this.fhirVersion) {
			case R4 -> VersionConvertorFactory_40_50.convertResource(response);
			case R4B -> VersionConvertorFactory_43_50.convertResource(response);
			case R5 -> response;
			default -> throw new MatchboxUnsupportedFhirVersionException("CodeSystemCodeValidationProvider",
																							 this.fhirVersion);
		};
	}

	@Operation(name = "$lookup",
		canonicalUrl = "http://hl7.org/fhir/OperationDefinition/CodeSystem-lookup",
		idempotent = true)
	public IAnyResource lookupCodeSystem(@ResourceParam final IBaseParameters baseParameters,
													 final HttpServletResponse servletResponse) {
		return null;
	}

	@Read
	public IAnyResource readCodeSystem(@IdParam final IIdType id,
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

		servletResponse.setStatus(422);
		// The original error message is:
		// Unable to find code to validate (looked for coding | codeableConcept | code)
		return mapErrorToOperationOutcome("Unable to find code to validate (looked for coding)");
	}

	/**
	 * Returns the type of resource returned by this provider
	 *
	 * @return Returns the type of resource returned by this provider
	 */
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return switch (this.fhirVersion) {
			case R4 -> org.hl7.fhir.r4.model.CodeSystem.class;
			case R4B -> org.hl7.fhir.r4b.model.CodeSystem.class;
			case R5 -> org.hl7.fhir.r5.model.CodeSystem.class;
			default -> throw new MatchboxUnsupportedFhirVersionException("CodeSystemProvider",
																							 this.fhirVersion);
		};
	}
}
