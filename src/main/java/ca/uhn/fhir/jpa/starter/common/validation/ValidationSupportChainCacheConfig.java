package ca.uhn.fhir.jpa.starter.common.validation;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Disables the ValidationSupportChain cache when configured via
 * {@code hapi.fhir.validation.support_chain_cache_enabled}.
 * Use this when remote terminology responses must always be fresh or when
 * troubleshooting cache-related behavior.
 */
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
