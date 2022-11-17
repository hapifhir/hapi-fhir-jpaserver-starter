package com.iprd.fhir.utils;

import org.junit.Test;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.github.andrewoma.dexx.collection.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

public class KeycloakGroupTemplateHelperTest {

	@Test
	public void testStateGroup() {
		GroupRepresentation stateGroupRep = KeycloakTemplateHelper.stateGroup("Oyo","12345");
		Map<String, List<String>> attributes = stateGroupRep.getAttributes();
		assertEquals(stateGroupRep.getName(), "Oyo");
		assertEquals(attributes.get("type").get(0), "state");
		assertEquals(attributes.get("location_id").get(0), "12345");
	}
	
	@Test
	public void testLgaGroup() {
		GroupRepresentation lgaGroupRep = KeycloakTemplateHelper.lgaGroup("Ibadan South West","parent123","location123");
		Map<String, List<String>> attributes = lgaGroupRep.getAttributes();
		assertEquals(lgaGroupRep.getName(), "Ibadan South West");
		assertEquals(attributes.get("type").get(0), "lga");
		assertEquals(attributes.get("parent").get(0), "parent123");
		assertEquals(attributes.get("location_id").get(0), "location123");
	}
	
	@Test
	public void testWardGroup() {
		GroupRepresentation wardGroupRep = KeycloakTemplateHelper.wardGroup("Agbokojo","parent124","location124");
		Map<String, List<String>> attributes = wardGroupRep.getAttributes();
		assertEquals(wardGroupRep.getName(), "Agbokojo");
		assertEquals(attributes.get("type").get(0), "ward");
		assertEquals(attributes.get("parent").get(0), "parent124");
		assertEquals(attributes.get("location_id").get(0), "location124");
	}
	
	@Test
	public void facilityGroupTest() {
		GroupRepresentation facilityGroupRep = KeycloakTemplateHelper.facilityGroup("St Lucia Hospital" ,"parent125", "organization125", "Location125", "primary", "public", "19145158", "30/08/1/1/1/0019", "16212");
		Map<String, List<String>> attributes = facilityGroupRep.getAttributes();
		assertEquals(facilityGroupRep.getName(), "19145158");
		assertEquals(attributes.get("type").get(0), "facility");
		assertEquals(attributes.get("parent").get(0), "parent125");
		assertEquals(attributes.get("organization_id").get(0), "organization125");
		assertEquals(attributes.get("facility_level").get(0), "primary");
		assertEquals(attributes.get("ownership").get(0), "public");
		assertEquals(attributes.get("facilityUID").get(0), "19145158");
		assertEquals(attributes.get("facility_code").get(0), "30/08/1/1/1/0019");
		assertEquals(attributes.get("argusoft_identifier").get(0),"16212");
	}
	
	@Test
	public void userTest() {
		UserRepresentation userRep = KeycloakTemplateHelper.user("temp", "nurse", "temp@test.org", "temp", "1234", "+91", "8150038173","48482551-e023-4515-82e8-241fa1c91ffc","5f613809-01cb-41d6-a041-10efb88e9167", "hcw", "stateGroup", "lgaGroup", "wardGroup", "clinicGroup", "95007");
		Map<String, List<String>> attributes = userRep.getAttributes();
		List<String> groups = userRep.getGroups();
		assertEquals(userRep.getFirstName(), "temp");
		assertEquals(userRep.getLastName(), "nurse");
		assertEquals(userRep.getEmail(), "temp@test.org");
		assertEquals(userRep.getUsername(), "temp");
//      assertEquals(userRep.getCredentials(), "1234");
		assertEquals(attributes.get("phoneNumber").get(0), "8150038173"+"+91");
		assertEquals(attributes.get("type").get(0), "hcw");
		assertEquals(attributes.get("practitioner_id").get(0), "48482551-e023-4515-82e8-241fa1c91ffc");
		assertEquals(attributes.get("practitioner_role_id").get(0), "5f613809-01cb-41d6-a041-10efb88e9167");
		assertEquals(groups.get(0),"stateGroup");
		assertEquals(groups.get(1),"lgaGroup");
		assertEquals(groups.get(2),"wardGroup");
		assertEquals(groups.get(3),"clinicGroup");
	}
}
