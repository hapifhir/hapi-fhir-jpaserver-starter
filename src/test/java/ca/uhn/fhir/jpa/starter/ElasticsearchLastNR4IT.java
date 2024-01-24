package ca.uhn.fhir.jpa.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.search.lastn.ElasticsearchRestClientFactory;
import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import ca.uhn.fhir.jpa.test.config.TestElasticsearchContainerHelper;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import java.io.IOException;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.annotation.PreDestroy;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.common.settings.Settings;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, JpaStarterWebsocketDispatcherConfig.class}, properties =
  {
    "spring.datasource.url=jdbc:h2:mem:dbr4",
    "hapi.fhir.fhir_version=r4",
    "hapi.fhir.lastn_enabled=true",
	 "hapi.fhir.store_resource_in_lucene_index_enabled=true",
	 "hapi.fhir.advanced_lucene_indexing=true",

    "elasticsearch.enabled=true",
	  "hapi.fhir.cr_enabled=false",
    // Because the port is set randomly, we will set the rest_url using the Initializer.
    // "elasticsearch.rest_url='http://localhost:9200'",
    "elasticsearch.username=SomeUsername",
    "elasticsearch.password=SomePassword",
    "elasticsearch.debug.refresh_after_write=true",
	 "elasticsearch.protocol=http",
	  "spring.main.allow-bean-definition-overriding=true",
	  "spring.jpa.properties.hibernate.search.enabled=true",
	  "spring.jpa.properties.hibernate.search.backend.type=elasticsearch",
	  "spring.jpa.properties.hibernate.search.backend.analysis.configurer=ca.uhn.fhir.jpa.search.elastic.HapiElasticsearchAnalysisConfigurer"
  })
@ContextConfiguration(initializers = ElasticsearchLastNR4IT.Initializer.class)
public class ElasticsearchLastNR4IT {

	private IGenericClient ourClient;
  private FhirContext ourCtx;

	@Container
	public static ElasticsearchContainer embeddedElastic = TestElasticsearchContainerHelper.getEmbeddedElasticSearch();

  @Autowired
  private ElasticsearchSvcImpl myElasticsearchSvc;

  @BeforeAll
  public static void beforeClass() throws IOException {
	  //Given
	  RestHighLevelClient elasticsearchHighLevelRestClient = ElasticsearchRestClientFactory.createElasticsearchHighLevelRestClient(
		  "http", embeddedElastic.getHost() + ":" + embeddedElastic.getMappedPort(9200), "", "");

	  /* As of 2023-08-10, HAPI FHIR sets SubscriptionConstants.MAX_SUBSCRIPTION_RESULTS to 50000
	  		which is in excess of elastic's default max_result_window. If MAX_SUBSCRIPTION_RESULTS is changed
	  		to a value <= 10000, the following will no longer be necessary. - dotasek
	  */
	  PutIndexTemplateRequest putIndexTemplateRequest = new PutIndexTemplateRequest("hapi_fhir_template");
	  putIndexTemplateRequest.patterns(List.of("*"));
	  Settings settings = Settings.builder().put("index.max_result_window", 50000).build();
	  putIndexTemplateRequest.settings(settings);
	  elasticsearchHighLevelRestClient.indices().putTemplate(putIndexTemplateRequest, RequestOptions.DEFAULT);


  }

  @PreDestroy
  public void stop() {
    embeddedElastic.stop();
  }

  @LocalServerPort
  private int port;

  //@Test
  void testLastN() throws IOException, InterruptedException {
	  Thread.sleep(2000);

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

    Parameters output = ourClient.operation().onType(Observation.class).named("lastn")
      .withParameter(Parameters.class, "max", new IntegerType(1))
      .andParameter("subject", new StringType("Patient/" + id.getIdPart()))
      .execute();
    Bundle b = (Bundle) output.getParameter().get(0).getResource();
    assertEquals(1, b.getTotal());
    assertEquals(obsId, b.getEntry().get(0).getResource().getIdElement().toUnqualifiedVersionless());
  }

  @BeforeEach
  void beforeEach() throws IOException {

    ourCtx = FhirContext.forR4();
    ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
    ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
    String ourServerBase = "http://localhost:" + port + "/fhir/";
    ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
    ourClient.registerInterceptor(new LoggingInterceptor(true));

  }

  static class Initializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(
      ConfigurableApplicationContext configurableApplicationContext) {
      // Since the port is dynamically generated, replace the URL with one that has the correct port
      TestPropertyValues.of("elasticsearch.rest_url=" + embeddedElastic.getHost() +":" + embeddedElastic.getMappedPort(9200))
        .applyTo(configurableApplicationContext.getEnvironment());
    }

  }
}
