package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.config.JpaDstu2Config;
import ca.uhn.fhir.jpa.starter.annotations.OnDSTU2Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(OnDSTU2Condition.class)
@Import({
	StarterJpaConfig.class,
	JpaDstu2Config.class
})
public class FhirServerConfigDstu2 {
}
