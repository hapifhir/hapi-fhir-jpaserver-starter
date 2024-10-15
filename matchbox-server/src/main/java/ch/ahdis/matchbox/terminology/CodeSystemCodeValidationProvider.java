package ch.ahdis.matchbox.terminology;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;

import static ch.ahdis.matchbox.terminology.TerminologyUtils.mapErrorToOperationOutcome;
import static java.util.Objects.requireNonNull;

/**
 * The HAPI FHIR provider for the CodeSystem/$validate-code operation.
 *
 * @author Quentin Ligier
 **/
public class CodeSystemCodeValidationProvider implements IResourceProvider {
	private static final Logger log = LoggerFactory.getLogger(CodeSystemCodeValidationProvider.class);

	private final FhirContext fhirContext;

	public CodeSystemCodeValidationProvider(final FhirContext fhirContext) {
		this.fhirContext = requireNonNull(fhirContext);
	}

	/**
	 *
	 */
	@Operation(name = "$validate-code", idempotent = true)
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
																			  this.fhirContext.getVersion().getVersion());
		}

		if (request.hasParameter("coding") && request.getParameterValue("coding") instanceof final Coding coding) {
			log.debug("Validating code in CS: {}|{}", coding.getCode(), coding.getSystem());
			final var outputParams = TerminologyUtils.createSuccessfulResponseParameters(coding);
			return switch (this.fhirContext.getVersion().getVersion()) {
				case R4 -> VersionConvertorFactory_40_50.convertResource(outputParams);
				case R4B -> VersionConvertorFactory_43_50.convertResource(outputParams);
				case R5 -> outputParams;
				default -> throw new MatchboxUnsupportedFhirVersionException("CodeSystemCodeValidationProvider",
																								 this.fhirContext.getVersion().getVersion());
			};
		}

		servletResponse.setStatus(422);
		// Original error message is:
		// Unable to find code to validate (looked for coding | codeableConcept | code)
		return mapErrorToOperationOutcome("Unable to find code to validate (looked for coding)");
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return switch (this.fhirContext.getVersion().getVersion()) {
			case R4 -> org.hl7.fhir.r4.model.CodeSystem.class;
			case R4B -> org.hl7.fhir.r4b.model.CodeSystem.class;
			case R5 -> org.hl7.fhir.r5.model.CodeSystem.class;
			default -> throw new MatchboxUnsupportedFhirVersionException("CodeSystemCodeValidationProvider",
																							 this.fhirContext.getVersion().getVersion());
		};
	}
}
