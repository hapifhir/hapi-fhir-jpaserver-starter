package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import javax.servlet.ServletException;

public class JpaRestfulServer extends BaseJpaRestfulServer {

  private static final long serialVersionUID = 1L;

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    // Add your own customization here
    AuthorizationInterceptor authorizationInterceptor = new CustomAuthorizationInterceptor();
    this.registerInterceptor(authorizationInterceptor);
  }

}
