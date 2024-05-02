package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.Entity;
import javax.persistence.UniqueConstraint;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import java.sql.Timestamp;

@Entity
@Table(name = "sms_info", uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id","resource_id", "resource_type"}))
public class SMSInfo {

	@Id
	@GeneratedValue
	private long id;
	@Column(name = "status", nullable = false)
	private String status;
	@Column(name = "created_at", nullable = false)
	private Timestamp createdAt;
	@Column(name = "sent_at", nullable = true)
	private Timestamp sentAt;
	@Column(name = "resource_id", nullable = false)
	private String resourceId;
	@Column(name = "patient_id", nullable = false)
	private String patientId;
	@Column(name = "organization_id", nullable = true)
	private String organizationId;
	@Column(name = "encounter_id", nullable = true)
	private String encounterId;
	@Column(name = "service_type", nullable = false)
	private String serviceType;
	@Column(name = "patient_card_number", nullable = true)
	private String patientCardNumber;
	@Column(name = "resource_type", nullable = false)
	private String resourceType;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getSentAt() {
		return sentAt;
	}

	public void setSentAt(Timestamp sentAt) {
		this.sentAt = sentAt;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public String getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(String encounterId) {
		this.encounterId = encounterId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getPatientCardNumber() {
		return patientCardNumber;
	}

	public void setPatientCardNumber(String patientCardNumber) {
		this.patientCardNumber = patientCardNumber;
	}

	public SMSInfo(String status, Timestamp createdAt, String patientId, String organizationId, String encounterId, String serviceType, String resourceId, String patientCardNumber, String resourceType) {
		this.status = status;
		this.createdAt = createdAt;
		this.patientId = patientId;
		this.organizationId = organizationId;
		this.encounterId = encounterId;
		this.serviceType = serviceType;
		this.resourceId = resourceId;
		this.patientCardNumber = patientCardNumber;
		this.resourceType = resourceType;
	}

	public SMSInfo(){}
}
