package ca.uhn.fhir.jpa.starter.model;

import lombok.Data;

@Data
public class BulkUploadClinicDetails {
	private String stateName;
	private String lgaName;
	private String wardName;
	private String facilityUID;
	private String facilityCode;
	private String countryCode;
	private String phoneNumber;
	private String facilityName;
	private String type;
	private String ownership;
	private String argusoftIdentifier;
	private String longitude;
	private String latitude;
	private String pluscode;
	private String countryName;

	public BulkUploadClinicDetails(String[] csvData) {
		stateName = csvData[0];
		lgaName = csvData[1];
		wardName = csvData[2];
		facilityUID = csvData[3];
		facilityCode = csvData[4];
		countryCode = csvData[5];
		phoneNumber = csvData[6];
		facilityName = csvData[7];
		type = csvData[8];
		ownership = csvData[9];
		argusoftIdentifier = csvData[10];
		longitude = csvData[11];
		latitude = csvData[12];
		pluscode = csvData[13];
		countryName = csvData[14];
	}
}
