package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.config.r4.JpaR4Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.jpa.starter.cr.StarterCrR4Config;
import ca.uhn.fhir.jpa.starter.ips.StarterIpsConfig;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(OnR4Condition.class)
@Import({
	JpaR4Config.class,
	StarterJpaConfig.class,
	StarterCrR4Config.class,
	ElasticsearchConfig.class,
	StarterIpsConfig.class
})
public class FhirServerConfigR4 {}
