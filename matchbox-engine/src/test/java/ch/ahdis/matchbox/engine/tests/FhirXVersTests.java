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
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ExplanationOfBenefit;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.MatchboxEngine.MatchboxEngineBuilder;

/**
 * https://build.fhir.org/ig/HL7/fhir-cross-version/package.tgz
 */
class FhirXVersTests {

//	static private MatchboxEngine engineR4B; 
	static private MatchboxEngine engineR4, engineR5;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FhirMappingLanguageTests.class);

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		engineR4 = new MatchboxEngineBuilder().withXVersion(true).getEngineR4();
//		engineR4B = new MatchboxEngineBuilder().getEngineR4B();
		engineR5 = new MatchboxEngineBuilder().withXVersion(true).getEngineR5();
		// optional, just for peformance reason
//  	engineR4.cacheXVersionEngine(engineR4B);
		engineR4.cacheXVersionEngine(engineR5);
		// engineR4B.cacheXVersionEngine(engineR4);
		// engineR4B.cacheXVersionEngine(engineR5);
		engineR5.cacheXVersionEngine(engineR4);
//		engineR5.cacheXVersionEngine(engineR4B);
	}
	
	@AfterAll
	static void teardownClass() throws Exception {
		engineR4 = null;
		engineR5 = null;
//		egnineR4B = null;
		CompareUtil.logMemory();
	}
	

	@BeforeEach
	void setUp() throws Exception {
	}

	public String getFileAsStringFromResources(String file) throws IOException {
		InputStream in = FhirXVersTests.class.getResourceAsStream("/xvers" + file);
		return IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	public StructureDefinition getStructureDefinitionFromFile(String file) throws IOException {
		return (StructureDefinition) new org.hl7.fhir.r4.formats.XmlParser()
				.parse(FhirMappingLanguageTests.class.getResourceAsStream(file));
	}

	@Test
	void testMedication5to4inR4() throws FHIRException, IOException {
		MatchboxEngine engine = FhirXVersTests.engineR4;
		assertEquals("4.0.1",engine.getVersion());
		assertEquals("4.0.1",engine.getContext().getVersion());		
		SimpleWorkerContext context = engine.getContext();
		assertNotNull(context.fetchResource(org.hl7.fhir.r5.model.Resource.class, "http://hl7.org/fhir/StructureDefinition/Patient"));
		assertNotNull(context.fetchResource(org.hl7.fhir.r5.model.Resource.class, "http://terminology.hl7.org/CodeSystem/v3-ActCode"));		 
		assertNotNull(context.fetchResource(org.hl7.fhir.r5.model.Resource.class, "http://hl7.org/fhir/uv/xver/StructureMap/Medication4to5"));

		String result = engine.transform(getFileAsStringFromResources("/medication-r5-med0301.json"), true,
				"http://hl7.org/fhir/uv/xver/StructureMap/Medication5to4", true);
		log.info(result);
		CompareUtil.compare(getFileAsStringFromResources("/medication-r4-med0301.json"), result, false);
		CompareUtil.logMemory();
	}

//	@Test
//	void testMedication5to4inR4B() throws FHIRException, IOException {
//		MatchboxEngine engine =FhirXVersTests.engineR4B;
// assertEquals("4.3.0",engine.getVersion());
// assertEquals("4.3.0",engine.getContext().getVersion());
// assertNotNull(context.fetchResource(org.hl7.fhir.r5.model.Resource.class, "http://hl7.org/fhir/StructureDefinition/Patient"));
// assertNotNull(context.fetchResource(org.hl7.fhir.r5.model.Resource.class, "http://hl7.org/fhir/uv/xver/StructureMap/Medication4to5"));
//		String result = engine.transform(getFileAsStringFromResources("/medication-r5-med0301.json"), true,
//				"http://hl7.org/fhir/uv/xver/StructureMap/Medication5to4", true);
//		log.info(result);
//		CompareUtil.compare(getFileAsStringFromResources("/medication-r4-med0301.json"), result, false);
//	}
//
//	@Test
//	void testMedication5to4BinR4B() throws FHIRException, IOException {
//		MatchboxEngine engine =FhirXVersTests.engineR4B;
//		String result = engine.transform(getFileAsStringFromResources("/medication-r5-med0301.json"), true,
//				"http://hl7.org/fhir/uv/xver/StructureMap/Medication5to4b", true);
//		log.info(result);
//		CompareUtil.compare(getFileAsStringFromResources("/medication-r4b-med0301.json"), result, false);
//	}

	@Test
	void testMedication5to4inR5() throws FHIRException, IOException {
		MatchboxEngine engine =FhirXVersTests.engineR5;
		SimpleWorkerContext context = engine.getContext();
		log.info("finished TestEngineR5");
		assertEquals("5.0.0",engine.getVersion());
		assertEquals("5.0.0",engine.getContext().getVersion());
		assertNotNull(context.fetchResource(org.hl7.fhir.r5.model.Resource.class, "http://hl7.org/fhir/StructureDefinition/Patient"));
		assertNotNull(context.fetchResource(org.hl7.fhir.r5.model.Resource.class, "http://hl7.org/fhir/uv/xver/StructureMap/Medication4to5"));
		String result = engine.transform(getFileAsStringFromResources("/medication-r5-med0301.json"), true,
				"http://hl7.org/fhir/uv/xver/StructureMap/Medication5to4", true);
		log.info(result);
		CompareUtil.compare(getFileAsStringFromResources("/medication-r4-med0301.json"), result, false);
		CompareUtil.logMemory();
	}

//	@Test
//	void testMedication5to4BinR5() throws FHIRException, IOException {
//		MatchboxEngine engine =FhirXVersTests.engineR5;
//		String result = engine.transform(getFileAsStringFromResources("/medication-r5-med0301.json"), true,
//				"http://hl7.org/fhir/uv/xver/StructureMap/Medication5to4b", true);
//		log.info(result);
//		CompareUtil.compare(getFileAsStringFromResources("/medication-r4b-med0301.json"), result, false);
//	}

	@Test
	void testMedication4to5inR4() throws FHIRException, IOException {
		MatchboxEngine engine =FhirXVersTests.engineR4;
		String result = engine.transform(getFileAsStringFromResources("/medication-r4-med0301.json"), true,
				"http://hl7.org/fhir/uv/xver/StructureMap/Medication4to5", true);
		log.info(result);
		CompareUtil.compare(getFileAsStringFromResources("/medication-r5-med0301.json"), result, false);
		CompareUtil.logMemory();
	}

//	@Test
//	void testMedication4to5inR4B() throws FHIRException, IOException {
//		MatchboxEngine engine =FhirXVersTests.engineR4B;
//		String result = engine.transform(getFileAsStringFromResources("/medication-r4-med0301.json"), true,
//				"http://hl7.org/fhir/uv/xver/StructureMap/Medication4to5", true);
//		CompareUtil.compare(getFileAsStringFromResources("/medication-r5-med0301.json"), result, false);
//	}
//	
//	@Test
//	void testMedication4Bto5inR4B() throws FHIRException, IOException {
//		MatchboxEngine engine =FhirXVersTests.engineR4B;
//		String result = engine.transform(getFileAsStringFromResources("/medication-r4b-med0301.json"), true,
//				"http://hl7.org/fhir/uv/xver/StructureMap/Medication4Bto5", true);
//		CompareUtil.compare(getFileAsStringFromResources("/medication-r5-med0301.json"), result, false);
//	}

	@Test
	void testMedication4to5inR5() throws FHIRException, IOException {
		MatchboxEngine engine =FhirXVersTests.engineR5;
		String result = engine.transform(getFileAsStringFromResources("/medication-r4-med0301.json"), true,
				"http://hl7.org/fhir/uv/xver/StructureMap/Medication4to5", true);
		log.info(result);
		CompareUtil.compare(getFileAsStringFromResources("/medication-r5-med0301.json"), result, false);
		CompareUtil.logMemory();
	}

//	@Test
//	void testMedication4Bto5inR5() throws FHIRException, IOException {
//		MatchboxEngine engine =FhirXVersTests.engineR5;
//		String result = engine.transform(getFileAsStringFromResources("/medication-r4b-med0301.json"), true,
//				"http://hl7.org/fhir/uv/xver/StructureMap/Medication4Bto5", true);
//		CompareUtil.compare(getFileAsStringFromResources("/medication-r5-med0301.json"), result, false);
//	}


}
