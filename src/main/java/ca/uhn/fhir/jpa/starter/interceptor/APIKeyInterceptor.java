package ca.uhn.fhir.jpa.starter.interceptor;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.ValueSet;
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

		// In this basic example we have API Keys configured in application.yaml.
		String apiKey = theRequestDetails.getHeader("X-API-KEY");

		if (apiKey != null && registeredKeys.contains(apiKey)) {
			logger.debug("The api key matches one of the keys registered in application.yaml. Unrestricted access will be granted.");
			return new RuleBuilder().allowAll().build();
		}

		if (apiKey == null || !registeredKeys.contains(apiKey)) {

			logger.debug("API key is null or not registered. Only read access will be granted.");

			var builder = new RuleBuilder();
			builder.allow().metadata();
			allowReadAndOperationsOnResourceType(builder, CodeSystem.class, List.of("lookup", "validate-code", "subsumes"));
			allowReadAndOperationsOnResourceType(builder, ValueSet.class, List.of("expand", "validate-code"));
			allowReadAndOperationsOnResourceType(builder, ConceptMap.class, List.of("translate"));
			return builder.build();
		}

		throw new AuthenticationException(Msg.code(644) + "Missing or invalid X-API-KEY header value");
	}

	private void allowReadAndOperationsOnResourceType(IAuthRuleBuilder builder, Class<? extends IBaseResource> type, List<String> operations) {
		builder
			.allow()
			.read()
			.resourcesOfType(type)
			.withAnyId();
		for (String operation : operations) {
			builder.allow()
				.operation().named(operation)
				.onType(type)
				.andAllowAllResponses();
		}
	}
}
