package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.jpa.starter.annotations.OnDSTU3Condition;
import org.opencds.cqf.fhir.cr.hapi.config.dstu3.ApplyOperationConfig;
import org.opencds.cqf.fhir.cr.hapi.config.dstu3.CrDstu3Config;
import org.opencds.cqf.fhir.cr.hapi.config.dstu3.DataRequirementsOperationConfig;
import org.opencds.cqf.fhir.cr.hapi.config.dstu3.EvaluateOperationConfig;
import org.opencds.cqf.fhir.cr.hapi.config.dstu3.PackageOperationConfig;
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
