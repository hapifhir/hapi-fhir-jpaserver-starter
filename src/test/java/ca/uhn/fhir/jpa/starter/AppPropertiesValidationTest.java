package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.support.IValidationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AppPropertiesValidationTest {

	@Test
	void unknownCodeSystemSeverityIsUnsetByDefault() {
		AppProperties.Validation validation = new AppProperties.Validation();

		assertThat(validation.getUnknown_code_system_severity()).isNull();
	}

	@Test
	void unknownCodeSystemSeverityBindsFromConfiguration() {
		AppProperties appProperties = new Binder(new MapConfigurationPropertySource(Map.of(
				"hapi.fhir.validation.unknown_code_system_severity", "WARNING")))
				.bind("hapi.fhir", AppProperties.class)
				.get();

		assertThat(appProperties.getValidation().getUnknown_code_system_severity())
				.isEqualTo(IValidationSupport.IssueSeverity.WARNING);
	}
}
