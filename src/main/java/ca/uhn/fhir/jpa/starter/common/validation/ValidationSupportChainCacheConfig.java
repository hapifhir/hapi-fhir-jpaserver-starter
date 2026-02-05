package ca.uhn.fhir.jpa.starter.common.validation;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationSupportChainCacheConfig {

	@Bean
	public BeanPostProcessor validationSupportChainCacheConfigurationPostProcessor(AppProperties theAppProperties) {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) {
				if (!Boolean.TRUE.equals(theAppProperties.getValidation().getSupport_chain_cache_enabled())
						&& bean instanceof ValidationSupportChain.CacheConfiguration
						&& "validationSupportChainCacheConfiguration".equals(beanName)) {
					return ValidationSupportChain.CacheConfiguration.disabled();
				}
				return bean;
			}
		};
	}
}
