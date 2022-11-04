package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.sql.Timestamp;


@Entity
@Table(name = "com_generator")
public class ComGenerator {
	
	public enum MessageStatus {
		PENDING,
		SENT,
		FAILED,
	}

	@Id
	@GeneratedValue
	private long id;
	
	@Column(name = "resource_type", nullable = false)
	private String resourceType;
	
	@Column(name = "resource_id", nullable = false)
	private String resourceId;
	
	@Column(name = "scheduled_date", nullable = false)
	private Date scheduledDate;
	
	@Column(name = "communication_status", nullable = true)
	private String communicationStatus;
	
	@Column(name = "patient_id", nullable = false)
	private String patientId;
	
	@Column(name = "next_visit_date", nullable = true)
	private Timestamp nextVisitDate;
	
	@Column(name = "created_at")
	@CreationTimestamp
	private Timestamp createdAt;
	
	@Column(name = "updated_at")
	@UpdateTimestamp
	private Timestamp updatedAt;


	public ComGenerator(String resourceType, String resourceId, Date scheduledDate, String communicationStatus, String patientId, Timestamp nextVisitDate) {
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		this.scheduledDate = scheduledDate;
		this.communicationStatus = communicationStatus;
		this.patientId = patientId;
		this.nextVisitDate = nextVisitDate;
	}
	
	// Default constructor required for entity.
	public ComGenerator(){}

	public Long getId() {
		return id;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}
	
	public String getCommunicationStatus() {
		return communicationStatus;
	}

	public void setCommunicationStatus(String communicationStatus) {
		this.communicationStatus = communicationStatus;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public Timestamp getNextVisitDate() {
		return nextVisitDate;
	}

	public void setNextVisitDate(Timestamp nextVisitDate) {
		this.nextVisitDate = nextVisitDate;
	}
	
	public Timestamp getCreatedAt() {
		return createdAt;
	}
	
	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	@Override
	public String toString() {
		return "ComGenerator[id=" + id + ",resourceType=" + resourceType + ",resourceId=" + resourceId + ",scheduledDate=" + scheduledDate + ",communicationStatus=" + communicationStatus + ",patientId=" + patientId + ",nextVisitDate=" + nextVisitDate + "]";
	}
}
