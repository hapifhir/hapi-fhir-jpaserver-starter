package com.iprd.fhir.utils;

import java.lang.String;
import org.keycloak.representations.idm.GroupRepresentation;

public class KeycloakGroupTemplateHelper {
	
	private static String STATE = "state";
	private static String LGA = "lga";
	private static String WARD = "ward";
	private static String FACILITY = "facility";
	
	public static GroupRepresentation stateGroup(String name, String fhirResourceId) {
		GroupRepresentation stateGroupRep = new GroupRepresentation();
		stateGroupRep.setName(name);
		stateGroupRep.singleAttribute("type", STATE);
		stateGroupRep.singleAttribute("fhirLocationId", fhirResourceId);
		return stateGroupRep;
	}
	
	public static GroupRepresentation lgaGroup(String name, String parentId, String fhirResourceId) {
		GroupRepresentation lgaGroupRep = new GroupRepresentation();
		lgaGroupRep.setName(name);
		lgaGroupRep.singleAttribute("type", LGA);
		lgaGroupRep.singleAttribute("parent", parentId);
		lgaGroupRep.singleAttribute("fhirLocationId",fhirResourceId);
		return lgaGroupRep;
	}
	
	public static GroupRepresentation wardGroup(String name, String parentId, String fhirResourceId) {
		GroupRepresentation wardGroupRep = new GroupRepresentation();
		wardGroupRep.setName(name);
		wardGroupRep.singleAttribute("type", WARD);
		wardGroupRep.singleAttribute("parent", parentId);
		wardGroupRep.singleAttribute("fhirLocationId",fhirResourceId);
		return wardGroupRep;
	}
	
	public static GroupRepresentation facilityGroup(String name ,String parentId, String fhirResourceId, String facilityLevel, String ownership, String facilityUID, String facilityCode) {
		GroupRepresentation facilityGroupRep = new GroupRepresentation();
		facilityGroupRep.setName(name);
		facilityGroupRep.singleAttribute("type", FACILITY);
		facilityGroupRep.singleAttribute("parent", parentId);
		facilityGroupRep.singleAttribute("facilityLevel",facilityLevel);
		facilityGroupRep.singleAttribute("fhirOrganizationId",fhirResourceId);
		facilityGroupRep.singleAttribute("ownership",ownership);
		facilityGroupRep.singleAttribute("facilityUID",facilityUID);
		facilityGroupRep.singleAttribute("facilityCode",facilityCode);
		return facilityGroupRep;
	}
}
