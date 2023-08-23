package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.apache.commons.lang3.ObjectUtils;

public class ApiKeyHelper {
	private static final String APIKEY_HEADER = "x-api-key";

  private ApiKeyHelper() {}

	public static boolean hasApiKey(RequestDetails theRequest) {
		String apiKey = theRequest.getHeader(APIKEY_HEADER);
		return (!ObjectUtils.isEmpty(apiKey));
	}

  public static String getApiKey(RequestDetails theRequest) {
    return theRequest.getHeader(APIKEY_HEADER);
  }

  public static boolean isAuthorized(RequestDetails theRequest, String apiKey) {
    return apiKey.equals(getApiKey(theRequest));
  }
}
