package ca.uhn.fhir.jpa.starter;

import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {
	
	private static final String FHIR_VERSION = System.getenv("fhir_version");
	private static final String OAUTH_ENABLED = System.getenv("OAUTH_ENABLED");

  @Autowired
  AppProperties appProperties;
  
  @Autowired
  CustomServerCapabilityStatementProviderR4 customCapabilityStatementProviderR4;

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    // Add your own customization here
  }

}
