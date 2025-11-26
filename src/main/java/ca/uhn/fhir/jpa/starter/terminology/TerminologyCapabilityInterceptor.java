package ca.uhn.fhir.jpa.starter.terminology;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.TerminologyCapabilities;

import java.util.Arrays;

@Interceptor
public class TerminologyCapabilityInterceptor {
	@Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
	public void responseHandling(ResponseDetails theResponseDetails, RequestDetails theRequestDetails) {
		var mode = theRequestDetails.getParameters().get("mode");
		if (mode != null && Arrays.asList(mode).contains("terminology") && theResponseDetails.getResponseResource() instanceof CapabilityStatement) {
			var capabilities = new TerminologyCapabilities();
			capabilities.setVersion("1.0.0");
			capabilities.setName("TerminologyCapabilities");
			capabilities.setTitle("Terminology capabilities");
			theResponseDetails.setResponseResource(capabilities);
		}
	}
}
