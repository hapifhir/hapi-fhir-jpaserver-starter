package com.iprd.fhir.utils;


import ca.uhn.fhir.jpa.starter.controller.SampleController;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestParam;

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
		Organization test = FhirResourceTemplateHelper.clinic("St Lucia Hospital", "19145158", "30/08/1/1/1/0019", "+234","90879067", "oyo", "Ibadan South West", "Agbokojo");
		assertEquals(test.getName(),"St Lucia Hospital");
		assertEquals(test.getAddress().get(0), "oyo");
		assertEquals(test.getAddress().get(1), "Ibadan South West");
		assertEquals(test.getAddress().get(2), "Agbokojo");
		assertEquals(test.getIdentifier(),"19145158");
		assertEquals(test.getIdentifier(),"30/08/1/1/1/0019");
		assertEquals(test.getType(),"prov");
		assertEquals(test.getTelecom(), "+234"+"90879067");
	}
}
