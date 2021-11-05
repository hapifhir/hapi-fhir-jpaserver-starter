package ca.uhn.fhir.jpa.starter.smart.util;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

// Originally from https://github.com/mcode/smart-backend-auths
public class WellknownEndpointHelper {
	// Well Known JSON Keys
	private static final String WELL_KNOWN_TOKEN_ENDPOINT_KEY = "token_endpoint";
	private static final String WELL_KNOWN_REGISTRATION_ENDPOINT_KEY = "registration_endpoint";
	private static final String RESPONSE_TYPES_SUPPORTED_KEY = "response_types_supported";
	private static final String SCOPES_SUPPORTED_KEY = "scopes_supported";

	private WellknownEndpointHelper(){

	}

	/**
	 * Create the .well-known/smart-configuration JSON object
	 *
	 * @param tokenEndpointUrl        - the OAuth token endpoint
	 * @param registrationEndpointUrl - (optional) the OAuth dynamic client registration endpoint
	 * @return the .well-known/smart-configuration JSON object
	 */
	public static String getWellKnownJson(String tokenEndpointUrl, String registrationEndpointUrl) throws JSONException {
		JSONArray scopesSupported = new JSONArray();
		scopesSupported.put("system/*.*");
		scopesSupported.put("system/*.read");
		scopesSupported.put("system/*.write");
		scopesSupported.put("offline_access");

		JSONArray responseTypesSupported = new JSONArray();
		responseTypesSupported.put("token");

		JSONObject wellKnownJson = new JSONObject();
		wellKnownJson.put(WELL_KNOWN_TOKEN_ENDPOINT_KEY, tokenEndpointUrl);
		wellKnownJson.put(RESPONSE_TYPES_SUPPORTED_KEY, responseTypesSupported);
		wellKnownJson.put(SCOPES_SUPPORTED_KEY, scopesSupported);

		if (registrationEndpointUrl != null) {
			wellKnownJson.put(WELL_KNOWN_REGISTRATION_ENDPOINT_KEY, registrationEndpointUrl);
		}

		return wellKnownJson.toString(2);
	}
}
