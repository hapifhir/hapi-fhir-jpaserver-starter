package ca.uhn.fhir.jpa.starter;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.jpa.starter.service.HelperService;
import ca.uhn.fhir.jpa.starter.service.ServerInterceptor;

import javax.servlet.ServletException;
import javax.ws.rs.client.ClientBuilder;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  @Autowired
  HelperService helperService;
  
  private static final long serialVersionUID = 1L;
  public static Keycloak keycloak;
  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();
    ServerInterceptor serverInterceptor = new ServerInterceptor();
    registerInterceptor(serverInterceptor);
    // Add your own customization here
    helperService.initializeKeycloak();
  }
}
