package ca.uhn.fhir.jpa.starter;

import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, JpaStarterWebsocketDispatcherConfig.class}, properties = {
		"hapi.fhir.custom-bean-packages=some.custom.pkg1",
		"hapi.fhir.custom-interceptor-classes=some.custom.pkg1.CustomInterceptorBean,some.custom.pkg1.CustomInterceptorPojo",
		"spring.datasource.url=jdbc:h2:mem:dbr4",
		// "hapi.fhir.enable_repository_validating_interceptor=true",
		"hapi.fhir.fhir_version=r4"
})

class CustomInterceptorTest {

	@LocalServerPort
	private int port;

	@Autowired
	private IFhirResourceDao<Patient> patientResourceDao;

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
	void testAuditInterceptors() {

		// we registered two custom interceptors via the property 'hapi.fhir.custom-interceptor-classes'
		// one is discovered as a Spring Bean, one instantiated via reflection
		// both should be registered with the server and will add a custom extension to any Patient resource created
		// so we can verify they were registered

		Patient pat = new Patient();
		String patId = client.create().resource(pat).execute().getId().getIdPart();

		Patient readPat = client.read().resource(Patient.class).withId(patId).execute();

		Assertions.assertNotNull(readPat.getExtensionByUrl("http://some.custom.pkg1/CustomInterceptorBean"));
		Assertions.assertNotNull(readPat.getExtensionByUrl("http://some.custom.pkg1/CustomInterceptorPojo"));

	}
}
