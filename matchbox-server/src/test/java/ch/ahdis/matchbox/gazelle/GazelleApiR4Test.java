package ch.ahdis.matchbox.gazelle;

import ca.uhn.fhir.jpa.starter.Application;
import ch.ahdis.matchbox.validation.gazelle.models.validation.SeverityLevel;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationReport;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationTestResult;
import ch.ahdis.matchbox.test.CompareUtil;
import org.apache.jena.base.Sys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Application.class})
@ActiveProfiles("test-r4")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GazelleApiR4Test extends AbstractGazelleTest {

	private final GazelleClient client = new GazelleClient("http://localhost:8081/matchboxv3/gazelle/");

	@BeforeAll
	void waitUntilStartup() throws Exception {
		Thread.sleep(10000); // give the server some time to start up
		CompareUtil.logMemory();
	}

	@Test
	void testProfiles() throws Exception {
		final var profiles = this.client.getProfiles();
		assertTrue(profiles.size() > 300);
	}

	@Test
	void validatePatientRawR4() throws Exception {
		final String patient = """
			<Patient xmlns="http://hl7.org/fhir">
				<id value="example"/>
				<text>
					<status value="generated"/>
					<div xmlns="http://www.w3.org/1999/xhtml">42 </div>
				</text>
			</Patient>""";

		ValidationReport report = this.client.validate(patient, "http://hl7.org/fhir/StructureDefinition/Patient");
		assertEquals(0, countValidationFailures(report));
		assertEquals(ValidationTestResult.PASSED, report.getOverallResult());
		assertEquals("first", report.getValidationItems().getFirst().getItemId());
		assertTrue(report.getReports().getFirst().getName().contains("first"));
		assertEquals(1, report.getReports().getFirst().getAssertionReports().size());
		assertEquals(ValidationTestResult.PASSED,
						 report.getReports().getFirst().getAssertionReports().getFirst().getResult());
		assertEquals(SeverityLevel.INFO,
						 report.getReports().getFirst().getAssertionReports().getFirst().getSeverity());
		// TODO: why no "first" in the report? No link between the validation item and the assertion report?

		report = this.client.validate(patient, "http://hl7.org/fhir/StructureDefinition/Bundle");
		assertEquals(1, countValidationFailures(report));
		assertEquals(ValidationTestResult.FAILED, report.getOverallResult());
	}

	@Test
	void testSameSessionIdsForSameIg() throws Exception {
		final String patient = """
			<Patient xmlns="http://hl7.org/fhir">
				<id value="example"/>
				<text>
					<status value="generated"/>
					<div xmlns="http://www.w3.org/1999/xhtml">42 </div>
				</text>
			</Patient>""";

		ValidationReport report = this.client.validate(patient, "http://hl7.org/fhir/StructureDefinition/Patient");
		final String sessionId1 = getSessionId(report);

		report = this.client.validate(patient, "http://hl7.org/fhir/StructureDefinition/Bundle");
		final String sessionId2 = getSessionId(report);

		assertEquals(sessionId1, sessionId2);
	}

	@Test
	void verifyIgVersioningGazelle() throws Exception {
		final String resource = """
			<Practitioner xmlns="http://hl7.org/fhir">
				<identifier>
					<system value="urn:oid:2.51.1.3"/>
					<value value="7610000050719"/>
				</identifier>
			</Practitioner>""";

		String profileMatchbox = "http://matchbox.health/ig/test/r4/StructureDefinition/practitioner-identifier-required";

		// validate just with the profile, profile has no business version
		ValidationReport report = this.client.validate(resource, profileMatchbox);
		String sessionIdFirst = getSessionId(report);

		assertEquals(0, countValidationFailures(report));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(report));

		report = this.client.validate(resource, profileMatchbox + "|0.2.0");
		String sessionIdThird = getSessionId(report);
		assertEquals(0, countValidationFailures(report));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(report));
		assertEquals(sessionIdFirst, sessionIdThird);

		// validate with the profile and the ig version, has an internal business version 9.9.9
		profileMatchbox = "http://matchbox.health/ig/test/r4/StructureDefinition/practitioner-identifier-version-different-then-ig";
		report = this.client.validate(resource, profileMatchbox);
		String sessionIdForth = getSessionId(report);
		assertEquals(0, countValidationFailures(report));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(report));
		assertEquals(sessionIdFirst, sessionIdForth);

		report = this.client.validate(resource, profileMatchbox + "|0.2.0");
		String sessionIdFifth = getSessionId(report);
		assertEquals(0, countValidationFailures(report));
		assertEquals("matchbox.health.test.ig.r4#0.2.0", getIg(report));
		assertEquals(sessionIdFirst, sessionIdFifth);
	}

	@Test
	// https://gazelle.ihe.net/jira/browse/EHS-431
	void validateEhs431() throws Exception {
		ValidationReport report = this.client.validate(getContent("ehs-431.json"),
																	  "http://hl7.org/fhir/StructureDefinition/Bundle");
		assertEquals(1, countValidationFailures(report));
	}

	@Test
	// https://gazelle.ihe.net/jira/browse/EHS-419
	void validateEhs419() throws Exception {
		ValidationReport report = this.client.validate(getContent("ehs-419.json"),
																	  "http://hl7.org/fhir/StructureDefinition/Patient");
		assertEquals(0, countValidationFailures(report));
	}

	@Test
	// https://gazelle.ihe.net/jira/browse/EHS-831
	void validateEhs831() throws Exception {
		final ValidationReport report = this.client.validate(getContent("ehs-831.json"),
																			  "http://hl7.org/fhir/StructureDefinition/Parameters");
		assertEquals(ValidationTestResult.PASSED, report.getOverallResult());
		assertEquals(0, countValidationFailures(report));
	}
}
