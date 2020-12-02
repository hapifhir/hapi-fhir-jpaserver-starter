package ca.uhn.fhir.jpa.starter.cql;

import ca.uhn.fhir.cql.config.CqlR4Config;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

@Conditional({CqlConfigCondition.class})
@Import({CqlR4Config.class})
public class CqlConfigR4 {
}
