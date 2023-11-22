package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.cr.config.RepositoryConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IIdType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static ca.uhn.fhir.util.TestUtil.waitForSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = {
		Application.class,
		JpaStarterWebsocketDispatcherConfig.class,
		RepositoryConfig.class
	}, properties =
  {
	  "spring.profiles.include=storageSettingsTest",
     "spring.datasource.url=jdbc:h2:mem:dbr3",
     "hapi.fhir.fhir_version=dstu3",
	  "hapi.fhir.cr_enabled=true",
     "hapi.fhir.subscription.websocket_enabled=true",
     "hapi.fhir.allow_external_references=true",
     "hapi.fhir.allow_placeholder_references=true",
	  "spring.main.allow-bean-definition-overriding=true"
  })


class ExampleServerDstu3IT implements IServerSupport {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerDstu3IT.class);
  private IGenericClient ourClient;
  private FhirContext ourCtx;

  @Autowired
  DaoRegistry myDaoRegistry;

  @LocalServerPort
  private int port;

  @BeforeEach
  void beforeEach() {
    ourCtx = FhirContext.forDstu3();
    ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
    ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
    String ourServerBase = "http://localhost:" + port + "/fhir/";
    ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
    ourClient.registerInterceptor(new LoggingInterceptor(true));
  }

  @Test
  void testCreateAndRead() {

    String methodName = "testCreateResourceConditional";

    Patient pt = new Patient();
    pt.addName().setFamily(methodName);
    IIdType id = ourClient.create().resource(pt).execute().getId();

    Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
    assertEquals(methodName, pt2.getName().get(0).getFamily());
  }

  // Currently fails with:
  // ca.uhn.fhir.rest.server.exceptions.InternalErrorException: HTTP 500 : Failed to call access method: java.lang.IllegalArgumentException: Could not load library source for libraries referenced in Measure/Measure/measure-EXM104-FHIR3-8.1.000/_history/1.
  //@Test Bad test data
  public void testCQLEvaluateMeasureEXM104() throws IOException {
    String measureId = "measure-EXM104-FHIR3-8.1.000";

    int numFilesLoaded = loadDataFromDirectory("dstu3/EXM104/EXM104_FHIR3-8.1.000-bundle.json");
    //assertEquals(numFilesLoaded, 3);
    ourLog.info("{} files imported successfully!", numFilesLoaded);
   // loadBundle("dstu3/EXM104/EXM104_FHIR3-8.1.000-bundle.json", ourCtx, ourClient);

    // http://localhost:8080/fhir/Measure/measure-EXM104-FHIR3-8.1.000/$evaluate-measure?periodStart=2019-01-01&periodEnd=2019-12-31
    Parameters inParams = new Parameters();
//    inParams.addParameter().setName("measure").setValue(new StringType("Measure/measure-EXM104-8.2.000"));
    inParams.addParameter().setName("patient").setValue(new StringType("Patient/numer-EXM104-FHIR3"));
    inParams.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
    inParams.addParameter().setName("periodEnd").setValue(new StringType("2019-12-31"));
	  inParams.addParameter().setName("reportType").setValue(new StringType("individual"));

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
    assertEquals("Measure/"+measureId, report.getMeasure());
  }

  private int loadDataFromDirectory(String theDirectoryName) throws IOException {
    int count = 0;
    ourLog.info("Reading files in directory: {}", theDirectoryName);
    ClassPathResource dir = new ClassPathResource(theDirectoryName);
    Collection<File> files = FileUtils.listFiles(dir.getFile(), null, false);
    ourLog.info("{} files found.", files.size());
    for (File file : files) {
      String filename = file.getAbsolutePath();
      ourLog.info("Processing filename '{}'", filename);
      if (filename.endsWith(".cql") || filename.contains("expectedresults")) {
        // Ignore .cql and expectedresults files
        ourLog.info("Ignoring file: '{}'", filename);
      } else if (filename.endsWith(".json")) {
        if (filename.contains("bundle")) {
          loadBundle(filename, ourCtx, ourClient);
        } else {
          loadResource(filename, ourCtx, myDaoRegistry);
        }
        count++;
      } else {
        ourLog.info("Ignoring file: '{}'", filename);
      }
    }
    return count;
  }

  private Bundle loadBundle(String theLocation, FhirContext theCtx, IGenericClient theClient) throws IOException {
    String json = stringFromResource(theLocation);
    Bundle bundle = (Bundle) theCtx.newJsonParser().parseResource(json);
    Bundle result = theClient.transaction().withBundle(bundle).execute();
    return result;
  }

  @Test
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
    waitForSize(1, () -> ourClient.search().forResource(Subscription.class).where(Subscription.STATUS.exactly().code("active")).cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute().getEntry().size());

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

}
