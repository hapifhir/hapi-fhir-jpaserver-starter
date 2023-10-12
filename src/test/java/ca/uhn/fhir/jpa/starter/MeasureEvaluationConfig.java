package ca.uhn.fhir.jpa.starter;

import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cr.measure.MeasureEvaluationOptions;
import org.opencds.cqf.fhir.utility.ValidationProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MeasureEvaluationConfig {

	@Bean
	public MeasureEvaluationOptions measureEvaluationOptions(){
		return new MeasureEvaluationOptions();
	}
}
