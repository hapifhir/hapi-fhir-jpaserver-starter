package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.json.JSONArray;
import org.json.JSONObject;

public class WellknownEndpointHelper {

  private static final String ISSUER_KEY = "issuer";
  private static final String JWKS_ENDPOINT_KEY = "jwks_uri";
  private static final String AUTHORIZATION_ENDPOINT_KEY = "authorization_endpoint";
  private static final String GRANT_TYPES_SUPPORTED_KEY = "grant_types_supported";
  private static final String TOKEN_ENDPOINT_KEY = "token_endpoint";
  private static final String SCOPES_SUPPORTED_KEY = "scopes_supported";
  private static final String RESPONSE_TYPES_SUPPORTED_KEY = "response_types_supported";
  private static final String INTROSPECTION_ENDPOINT_KEY = "introspection_endpoint";
  private static final String REVOCATION_ENDPOINT_KEY = "revocation_endpoint";
  private static final String CAPABILITIES_KEY = "capabilities";
  private static final String CODE_CHALLENGE_METHODS_SUPPORTED_KEY = "code_challenge_methods_supported";

  private WellknownEndpointHelper() {
  }

  /**
   * Helper function to return metadata.
   * See https://www.hl7.org/fhir/smart-app-launch/conformance.html#using-well-known.
   *
   * @return String representing json object of metadata returned at this url
   */
  public static String getWellKnownJson(AppProperties appProperties) {
    return new JSONObject()
      .put(ISSUER_KEY, appProperties.getOauth().getIssuer())
      .put(JWKS_ENDPOINT_KEY, appProperties.getOauth().getJwks_url())
      .put(AUTHORIZATION_ENDPOINT_KEY, appProperties.getOauth().getAuthorization_url())
      .put(GRANT_TYPES_SUPPORTED_KEY, appProperties.getOauth().getGrant_types_supported())
      .put(TOKEN_ENDPOINT_KEY, appProperties.getOauth().getToken_url())
      .put(SCOPES_SUPPORTED_KEY, getScopesSupported())
      .put(RESPONSE_TYPES_SUPPORTED_KEY, getResponseTypesSupported())
      .putOpt(INTROSPECTION_ENDPOINT_KEY, appProperties.getOauth().getIntrospection_url())
      .putOpt(REVOCATION_ENDPOINT_KEY, appProperties.getOauth().getRevocation_url())
      .put(CAPABILITIES_KEY, getCapabilities())
      .put(CODE_CHALLENGE_METHODS_SUPPORTED_KEY, getCodeChallengeMethodsSupported())
      .toString(2);
  }

  private static JSONArray getScopesSupported() {
    return new JSONArray()
      .put("launch")
      .put("profile")
      .put("openid")
      .put("fhirUser")
      .put("online_access")
      .put("offline_access")
      .put("user/*.*")
      .put("patient/*.*")
      .put("system/*.*");
  }

  private static JSONArray getResponseTypesSupported() {
    return new JSONArray()
      .put("code")
      .put("token");
  }

  private static JSONArray getCapabilities() {
    return new JSONArray()
      .put("sso-openid-connect")
      .put("context-patient")
      .put("context-tenant");
  }

  private static JSONArray getCodeChallengeMethodsSupported() {
    return new JSONArray()
      .put("S256");
  }
}
