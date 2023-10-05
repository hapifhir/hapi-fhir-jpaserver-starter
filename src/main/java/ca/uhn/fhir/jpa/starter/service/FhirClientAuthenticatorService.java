package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Import(AppProperties.class)
@Service
public class FhirClientAuthenticatorService {
	
	private static final long AUTH_FIXED_DELAY = 50 * 60000;

	private static final long AUTH_INITIAL_DELAY = 50 * 60000;

	@Autowired
	AppProperties appProperties;
	
	private Keycloak keycloak;

	private static FhirContext ctx;
	static String serverBase;
	private Keycloak instance;
	private TokenManager tokenManager;
	private static BearerTokenAuthInterceptor authInterceptor;

	public void initializeKeycloak() {
		  ctx = FhirContext.forCached(FhirVersionEnum.R4);
		  ctx.getRestfulClientFactory().setSocketTimeout(900 * 1000);
		  ctx.getRestfulClientFactory().setConnectionRequestTimeout(900 * 1000);
		  serverBase = appProperties.getHapi_Server_address();
//		  ResteasyClient client = (ResteasyClient)ClientBuilder.newClient();
		    keycloak = KeycloakBuilder
		       .builder()
		       .serverUrl(appProperties.getKeycloak_Server_address())
		       .grantType(OAuth2Constants.PASSWORD)
		       .realm(appProperties.getKeycloak_Realm())
		       .clientId(appProperties.getKeycloak_Client_Id())
		       .username (appProperties.getKeycloak_Username())
		       .password(appProperties.getKeycloak_Password())
//		       .resteasyClient(client)
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
	  authInterceptor = new BearerTokenAuthInterceptor(accessToken); // the reason this is below is to unregister interceptors to avoid memory leak. Null pointer is caught in try catch. 
	}
	
	public Keycloak getKeycloak() {
		return keycloak;
	}
	
	public static IGenericClient getFhirClient() {
		IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);
		fhirClient.registerInterceptor(authInterceptor);
		return fhirClient;
	}

}
