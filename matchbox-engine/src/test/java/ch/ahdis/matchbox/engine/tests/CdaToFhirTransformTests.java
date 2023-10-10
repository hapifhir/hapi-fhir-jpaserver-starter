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
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.spi.CalendarNameProvider;

class CdaToFhirTransformTests {

	static private CdaMappingEngine engine;
	static String cdaLabItaly;
    

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CdaToFhirTransformTests.class);

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		InputStream in = getResourceAsStream("cda-it.xml");
		cdaLabItaly = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private CdaMappingEngine getEngine() {
		if (engine == null) {
			try {
				engine = new CdaMappingEngine.CdaMappingEngineBuilder().getEngine();
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
                sm = getEngine().parseMap(getFileAsStringFromResources("cda-it-observation-condition.map"));
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
		assertEquals("POCD_MT000040UV02",
						 getEngine().evaluateFhirPath(cdaLabItaly, false, "typeId.extension"));
		assertEquals("active", getEngine().evaluateFhirPath(cdaLabItaly, false, "statusCode.code"));
// 	<effectiveTime value="20220330112426+0100"/>
		assertEquals("2022-03-30T11:24:26+01:00",
						 getEngine().evaluateFhirPath(cdaLabItaly, false, "effectiveTime.value"));
		assertEquals("1993-06-19",
						 getEngine().evaluateFhirPath(cdaLabItaly,
																false,
																"recordTarget.patientRole.patient.birthTime.value"));
		assertEquals("Verdi",
						 getEngine().evaluateFhirPath(cdaLabItaly,
																false,
																"recordTarget.patientRole.patient.name.family.xmlText"));
		assertEquals("Giuseppe",
						 getEngine().evaluateFhirPath(cdaLabItaly,
																false,
																"recordTarget.patientRole.patient.name.given.xmlText"));
	}

	@Test
	void TestFhirPathObservationIt() throws FHIRException, IOException {
    	InputStream in = getResourceAsStream("cda-it-observation.xml");
		String observationIt = IOUtils.toString(in, StandardCharsets.UTF_8);
        assertEquals("2022-03-01T22:11:22+01:00", getEngine().evaluateFhirPath(observationIt, false, "value.high.value"));
    }


	@Test
	void TestInitial() throws FHIRException, IOException {
		String result = getEngine().transform(cdaLabItaly,
														  false,
														  "http://salute.gov.it/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY",
														  true);
		assertNotNull(result);
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
	void TestThird() throws FHIRException, IOException {
		String result = getEngine().transform(cdaLabItaly,
														  false,
														  "http://salute.gov.it/ig/cda-fhir-maps/StructureMap/RefertodilaboratorioFULLBODY",
														  true);
		assertNotNull(result);
	}


	@Test
	void TestValidateCdaIt() throws FHIRException, IOException, EOperationOutcome {
		InputStream in = getResourceAsStream("/cda-it.xml");
		OperationOutcome outcome = getEngine().validate(in,
																		FhirFormat.XML,
																		"http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument");
		assertNotNull(outcome);

		assertEquals(0, errors(outcome));
	}

	private int errors(OperationOutcome op) {
		int i = 0;
		for (OperationOutcomeIssueComponent vm : op.getIssue()) {
			if (vm.getSeverity() == IssueSeverity.ERROR || vm.getSeverity() == IssueSeverity.FATAL) {
				// eg ('tel: 390 666 0581')
				if (vm.getDetails().getText().startsWith("URI values cannot have whitespace")) {
    				continue;
				}
				// https://terminology.hl7.org/5.0.0/ValueSet-v3-RoleClassAssignedEntity.json.html has a filter with an is-a concept to ASSIGEND and this cannot be evaluated by org.hl7.fhir.r5.terminologies.ValueSetCheckerSimple
				if (vm.getDetails().getText().startsWith(
					"The value provided ('ASSIGNED') is not in the value set 'RoleClassAssignedEntity'")) {
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
