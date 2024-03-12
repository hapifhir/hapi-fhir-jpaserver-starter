package ch.ahdis.matchbox.engine.tests;

import ch.ahdis.matchbox.engine.MatchboxEngine;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test bench for the 'suppressed warning/information-level issues' feature.
 *
 * @author Quentin Ligier
 **/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SuppressedWarnInfoTests {
	private final MatchboxEngine engine;
	private final byte[] resource;

	private final static String WARN1 = "A measure should contain at least one group";
	private final static String WARN_DOM6 = "Constraint failed: dom-6: 'A resource should have narrative for robust " +
		"management' (defined in http://hl7.org/fhir/StructureDefinition/DomainResource) (Best Practice Recommendation)";

	public SuppressedWarnInfoTests() throws IOException {
		this.engine = this.getEngine();

		this.resource = this.loadSample("measure.xml")
			.replace("{{STATUS}}", "active")
			.getBytes(StandardCharsets.UTF_8);
	}

	@Test
	void testNoSuppressedWarning() throws Exception {
		this.engine.getSuppressedWarnInfoPatterns().clear();
		final var oo = this.engine.validate(new ByteArrayInputStream(this.resource),
														Manager.FhirFormat.XML,
														"http://hl7.org/fhir/StructureDefinition/Measure");

		assertEquals(2, oo.getIssue().size());
		assertTrue(hasWarningWithText(oo, WARN1));
		assertTrue(hasWarningWithText(oo, WARN_DOM6));
	}

	@Test
	void testSuppressedWarningFull() throws Exception {
		this.engine.getSuppressedWarnInfoPatterns().clear();
		this.engine.addSuppressedWarnInfo(WARN1);
		final var oo = this.engine.validate(new ByteArrayInputStream(this.resource),
														Manager.FhirFormat.XML,
														"http://hl7.org/fhir/StructureDefinition/Measure");

		assertEquals(1, oo.getIssue().size());
		assertTrue(hasWarningWithText(oo, WARN_DOM6));
	}

	@Test
	void testSuppressedWarningRegex() throws Exception {
		this.engine.getSuppressedWarnInfoPatterns().clear();
		this.engine.addSuppressedWarnInfoPattern("Constraint failed: dom-6");
		final var oo = this.engine.validate(new ByteArrayInputStream(this.resource),
														Manager.FhirFormat.XML,
														"http://hl7.org/fhir/StructureDefinition/Measure");

		assertEquals(1, oo.getIssue().size());
		assertTrue(hasWarningWithText(oo, WARN1));
	}

	@Test
	void testSuppressAllWarning() throws Exception {
		this.engine.getSuppressedWarnInfoPatterns().clear();
		this.engine.addSuppressedWarnInfoPattern(".+");
		final var oo = this.engine.validate(new ByteArrayInputStream(this.resource),
														Manager.FhirFormat.XML,
														"http://hl7.org/fhir/StructureDefinition/Measure");

		assertEquals(0, oo.getIssue().size());
	}

	private boolean hasWarningWithText(final OperationOutcome oo,
												  final String text) {
		return oo.getIssue().parallelStream().anyMatch(issue -> {
			if (issue.getSeverity() != OperationOutcome.IssueSeverity.WARNING) {
				return false;
			}
			return issue.getDetails().getText().equals(text);
		});
	}

	/**
	 * Initialize a R4 matchbox engine for the tests
	 */
	private MatchboxEngine getEngine() {
		return new MatchboxEngine.MatchboxEngineBuilder().getEngineR4();
	}


	private String loadSample(final String filename) throws IOException {
		return new String(
			R4ValidationTests.class.getResourceAsStream("/r4-samples/" + filename).readAllBytes()
		);
	}
}
