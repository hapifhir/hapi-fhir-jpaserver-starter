package ch.ahdis.matchbox.engine.tests;

/*
 * #%L
 * Matchbox Engine
 * %%
 * Copyright (C) 2022 ahdis
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.ahdis.matchbox.engine.CdaMappingEngine;

class CdaToFhirTransformTests {

	static private CdaMappingEngine engine;
	static String cdaLabItaly;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CdaToFhirTransformTests.class);

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		InputStream in = CdaToFhirTransformTests.class
				.getResourceAsStream("/cda-it.xml");
		cdaLabItaly = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private CdaMappingEngine getEngine() {
		if (engine == null) {
			try {
				engine = new CdaMappingEngine.CdaMappingEngineBuilder().getEngine("/cda-fhir-maps-300.tgz");
			} catch (FHIRException | IOException | URISyntaxException e) {
				e.printStackTrace();
			}
			return engine;
		}
		return engine;
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void TestFhirPath() throws FHIRException, IOException {
		assertEquals("11502-2",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.code.code"));
		String attributeWithCdaWhiteSpace = cdaLabItaly.replaceAll("11502-2", " 11502-2");
		assertTrue(attributeWithCdaWhiteSpace.indexOf(" 11502-2")>0);
		assertEquals("11502-2",getEngine().evaluateFhirPath(attributeWithCdaWhiteSpace, false, "ClinicalDocument.code.code"));
		assertEquals("REFERTO DI LABORATORIO",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.title.dataString"));
		assertEquals("IT",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.realmCode.code"));
		assertEquals("2.16.840.1.113883.1.3",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.typeId.root"));
		assertEquals("POCD_MT000040UV02",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.typeId.extension"));
		assertEquals("active",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.statusCode.code"));
// 	<effectiveTime value="20220330112426+0100"/>
		assertEquals("2022-03-30T11:24:26+01:00",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.effectiveTime.value"));
		assertEquals("1993-06-19",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.recordTarget.patientRole.patient.birthTime.value"));
		assertEquals("Verdi",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.recordTarget.patientRole.patient.name.family.dataString"));
		assertEquals("Giuseppe",getEngine().evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.recordTarget.patientRole.patient.name.given.dataString"));
	}

	@Test
	void TestInitial() throws FHIRException, IOException {
		String result = getEngine().transform(cdaLabItaly, false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY", true);
		assertTrue(result != null);
	}

	@Test
	void TestObservation() throws FHIRException, IOException {
		InputStream in = CdaToFhirTransformTests.class.getResourceAsStream("/cda-it-observation.xml");
		String cdaObservation = IOUtils.toString(in, StandardCharsets.UTF_8);
		Resource resource = getEngine().transformToFhir(cdaObservation,false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		assertTrue(resource != null);
	}

	@Test
	void TestSecond() throws FHIRException, IOException {
		String result = getEngine().transform(cdaLabItaly, false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY", true);
		assertTrue(result != null);
	}

	@Test
	void TestThird() throws FHIRException, IOException {
		String result = getEngine().transform(cdaLabItaly, false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY", true);
		assertTrue(result != null);
	}
	


	/*
	@Test
	void TestLab10() throws FHIRException, IOException {
		InputStream inputStream = CdaToFhirTransformTests.class.getResourceAsStream("/mapped-Esempio CDA2_Referto Medicina di Laboratorio v10-230-fix.xml");
		Bundle bundle = (Bundle) new org.hl7.fhir.r4.formats.XmlParser().parse(inputStream);
		Bundle bundleTransformed = getEngine().transformCdaToFhir(cdaLabItaly,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		CompareUtil.compare(bundle, bundleTransformed, false);
	}
	*/

	@Test
	void TestValidateCdaCh() throws FHIRException, IOException, EOperationOutcome {
		InputStream in = CdaToFhirTransformTests.class
		.getResourceAsStream("/cda-ch.xml");
		org.hl7.fhir.r4.model.OperationOutcome outcome = getEngine().validate(in,
				FhirFormat.XML, "http://hl7.org/fhir/cda/StructureDefinition/ClinicalDocument");
		assertTrue(outcome != null);
		assertEquals(0, errors(outcome));
	}

	@Test
	void TestValidateCdaIt() throws FHIRException, IOException, EOperationOutcome {
		InputStream in = CdaToFhirTransformTests.class
		.getResourceAsStream("/cda-it.xml");
		org.hl7.fhir.r4.model.OperationOutcome outcome = getEngine().validate(in,
				FhirFormat.XML, "http://hl7.org/fhir/cda/StructureDefinition/ClinicalDocument");
		assertTrue(outcome != null);
		assertEquals(0, errors(outcome));
	}

	@Test
	void TestValidateFhir() throws FHIRException, IOException, EOperationOutcome {
		InputStream in = CdaToFhirTransformTests.class
				.getResourceAsStream("/pat.json");
		org.hl7.fhir.r4.model.OperationOutcome outcome = getEngine().validate(in,
				FhirFormat.JSON, "http://hl7.org/fhir/StructureDefinition/Patient");
		assertTrue(outcome != null);
		assertEquals(0, errors(outcome));
	}

	@Test
	void TestRefertoDiLaboratoriFull() throws FHIRException, IOException {
		InputStream in = CdaToFhirTransformTests.class
				.getResourceAsStream("/cda-it.xml");
		String refertoDiLaboratori = IOUtils.toString(in, StandardCharsets.UTF_8);
		Bundle bundleTransformed = getEngine().transformCdaToFhir(refertoDiLaboratori,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		assertTrue(bundleTransformed!=null);
	}

	@Test
	void TestRefertoDiLaboratoriSimple() throws FHIRException, IOException {
		InputStream in = CdaToFhirTransformTests.class
				.getResourceAsStream("/cda-it.xml");
		String refertoDiLaboratori = IOUtils.toString(in, StandardCharsets.UTF_8);
		Bundle bundleTransformed = getEngine().transformCdaToFhir(refertoDiLaboratori,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		assertTrue(bundleTransformed!=null);
	}
	

	private int errors(OperationOutcome op) {
		int i = 0;
		for (OperationOutcomeIssueComponent vm : op.getIssue()) {
			if (vm.getSeverity() == IssueSeverity.ERROR || vm.getSeverity() == IssueSeverity.FATAL) {
				log.error(vm.getDetails().getText());
				// eg ('tel: 390 666 0581')
				if (vm.getDetails().getText().startsWith("URI values cannot have whitespace")) {
					continue;
				}
				// https://terminology.hl7.org/5.0.0/ValueSet-v3-RoleClassAssignedEntity.json.html has a filter with an is-a concept to ASSIGEND and this cannot be evaluated by org.hl7.fhir.r5.terminologies.ValueSetCheckerSimple
				if (vm.getDetails().getText().startsWith("The value provided ('ASSIGNED') is not in the value set 'RoleClassAssignedEntity' (http://terminology.hl7.org/ValueSet/v3-RoleClassAssignedEntity|2.0.0), and a code is required from this value set) (error message = Unable to resolve system - value set http://terminology.hl7.org/ValueSet/v3-RoleClassAssignedEntity|2.0.0 include #0 has no system)")) {
					continue;
				}
				++i;
			}
		}
		return i;
	}

}
