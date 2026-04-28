package ca.uhn.fhir.jpa.starter.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link WebAppFilesConfigurer}.
 * Validates that the WEB_CONTENT constant has a leading slash
 * so that resource handler patterns work correctly.
 */
class WebAppFilesConfigurerTest {

	@Test
	void testWebContentHasLeadingSlash() {
		// The WEB_CONTENT constant must start with "/" for Spring's resource handler
		// to properly match request paths like "/web/app/index.html"
		assertTrue(WebAppFilesConfigurer.WEB_CONTENT.startsWith("/"),
			"WEB_CONTENT must start with '/' for proper resource handler pattern matching");
		assertEquals("/web/apps", WebAppFilesConfigurer.WEB_CONTENT);
	}

	@Test
	void testCustomContentHasLeadingSlash() {
		// Also verify the CUSTOM_CONTENT constant for completeness
		assertTrue(CustomContentFilesConfigurer.CUSTOM_CONTENT.startsWith("/"),
			"CUSTOM_CONTENT must start with '/' for proper resource handler pattern matching");
		assertEquals("/content/custom", CustomContentFilesConfigurer.CUSTOM_CONTENT);
	}
}
