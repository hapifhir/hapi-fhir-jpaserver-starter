package earth.angelson.security;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Interceptor
public class AuthenticationInterceptor {
	/**
	 * This interceptor implements HTTP Basic Auth, which specifies that
	 * a username and password are provided in a header called Authorization.
	 */
	@Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
	public boolean incomingRequestPostProcessed(
		RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse)
		throws AuthenticationException {

		String authHeader = theRequest.getHeader("Authorization");

		// The format of the header must be:
		if (authHeader == null) {
			throw new AuthenticationException(Msg.code(642) + "Missing or invalid Authorization header");
		} else if ("Bearer dfw98h38r".equals(authHeader)) {
			//todo validate token
			// This user has access only to Practitioner/1 resources
			return true;
		} else if ("Bearer 39ff939jgg".equals(authHeader)) {
			// This user has access to everything
			return true;
		} else {
			// Throw an HTTP 401
			return true;
//			throw new AuthenticationException(Msg.code(644) + "Missing or invalid Authorization header value");
		}
	}
}
