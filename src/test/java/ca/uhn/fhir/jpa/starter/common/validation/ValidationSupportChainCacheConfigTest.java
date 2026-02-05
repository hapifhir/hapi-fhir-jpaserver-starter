package ca.uhn.fhir.jpa.starter.common.validation;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ValidationSupportChainCacheConfigTest {

	@Test
	void disablesCacheConfigurationWhenPropertyIsFalse() {
		AppProperties properties = new AppProperties();
		properties.getValidation().setSupport_chain_cache_enabled(false);

		ValidationSupportChainCacheConfig config = new ValidationSupportChainCacheConfig();
		BeanPostProcessor postProcessor =
				config.validationSupportChainCacheConfigurationPostProcessor(properties);

		ValidationSupportChain.CacheConfiguration input = ValidationSupportChain.CacheConfiguration.defaultValues();
		Object result = postProcessor.postProcessAfterInitialization(
				input, "validationSupportChainCacheConfiguration");

		ValidationSupportChain.CacheConfiguration disabled =
				ValidationSupportChain.CacheConfiguration.disabled();
		ValidationSupportChain.CacheConfiguration actual = (ValidationSupportChain.CacheConfiguration) result;

		assertEquals(disabled.getCacheSize(), actual.getCacheSize());
		assertEquals(disabled.getCacheTimeout(), actual.getCacheTimeout());
	}

	@Test
	void keepsCacheConfigurationWhenPropertyIsTrue() {
		AppProperties properties = new AppProperties();
		properties.getValidation().setSupport_chain_cache_enabled(true);

		ValidationSupportChainCacheConfig config = new ValidationSupportChainCacheConfig();
		BeanPostProcessor postProcessor =
				config.validationSupportChainCacheConfigurationPostProcessor(properties);

		ValidationSupportChain.CacheConfiguration input = ValidationSupportChain.CacheConfiguration.defaultValues();
		Object result = postProcessor.postProcessAfterInitialization(
				input, "validationSupportChainCacheConfiguration");

		assertSame(input, result);
	}
}
