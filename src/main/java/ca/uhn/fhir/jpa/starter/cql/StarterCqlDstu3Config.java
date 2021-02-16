package ca.uhn.fhir.jpa.starter.cql;

import ca.uhn.fhir.cql.config.CqlDstu3Config;
import ca.uhn.fhir.jpa.starter.annotations.OnDSTU3Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional({OnDSTU3Condition.class, CqlConfigCondition.class})
@Import({CqlDstu3Config.class})
public class StarterCqlDstu3Config {
}
