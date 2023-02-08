package ch.ahdis.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.Application;
import ca.uhn.fhir.rest.api.EncodingEnum;

import org.springframework.test.context.junit4.SpringRunner;


@RunWith(Parameterized.class)
@Ignore
public class QrTransformTestsStandalone {
  
  private FhirContext contextR4 = FhirVersionEnum.R4.newContext();
  private GenericFhirClient genericClient = new GenericFhirClient(contextR4, "http://10.2.254.194:8080/matchboxv3/fhir");

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CdaTransformTests.class);

  @Parameters
  public static Object[][] data() {
      return new Object[5000][0];
  }

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