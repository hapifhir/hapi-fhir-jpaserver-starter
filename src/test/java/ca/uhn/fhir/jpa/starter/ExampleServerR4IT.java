package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.BundleUtil;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static ca.uhn.fhir.util.TestUtil.waitForSize;
import static java.util.Comparator.comparing;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class, properties =
<<<<<<< HEAD
	{
		"spring.batch.job.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:dbr4",
		"hapi.fhir.enable_repository_validating_interceptor=true",
		"hapi.fhir.fhir_version=r4",
		"hapi.fhir.subscription.websocket_enabled=true",
		"hapi.fhir.mdm_enabled=true",
		//Override is currently required when using MDM as the construction of the MDM beans are ambiguous as they are constructed multiple places. This is evident when running in a spring boot environment
		"spring.main.allow-bean-definition-overriding=true"
	})
public class ExampleServerR4IT {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerDstu2IT.class);
	private IGenericClient ourClient;
	private FhirContext ourCtx;

	@LocalServerPort
	private int port;


	@Test
	@Order(0)
	void testCreateAndRead() {

		String methodName = "testCreateResourceConditional";

		Patient pt = new Patient();
		pt.setActive(true);
		pt.getBirthDateElement().setValueAsString("2020-01-01");
		pt.addIdentifier().setSystem("http://foo").setValue("12345");
		pt.addName().setFamily(methodName);
		IIdType id = ourClient.create().resource(pt).execute().getId();

		Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
		assertEquals(methodName, pt2.getName().get(0).getFamily());


		// Test MDM

		// Wait until the MDM message has been processed
		await().until(() -> getPatients().size(), equalTo(2));
		List<Patient> persons = getPatients();
		Patient goldenRecord = persons.get(0);

		// Verify that a golden record Patient was created
		assertNotNull(goldenRecord.getMeta().getTag("http://hapifhir.io/fhir/NamingSystem/mdm-record-status", "GOLDEN_RECORD"));
	}

	private List<Patient> getPatients() {
		Bundle bundle = ourClient.search().forResource(Patient.class).cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute();
		List<Patient> retVal = BundleUtil.toListOfResourcesOfType(ourCtx, bundle, Patient.class);
		retVal.sort(comparing(o -> ((Patient) o).getMeta().getLastUpdated()).reversed());
		return retVal;
	}

	@Test
	@Order(1)
	public void testWebsocketSubscription() throws Exception {
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
		await().until(() -> activeSubscriptionCount() == 3);

		/*
		 * Attach websocket
		 */

		WebSocketClient myWebSocketClient = new WebSocketClient();
		SocketImplementation mySocketImplementation = new SocketImplementation(mySubscriptionId.getIdPart(), EncodingEnum.JSON);

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

		// Give some time for the subscription to deliver
		Thread.sleep(2000);

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
		return ourClient.search().forResource(Subscription.class).where(Subscription.STATUS.exactly().code("active")).cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute().getEntry().size();
	}


	@BeforeEach
	void beforeEach() {

		ourCtx = FhirContext.forR4();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		ourClient.registerInterceptor(new LoggingInterceptor(true));
	}
=======
  {
    "spring.batch.job.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:dbr4",
    "hapi.fhir.fhir_version=R4",
    "hapi.fhir.cql_enabled=true",
    "hapi.fhir.empi_enabled=true",
    "hapi.fhir.subscription.websocket_enabled=true",
    //Override is currently required when using Empi as the construction of the Empi beans are ambiguous as they are constructed multiple places. This is evident when running in a spring boot environment
    "spring.main.allow-bean-definition-overriding=true"
  })
public class ExampleServerR4IT implements IServerSupport {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerDstu2IT.class);
  private IGenericClient ourClient;
  private FhirContext ourCtx;
  private String ourServerBaseURL;

  @Autowired
  DaoRegistry myDaoRegistry;

  @LocalServerPort
  private int port;

  @BeforeEach
  void beforeEach() {
    ourCtx = FhirContext.forR4();
    ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
    ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
    ourServerBaseURL = "http://localhost:" + port + "/fhir/";
    ourClient = ourCtx.newRestfulGenericClient(ourServerBaseURL);
    ourClient.registerInterceptor(new LoggingInterceptor(true));
  }

  @AfterEach
  void afterEach() {
    ourLog.info("Finished running a test...");
  }

  @Test
  void testCreateAndRead() {

    String methodName = "testCreateResourceConditional";

    Patient pt = new Patient();
    pt.setActive(true);
    pt.getBirthDateElement().setValueAsString("2020-01-01");
    pt.addIdentifier().setSystem("http://foo").setValue("12345");
    pt.addName().setFamily(methodName);
    IIdType id = ourClient.create().resource(pt).execute().getId();

    Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
    assertEquals(methodName, pt2.getName().get(0).getFamily());

    // Test EMPI

    // Wait until the EMPI message has been processed
    await().until(() -> getPeople().size() > 0);
    List<Person> persons = getPeople();

    // Verify a Person was created that links to our Patient
    Optional<String> personLinkToCreatedPatient = persons.stream()
      .map(Person::getLink)
      .flatMap(Collection::stream)
      .map(Person.PersonLinkComponent::getTarget)
      .map(Reference::getReference)
      .filter(pid -> id.toUnqualifiedVersionless().getValue().equals(pid))
      .findAny();
    assertTrue(personLinkToCreatedPatient.isPresent());
  }

  private List<Person> getPeople() {
    Bundle bundle = ourClient.search().forResource(Person.class).cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute();
    return BundleUtil.toListOfResourcesOfType(ourCtx, bundle, Person.class);
  }

  @Test
  public void testCQLEvaluateMeasureEXM104() throws IOException {
    String measureId = "measure-EXM104-8.2.000";

    loadBundle("r4/EXM104/EXM104-8.2.000-bundle.json", ourCtx, ourClient);

    // http://localhost:8080/fhir/Measure/measure-EXM104-8.2.000/$evaluate-measure?periodStart=2019-01-01&periodEnd=2019-12-31
    Parameters inParams = new Parameters();
//    inParams.addParameter().setName("measure").setValue(new StringType("Measure/measure-EXM104-8.2.000"));
//    inParams.addParameter().setName("patient").setValue(new StringType("Patient/numer-EXM104-FHIR3"));
    inParams.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
    inParams.addParameter().setName("periodEnd").setValue(new StringType("2019-12-31"));

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
    Assert.assertTrue(!response.isEmpty());
    Parameters.ParametersParameterComponent component = response.get(0);
    Assert.assertTrue(component.getResource() instanceof MeasureReport);
    MeasureReport report = (MeasureReport) component.getResource();
    Assert.assertEquals("Measure/"+measureId, report.getMeasure());
  }

  private Bundle loadBundle(String theLocation, FhirContext theCtx, IGenericClient theClient) throws IOException {
    String json = stringFromResource(theLocation);
    Bundle bundle = (Bundle) theCtx.newJsonParser().parseResource(json);
    Bundle result = (Bundle) theClient.transaction().withBundle(bundle).execute();
    return result;
  }

  // Fails with: ca.uhn.fhir.rest.server.exceptions.InternalErrorException: HTTP 500 :
  //             Failed to call access method: java.lang.IllegalArgumentException:
  //             Could not load library source for libraries referenced in Measure/Measure/measure-EXM125-FHIR4-7.2.000/_history/1.
  //@Test
  public void testCQLEvaluateMeasureEXM125() throws IOException {
    String measureId = "measure-EXM125-FHIR4-7.2.000";

    loadBundle("r4/EXM125/library-deps-EXM125_FHIR4-7.2.000-bundle.json", ourCtx, ourClient);
    loadResource("r4/EXM125/library-EXM125_FHIR4-7.2.000.json", ourCtx, myDaoRegistry);
    loadBundle("r4/EXM125/EXM125_FHIR4-7.2.000-bundle.json", ourCtx, ourClient);
    loadResource("r4/EXM125/measure-EXM125_FHIR4-7.2.000.json", ourCtx, myDaoRegistry);

    // http://localhost:8080/fhir/Measure/measure-EXM125-FHIR4-7.2.000/$evaluate-measure?periodStart=2019-01-01&periodEnd=2019-12-31
    Parameters inParams = new Parameters();
//    inParams.addParameter().setName("measure").setValue(new StringType("Measure/measure-EXM104-8.2.000"));
//    inParams.addParameter().setName("patient").setValue(new StringType("Patient/numer-EXM104-FHIR3"));
    inParams.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
    inParams.addParameter().setName("periodEnd").setValue(new StringType("2019-12-31"));

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
    Assert.assertTrue(!response.isEmpty());
    Parameters.ParametersParameterComponent component = response.get(0);
    Assert.assertTrue(component.getResource() instanceof MeasureReport);
    MeasureReport report = (MeasureReport) component.getResource();
    Assert.assertEquals("Measure/"+measureId, report.getMeasure());
  }

  // Fails with: ca.uhn.fhir.rest.server.exceptions.InternalErrorException: HTTP 500 :
  //             Failed to call access method: java.lang.IllegalArgumentException:
  //             Could not load library source for libraries referenced in Measure/Measure/measure-EXM130-FHIR4-7.2.000/_history/1.
  //@Test
  public void testCQLEvaluateMeasureEXM130() throws IOException {
    String measureId = "measure-EXM130-FHIR4-7.2.000";

    loadBundle("r4/EXM130/EXM130_FHIR4-7.2.000-bundle.json", ourCtx, ourClient);

    // http://localhost:8080/fhir/Measure/measure-EXM130-FHIR4-7.2.000/$evaluate-measure?periodStart=2019-01-01&periodEnd=2019-12-31
    Parameters inParams = new Parameters();
//    inParams.addParameter().setName("measure").setValue(new StringType("Measure/measure-EXM104-8.2.000"));
//    inParams.addParameter().setName("patient").setValue(new StringType("Patient/numer-EXM104-FHIR3"));
    inParams.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
    inParams.addParameter().setName("periodEnd").setValue(new StringType("2019-12-31"));

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
    Assert.assertTrue(!response.isEmpty());
    Parameters.ParametersParameterComponent component = response.get(0);
    Assert.assertTrue(component.getResource() instanceof MeasureReport);
    MeasureReport report = (MeasureReport) component.getResource();
    Assert.assertEquals("Measure/"+measureId, report.getMeasure());
  }

  @Test
  public void testWebsocketSubscription() throws Exception {
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
    await().until(() -> activeSubscriptionCount() == 3);

    /*
     * Attach websocket
     */

    WebSocketClient myWebSocketClient = new WebSocketClient();
    SocketImplementation mySocketImplementation = new SocketImplementation(mySubscriptionId.getIdPart(), EncodingEnum.JSON);

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

    // Give some time for the subscription to deliver
    Thread.sleep(2000);

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
    return ourClient.search().forResource(Subscription.class).where(Subscription.STATUS.exactly().code("active")).cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute().getEntry().size();
  }
>>>>>>> Adding Integration Tests for Measure Reports. There's still more work to do to get them to Pass...
}
