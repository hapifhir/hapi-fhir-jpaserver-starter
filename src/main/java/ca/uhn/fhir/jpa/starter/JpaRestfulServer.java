package ca.uhn.fhir.jpa.starter;

import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.SearchNarrowingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentInterceptor;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {
	
	private static final String FHIR_VERSION = System.getenv("fhir_version");
	private static final String OAUTH_ENABLED = System.getenv("OAUTH_ENABLED");

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
    
    /* Custom ServerConformanceProvider will be triggered when fhir version is R4 and Oauth is enabled. */
    if (FHIR_VERSION.equals(FhirVersionEnum.R4.name()) && Boolean.parseBoolean(OAUTH_ENABLED)) {
    	CustomServerCapabilityStatementProviderR4 customCapabilityStatementProviderR4 = new CustomServerCapabilityStatementProviderR4(this);
    	setServerConformanceProvider(customCapabilityStatementProviderR4);
    }
    SearchNarrowingInterceptor customSearchNarrowingInterceptor = new CustomSearchNarrowingInterceptor();
    this.registerInterceptor(customSearchNarrowingInterceptor);
    ConsentInterceptor consentInterceptor = new ConsentInterceptor(new CustomConsentService(super.daoRegistry));
    this.registerInterceptor(consentInterceptor);
    AuthorizationInterceptor authorizationInterceptor = new CustomAuthorizationInterceptor();
    this.registerInterceptor(authorizationInterceptor);
  }
}
