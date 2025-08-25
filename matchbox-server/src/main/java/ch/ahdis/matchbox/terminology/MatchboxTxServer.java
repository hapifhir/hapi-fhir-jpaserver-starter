package ch.ahdis.matchbox.terminology;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;

public class MatchboxTxServer extends RestfulServer {

	public MatchboxTxServer(final FhirContext fhirContext) {
		super(fhirContext);
	}
}
