package ch.ahdis.validation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.io.ClassPathResource;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
public class QrTransformTestsStandalone {
  
  private FhirContext contextR4 = FhirVersionEnum.R4.newContext();
  private GenericFhirClient genericClient = new GenericFhirClient(contextR4, "http://10.2.254.194:8080/matchboxv3/fhir");

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CdaTransformTests.class);

  @BeforeAll 
  void waitUntilStartup() throws InterruptedException {
    Thread.sleep(20000); // give the server some time to start up
    genericClient.capabilities();
  }
  
  @Test
  public void converQrToBundle() throws IOException {
    String qr= getContent("qr.json");
    Bundle bundle = (Bundle) genericClient.convert(qr, EncodingEnum.JSON,"http://hcisolutions.ch/ig/ig-hci-vacd/StructureMap/HciVacQrToBundle", "application/fhir+json");
    assertNotNull(bundle);
    if (!bundle.getResourceType().equals(ResourceType.Bundle)) {
      log.error("wrong response "+bundle);
    }
    assertTrue(bundle.getResourceType().equals(ResourceType.Bundle));
  }

  private String getContent(String resourceName) throws IOException {
    ClassPathResource resource = new ClassPathResource(resourceName);
    File file = resource.getFile();
    return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
  }
}
