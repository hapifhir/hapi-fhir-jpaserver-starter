package ca.uhn.fhir.jpa.starter.model;

public class OCLQrRequest {
	String baseUrl;
	String campGuid;
	String campName;
	String campUrl;
	String location;
	String locationPre;
	String timePre;
	String verticalCode;
	String verticalDescription;
	String userDefinedData;
	boolean humanReadableFlag;
	int errorCorrectionLevelBits;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getCampGuid() {
		return campGuid;
	}

	public void setCampGuid(String campGuid) {
		this.campGuid = campGuid;
	}

	public String getCampName() {
		return campName;
	}

	public void setCampName(String campName) {
		this.campName = campName;
	}

	public String getCampUrl() {
		return campUrl;
	}

	public void setCampUrl(String campUrl) {
		this.campUrl = campUrl;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocationPre() {
		return locationPre;
	}

	public void setLocationPre(String locationPre) {
		this.locationPre = locationPre;
	}

	public String getTimePre() {
		return timePre;
	}

	public void setTimePre(String timePre) {
		this.timePre = timePre;
	}

	public String getVerticalCode() {
		return verticalCode;
	}

	public void setVerticalCode(String verticalCode) {
		this.verticalCode = verticalCode;
	}

	public String getVerticalDescription() {
		return verticalDescription;
	}

	public void setVerticalDescription(String verticalDescription) {
		this.verticalDescription = verticalDescription;
	}

	public String getUserDefinedData() {
		return userDefinedData;
	}

	public void setUserDefinedData(String userDefinedData) {
		this.userDefinedData = userDefinedData;
	}

	public boolean isHumanReadableFlag() {
		return humanReadableFlag;
	}

	public void setHumanReadableFlag(boolean humanReadableFlag) {
		this.humanReadableFlag = humanReadableFlag;
	}

	public int getErrorCorrectionLevelBits() {
		return errorCorrectionLevelBits;
	}

	public void setErrorCorrectionLevelBits(int errorCorrectionLevelBits) {
		this.errorCorrectionLevelBits = errorCorrectionLevelBits;
	}
}
