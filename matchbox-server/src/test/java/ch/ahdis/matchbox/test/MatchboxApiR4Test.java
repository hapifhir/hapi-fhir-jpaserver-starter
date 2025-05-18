package ch.ahdis.matchbox.test;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.jpa.starter.Application;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationItem;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationReport;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.*;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Application.class})
@ActiveProfiles("test-r4")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MatchboxApiR4Test {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxApiR4Test.class);
	private static final String TARGET_SERVER = "http://localhost:8081/matchboxv3";
	private static final FhirContext FHIR_CONTEXT = FhirVersionEnum.R4.newContextCached();

	private final ValidationClient validationClient = new ValidationClient(FHIR_CONTEXT, TARGET_SERVER + "/fhir");
	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeAll
	void waitUntilStartup() throws Exception {
		Thread.sleep(10000); // give the server some time to start up
		this.validationClient.capabilities();
		CompareUtil.logMemory();
	}

	@Test
	void validatePatientRawR4() {

		String patient = """
			<Patient xmlns="http://hl7.org/fhir">
				<id value="example"/>
				<text>
					<status value="generated"/>
					<div xmlns="http://www.w3.org/1999/xhtml">42 </div>
				</text>
			</Patient>""";

		IBaseOperationOutcome operationOutcome = this.validationClient.validate(patient,
																										"http://hl7.org/fhir/StructureDefinition/Patient");
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));

		String sessionIdFirst = getSessionId(operationOutcome);

		operationOutcome = this.validationClient.validate(patient,
																		  "http://hl7.org/fhir/StructureDefinition/Bundle");
		assertEquals(1, getValidationFailures((OperationOutcome) operationOutcome));

		String sessionIdF2nd = getSessionId(operationOutcome);
		assertEquals(sessionIdFirst, sessionIdF2nd);
	}

	@Test
	void verifyCachingImplementationGuides() {
		String resource = """
			<Practitioner xmlns="http://hl7.org/fhir">
				<identifier>
					<system value="urn:oid:2.51.1.3"/>
					<value value="7610000050719"/>
				</identifier>
			</Practitioner>""";

		// tests against base core profile
		String profileCore = "http://hl7.org/fhir/StructureDefinition/Practitioner";
		IBaseOperationOutcome operationOutcome = validationClient.validate(resource, profileCore);
		String sessionIdCore = getSessionId(operationOutcome);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertEquals("hl7.fhir.r4.core#4.0.1", getIg(operationOutcome));
		assertEquals("http://localhost:8081/matchboxv3/fhir", getTxServer(operationOutcome));

		// tests against matchbox r4 test ig
		String profileMatchbox = "http://matchbox.health/ig/test/r4/StructureDefinition/practitioner-identifier-required";
		operationOutcome = validationClient.validate(resource, profileMatchbox);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(operationOutcome));
		String sessionIdMatchbox = getSessionId(operationOutcome);

		// verify that we have have different validation engine
		assertNotEquals(sessionIdCore, sessionIdMatchbox);

		// check that the cached validation engine of core gets used
		operationOutcome = validationClient.validate(resource, profileCore);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		String sessionId2Core = getSessionId(operationOutcome);
		assertEquals(sessionIdCore, sessionId2Core);

		// check that the cached validation engine of matchbox r4 test ig is used again
		operationOutcome = validationClient.validate(resource, profileMatchbox);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		String sessionId2Matchbox = getSessionId(operationOutcome);
		assertEquals(sessionIdMatchbox, sessionId2Matchbox);

		// add new parameters should create a new validation engine for matchbox r4 test ig
		Parameters parameters = new Parameters();
		parameters.addParameter("txServer", "n/a");
		operationOutcome = validationClient.validate(resource, profileMatchbox, parameters);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		String sessionId2MatchboxTxNa = getSessionId(operationOutcome);
		assertNotEquals(sessionIdMatchbox, sessionId2MatchboxTxNa);
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(operationOutcome));
		assertEquals("n/a", getTxServer(operationOutcome));

		// add new parameters should create a new validation engine for default validation
		operationOutcome = validationClient.validate(resource, profileCore, parameters);
		String sessionId3CoreTxNa = getSessionId(operationOutcome);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertNotEquals(sessionIdCore, sessionId3CoreTxNa);
		assertEquals("n/a", getTxServer(operationOutcome));
	}

	@Test
	void verifyIgVersioning() {
		String resource = """
			<Practitioner xmlns="http://hl7.org/fhir">
				<identifier>
					<system value="urn:oid:2.51.1.3"/>
					<value value="7610000050719"/>
				</identifier>
			</Practitioner>""";

		String profileMatchbox = "http://matchbox.health/ig/test/r4/StructureDefinition/practitioner-identifier-required";

		// validate just with the profile, profile has no business version
		IBaseOperationOutcome operationOutcome = validationClient.validate(resource, profileMatchbox);
		String sessionIdFirst = getSessionId(operationOutcome);

		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(operationOutcome));

		// validate with the profile and the ig
		Parameters parameters = new Parameters();
		parameters.addParameter("ig", "matchbox.health.test.ig.r4#0.2.0");
		operationOutcome = validationClient.validate(resource, profileMatchbox, parameters);
		String sessionIdSecond = getSessionId(operationOutcome);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(operationOutcome));
		assertEquals(sessionIdFirst, sessionIdSecond);

		operationOutcome = validationClient.validate(resource, profileMatchbox + "|0.2.0");
		String sessionIdThird = getSessionId(operationOutcome);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(operationOutcome));
		assertEquals(sessionIdFirst, sessionIdThird);

		// validate with the profile and the ig version, has an internal business version 9.9.9
		profileMatchbox = "http://matchbox.health/ig/test/r4/StructureDefinition/practitioner-identifier-version-different-then-ig";
		operationOutcome = validationClient.validate(resource, profileMatchbox);
		String sessionIdForth = getSessionId(operationOutcome);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(operationOutcome));
		assertEquals(sessionIdFirst, sessionIdForth);

		operationOutcome = validationClient.validate(resource, profileMatchbox + "|0.2.0");
		String sessionIdFifth = getSessionId(operationOutcome);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(operationOutcome));
		assertEquals(sessionIdFirst, sessionIdFifth);
	}

	@Test
	void verifyIgVersioningGazelle() throws Exception {
		String resource = """
			<Practitioner xmlns="http://hl7.org/fhir">
				<identifier>
					<system value="urn:oid:2.51.1.3"/>
					<value value="7610000050719"/>
				</identifier>
			</Practitioner>""";

		String profileMatchbox = "http://matchbox.health/ig/test/r4/StructureDefinition/practitioner-identifier-required";

		// validate just with the profile, profile has no business version
		ValidationReport report = this.validateWithGazelle(resource, profileMatchbox);
		String sessionIdFirst = getSessionId(report);

		assertEquals(0, getValidationFailures(report));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(report));

		report = this.validateWithGazelle(resource, profileMatchbox + "|0.2.0");
		String sessionIdThird = getSessionId(report);
		assertEquals(0, getValidationFailures(report));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(report));
		assertEquals(sessionIdFirst, sessionIdThird);

		// validate with the profile and the ig version, has an internal business version 9.9.9
		profileMatchbox = "http://matchbox.health/ig/test/r4/StructureDefinition/practitioner-identifier-version-different-then-ig";
		report = this.validateWithGazelle(resource, profileMatchbox);
		String sessionIdForth = getSessionId(report);
		assertEquals(0, getValidationFailures(report));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(report));
		assertEquals(sessionIdFirst, sessionIdForth);

		report = this.validateWithGazelle(resource, profileMatchbox + "|0.2.0");
		String sessionIdFifth = getSessionId(report);
		assertEquals(0, getValidationFailures(report));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(report));
		assertEquals(sessionIdFirst, sessionIdFifth);
	}


	@Test
		// https://gazelle.ihe.net/jira/browse/EHS-431
	void validateEhs431() throws IOException {
		// IBaseOperationOutcome operationOutcome =
		// validationClient.validate(getContent("ehs-431.json"),
		// "http://fhir.ch/ig/ch-emed/StructureDefinition/ch-emed-document-medicationcard");
		IBaseOperationOutcome operationOutcome = validationClient.validate(getContent("ehs-431.json"),
																								 "http://hl7.org/fhir/StructureDefinition/Bundle");
		log.debug(FHIR_CONTEXT.newJsonParser().encodeResourceToString(operationOutcome));
		assertEquals(1, getValidationFailures((OperationOutcome) operationOutcome));
	}

	@Test
	void validateEhs431Gazelle() throws Exception {
		ValidationReport report = this.validateWithGazelle(getContent("ehs-431.json"),
																			"http://hl7.org/fhir/StructureDefinition/Bundle");
		assertEquals(1, getValidationFailures(report));
	}

	@Test
		// https://gazelle.ihe.net/jira/browse/EHS-419
	void validateEhs419() throws IOException {
		IBaseOperationOutcome operationOutcome = validationClient.validate(getContent("ehs-419.json"),
																								 "http://hl7.org/fhir/StructureDefinition/Patient");
		log.debug(FHIR_CONTEXT.newJsonParser().encodeResourceToString(operationOutcome));
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
	}

	@Test
	void validateIgnoreError() {

		String patient = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
						"<RelatedPerson xmlns=\"http://hl7.org/fhir\">\r\n" + //
						"    <extension url=\"http://hl7.org/fhir/StructureDefinition/patient-citizenship\">\r\n" + //
						"        <extension url=\"code\">\r\n" + //
						"            <valueCodeableConcept>\r\n" + //
						"                <coding>\r\n" + //
						"                    <system value=\"urn:iso:std:iso:3166\"/>\r\n" + //
						"                    <code value=\"CH\"/>\r\n" + //
						"                    <display value=\"Switzerland\"/>\r\n" + //
						"                </coding>\r\n" + //
						"            </valueCodeableConcept>\r\n" + //
						"        </extension>\r\n" + //
						"    </extension>\r\n" + //
						"    <patient>\r\n" + //
						"        <display value=\"Ungeborenes Kind\"/>\r\n" + //
						"    </patient>\r\n" + //
						"</RelatedPerson>";

		IBaseOperationOutcome operationOutcome = this.validationClient.validate(patient,
																										"http://hl7.org/fhir/StructureDefinition/RelatedPerson");
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));


	}

	@Test
	void validateEhs419Gazelle() throws Exception {
		ValidationReport report = this.validateWithGazelle(getContent("ehs-419.json"),
																			"http://hl7.org/fhir/StructureDefinition/Patient");
		assertEquals(0, getValidationFailures(report));
	}

	private String getContent(String resourceName) throws IOException {
		Resource resource = new ClassPathResource(resourceName);
		File file = resource.getFile();
		return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	}

	private ValidationReport validateWithGazelle(final String resource, final String profileId) throws Exception {
		final var gazelleRequestItem = new ValidationItem();
		gazelleRequestItem.setItemId("first");
		gazelleRequestItem.setContent(resource.getBytes(StandardCharsets.UTF_8));
		gazelleRequestItem.setRole("request");
		gazelleRequestItem.setLocation("localhost");

		final var gazelleRequest = new ValidationRequest();
		gazelleRequest.setApiVersion("3.5.3");
		gazelleRequest.setValidationServiceName("Matchbox");
		gazelleRequest.setValidationProfileId(profileId);
		gazelleRequest.addValidationItem(gazelleRequestItem);

		final HttpRequest request = HttpRequest.newBuilder(new URI(TARGET_SERVER + "/gazelle/validation/validate"))
			.POST(HttpRequest.BodyPublishers.ofString(this.objectMapper.writeValueAsString(gazelleRequest)))
			.header("Content-Type", "application/json")
			.build();
		final var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return this.objectMapper.readValue(response.body(), ValidationReport.class);
	}

	private static IBaseExtension<?, ?> getMatchboxValidationExtension(IBaseOperationOutcome theOutcome) {
		if (theOutcome == null) {
			return null;
		}
		RuntimeResourceDefinition ooDef = FHIR_CONTEXT.getResourceDefinition(theOutcome);
		BaseRuntimeChildDefinition issueChild = ooDef.getChildByName("issue");
		List<IBase> issues = issueChild.getAccessor().getValues(theOutcome);
		if (issues.isEmpty()) {
			return null;
		}
		IBase issue = issues.get(0);
		if (issue instanceof IBaseHasExtensions) {
			List<? extends IBaseExtension<?, ?>> extensions = ((IBaseHasExtensions) issue).getExtension();
			for (IBaseExtension<?, ?> nextSource : extensions) {
				if (nextSource.getUrl().equals("http://matchbox.health/validation")) {
					return nextSource;
				}
			}
		}
		return null;
	}

	private static String getSessionId(IBaseOperationOutcome outcome) {
		IBaseExtension<?, ?> ext = getMatchboxValidationExtension(outcome);
		List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
		for (IBaseExtension<?, ?> next : extensions) {
			if (next.getUrl().equals("sessionId")) {
				IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
				return value.getValueAsString();
			}
		}
		return null;
	}

	private static String getIg(IBaseOperationOutcome outcome) {
		IBaseExtension<?, ?> ext = getMatchboxValidationExtension(outcome);
		List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
		for (IBaseExtension<?, ?> next : extensions) {
			if (next.getUrl().equals("ig")) {
				IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
				return value.getValueAsString();
			}
		}
		return null;
	}

	private static String getSessionId(final ValidationReport report) {
		for (final var metadata : report.getAdditionalMetadata()) {
			if ("sessionId".equals(metadata.getName())) {
				return metadata.getValue();
			}
		}
		return null;
	}

	private static String getIg(final ValidationReport report) {
		for (final var metadata : report.getAdditionalMetadata()) {
			if ("ig".equals(metadata.getName())) {
				return metadata.getValue();
			}
		}
		return null;
	}

	private static String getTxServer(IBaseOperationOutcome outcome) {
		IBaseExtension<?, ?> ext = getMatchboxValidationExtension(outcome);
		List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
		for (IBaseExtension<?, ?> next : extensions) {
			if (next.getUrl().equals("txServer")) {
				IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
				return value.getValueAsString();
			}
		}
		return null;
	}

	private static int getValidationFailures(OperationOutcome outcome) {
		int fails = 0;
		if (outcome != null && outcome.getIssue() != null) {
			for (OperationOutcomeIssueComponent issue : outcome.getIssue()) {
				if (IssueSeverity.FATAL == issue.getSeverity()) {
					++fails;
				}
				if (IssueSeverity.ERROR == issue.getSeverity()) {
					++fails;
				}
			}
		}
		return fails;
	}

	private static int getValidationFailures(final ValidationReport report) {
		return report.getCounters().getNumberOfFailedWithErrors() + report.getCounters().getNumberOfUnexpectedErrors();
	}
}
