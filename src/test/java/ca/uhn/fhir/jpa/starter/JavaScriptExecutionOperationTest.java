package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {Application.class},
		properties = {
			"spring.datasource.url=jdbc:h2:mem:dbr4-js",
			"spring.ai.mcp.server.enabled=false",
			"hapi.fhir.cr_enabled=false",
			"hapi.fhir.fhir_version=r4",
			"hapi.fhir.javascript_execution_enabled=true",
			// Scripts live under src/test/resources/javascript; reference it absolutely.
			"hapi.fhir.javascript_execution_scripts_dir=${user.dir}/src/test/resources/javascript",
			// Keep the timeout short so the infinite-loop test finishes quickly.
			"hapi.fhir.javascript_execution_timeout_seconds=3"
		})
class JavaScriptExecutionOperationTest {

	@LocalServerPort
	private int port;

	private IGenericClient client;
	private FhirContext ctx;

	@BeforeEach
	void setUp() {
		ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ctx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		client = ctx.newRestfulGenericClient("http://localhost:" + port + "/fhir/");
	}

	private Parameters execute(String scriptName, org.hl7.fhir.r4.model.Resource... resources) {
		Parameters in = new Parameters();
		in.addParameter().setName("script").setValue(new StringType(scriptName));
		for (org.hl7.fhir.r4.model.Resource resource : resources) {
			in.addParameter().setName("resource").setResource(resource);
		}
		return client.operation()
				.onServer()
				.named("$execute-javascript")
				.withParameters(in)
				.execute();
	}

	private Patient patient(String family) {
		Patient p = new Patient();
		p.addName().setFamily(family);
		return p;
	}

	@Test
	void transformsBothInputResources() {
		Patient a = patient("A");
		a.setActive(false);
		Patient b = patient("B");
		b.setActive(false);

		Parameters out = execute("set-active", a, b);

		List<Parameters.ParametersParameterComponent> returns = out.getParameter();
		assertEquals(2, returns.size());
		assertEquals("return", returns.get(0).getName());
		assertTrue(assertInstanceOf(Patient.class, returns.get(0).getResource()).getActive());
		assertTrue(assertInstanceOf(Patient.class, returns.get(1).getResource()).getActive());
	}

	@Test
	void mergesTwoResourcesIntoOne() {
		// A script can read both inputs (input[0], input[1]) and emit a single combined resource.
		Patient first = patient("First");
		Patient second = patient("Second");

		Parameters out = execute("merge", first, second);

		assertEquals(1, out.getParameter().size());
		Patient merged = (Patient) out.getParameter().get(0).getResource();
		assertEquals("First", merged.getName().get(0).getFamily());
		assertEquals("Second", merged.getName().get(1).getFamily());
	}

	@Test
	void acceptsASingleObjectResult() {
		Parameters out = execute("set-gender", patient("A"), patient("B"));

		assertEquals(1, out.getParameter().size());
		Patient result = (Patient) out.getParameter().get(0).getResource();
		assertEquals(Enumerations.AdministrativeGender.FEMALE, result.getGender());
	}

	@Test
	void acceptsScriptNameWithJsSuffix() {
		Parameters out = execute("set-gender.js", patient("A"), patient("B"));
		assertEquals(1, out.getParameter().size());
	}

	@Test
	void canSynthesizeANewResource() {
		Parameters out = execute("synthesize", patient("A"), patient("B"));

		assertEquals(1, out.getParameter().size());
		Observation obs = assertInstanceOf(Observation.class, out.getParameter().get(0).getResource());
		assertEquals(Observation.ObservationStatus.FINAL, obs.getStatus());
	}

	@Test
	void emptyResultProducesEmptyParameters() {
		Parameters out = execute("empty", patient("A"), patient("B"));
		assertTrue(out.getParameter().isEmpty());
	}

	@Test
	void resolvesLiteralReferencesBeforeExecution() {
		// Store a resource on the server, then reference it (rather than inlining it).
		Patient stored = patient("Stored");
		stored.setActive(false);
		IIdType id = client.create().resource(stored).execute().getId().toUnqualifiedVersionless();

		Parameters in = new Parameters();
		in.addParameter().setName("script").setValue(new StringType("set-active"));
		in.addParameter().setName("reference").setValue(new Reference(id.getValue()));

		Parameters out = client.operation()
				.onServer()
				.named("$execute-javascript")
				.withParameters(in)
				.execute();

		assertEquals(1, out.getParameter().size());
		Patient result = (Patient) out.getParameter().get(0).getResource();
		assertEquals("Stored", result.getNameFirstRep().getFamily());
		assertTrue(result.getActive());
	}

	@Test
	void mixesInlineResourcesAndReferences() {
		Patient stored = patient("FromServer");
		IIdType id = client.create().resource(stored).execute().getId().toUnqualifiedVersionless();

		Parameters in = new Parameters();
		in.addParameter().setName("script").setValue(new StringType("merge")); // uses input[0] + input[1]
		in.addParameter().setName("resource").setResource(patient("Inline")); // input[0]
		in.addParameter().setName("reference").setValue(new Reference(id.getValue())); // input[1]

		Parameters out = client.operation()
				.onServer()
				.named("$execute-javascript")
				.withParameters(in)
				.execute();

		assertEquals(1, out.getParameter().size());
		Patient merged = (Patient) out.getParameter().get(0).getResource();
		assertEquals("Inline", merged.getName().get(0).getFamily());
		assertEquals("FromServer", merged.getName().get(1).getFamily());
	}

	@Test
	void unresolvableReferenceIsRejected() {
		Parameters in = new Parameters();
		in.addParameter().setName("script").setValue(new StringType("set-active"));
		in.addParameter().setName("reference").setValue(new Reference("Patient/does-not-exist"));

		assertThrows(
				ResourceNotFoundException.class,
				() -> client.operation()
						.onServer()
						.named("$execute-javascript")
						.withParameters(in)
						.execute());
	}

	@Test
	void acceptsZeroResources() {
		// 'input' is simply an empty array; a script can ignore it and synthesize output.
		Parameters out = execute("synthesize");
		assertEquals(1, out.getParameter().size());
		assertInstanceOf(Observation.class, out.getParameter().get(0).getResource());
	}

	@Test
	void scriptErrorIsReportedAsBadRequest() {
		assertThrows(InvalidRequestException.class, () -> execute("error", patient("A"), patient("B")));
	}

	@Test
	void nonResourceResultIsRejected() {
		assertThrows(InvalidRequestException.class, () -> execute("non-resource", patient("A"), patient("B")));
	}

	@Test
	void longRunningScriptIsStoppedByTimeout() {
		// The 3s timeout configured above must fire and abort the infinite loop.
		long start = System.nanoTime();
		assertThrows(InvalidRequestException.class, () -> execute("infinite-loop", patient("A"), patient("B")));
		long elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000L;
		// Should fail soon after the 3s budget, not hang indefinitely.
		assertTrue(elapsedSeconds < 20, "Execution was not stopped promptly; took " + elapsedSeconds + "s");
	}

	@Test
	void unknownScriptIsRejected() {
		assertThrows(InvalidRequestException.class, () -> execute("does-not-exist", patient("A"), patient("B")));
	}

	@Test
	void pathTraversalIsRejected() {
		assertThrows(
				InvalidRequestException.class,
				() -> execute("../application", patient("A"), patient("B")));
	}

	@Test
	void javaAccessIsBlockedBySandbox() {
		// The ClassFilter denies all Java classes, so Java.type(...) must fail rather than
		// giving the script a handle on the host JVM.
		assertThrows(InvalidRequestException.class, () -> execute("java-access", patient("A"), patient("B")));
	}
}
