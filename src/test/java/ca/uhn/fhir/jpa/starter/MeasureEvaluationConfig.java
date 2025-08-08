package ca.uhn.fhir.jpa.starter;

import org.opencds.cqf.fhir.cr.measure.MeasureEvaluationOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeasureEvaluationConfig {

	@Bean
	public MeasureEvaluationOptions measureEvaluationOptions(){
		return new MeasureEvaluationOptions();
	}
}
