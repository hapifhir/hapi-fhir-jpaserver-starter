package ca.uhn.fhir.jpa.starter.service;

import javax.ws.rs.client.ClientBuilder;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

@Import(AppProperties.class)
@Service
public class FhirClientAuthenticatorService {
	
	private static final long AUTH_FIXED_DELAY = 50 * 60000;

	private static final long AUTH_INITIAL_DELAY = 50 * 60000;

	@Autowired
	AppProperties appProperties;
	
	private static Keycloak keycloak;
	private static IGenericClient fhirClient;

	private FhirContext ctx;
	static String serverBase;
	private Keycloak instance;
	private TokenManager tokenManager;
	private BearerTokenAuthInterceptor authInterceptor;

	public void initializeKeycloak() {
		  ctx = FhirContext.forCached(FhirVersionEnum.R4);
		  serverBase = appProperties.getHapi_Server_address();
		  fhirClient = ctx.newRestfulGenericClient(serverBase);     
		  ResteasyClient client = (ResteasyClient)ClientBuilder.newClient();
		    keycloak = KeycloakBuilder
		       .builder()
		       .serverUrl(appProperties.getKeycloak_Server_address())
		       .grantType(OAuth2Constants.PASSWORD)
		       .realm(appProperties.getKeycloak_Realm())
		       .clientId(appProperties.getKeycloak_Client_Id())
		       .username (appProperties.getKeycloak_Username())
		       .password(appProperties.getKeycloak_Password())
		       .resteasyClient(client)
		       .build();
		  instance = Keycloak.
		      getInstance(
		        appProperties.getKeycloak_Server_address(),
		        appProperties.getKeycloak_Client_Realm(),
		        appProperties.getFhir_user(),
		        appProperties.getFhir_password(),
		        appProperties.getFhir_hapi_client_id(),
		        appProperties.getFhir_hapi_client_secret()
		      );
		  tokenManager = instance.tokenManager();
		  registerClientAuthInterceptor();
		}

	@Scheduled(fixedDelay = AUTH_FIXED_DELAY, initialDelay = AUTH_INITIAL_DELAY)
	private void registerClientAuthInterceptor() {
	  String accessToken = tokenManager.getAccessTokenString();
	  try {
	    fhirClient.unregisterInterceptor(authInterceptor);
	  }catch(Exception e) {
	    e.printStackTrace();
	  }
	  authInterceptor = new BearerTokenAuthInterceptor(accessToken); // the reason this is below is to unregister interceptors to avoid memory leak. Null pointer is caught in try catch.
	  fhirClient = null;
	  fhirClient = ctx.newRestfulGenericClient(serverBase);
	  fhirClient.registerInterceptor(authInterceptor);
	}
	
	public static Keycloak getKeycloak() {
		return keycloak;
	}
	
	public static IGenericClient getFhirClient() {
		return fhirClient;
	}

}
