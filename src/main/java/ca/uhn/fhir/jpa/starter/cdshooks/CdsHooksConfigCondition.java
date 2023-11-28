package ca.uhn.fhir.jpa.starter.cdshooks;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CdsHooksConfigCondition implements Condition {

	@Override
	public boolean matches(ConditionContext theConditionContext, AnnotatedTypeMetadata theAnnotatedTypeMetadata) {
		String property = theConditionContext.getEnvironment().getProperty("hapi.fhir.cdshooks.enabled");
		return Boolean.parseBoolean(property);
	}
}
