package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.cql.provider.CqlProviderLoader;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IIdType;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static ca.uhn.fhir.util.TestUtil.waitForSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class, properties =
  {
    "spring.batch.job.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:dbr3",
    "hapi.fhir.cql_enabled=true",
    "hapi.fhir.fhir_version=dstu3",
    "hapi.fhir.subscription.websocket_enabled=true",
    "hapi.fhir.allow_external_references=true",
    "hapi.fhir.allow_placeholder_references=true",
  })


public class ExampleServerDstu3IT implements IServerSupport {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerDstu2IT.class);
  private IGenericClient ourClient;
  private FhirContext ourCtx;

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
  public void testCreateAndRead() {

    String methodName = "testCreateResourceConditional";

    Patient pt = new Patient();
    pt.addName().setFamily(methodName);
    IIdType id = ourClient.create().resource(pt).execute().getId();

    Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
    assertEquals(methodName, pt2.getName().get(0).getFamily());
  }

  @Test
  public void testCQLEvaluateMeasure() throws IOException {
    CqlProviderLoader cqlProviderLoader = null;

    // FIXME KBD Remove this and put some Unit Test code here
    loadBundle("dstu3/EXM104/EXM104_FHIR3-8.1.000-bundle.json", ourCtx, ourClient);

    Parameters inParams = new Parameters();
//    inParams.addParameter().setName("measure").setValue(new StringType("Measure/measure-EXM104-8.2.000"));
//    inParams.addParameter().setName("patient").setValue(new StringType("Patient/numer-EXM104-FHIR3"));
    inParams.addParameter().setName("periodStart").setValue(new DateType("2019-01-01"));
    inParams.addParameter().setName("periodEnd").setValue(new DateType("2019-12-31"));

    Parameters outParams = ourClient
      .operation()
      .onInstance(new IdDt("Measure", "measure-EXM104-FHIR3-8.1.000"))
      .named("$evaluate-measure")
      .withParameters(inParams)
      .useHttpGet()
      .execute();

//    Parameters outParams = ourClient
//      .operation()
//      .onType(Measure.class)
//      .named("$evaluate-measure")
//      .withParameters(inParams)
//      .useHttpGet()
//      .execute();

    List<Parameters.ParametersParameterComponent> response = outParams.getParameter();

    Assert.assertTrue(!response.isEmpty());

    Parameters.ParametersParameterComponent component = response.get(0);

    Assert.assertTrue(component.getResource() instanceof MeasureReport);

    MeasureReport report = (MeasureReport) component.getResource();

    for (MeasureReport.MeasureReportGroupComponent group : report.getGroup()) {
      for (MeasureReport.MeasureReportGroupPopulationComponent population : group.getPopulation()) {
        Assert.assertTrue(population.getCount() > 0);
      }
    }
  }

  private Bundle loadBundle(String theLocation, FhirContext theCtx, IGenericClient theClient) throws IOException {
    String json = stringFromResource(theLocation);
    Bundle bundle = (Bundle) theCtx.newJsonParser().parseResource(json);
    Bundle result = (Bundle) theClient.transaction().withBundle(bundle).execute();
    return result;
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
