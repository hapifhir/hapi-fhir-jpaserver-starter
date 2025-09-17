package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.EnvironmentHelper;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnPartitionModeEnabled implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		var appProperties = EnvironmentHelper.getConfiguration(context, "hapi.fhir", AppProperties.class);
		if (appProperties == null) return false;
		return appProperties.getPartitioning() != null;
	}
}
