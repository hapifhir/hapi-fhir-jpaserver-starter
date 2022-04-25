package ca.uhn.fhir.jpa.starter.tester;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.tester.interceptor.AuthorizationHeaderAuthInterceptor;
import ca.uhn.fhir.jpa.starter.tester.interceptor.KeycloakAuthInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class FhirClientFactory implements ITestingUiClientFactory {

	private final AppProperties.Tester tester;
	private final AppProperties.Auth auth;
	private static final Logger logger = LoggerFactory.getLogger(FhirClientFactory.class);

	public FhirClientFactory(AppProperties.Tester tester) {
		this.tester = tester;
		this.auth = tester.getAuth();
		logger.info("Created FHIR client factory for FHIR server [{}] with authentication type [{}].",
			tester.getName(),
			Optional.ofNullable(auth).orElse(new AppProperties.Auth()).getType());
	}

	@Override
	public IGenericClient newClient(FhirContext theFhirContext, HttpServletRequest theRequest, String theServerBaseUrl) {
		// Create a client
		IGenericClient client = theFhirContext.newRestfulGenericClient(theServerBaseUrl);

		// if no auth specified, we are done
		if (auth == null) {
			logger.debug("Register no auth interceptor for FHIR server [{}]", tester.getName());
			return client;
		}

		if ("BASIC".equalsIgnoreCase(auth.getType())) {
			logger.debug("Register basic auth interceptor for FHIR server [{}]", tester.getName());
			client.registerInterceptor(
				new BasicAuthInterceptor(auth.getUsername(), auth.getPassword()));
			return client;
		}

		if ("BEARER".equalsIgnoreCase(auth.getType())) {
			logger.debug("Register bearer token auth interceptor for FHIR server [{}]", tester.getName());
			client.registerInterceptor(
				new BearerTokenAuthInterceptor(auth.getToken())
			);
			return client;
		}

		if ("HEADER".equalsIgnoreCase(auth.getType())) {
			logger.debug("Register authorization header interceptor for FHIR server [{}]", tester.getName());
			client.registerInterceptor(
				new AuthorizationHeaderAuthInterceptor(auth.getHeader())
			);
			return client;
		}

		if ("KEYCLOAK".equalsIgnoreCase(auth.getType())) {
			logger.debug("Register keycloak authorization interceptor for FHIR server [{}]", tester.getName());
			client.registerInterceptor(
				new KeycloakAuthInterceptor(auth.getKeycloakConfig())
			);
			return client;
		}

		logger.warn("Cannot identify the authentication type for FHIR server [{}]. Hence, no auth!", tester.getName());

		return client;
	}
}
