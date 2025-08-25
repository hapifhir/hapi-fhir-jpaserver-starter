package ch.ahdis.matchbox.terminology.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;

public record DummyValidationSupport(FhirContext context) implements IValidationSupport {
	@Override
	public FhirContext getFhirContext() {
		return context;
	}
}
