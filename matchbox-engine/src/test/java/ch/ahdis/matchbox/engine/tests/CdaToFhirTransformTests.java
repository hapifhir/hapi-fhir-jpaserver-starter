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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

//	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CdaToFhirTransformTests.class);

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		engine = new CdaMappingEngine.CdaMappingEngineBuilder().getEngine("/cda-fhir-maps-300.tgz");
		InputStream in = CdaToFhirTransformTests.class
				.getResourceAsStream("/cda-it.xml");
		cdaLabItaly = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void TestFhirPath() throws FHIRException, IOException {
		assertEquals("11502-2",engine.evaluateFhirPath(cdaLabItaly, false, "ClinicalDocument.code.code"));
		String attributeWithCdaWhiteSpace = cdaLabItaly.replaceAll("11502-2", " 11502-2");
		assertTrue(attributeWithCdaWhiteSpace.indexOf(" 11502-2")>0);
		assertEquals("11502-2",engine.evaluateFhirPath(attributeWithCdaWhiteSpace, false, "ClinicalDocument.code.code"));
	}

	@Test
	void TestInitial() throws FHIRException, IOException {
		String result = engine.transform(cdaLabItaly, false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY", true);
		assertTrue(result != null);
	}

	@Test
	void TestObservation() throws FHIRException, IOException {
		InputStream in = CdaToFhirTransformTests.class.getResourceAsStream("/cda-it-observation.xml");
		String cdaObservation = IOUtils.toString(in, StandardCharsets.UTF_8);
		Resource resource = engine.transformToFhir(cdaObservation,false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		assertTrue(resource != null);
	}

	@Test
	void TestSecond() throws FHIRException, IOException {
		String result = engine.transform(cdaLabItaly, false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY", true);
		assertTrue(result != null);
	}

	@Test
	void TestThird() throws FHIRException, IOException {
		String result = engine.transform(cdaLabItaly, false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY", true);
		assertTrue(result != null);
	}
	


	/*
	@Test
	void TestLab10() throws FHIRException, IOException {
		InputStream inputStream = CdaToFhirTransformTests.class.getResourceAsStream("/mapped-Esempio CDA2_Referto Medicina di Laboratorio v10-230-fix.xml");
		Bundle bundle = (Bundle) new org.hl7.fhir.r4.formats.XmlParser().parse(inputStream);
		Bundle bundleTransformed = engine.transformCdaToFhir(cdaLabItaly,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		CompareUtil.compare(bundle, bundleTransformed, false);
	}
	*/

	/*
	@Test
	void TestValidate() throws FHIRException, IOException, EOperationOutcome {
		String result = engine.transform(cdaLabItaly, false,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY", true);
		assertTrue(result != null);
		org.hl7.fhir.r4.model.OperationOutcome outcome = engine.validate(new ByteArrayInputStream(result.getBytes("UTF-8")),
				FhirFormat.JSON, "http://hl7.org/fhir/StructureDefinition/Bundle");
		assertTrue(outcome != null);
		assertEquals(0, errors(outcome));
	}
	*/

	@Test
	void TestRefertoDiLaboratoriFull() throws FHIRException, IOException {
		InputStream in = CdaToFhirTransformTests.class
				.getResourceAsStream("/cda-it.xml");
		String refertoDiLaboratori = IOUtils.toString(in, StandardCharsets.UTF_8);
		Bundle bundleTransformed = engine.transformCdaToFhir(refertoDiLaboratori,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		assertTrue(bundleTransformed!=null);
	}

	@Test
	void TestRefertoDiLaboratoriSimple() throws FHIRException, IOException {
		InputStream in = CdaToFhirTransformTests.class
				.getResourceAsStream("/cda-it.xml");
		String refertoDiLaboratori = IOUtils.toString(in, StandardCharsets.UTF_8);
		Bundle bundleTransformed = engine.transformCdaToFhir(refertoDiLaboratori,
				"http://www.ey.com/italy/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		assertTrue(bundleTransformed!=null);
	}
	

	private int errors(OperationOutcome op) {
		int i = 0;
		for (OperationOutcomeIssueComponent vm : op.getIssue()) {
			if (vm.getSeverity() == IssueSeverity.ERROR || vm.getSeverity() == IssueSeverity.FATAL)
				i++;
		}
		return i;
	}

}
