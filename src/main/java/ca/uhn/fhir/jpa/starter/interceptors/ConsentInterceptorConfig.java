package ca.uhn.fhir.jpa.starter.interceptors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires {@link ConsentEnforcementInterceptor} as a Spring-managed bean and connects it to
 * {@link ConsentEnforcementService}.
 * <p>
 * This bean is picked up by {@code StarterJpaConfig#registerCustomInterceptors} via
 * {@code hapi.fhir.custom-interceptor-classes} (see application.yaml). Because it is resolved
 * from the Spring {@code ApplicationContext} (and not instantiated via reflection), the
 * {@link ConsentEnforcementService} dependency below is properly injected and registered
 * before the interceptor is attached to the {@code RestfulServer}.
 * </p>
 */
@Configuration
public class ConsentInterceptorConfig {

	@Bean
	public ConsentEnforcementInterceptor consentEnforcementInterceptor(
			ConsentEnforcementService theConsentEnforcementService) {
		ConsentEnforcementInterceptor interceptor = new ConsentEnforcementInterceptor();
		interceptor.registerConsentService(theConsentEnforcementService);
		return interceptor;
	}
}
