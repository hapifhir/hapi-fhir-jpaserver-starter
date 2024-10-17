package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.cr.config.dstu3.ApplyOperationConfig;
import ca.uhn.fhir.cr.config.dstu3.CrDstu3Config;
import ca.uhn.fhir.cr.config.dstu3.DataRequirementsOperationConfig;
import ca.uhn.fhir.cr.config.dstu3.EvaluateOperationConfig;
import ca.uhn.fhir.cr.config.dstu3.PackageOperationConfig;
import ca.uhn.fhir.jpa.starter.annotations.OnDSTU3Condition;
import org.springframework.context.annotation.*;

@Configuration
@Conditional({OnDSTU3Condition.class, CrConfigCondition.class})
@Import({
	CrCommonConfig.class,
	CrDstu3Config.class,
	ApplyOperationConfig.class,
	DataRequirementsOperationConfig.class,
	EvaluateOperationConfig.class,
	PackageOperationConfig.class
})
public class StarterCrDstu3Config {}
