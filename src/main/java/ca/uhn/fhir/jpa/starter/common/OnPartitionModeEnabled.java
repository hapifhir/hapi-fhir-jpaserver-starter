package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnPartitionModeEnabled implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		var appProperties = Binder.get(context.getEnvironment())
				.bind("hapi.fhir", AppProperties.class)
				.orElse(null);
		if (appProperties == null) return false;
		return appProperties.getPartitioning() != null;
	}
}
