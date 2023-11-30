package ch.ahdis.matchbox.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.TerminologyCapabilities;

/**
 * An interceptor that provides the terminology capabilities of the server when requested. Otherwise HAPI sends the
 * capability statement again.
 *
 * @author Quentin Ligier
 **/
@Interceptor
public class TerminologyCapabilitiesInterceptor {

	@Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
	public boolean customize(final RequestDetails theRequestDetails,
								 final ResponseDetails theResponseDetails) {
		if (theRequestDetails.getParameters().containsKey("mode") && "terminology".equals(theRequestDetails.getParameters().get("mode")[0])) {
			final var cs = (CapabilityStatement) theResponseDetails.getResponseResource();
			theResponseDetails.setResponseResource(this.getTerminologyCapabilities(cs));
		}
		return true;
	}

	private TerminologyCapabilities getTerminologyCapabilities(final CapabilityStatement cs) {
		final var tc = new TerminologyCapabilities();
		tc.setId(cs.getId());
		tc.setUrl(cs.getUrl());
		tc.setVersion(cs.getVersion());
		tc.setName(cs.getName());
		tc.setTitle(cs.getTitle());
		tc.setStatus(cs.getStatus());
		tc.setExperimental(cs.getExperimental());
		tc.setDate(cs.getDate());
		tc.setPublisher(cs.getPublisher());
		tc.setDescription(cs.getDescription());
		return tc;
	}
}
