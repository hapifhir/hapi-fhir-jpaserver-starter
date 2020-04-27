package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.empi.api.IEmpiSettings;
import ca.uhn.fhir.empi.rules.config.EmpiSettingsImpl;
import ca.uhn.fhir.jpa.empi.config.EmpiConsumerConfig;
import ca.uhn.fhir.jpa.empi.config.EmpiSubmitterConfig;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
@Import({EmpiSubmitterConfig.class, EmpiConsumerConfig.class})
public class EmpiConfig {
  @Bean
  IEmpiSettings empiProperties() throws IOException {
    DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource("empi-rules.json");
    String json = IOUtils.toString(resource.getInputStream(), Charsets.UTF_8);
    return new EmpiSettingsImpl().setEnabled(HapiProperties.getEmpiEnabled()).setScriptText(json);
  }
}
