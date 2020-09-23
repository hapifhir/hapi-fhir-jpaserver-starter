package ca.uhn.fhir.jpa.empi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.empi.api.IEmpiSettings;
import ca.uhn.fhir.empi.rules.config.EmpiRuleValidator;
import ca.uhn.fhir.empi.rules.config.EmpiSettings;
import ca.uhn.fhir.jpa.empi.config.EmpiConsumerConfig;
import ca.uhn.fhir.jpa.empi.config.EmpiSubmitterConfig;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.server.util.ISearchParamRetriever;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * TODO: Move this to package "ca.uhn.fhir.jpa.starter" in HAPI FHIR 5.2.0+. The lousy component scan
 * in 5.1.0 picks this up even if EMPI is disabled currently.
 */
@Configuration
@Conditional(EmpiConfigCondition.class)
@Import({EmpiConsumerConfig.class, EmpiSubmitterConfig.class})
public class EmpiConfig {

  @Bean
  EmpiRuleValidator empiRuleValidator(FhirContext theFhirContext, ISearchParamRetriever theSearchParamRetriever) {
    return new EmpiRuleValidator(theFhirContext, theSearchParamRetriever);
  }

  @Bean
  IEmpiSettings empiSettings(@Autowired EmpiRuleValidator theEmpiRuleValidator, AppProperties appProperties) throws IOException {
    DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource("empi-rules.json");
    String json = IOUtils.toString(resource.getInputStream(), Charsets.UTF_8);
    return new EmpiSettings(theEmpiRuleValidator).setEnabled(appProperties.getEmpi_enabled()).setScriptText(json);
  }

}
