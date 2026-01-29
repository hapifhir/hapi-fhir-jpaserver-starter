package ca.uhn.fhir.jpa.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import ca.uhn.fhir.jpa.starter.common.TestContainerHelper;
import ca.uhn.fhir.jpa.starter.elastic.ElasticsearchBootSvcImpl;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-elasticsearch-lastn.yaml")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
class ElasticsearchLastNR4IT {
  private IGenericClient ourClient;

  @Container
  private static final ElasticsearchContainer ELASTICSEARCH = TestContainerHelper.newElasticsearchContainer()
    // Set index defaults to handle HAPI FHIR's MAX_SUBSCRIPTION_RESULTS (50000)
    .withEnv("indices.query.bool.max_clause_count", "50000");

  @DynamicPropertySource
  static void registerElasticsearchProperties(DynamicPropertyRegistry registry) {
    TestContainerHelper.registerElasticsearchProperties(registry, ELASTICSEARCH);
    // Also register spring.elasticsearch.uris for ElasticConfigCondition to enable ElasticsearchBootSvcImpl
    registry.add("spring.elasticsearch.uris", () -> TestContainerHelper.getElasticsearchHttpUrl(ELASTICSEARCH));
  }

  @Autowired
  private ElasticsearchBootSvcImpl myElasticsearchSvc;

  @LocalServerPort
  private int port;

  @Test
  void testLastN() throws IOException, InterruptedException {
    Thread.sleep(2000);

    Patient pt = new Patient();
    pt.addName().setFamily("Lastn").addGiven("Arthur");
    IIdType id = ourClient.create().resource(pt).execute().getId().toUnqualifiedVersionless();

    Observation obs = new Observation();
    obs.getSubject().setReferenceElement(id);
    String observationCode = "testobservationcode";

    obs.getCode().addCoding().setCode(observationCode).setSystem("http://testobservationcodesystem");
    obs.setValue(new StringType(observationCode));

    Date effectiveDtm = new GregorianCalendar().getTime();
    obs.setEffective(new DateTimeType(effectiveDtm));
    obs.getCategoryFirstRep().addCoding().setCode("testcategorycode").setSystem("http://testcategorycodesystem");
    IIdType obsId = ourClient.create().resource(obs).execute().getId().toUnqualifiedVersionless();

    myElasticsearchSvc.refreshIndex(ElasticsearchSvcImpl.OBSERVATION_INDEX);
	  Thread.sleep(2000);

    Parameters output = ourClient.operation().onType(Observation.class).named("lastn")
      .withParameter(Parameters.class, "max", new IntegerType(1))
      .andParameter("subject", new StringType("Patient/" + id.getIdPart()))
      .execute();
    Bundle b = (Bundle) output.getParameter().get(0).getResource();
    assertEquals(1, b.getTotal());
    assertEquals(obsId, b.getEntry().get(0).getResource().getIdElement().toUnqualifiedVersionless());
  }

  @BeforeEach
  void beforeEach() {
    FhirContext ctx = FhirContext.forR4();
    ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
    ctx.getRestfulClientFactory().setSocketTimeout((int) Duration.ofMinutes(20).toMillis());
    ourClient = ctx.newRestfulGenericClient("http://localhost:" + port + "/fhir/");
    ourClient.registerInterceptor(new LoggingInterceptor(true));
  }
}
