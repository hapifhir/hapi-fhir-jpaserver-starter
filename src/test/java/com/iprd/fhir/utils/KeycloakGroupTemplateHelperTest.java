package com.iprd.fhir.utils;

import org.junit.Test;
import org.keycloak.representations.idm.GroupRepresentation;

import com.github.andrewoma.dexx.collection.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

public class KeycloakGroupTemplateHelperTest {

	@Test
	public void testStateGroup() {
		GroupRepresentation stateGroupRep = KeycloakGroupTemplateHelper.stateGroup("Oyo","12345");
		Map<String, List<String>> attributes = stateGroupRep.getAttributes();
		assertEquals(stateGroupRep.getName(), "Oyo");
		assertEquals(attributes.get("type").get(0), "state");
		assertEquals(attributes.get("fhirLocationId").get(0), "12345");
	}
	
	@Test
	public void testLgaGroup() {
		GroupRepresentation lgaGroupRep = KeycloakGroupTemplateHelper.lgaGroup("Ibadan South West","parent123","location123");
		Map<String, List<String>> attributes = lgaGroupRep.getAttributes();
		assertEquals(lgaGroupRep.getName(), "Ibadan South West");
		assertEquals(attributes.get("type").get(0), "lga");
		assertEquals(attributes.get("parent").get(0), "parent123");
		assertEquals(attributes.get("fhirLocationId").get(0), "location123");
	}
	
	@Test
	public void testWardGroup() {
		GroupRepresentation wardGroupRep = KeycloakGroupTemplateHelper.wardGroup("Agbokojo","parent124","location124");
		Map<String, List<String>> attributes = wardGroupRep.getAttributes();
		assertEquals(wardGroupRep.getName(), "Agbokojo");
		assertEquals(attributes.get("type").get(0), "ward");
		assertEquals(attributes.get("parent").get(0), "parent124");
		assertEquals(attributes.get("fhirLocationId").get(0), "location124");
	}
	
	@Test
	public void facilityGroupTest() {
		GroupRepresentation facilityGroupRep = KeycloakGroupTemplateHelper.facilityGroup("St Lucia Hospital" ,"parent125", "organization125", "primary", "public", "19145158", "30/08/1/1/1/0019");
		Map<String, List<String>> attributes = facilityGroupRep.getAttributes();
		assertEquals(facilityGroupRep.getName(), "St Lucia Hospital");
		assertEquals(attributes.get("type").get(0), "facility");
		assertEquals(attributes.get("parent").get(0), "parent125");
		assertEquals(attributes.get("fhirOrganizationId").get(0), "organization125");
		assertEquals(attributes.get("facilityLevel").get(0), "primary");
		assertEquals(attributes.get("ownership").get(0), "public");
		assertEquals(attributes.get("facilityUID").get(0), "19145158");
		assertEquals(attributes.get("facilityCode").get(0), "30/08/1/1/1/0019");
	}
}
