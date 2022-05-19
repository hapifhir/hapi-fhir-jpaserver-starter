package com.iprd.fhir.utils;

import java.util.List;
import java.util.ArrayList;
import java.lang.String;


import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Location.LocationMode;
import org.hl7.fhir.r4.model.Location.LocationStatus;
import org.hl7.fhir.r4.model.ContactPoint;


public class FhirResourceTemplateHelper {
	private static String CODE_JDN = "jdn";
	private static String DISPLAY_JURISDICTION = "Jurisdiction";
	private static String SYSTEM_JURISDICTION = "http://hl7.org/fhir/ValueSet/location-physical-type";
	private static String CLINIC_FACILITYCODE = "19145158";
	private static String CLINIC_FACILITYUID = "30/08/1/1/1/0019";
	private static String CODE_CLINIC = "prov";
	private static String DISPLAY_CLINIC = "Healthcare Provider";
	private static String SYSTEM_CLINIC = "	http://hl7.org/fhir/ValueSet/organization-type";

	private static String CONTACT_CLINIC = "+234-9087890123";
	public static Location state(String name)
	{
		Location state = new Location();
		Address stateAddress = new Address();
		CodeableConcept statePhysicalType = new CodeableConcept();
		Coding physicalTypeCoding = new Coding();
		physicalTypeCoding
		.setCode(CODE_JDN)
		.setDisplay(DISPLAY_JURISDICTION)
		.setSystem(SYSTEM_JURISDICTION);
		statePhysicalType.addCoding(physicalTypeCoding);
		stateAddress.setState(name);
		state.setName(name);
		state.setAddress(stateAddress);
		state.setStatus(LocationStatus.ACTIVE);
		state.setMode(LocationMode.INSTANCE);
		state.setPhysicalType(statePhysicalType);
	
		return state;
	}
	public static Location lga(String name, String address) {
		Location lga = new Location();
		Address lgaAddress = new Address();
		CodeableConcept lgaPhysicalType = new CodeableConcept();
		Coding physicalTypeCoding = new Coding();
		physicalTypeCoding
		.setCode(CODE_JDN)
		.setDisplay(DISPLAY_JURISDICTION)
		.setSystem(SYSTEM_JURISDICTION);
		lgaPhysicalType.addCoding(physicalTypeCoding);
		lgaAddress.setState(address);
		lga.setName(name);
		lga.setAddress(lgaAddress);
		lga.setStatus(LocationStatus.ACTIVE);
		lga.setMode(LocationMode.INSTANCE);
		lga.setPhysicalType(lgaPhysicalType);
		lga.setName(name);
		
		return lga;
	}
	public static Location ward(String name, String address) {
		Location ward = new Location();
		Address wardAddress = new Address();
		CodeableConcept wardPhysicalType = new CodeableConcept();
		Coding physicalTypeCoding = new Coding();
		physicalTypeCoding
		.setCode(CODE_JDN)
		.setDisplay(DISPLAY_JURISDICTION)
		.setSystem(SYSTEM_JURISDICTION);
		wardPhysicalType.addCoding(physicalTypeCoding);
		wardAddress.setState(address);
		ward.setName(name);
		ward.setAddress(wardAddress);
		ward.setStatus(LocationStatus.ACTIVE);
		ward.setMode(LocationMode.INSTANCE);
		ward.setPhysicalType(wardPhysicalType);
		ward.setName(name);
		
		return ward;
	}
	
	public static Organization clinic(String name, String address,String facilityUID,String facilityCode,String contact) {
		Organization clinic = new Organization();
		List<CodeableConcept> codeableConcepts = new ArrayList<>();
		List<Address> addresses = new ArrayList<>();
		Address address2 = new Address();
		address2.setState(address);
		addresses.add(address2);
		clinic.setAddress(addresses);
		List<Identifier> identifiers = new ArrayList<>();
		Identifier identifier1 = new Identifier();
		Identifier identifier2 = new Identifier();
		identifier1.setId(facilityCode);
		identifier2.setId(facilityUID);
		identifiers.add(identifier1);
		identifiers.add(identifier2);
		clinic.setIdentifier(identifiers);
		List<ContactPoint> contactPoints = new ArrayList<>();
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setValue(String.valueOf(new org.hl7.fhir.String().withValue(contact)));
		clinic.setTelecom(contactPoints);
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setCode(CODE_CLINIC);
		coding.setSystem(SYSTEM_CLINIC);
		coding.setDisplay(DISPLAY_CLINIC);
		codeableConcept.addCoding(coding);
		codeableConcept.setText(DISPLAY_CLINIC);
		codeableConcepts.add(codeableConcept);
		clinic.setType(codeableConcepts);
		clinic.setName(name);
		return clinic;
	}
}
