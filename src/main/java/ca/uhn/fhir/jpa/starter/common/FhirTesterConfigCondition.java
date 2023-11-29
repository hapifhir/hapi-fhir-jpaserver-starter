package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.starter.util.EnvironmentHelper;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class FhirTesterConfigCondition implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {

		var properties = EnvironmentHelper.getPropertiesStartingWith(
				(ConfigurableEnvironment) conditionContext.getEnvironment(), "hapi.fhir.tester");
		return !properties.isEmpty();
	}
}
