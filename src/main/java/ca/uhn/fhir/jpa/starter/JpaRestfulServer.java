package ca.uhn.fhir.jpa.starter;

import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    // Add your own customization here
    AuthorizationInterceptor authorizationInterceptor = new CustomAuthorizationInterceptor();
    this.registerInterceptor(authorizationInterceptor);
  }

}