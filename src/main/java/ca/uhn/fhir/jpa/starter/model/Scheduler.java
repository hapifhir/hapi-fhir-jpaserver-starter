package ca.uhn.fhir.jpa.starter.model;
import javax.persistence.Column;

import com.iprd.report.DataResult;

import javax.persistence.*;

@Entity
@Table(name = "patient_details")



public class Scheduler {

	@Id
	@GeneratedValue
	private long id;

	@Column(name = "uuid", nullable = false)
	private String uuid;

	@Column(name = "status", nullable = true)
	private String status;
	
	@Lob
	@Column(name = "data", nullable = true)

	 private java.sql.Clob data;

	public Scheduler() {}
	public Scheduler(String uuid, String status, java.sql.Clob  data) {
		this.uuid = uuid;
		this.status = status;
		this.data = data;
	}
	public Long getId() {
		return id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public java.sql.Clob getData() {
		return data;
	}

	public void setData(java.sql.Clob  data) {
		this.data = data;
	}


}
