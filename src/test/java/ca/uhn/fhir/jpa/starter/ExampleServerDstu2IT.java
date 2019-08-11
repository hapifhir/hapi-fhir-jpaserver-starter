package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.test.utilities.JettyUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hl7.fhir.instance.model.api.IIdType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ExampleServerDstu2IT {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerDstu2IT.class);
	private static IGenericClient ourClient;
	private static FhirContext ourCtx;
	private static int ourPort;
	private static Server ourServer;

	static {
		HapiProperties.forceReload();
		HapiProperties.setProperty(HapiProperties.FHIR_VERSION, "DSTU2");
		HapiProperties.setProperty(HapiProperties.DATASOURCE_URL, "jdbc:h2:mem:dbr2");
		ourCtx = FhirContext.forDstu2();
	}

	@Test
	public void testCreateAndRead() {
		ourLog.info("Base URL is: " +  HapiProperties.getServerAddress());
		String methodName = "testCreateResourceConditional";

		Patient pt = new Patient();
		pt.addName().addFamily(methodName);
		IIdType id = ourClient.create().resource(pt).execute().getId();

		Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
		assertEquals(methodName, pt2.getName().get(0).getFamily().get(0).getValue());
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
		webAppContext.setDescriptor(path + "/src/main/webapp/WEB-INF/web.xml");
		webAppContext.setResourceBase(path + "/target/hapi-fhir-jpaserver-starter");
		webAppContext.setParentLoaderPriority(true);

		ourServer.setHandler(webAppContext);
		ourServer.start();

		ourPort = JettyUtil.getPortForStartedServer(ourServer);

		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + ourPort + "/hapi-fhir-jpaserver/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		ourClient.registerInterceptor(new LoggingInterceptor(true));
	}

	public static void main(String[] theArgs) throws Exception {
		ourPort = 8080;
		beforeClass();
	}
}
