package ca.uhn.fhir.jpa.starter.interceptor;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.test.utilities.JettyUtil;

@ExtendWith(OutputCaptureExtension.class)
class CustomLoggingInterceptorTest {

	private static RestfulServer ourServlet;
	private static boolean ourHitMethod;
	private static List<Resource> ourReturn;
	private static int ourPort;
	private static CloseableHttpClient ourClient;
	private static Server ourServer;
	private static final FhirContext ourCtx = FhirContext.forR4();
	private static ObjectMapper mapper = new ObjectMapper();
	
	private static final String PATIENT_ID = "12345";
	private static final String X_CORRELATION_ID = "X-Correlation-Id";
	private static final String X_CORRELATION_ID_VALUE = "123454687544644";

	@BeforeEach
	public void before() throws IOException {
		MockitoAnnotations.openMocks(this);
		ourReturn = null;
		ourHitMethod = false;
	}

	@BeforeAll
	public static void beforeClass() throws Exception {
		ourServer = new Server(0);
		MockPatientResourceProvider patProvider = new MockPatientResourceProvider();
		ServletHandler proxyHandler = new ServletHandler();
		ourServlet = new RestfulServer(ourCtx);
		ourServlet.setFhirContext(ourCtx);
		ourServlet.registerProviders(patProvider);
		ourServlet.setDefaultResponseEncoding(EncodingEnum.JSON);
		ourServlet.registerInterceptor(new LoggingInterceptor());
		ourServlet.registerInterceptor(new CustomLoggingInterceptor());
		ServletHolder servletHolder = new ServletHolder(ourServlet);
		proxyHandler.addServletWithMapping(servletHolder, "/fhir/*");
		ourServer.setHandler(proxyHandler);
		JettyUtil.startServer(ourServer);
		ourPort = JettyUtil.getPortForStartedServer(ourServer);
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(5000, TimeUnit.MILLISECONDS);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(connectionManager);
		ourClient = builder.build();
	}

	@Test
	void testCorrelationIdPresentInRequestHeader(CapturedOutput output) throws Exception {
		ourServlet.registerInterceptor(new CustomLoggingInterceptor());
		
		HttpGet httpGet;
		HttpResponse status;
		
		ourReturn = Collections.singletonList(createPatient(Integer.valueOf(PATIENT_ID)));
		httpGet = new HttpGet("http://localhost:" + ourPort + "/fhir/Patient/" + PATIENT_ID);
		httpGet.setHeader(X_CORRELATION_ID, X_CORRELATION_ID_VALUE);
		status = ourClient.execute(httpGet);
		await().atMost(200, TimeUnit.MILLISECONDS).until(() -> status.containsHeader("X-Correlation-ID"));
		assertEquals(200, status.getStatusLine().getStatusCode());
		assertTrue(ourHitMethod);
		assertTrue(status.containsHeader(X_CORRELATION_ID));
		String ExpectedCorrelationId = getCorrelationIdFromLogs(output);
		assertNotNull(ExpectedCorrelationId);
		assertEquals(X_CORRELATION_ID_VALUE, ExpectedCorrelationId);
	}

	@Test
	void testCorrelationIdNotPresentInRequestHeader(CapturedOutput output) throws Exception {
		ourServlet.registerInterceptor(new CustomLoggingInterceptor());
		
		HttpGet httpGet;
		HttpResponse status;
		
		ourReturn = Collections.singletonList(createPatient(Integer.valueOf(PATIENT_ID)));
		httpGet = new HttpGet("http://localhost:" + ourPort + "/fhir/Patient/" + PATIENT_ID);
		status = ourClient.execute(httpGet);
		await().atMost(200, TimeUnit.MILLISECONDS).until(() -> status.containsHeader(X_CORRELATION_ID));
		assertEquals(200, status.getStatusLine().getStatusCode());
		assertTrue(ourHitMethod);
		assertTrue(status.containsHeader(X_CORRELATION_ID));
		String ExpectedCorrelationId = getCorrelationIdFromLogs(output);
		assertNotNull(ExpectedCorrelationId);
	}

	private String getCorrelationIdFromLogs(CapturedOutput output) throws JsonMappingException, JsonProcessingException {
		String[] logLines = output.getAll().split("\\n");
		JsonNode jsonNode = mapper.readTree(logLines[logLines.length - 1].toString());
		return jsonNode.get(X_CORRELATION_ID).asText();
	}

	private Resource createPatient(Integer theId) {
		Patient retVal = new Patient();
		if (theId != null) {
			retVal.setId(new IdType("Patient", (long) theId));
		}
		retVal.addName().setFamily("FAM");
		return retVal;
	}

	public static class MockPatientResourceProvider implements IResourceProvider {

		@Override
		public Class<? extends IBaseResource> getResourceType() {
			return Patient.class;
		}

		@Read(version = true)
		public Patient read(@IdParam IdType theId) {
			ourHitMethod = true;
			if (ourReturn.isEmpty()) {
				throw new ResourceNotFoundException(theId);
			}
			return (Patient) ourReturn.get(0);
		}
	}
}
