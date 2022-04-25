package ca.uhn.fhir.jpa.starter.tester.interceptor;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.tester.FhirClientFactory;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeycloakAuthInterceptor implements IClientInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(KeycloakAuthInterceptor.class);

    private Keycloak keycloak;

    public KeycloakAuthInterceptor(AppProperties.KeycloakConfig keycloakConfig) {
        buildKeycloak(keycloakConfig);
    }

    private void buildKeycloak(AppProperties.KeycloakConfig keycloakConfig) {
        KeycloakBuilder keycloakBuilder = KeycloakBuilder.builder()
                .serverUrl(keycloakConfig.getServerUrl())
                .realm(keycloakConfig.getRealm())
                .clientId(keycloakConfig.getResource());
        if (keycloakConfig.getCredentials().getSecret() != null) {
            keycloakBuilder
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientSecret(keycloakConfig.getCredentials().getSecret());
        } else {
            keycloakBuilder
                    .grantType(OAuth2Constants.PASSWORD)
                    .username(keycloakConfig.getCredentials().getUsername())
                    .password(keycloakConfig.getCredentials().getPassword());
        }
        keycloak = keycloakBuilder.build();
    }

    @Override
    public void interceptRequest(IHttpRequest theRequest) {
        theRequest.addHeader(
                Constants.HEADER_AUTHORIZATION,
                (Constants.HEADER_AUTHORIZATION_VALPREFIX_BEARER + keycloak.tokenManager().getAccessTokenString())
        );
		 logger.info(theRequest.getUri());
		 theRequest.getAllHeaders().keySet().forEach(
			 header -> theRequest.getAllHeaders().get(header).forEach(
				 headerValue -> logger.info("\"" + header + "\": " + "\"" + headerValue + "\"")
			 )
		 );
    }

    @Override
    public void interceptResponse(IHttpResponse iHttpResponse) {
        // nothing
    }
}
