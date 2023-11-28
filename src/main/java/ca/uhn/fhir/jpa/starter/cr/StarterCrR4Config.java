package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.cr.config.r4.CrR4Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

@Conditional({OnR4Condition.class, CrConfigCondition.class})
@Import({CrR4Config.class})
public class StarterCrR4Config {
}
