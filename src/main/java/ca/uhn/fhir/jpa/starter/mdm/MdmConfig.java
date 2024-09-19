package ca.uhn.fhir.jpa.starter.mdm;

import ca.uhn.fhir.jpa.mdm.config.MdmConsumerConfig;
import ca.uhn.fhir.jpa.mdm.config.MdmSubmitterConfig;
import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.mdm.api.IMdmSettings;
import ca.uhn.fhir.mdm.rules.config.MdmRuleValidator;
import ca.uhn.fhir.mdm.rules.config.MdmSettings;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Conditional(MdmConfigCondition.class)
@Import({MdmConsumerConfig.class, MdmSubmitterConfig.class, NicknameServiceConfig.class})
public class MdmConfig {

	@Bean
	IMdmSettings mdmSettings(@Autowired MdmRuleValidator theMdmRuleValidator, AppProperties appProperties)
			throws IOException {
		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = resourceLoader.getResource(appProperties.getMdm_rules_json_location());
		String json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
		return new MdmSettings(theMdmRuleValidator)
				.setEnabled(appProperties.getMdm_enabled())
				.setScriptText(json);
	}
}
