package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.interceptor.validation.IRepositoryValidatingRule;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingRuleBuilder;
import ca.uhn.fhir.jpa.starter.annotations.OnEitherVersion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This class can be customized to enable the {@link ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor}
 * on this server.
 * <p>
 * The <code>enable_repository_validating_interceptor</code> property must be enabled in <code>application.yaml</code>
 * in order to use this class.
 */
@ConditionalOnProperty(prefix = "hapi.fhir", name = "enable_repository_validating_interceptor", havingValue = "true")
@Configuration
@Conditional({OnEitherVersion.class})
public class RepositoryValidationInterceptorFactory {

	private final FhirContext fhirContext;
	private final RepositoryValidatingRuleBuilder repositoryValidatingRuleBuilder;

	public RepositoryValidationInterceptorFactory(RepositoryValidatingRuleBuilder repositoryValidatingRuleBuilder, DaoRegistry daoRegistry, IInterceptorService interceptorService) {
		this.repositoryValidatingRuleBuilder = repositoryValidatingRuleBuilder;
		this.fhirContext = daoRegistry.getSystemDao().getContext();
		interceptorService.registerInterceptor(build());
	}

	public RepositoryValidatingInterceptor build() {

		// Customize the ruleBuilder here to have the rules you want! We will give a simple example
		// of enabling validation for all Patient resources
		repositoryValidatingRuleBuilder.forResourcesOfType("Patient").requireValidationToDeclaredProfiles();

		// Do not customize below this line
		List<IRepositoryValidatingRule> rules = repositoryValidatingRuleBuilder.build();
		return new RepositoryValidatingInterceptor(fhirContext, rules);
	}

}
