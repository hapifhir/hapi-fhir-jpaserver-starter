package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.api.config.JpaStorageSettings;
import org.hl7.fhir.dstu2.model.Subscription;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("storageSettingsTest")
@Configuration
public class JpaStorageSettingsConfig {
	@Primary
	@Bean
	public JpaStorageSettings storageSettings() {
		JpaStorageSettings retVal = new JpaStorageSettings();

		retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.WEBSOCKET);
		retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.MESSAGE);
		return retVal;
	}
}
