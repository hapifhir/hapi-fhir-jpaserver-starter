package ch.ahdis.matchbox.terminology;

import ca.uhn.fhir.context.FhirContext;
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
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
		} else {
			throw new MatchboxUnsupportedFhirVersionException("CodeSystemCodeValidationProvider",
																			  this.fhirContext.getVersion().getVersion());
		}

		final String valueSetMode = request.hasParameter("valueSetMode")
			? request.getParameterValue("valueSetMode").toString()
			: "DEFAULT";

		final String cacheId = request.hasParameter("cache-id")
			? request.getParameterValue("cache-id").toString()
			: null;

		final boolean inferSystem = request.hasParameter("inferSystem")
			? request.getParameterBool("inferSystem")
			: false;

		if (!request.hasParameter("coding")) {
			servletResponse.setStatus(422);
			return mapErrorToOperationOutcome("Missing parameter 'coding' in the request");
		}
		if (request.getParameterValue("coding") instanceof final Coding coding) {
			if ("NO_MEMBERSHIP_CHECK".equals(valueSetMode)) {
				return mapCodingToSuccessfulParameters(coding);
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
				return mapCodingToSuccessfulParameters(coding);
			}
			log.debug("Validating code in VS: {}|{} in {}", coding.getCode(), coding.getSystem(), url);

			if (inferSystem && !coding.hasSystem()) {
				// Infer the coding system as the first included system in the composition
				coding.setSystem(valueSet.getCompose().getIncludeFirstRep().getSystem());
			}

			if (!cachedValueSet) {
				// We have to expand the value set
				final IValidationSupport.ValueSetExpansionOutcome result = this.inMemoryTerminologySupport.expandValueSet(
					this.validationSupportContext,
					this.expansionOptions,
					valueSet);
				if (result == null || result.getValueSet() == null) {
					// We have failed expanding the value set, this means it may be a complex one
					log.debug("OK - expansion failed");
					return mapCodingToSuccessfulParameters(coding);
				}
				final var baseValueSet = (IDomainResource) result.getValueSet();
				if (baseValueSet instanceof final ValueSet valueSetR5) {
					valueSet = valueSetR5;
				} else if (baseValueSet instanceof final org.hl7.fhir.r4.model.ValueSet valueSetR4) {
					valueSet = (ValueSet) VersionConvertorFactory_40_50.convertResource(valueSetR4);
				} else {
					throw new MatchboxUnsupportedFhirVersionException("ValueSetCodeValidationProvider",
																					  this.fhirContext.getVersion().getVersion());
				}

				if (cacheId != null) {
					this.cacheExpandedValueSet(cacheId, url, valueSet);
				}
			}

			// Now we have an expanded value set, we can properly validate the code
			if (this.validateCodeInExpandedValueSet(coding, valueSet)) {
				log.debug("OK - present in expanded value set (expansion contains {} codes)",
							 valueSet.getExpansion().getContains().size());
				return mapCodingToSuccessfulParameters(coding);
			}
			log.debug("FAIL - not present in expanded value set (expansion contains {} codes)",
						 valueSet.getExpansion().getContains().size());
			return mapCodeErrorToParameters(
				"The code '%s' is not in the value set '%s' (expansion contains %d codes)".formatted(
					coding.getCode(),
					url,
					valueSet.getExpansion().getContains().size()
				),
				coding
			);
		}

		servletResponse.setStatus(422);
		// Original error message is:
		// Unable to find code to validate (looked for coding | codeableConcept | code)
		return mapErrorToOperationOutcome("Unable to find code to validate (looked for coding)");
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
					final var pattern = Pattern.compile(filter.getOp().toCode());
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
		requireNonNull(cacheId);
		requireNonNull(valueSetUrl);
		return Optional.ofNullable(this.valueSetCache.get(cacheId))
			.map(m -> m.get(valueSetUrl))
			.orElse(null);
	}

	private void cacheExpandedValueSet(final String cacheId,
												  final String valueSetUrl,
												  final ValueSet valueSet) {
		requireNonNull(cacheId);
		requireNonNull(valueSetUrl);
		requireNonNull(valueSet);
		this.valueSetCache
			.computeIfAbsent(cacheId, k -> new HashMap<>(20))
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
