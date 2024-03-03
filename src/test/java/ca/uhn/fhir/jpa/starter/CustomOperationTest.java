package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class}, properties = {
		"hapi.fhir.custom-bean-packages=some.custom.pkg1",
		"hapi.fhir.custom-provider-classes=some.custom.pkg1.CustomOperationBean,some.custom.pkg1.CustomOperationPojo",
		"spring.datasource.url=jdbc:h2:mem:dbr4",
		"hapi.fhir.cr_enabled=false",
		// "hapi.fhir.enable_repository_validating_interceptor=true",
		"hapi.fhir.fhir_version=r4"
})

class CustomOperationTest {

	@LocalServerPort
	private int port;

	private IGenericClient client;
	private FhirContext ctx;

	@BeforeEach
	void setUp() {
		ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ctx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		client = ctx.newRestfulGenericClient(ourServerBase);

		// Properties props = new Properties();
		// props.put("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration");
	}

	@Test
	void testCustomOperations() {

		// we registered two custom operations via the property 'hapi.fhir.custom-provider-classes'
		// one is discovered as a Spring Bean ($springBeanOperation), one instantiated via reflection ($pojoOperation)
		// both should be registered with the server and will add a custom operation.

		// test Spring bean operation
		MethodOutcome springBeanOutcome = client.operation().onServer().named("$springBeanOperation")
			.withNoParameters(Parameters.class).returnMethodOutcome().execute();

		// the hapi client will return our operation result (just a string) as a Binary with the string stored as the
		// data
		Assertions.assertEquals(200, springBeanOutcome.getResponseStatusCode());
		Binary springReturnResource = (Binary) springBeanOutcome.getResource();
		String springReturn = new String(springReturnResource.getData());
		Assertions.assertEquals("springBean", springReturn);

		// test Pojo bean
		MethodOutcome pojoOutcome = client.operation().onServer().named("$pojoOperation")
			.withNoParameters(Parameters.class).returnMethodOutcome().execute();

		Assertions.assertEquals(200, pojoOutcome.getResponseStatusCode());
		Binary pojoReturnResource = (Binary) pojoOutcome.getResource();
		String pojoReturn = new String(pojoReturnResource.getData());
		Assertions.assertEquals("pojo", pojoReturn);
	}
}
