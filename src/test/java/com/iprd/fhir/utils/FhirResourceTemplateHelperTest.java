package com.iprd.fhir.utils;


import org.junit.Test;

import com.ctc.wstx.shaded.msv_core.reader.State;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;


public class FhirResourceTemplateHelperTest {

	@Test
	public void testState() {
		Location test = FhirResourceTemplateHelper.state("oyo");
		assertEquals(test.getName(), "Oyo");
		assertEquals(test.getAddress(), "Oyo");
		assertEquals(test.getPhysicalType(), "jdn");
		assertEquals(test.getStatus(), "active");
		assertEquals(test.getMode(), "instance");
	}
	
	@Test
	public void testLga() {
		Location test = FhirResourceTemplateHelper.lga("Ibadan South West", "oyo");
		assertEquals(test.getName(), "Ibadan South West");
		assertEquals(test.getAddress(), "Oyo");
		assertEquals(test.getPhysicalType(), "jdn");
		assertEquals(test.getStatus(), "active");
		assertEquals(test.getMode(), "instance");
	}
	
	@Test
	public void testWard() {
		Location test = FhirResourceTemplateHelper.ward("Agbokojo", "oyo");
		assertEquals(test.getName(), "Agbokojo");
		assertEquals(test.getAddress(), "Oyo");
		assertEquals(test.getPhysicalType(), "jdn");
		assertEquals(test.getStatus(), "active");
		assertEquals(test.getMode(), "instance");
	}
	
	@Test
	public void testClinic() {
		Organization test = FhirResourceTemplateHelper.clinic("St Lucia Hospital", "oyo");
		assertEquals(test.getName(),"St Lucia Hospital");
		assertEquals(test.getAddress(), "oyo");
		assertEquals(test.getIdentifier(),"19145158");
		assertEquals(test.getIdentifier(),"30/08/1/1/1/0019");
		assertEquals(test.getType(),"prov");
	}
}
