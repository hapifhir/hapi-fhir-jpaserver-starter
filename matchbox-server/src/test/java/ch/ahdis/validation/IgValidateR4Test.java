package ch.ahdis.validation;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.packages.loader.PackageLoaderSvc;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.Application;
import ch.ahdis.matchbox.util.PackageCacheInitializer;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * see https://www.baeldung.com/springjunit4classrunner-parameterized read the implementation guides defined in ig and
 * execute the validations
 *
 * @author oliveregger
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Application.class})
@ActiveProfiles("test-ch")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IgValidateR4Test {

	private static final String TARGET_SERVER = "http://localhost:8082/matchboxv3/fhir";
	private static final Logger log = LoggerFactory.getLogger(IgValidateR4Test.class);
	@Autowired
	ApplicationContext context;
	private ValidationClient validationClient;

	static public int getValidationFailures(OperationOutcome outcome) {
		int fails = 0;
		if (outcome != null && outcome.getIssue() != null) {
			for (OperationOutcome.OperationOutcomeIssueComponent issue : outcome.getIssue()) {
				if (OperationOutcome.IssueSeverity.FATAL == issue.getSeverity()) {
					++fails;
				}
				if (OperationOutcome.IssueSeverity.ERROR == issue.getSeverity()) {
					++fails;
				}
			}
		}
		return fails;
	}

	@BeforeAll
	public synchronized void beforeAll() throws Exception {
		Path dir = Paths.get("database");
		if (Files.exists(dir)) {
			for (Path file : Files.list(dir).collect(Collectors.toList())) {
				if (Files.isRegularFile(file)) {
					Files.delete(file);
				}
			}
		}
		Thread.sleep(40000); // give the server some time to start up
		FhirContext contextR4 = FhirVersionEnum.R4.newContext();
		this.validationClient = new ValidationClient(contextR4, TARGET_SERVER);
		this.validationClient.capabilities();
	}

	public Stream<Arguments> provideResources() throws Exception {

		Map<String, Object> obj = new Yaml().load(getClass().getResourceAsStream("/application-test-ch.yaml"));
		final List<AppProperties.ImplementationGuide> igs = PackageCacheInitializer.getIgs(obj, true);
		List<Arguments> arguments = new ArrayList<>();
		for (AppProperties.ImplementationGuide ig : igs) {
			Map<String, byte[]> source = fetchByPackage(ig, true);
			String version = "4.0.1";
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
						arguments.add(Arguments.of(ig.getName() + "-" + r.getResourceType() + "-" + r.getId(), r));
					}
				}
			}
		}
		return arguments.stream();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("provideResources")
	public void testValidate(String name, Resource resource) throws Exception {
		OperationOutcome outcome = doValidate(name, resource);
		int fails = getValidationFailures(outcome);
		if (fails > 0) {
			log.error("failing " + name);
			for (final var issue : outcome.getIssue()) {
				log.debug(String.format("  [%s][%s] %s",
												issue.getSeverity().name(),
												issue.getCode().name(),
												issue.getDiagnostics()));
			}
			//log.debug(contextR4.newJsonParser().encodeResourceToString(resource));
			//log.debug(contextR4.newJsonParser().encodeResourceToString(outcome));
		}
		assertEquals(0, fails);
	}

	public OperationOutcome doValidate(String name, Resource resource) throws IOException {
		log.debug("validating resource " + resource.getId() + " with " + TARGET_SERVER);
		FhirContext contextR4 = FhirVersionEnum.R4.newContext();

		if (!resource.getMeta().hasProfile()) {
			Assumptions.abort("No meta.profile found, unable to valide this resource");
		}

		boolean skip = "ch.fhir.ig.ch-core#1.0.0-PractitionerRole-HPWengerRole".equals(name); // wrong value inside
		skip = skip || "ch.fhir.ig.ch-epr-mhealth#0.1.2-Bundle-2-7-BundleProvideDocument".equals(name); // error in testcase, however cannot reproduce yet directly ???
		if (skip) {
			Assumptions.abort("Ignoring validation for " + name);
		}

		String content = new org.hl7.fhir.r4.formats.JsonParser().composeString(resource);
		OperationOutcome outcome = (OperationOutcome) this.validationClient.validate(content,
																											  resource.getMeta().getProfile().get(0).getValue());

		if (outcome == null) {
			log.debug(contextR4.newXmlParser().encodeResourceToString(resource));
			log.error("should have a return element");
		} else {
			if (getValidationFailures(outcome) > 0) {
				log.debug(contextR4.newXmlParser().encodeResourceToString(resource));
				log.debug("Validation Errors " + getValidationFailures(outcome));
				log.error(contextR4.newXmlParser().encodeResourceToString(outcome));
			}
		}

		return outcome;
	}

	static private boolean exemptFile(String fn) {
		return Utilities.existsInList(fn, "spec.internals", "version.info", "schematron.zip", "package.json");
	}

	static private Map<String, byte[]> fetchByPackage(AppProperties.ImplementationGuide src, boolean examples) throws Exception {
		String thePackageUrl = src.getUrl();
		PackageLoaderSvc loader = new PackageLoaderSvc();
		InputStream inputStream = new ByteArrayInputStream(loader.loadPackageUrlContents(thePackageUrl));
		NpmPackage pi = NpmPackage.fromPackage(inputStream, null, true);
		return loadPackage(pi, examples);
	}

	static public Map<String, byte[]> loadPackage(NpmPackage pi, boolean examples) throws Exception {
		Map<String, byte[]> res = new HashMap<String, byte[]>();
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

	static public boolean process(String file) {
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
}
