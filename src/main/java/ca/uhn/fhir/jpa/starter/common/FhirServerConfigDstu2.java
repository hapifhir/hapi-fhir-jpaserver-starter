package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.config.JpaDstu2Config;
import ca.uhn.fhir.jpa.starter.annotations.OnDSTU2Condition;
import ca.uhn.fhir.jpa.term.TermLoaderSvcImpl;
import ca.uhn.fhir.jpa.term.api.ITermCodeSystemStorageSvc;
import ca.uhn.fhir.jpa.term.api.ITermDeferredStorageSvc;
import ca.uhn.fhir.jpa.term.api.ITermLoaderSvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(OnDSTU2Condition.class)
@Import({
	JpaDstu2Config.class,
	StarterJpaConfig.class
})
public class FhirServerConfigDstu2 {
	@Bean
	public ITermLoaderSvc termLoaderService(ITermDeferredStorageSvc theDeferredStorageSvc, ITermCodeSystemStorageSvc theCodeSystemStorageSvc) {
		return new TermLoaderSvcImpl(theDeferredStorageSvc, theCodeSystemStorageSvc);
	}

}
