package ca.uhn.fhir.jpa.starter.model;

import lombok.Data;

@Data
public class BulkUploadUserDetails {
	private String firstName;
	private String lastName;
	private String email;
	private String countryCode;
	private String phoneNumber;
	private String gender;
	private String birthDate;
	private String keycloakUserName;
	private String initialPassword;
	private String state;
	private String lga;
	private String ward;
	private String facilityUID;
	private String role;
	private String qualification;
	private String stateIdentifier;
	private String argusoftIdentifier;
	private String countryName;

	public BulkUploadUserDetails(String[] hcwData) {
		firstName = hcwData[0];
		lastName = hcwData[1];
		email = hcwData[2];
		countryCode = hcwData[3];
		phoneNumber = hcwData[4];
		gender = hcwData[5];
		birthDate = hcwData[6];
		keycloakUserName = hcwData[7];
		initialPassword = hcwData[8];
		state = hcwData[9];
		lga = hcwData[10];
		ward = hcwData[11];
		facilityUID = hcwData[12];
		role = hcwData[13];
		qualification = hcwData[14];
		stateIdentifier = hcwData[15];
		argusoftIdentifier = hcwData[16];
		countryName = hcwData[17];
	}
}
