package ca.uhn.fhir.jpa.starter.annotations;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnCorsPresent implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {

		AppProperties config = Binder.get(conditionContext.getEnvironment()).bind("hapi.fhir", AppProperties.class).orElse(null);
		if (config == null) return false;
		if (config.getCors() == null) return false;
		return true;
	}
}