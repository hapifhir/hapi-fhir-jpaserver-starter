package ca.uhn.fhir.jpa.starter.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor class for handling API key-related tasks during incoming server requests.
 *
 * Registered using the {@link @Interceptor} annotation, this class hooks into the
 * server's request-processing lifecycle at the defined pointcut.
 */
@Interceptor
public class APIKeyInterceptor {

	private final Logger ourLog = LoggerFactory.getLogger(APIKeyInterceptor.class);

	@Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
	public boolean logRequests(RequestDetails theRequest) {

		ourLog.info("Request of type {} with request ID: {}", theRequest.getRequestType(), theRequest.getRequestId());

		return true; // if false, the request is blocked and the connection is closed
	}
}