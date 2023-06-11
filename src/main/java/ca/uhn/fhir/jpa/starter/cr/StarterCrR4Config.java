package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.cr.config.CrR4Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional({OnR4Condition.class, CrConfigCondition.class})
@Import({CrR4Config.class})
public class StarterCrR4Config {

}
