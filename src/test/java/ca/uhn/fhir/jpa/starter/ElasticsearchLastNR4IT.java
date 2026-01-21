package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import ca.uhn.fhir.jpa.starter.elastic.ElasticsearchBootSvcImpl;
import ca.uhn.fhir.jpa.test.config.TestElasticsearchContainerHelper;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import jakarta.annotation.PreDestroy;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@Testcontainers
@ActiveProfiles({"test", "elastic"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class}, properties = {"spring.datasource.url=jdbc:h2:mem:dbr4",
	// Override the default exclude configuration for the Elasticsearch client.
	"spring.autoconfigure.exclude=", "hapi.fhir.fhir_version=r4"
})
class ElasticsearchLastNR4IT {
	private IGenericClient ourClient;

	@Container
	public static ElasticsearchContainer embeddedElastic = TestElasticsearchContainerHelper.getEmbeddedElasticSearch();

	@Autowired
	private ElasticsearchBootSvcImpl myElasticsearchSvc;

	@PreDestroy
	public void stop() {
		embeddedElastic.stop();
	}

	@LocalServerPort
	private int port;

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {

		registry.add("spring.elasticsearch.uris", () -> "http://" + embeddedElastic.getHttpHostAddress());

		registry.add("spring.jpa.properties.hibernate.search.backend.hosts", embeddedElastic::getHttpHostAddress);
		registry.add("spring.jpa.properties.hibernate.search.backend.protocol", () -> "http");
		registry.add("spring.jpa.properties.hibernate.search.backend.username", () -> "");
		registry.add("spring.jpa.properties.hibernate.search.backend.password", () -> "");
	}

	@Test
	void testLastN() throws IOException, InterruptedException {
		Patient pt = new Patient();
		pt.addName().setFamily("Lastn").addGiven("Arthur");
		IIdType id = ourClient.create().resource(pt).execute().getId().toUnqualifiedVersionless();

		Observation obs = new Observation();
		obs.getSubject().setReferenceElement(id);
		String observationCode = "testobservationcode";
		String codeSystem = "http://testobservationcodesystem";

		obs.getCode().addCoding().setCode(observationCode).setSystem(codeSystem);
		obs.setValue(new StringType(observationCode));

		Date effectiveDtm = new GregorianCalendar().getTime();
		obs.setEffective(new DateTimeType(effectiveDtm));
		obs.getCategoryFirstRep().addCoding().setCode("testcategorycode").setSystem("http://testcategorycodesystem");
		IIdType obsId = ourClient.create().resource(obs).execute().getId().toUnqualifiedVersionless();

		myElasticsearchSvc.refreshIndex(ElasticsearchSvcImpl.OBSERVATION_INDEX);

		Thread.sleep(2000);
		Parameters output = ourClient.operation().onType(Observation.class).named("lastn").withParameter(Parameters.class, "max", new IntegerType(1)).andParameter("subject", new StringType("Patient/" + id.getIdPart())).execute();
		Bundle b = (Bundle) output.getParameter().get(0).getResource();
		assertEquals(1, b.getTotal());
		assertEquals(obsId, b.getEntry().get(0).getResource().getIdElement().toUnqualifiedVersionless());
	}

	@BeforeEach
	void beforeEach() {

		FhirContext ourCtx = FhirContext.forR4();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		ourClient.registerInterceptor(new LoggingInterceptor(true));

	}
}
