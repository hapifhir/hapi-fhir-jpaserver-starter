package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.model.config.SubscriptionSettings;
import ca.uhn.fhir.jpa.starter.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FhirServerConfigCommonSubscriptionSettingsTest {

	private final FhirServerConfigCommon myConfig = new FhirServerConfigCommon(new AppProperties());

	@Test
	void subscriptionSettingsDefaultToCrossPartitionDisabled() {
		SubscriptionSettings settings = myConfig.subscriptionSettings(new AppProperties());

		assertThat(settings.isCrossPartitionSubscriptionEnabled()).isFalse();
	}

	@Test
	void subscriptionSettingsEnableCrossPartitionFromProperties() {
		AppProperties appProperties = bindAppProperties(Map.of(
				"hapi.fhir.subscription.cross_partition_enabled", "true"));

		SubscriptionSettings settings = myConfig.subscriptionSettings(appProperties);

		assertThat(settings.isCrossPartitionSubscriptionEnabled()).isTrue();
	}

	@Test
	void subscriptionSettingsKeepCrossPartitionDisabledWhenPropertyIsFalse() {
		AppProperties appProperties = bindAppProperties(Map.of(
				"hapi.fhir.subscription.cross_partition_enabled", "false"));

		SubscriptionSettings settings = myConfig.subscriptionSettings(appProperties);

		assertThat(settings.isCrossPartitionSubscriptionEnabled()).isFalse();
	}

	private AppProperties bindAppProperties(Map<String, String> properties) {
		return new Binder(new MapConfigurationPropertySource(properties))
				.bind("hapi.fhir", AppProperties.class)
				.get();
	}
}
