package com.iprd.fhir.utils;

import org.hl7.fhir.r4.model.*;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FhirResourceTemplateHelperTest {
	@Test
	public void testState() {
		Organization state = FhirResourceTemplateHelper.state("oyo","nigeria","1234");
		assertEquals(state.getName(), "oyo");
		assertEquals(state.getType().get(0).getText(), "Government");
		assertEquals(state.getAddress().get(0).getState(), "oyo");
	}
	
	@Test
	public void testLga() {
		Organization test = FhirResourceTemplateHelper.lga("Ibadan South West", "oyo","12345");
		assertEquals(test.getName(), "Ibadan South West");
		assertEquals(test.getAddress().get(0).getState(), "oyo");
		assertEquals(test.getAddress().get(0).getDistrict(),"Ibadan South West");
		assertEquals(test.getType().get(0).getText(), "Government");
		assertEquals(test.getPartOf().getReference(),"Organization/"+"12345");
	}
	
	@Test
	public void testWard() {
		Organization test = FhirResourceTemplateHelper.ward("oyo", "Ibadan South West", "Agbokojo","12345");
		assertEquals(test.getName(), "Agbokojo");
		assertEquals(test.getAddress().get(0).getState(), "oyo");
		assertEquals(test.getAddress().get(0).getDistrict(),"Ibadan South West");
		assertEquals(test.getAddress().get(0).getCity(),"Agbokojo");
		assertEquals(test.getType().get(0).getText(), "Government");
		assertEquals(test.getPartOf().getReference(),"Organization/"+"12345");
	}
	
	@Test
	public void testClinic() {
		Organization test = FhirResourceTemplateHelper.clinic("St Lucia Hospital", "19145158", "30/08/1/1/1/0019","+234","78945645796","oyo", "Ibadan South West", "Agbokojo","sds","58562");
		assertEquals(test.getName(),"St Lucia Hospital");
		assertEquals(test.getAddress().get(0).getState(), "oyo");
		assertEquals(test.getAddress().get(0).getDistrict(), "Ibadan South West");
		assertEquals(test.getAddress().get(0).getCity(), "Agbokojo");
		assertEquals(test.getIdentifier().get(0).getId(),"30/08/1/1/1/0019");
		assertEquals(test.getIdentifier().get(1).getId(),"19145158");
		assertEquals(test.getTelecom().get(0).getValue(), "+234"+"78945645796");
		assertEquals(test.getType().get(0).getText(),"Healthcare Provider");
		assertEquals(test.getPartOf().getReference(),"Organization/"+"sds");
	}
	
	@Test
	public void testPractitioner() throws Exception {
		Practitioner test = FhirResourceTemplateHelper.hcw("temp", "nurse", "+91", "8150038173", "female", "09/22/1995", "Oyo", "Ibadan-South-West", "Agbokojo", "19145158", "Nurse", "Bachelor-of-Nursing","N123","52620");
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
		PractitionerRole test = FhirResourceTemplateHelper.practitionerRole("Nurse", "Bachelor-of-Nursing", "32621379-fe59-49a0-93e9-203226d0cf52", "32621379-fe59-49a0-93e9-203226d0cf52");
		assertEquals(test.getCode().get(0), "Nurse");
		assertEquals(test.getCode().get(1), "Bachelor-of-Nursing");
		assertEquals(test.getId(),"32621379-fe59-49a0-93e9-203226d0cf52");
	}
}
