package com.iprd.fhir.utils;

import java.lang.String;
import java.util.Arrays;
import java.util.List;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public class KeycloakTemplateHelper {
	
	private static String STATE = "state";
	private static String LGA = "lga";
	private static String WARD = "ward";
	private static String FACILITY = "facility";
	
	public static GroupRepresentation stateGroup(String name, String fhirResourceId) {
		GroupRepresentation stateGroupRep = new GroupRepresentation();
		stateGroupRep.setName(name);
		stateGroupRep.singleAttribute("type", STATE);
		stateGroupRep.singleAttribute("location_id", fhirResourceId);
		return stateGroupRep;
	}
	
	public static GroupRepresentation lgaGroup(String name, String parentId, String fhirResourceId) {
		GroupRepresentation lgaGroupRep = new GroupRepresentation();
		lgaGroupRep.setName(name);
		lgaGroupRep.singleAttribute("type", LGA);
		lgaGroupRep.singleAttribute("parent", parentId);
		lgaGroupRep.singleAttribute("location_id",fhirResourceId);
		return lgaGroupRep;
	}
	
	public static GroupRepresentation wardGroup(String name, String parentId, String fhirResourceId) {
		GroupRepresentation wardGroupRep = new GroupRepresentation();
		wardGroupRep.setName(name);
		wardGroupRep.singleAttribute("type", WARD);
		wardGroupRep.singleAttribute("parent", parentId);
		wardGroupRep.singleAttribute("location_id",fhirResourceId);
		return wardGroupRep;
	}
	
	public static GroupRepresentation facilityGroup(String name ,String parentId, String fhirResourceId, String facilityLevel, String ownership, String facilityUID, String facilityCode) {
		GroupRepresentation facilityGroupRep = new GroupRepresentation();
		facilityGroupRep.setName(facilityUID);
		facilityGroupRep.singleAttribute("type", FACILITY);
		facilityGroupRep.singleAttribute("facility_name",name);
		facilityGroupRep.singleAttribute("parent", parentId);
		facilityGroupRep.singleAttribute("facility_level",facilityLevel);
		facilityGroupRep.singleAttribute("organization_id",fhirResourceId);
		facilityGroupRep.singleAttribute("ownership",ownership);
		facilityGroupRep.singleAttribute("facility_code",facilityCode);
		return facilityGroupRep;
	}
	
	public static UserRepresentation user(String firstName,String lastName,String email,String userName,String password,String phoneNumber,String countryCode, String practitionerId, String practitionerRoleId, String stateGroup, String lgaGroup, String wardGroup, String facilityGroup) {
		UserRepresentation user = new UserRepresentation();
		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(password);
		user.setCredentials(Arrays.asList(credential));
		user.setGroups(Arrays.asList(stateGroup, lgaGroup, wardGroup, facilityGroup));
		user.setUsername(userName);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.singleAttribute("phoneNumber", countryCode+phoneNumber);
		user.singleAttribute("type","HCW");
		user.singleAttribute("practitioner_id", practitionerId);
		user.singleAttribute("practitioner_role_id", practitionerRoleId);
		user.setEnabled(true);
		return user;
	}
}
