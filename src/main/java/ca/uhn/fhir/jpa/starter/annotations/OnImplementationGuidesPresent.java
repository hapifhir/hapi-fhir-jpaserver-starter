package ca.uhn.fhir.jpa.starter.annotations;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.EnvironmentHelper;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnImplementationGuidesPresent implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {

		AppProperties config = EnvironmentHelper.getConfiguration(conditionContext, "hapi.fhir", AppProperties.class);
		if (config == null) return false;
		if (config.getImplementationGuides() == null) return false;
		return !config.getImplementationGuides().isEmpty();
	}
}
