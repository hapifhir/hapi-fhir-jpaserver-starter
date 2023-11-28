package ca.uhn.fhir.jpa.starter.ips;

import org.springframework.context.annotation.Bean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.ips.api.IIpsGenerationStrategy;
import ca.uhn.fhir.jpa.ips.strategy.DefaultIpsGenerationStrategy;
import ca.uhn.fhir.jpa.ips.generator.IIpsGeneratorSvc;
import org.springframework.context.annotation.Conditional;
import ca.uhn.fhir.jpa.ips.provider.IpsOperationProvider;
import ca.uhn.fhir.jpa.ips.generator.IpsGeneratorSvcImpl;


@Conditional(IpsConfigCondition.class)
public class StarterIpsConfig {
	@Bean 
	IIpsGenerationStrategy ipsGenerationStrategy()
	{
		return new DefaultIpsGenerationStrategy();
	}

	@Bean
	public IpsOperationProvider ipsOperationProvider(IIpsGeneratorSvc theIpsGeneratorSvc){
		return new IpsOperationProvider(theIpsGeneratorSvc);
	}

	@Bean
	public IIpsGeneratorSvc ipsGeneratorSvcImpl(FhirContext theFhirContext, IIpsGenerationStrategy theGenerationStrategy, DaoRegistry theDaoRegistry)
	{
		return new IpsGeneratorSvcImpl(theFhirContext, theGenerationStrategy, theDaoRegistry);
	}
	
}
