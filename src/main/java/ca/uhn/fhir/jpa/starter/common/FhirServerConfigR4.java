package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.jpa.config.r4.JpaR4Config;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.jpa.starter.common.validation.OnRemoteTerminologyPresent;
import ca.uhn.fhir.jpa.starter.cr.StarterCrR4Config;
import ca.uhn.fhir.jpa.starter.ips.StarterIpsConfig;
import org.hl7.fhir.common.hapi.validation.support.RemoteTerminologyServiceValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(OnR4Condition.class)
@Import({
	JpaR4Config.class,
	StarterJpaConfig.class,
	StarterCrR4Config.class,
	ElasticsearchConfig.class,
	StarterIpsConfig.class
})
public class FhirServerConfigR4 {

	@Bean(name = "myHybridRemoteValidationSupportChain")
	@Conditional({OnR4Condition.class, OnRemoteTerminologyPresent.class})
	public IValidationSupport addRemoteValidation(
			ValidationSupportChain theValidationSupport, FhirContext theFhirContext, AppProperties theAppProperties) {
		var values = theAppProperties.getRemoteTerminologyServicesMap().values();

		// If the remote terminology service is "*" and is the only one then forward all requests to the remote
		// terminology service
		if (values.size() == 1 && "*".equalsIgnoreCase(values.iterator().next().getSystem())) {
			var remoteSystem = values.iterator().next();
			theValidationSupport.addValidationSupport(
					0, new RemoteTerminologyServiceValidationSupport(theFhirContext, remoteSystem.getUrl()));
			return theValidationSupport;

			// If there are multiple remote terminology services, then add each one to the validation chain
		} else {
			values.forEach((remoteSystem) -> theValidationSupport.addValidationSupport(
					0, new RemoteTerminologyServiceValidationSupport(theFhirContext, remoteSystem.getUrl()) {
						@Override
						public boolean isCodeSystemSupported(
								ValidationSupportContext theValidationSupportContext, String theSystem) {
							return remoteSystem.getSystem().equalsIgnoreCase(theSystem);
						}

						@Override
						public CodeValidationResult validateCode(
								ValidationSupportContext theValidationSupportContext,
								ConceptValidationOptions theOptions,
								String theCodeSystem,
								String theCode,
								String theDisplay,
								String theValueSetUrl) {
							if (remoteSystem.getSystem().equalsIgnoreCase(theCodeSystem)) {
								return super.validateCode(
										theValidationSupportContext,
										theOptions,
										theCodeSystem,
										theCode,
										theDisplay,
										theValueSetUrl);
							}
							return null;
						}
					}));
		}
		return theValidationSupport;
	}
}
