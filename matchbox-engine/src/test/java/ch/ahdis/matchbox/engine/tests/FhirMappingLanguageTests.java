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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.ExplanationOfBenefit;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.formats.JsonParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.MatchboxEngine.MatchboxEngineBuilder;

class FhirMappingLanguageTests {

	static private MatchboxEngine engine;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FhirMappingLanguageTests.class);

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		engine = new MatchboxEngineBuilder().getEngineR4();
	}

	@AfterAll
	static void teardownClass() throws Exception {
		engine = null;
		CompareUtil.logMemory();
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	public String getFileAsStringFromResources(String file) throws IOException {
		InputStream in = FhirMappingLanguageTests.class.getResourceAsStream("/mapping-language" + file);
		return IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	public InputStream getFileAsInputStream(String file) throws IOException {
		return FhirMappingLanguageTests.class.getResourceAsStream("/mapping-language" + file);
	}

	@Test
	void testQr2Patient() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/qr2patgender.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/qr.json"), true,
				"http://ahdis.ch/matchbox/fml/qr2patgender");
		assertTrue(res != null);
		assertEquals("Patient", res.getResourceType().name());
		Patient patient = (Patient) res;
		assertEquals("FEMALE", patient.getGender().name());
	}

	@Test
	void testMemberOf() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/memberof.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/pat.json"), true,
				"http://ahdis.ch/matchbox/fml/memberof");
		assertTrue(res != null);
		assertEquals("Patient", res.getResourceType().name());
		Patient patient = (Patient) res;
		assertEquals("MALE", patient.getGender().name());
	}

	@Test
	void testNarrative() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/narrative.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/pat.json"), true,
				"http://ahdis.ch/matchbox/fml/narrative");
		assertTrue(res != null);
		assertEquals("Patient", res.getResourceType().name());
		Patient patient = (Patient) res;
		assertEquals("<div xmlns=\"http://www.w3.org/1999/xhtml\">text</div>", patient.getText().getDivAsString());
	}

	@Test
	void testConformsTo() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/conformsto.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/pat.json"), true,
				"http://ahdis.ch/matchbox/fml/conformsto");
		assertTrue(res != null);
		assertEquals("Patient", res.getResourceType().name());
		Patient patient = (Patient) res;
		assertEquals("MALE", patient.getGender().name());
	}

	@Test
	void testMatchboxEngine() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/conformstoneg.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		assertTrue(engine.getCanonicalResource(sm.getUrl(), "4.0.1") != null);
		assertTrue(engine.getContext().fetchResource(org.hl7.fhir.r5.model.StructureMap.class, sm.getUrl()) != null);

		String qr = getFileAsStringFromResources("/questionnairepatient.xml");

		XmlParser xml = new XmlParser();
		Questionnaire questionnaire = (Questionnaire) xml.parse(qr);

		engine.addCanonicalResource(questionnaire);
		assertTrue(engine.getCanonicalResource(questionnaire.getUrl(), "4.0.1") != null);
		assertTrue(engine.getContext().fetchResource(org.hl7.fhir.r5.model.Questionnaire.class,
				questionnaire.getUrl()) != null);
		assertTrue(engine.getCanonicalResourceById("Questionnaire", questionnaire.getId()) != null);
	}

	@Test
	void testConformsToNeg() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/conformstoneg.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/pat.json"), true,
				"http://ahdis.ch/matchbox/fml/conformstoneg");
		assertTrue(res != null);
		assertEquals("Patient", res.getResourceType().name());
		Patient patient = (Patient) res;
		assertTrue(patient.getGender() == null);
	}

	@Test
	void testQty() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/quantity.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		try {
			Resource res = engine.transformToFhir(getFileAsStringFromResources("/qr.json"), true,
					"http://ahdis.ch/matchbox/fml/qty");
			assertTrue(res != null);
			assertEquals("Oberservation", res.getResourceType().name());
			Observation obs = (Observation) res;
			assertEquals("http://unit.org", obs.getValueQuantity().getSystem());
			assertEquals("kg", obs.getValueQuantity().getUnit());
			assertEquals("kg", obs.getValueQuantity().getCode());
			assertEquals("90", obs.getValueQuantity().getValue());
		} catch (FHIRException e) {
			log.info("qty is not yet implmemented");
			// correct Transform Unknown: qty
		}
	}

	@Test
	void testStringToCoding() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/stringtocoding.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/qr.json"), true,
				"http://ahdis.ch/matchbox/fml/stringtocoding");
		assertTrue(res != null);
		assertEquals("ExplanationOfBenefit", res.getResourceType().name());
		ExplanationOfBenefit eob = (ExplanationOfBenefit) res;
		assertEquals("http://terminology.hl7.org/CodeSystem/claim-type", eob.getType().getCodingFirstRep().getSystem());
		assertEquals("oral", eob.getType().getCodingFirstRep().getCode());
	}

	@Test
	void testStringToCodingWithCC() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/stringtocodingwithcc.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/qr.json"), true,
				"http://ahdis.ch/matchbox/fml/stringtocodingwithcc");
		assertTrue(res != null);
		assertEquals("ExplanationOfBenefit", res.getResourceType().name());
		ExplanationOfBenefit eob = (ExplanationOfBenefit) res;
		assertEquals("http://terminology.hl7.org/CodeSystem/communication-category",
				eob.getType().getCodingFirstRep().getSystem());
		assertEquals("notification", eob.getType().getCodingFirstRep().getCode());
	}

	@Test
	void testCast() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/cast.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/qrext.json"), true,
				"http://ahdis.ch/matchbox/fml/cast");
		assertTrue(res != null);
		assertEquals("Observation", res.getResourceType().name());
	}

	@Test
	void testBundleTimestamp() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/bundlets.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/qrext.json"), true,
				"http://ahdis.ch/matchbox/fml/bundlets");
		assertTrue(res != null);
		assertEquals("Bundle", res.getResourceType().name());
		Bundle bundle = (Bundle) res;
		assertEquals("2023-10-21T00:00:00Z", bundle.getTimestampElement().getValueAsString());
	}

	@Test
	void testWhereClause() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/whereclause.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		CapabilityStatement result = (CapabilityStatement) engine.transformToFhir(
				getFileAsStringFromResources("/capabilitystatement-example.json"), true,
				"http://ahdis.ch/matchbox/fml/whereclause");
		assertTrue(result != null);
		assertEquals(5, result.getRest().get(0).getResource().get(0).getInteraction().size());
	}

	@Test
	void testDateManipulation() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/qr2patfordates.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		Resource res = engine.transformToFhir(getFileAsStringFromResources("/qrext.json"), true,
				"http://ahdis.ch/matchbox/fml/qr2patfordates");
		assertTrue(res != null);
		assertEquals("Patient", res.getResourceType().name());
		Patient patient = (Patient) res;
		assertEquals("2023-10-26", patient.getBirthDateElement().getValueAsString());
		assertEquals("2023-09-20T13:19:13.502Z", patient.getDeceasedDateTimeType().getValueAsString());
	}

	private org.hl7.fhir.r5.model.CanonicalResource getCanonicalResourceFromJson(String file)
			throws FHIRFormatError, IOException {
		JsonParser json = new JsonParser();
		return (org.hl7.fhir.r5.model.CanonicalResource) json.parse(getFileAsInputStream(file));
	}

	@Test
	void testTutorialStep1Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step1/map/step1.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step1/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-1", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step1/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-1", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step1/source/source1.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step1", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step1/result/step1.source1.xml"), result);
	}

	@Test
	void testTutorialStep1Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step1/map/step1.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step1/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-1", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step1/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-1", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step1/source/source1.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step1", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step1/result/step1.source1.json"), result, false);
	}

	@Test
	void testTutorialStep1bJson() throws FHIRException, IOException {
		// from rule 'rule_a_short'
		// 1b org.hl7.fhir.exceptions.FHIRException: No matches found for rule for
		// 'string to string' from http://hl7.org/fhir/StructureMap/tutorial-step1b,
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step1/map/step1b.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step1/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-1", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step1/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-1", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step1/source/source1.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step1b", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step1/result/step1.source1.json"), result, false);
	}

	@Test
	void testTutorialStep2Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step2/map/step2.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step2/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-2", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step2/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-2", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step2/source/source2.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step2", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step2/result/step2.source2.xml"), result);
	}

	@Test
	void testTutorialStep2Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step2/map/step2.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step2/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-2", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step2/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-2", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step2/source/source2.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step2", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step2/result/step2.source2.json"), result, false);
	}

	@Test
	void testTutorialStep3aXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step3/map/step3a.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step3/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-3", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step3/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-3", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step3a", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step3/result/step3a.source3.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3min.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step3a", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step3/result/step3a.source3min.xml"), result);
	}

	@Test
	void testTutorialStep3aJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step3/map/step3a.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step3/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-3", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step3/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-3", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step3a", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step3/result/step3a.source3.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3min.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step3a", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step3/result/step3a.source3min.json"), result, false);
	}

	@Test
	void testTutorialStep3bXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step3/map/step3b.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step3/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-3", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step3/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-3", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step3b", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step3/result/step3b.source3.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3min.json"), true,
		"http://hl7.org/fhir/StructureMap/tutorial-step3b", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step3/result/step3b.source3min.json"), result, false);
	}

	@Test
	void testTutorialStep3bJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step3/map/step3b.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step3/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-3", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step3/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-3", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step3b", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step3/result/step3b.source3.json"), result, false);


		result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3min.json"), true,
		"http://hl7.org/fhir/StructureMap/tutorial-step3b", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step3/result/step3b.source3min.json"), result, false);
	}

	@Test
	void testTutorialStep3cXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step3/map/step3c.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step3/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-3", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step3/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-3", "5.0.0"));

		try {
			String result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3.xml"), false,
					"http://hl7.org/fhir/StructureMap/tutorial-step3c", false);
					assertTrue(result != null);
			assertTrue(false, "should throw an exception Rule \"rule_a20c\": Check condition failed");
		}
		catch (FHIRException e) {
			assertEquals("Rule \"rule_a20c\": Check condition failed", e.getMessage());
		}

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3min.json"), true,
		"http://hl7.org/fhir/StructureMap/tutorial-step3c", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step3/result/step3c.source3min.json"), result, false);
	}

	@Test
	void testTutorialStep3cJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step3/map/step3c.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step3/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-3", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step3/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-3", "5.0.0"));

		try {
			String result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3.json"), true,
					"http://hl7.org/fhir/StructureMap/tutorial-step3c", true);
					assertTrue(result != null);
			assertTrue(false, "should throw an exception Rule \"rule_a20c\": Check condition failed");
		}
		catch (FHIRException e) {
			assertEquals("Rule \"rule_a20c\": Check condition failed", e.getMessage());
		}

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step3/source/source3min.json"), true,
		"http://hl7.org/fhir/StructureMap/tutorial-step3c", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step3/result/step3c.source3min.json"), result, false);
	}

	@Test
	void testTutorialStep4aXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step4/map/step4a.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step4/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-4", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step4/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-4", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step4a", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step4/result/step4a.source4.xml"), result);

		try {
			result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4b.xml"), false, "http://hl7.org/fhir/StructureMap/tutorial-step4a", false);
			assertTrue(false, "should throw an exception API-0389: Failed to call access method: org.hl7.fhir.exceptions.FHIRException: Exception executing transform tgt.a21 = cast(a, 'integer') on Rule &quot;tutorial-step4a|tutorial|rule_a21a&quot;: java.lang.NumberFormatException: For input string: &quot;notanumber&quot;");
		}
		catch (FHIRException e) {
		}
	}

	@Test
	void testTutorialStep4aJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step4/map/step4a.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step4/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-4", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step4/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-4", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step4a", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step4/result/step4a.source4.json"), result, false);

		try {
			result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4b.json"), true,
			"http://hl7.org/fhir/StructureMap/tutorial-step4a", true);
			assertTrue(false, "should throw an exception API-0389: Failed to call access method: org.hl7.fhir.exceptions.FHIRException: Exception executing transform tgt.a21 = cast(a, 'integer') on Rule &quot;tutorial-step4a|tutorial|rule_a21a&quot;: java.lang.NumberFormatException: For input string: &quot;notanumber&quot;");
		}
		catch (FHIRException e) {
		}
	}

	@Test
	void testTutorialStep4b2Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step4/map/step4b2.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step4/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-4", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step4/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-4", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step4b2", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step4/result/step4b2.source4.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4b.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step4b2", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step4/result/step4b2.source4b.xml"), result);
	}

	@Test
	void testTutorialStep4b2Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step4/map/step4b2.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step4/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-4", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step4/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-4", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step4b2", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step4/result/step4b2.source4.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4b.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step4b2", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step4/result/step4b2.source4b.json"), result, false);
	}

	@Test
	void testTutorialStep4b3Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step4/map/step4b3.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step4/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-4", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step4/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-4", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step4b3", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step4/result/step4b3.source4.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4b.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step4b3", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step4/result/step4b3.source4b.xml"), result);
	}

	@Test
	void testTutorialStep4b3Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step4/map/step4b3.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step4/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-4", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step4/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-4", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step4b3", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step4/result/step4b3.source4.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4b.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step4b3", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step4/result/step4b3.source4b.json"), result, false);
	}

	@Test
	void testTutorialStep4cXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step4/map/step4c.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step4/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-4", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step4/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-4", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step4c", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step4/result/step4c.source4.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4b.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step4c", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step4/result/step4c.source4b.xml"), result);
	}

	@Test
	void testTutorialStep4cJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step4/map/step4c.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step4/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-4", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step4/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-4", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step4c", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step4/result/step4c.source4.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step4/source/source4b.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step4c", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step4/result/step4c.source4b.json"), result, false);
	}

	@Test
	void testTutorialStep5Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step5/map/step5.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step5/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-5", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step5/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-5", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step5/source/source5.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step5", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step5/result/step5.source5.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step5/source/source5b.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step5", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step5/result/step5.source5b.xml"), result);
	}

	@Test
	void testTutorialStep5Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step5/map/step5.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step5/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-5", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step5/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-5", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step5/source/source5.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step5", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step5/result/step5.source5.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step5/source/source5b.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step5", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step5/result/step5.source5b.json"), result, false);
	}

	@Test
	void testTutorialStep6aXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step6/map/step6a.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step6/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-6", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step6/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-6", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step6a", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step6/result/step6a.source6.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6b.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step6a", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step6/result/step6a.source6b.xml"), result);
	}

	@Test
	void testTutorialStep6aJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step6/map/step6a.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step6/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-6", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step6/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-6", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step6a", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step6/result/step6a.source6.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6b.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step6a", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step6/result/step6a.source6b.json"), result, false);
	}

	@Test
	void testTutorialStep6bXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step6/map/step6b.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step6/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-6", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step6/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-6", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step6b", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step6/result/step6b.source6.xml"), result);

		try {
			result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6b.xml"), false,
			"http://hl7.org/fhir/StructureMap/tutorial-step6b", false);
				assertTrue(false, "API-0389: Failed to call access method: org.hl7.fhir.exceptions.FHIRException: Rule &quot;rule_a23b&quot;: Check condition failed: the collection has more than one item");
		}
		catch (FHIRException e) {
		}		
	}

	@Test
	void testTutorialStep6bJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step6/map/step6b.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step6/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-6", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step6/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-6", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step6b", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step6/result/step6b.source6.json"), result, false);

		try {
			result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6b.json"), true,
			"http://hl7.org/fhir/StructureMap/tutorial-step6b", true);
			assertTrue(false, "API-0389: Failed to call access method: org.hl7.fhir.exceptions.FHIRException: Rule &quot;rule_a23b&quot;: Check condition failed: the collection has more than one item");
		}
		catch (FHIRException e) {
		}	
	}

	@Test
	void testTutorialStep6cXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step6/map/step6c.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step6/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-6", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step6/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-6", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step6c", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step6/result/step6c.source6.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6b.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step6c", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step6/result/step6c.source6b.xml"), result);
	}

	@Test
	void testTutorialStep6cJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step6/map/step6c.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step6/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-6", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step6/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-6", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step6c", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step6/result/step6c.source6.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6b.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step6c", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step6/result/step6c.source6b.json"), result, false);
	}

	@Test
	void testTutorialStep6dXml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step6/map/step6d.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step6/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-6", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step6/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-6", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step6d", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step6/result/step6d.source6.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6b.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step6d", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step6/result/step6d.source6b.xml"), result);
	}

	@Test
	void testTutorialStep6dJson() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step6/map/step6d.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step6/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-6", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step6/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-6", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step6d", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step6/result/step6d.source6.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step6/source/source6b.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step6d", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step6/result/step6d.source6b.json"), result, false);
	}

	@Test
	void testTutorialStep7Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step7/map/step7.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);

		sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step7/map/step7b.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);

		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step7/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-7", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step7/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-7", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step7/source/source7.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step7", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step7/result/step7.source7.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step7/source/source7.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step7b", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step7/result/step7.source7.xml"), result);
	}

	@Test
	void testTutorialStep7Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step7/map/step7.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step7/map/step7b.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step7/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-7", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step7/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-7", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step7/source/source7.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step7", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step7/result/step7.source7.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step7/source/source7.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step7b", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step7/result/step7.source7.json"), result, false);
	}

	@Test
	void testTutorialStep8Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step8/map/step8.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step8/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-8", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step8/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-8", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step8/source/source8.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step8", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step8/result/step8.source8.xml"), result);
	}

	@Test
	void testTutorialStep8Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step8/map/step8.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step8/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-8", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step8/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-8", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step8/source/source8.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step8", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step8/result/step8.source8.json"), result, false);
	}

	@Test
	void testTutorialStep9Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step9/map/step9.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step9/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-9", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step9/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-9", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step9/source/source9.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step9", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step9/result/step9.source9.xml"), result);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step9/source/source9b.xml"), false,
		"http://hl7.org/fhir/StructureMap/tutorial-step9", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step9/result/step9.source9b.xml"), result);
	}

	@Test
	void testTutorialStep9Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step9/map/step9.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step9/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-9", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step9/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-9", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step9/source/source9.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step9", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step9/result/step9.source9.json"), result, false);

		result = engine.transform(getFileAsStringFromResources("/tutorial/step9/source/source9b.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step9", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step9/result/step9.source9b.json"), result, false);
	}

	@Test
	void testTutorialStep10Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step10/map/step10.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step10/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-10", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step10/logical/structuredefinition-tleftinner.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-leftinner-10", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step10/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-10", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step10/logical/structuredefinition-trightinner.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-rightinner-10", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step10/source/source10.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step10", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step10/result/step10.source10.xml"), result);
	}

	@Test
	void testTutorialStep10Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step10/map/step10.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step10/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-10", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step10/logical/structuredefinition-tleftinner.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-leftinner-10", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step10/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-10", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step10/logical/structuredefinition-trightinner.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-rightinner-10", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step10/source/source10.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step10", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step10/result/step10.source10.json"), result, false);
	}

	@Test
	void testTutorialStep11Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step11/map/step11.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step11/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-11", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step11/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-11", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step11/source/source11.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step11", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step11/result/step11.source11.xml"), result);
	}

	@Test
	void testTutorialStep11Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step11/map/step11.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step11/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-11", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step11/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-11", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step11/source/source11.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step11", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step11/result/step11.source11.json"), result, false);
	}

	@Test
	void testTutorialStep12Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step12/map/step12.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step12/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-12", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step12/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-12", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step12/source/source12.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step12", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step12/result/step12.source12.xml"), result);
	}

	@Test
	void testTutorialStep12Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step12/map/step12.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step12/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-12", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step12/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-12", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step12/source/source12.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step12", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step12/result/step12.source12.json"), result, false);
	}

	@Test
	void testTutorialStep13Xml() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step13/map/step13.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step13/logical/structuredefinition-tleft.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-13", "5.0.0"));
		engine.addCanonicalResource(getFileAsInputStream("/tutorial/step13/logical/structuredefinition-tright.xml"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-13", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step13/source/source13.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step13", false);
		assertTrue(result != null);
		CompareUtil.compareXml(getFileAsStringFromResources("/tutorial/step13/result/step13.source13.xml"), result);
	}

	@Test
	void testTutorialStep13Json() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step13/map/step13.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step13/logical/structuredefinition-tleft.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-left-13", "5.0.0"));
		engine.addCanonicalResource(
				getCanonicalResourceFromJson("/tutorial/step13/logical/structuredefinition-tright.json"));
		assertNotNull(engine.getCanonicalResource("http://hl7.org/fhir/StructureDefinition/tutorial-right-13", "5.0.0"));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step13/source/source13.json"), true,
				"http://hl7.org/fhir/StructureMap/tutorial-step13", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step13/result/step13.source13.json"), result, false);
	}


}
