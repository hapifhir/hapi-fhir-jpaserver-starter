package ch.ahdis.matchbox.mapping;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.Application;
import ch.ahdis.matchbox.test.CompareUtil;
import ch.ahdis.matchbox.test.ValidationClient;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Application.class})
@ActiveProfiles("test-transform")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TransformTest {
	private static final String TARGET_SERVER = "http://localhost:8086/matchboxv3";
	private static final FhirContext FHIR_CONTEXT = FhirVersionEnum.R4.newContextCached();

	private final ValidationClient validationClient = new ValidationClient(FHIR_CONTEXT, TARGET_SERVER + "/fhir");
	private final HttpClient httpClient = HttpClient.newHttpClient();

	@BeforeAll
	void waitUntilStartup() throws Exception {
		Thread.sleep(10000); // give the server some time to start up
		this.validationClient.capabilities();
		CompareUtil.logMemory();
	}

	@Test
	void testTransformFullyContained() throws Exception {
		final var transformRequest = HttpRequest.newBuilder(URI.create(TARGET_SERVER + "/fhir/StructureMap/$transform"))
			.POST(HttpRequest.BodyPublishers.ofString(this.getContent("transform_fully_contained_body.json")))
			.header("Content-Type", "application/fhir+json")
			.header("Accept", "application/fhir+xml")
			.build();
		final var response = this.httpClient.send(transformRequest, HttpResponse.BodyHandlers.ofString());
		assertEquals("""
			 <?xml version="1.0" encoding="UTF-8"?>
			 
			 <TRight xmlns="http://hl7.org/fhir/tutorial">
				  <a2 value="test"/>
			 </TRight>""", response.body());
	}

	private String getContent(final String resourceName) throws IOException {
		Resource resource = new ClassPathResource(resourceName);
		File file = resource.getFile();
		return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	}
}
