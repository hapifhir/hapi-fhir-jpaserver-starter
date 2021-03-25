package ca.uhn.fhir.jpa.starter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizedList;
import ca.uhn.fhir.rest.server.interceptor.auth.SearchNarrowingInterceptor;

@Interceptor
public class CustomSearchNarrowingInterceptor extends SearchNarrowingInterceptor {
  private OAuth2Helper oAuth2Helper = new OAuth2Helper();

  @Override
  protected AuthorizedList buildAuthorizedList(RequestDetails theRequestDetails) {
    String patientId = getPatientFromToken(theRequestDetails);
    String patientRef = "Patient/" + patientId;
    return new AuthorizedList().addCompartment(patientRef);
  }

  private String getPatientFromToken(RequestDetails theRequestDetails) {
    String token = theRequestDetails.getHeader("Authorization");
    token = token.substring(CustomAuthorizationInterceptor.getTokenPrefix().length());
    DecodedJWT jwt = JWT.decode(token);
    String patRefId = oAuth2Helper.getPatientReferenceFromToken(jwt);
    return patRefId;
  }

}
