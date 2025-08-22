package ch.ahdis.matchbox.terminology;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.context.support.ValueSetExpansionOptions;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r5.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static ch.ahdis.matchbox.terminology.TerminologyUtils.*;
import static java.util.Objects.requireNonNull;

/**
 * The HAPI FHIR provider for the ValueSet/$validate-code operation.
 * <p>
 * It currently only supports FHIR R4 value sets.
 *
 * @author Quentin Ligier
 **/
public class ValueSetCodeValidationProvider implements IResourceProvider {
	private static final Logger log = LoggerFactory.getLogger(ValueSetCodeValidationProvider.class);

	private final FhirContext fhirContext;
	private final FhirVersionEnum fhirVersion;

	private final InMemoryTerminologyServerValidationSupport inMemoryTerminologySupport;

	/**
	 * A cache that stores a mapping from value set URLs to expanded value sets, per cache ID.
	 */
	private final Map<String, Map<String, ValueSet>> valueSetCache =
		new PassiveExpiringMap<>(5, TimeUnit.MINUTES);

	private final ValueSetExpansionOptions expansionOptions = new ValueSetExpansionOptions();

	private final ValidationSupportContext validationSupportContext;

	public ValueSetCodeValidationProvider(final FhirContext fhirContext) {
		this.fhirContext = requireNonNull(fhirContext);
		this.expansionOptions.setFailOnMissingCodeSystem(false);
		this.inMemoryTerminologySupport =
			new InMemoryTerminologyServerValidationSupport(fhirContext);
		this.validationSupportContext =
			new ValidationSupportContext(new DummyValidationSupport(fhirContext));
		this.fhirVersion = fhirContext.getVersion().getVersion();
	}

	/**
	 *
	 */
	@Operation(name = "$validate-code", idempotent = true)
	public IAnyResource validateCode(@ResourceParam final IBaseParameters baseParameters,
												final HttpServletResponse servletResponse) {
		Objects.requireNonNull(baseParameters, "baseParameters is null in ValueSetCodeValidationProvider.validateCode");
		// Convert the incoming parameters to R5, to handle a single FHIR version in the method
		final Parameters request = switch (baseParameters) {
			case final Parameters parametersR5 -> parametersR5;
			case final org.hl7.fhir.r4.model.Parameters parametersR4 ->
				(Parameters) VersionConvertorFactory_40_50.convertResource(parametersR4);
			case final org.hl7.fhir.r4b.model.Parameters parametersR4B ->
				(Parameters) VersionConvertorFactory_43_50.convertResource(parametersR4B);
			default -> throw new MatchboxUnsupportedFhirVersionException("ValueSetCodeValidationProvider",
																							 baseParameters.getStructureFhirVersionEnum());
		};

		final var response = doValidateR5Code(request, servletResponse);
		return switch (this.fhirVersion) {
			case R4 -> VersionConvertorFactory_40_50.convertResource(response);
			case R4B -> VersionConvertorFactory_43_50.convertResource(response);
			case R5 -> response;
			default -> throw new MatchboxUnsupportedFhirVersionException("ValueSetCodeValidationProvider",
																							 this.fhirVersion);
		};
	}

	private Resource doValidateR5Code(final Parameters request,
												 final HttpServletResponse servletResponse) {
		final String valueSetMode = request.hasParameter("valueSetMode")
			? request.getParameterValue("valueSetMode").toString()
			: "DEFAULT";

		final String cacheId = request.hasParameter("cache-id")
			? request.getParameterValue("cache-id").toString()
			: null;

		final boolean inferSystem = request.hasParameter("inferSystem") && request.getParameterBool("inferSystem");

		final String code = request.hasParameter("code")
			? request.getParameterValue("code").toString()	: null;

		if (code != null) {
			return mapCodeToSuccessfulParameters(code);
		}

		//final boolean lenientDisplayValidation = request.hasParameter("lenient-display-validation")
		//	&& request.getParameterBool("lenient-display-validation");

		//final String mode = request.hasParameter("mode")
		//	? request.getParameterValue("mode").toString()	: null;

		// parameter default-to-lastest-version (Boolean)
		// parameter profile-url "http://hl7.org/fhir/ExpansionProfile/dc8fd4bc-091a-424a-8a3b-6198ef146891"

		if (!request.hasParameter("coding") && !request.hasParameter("codeableConcept")) {
			servletResponse.setStatus(422);
			return mapErrorToOperationOutcome("Missing parameter 'coding' or 'codeableConcept' in the request");
		}

		if (!(request.getParameterValue("coding") instanceof Coding)
			&& !(request.getParameterValue("codeableConcept") instanceof CodeableConcept)) {
			servletResponse.setStatus(422);
			// The original error message is:
			//    Unable to find code to validate (looked for coding | codeableConcept | code)
			return mapErrorToOperationOutcome("Unable to find code to validate (looked for 'coding' and 'codeableConcept')");
		}

		final Coding coding;
		if (request.hasParameter("coding")) {
			coding = (Coding) request.getParameterValue("coding");
		} else {
			coding = null;
		}
		final CodeableConcept codeableConcept;
		if (request.hasParameter("codeableConcept")) {
			codeableConcept = (CodeableConcept) request.getParameterValue("codeableConcept");
		} else {
			codeableConcept = null;
		}

		final List<Coding> codings;
		if (coding != null) {
			codings = List.of(coding);
		} else {
			codings = requireNonNull(codeableConcept).getCoding();
		}

		if ("NO_MEMBERSHIP_CHECK".equals(valueSetMode)) {
			return createSuccessfulResponseParameters(codings.getFirst());
		}

		String url = null;
		ValueSet valueSet = null;
		boolean cachedValueSet = false;
		if (request.hasParameter("url")) {
			url = request.getParameterValue("url").toString();
			valueSet = this.getExpandedValueSet(cacheId, url);
			cachedValueSet = true;
		} else if (request.hasParameter("valueSet")) {
			valueSet = (ValueSet) request.getParameter("valueSet").getResource();
			url = valueSet.getUrl();
		}

		if (valueSet == null) {
			// That value set is not cached
			log.debug("OK - cache miss, value set is null");
			return createSuccessfulResponseParameters(codings.getFirst());
		}
		if (coding != null) {
			log.debug("Validating code '{}|{}' in ValueSet '{}'", coding.getCode(), coding.getSystem(), url);
		} else {
			log.debug("Validating codeableConcept '{}' in ValueSet '{}'", codeableConcept, url);
		}

		if (!cachedValueSet) {
			// We have to expand the value set
			final IValidationSupport.ValueSetExpansionOutcome result = this.inMemoryTerminologySupport.expandValueSet(
				this.validationSupportContext,
				this.expansionOptions,
				valueSet);
			if (result == null || result.getValueSet() == null) {
				// The value set expansion has failed; this means it may be too complex for the current implementation
				// We try to infer the code membership from the value set definition as a last resort
				log.debug(" - expansion failed");

				for (final var validatedCoding : codings) {
					final var membership = this.evaluateCodeInComposition(validatedCoding, valueSet.getCompose());
					if (membership == CodeMembership.EXCLUDED) {
						log.debug(" - code '{}' is excluded from value set composition", validatedCoding.getCode());
					} else if (membership == CodeMembership.INCLUDED) {
						log.debug(" - code '{}' is included in value set composition", validatedCoding.getCode());
						// We can stop here, we've found a 'Coding' explicitly included
						return createSuccessfulResponseParameters(validatedCoding);
					} else {
						log.debug(" - code '{}' is not included/excluded from value set composition", validatedCoding.getCode());
						// The 'Coding' is neither included nor excluded from the composition
						// We can't infer the code membership, so we return a success in this case
						return createSuccessfulResponseParameters(validatedCoding);
					}
				}
				return createErrorResponseParameters(
					"The provided Coding/CodeableConcept is excluded from the value set '%s' composition".formatted(url),
					coding,
					codeableConcept
				);
			}

			// Value set is expanded, convert it to R5 for internal use
			final var baseValueSet = (IDomainResource) result.getValueSet();
			if (baseValueSet instanceof final ValueSet valueSetR5) {
				valueSet = valueSetR5;
			} else if (baseValueSet instanceof final org.hl7.fhir.r4.model.ValueSet valueSetR4) {
				valueSet = (ValueSet) VersionConvertorFactory_40_50.convertResource(valueSetR4);
			} else if (baseValueSet instanceof final org.hl7.fhir.r4b.model.ValueSet valueSetR4B) {
				valueSet = (ValueSet) VersionConvertorFactory_43_50.convertResource(valueSetR4B);
			} else {
				throw new MatchboxUnsupportedFhirVersionException("ValueSetCodeValidationProvider",
																				  baseValueSet.getStructureFhirVersionEnum());
			}

			if (valueSet.getExpansion().getContains().isEmpty()) {
				// The value set expansion is successful but empty
				log.debug("OK - expansion successful but empty");
				return createSuccessfulResponseParameters(codings.getFirst());
			}

			if (cacheId != null) {
				this.cacheExpandedValueSet(cacheId, url, valueSet);
			}
		}

		for (final var validatedCoding : codings) {
			if (this.evaluateCodingInExpandedValueSet(validatedCoding, valueSet, inferSystem)) {
				return createSuccessfulResponseParameters(validatedCoding);
			}
		}
		return createErrorResponseParameters(
			"The provided Coding/CodeableConcept is not in the value set '%s' (expansion contains %d codes)".formatted(
				url,
				valueSet.getExpansion().getContains().size()
			),
			coding,
			codeableConcept
		);
	}

	/**
	 * Try to infer from an expanded value set if a coding is included or excluded.
	 */
	private boolean evaluateCodingInExpandedValueSet(final Coding coding,
												                final ValueSet valueSet,
												                final boolean inferSystem) {
		if (inferSystem && !coding.hasSystem()) {
			// Infer the coding system as the first included system in the composition
			coding.setSystem(valueSet.getCompose().getIncludeFirstRep().getSystem());
		}

		if (this.validateCodeInExpandedValueSet(coding, valueSet)) {
			log.debug("OK - present in expanded value set (expansion contains {} codes)",
						 valueSet.getExpansion().getContains().size());
			return true;
		}
		log.debug("FAIL - not present in expanded value set (expansion contains {} codes)",
					 valueSet.getExpansion().getContains().size());
		return false;
	}

	/**
	 * Try to infer from a value set composition (we failed to expand it) if a given code is explicitly included or
	 * excluded.
	 */
	private CodeMembership evaluateCodeInComposition(final Coding coding,
																	 final ValueSet.ValueSetComposeComponent compose) {
		int mayBeIncludedByInclude = 0;
		for (final var include : compose.getInclude()) {
			if (include.hasSystem() && !include.getSystem().equals(coding.getSystem())) {
				continue;
			}

			for (final var concept : include.getConcept()) {
				if (concept.getCode().equals(coding.getCode())) {
					return CodeMembership.INCLUDED;
				}
			}

			for (final var filter : include.getFilter()) {
				if ("regex".equals(filter.getOp().toCode())) {
					final var pattern = Pattern.compile(filter.getValue());
					// Try to match the full code with the regex
					if (!pattern.matcher(coding.getCode()).matches()) {
						return CodeMembership.EXCLUDED;
					}
				}
			}
			++mayBeIncludedByInclude;
		}

		if (mayBeIncludedByInclude > 0) {
			return CodeMembership.UNKNOWN;
		}

		// The system is not present in the composition
		return CodeMembership.EXCLUDED;
	}

	private @Nullable ValueSet getExpandedValueSet(final String cacheId,
																  final String valueSetUrl) {
		return Optional.ofNullable(this.valueSetCache.get(cacheId))
			.map(m -> m.get(valueSetUrl))
			.orElse(null);
	}

	private void cacheExpandedValueSet(final String cacheId,
												  final String valueSetUrl,
												  final ValueSet valueSet) {
		this.valueSetCache
			.computeIfAbsent(cacheId, k -> HashMap.newHashMap(20))
			.put(valueSetUrl, valueSet);
	}

	private boolean validateCodeInExpandedValueSet(final Coding coding,
																  final ValueSet valueSet) {
		for (final var item : valueSet.getExpansion().getContains()) {
			if (item.getSystem().equals(coding.getSystem()) && item.getCode().equals(coding.getCode())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return switch (this.fhirContext.getVersion().getVersion()) {
			case R4 -> org.hl7.fhir.r4.model.ValueSet.class;
			case R4B -> org.hl7.fhir.r4b.model.ValueSet.class;
			case R5 -> org.hl7.fhir.r5.model.ValueSet.class;
			default -> throw new MatchboxUnsupportedFhirVersionException("ValueSetCodeValidationProvider",
																							 this.fhirContext.getVersion().getVersion());
		};
	}

	public static class DummyValidationSupport implements IValidationSupport {

		private final FhirContext context;

		DummyValidationSupport(final FhirContext context) {
			this.context = context;
		}

		@Override
		public FhirContext getFhirContext() {
			return context;
		}
	}

	enum CodeMembership {
		INCLUDED, EXCLUDED, UNKNOWN
	}
}
