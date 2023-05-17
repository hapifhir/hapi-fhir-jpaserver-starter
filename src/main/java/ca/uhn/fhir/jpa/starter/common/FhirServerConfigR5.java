package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.config.r5.JpaR5Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR5Condition;
import ca.uhn.fhir.jpa.topic.SubscriptionTopicConfig;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(OnR5Condition.class)
@Import({
	StarterJpaConfig.class,
	JpaR5Config.class,
	SubscriptionTopicConfig.class,
	ElasticsearchConfig.class
})
public class FhirServerConfigR5 {
}
