package ca.uhn.fhir.jpa.starter.interceptor;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Interceptor class for handling API key-related tasks during incoming server requests.
 * <p>
 * Registered using the {@link @Interceptor} annotation, this class hooks into the
 * server's request-processing lifecycle at the defined pointcut.
 */
@Component
@Interceptor
public class APIKeyInterceptor extends AuthorizationInterceptor {

	private final Logger logger = LoggerFactory.getLogger(APIKeyInterceptor.class);

	@Value("${security.api-keys}")
	private Set<String> registeredKeys;

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

		// In this basic example we have hardcoded API Keys.
		boolean userIsAdmin = false;
		String apiKey = theRequestDetails.getHeader("X-API-KEY");
		logger.info("API Key from header is {}.", apiKey);

		if (apiKey != null && registeredKeys.contains(apiKey)) {
			logger.info("The api key matches one of the keys registered in application.yaml. Unrestricted access will be granted.");
			return new RuleBuilder().allowAll().build();
		}

		if (apiKey == null || !registeredKeys.contains(apiKey)) {

			logger.info("API key is null or not registered. Only read access will be granted.");

			return new RuleBuilder()
				.allow()
				.read()
				.allResources()
				.withAnyId()
				.build();
		}

		throw new AuthenticationException(Msg.code(644) + "Missing or invalid X-API-KEY header value");
	}
}
