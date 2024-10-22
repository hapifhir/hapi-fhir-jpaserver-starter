package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class}, properties = {
	"spring.datasource.url=jdbc:h2:mem:dbr4",
	"hapi.fhir.fhir_version=r4",
	"hapi.fhir.userRequestRetryVersionConflictsInterceptorEnabled=true"
})

/**
 * This class tests running parallel updates to a single resource with and without setting the 'X-Retry-On-Version-Conflict' header
 * to ensure we get the expected behavior of detecting conflicts
 */
public class ParallelUpdatesVersionConflictTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ParallelUpdatesVersionConflictTest.class);

	@LocalServerPort
	private int port;

	private IGenericClient client;
	private FhirContext ctx;

	@BeforeEach
	void setUp() {
		ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ctx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		client = ctx.newRestfulGenericClient(ourServerBase);
	}

	@Test
	void testParallelResourceUpdateBundle() throws Throwable {
		//send 10 bundles with updates to the patient in parallel, except the header to deconflict them
		Patient pat = new Patient();
		String patId = client.create().resource(pat).execute().getId().getIdPart();
		launchThreads(patId, true, "X-Retry-On-Version-Conflict");
	}

	@Test
	void testParallelResourceUpdateNoBundle() throws Throwable {
		//send 10 resource puts  to the patient in parallel, except the header to deconflict them
		Patient pat = new Patient();
		String patId = client.create().resource(pat).execute().getId().getIdPart();
		launchThreads(patId, false, "X-Retry-On-Version-Conflict");
	}

	@Test
	void testParallelResourceUpdateBundleExpectConflict() {
		//send 10 bundles with updates to the patient in parallel, expect a ResourceVersionConflictException since we are not setting the retry header
		Patient pat = new Patient();
		String patId = client.create().resource(pat).execute().getId().getIdPart();
		ResourceVersionConflictException exception = assertThrows(ResourceVersionConflictException.class, () ->
			launchThreads(patId, true, "someotherheader"));
	}

	@Test
	void testParallelResourceUpdateNoBundleExpectConflict() {
		//send 10 resource puts  to the patient in parallel, expect a ResourceVersionConflictException since we are not setting the retry header
		Patient pat = new Patient();
		String patId = client.create().resource(pat).execute().getId().getIdPart();
		ResourceVersionConflictException exception = assertThrows(ResourceVersionConflictException.class, () ->
			launchThreads(patId, false, "someotherheader"));
	}

	private void launchThreads(String patientId, boolean useBundles, String headerName) throws Throwable {
		int threadCnt = 10;
		ExecutorService execSvc = Executors.newFixedThreadPool(threadCnt);

		//launch a bunch of threads at the same time that update the same patient
		List<Callable<Integer>> callables = new ArrayList<>();
		for (int i = 0; i < threadCnt; i++) {
			final int cnt = i;
			Callable<Integer> callable = new Callable<>() {
				@Override
				public Integer call() throws Exception {
					Patient pat = new Patient();
					//make sure to change something so the server doesnt short circuit on a no-op
					pat.addName().setFamily("fam-" + cnt);
					pat.setId(patientId);

					if( useBundles) {
						Bundle b = new Bundle();
						b.setType(BundleType.TRANSACTION);
						BundleEntryComponent bec = b.addEntry();
						bec.setResource(pat);
						//bec.setFullUrl("Patient/" + patId);
						Bundle.BundleEntryRequestComponent req = bec.getRequest();
						req.setUrl("Patient/" + patientId);
						req.setMethod(HTTPVerb.PUT);
						bec.setRequest(req);

						Bundle returnBundle = client.transaction().withBundle(b)
							.withAdditionalHeader(headerName, "retry; max-retries=10")
							.execute();

						String statusString = returnBundle.getEntryFirstRep().getResponse().getStatus();
						ourLog.trace("statusString->{}", statusString);
						try {
							return Integer.parseInt(statusString.substring(0,3));
						}catch(NumberFormatException nfe) {
							return 500;
						}
					}
					else {
						MethodOutcome outcome = client.update().resource(pat).withId(patientId)
							.withAdditionalHeader(headerName, "retry; max-retries=10")
							.execute();
						ourLog.trace("updated patient: " + outcome.getResponseStatusCode());
						return outcome.getResponseStatusCode();
					}
				}
			};
			callables.add(callable);
		}

		List<Future<Integer>> futures = new ArrayList<>();

		//launch them all at once
		for (Callable<Integer> callable : callables) {
			futures.add(execSvc.submit(callable));
		}

		//wait for calls to complete
		for (Future<Integer> future : futures) {
			try {
				Integer httpResponseCode = future.get();
				Assertions.assertEquals(200, httpResponseCode);
			} catch (InterruptedException | ExecutionException e) {
				//throw the ResourceVersionConflictException back up so we can test it
				throw e.getCause();
			}
		}
	}
}
