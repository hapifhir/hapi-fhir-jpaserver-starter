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

/**
 * https://build.fhir.org/ig/HL7/fhir-cross-version/package.tgz
 */
class FhirXVersTests {

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
		InputStream in = FhirXVersTests.class.getResourceAsStream("/xvers" + file);
		return IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	public StructureDefinition getStructureDefinitionFromFile(String file) throws IOException {
		return (StructureDefinition) new org.hl7.fhir.r4.formats.XmlParser()
				.parse(FhirMappingLanguageTests.class.getResourceAsStream(file));
	}

	@Test
	void testMedication5to4inR4() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirXVersTests.engine);
		String result = engine.transform(getFileAsStringFromResources("/medication-r5-med0301.json"), true,
				"http://hl7.org/fhir/uv/xver/StructureMap/Medication5to4", true);
		log.info(result);
		CompareUtil.compare(getFileAsStringFromResources("/medication-r4-med0301.json"), result, false);
	}

	@Test
	void testMedication4to5inR4() throws FHIRException, IOException {
		MatchboxEngine engine = new MatchboxEngine(FhirXVersTests.engine);
		String result = engine.transform(getFileAsStringFromResources("/medication-r4-med0301.json"), true,
				"http://hl7.org/fhir/uv/xver/StructureMap/Medication4to5", true);
		log.info(result);
		CompareUtil.compare(getFileAsStringFromResources("/medication-r5-med0301.json"), result, false);
	}


}
