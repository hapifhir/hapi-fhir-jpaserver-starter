package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.*;

@Entity
@Table(name = "patientIdentifierEntity", indexes = {@Index(columnList = "patientId,patientIdentifier,status", name = "patient_id_identifier")}, uniqueConstraints = @UniqueConstraint(columnNames = {"patientId", "patientIdentifier", "identifierType"}))
public class PatientIdentifierEntity {

	public enum PatientIdentifierType {
		OCL_ID,
		PATIENT_CARD_NUM,
		PHONE_NUM,
	}

	@Id
	@GeneratedValue
	private long id;

	@Column(name = "patientId", nullable = false)
	private String patientId;

	@Column(name = "patientIdentifier", nullable = true)
	private String patientIdentifier;

	@Column(name = "identifierType", nullable = false)
	private String identifierType;

	@Column(name = "oclVersionId", nullable = true)
	private String oclVersionId;

	@Column(name = "oclGuid", nullable = true)
	private String oclGuid;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "createdTime", nullable = false)
	private long cratedTime;

	@Column(name = "updatedTime", nullable = false)
	private long updatedTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getPatientIdentifier() {
		return patientIdentifier;
	}

	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}

	public String getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOclVersionId() {
		return oclVersionId;
	}

	public void setCclVersionId(String oclVersionId) {
		this.oclVersionId = oclVersionId;
	}

	public String getOclGuid() {
		return oclGuid;
	}

	public void setOclGuid(String oclGuid) {
		this.status = oclGuid;
	}

	public long getCratedTime() {
		return cratedTime;
	}

	public void setCratedTime(long cratedTime) {
		this.cratedTime = cratedTime;
	}

	public long getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(long updatedTime) {
		this.updatedTime = updatedTime;
	}

	public PatientIdentifierEntity() {
	}

	public PatientIdentifierEntity(String patientId, String patientIdentifier, String identifierType, String oclVersionId, String oclGuid, String status, long createdAt, long updatedAt) {
		this.patientId = patientId;
		this.patientIdentifier = patientIdentifier;
		this.identifierType = identifierType;
		this.oclVersionId = oclVersionId;
		this.oclGuid = oclGuid;
		this.status = status;
		this.cratedTime = createdAt;
		this.updatedTime = updatedAt;
	}

	@Override
	public String toString() {
		return "PatientInfoResourceEntity[id=" + id + ",patientId=" + patientId + ",patientIdentifier=" + patientIdentifier + ",identifierType=" + identifierType + ",oclVersionId=" + oclVersionId + ",oclGuid=" + oclGuid + ",status=" + status + ",createdAt=" + cratedTime + ",updatedAt=" + updatedTime + "]";
	}
}
