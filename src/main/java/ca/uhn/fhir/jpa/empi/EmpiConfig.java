package ca.uhn.fhir.jpa.empi;

import ca.uhn.fhir.empi.api.IEmpiSettings;
import ca.uhn.fhir.empi.rules.config.EmpiRuleValidator;
import ca.uhn.fhir.empi.rules.config.EmpiSettings;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * TODO: Move this to package "ca.uhn.fhir.jpa.starter" in HAPI FHIR 5.2.0+. The lousy component scan
 * in 5.1.0 picks this up even if EMPI is disabled currently.
 */
@Configuration
public class EmpiConfig {

  @Bean
  IEmpiSettings empiSettings(EmpiRuleValidator theEmpiRuleValidator) throws IOException {
    DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource("empi-rules.json");
    String json = IOUtils.toString(resource.getInputStream(), Charsets.UTF_8);
    return new EmpiSettings(theEmpiRuleValidator).setEnabled(HapiProperties.getEmpiEnabled()).setScriptText(json);
  }

}
