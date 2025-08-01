package ch.ahdis.matchbox.engine.tests;

import ch.ahdis.matchbox.engine.MatchboxEngine;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test bench to test the validation of R5 resources.
 *
 * @author Quentin Ligier
 **/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class R5ValidationTests {
	private static final Logger log = LoggerFactory.getLogger(R5ValidationTests.class);
	private final MatchboxEngine engine;
	private final String careplanRaw;
	private final String measureRaw;

	public R5ValidationTests() throws IOException, URISyntaxException {
		this.engine = this.getEngine();
		//this.engine.setTerminologyServer("http://tx.fhir.org", null, FhirPublication.R4);
		this.careplanRaw = this.loadSample("careplan.xml");
		this.measureRaw = this.loadSample("measure.xml");
	}

	/**
	 * Test the validation of a code from a value set defined with simple includes.
	 *
	 * http://hl7.org/fhir/R4/careplan.html
	 * http://hl7.org/fhir/R4/valueset-care-plan-intent.html
	 */
	@Test
	void testValueSetWithSimpleInclude() throws Exception {
		final String validCareplan = this.careplanRaw.replace("{{INTENT}}", "plan");
		this.expectValid(validCareplan, Manager.FhirFormat.XML, "http://hl7.org/fhir/StructureDefinition/CarePlan");

		final String invalidCareplan = this.careplanRaw.replace("{{INTENT}}", "non-existent-code");
		final var errors = this.expectInvalid(invalidCareplan, Manager.FhirFormat.XML, "http://hl7" +
			".org/fhir/StructureDefinition/CarePlan");
		assertEquals(2, errors.size());
		assertTrue(errors.get(0).getDetails().getText().startsWith("The value provided ('non-existent-code') was not found in the value set 'Care Plan Intent' (http://hl7.org/fhir/ValueSet/care-plan-intent|5.0.0)") || errors.get(1).getDetails().getText().startsWith("The value provided ('non-existent-code') was not found in the value set 'Care Plan Intent' (http://hl7.org/fhir/ValueSet/care-plan-intent|5.0.0)") );
	}

	/**
	 * Test the validation of a code from a value set defined with all the codes from a code system.
	 *
	 * http://hl7.org/fhir/R4/measure.html
	 * http://hl7.org/fhir/R4/valueset-publication-status.html
	 */
	@Test
	void testValueSetWithAllFromCodeSystem() throws Exception {
		final String validMeasure = this.measureRaw.replace("{{STATUS}}", "active");
		this.expectValid(validMeasure, Manager.FhirFormat.XML, "http://hl7.org/fhir/StructureDefinition/Measure");

		final String invalidMeasure = this.measureRaw.replace("{{STATUS}}", "non-existent-code");
		final var errors = this.expectInvalid(invalidMeasure, Manager.FhirFormat.XML, "http://hl7.org/fhir/StructureDefinition/Measure");
		assertEquals(2, errors.size());
		assertTrue(errors.get(0).getDetails().getText().startsWith("The value provided ('non-existent-code') was not found in the value set 'PublicationStatus'") || errors.get(1).getDetails().getText().startsWith("The value provided ('non-existent-code') was not found in the value set 'PublicationStatus'"));
	}
	
	/**
	 * Test the validation of a code from a value set that expands urn:ietf:bcp:13.
	 *
	 * http://hl7.org/fhir/R4/binary.html
	 * http://hl7.org/fhir/R4/valueset-mimetypes.html
	 */
	@Test
//	@Disabled(value = "No offline expansion yet")
	void testCodeCorrect() throws Exception {
		final String validBinary = this.loadSample("code-correct.xml");
		this.expectValid(validBinary, Manager.FhirFormat.XML, "http://hl7.org/fhir/StructureDefinition/Basic");
	}

	/**
	 * Test the validation of a code from a value set that expands urn:ietf:bcp:13.
	 *
	 * Disabled: the offline validator isn't able to expand urn:ietf:bcp:13.
	 *
	 * http://hl7.org/fhir/R4/binary.html
	 * http://hl7.org/fhir/R4/valueset-mimetypes.html
	 */
	@Test
	@Disabled(value = "No offline expansion yet")
	void testValueSetWithIetfBcp13Expansion() throws Exception {
		final String binaryRaw = this.loadSample("binary.xml");

		final String validBinary = binaryRaw.replace("{{CONTENTTYPE}}", "application/pdf");
		this.expectValid(validBinary, Manager.FhirFormat.XML, "http://hl7.org/fhir/StructureDefinition/Binary");
	}

	/**
	 * Test the validation of a UCUM code.
	 */
	@Test
//	@Disabled(value = "No offline expansion yet")
	void testValueSetWithUcumCodes() throws Exception {
		final String observationRaw = this.loadSample("observation.xml");

		final String validObservation = observationRaw.replace("{{UNIT}}", "min");
		this.expectValid(validObservation, Manager.FhirFormat.XML, "http://hl7.org/fhir/StructureDefinition/Observation");

		//final String invalidObservation = observationRaw.replace("{{UNIT}}", "non-existent-code");
		//final var errors = this.expectInvalid(invalidObservation, Manager.FhirFormat.XML, "http://hl7.org/fhir/StructureDefinition/Observation");
		//assertEquals(1, errors.size());
	}

	/**
	 * Todo: test the validation of a code from a value set defined with 'concept is-a' filter from SNOMED CT.
	 */

	/**
	 * Todo: test the validation of a code from a value set defined with 'canonical =' filter from UCUM.
	 */

	List<OperationOutcome.OperationOutcomeIssueComponent> getValidationFailures(final OperationOutcome outcome) {
		return outcome.getIssue().stream()
			.filter(issue -> OperationOutcome.IssueSeverity.FATAL == issue.getSeverity() || OperationOutcome.IssueSeverity.ERROR == issue.getSeverity())
			.collect(Collectors.toList());
	}

	/**
	 * Initialize a R5 matchbox engine for the tests
	 */
	private MatchboxEngine getEngine() {
		MatchboxEngine.MatchboxEngineBuilder builder = new MatchboxEngine.MatchboxEngineBuilder();
		return builder.getEngineR5();
	}

	private void expectValid(final String resource,
									 final Manager.FhirFormat format,
									 final String profile) throws EOperationOutcome, IOException {
		final var response = this.engine.validate(new ByteArrayInputStream(resource.getBytes(StandardCharsets.UTF_8)),
																format,
																profile);
		final var errors = getValidationFailures(response);
		for (final var error : errors) {
			log.error(String.format("[%s][%s] %s",
											error.getSeverity().name(),
											error.getCode().name(),
											error.getDetails().getText()));
		}
		assertEquals(0, errors.size());
	}

	private List<OperationOutcome.OperationOutcomeIssueComponent> expectInvalid(final String resource,
																										 final Manager.FhirFormat format,
																										 final String profile) throws EOperationOutcome, IOException {
		final var response = this.engine.validate(new ByteArrayInputStream(resource.getBytes(StandardCharsets.UTF_8)),
																format,
																profile);
		final var errors = getValidationFailures(response);
		assertNotEquals(0, errors.size());
		return errors;
	}

	private String loadSample(final String filename) throws IOException {
		return new String(
			R5ValidationTests.class.getResourceAsStream("/r4-samples/" + filename).readAllBytes()
		);
	}
}
