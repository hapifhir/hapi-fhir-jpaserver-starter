package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.common.TestContainerHelper;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-postgres-lucene.yaml")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
class PostgresLucenePatientIT {

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = TestContainerHelper.newPostgresContainer();

  @DynamicPropertySource
  static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
    TestContainerHelper.registerPostgresProperties(registry, POSTGRES);
  }

  @LocalServerPort
  private int port;

  private IGenericClient ourClient;

  @BeforeEach
  void beforeEach() {
    FhirContext ctx = FhirContext.forR4();
    ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
    ctx.getRestfulClientFactory().setSocketTimeout((int) Duration.ofMinutes(20).toMillis());
    ourClient = ctx.newRestfulGenericClient("http://localhost:" + port + "/fhir/");
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
