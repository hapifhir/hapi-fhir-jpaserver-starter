package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.cr.config.RepositoryConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsServiceRegistry;
import ca.uhn.hapi.fhir.cdshooks.api.json.CdsServiceResponseJson;
import ca.uhn.hapi.fhir.cdshooks.config.CdsHooksConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = {
		Application.class,
		NicknameServiceConfig.class,
		RepositoryConfig.class,
		CdsHooksConfig.class
	}, properties = {
	"spring.profiles.include=storageSettingsTest",
	"spring.datasource.url=jdbc:h2:mem:dbr4",
	"hapi.fhir.enable_repository_validating_interceptor=true",
	"hapi.fhir.fhir_version=r4",
	"hapi.fhir.cr.enabled=true",
	"hapi.fhir.cr.caregaps.section_author=Organization/alphora-author",
	"hapi.fhir.cr.caregaps.reporter=Organization/alphora",
	"hapi.fhir.cdshooks.enabled=true",
	"spring.main.allow-bean-definition-overriding=true"})
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

	@BeforeEach
	void beforeEach() {
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		ourCdsBase = "http://localhost:" + port + "/cds-services";
	}

	private JsonArray getCdsServices() throws IOException {
		var response = callCdsServicesDiscovery();
		String result = EntityUtils.toString(response.getEntity());
		Gson gsonResponse = new Gson();
		JsonObject services = gsonResponse.fromJson(result, JsonObject.class);
		return !services.has("services") ? new JsonArray() : services.get("services").getAsJsonArray();
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
	void testCdsHooks() throws IOException, InterruptedException {
		loadBundle("r4/HelloWorld-Bundle.json", ourCtx, ourClient);
		await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> getCdsServices().size(), equalTo(1));
		var cdsRequest = "{\n" +
			"  \"hookInstance\": \"12345\",\n" +
			"  \"hook\": \"patient-view\",\n" +
			"  \"context\": {\n" +
			"    \"userId\": \"Practitioner/example\",\n" +
			"    \"patientId\": \"Patient/example-hello-world\"\n" +
			"  },\n" +
			"  \"prefetch\": {\n" +
			"    \"item1\": {\n" +
			"      \"resourceType\": \"Patient\",\n" +
			"      \"id\": \"example-hello-world\",\n" +
			"      \"gender\": \"male\",\n" +
			"      \"birthDate\": \"2000-01-01\"\n" +
			"    }\n" +
			"  }\n" +
			"}";
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
}
