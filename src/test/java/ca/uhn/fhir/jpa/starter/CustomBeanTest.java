package ca.uhn.fhir.jpa.starter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, JpaStarterWebsocketDispatcherConfig.class}, properties = {
		"hapi.fhir.custom-bean-packages=some.custom.pkg1,some.custom.pkg2",
		"spring.datasource.url=jdbc:h2:mem:dbr4",
		"hapi.fhir.enable_repository_validating_interceptor=true",
		"hapi.fhir.fhir_version=r4",
		"hapi.fhir.mdm_enabled=false",
		"hapi.fhir.cr_enabled=false",
		"hapi.fhir.subscription.websocket_enabled=false",
		"spring.main.allow-bean-definition-overriding=true"
})
class CustomBeanTest {

	@Autowired
	some.custom.pkg1.CustomBean customBean1;

	@Test
	void testCustomBeanExists() {
		Assertions.assertNotNull(customBean1);
		Assertions.assertEquals("I am alive", customBean1.getInitFlag());
	}
}
