package com.iprd.fhir.utils;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FhirResourceTemplateHelperTest {
	@Test
	public void testState() {
		Location state = FhirResourceTemplateHelper.state("oyo");
		assertEquals(state.getName(), "Oyo");
		assertEquals(state.getAddress(), "Oyo");
		assertEquals(state.getPhysicalType(), "jdn");
		assertEquals(state.getStatus(), "active");
		assertEquals(state.getMode(), "instance");
	}
	
	@Test
	public void testLga() {
		Location test = FhirResourceTemplateHelper.lga("Ibadan South West", "oyo");
		assertEquals(test.getName(), "Ibadan South West");
		assertEquals(test.getAddress().getState(), "Oyo");
		assertEquals(test.getPhysicalType(), "jdn");
		assertEquals(test.getStatus(), "active");
		assertEquals(test.getMode(), "instance");
	}
	
	@Test
	public void testWard() {
		Location test = FhirResourceTemplateHelper.ward("Agbokojo", "oyo", "Agbokojo");
		assertEquals(test.getName(), "Agbokojo");
		assertEquals(test.getAddress().getState(), "Oyo");
		assertEquals(test.getAddress().getDistrict(), "Agbokojo");
		assertEquals(test.getPhysicalType(), "jdn");
		assertEquals(test.getStatus(), "active");
		assertEquals(test.getMode(), "instance");
	}
	
	@Test
	public void testClinic() {
		Organization test = FhirResourceTemplateHelper.clinic("St Lucia Hospital", "19145158", "30/08/1/1/1/0019","+234","78945645796","oyo", "Ibadan South West", "Agbokojo");
		assertEquals(test.getName(),"St Lucia Hospital");
		assertEquals(test.getAddress().get(0), "oyo");
		assertEquals(test.getAddress().get(1), "Ibadan South West");
		assertEquals(test.getAddress().get(2), "Agbokojo");
		assertEquals(test.getIdentifier(),"19145158");
		assertEquals(test.getIdentifier(),"30/08/1/1/1/0019");
		assertEquals(test.getTelecom(), "+234"+"78945645796");
		assertEquals(test.getType(),"prov");
	}
	
	@Test
	public void testPractitioner() throws Exception {
		Practitioner test = FhirResourceTemplateHelper.hcw("temp", "nurse", "+91", "8150038173", "female", "09/22/1995", "Oyo", "Ibadan-South-West", "Agbokojo", "19145158", "Nurse", "Bachelor-of-Nursing","N123");
		assertEquals(test.getName().get(0), "nurse");
		assertEquals(test.getName().get(1), "temp");
		assertEquals(test.getTelecom(), "+91"+"8150038173");
		assertEquals(test.getGender(), "female");
		assertEquals(test.getBirthDate(), "09/22/1995");
		assertEquals(test.getAddress().get(0), "Oyo");
		assertEquals(test.getAddress().get(1), "Ibadan-South-West");
		assertEquals(test.getAddress().get(2), "Agbokojo");
		assertEquals(test.getIdentifier(), "19145158");
		assertEquals(test.getQualification(), "Bachelor-of-Nursing");
	}
	
	@Test
	public void testPractitionerRole() {
		PractitionerRole test = FhirResourceTemplateHelper.practitionerRole("Nurse", "Bachelor-of-Nursing", "32621379-fe59-49a0-93e9-203226d0cf52");
		assertEquals(test.getCode().get(0), "Nurse");
		assertEquals(test.getCode().get(1), "Bachelor-of-Nursing");
		assertEquals(test.getId(),"32621379-fe59-49a0-93e9-203226d0cf52");
	}
}