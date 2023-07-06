package ca.uhn.fhir.jpa.starter;

import java.util.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import ca.uhn.fhir.rest.api.server.RequestDetails;

public class BasicAuthHelper {

  private static final String AUTHORIZATION_PREFIX = "BASIC ";

  public static String getCredentials(RequestDetails theRequest) {
    String auth = theRequest.getHeader(HttpHeaders.AUTHORIZATION);
    return auth.substring(AUTHORIZATION_PREFIX.length());
  }

  public boolean isValid(String basicAuthUsername, String basicAuthPass, String basicAuthToken) {
    String unEncodedUsernamePass = basicAuthUsername + ":" + basicAuthPass;
    String encodedUsernamePass = Base64.getEncoder().encodeToString(unEncodedUsernamePass.getBytes());
    return encodedUsernamePass.equals(basicAuthToken);
  }

	public static boolean isBasicAuthHeaderPresent(RequestDetails theRequest) {
		String auth = theRequest.getHeader(HttpHeaders.AUTHORIZATION);
		return (!ObjectUtils.isEmpty(auth) && auth.toUpperCase().startsWith(AUTHORIZATION_PREFIX));
	}
}
