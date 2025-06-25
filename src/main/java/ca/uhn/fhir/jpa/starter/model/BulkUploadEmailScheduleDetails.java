package ca.uhn.fhir.jpa.starter.model;

import lombok.Data;

@Data
public class BulkUploadEmailScheduleDetails {
	private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
	private Integer id; // Only used for updates
	private String recipientEmail;
	private String scheduleType;
	private String emailSubject;
	private String orgId;
	private String adminOrg;

	public BulkUploadEmailScheduleDetails(String[] scheduleData, boolean isUpdate) {
		if (scheduleData == null || scheduleData.length < (isUpdate ? 6 : 5)) {
			throw new IllegalArgumentException("Insufficient data for email schedule");
		}
		int offset = isUpdate ? 1 : 0;
		if (isUpdate) {
			this.id = Integer.parseInt(scheduleData[0].trim());
		}
		this.recipientEmail = scheduleData[offset].trim();
		if (!this.recipientEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
			throw new IllegalArgumentException("Invalid email format: " + this.recipientEmail);
		}
		this.scheduleType = scheduleData[offset + 1].trim();
		if (!this.scheduleType.matches("^(daily|weekly|monthly)$")) {
			throw new IllegalArgumentException("Invalid schedule type: " + this.scheduleType);
		}
		this.emailSubject = scheduleData[offset + 2].trim();
		if (this.emailSubject.isEmpty()) {
			throw new IllegalArgumentException("Email subject cannot be empty");
		}
		this.orgId = scheduleData[offset + 3].trim();
		if (!this.orgId.matches(UUID_REGEX)) {
			throw new IllegalArgumentException("Invalid orgId format: " + this.orgId);
		}
		this.adminOrg = scheduleData[offset + 4].trim();
		if (!this.adminOrg.matches(UUID_REGEX)) {
			throw new IllegalArgumentException("Invalid adminOrg format: " + this.adminOrg);
		}
	}
}