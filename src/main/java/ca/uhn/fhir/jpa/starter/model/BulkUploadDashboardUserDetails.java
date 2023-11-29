package ca.uhn.fhir.jpa.starter.model;

import lombok.Data;

@Data
public class BulkUploadDashboardUserDetails {
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String countryCode;
	private String gender;
	private String birthDate;
	private String userName;
	private String initialPassword;
	private String facilityUID;
	private String role;
	private String organizationName;
	private String type;
	public BulkUploadDashboardUserDetails(String[] hcwData) {
		firstName = hcwData[0];
		lastName = hcwData[1];
		email = hcwData[2];
		phoneNumber = hcwData[3];
		countryCode = hcwData[4];
		gender = hcwData[5];
		birthDate = hcwData[6];
		userName = hcwData[7];
		initialPassword = hcwData[8];
		facilityUID = hcwData[9];
		role = hcwData[10];
		organizationName = hcwData[11];
		type = hcwData[12];
	}
}
