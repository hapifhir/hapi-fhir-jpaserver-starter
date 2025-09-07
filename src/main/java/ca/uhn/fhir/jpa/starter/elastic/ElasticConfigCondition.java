package ca.uhn.fhir.jpa.starter.elastic;

import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ElasticConfigCondition implements Condition {

	@Override
	public boolean matches(ConditionContext theConditionContext, AnnotatedTypeMetadata theAnnotatedTypeMetadata) {
		ElasticsearchProperties config = Binder.get(theConditionContext.getEnvironment())
				.bind("spring.elasticsearch", ElasticsearchProperties.class)
				.orElse(null);
		if (config == null) return false;
		else return true;
	}
}
