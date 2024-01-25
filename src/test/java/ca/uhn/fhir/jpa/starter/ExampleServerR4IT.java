package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.cr.config.RepositoryConfig;
import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.jpa.starter.cr.CrProperties;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ca.uhn.fhir.util.TestUtil.waitForSize;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opencds.cqf.fhir.utility.r4.Parameters.parameters;
import static org.opencds.cqf.fhir.utility.r4.Parameters.stringPart;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = {
		Application.class,
		NicknameServiceConfig.class,
		RepositoryConfig.class
	}, properties = {
	"spring.profiles.include=storageSettingsTest",
	"spring.datasource.url=jdbc:h2:mem:dbr4",
	"hapi.fhir.enable_repository_validating_interceptor=true",
	"hapi.fhir.fhir_version=r4",
	//"hapi.fhir.subscription.websocket_enabled=true",
	//"hapi.fhir.mdm_enabled=true",
	"hapi.fhir.cr.enabled=true",
	"hapi.fhir.cr.caregaps_section_author=Organization/alphora-author",
	"hapi.fhir.cr.caregaps_reporter=Organization/alphora",
	"hapi.fhir.implementationguides.dk-core.name=hl7.fhir.dk.core",
	"hapi.fhir.implementationguides.dk-core.version=1.1.0",
	"hapi.fhir.auto_create_placeholder_reference_targets=true",
	// Override is currently required when using MDM as the construction of the MDM
	// beans are ambiguous as they are constructed multiple places. This is evident
	// when running in a spring boot environment
	"spring.main.allow-bean-definition-overriding=true"})
class ExampleServerR4IT implements IServerSupport {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerR4IT.class);
	private IGenericClient ourClient;
	private FhirContext ourCtx;

	@Autowired
	private CrProperties crProperties;

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
		assertEquals(measureUrl + "|0.0.003", report.getMeasure());
	}

	private org.hl7.fhir.r4.model.Bundle loadBundle(String theLocation, FhirContext theCtx, IGenericClient theClient) throws IOException {
		String json = stringFromResource(theLocation);
		org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) theCtx.newJsonParser().parseResource(json);
		org.hl7.fhir.r4.model.Bundle result = theClient.transaction().withBundle(bundle).execute();
		return result;
	}

	public Parameters runCqlExecution(Parameters parameters) {

		var results = ourClient.operation().onServer()
			.named("$cql")
			.withParameters(parameters)
			.execute();
		return results;
	}

	@Test
	void testSimpleDateCqlExecutionProvider() {
		Parameters params = parameters(stringPart("expression", "Interval[Today() - 2 years, Today())"));
		Parameters results = runCqlExecution(params);
		assertTrue(results.getParameter("return").getValue() instanceof Period);
	}

	private IBaseResource loadRec(String theLocation, FhirContext theCtx, IGenericClient theClient) throws IOException {
		String json = stringFromResource(theLocation);
		List<IBaseResource> resList = new ArrayList<>();
		IBaseResource resource = (IBaseResource) theCtx.newJsonParser().parseResource(json);
		resList.add(resource);
		var result = theClient.transaction().withResources(resList).execute();
		//.withResources(resource).execute();
		return result.get(0);
	}

	@Test
	void testBatchPutWithIdenticalTags() {
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

		int initialActiveSubscriptionCount = activeSubscriptionCount();

		MethodOutcome methodOutcome = ourClient.create().resource(subscription).execute();
		IIdType mySubscriptionId = methodOutcome.getId();

		// Wait for the subscription to be activated
		await().atMost(1, TimeUnit.MINUTES).until(()->activeSubscriptionCount(), equalTo(initialActiveSubscriptionCount + 1));

		/*
		 * Attach websocket
		 */

		SocketImplementation mySocketImplementation = new SocketImplementation(mySubscriptionId.getIdPart(),
			EncodingEnum.JSON);

		URI echoUri = new URI("ws://localhost:" + port + "/websocket");

		WebSocketContainer container = ContainerProvider.getWebSocketContainer();

		ourLog.info("Connecting to : {}", echoUri);
		Session session = container.connectToServer(mySocketImplementation, echoUri);
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

	@Test
	void testCareGaps() throws IOException {

		var reporter = crProperties.getCareGapsReporter();
		var author = crProperties.getCareGapsSectionAuthor();

		assertTrue(reporter.equals("Organization/alphora"));
		assertTrue(author.equals("Organization/alphora-author"));

		String periodStartValid = "2019-01-01";
		String periodEndValid = "2019-12-31";
		String subjectPatientValid = "Patient/numer-EXM125";
		String statusValid = "open-gap";
		String measureIdValid = "BreastCancerScreeningFHIR";

		loadBundle("r4/CareGaps/authreporter-bundle.json", ourCtx, ourClient);
		loadBundle("r4/CareGaps/BreastCancerScreeningFHIR-bundle.json", ourCtx, ourClient);

		Parameters params = new Parameters();
		params.addParameter().setName("periodStart").setValue(new DateType(periodStartValid));
		params.addParameter().setName("periodEnd").setValue(new DateType(periodEndValid));
		params.addParameter().setName("subject").setValue(new StringType(subjectPatientValid));
		params.addParameter().setName("status").setValue(new StringType(statusValid));
		params.addParameter().setName("measureId").setValue(new IdType(measureIdValid));


		assertDoesNotThrow(() -> {
			ourClient.operation()
				.onType(Measure.class)
				.named("$care-gaps")
				.withParameters(params)
				.returnResourceType(Parameters.class)
				.execute();
		});
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
		//await().atMost(2, TimeUnit.MINUTES).until(() -> {
		//	sleep(1000); // execute below function every 1 second
		//	return activeSubscriptionCount() == 2; // 2 subscription based on mdm-rules.json
		//});
	}
}
