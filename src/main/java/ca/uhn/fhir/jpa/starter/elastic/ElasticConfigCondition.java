package ca.uhn.fhir.jpa.starter.elastic;

import ca.uhn.fhir.jpa.starter.util.EnvironmentHelper;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ElasticConfigCondition implements Condition {

	@Override
	public boolean matches(ConditionContext theConditionContext, AnnotatedTypeMetadata theAnnotatedTypeMetadata) {
		return EnvironmentHelper.getConfiguration(
						theConditionContext, "spring.elasticsearch", ElasticsearchProperties.class)
				!= null;
	}
}
