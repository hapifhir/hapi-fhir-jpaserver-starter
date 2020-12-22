package ch.ahdis.fhir.hapi.jpa.validation;

import ca.uhn.fhir.jpa.term.TermReadSvcR4;

public class ExtTermReadSvcR4 extends TermReadSvcR4 {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExtTermReadSvcR4.class);

  @Override
	public synchronized void preExpandDeferredValueSetsToTerminologyTables() {
      ourLog.info("Skipping scheduled pre-expansion to rely on InMemoryTerminologyServerValidationSupport. Be aware: codes in expanded ValueSets are not validated against a terminology server");
  }

}
