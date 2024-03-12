package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.cr.config.r4.ApplyOperationConfig;
import ca.uhn.fhir.cr.config.r4.CrR4Config;
import ca.uhn.fhir.cr.config.r4.ExtractOperationConfig;
import ca.uhn.fhir.cr.config.r4.PackageOperationConfig;
import ca.uhn.fhir.cr.config.r4.PopulateOperationConfig;
import ca.uhn.fhir.cr.config.r4.QuestionnaireOperationConfig;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional({OnR4Condition.class, CrConfigCondition.class})
@Import({
	CrCommonConfig.class,
	CrR4Config.class,
	ApplyOperationConfig.class,
	ExtractOperationConfig.class,
	PackageOperationConfig.class,
	PopulateOperationConfig.class,
	QuestionnaireOperationConfig.class
})
public class StarterCrR4Config {
	
}
