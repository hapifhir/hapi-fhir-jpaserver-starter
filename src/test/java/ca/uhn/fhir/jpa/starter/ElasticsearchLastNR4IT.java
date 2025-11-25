package ca.uhn.fhir.jpa.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import ca.uhn.fhir.jpa.starter.elastic.ElasticsearchBootSvcImpl;
import ca.uhn.fhir.jpa.test.config.TestElasticsearchContainerHelper;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.cluster.PutClusterSettingsResponse;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
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
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@Testcontainers
@ActiveProfiles("test,elastic")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class}, properties = {
	"spring.datasource.url=jdbc:h2:mem:dbr4",

	// Override the default exclude configuration for the Elasticsearch client.
	"spring.autoconfigure.exclude=",

	"hapi.fhir.fhir_version=r4",

	// Because the port is set randomly, we will set the rest_url using the Initializer.
	// "elasticsearch.rest_url='http://localhost:9200'",
})
@ContextConfiguration(initializers = ElasticsearchLastNR4IT.Initializer.class)
class ElasticsearchLastNR4IT {
	private IGenericClient ourClient;
	private FhirContext ourCtx;

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
		registry.add("spring.datasource.password", embeddedElastic::getHttpHostAddress);
	}


	@Autowired
	ConfigurableApplicationContext configurableApplicationContext;

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

		String elasticHost = embeddedElastic.getHost();
		int elasticPort = embeddedElastic.getMappedPort(9200);
		TestPropertyValues.of("spring.elasticsearch.uris=" + elasticHost + ":" + elasticPort).applyTo(configurableApplicationContext.getEnvironment());
		ourCtx = FhirContext.forR4();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		ourClient.registerInterceptor(new LoggingInterceptor(true));

		//buildElasticRestClient(configurableApplicationContext.getEnvironment());
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

			//ElasticsearchClient client = buildElasticRestClient(configurableApplicationContext.getEnvironment());

			/*try {
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
			}*/
		}
	}
/*
	static ElasticsearchClient buildElasticRestClient(Environment environment) throws IllegalStateException {
		String uri = environment.getProperty("spring.elasticsearch.uris");
		if (!StringUtils.hasText(uri)) {
			throw new IllegalStateException("spring.elasticsearch.uris must be set for tests");
		}

		HttpHost host = HttpHost.create(uri);
		RestClientBuilder builder = RestClient.builder(host);

		String username = environment.getProperty("spring.elasticsearch.username");
		String password = environment.getProperty("spring.elasticsearch.password");
		if (StringUtils.hasText(username)) {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
		}

		return new ElasticsearchClient(new RestClientTransport(builder.build(), new JacksonJsonpMapper()));
	}*/
}
