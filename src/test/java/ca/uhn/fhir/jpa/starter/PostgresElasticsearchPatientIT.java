package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-postgres-elasticsearch.yaml")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
class PostgresElasticsearchPatientIT {

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("hapi")
    .withUsername("fhiruser")
    .withPassword("fhirpass");

  @Container
  private static final ElasticsearchContainer ELASTICSEARCH = new ElasticsearchContainer(
    "docker.elastic.co/elasticsearch/elasticsearch:8.11.0"
  )
    .withEnv("xpack.security.enabled", "false")
    .withEnv("discovery.type", "single-node")
    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

  @DynamicPropertySource
  static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
    // PostgreSQL configuration
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    registry.add("spring.jpa.properties.hibernate.dialect", () -> "ca.uhn.fhir.jpa.model.dialect.HapiFhirPostgresDialect");

    // Elasticsearch configuration
    registry.add("spring.jpa.properties.hibernate.search.backend.hosts", ELASTICSEARCH::getHttpHostAddress);
    registry.add("spring.jpa.properties.hibernate.search.backend.protocol", () -> "http");
    registry.add("spring.jpa.properties.hibernate.search.backend.username", () -> "");
    registry.add("spring.jpa.properties.hibernate.search.backend.password", () -> "");
  }

  @LocalServerPort
  private int port;

  private IGenericClient ourClient;
  private FhirContext ourCtx;

  @BeforeEach
  void beforeEach() {
    ourCtx = FhirContext.forR4();
    ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
    ourCtx.getRestfulClientFactory().setSocketTimeout((int) Duration.ofMinutes(20).toMillis());
    String ourServerBase = "http://localhost:" + port + "/fhir/";
    ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
    ourClient.registerInterceptor(new LoggingInterceptor(true));
  }

  @Test
  void testCreateAndSearchPatientByFamilyName() {
    String givenName = "Jane";
    String familyName = "Smith Doe";

    Patient patient = new Patient();
    patient.addName().setFamily(familyName).addGiven(givenName);
    IIdType id = ourClient.create().resource(patient).execute().getId().toUnqualifiedVersionless();
    assertNotNull(id);

    await().atMost(Duration.ofSeconds(30)).until(() -> searchByFamily(familyName).getTotal() == 1);

    Bundle results = searchByFamily(familyName);
    assertEquals(1, results.getTotal());
    Patient found = (Patient) results.getEntry().get(0).getResource();
    assertEquals(familyName, found.getNameFirstRep().getFamily());
    assertEquals(id, found.getIdElement().toUnqualifiedVersionless());
  }

  private Bundle searchByFamily(String family) {
    return ourClient
      .search()
      .forResource(Patient.class)
      .where(Patient.FAMILY.matches().value(family))
      .returnBundle(Bundle.class)
      .execute();
  }
}
