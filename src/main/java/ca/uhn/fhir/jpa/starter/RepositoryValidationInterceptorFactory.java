package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.interceptor.validation.IRepositoryValidatingRule;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingRuleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * This class can be customized to enable the {@link ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor}
 * on this server.
 *
 * The <code>enable_repository_validating_interceptor</code> property must be enabled in <code>application.yaml</code>
 * in order to use this class.
 */
public class RepositoryValidationInterceptorFactory {

	@Autowired
	private ApplicationContext myApplicationContext;
	@Autowired
	private FhirContext myFhirContext;

	public RepositoryValidatingInterceptor build() {
		RepositoryValidatingRuleBuilder ruleBuilder = myApplicationContext.getBean(RepositoryValidatingRuleBuilder.class);

		// Customize the ruleBuilder here to have the rules you want! We will give a simple example
		// of enabling validation for all Patient resources
		ruleBuilder.forResourcesOfType("Patient").requireValidationToDeclaredProfiles();

		// Do not customize below this line
		List<IRepositoryValidatingRule> rules = ruleBuilder.build();
		return new RepositoryValidatingInterceptor(myFhirContext, rules);
	}

}
