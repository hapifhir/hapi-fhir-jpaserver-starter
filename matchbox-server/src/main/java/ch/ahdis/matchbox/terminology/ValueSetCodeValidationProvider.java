package ch.ahdis.matchbox.terminology;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.context.support.ValueSetExpansionOptions;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static ch.ahdis.matchbox.terminology.TerminologyUtils.*;
import static java.util.Objects.requireNonNull;

/**
 * The HAPI FHIR provider for the ValueSet/$validate-code operation.
 *
 * @author Quentin Ligier
 **/
public class ValueSetCodeValidationProvider implements IResourceProvider {
	private static final Logger log = LoggerFactory.getLogger(ValueSetCodeValidationProvider.class);

	private final InMemoryTerminologyServerValidationSupport inMemoryTerminologySupport =
		new InMemoryTerminologyServerValidationSupport(FhirContext.forR4Cached());

	/**
	 * A cache that stores a mapping from value set URLs to expanded value sets, per cache ID.
	 */
	private final Map<String, Map<String, ValueSet>> valueSetCache =
		new PassiveExpiringMap<>(5, TimeUnit.MINUTES);

	private final ValueSetExpansionOptions expansionOptions = new ValueSetExpansionOptions();
	private final ConceptValidationOptions validationOptions = new ConceptValidationOptions();

	private final ValidationSupportContext validationSupportContext =
		new ValidationSupportContext(new DummyValidationSupport(FhirContext.forR4Cached()));

	public ValueSetCodeValidationProvider() {
		this.expansionOptions.setFailOnMissingCodeSystem(false);
		this.validationOptions.setValidateDisplay(false);
	}

	/**
	 *
	 */
	@Operation(name = "$validate-code", idempotent = true)
	public IAnyResource validateCode(@ResourceParam final Parameters request,
												final HttpServletResponse servletResponse) {
		final String valueSetMode = request.hasParameter("valueSetMode")
			? request.getParameterValue("valueSetMode").toString()
			: "DEFAULT";

		final String cacheId = request.hasParameter("cache-id")
			? request.getParameterValue("cache-id").toString()
			: null;

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
			if (request.hasParameter("url")) {
				url = request.getParameterValue("url").toString();
			} else if (request.hasParameter("valueSet")) {
				valueSet = (ValueSet) request.getParameter("valueSet").getResource();
				url = valueSet.getUrl();
			}
			log.debug("Validating code in VS: {}|{} in {}", coding.getCode(), coding.getSystem(), url);

			if (valueSet == null) {
				valueSet = this.getExpandedValueSet(cacheId, url);
				if (valueSet == null) {
					// That value set is not cached
					log.debug("OK - cache miss");
					return mapCodingToSuccessfulParameters(coding);
				}
			} else {
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
				valueSet = (ValueSet) result.getValueSet();
				if (cacheId != null) {
					this.cacheExpandedValueSet(cacheId, url, valueSet);
				}
			}

			// Now we have an expanded value set, we can properly validate the code
			if (this.validateCodeInValueSet(coding, valueSet)) {
				log.debug("OK - present in expanded value set");
				return mapCodingToSuccessfulParameters(coding);
			}
			log.debug("FAIL - not present in expanded value set");
			return mapCodeErrorToParameters("The code " + coding.getCode() + " is not in the value set " + url);
		}

		servletResponse.setStatus(422);
		// Original error message is:
		// Unable to find code to validate (looked for coding | codeableConcept | code)
		return mapErrorToOperationOutcome("Unable to find code to validate (looked for coding)");
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

	private boolean validateCodeInValueSet(final Coding coding,
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
		return ValueSet.class;
	}


	public class DummyValidationSupport implements IValidationSupport {

		FhirContext context;

		DummyValidationSupport(FhirContext context) {
			this.context = context;
		}

		@Override
		public FhirContext getFhirContext() {
			return context;
		}
	}
}
