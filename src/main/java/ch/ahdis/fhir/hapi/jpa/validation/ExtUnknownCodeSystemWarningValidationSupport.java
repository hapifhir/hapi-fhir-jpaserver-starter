package ch.ahdis.fhir.hapi.jpa.validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.ValidationSupportContext;

/**
 * This validation support module may be placed at the end of a {@link ValidationSupportChain}
 * in order to configure the validator to generate a warning if a resource being validated
 * contains an unknown code system.
 *
 * Note that this module must also be activated by calling {@link #setAllowNonExistentCodeSystem(boolean)}
 * in order to specify that unknown code systems should be allowed.
 */
public class ExtUnknownCodeSystemWarningValidationSupport extends org.hl7.fhir.common.hapi.validation.support.UnknownCodeSystemWarningValidationSupport {

	public ExtUnknownCodeSystemWarningValidationSupport(FhirContext theFhirContext) {
		super(theFhirContext);
	}

	@Nullable
	@Override
	public CodeValidationResult validateCodeInValueSet(ValidationSupportContext theValidationSupportContext, ConceptValidationOptions theOptions, String theCodeSystem, String theCode, String theDisplay, @Nonnull IBaseResource theValueSet) {
		if (theCodeSystem == null) {
			return null;
		}
		return super.validateCodeInValueSet(theValidationSupportContext, theOptions, theCodeSystem, theCode, theDisplay, theValueSet);
	}

}
