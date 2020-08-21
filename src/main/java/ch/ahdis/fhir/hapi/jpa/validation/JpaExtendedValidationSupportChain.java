package ch.ahdis.fhir.hapi.jpa.validation;

import javax.annotation.PostConstruct;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.validation.JpaValidationSupportChain;

public class JpaExtendedValidationSupportChain extends JpaValidationSupportChain {

  public JpaExtendedValidationSupportChain(FhirContext theFhirContext) {
    super(theFhirContext);
  }

  @PostConstruct
  public void postConstruct() {
    super.postConstruct();
  }
}
