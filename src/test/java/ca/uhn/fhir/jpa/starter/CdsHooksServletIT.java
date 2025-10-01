package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.jpa.starter.cdshooks.StarterCdsHooksConfig;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsServiceRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.fhir.cr.hapi.config.CrCdsHooksConfig;
import org.opencds.cqf.fhir.cr.hapi.config.RepositoryConfig;
import org.opencds.cqf.fhir.cr.hapi.config.test.TestCdsHooksConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, NicknameServiceConfig.class, RepositoryConfig.class, TestCdsHooksConfig.class, CrCdsHooksConfig.class, StarterCdsHooksConfig.class},
	properties = {
		"spring.profiles.include=storageSettingsTest",
		"spring.datasource.url=jdbc:h2:mem:dbr4",
		"spring.jpa.properties.hibernate.search.backend.directory.type=local-heap",
		"hapi.fhir.enable_repository_validating_interceptor=true",
		"hapi.fhir.fhir_version=r4",
		"hapi.fhir.cr.enabled=true",
		"hapi.fhir.cr.caregaps.section_author=Organization/alphora-author",
		"hapi.fhir.cr.caregaps.reporter=Organization/alphora",
		"hapi.fhir.cdshooks.enabled=true",
		"spring.main.allow-bean-definition-overriding=true",
		"server.max-http-request-header-size=16KB"})
class CdsHooksServletIT implements IServerSupport {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(CdsHooksServletIT.class);
	private final FhirContext ourCtx = FhirContext.forR4Cached();
	private final IParser ourParser = ourCtx.newJsonParser();
	private IGenericClient ourClient;
	private String ourCdsBase;

	@Autowired
	DaoRegistry myDaoRegistry;

	@Autowired
	ICdsServiceRegistry myCdsServiceRegistry;

	@LocalServerPort
	private int port;

	private String ourServerBase;

	@BeforeEach
	void beforeEach() {
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		ourCdsBase = "http://localhost:" + port + "/cds-services";

		var cdsServicesJson = myCdsServiceRegistry.getCdsServicesJson();
		if (cdsServicesJson != null && cdsServicesJson.getServices() != null) {
			var services = cdsServicesJson.getServices();
			for (int i = 0; i < services.size(); i++) {
				myCdsServiceRegistry.unregisterService(services.get(i).getId(), "CR");
			}
		}
	}

	private Boolean hasCdsServices() throws IOException {
		var response = callCdsServicesDiscovery();

		// NOTE: this is looking for a response that indicates there are CDS services available.
		// And empty response looks like: {"services": []}
		// Looking at the actual response string consumes the InputStream which has side effects, making it tricky to compare the actual contents.
		// Hence, the test just looks at the length to make this determination.
		// The actual response has newlines in it which vary in size on some systems, but a value of 25 seems to work across linux/mac/windows
		// to ensure the response actually contains CDS services in it
		return response.getEntity().getContentLength() > 25 || response.getEntity().isChunked();
	}

	private CloseableHttpResponse callCdsServicesDiscovery() {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(ourCdsBase);
			request.addHeader("Content-Type", "application/json");
			return httpClient.execute(request);
		} catch (IOException ioe) {
			fail(ioe.getMessage());
			return null;
		}
	}

	@Test
	void testGetCdsServices() {
		var response = callCdsServicesDiscovery();
		assertEquals(200, response.getStatusLine().getStatusCode());
	}

	@Test
	void testCdsHooks() throws IOException {
		loadBundle("r4/HelloWorld-Bundle.json", ourCtx, ourClient);
		await().atMost(10000, TimeUnit.MILLISECONDS).until(this::hasCdsServices);
		var cdsRequest = """
			{
			  "hookInstance": "12345",
			  "hook": "patient-view",
			  "context": {
			    "userId": "Practitioner/example",
			    "patientId": "Patient/example-hello-world"
			  },
			  "prefetch": {
			    "item1": {
			      "resourceType": "Patient",
			      "id": "example-hello-world",
			      "gender": "male",
			      "birthDate": "2000-01-01"
			    }
			  }
			}""";
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost request = new HttpPost(ourCdsBase + "/hello-world");
			request.setEntity(new StringEntity(cdsRequest));
			request.addHeader("Content-Type", "application/json");

			CloseableHttpResponse httpResponse = httpClient.execute(request);
			String result = EntityUtils.toString(httpResponse.getEntity());
			Gson gsonResponse = new Gson();
			JsonObject response = gsonResponse.fromJson(result, JsonObject.class);
			assertNotNull(response);
			JsonArray cards = response.getAsJsonArray("cards");
			assertEquals(1, cards.size());
			assertEquals("\"Hello World!\"", cards.get(0).getAsJsonObject().get("summary").toString());
		} catch (IOException ioe) {
			fail(ioe.getMessage());
		}
	}
	
	@Test
	void testRec10() throws IOException {
		loadBundle("r4/opioidcds-10-order-sign-bundle.json", ourCtx, ourClient);
		await().atMost(20000, TimeUnit.MILLISECONDS).until(this::hasCdsServices);
		var fhirServer = "  \"fhirServer\": " + "\"" + ourServerBase + "\"" + ",\n";
		var cdsRequest = """
			{
			  "hookInstance": "055b009c-4a7d-4db4-a35e-0e5198918ed1",
			  "hook": "order-sign",
			""" + fhirServer + """
			  "context": {
				  "patientId": "example-rec-10-order-sign-illicit-POS-Cocaine-drugs",
				  "userId": "COREPRACTITIONER1",
				  "draftOrders": {
					 "resourceType": "Bundle",
					 "entry": [
						{
						  "resource": {
							 "resourceType": "MedicationRequest",
							 "id": "request-123",
							 "status": "draft",
							 "subject": {
								"reference": "Patient/example-rec-10-order-sign-illicit-POS-Cocaine-drugs"
							 },
							 "authoredOn": "2024-03-27",
							 "dosageInstruction": [
								{
								  "timing": {
									 "repeat": {
										"frequency": 1,
										"period": 1,
										"periodUnit": "d"
									 }
								  },
								  "doseAndRate": [
									 {
										"doseQuantity": {
										  "value": 1,
										  "system": "http://unitsofmeasure.org",
										  "code": "{pill}"
										}
									 }
								  ]
								}
							 ],
							 "dispenseRequest": {
								"expectedSupplyDuration": {
								  "value": 90,
								  "unit": "days",
								  "system": "http://unitsofmeasure.org",
								  "code": "d"
								}
							 },
							 "intent": "order",
							 "category": [
								{
								  "coding": [
									 {
										"system": "http://terminology.hl7.org/CodeSystem/medicationrequest-category",
										"code": "community"
									 }
								  ]
								}
							 ],
							 "medicationCodeableConcept": {
								"coding": [
								  {
									 "system": "http://www.nlm.nih.gov/research/umls/rxnorm",
									 "code": "1049502",
									 "display": "12 HR oxycodone hydrochloride 10 MG Extended Release Oral Tablet"
								  }
								]
							 }
						  }
						}
					 ]
				  }
				}
			 }
			""";
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost request = new HttpPost(ourCdsBase + "/opioidcds-10-order-sign");
			request.setEntity(new StringEntity(cdsRequest));
			request.addHeader("Content-Type", "application/json");

			CloseableHttpResponse httpResponse = httpClient.execute(request);
			String result = EntityUtils.toString(httpResponse.getEntity());
			Gson gsonResponse = new Gson();
			JsonObject response = gsonResponse.fromJson(result, JsonObject.class);
			assertNotNull(response);
			JsonArray cards = response.getAsJsonArray("cards");
			assertEquals(0, cards.size());
			//assertEquals("\"Hello World!\"", cards.get(0).getAsJsonObject().get("summary").toString());
		} catch (IOException ioe) {
			fail(ioe.getMessage());
		}
	}
}
