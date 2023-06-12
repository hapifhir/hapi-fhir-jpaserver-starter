package ca.uhn.fhir.jpa.starter.cr;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;

@Configuration
@Conditional(CrConfigCondition.class)
public class CrProviderConfig {

	@Bean
	CrOperationFactory crOperationFactory() {
		return new CrOperationFactory();
	}

	@Bean
	CrOperationLoader crOperationLoader(FhirContext theFhirContext, ResourceProviderFactory theResourceProviderFactory,
													CrOperationFactory theCrlProviderFactory) {
		return new CrOperationLoader(theFhirContext, theResourceProviderFactory, theCrlProviderFactory);
	}

}

