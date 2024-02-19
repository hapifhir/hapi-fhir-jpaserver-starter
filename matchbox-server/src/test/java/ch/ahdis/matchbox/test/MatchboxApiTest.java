package ch.ahdis.matchbox.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseHasExtensions;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.jpa.starter.Application;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = { Application.class })
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MatchboxApiTest {

  static public int getValidationFailures(OperationOutcome outcome) {
    int fails = 0;
    if (outcome != null && outcome.getIssue() != null) {
      for (OperationOutcomeIssueComponent issue : outcome.getIssue()) {
        if (IssueSeverity.FATAL == issue.getSeverity()) {
          ++fails;
        }
        if (IssueSeverity.ERROR == issue.getSeverity()) {
          ++fails;
        }
      }
    }
    return fails;
  }

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxApiTest.class);

  private String targetServer = "http://localhost:8081/matchboxv3/fhir";

  @BeforeAll
  void waitUntilStartup() throws Exception {
    Path dir = Paths.get("database");
    if (Files.exists(dir)) {
      for (Path file : Files.list(dir).collect(Collectors.toList())) {
        if (Files.isRegularFile(file)) {
          Files.delete(file);
        }
      }
    }
    Thread.sleep(10000); // give the server some time to start up
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    ValidationClient validationClient = new ValidationClient(contextR4, this.targetServer);
    validationClient.capabilities();
  }

  private static IBaseExtension<?, ?> getMatchboxValidationExtension(FhirContext theCtx,
      IBaseOperationOutcome theOutcome) {
    if (theOutcome == null) {
      return null;
    }
    RuntimeResourceDefinition ooDef = theCtx.getResourceDefinition(theOutcome);
    BaseRuntimeChildDefinition issueChild = ooDef.getChildByName("issue");
    List<IBase> issues = issueChild.getAccessor().getValues(theOutcome);
    if (issues.isEmpty()) {
      return null;
    }
    IBase issue = issues.get(0);
    if (issue instanceof IBaseHasExtensions) {
      List<? extends IBaseExtension<?, ?>> extensions = ((IBaseHasExtensions) issue).getExtension();
      for (IBaseExtension<?, ?> nextSource : extensions) {
        if (nextSource.getUrl().equals("http://matchbox.health/validiation")) {
          return nextSource;
        }
      }
    }
    return null;
  }

  public String getSessionId(FhirContext ctx, IBaseOperationOutcome outcome) {
    IBaseExtension<?, ?> ext = getMatchboxValidationExtension(ctx, outcome);
    List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
    for (IBaseExtension<?, ?> next : extensions) {
      if (next.getUrl().equals("sessionId")) {
        IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
        return value.getValueAsString();
      }
    }
    return null;
  }

  public String getIg(FhirContext ctx, IBaseOperationOutcome outcome) {
    IBaseExtension<?, ?> ext = getMatchboxValidationExtension(ctx, outcome);
    List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
    for (IBaseExtension<?, ?> next : extensions) {
      if (next.getUrl().equals("ig")) {
        IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
        return value.getValueAsString();
      }
    }
    return null;
  }

  public String getTxServer(FhirContext ctx, IBaseOperationOutcome outcome) {
    IBaseExtension<?, ?> ext = getMatchboxValidationExtension(ctx, outcome);
    List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
    for (IBaseExtension<?, ?> next : extensions) {
      if (next.getUrl().equals("txServer")) {
        IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
        return value.getValueAsString();
      }
    }
    return null;
  }

  @Test
  public void validatePatientRawR4() {
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    ValidationClient validationClient = new ValidationClient(contextR4, this.targetServer);

    String patient = "<Patient xmlns=\"http://hl7.org/fhir\">\n" + "            <id value=\"example\"/>\n"
        + "            <text>\n" + "               <status value=\"generated\"/>\n"
        + "               <div xmlns=\"http://www.w3.org/1999/xhtml\">42 </div>\n" + "            </text>\n"
        + "         </Patient>\n";

    IBaseOperationOutcome operationOutcome = validationClient.validate(patient,
        "http://hl7.org/fhir/StructureDefinition/Patient");
    assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));

    String sessionIdFirst = getSessionId(contextR4, operationOutcome);

    operationOutcome = validationClient.validate(patient,
        "http://hl7.org/fhir/StructureDefinition/Bundle");
    assertEquals(1, getValidationFailures((OperationOutcome) operationOutcome));

    String sessionIdF2nd = getSessionId(contextR4, operationOutcome);
    assertEquals(sessionIdFirst,sessionIdF2nd);
  }

  @Test
  public void verifyCachingImplementationGuides() {
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    ValidationClient validationClient = new ValidationClient(contextR4, this.targetServer);

    String resource = "<Practitioner xmlns=\"http://hl7.org/fhir\">\n" + //
            "  <identifier>\n" + //
            "    <system value=\"urn:oid:2.51.1.3\"/>\n" + //
            "    <value value=\"7610000050719\"/>\n" + //
            "  </identifier>\n" + //
            "</Practitioner>";

    // tests against base core profile
    String profileCore = "http://hl7.org/fhir/StructureDefinition/Practitioner";
    IBaseOperationOutcome operationOutcome = validationClient.validate(resource, profileCore);
    String sessionIdCore = getSessionId(contextR4, operationOutcome);
    assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
    assertEquals("hl7.fhir.r4.core#4.0.1", getIg(contextR4, operationOutcome));
    assertEquals("http://localhost:8081/matchboxv3/fhir", this.getTxServer(contextR4, operationOutcome));

    // tests against matchbox r4 test ig
    String profileMatchbox = "http://matchbox.health/ig/test/r4/StructureDefinition/practitioner-identifier-required";
    operationOutcome = validationClient.validate(resource, profileMatchbox);
    String sessionIdMatchbox = getSessionId(contextR4, operationOutcome);
    assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
    assertEquals("matchbox.health.test.ig.r4#0.1.0", getIg(contextR4, operationOutcome));

    // verify that we have have different validation engine
    assertNotEquals(sessionIdCore, sessionIdMatchbox);

    // check that the cached validation engine of core gets used
    operationOutcome = validationClient.validate(resource, profileCore);
    assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
    String sessionId2Core = getSessionId(contextR4, operationOutcome);
    assertEquals(sessionIdCore, sessionId2Core);

    // check that the cached validation engine of matchbox r4 test ig is used again
    operationOutcome = validationClient.validate(resource, profileMatchbox);
    assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
    String sessionId2Matchbox = getSessionId(contextR4, operationOutcome);
    assertEquals(sessionIdMatchbox, sessionId2Matchbox);

    // add new parameters should create a new validation engine for matchbox r4 test ig
    Parameters parameters = new Parameters();
    parameters.addParameter("txServer", "n/a");
    operationOutcome = validationClient.validate(resource, profileMatchbox, parameters);
    assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
    String sessionId2MatchboxTxNa = getSessionId(contextR4, operationOutcome);
    assertNotEquals(sessionIdMatchbox, sessionId2MatchboxTxNa);
    assertEquals("matchbox.health.test.ig.r4#0.1.0", getIg(contextR4, operationOutcome));
    assertEquals("n/a", this.getTxServer(contextR4, operationOutcome));

    // add new parameters should create a new validation engine for default validation
    operationOutcome = validationClient.validate(resource, profileCore, parameters);
    String sessionId3CoreTxNa = getSessionId(contextR4, operationOutcome);
    assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
    assertNotEquals(sessionIdCore, sessionId3CoreTxNa);
    assertEquals("n/a", this.getTxServer(contextR4, operationOutcome));
  }

  @Test
  // https://gazelle.ihe.net/jira/browse/EHS-431
  public void validateEhs431() throws IOException {
    //
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    ValidationClient validationClient = new ValidationClient(contextR4, this.targetServer);

    validationClient.capabilities();

    // IBaseOperationOutcome operationOutcome =
    // validationClient.validate(getContent("ehs-431.json"),
    // "http://fhir.ch/ig/ch-emed/StructureDefinition/ch-emed-document-medicationcard");
    IBaseOperationOutcome operationOutcome = validationClient.validate(getContent("ehs-431.json"),
        "http://hl7.org/fhir/StructureDefinition/Bundle");
    log.debug(contextR4.newJsonParser().encodeResourceToString(operationOutcome));
    assertEquals(1, getValidationFailures((OperationOutcome) operationOutcome));
  }

  @Test
  // https://gazelle.ihe.net/jira/browse/EHS-419
  public void validateEhs419() throws IOException {
    //
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    ValidationClient validationClient = new ValidationClient(contextR4, this.targetServer);

    validationClient.capabilities();

    IBaseOperationOutcome operationOutcome = validationClient.validate(getContent("ehs-419.json"),
        "http://hl7.org/fhir/StructureDefinition/Patient");
    log.debug(contextR4.newJsonParser().encodeResourceToString(operationOutcome));
    assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
  }

  private String getContent(String resourceName) throws IOException {
    Resource resource = new ClassPathResource(resourceName);
    File file = resource.getFile();
    return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
  }

}
