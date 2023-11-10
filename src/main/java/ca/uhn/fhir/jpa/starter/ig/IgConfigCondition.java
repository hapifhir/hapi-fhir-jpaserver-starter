package ca.uhn.fhir.jpa.starter.ig;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class IgConfigCondition implements Condition {

	@Override
	public boolean matches(ConditionContext theConditionContext, AnnotatedTypeMetadata theAnnotatedTypeMetadata) {
		String property = theConditionContext.getEnvironment().getProperty("hapi.fhir.ig_runtime_upload_enabled");
		return Boolean.parseBoolean(property);
	}
}
