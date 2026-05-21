package ca.uhn.fhir.jpa.starter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppPropertiesCorsDefaultsTest {

	@Test
	void defaultCorsHeadersIncludeFhirOptimisticLockingHeaders() {
		AppProperties.Cors cors = new AppProperties.Cors();

		assertFalse(cors.getAllow_Credentials());
		assertTrue(cors.getAllowed_headers().contains("If-Match"));
		assertTrue(cors.getExposed_headers().contains("ETag"));
	}

	@Test
	void nullCorsListsFallBackToDefaults() {
		AppProperties.Cors cors = new AppProperties.Cors();
		cors.setAllowed_headers(null);
		cors.setExposed_headers(null);
		cors.setAllowed_methods(null);

		assertTrue(cors.getAllowed_headers().contains("If-Match"));
		assertTrue(cors.getExposed_headers().contains("ETag"));
		assertTrue(cors.getAllowed_methods().contains("PATCH"));
	}
}
