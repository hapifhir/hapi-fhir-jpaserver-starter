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

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

class CdaToFhirTransformTests {

	static private CdaMappingEngine engine;
	static String cdaLabItaly;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CdaToFhirTransformTests.class);

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		InputStream in = getResourceAsStream("cda-it.xml");
		cdaLabItaly = IOUtils.toString(in, StandardCharsets.UTF_8);
	}
	
	@AfterAll
	static void teardownClass() throws Exception {
		engine = null;
		CompareUtil.logMemory();
	}


	private CdaMappingEngine getEngine() {
		if (engine == null) {
			try {
				engine = new CdaMappingEngine.CdaMappingEngineBuilder().getCdaEngineR4();
				StructureMap sm = engine.parseMap(getFileAsStringFromResources("datatypes.map"));
				assertTrue(sm != null);
				engine.addCanonicalResource(sm);
				sm = engine.parseMap(getFileAsStringFromResources("FullHeader.map"));
				assertTrue(sm != null);
				engine.addCanonicalResource(sm);
				sm = engine.parseMap(getFileAsStringFromResources("LabBody.map"));
				assertTrue(sm != null);
				engine.addCanonicalResource(sm);
				sm = getEngine().parseMap(getFileAsStringFromResources("cda-it-observation.map"));
				assertTrue(sm != null);
				engine.addCanonicalResource(sm);
				sm = getEngine().parseMap(getFileAsStringFromResources("cda-it-observation-st-r2b.map"));
				assertTrue(sm != null);
				engine.addCanonicalResource(sm);
				sm = getEngine().parseMap(getFileAsStringFromResources("cda-it-observation-condition.map"));
				assertTrue(sm != null);
				engine.addCanonicalResource(sm);
				sm = getEngine().parseMap(getFileAsStringFromResources("fhir-to-cda.map"));
				assertTrue(sm != null);
				engine.addCanonicalResource(sm);
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
	    
	  String str = getEngine().convert(cdaLabItaly, false);
	  log.debug(str);

		assertEquals("11502-2", getEngine().evaluateFhirPath(cdaLabItaly, false, "code.code"));
		String attributeWithCdaWhiteSpace = cdaLabItaly.replaceAll("11502-2", " 11502-2");
		assertTrue(attributeWithCdaWhiteSpace.indexOf(" 11502-2") > 0);
		assertEquals("11502-2",
				getEngine().evaluateFhirPath(attributeWithCdaWhiteSpace, false, "code.code"));
		assertEquals("REFERTO DI LABORATORIO",
				getEngine().evaluateFhirPath(cdaLabItaly, false, "title.xmlText"));
		assertEquals("IT", getEngine().evaluateFhirPath(cdaLabItaly, false, "realmCode.code"));
		assertEquals("2.16.840.1.113883.1.3",
				getEngine().evaluateFhirPath(cdaLabItaly, false, "typeId.root"));
//		assertEquals("POCD_MT000040UV02",
//				getEngine().evaluateFhirPath(cdaLabItaly, false, "typeId.extension"));
    assertEquals("POCD_HD000040",
            getEngine().evaluateFhirPath(cdaLabItaly, false, "typeId.extension"));
		// stdc
//		assertEquals("active", getEngine().evaluateFhirPath(cdaLabItaly, false, "statusCode.code"));
		// changed with core 7.3.0
    assertEquals("active", getEngine().evaluateFhirPath(cdaLabItaly, false, "sdtcStatusCode.code"));
		// <effectiveTime value="20220330112426+0100"/>
		assertEquals("2022-03-30T11:24:26+01:00",
				getEngine().evaluateFhirPath(cdaLabItaly, false, "effectiveTime.value"));
		assertEquals("1993-06-19",
				getEngine().evaluateFhirPath(cdaLabItaly,
						false,
						"recordTarget.patientRole.patient.birthTime.value"));
		
		
//		https://hl7.org/cda/stds/core/2.0.0-sd-snapshot1/StructureDefinition-PN.html
		assertEquals("Verdi",
				getEngine().evaluateFhirPath(cdaLabItaly,
						false,
						"recordTarget.patientRole.patient.name.item.family.xmlText"));
		assertEquals("Giuseppe",
				getEngine().evaluateFhirPath(cdaLabItaly,
						false,
						"recordTarget.patientRole.patient.name.item.given.xmlText"));

		// hl7 austria

		assertEquals("2021-06-01", getEngine().evaluateFhirPath(cdaLabItaly, false, "terminologyDate.value"));
		assertEquals("code", getEngine().evaluateFhirPath(cdaLabItaly, false, "formatCode.code"));
		assertEquals("F028", getEngine().evaluateFhirPath(cdaLabItaly, false, "practiceSettingCode.code"));
	}

	@Test
	void TestInitial() throws FHIRException, IOException {
		String result = getEngine().transform(cdaLabItaly,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY",
				true);
		assertNotNull(result);

		Bundle resource = (Bundle) getEngine().transformToFhir(cdaLabItaly,	false,"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY");
		assertNotNull(resource);
		Composition composition = (Composition) resource.getEntryFirstRep().getResource();
		assertNotNull(composition);
		assertEquals("2022-03-30T11:24:26+01:00", composition.getDateElement().getValueAsString());
		Patient patient = (Patient) resource.getEntry().stream()
				.filter(e -> e.getResource() instanceof Patient)
				.map(e -> e.getResource())
				.findFirst()
				.orElse(null);
		assertEquals("058091", patient.getAddressFirstRep().getLine().getFirst().getExtension().getFirst().getValue().toString());
	}

	@Test
	void TestObservation() throws FHIRException, IOException {
		InputStream in = getResourceAsStream("cda-it-observation.xml");

		StructureMap sm = getEngine().parseMap(getFileAsStringFromResources("cda-it-observation.map"));
		assertTrue(sm != null);
		engine.addCanonicalResource(sm);

		String cdaObservation = IOUtils.toString(in, StandardCharsets.UTF_8);
		Resource resource = getEngine().transformToFhir(cdaObservation,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/TestObservation");

		Observation obs = (Observation) resource;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(obs.getValuePeriod().getEnd());
		assertEquals(2022, calendar.get(Calendar.YEAR));

		assertNotNull(resource);
	}

	@Test
	void TestObservationSt() throws FHIRException, IOException {
		InputStream in = getResourceAsStream("cda-it-observation-st.xml");

		String cdaObservation = IOUtils.toString(in, StandardCharsets.UTF_8);
		Resource resource = getEngine().transformToFhir(cdaObservation,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/TestObservation");
		Observation obs = (Observation) resource;

		assertEquals("Nessun Trauma riscontrato", obs.getValueStringType().getValue());

		assertNotNull(resource);
	}

	@Test
	void TestObservationStR2b() throws FHIRException, IOException {
		InputStream in = getResourceAsStream("cda-it-observation-st-r2b.xml");

		String cdaObservation = IOUtils.toString(in, StandardCharsets.UTF_8);
		Resource resource = getEngine().transformToFhir(cdaObservation,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/TestObservationStR2b");
		Observation obs = (Observation) resource;

		assertEquals("prova", obs.getValueStringType().getValue());

		assertNotNull(resource);
	}

	@Test
	void TestObservationCs() throws FHIRException, IOException {
		InputStream in = getResourceAsStream("cda-it-observation-cs.xml");

		String cdaObservation = IOUtils.toString(in, StandardCharsets.UTF_8);
		Resource resource = getEngine().transformToFhir(cdaObservation,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/TestObservation");
		Observation obs = (Observation) resource;

		assertEquals("completed", obs.getValueCodeableConcept().getCoding().get(0).getCode());

		assertNotNull(resource);
	}


	@Test
	void TestObservationCondition() throws FHIRException, IOException {
		InputStream in = getResourceAsStream("cda-it-observation-condition.xml");

		String cdaObservation = IOUtils.toString(in, StandardCharsets.UTF_8);
		Resource resource = getEngine().transformToFhir(cdaObservation,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/TestObservationConditionCoding");
		Condition condition = (Condition) resource;
		assertEquals("60975-0", condition.getCode().getCodingFirstRep().getCode());

		assertNotNull(resource);
	}

	@Test
	void TestSecond() throws FHIRException, IOException {
		String result = getEngine().transform(cdaLabItaly,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY",
				true);
		assertNotNull(result);
	}

	@Test
	void TestFhirToCda() throws FHIRException, IOException {
		String bundleString = "<Bundle xmlns=\"http://hl7.org/fhir\">" + //
						"<id value=\"test\"/>" + //
						"<identifier>" + //
						"  <system value=\"urn:ietf:rfc:3986\"/>" + //
						"  <value value=\"urn:uuid:6b6ed376-a7da-44cb-92d1-e75ce1ae73b0\"/>" + //
						"</identifier>" + //
						"<type value=\"document\"/>" + //
						"<timestamp value=\"2012-02-04T14:05:00+01:00\"/>"+
						"	<entry>" + //
						"        <fullUrl value=\"urn:uuid:d543ae7b-3a94-4a2a-a120-6ce2ee3027fc\"/>" + //
						"        <resource>" + //
						"            <Composition>" + //
						"                <id value=\"d543ae7b-3a94-4a2a-a120-6ce2ee3027fc\"/>" + //
						"                <section>" + //
						"                    <title value=\"Medikamentenliste\"/>" + //
						"                </section>" + //
						"                <section>" + //
						"                    <title value=\"Kommentar\"/>" + //
						"                    <text>" + //
						"                        <status value=\"generated\"/>" + //
						"                        <div xmlns=\"http://www.w3.org/1999/xhtml\">" + //
						"                            <h2>Kommentar</h2>"
						+ "														<p>" + //
						"                                 Medication Treatment" + //
						"                            </p>" + //
						"                        </div>" + //
						"                    </text>" + //
						"                </section>" + //
						"            </Composition>" + //
						"        </resource>" + //
						"    </entry>" + //
						
						"</Bundle>";
		String result = getEngine().transform(bundleString,
														  false,
														  "http://fhir.ch/ig/cda-fhir-maps/StructureMap/BundleToCda",
														  false);
		assertEquals("2.16.840.1.113883.1.3",
						getEngine().evaluateFhirPath(result, false, "typeId.root"));
//		assertEquals("POCD_MT000040UV02",
//						getEngine().evaluateFhirPath(result, false, "typeId.extension"));
		assertNotNull(result);
		assertTrue(result.indexOf("20120204140500+0100")>0);
		assertTrue(result.indexOf("6B6ED376-A7DA-44CB-92D1-E75CE1AE73B0")>0);
		assertTrue(result.indexOf("6B6ED376-A7DA-44CB-92D1-E75CE1AE73B0")>0);
		assertTrue(result.indexOf("caption")>0);
		assertTrue(result.indexOf("Kommentar")>0);
			assertEquals("2012-02-04T14:05:00+01:00",
				getEngine().evaluateFhirPath(result, false, "effectiveTime.value"));
	}

	@Test
	void TestThird() throws FHIRException, IOException {
		String result = getEngine().transform(cdaLabItaly,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY",
				true);
		assertNotNull(result);
	}

	@Test
	void TestValidateCdaIt() throws FHIRException, IOException, EOperationOutcome, URISyntaxException {
	  ch.ahdis.matchbox.engine.CdaMappingEngine  engine = new CdaMappingEngine.CdaMappingEngineBuilder().getCdaEngineR4();
		String fhirBundle = getEngine().transform(cdaLabItaly,
				false,
				"http://salute.gov.it/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY",
				false);
		assertNotNull(fhirBundle);
		log.debug(fhirBundle);
		String cda = getEngine().transform(fhirBundle,
				false,
				"http://fhir.ch/ig/cda-fhir-maps/StructureMap/BundleToCda",
				false);
		log.debug(cda);
		assertNotNull(cda);
		// check that we do not have an "item in deserialization" see https://github.com/ahdis/matchbox/issues/196
		assertTrue(cda.indexOf("item") < 0);
		CompareUtil.logMemory();
	}

	private int errors(OperationOutcome op) {
		int i = 0;
		for (OperationOutcomeIssueComponent vm : op.getIssue()) {
			if (vm.getSeverity() == IssueSeverity.ERROR || vm.getSeverity() == IssueSeverity.FATAL) {
				// eg ('tel: 390 666 0581')
				if (vm.getDetails().getText().startsWith("URI values cannot have whitespace")) {
					continue;
				}
				// https://terminology.hl7.org/5.0.0/ValueSet-v3-RoleClassAssignedEntity.json.html
				// has a filter with an is-a concept to ASSIGEND and this cannot be evaluated by
				// org.hl7.fhir.r5.terminologies.ValueSetCheckerSimple
        if (vm.getDetails().getText().startsWith(
                "The System URI could not be determined for the code 'it-IT'")) {
              continue;
            }
				if (vm.getDetails().getText().startsWith(
						"The value provided ('it-IT') was not found in the value set 'All Languages'")) {
					continue;
				}
        if (vm.getDetails().getText().startsWith(
                "The value provided ('ASSIGNED') was not found in the value set 'RoleClassAssignedEntity'")) {
              continue;
            }
        
        if (vm.getDetails().getText().startsWith(
                "IntendedRecipient.classCode: minimum required = 1,")) {
              continue;
            }
        
        if (vm.getDetails().getText().startsWith(
                "ServiceEvent.classCode: minimum required = 1, ")) {
              continue;
            }
        
        if (vm.getDetails().getText().startsWith(
                "HealthCareFacility.classCode: minimum required = 1, ")) {
              continue;
            }
        
        if (vm.getDetails().getText().startsWith(
                "PlayingEntity.classCode: minimum required = 1, ")) {
              continue;
            }
        if (vm.getDetails().getText().startsWith(
                "ObservationRange.classCode: minimum required = 1, ")) {
              continue;
            }
        if (vm.getDetails().getText().startsWith(
                "LabCriterion.classCode: minimum required = 1, ")) {
              continue;
            }
        if (vm.getDetails().getText().startsWith(
                "LabCriterion.moodCode: minimum required = 1, ")) {
              continue;
            }
        
				// id value 'ESAMI_URINE' is not valid
				// id value 'ALBUMINA_URINE' is not valid
				if (vm.getDetails().getText().startsWith(
						"id value ")) {
					continue;
				}

				log.error(vm.getDetails().getText());
				++i;
			}
		}
		return i;
	}

	private static InputStream getResourceAsStream(final String filename) {
		return CdaToFhirTransformTests.class.getResourceAsStream("/cda/" + filename);
	}

	public String getFileAsStringFromResources(String file) throws IOException {
		InputStream in = CdaToFhirTransformTests.class.getResourceAsStream("/cda/" + file);
		return IOUtils.toString(in, StandardCharsets.UTF_8);
	}

}
