package ch.ahdis.matchbox.test;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.jpa.starter.Application;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.*;
import org.hl7.fhir.r4b.model.OperationOutcome;
import org.hl7.fhir.r4b.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4b.model.OperationOutcome.OperationOutcomeIssueComponent;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT,
	properties = {
		"hapi.fhir.implementationguides.fhir_r4_core.name=",
		"hapi.fhir.implementationguides.fhir_terminology.name=",
		"hapi.fhir.implementationguides.fhir_extensions.name="}) // Unset R4 IGs
@ContextConfiguration(classes = {Application.class})
@ActiveProfiles("test-r4b")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MatchboxApiR4BTest {

	static public int getValidationFailures(OperationOutcome outcome) {
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

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxApiR4BTest.class);

	private String targetServer = "http://localhost:8083/matchboxv3/fhir";

	private final FhirContext context = FhirContext.R4B.newContextCached();
	
	@BeforeAll
	void waitUntilStartup() throws Exception {
		Thread.sleep(10000); // give the server some time to start up
		ValidationClient validationClient = new ValidationClient(this.context, this.targetServer);
		validationClient.capabilities();
		CompareUtil.logMemory();
	}

	private static IBaseExtension<?, ?> getMatchboxValidationExtension(FhirContext theCtx,
																							 IBaseOperationOutcome theOutcome) {
		if (theOutcome == null) {
			return null;
		}
		RuntimeResourceDefinition ooDef = theCtx.getResourceDefinition(theOutcome);
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

	public String getSessionId(FhirContext ctx, IBaseOperationOutcome outcome) {
		IBaseExtension<?, ?> ext = getMatchboxValidationExtension(ctx, outcome);
		List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
		for (IBaseExtension<?, ?> next : extensions) {
			if (next.getUrl().equals("sessionId")) {
				IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
				return value.getValueAsString();
			}
		}
		return null;
	}

	public String getIg(FhirContext ctx, IBaseOperationOutcome outcome) {
		IBaseExtension<?, ?> ext = getMatchboxValidationExtension(ctx, outcome);
		List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
		for (IBaseExtension<?, ?> next : extensions) {
			if (next.getUrl().equals("ig")) {
				IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
				return value.getValueAsString();
			}
		}
		return null;
	}

	public String getTxServer(FhirContext ctx, IBaseOperationOutcome outcome) {
		IBaseExtension<?, ?> ext = getMatchboxValidationExtension(ctx, outcome);
		List<IBaseExtension<?, ?>> extensions = (List<IBaseExtension<?, ?>>) ext.getExtension();
		for (IBaseExtension<?, ?> next : extensions) {
			if (next.getUrl().equals("txServer")) {
				IPrimitiveType<?> value = (IPrimitiveType<?>) next.getValue();
				return value.getValueAsString();
			}
		}
		return null;
	}

	@Test
	public void validatePatientRawR4B() {
		ValidationClient validationClient = new ValidationClient(this.context, this.targetServer);

		String patient = "<Patient xmlns=\"http://hl7.org/fhir\">\n" + "            <id value=\"example\"/>\n"
			+ "            <text>\n" + "               <status value=\"generated\"/>\n"
			+ "               <div xmlns=\"http://www.w3.org/1999/xhtml\">42 </div>\n" + "            </text>\n"
			+ "         </Patient>\n";

		IBaseOperationOutcome operationOutcome = validationClient.validate(patient,
																								 "http://hl7.org/fhir/StructureDefinition/Patient");
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));

		String sessionIdFirst = getSessionId(this.context, operationOutcome);

		operationOutcome = validationClient.validate(patient,
																	"http://hl7.org/fhir/StructureDefinition/Bundle");
		assertEquals(1, getValidationFailures((OperationOutcome) operationOutcome));

		String sessionIdF2nd = getSessionId(this.context, operationOutcome);
		assertEquals(sessionIdFirst, sessionIdF2nd);
	}

	@Test
	public void verifyCachingImplementationGuides() {
		ValidationClient validationClient = new ValidationClient(this.context, this.targetServer);

		String resource = "<Practitioner xmlns=\"http://hl7.org/fhir\">\n" + //
			"  <identifier>\n" + //
			"    <system value=\"urn:oid:2.51.1.3\"/>\n" + //
			"    <value value=\"7610000050719\"/>\n" + //
			"  </identifier>\n" + //
			"</Practitioner>";

		// tests against base core profile
		String profileCore = "http://hl7.org/fhir/StructureDefinition/Practitioner";
		IBaseOperationOutcome operationOutcome = validationClient.validate(resource, profileCore);
		String sessionIdCore = getSessionId(this.context, operationOutcome);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		assertEquals("hl7.fhir.r4b.core#4.3.0", getIg(this.context, operationOutcome));
		assertEquals(this.targetServer, this.getTxServer(this.context, operationOutcome));

		// check that the cached validation engine of core gets used
		operationOutcome = validationClient.validate(resource, profileCore);
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
		String sessionId2Core = getSessionId(this.context, operationOutcome);
		assertEquals(sessionIdCore, sessionId2Core);
	}

	@Test
	// https://gazelle.ihe.net/jira/browse/EHS-431
	public void validateEhs431() throws IOException {
		//
		ValidationClient validationClient = new ValidationClient(this.context, this.targetServer);

		validationClient.capabilities();

		// IBaseOperationOutcome operationOutcome =
		// validationClient.validate(getContent("ehs-431.json"),
		// "http://fhir.ch/ig/ch-emed/StructureDefinition/ch-emed-document-medicationcard");
		IBaseOperationOutcome operationOutcome = validationClient.validate(getContent("ehs-431.json"),
																								 "http://hl7.org/fhir/StructureDefinition/Bundle");
		log.debug(this.context.newJsonParser().encodeResourceToString(operationOutcome));
		assertEquals(1, getValidationFailures((OperationOutcome) operationOutcome));
	}

	@Test
	// https://gazelle.ihe.net/jira/browse/EHS-419
	public void validateEhs419() throws IOException {
		//
		ValidationClient validationClient = new ValidationClient(this.context, this.targetServer);

		validationClient.capabilities();

		IBaseOperationOutcome operationOutcome = validationClient.validate(getContent("ehs-419.json"),
																								 "http://hl7.org/fhir/StructureDefinition/Patient");
		log.debug(this.context.newJsonParser().encodeResourceToString(operationOutcome));
		assertEquals(0, getValidationFailures((OperationOutcome) operationOutcome));
	}

	private String getContent(String resourceName) throws IOException {
		Resource resource = new ClassPathResource(resourceName);
		File file = resource.getFile();
		return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	}

}
