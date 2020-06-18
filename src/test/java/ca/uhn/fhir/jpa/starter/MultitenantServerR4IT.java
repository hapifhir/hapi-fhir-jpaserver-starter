package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.model.util.ProviderConstants;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.client.interceptor.UrlTenantSelectionInterceptor;
import ca.uhn.fhir.test.utilities.JettyUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class MultitenantServerR4IT {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MultitenantServerR4IT.class);
  private static IGenericClient ourClient;
  private static FhirContext ourCtx;
  private static int ourPort;
  private static Server ourServer;
  private static UrlTenantSelectionInterceptor ourClientTenantInterceptor;

  static {
    HapiProperties.forceReload();
    HapiProperties.setProperty(HapiProperties.DATASOURCE_URL, "jdbc:h2:mem:dbr4-mt");
    HapiProperties.setProperty(HapiProperties.FHIR_VERSION, "R4");
    HapiProperties.setProperty(HapiProperties.SUBSCRIPTION_WEBSOCKET_ENABLED, "true");
    HapiProperties.setProperty(HapiProperties.PARTITIONING_MULTITENANCY_ENABLED, "true");
    ourCtx = FhirContext.forR4();
  }

  @Test
  public void testCreateAndReadInTenantA() {
    ourLog.info("Base URL is: " + HapiProperties.getServerAddress());

    // Create tenant A
    ourClientTenantInterceptor.setTenantId("DEFAULT");
    ourClient
      .operation()
      .onServer()
      .named(ProviderConstants.PARTITION_MANAGEMENT_CREATE_PARTITION)
      .withParameter(Parameters.class, ProviderConstants.PARTITION_MANAGEMENT_PARTITION_ID, new IntegerType(1))
      .andParameter(ProviderConstants.PARTITION_MANAGEMENT_PARTITION_NAME, new CodeType("TENANT-A"))
      .execute();


    ourClientTenantInterceptor.setTenantId("TENANT-A");
    Patient pt = new Patient();
    pt.addName().setFamily("Family A");
    ourClient.create().resource(pt).execute().getId();

    Bundle searchResult = ourClient.search().forResource(Patient.class).returnBundle(Bundle.class).cacheControl(new CacheControlDirective().setNoCache(true)).execute();
    assertEquals(1, searchResult.getEntry().size());
    Patient pt2 = (Patient) searchResult.getEntry().get(0).getResource();
    assertEquals("Family A", pt2.getName().get(0).getFamily());
  }

  @Test
  public void testCreateAndReadInTenantB() {
    ourLog.info("Base URL is: " + HapiProperties.getServerAddress());

    // Create tenant A
    ourClientTenantInterceptor.setTenantId("DEFAULT");
    ourClient
      .operation()
      .onServer()
      .named(ProviderConstants.PARTITION_MANAGEMENT_CREATE_PARTITION)
      .withParameter(Parameters.class, ProviderConstants.PARTITION_MANAGEMENT_PARTITION_ID, new IntegerType(2))
      .andParameter(ProviderConstants.PARTITION_MANAGEMENT_PARTITION_NAME, new CodeType("TENANT-B"))
      .execute();


    ourClientTenantInterceptor.setTenantId("TENANT-B");
    Patient pt = new Patient();
    pt.addName().setFamily("Family B");
    ourClient.create().resource(pt).execute().getId();

    Bundle searchResult = ourClient.search().forResource(Patient.class).returnBundle(Bundle.class).cacheControl(new CacheControlDirective().setNoCache(true)).execute();
    assertEquals(1, searchResult.getEntry().size());
    Patient pt2 = (Patient) searchResult.getEntry().get(0).getResource();
    assertEquals("Family B", pt2.getName().get(0).getFamily());
  }

  @AfterClass
  public static void afterClass() throws Exception {
    ourServer.stop();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    String path = Paths.get("").toAbsolutePath().toString();

    ourLog.info("Project base path is: {}", path);

    ourServer = new Server(0);

    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setContextPath("/hapi-fhir-jpaserver");
    webAppContext.setDisplayName("HAPI FHIR");
    webAppContext.setDescriptor(path + "/src/main/webapp/WEB-INF/web.xml");
    webAppContext.setResourceBase(path + "/target/hapi-fhir-jpaserver-starter");
    webAppContext.setParentLoaderPriority(true);

    ourServer.setHandler(webAppContext);
    ourServer.start();

    ourPort = JettyUtil.getPortForStartedServer(ourServer);

    ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
    ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
    String ourServerBase = HapiProperties.getServerAddress();
    ourServerBase = "http://localhost:" + ourPort + "/hapi-fhir-jpaserver/fhir/";

    ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
    ourClient.registerInterceptor(new LoggingInterceptor(true));

    ourClientTenantInterceptor = new UrlTenantSelectionInterceptor();
    ourClient.registerInterceptor(ourClientTenantInterceptor);
  }

  public static void main(String[] theArgs) throws Exception {
    ourPort = 8080;
    beforeClass();
  }
}
