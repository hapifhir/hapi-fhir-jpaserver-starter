package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "email_schedule")
public class EmailScheduleEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "recipient_email", length = 255, nullable = false, unique = true)
	private String recipientEmail;

	@Column(name = "schedule_type", length = 50, nullable = false)
	private String scheduleType;

	@Column(name = "email_subject", length = 255, nullable = false)
	private String emailSubject;

	@Column(name = "org_id", length = 50, nullable = false)
	private String orgId;

	@Column(name = "admin_org", length = 50, nullable = false)
	private String adminOrg;

	@Column(name = "created_at", nullable = false)
	private Timestamp createdAt;

	@Column(name = "updated_at", nullable = false)
	private Timestamp updatedAt;

	public EmailScheduleEntity() {
		this.createdAt = new Timestamp(System.currentTimeMillis());
		this.updatedAt = new Timestamp(System.currentTimeMillis());
	}

	public EmailScheduleEntity(String recipientEmail, String scheduleType, String emailSubject, String orgId, String adminOrg) {
		this.recipientEmail = recipientEmail;
		this.scheduleType = scheduleType;
		this.emailSubject = emailSubject;
		this.orgId = orgId;
		this.adminOrg = adminOrg;
		this.createdAt = new Timestamp(System.currentTimeMillis());
		this.updatedAt = new Timestamp(System.currentTimeMillis());
	}

	public EmailScheduleEntity(String recipientEmail, String scheduleType, String emailSubject, String orgId,
										String adminOrg, Timestamp createdAt, Timestamp updatedAt) {
		this.recipientEmail = recipientEmail;
		this.scheduleType = scheduleType;
		this.emailSubject = emailSubject;
		this.orgId = orgId;
		this.adminOrg = adminOrg;
		this.createdAt = createdAt != null ? createdAt : new Timestamp(System.currentTimeMillis());
		this.updatedAt = updatedAt != null ? updatedAt : new Timestamp(System.currentTimeMillis());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getAdminOrg() {
		return adminOrg;
	}

	public void setAdminOrg(String adminOrg) {
		this.adminOrg = adminOrg;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}
}