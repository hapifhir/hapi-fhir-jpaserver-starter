package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static ca.uhn.fhir.util.TestUtil.waitForSize;
import static java.lang.Thread.sleep;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, JpaStarterWebsocketDispatcherConfig.class}, properties = {
		"spring.datasource.url=jdbc:h2:mem:dbr4",
		"hapi.fhir.enable_repository_validating_interceptor=true",
		"hapi.fhir.fhir_version=r4",
		"hapi.fhir.subscription.websocket_enabled=true",
		"hapi.fhir.mdm_enabled=true",
		"hapi.fhir.cr_enabled=true",
		"hapi.fhir.implementationguides.dk-core.name=hl7.fhir.dk.core",
		"hapi.fhir.implementationguides.dk-core.version=1.1.0",
		// Override is currently required when using MDM as the construction of the MDM
		// beans are ambiguous as they are constructed multiple places. This is evident
		// when running in a spring boot environment
		"spring.main.allow-bean-definition-overriding=true" })
class ExampleServerR4IT implements IServerSupport{
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerR4IT.class);
	private IGenericClient ourClient;
	private FhirContext ourCtx;

	private ApplicationContext ctx;

	@LocalServerPort
	private int port;

	@Test
	@Order(0)
	void testCreateAndRead() {
		String methodName = "testCreateAndRead";
		ourLog.info("Entering " + methodName + "()...");

		Patient pt = new Patient();
		pt.setActive(true);
		pt.getBirthDateElement().setValueAsString("2020-01-01");
		pt.addIdentifier().setSystem("http://foo").setValue("12345");
		pt.addName().setFamily(methodName);
		IIdType id = ourClient.create().resource(pt).execute().getId();

		Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
		assertEquals(methodName, pt2.getName().get(0).getFamily());

		// Wait until the MDM message has been processed
		await().atMost(1, TimeUnit.MINUTES).until(() -> getGoldenResourcePatient() != null);
		Patient goldenRecord = getGoldenResourcePatient();

		// Verify that a golden record Patient was created
		assertNotNull(
			goldenRecord.getMeta().getTag("http://hapifhir.io/fhir/NamingSystem/mdm-record-status", "GOLDEN_RECORD"));
	}

	private Patient getGoldenResourcePatient() {
		Bundle bundle = ourClient.search().forResource(Patient.class)
			.withTag("http://hapifhir.io/fhir/NamingSystem/mdm-record-status", "GOLDEN_RECORD")
			.cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute();
		if (bundle.getEntryFirstRep() != null) {
			return (Patient) bundle.getEntryFirstRep().getResource();
		} else {
			return null;
		}
	}
	@Test
	public void testCQLEvaluateMeasureEXM130() throws IOException {
		String measureId = "ColorectalCancerScreeningsFHIR";
		String measureUrl = "http://ecqi.healthit.gov/ecqms/Measure/ColorectalCancerScreeningsFHIR";

		loadBundle("r4/EXM130/EXM130-7.3.000-bundle.json", ourCtx, ourClient);


		Parameters inParams = new Parameters();
		inParams.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
		inParams.addParameter().setName("periodEnd").setValue(new StringType("2019-12-31"));
		inParams.addParameter().setName("reportType").setValue(new StringType("summary"));

		Parameters outParams = ourClient
			.operation()
			.onInstance(new IdDt("Measure", measureId))
			.named("$evaluate-measure")
			.withParameters(inParams)
			.cacheControl(new CacheControlDirective().setNoCache(true))
			.withAdditionalHeader("Content-Type", "application/json")
			.useHttpGet()
			.execute();

		List<Parameters.ParametersParameterComponent> response = outParams.getParameter();
		assertFalse(response.isEmpty());
		Parameters.ParametersParameterComponent component = response.get(0);
		assertTrue(component.getResource() instanceof MeasureReport);
		MeasureReport report = (MeasureReport) component.getResource();
		assertEquals(measureUrl, report.getMeasure());
	}

	private org.hl7.fhir.r4.model.Bundle loadBundle(String theLocation, FhirContext theCtx, IGenericClient theClient) throws IOException {
		String json = stringFromResource(theLocation);
		org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) theCtx.newJsonParser().parseResource(json);
		org.hl7.fhir.r4.model.Bundle result = theClient.transaction().withBundle(bundle).execute();
		return result;
	}
	@Test
	public void testBatchPutWithIdenticalTags() {
		String batchPuts = "{\n" +
			"\t\"resourceType\": \"Bundle\",\n" +
			"\t\"id\": \"patients\",\n" +
			"\t\"type\": \"batch\",\n" +
			"\t\"entry\": [\n" +
			"\t\t{\n" +
			"\t\t\t\"request\": {\n" +
			"\t\t\t\t\"method\": \"PUT\",\n" +
			"\t\t\t\t\"url\": \"Patient/pat-1\"\n" +
			"\t\t\t},\n" +
			"\t\t\t\"resource\": {\n" +
			"\t\t\t\t\"resourceType\": \"Patient\",\n" +
			"\t\t\t\t\"id\": \"pat-1\",\n" +
			"\t\t\t\t\"meta\": {\n" +
			"\t\t\t\t\t\"tag\": [\n" +
			"\t\t\t\t\t\t{\n" +
			"\t\t\t\t\t\t\t\"system\": \"http://mysystem.org\",\n" +
			"\t\t\t\t\t\t\t\"code\": \"value2\"\n" +
			"\t\t\t\t\t\t}\n" +
			"\t\t\t\t\t]\n" +
			"\t\t\t\t}\n" +
			"\t\t\t},\n" +
			"\t\t\t\"fullUrl\": \"/Patient/pat-1\"\n" +
			"\t\t},\n" +
			"\t\t{\n" +
			"\t\t\t\"request\": {\n" +
			"\t\t\t\t\"method\": \"PUT\",\n" +
			"\t\t\t\t\"url\": \"Patient/pat-2\"\n" +
			"\t\t\t},\n" +
			"\t\t\t\"resource\": {\n" +
			"\t\t\t\t\"resourceType\": \"Patient\",\n" +
			"\t\t\t\t\"id\": \"pat-2\",\n" +
			"\t\t\t\t\"meta\": {\n" +
			"\t\t\t\t\t\"tag\": [\n" +
			"\t\t\t\t\t\t{\n" +
			"\t\t\t\t\t\t\t\"system\": \"http://mysystem.org\",\n" +
			"\t\t\t\t\t\t\t\"code\": \"value2\"\n" +
			"\t\t\t\t\t\t}\n" +
			"\t\t\t\t\t]\n" +
			"\t\t\t\t}\n" +
			"\t\t\t},\n" +
			"\t\t\t\"fullUrl\": \"/Patient/pat-2\"\n" +
			"\t\t}\n" +
			"\t]\n" +
			"}";
		Bundle bundle = FhirContext.forR4().newJsonParser().parseResource(Bundle.class, batchPuts);
		ourClient.transaction().withBundle(bundle).execute();
	}

	@Test
	@Order(1)
	void testWebsocketSubscription() throws Exception {
		/*
		 * Create subscription
		 */
		Subscription subscription = new Subscription();
		subscription.setReason("Monitor new neonatal function (note, age will be determined by the monitor)");
		subscription.setStatus(Subscription.SubscriptionStatus.REQUESTED);
		subscription.setCriteria("Observation?status=final");

		Subscription.SubscriptionChannelComponent channel = new Subscription.SubscriptionChannelComponent();
		channel.setType(Subscription.SubscriptionChannelType.WEBSOCKET);
		channel.setPayload("application/json");
		subscription.setChannel(channel);

		MethodOutcome methodOutcome = ourClient.create().resource(subscription).execute();
		IIdType mySubscriptionId = methodOutcome.getId();

		// Wait for the subscription to be activated
		await().atMost(1, TimeUnit.MINUTES).until(() -> activeSubscriptionCount() == 3);

		/*
		 * Attach websocket
		 */

		WebSocketClient myWebSocketClient = new WebSocketClient();
		SocketImplementation mySocketImplementation = new SocketImplementation(mySubscriptionId.getIdPart(),
			EncodingEnum.JSON);

		myWebSocketClient.start();
		URI echoUri = new URI("ws://localhost:" + port + "/websocket");
		ClientUpgradeRequest request = new ClientUpgradeRequest();
		ourLog.info("Connecting to : {}", echoUri);
		Future<Session> connection = myWebSocketClient.connect(mySocketImplementation, echoUri, request);
		Session session = connection.get(2, TimeUnit.SECONDS);

		ourLog.info("Connected to WS: {}", session.isOpen());

		/*
		 * Create a matching resource
		 */
		Observation obs = new Observation();
		obs.setStatus(Observation.ObservationStatus.FINAL);
		ourClient.create().resource(obs).execute();

		/*
		 * Ensure that we receive a ping on the websocket
		 */
		waitForSize(1, () -> mySocketImplementation.myPingCount);

		/*
		 * Clean up
		 */
		ourClient.delete().resourceById(mySubscriptionId).execute();
	}

	private int activeSubscriptionCount() {
		return ourClient.search().forResource(Subscription.class).where(Subscription.STATUS.exactly().code("active"))
			.cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute().getEntry()
			.size();
	}

	@BeforeEach
	void beforeEach() {

		ourCtx = FhirContext.forR4();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		await().atMost(2, TimeUnit.MINUTES).until(() -> {
			sleep(1000); // execute below function every 1 second
			return activeSubscriptionCount() == 2; // 2 subscription based on mdm-rules.json
		});
	}
}
