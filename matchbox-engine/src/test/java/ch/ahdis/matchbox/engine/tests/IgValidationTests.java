package ch.ahdis.matchbox.engine.tests;

import ch.ahdis.matchbox.engine.MatchboxEngine;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test bench to load IGs and validate all their examples.
 *
 * @author Quentin Ligier
 **/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IgValidationTests {
	private static final List<String> IGS = List.of("/ch-epr-term.tgz", "/ch-core.tgz", "/ch-emed.tgz");
	private static final Logger log = LoggerFactory.getLogger(IgValidationTests.class);
	private final MatchboxEngine engine;

	public IgValidationTests() throws IOException, URISyntaxException {
		this.engine = this.getEngine();
		for (final String ig : IGS) {
			this.engine.loadPackage(getClass().getResourceAsStream(ig));
		}
		log.info("------- Initialized --------");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("provideResources")
	void testValidate(final String name, final Resource resource) throws Exception {
		if (!resource.getMeta().hasProfile()) {
			Assumptions.abort("No meta.profile found, unable to valide this resource");
		}
		final OperationOutcome outcome = this.engine.validate(resource, resource.getMeta().getProfile().get(0).getValue());

		final List<OperationOutcome.OperationOutcomeIssueComponent> errors = outcome.getIssue().stream()
			.filter(issue -> OperationOutcome.IssueSeverity.FATAL == issue.getSeverity() || OperationOutcome.IssueSeverity.ERROR == issue.getSeverity())
			.collect(Collectors.toList());

		final Map<OperationOutcome.IssueSeverity, Long> count = outcome.getIssue().stream()
			.map(OperationOutcome.OperationOutcomeIssueComponent::getSeverity)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		for (final var c : count.entrySet()) {
			log.info(c.getKey().name() + " " + c.getValue());
		}

		for (final var error : errors) {
			log.error(String.format("[%s][%s] %s",
											error.getSeverity().name(),
											error.getCode().name(),
											error.getDetails().getText()));
		}
		assertTrue(errors.isEmpty());
	}

	public static Stream<Arguments> provideResources() throws Exception {
		List<Arguments> arguments = new ArrayList<>();
		for (final String ig : IGS) {
			Map<String, byte[]> source = fetchByPackage(ig, true);
			for (Map.Entry<String, byte[]> t : source.entrySet()) {
				String fn = t.getKey();
				if (!exemptFile(fn)) {
					Resource r = null;
					if (fn.endsWith(".xml") && !fn.endsWith("template.xml"))
						r = new org.hl7.fhir.r4.formats.XmlParser().parse(new ByteArrayInputStream(t.getValue()));
					else if (fn.endsWith(".json") && !fn.endsWith("template.json"))
						r = new org.hl7.fhir.r4.formats.JsonParser().parse(new ByteArrayInputStream(t.getValue()));
					else if (fn.endsWith(".txt") || fn.endsWith(".map"))
						r = new org.hl7.fhir.r4.utils.StructureMapUtilities(null).parse(new String(t.getValue()), fn);
					else
						throw new Exception("Unsupported format for " + fn);
					if (r != null) {
						arguments.add(Arguments.of(ig + "-" + r.getResourceType() + "-" + r.getId(), r));
					}
				}
			}
		}
		return arguments.stream();
	}


	private static boolean exemptFile(String fn) {
		return Utilities.existsInList(fn, "spec.internals", "version.info", "schematron.zip", "package.json");
	}

	private static Map<String, byte[]> fetchByPackage(String src, boolean examples) throws Exception {
		NpmPackage pi = NpmPackage.fromPackage(IgValidationTests.class.getResourceAsStream(src), null, true);
		return loadPackage(pi, examples);
	}

	public static Map<String, byte[]> loadPackage(NpmPackage pi, boolean examples) throws Exception {
		Map<String, byte[]> res = new HashMap<>();
		if (pi != null) {
			if (examples) {
				for (String s : pi.list("example")) {
					if (process(s)) {
						res.put(s, TextFile.streamToBytes(pi.load("example", s)));
					}
				}
			} else {
				for (String s : pi.list("package")) {
					if (process(s)) {
						res.put(s, TextFile.streamToBytes(pi.load("package", s)));
					}
				}
			}
		}
		return res;
	}

	public static boolean process(String file) {
		if (file == null) {
			return false;
		}
		if ("ig-r4.json".equals(file)) {
			return false;
		}
		if ("package.json".equals(file)) {
			return false;
		}
		if (file.startsWith("ConceptMap-")) {
			return false;
		}
		return true;
	}

	/**
	 * Initialize a R4 matchbox engine with no terminology server.
	 */
	private MatchboxEngine getEngine() throws IOException, URISyntaxException {
		return new MatchboxEngine.MatchboxEngineBuilder().getEngineR4();
	}
}
