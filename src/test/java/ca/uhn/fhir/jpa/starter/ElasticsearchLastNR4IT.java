package ca.uhn.fhir.jpa.starter;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import ca.uhn.fhir.jpa.starter.elastic.ElasticsearchBootSvcImpl;
import ca.uhn.fhir.jpa.starter.elastic.TestElasticsearchClientConfig;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.cluster.PutClusterSettingsResponse;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateResponse;
import co.elastic.clients.json.JsonData;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.GregorianCalendar;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class}, properties = {"spring.datasource.url=jdbc:h2:mem:dbr4", "hapi.fhir.fhir_version=r4", "hapi.fhir.lastn_enabled=true", "hapi.fhir.store_resource_in_lucene_index_enabled=true", "hapi.fhir.advanced_lucene_indexing=true", "hapi.fhir.search_index_full_text_enabled=true", "hapi.fhir.cr_enabled=false",
	// Because the port is set randomly, we will set the rest_url using the Initializer.
	// "elasticsearch.rest_url='http://localhost:9200'",

	"spring.elasticsearch.uris=http://localhost:9200", "spring.elasticsearch.username=elastic", "spring.elasticsearch.password=changeme", "spring.main.allow-bean-definition-overriding=true", "spring.jpa.properties.hibernate.search.enabled=true", "spring.jpa.properties.hibernate.search.backend.type=elasticsearch", "spring.jpa.properties.hibernate.search.backend.hosts=localhost:9200", "spring.jpa.properties.hibernate.search.backend.protocol=http", "spring.jpa.properties.hibernate.search.backend.analysis.configurer=ca.uhn.fhir.jpa.search.HapiHSearchAnalysisConfigurers$HapiElasticsearchAnalysisConfigurer"})
@ContextConfiguration(initializers = ElasticsearchLastNR4IT.Initializer.class)
@Import(TestElasticsearchClientConfig.class)
class ElasticsearchLastNR4IT {
	private IGenericClient ourClient;
	private FhirContext ourCtx;

	@Container
	public static ElasticsearchContainer embeddedElastic = getEmbeddedElasticSearch();

	@Autowired
	private ElasticsearchBootSvcImpl myElasticsearchSvc;

	@PreDestroy
	public void stop() {
		embeddedElastic.stop();
	}

	@LocalServerPort
	private int port;

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

		Parameters output = ourClient.operation().onType(Observation.class).named("lastn").withParameter(Parameters.class, "max", new IntegerType(1)).andParameter("subject", new StringType("Patient/" + id.getIdPart())).execute();
		Bundle b = (Bundle) output.getParameter().get(0).getResource();
		assertEquals(1, b.getTotal());
		assertEquals(obsId, b.getEntry().get(0).getResource().getIdElement().toUnqualifiedVersionless());
	}

	@BeforeEach
	void beforeEach() {

		ourCtx = FhirContext.forR4();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		ourClient.registerInterceptor(new LoggingInterceptor(true));
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			if (!embeddedElastic.isRunning()) {
				embeddedElastic.start();
			}
			String elasticHost = embeddedElastic.getHost();
			int elasticPort = embeddedElastic.getMappedPort(9200);
			// Since the port is dynamically generated, replace the URL with one that has the correct port
			TestPropertyValues.of("spring.elasticsearch.uris=" + elasticHost + ":" + elasticPort).applyTo(configurableApplicationContext.getEnvironment());
			TestPropertyValues.of("spring.jpa.properties.hibernate.search.backend.hosts=" + elasticHost + ":" + elasticPort).applyTo(configurableApplicationContext.getEnvironment());

			try (ElasticsearchClient client = TestElasticsearchClientConfig.createElasticsearchClient(configurableApplicationContext.getEnvironment())) {

				try {
					PutClusterSettingsResponse clusterResponse = client.cluster().putSettings(s -> s.persistent("indices.max_ngram_diff", JsonData.of(17)));
					if (clusterResponse.acknowledged()) {
						return;
					}
				} catch (ElasticsearchException | IOException e) {
					// Fall through to index template approach when cluster setting is rejected
				}

				PutIndexTemplateResponse templateResponse;
				try {
					templateResponse = client.indices().putIndexTemplate(b -> b
						.name("hapi-max-ngram-diff")
						.indexPatterns("*")
						.priority(500L)
						.template(t -> t.settings(s -> s.maxNgramDiff(17))));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				if (!templateResponse.acknowledged()) {
					throw new IllegalStateException("Unable to set index.max_ngram_diff via cluster settings or index template");
				}
			}
	}


	public static final String ELASTICSEARCH_VERSION = "8.19.7";
	public static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:" + ELASTICSEARCH_VERSION;
	public static ElasticsearchContainer getEmbeddedElasticSearch() {

		return new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
			// the default is 4GB which is too much for our little tests
			.withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
			// turn off security warnings
			.withEnv("xpack.security.enabled", "false")
			// turn off machine learning (we don't need it in tests anyways)
			.withEnv("xpack.ml.enabled", "false")
			.withStartupTimeout(Duration.of(300, SECONDS));
	}
}
