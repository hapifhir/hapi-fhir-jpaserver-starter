package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.*;

import com.iprd.fhir.utils.PatientIdentifierStatus;

@Entity
@Table(name = "PatientIdentifierEntity",indexes = {
	@Index(columnList = "patientId,patientIdentifier,status ", name = "patient_id_identifier") })
public class PatientIdentifierEntity {
	
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

	public String getPatientIdentifierExtraInfo() {
		return patientIdentifierExtraInfo;
	}

	public void setPatientIdentifierExtraInfo(String patientIdentifierExtraInfo) {
		this.patientIdentifierExtraInfo = patientIdentifierExtraInfo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	@Id
	@GeneratedValue
	private long id;
	
	@Column(name = "patientId", nullable = false)
	private String patientId;

	@Column(name = "patientIdentifier", nullable = true)
	private String patientIdentifier;

	@Column(name = "patientIdentifierExtraInfo", nullable = true)
	private String patientIdentifierExtraInfo;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "resourceType", nullable = false)
	private String resourceType;

	@Column(name = "lastModified", nullable = false)
	private String lastModified;

	public PatientIdentifierEntity(){}
	
	public PatientIdentifierEntity(String patientId, String patientIdentifier, String patientIdentifierExtraInfo, String status, String resourceType, String lastModified){
		this.patientId = patientId;
		this.patientIdentifier = patientIdentifier;
		this.patientIdentifierExtraInfo = patientIdentifierExtraInfo;
		this.status = status;
		this.resourceType = resourceType;
		this.lastModified = lastModified;
	}

	@Override
	public String toString() {
		return "PatientInfoResourceEntity[id=" + id + ",patientId=" + patientId + ",patientIdentifier=" + patientIdentifier + ",patientIdentifierExtraInfo=" + patientIdentifierExtraInfo + ",status=" + status + ",lastModified=" + lastModified + ",resourceType=" + resourceType + "]";
	}
}
