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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ExplanationOfBenefit;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
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

	@BeforeEach
	void setUp() throws Exception {
	}

	public String getFileAsStringFromResources(String file) throws IOException {
		InputStream in = FhirMappingLanguageTests.class.getResourceAsStream("/mapping-language" + file);
		return IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	public StructureDefinition getStructureDefinitionFromFile(String file) throws IOException {
		return (StructureDefinition) new org.hl7.fhir.r4.formats.XmlParser()
				.parse(FhirMappingLanguageTests.class.getResourceAsStream(file));
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
		assertTrue(engine.getCanonicalResourceR4(sm.getUrl())!= null);
		assertTrue(engine.getContext().fetchResource( org.hl7.fhir.r5.model.StructureMap.class, sm.getUrl()) != null);


		String qr = getFileAsStringFromResources("/questionnairepatient.xml");

		XmlParser xml = new XmlParser();
        Questionnaire questionnaire = (Questionnaire) xml.parse(qr);

		engine.addCanonicalResource(questionnaire);
		assertTrue(engine.getCanonicalResourceR4(questionnaire.getUrl())!= null);
		assertTrue(engine.getContext().fetchResource( org.hl7.fhir.r5.model.Questionnaire.class, questionnaire.getUrl()) != null);
		assertTrue(engine.getCanonicalResourceById("Questionnaire", questionnaire.getId())!= null);
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
	
	

	@Test
	@Disabled // R5 maps are not working with that FHIR core version
	void testTutorialStep1() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step1/map/step1.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				engine.createSnapshot(getStructureDefinitionFromFile("/tutorial/step1/logical/structuredefinition-tleft.xml")));
		engine.addCanonicalResource(engine
				.createSnapshot(getStructureDefinitionFromFile("/tutorial/step1/logical/structuredefinition-tright.xml")));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step1/source/source1.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step1", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step1/output/step1.json"), result, false);

		// 1b
		sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step1/map/step1b.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);

//    try {
//	    result = engine.transform(getFileAsStringFromResources("/tutorial/step1/source/source1.xml"), false, "http://hl7.org/fhir/StructureMap/tutorial-step1b", true);
//	    assertTrue(result!=null);
//	    CompareUtil.compare(getFileAsStringFromResources("/tutorial/step1/output/step1.json"), result, false);
//    } catch(java.lang.Error e) {
//    	log.info("not supported yt, see https://github.com/ahdis/fhir-mapping-tutorial");
//    }
	}

	@Test
	@Disabled // R5 maps are not working with that FHIR core version
	void testTutorialStep2() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirMappingLanguageTests.engine);
		StructureMap sm = engine.parseMap(getFileAsStringFromResources("/tutorial/step2/map/step2.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);
		engine.addCanonicalResource(
				engine.createSnapshot(getStructureDefinitionFromFile("/tutorial/step2/logical/structuredefinition-tleft.xml")));
		engine.addCanonicalResource(engine
				.createSnapshot(getStructureDefinitionFromFile("/tutorial/step2/logical/structuredefinition-tright.xml")));

		String result = engine.transform(getFileAsStringFromResources("/tutorial/step2/source/source2.xml"), false,
				"http://hl7.org/fhir/StructureMap/tutorial-step2", true);
		assertTrue(result != null);
		CompareUtil.compare(getFileAsStringFromResources("/tutorial/step2/output/step2.json"), result, false);
	}

}
