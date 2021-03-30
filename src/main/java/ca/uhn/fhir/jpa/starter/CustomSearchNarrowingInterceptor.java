package ca.uhn.fhir.jpa.starter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizedList;
import ca.uhn.fhir.rest.server.interceptor.auth.SearchNarrowingInterceptor;

@Interceptor
public class CustomSearchNarrowingInterceptor extends SearchNarrowingInterceptor {

  private static final String OAUTH_CLAIM_NAME = System.getenv("OAUTH_CLAIM_NAME");
  
  private OAuth2Helper oAuth2Helper = new OAuth2Helper();

  @Override
  protected AuthorizedList buildAuthorizedList(RequestDetails theRequestDetails) {
    String patientId = getPatientFromToken(theRequestDetails);
    if (patientId != null) {
      String patientRef = "Patient/" + patientId;
      return new AuthorizedList().addCompartment(patientRef);
    }
    return new AuthorizedList();
  }

  private String getPatientFromToken(RequestDetails theRequestDetails) {
    String token = theRequestDetails.getHeader("Authorization");
    if (token != null) {
      token = token.substring(CustomAuthorizationInterceptor.getTokenPrefix().length());
      DecodedJWT jwt = JWT.decode(token);
      String patRefId = oAuth2Helper.getPatientReferenceFromToken(jwt, OAUTH_CLAIM_NAME);
      return patRefId;
    }
    return null;
  }

}
