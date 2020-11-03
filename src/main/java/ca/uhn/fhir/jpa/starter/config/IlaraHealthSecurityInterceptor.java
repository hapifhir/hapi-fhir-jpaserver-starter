package ca.uhn.fhir.jpa.starter.config;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Interceptor
public class IlaraHealthSecurityInterceptor {
  @Value("${ilara.health.security.username}") private String basicUsername;
  @Value("${ilara.health.security.password}") private String basicPassword;
  Logger logger= LoggerFactory.getLogger(this.getClass().getName());


  @Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
  public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {
    logger.info("RMG auth {}",theRequest.getHeader("Authorization"));
    String authHeader = theRequest.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Basic ")) {
      throw new AuthenticationException("Missing or invalid Authorization header");
    }

    String base64 = authHeader.substring("Basic ".length());
    String base64decoded = new String(Base64.decodeBase64(base64));
    String[] parts = base64decoded.split(":");

    String username = parts[0];
    String password = parts[1];
    if (!username.equals(basicUsername) || !password.equals(basicPassword)) {
      AuthenticationException ex = new AuthenticationException();
      ex.addAuthenticateHeaderForRealm("myRealm");
      throw ex;
    }
    return true;
  }
}

