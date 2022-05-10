package ch.ahdis.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.starter.Application;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Rud
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Application.class})
@ActiveProfiles("test1")
@Slf4j
public class CapabilityStatementTests {

	private final FhirContext contextR4 = FhirVersionEnum.R4.newContext();
	private final GenericFhirClient client = new GenericFhirClient(contextR4);

	@Autowired
	private MatchboxPackageInstallerImpl packageInstallerSvc;

	@BeforeClass
	public static void beforeClass() throws Exception {
		Path dir = Paths.get("database");
		for (Path file : Files.list(dir).collect(Collectors.toList())) {
			if (Files.isRegularFile(file)) {
				Files.delete(file);
			}
		}	
	}

	/**
	 * Test that run-time loading of Implementation Guides
	 * accordingly updates the CapabilityStatement.
	 * See <a href="https://github.com/ahdis/matchbox/issues/43">issue 43</a>.
	 */
	@Test
	public void testCapabilityStatementDynamicity() throws Exception {
		PackageInstallationSpec packageSpec = new PackageInstallationSpec()
			.setPackageUrl("http://fhir.ch/ig/ch-epr-ppqm/package.tgz")
			.setName("ch.fhir.ig.ch-epr-ppqm")
			.setVersion("0.2.0")
			.setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL);

		assertProfileCount(0);

		packageInstallerSvc.install(packageSpec);
		Thread.sleep(2000L);

		assertProfileCount(1);
	}

	private void assertIgCount(int expectedCount) {
		CapabilityStatement capabilityStatement = client.retrieveCapabilityStatement();
		List<CanonicalType> implementationGuides = capabilityStatement.getImplementationGuide();
		assertEquals(expectedCount, implementationGuides.size());
	}

	private void assertProfileCount(int expectedCount) {
		Bundle bundle  = (Bundle) client.search()
			.forResource(StructureDefinition.class)
			.where(StructureDefinition.URL.matches().value("http://fhir.ch/ig/ch-epr-ppqm/StructureDefinition/PpqmConsent"))
			.execute();
		assertEquals(expectedCount, bundle.getTotal());

		CapabilityStatement capabilityStatement = client.retrieveCapabilityStatement();
		List<CapabilityStatement.CapabilityStatementRestResourceComponent> resources =
			capabilityStatement.getRest().get(0).getResource();
		int size = resources.stream()
			.filter(stmt -> "Consent".equals(stmt.getType()))
			.findAny()
			.get()
			.getSupportedProfile()
			.size();
		assertEquals(expectedCount, size);
	}

}
