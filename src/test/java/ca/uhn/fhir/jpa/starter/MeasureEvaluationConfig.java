package ca.uhn.fhir.jpa.starter;

import org.opencds.cqf.fhir.cr.measure.MeasureEvaluationOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "hapi.fhir.cr.enabled", havingValue = "false", matchIfMissing = true)
public class MeasureEvaluationConfig {

	@Bean
	public MeasureEvaluationOptions measureEvaluationOptions(){
		return new MeasureEvaluationOptions();
	}
}
