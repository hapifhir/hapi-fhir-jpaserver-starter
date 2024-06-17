package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.model.config.SubscriptionSettings;
import org.hl7.fhir.dstu2.model.Subscription;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("subscriptionSettingsTest")
@Configuration
public class SubscriptionSettingsConfig {
	@Primary
	@Bean
	public SubscriptionSettings subscriptionSettings() {
		SubscriptionSettings retVal = new SubscriptionSettings();

		retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.WEBSOCKET);
		retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.MESSAGE);
		return retVal;
	}
}
