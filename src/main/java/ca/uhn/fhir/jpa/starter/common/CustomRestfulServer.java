package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.storage.interceptor.balp.AsyncMemoryQueueBackedFhirClientBalpSink;
import ca.uhn.fhir.storage.interceptor.balp.IBalpAuditContextServices;
import ca.uhn.fhir.storage.interceptor.balp.IBalpAuditEventSink;

import javax.servlet.ServletException;

public class CustomRestfulServer extends RestfulServer {

	/**
	 * Constructor
	 */
	public CustomRestfulServer(FhirContext theCtx) {
		super(theCtx);
	}

	@Override
	protected void initialize() throws ServletException {
		// Register your resource providers and other interceptors here...

		/*
		 * Create our context services object
		 */
		IBalpAuditContextServices contextServices = new BalpAuditContextServices();

		IBalpAuditEventSink eventSink =
			new AsyncMemoryQueueBackedFhirClientBalpSink(
				FhirContext.forR4Cached(), "http://localhost:8080/fhir/dev");

		registerInterceptor(new CustomBalpAuditCaptureInterceptor(eventSink, contextServices));
	}

}
