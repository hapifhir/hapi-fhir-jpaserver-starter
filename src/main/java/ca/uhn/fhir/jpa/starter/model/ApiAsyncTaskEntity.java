package ca.uhn.fhir.jpa.starter.model;
import javax.persistence.Column;

import javax.persistence.*;


@Entity
@Table(name = "async_task_status")
public class ApiAsyncTaskEntity {

	public enum Status {
		PROCESSING,
		COMPLETED
	}

	@Id
	@GeneratedValue
	private long id;

	@Column(name = "uuid", nullable = false)
	private String uuid;

	@Column(name = "status", nullable = true)
	private String status;
	
	@Lob
	@Column(name = "summaryResult", nullable = true)
	 private java.sql.Clob summaryResult;

	@Lob
	@Column(name = "dailyResult", nullable = true)
	private java.sql.Clob dailyResult;

	public ApiAsyncTaskEntity() {}
	public ApiAsyncTaskEntity(String uuid, String status, java.sql.Clob  summaryResult,java.sql.Clob dailyResult) {
		this.uuid = uuid;
		this.status = status;
		this.summaryResult = summaryResult;
		this.dailyResult = dailyResult;
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

	public java.sql.Clob getSummaryResult() {
		return summaryResult;
	}

	public void setSummaryResult(java.sql.Clob  summaryResult) {
		this.summaryResult = summaryResult;
	}

	public java.sql.Clob getDailyResult() {
		return dailyResult;
	}

	public void setDailyResult(java.sql.Clob  dailyResult) {
		this.dailyResult = dailyResult;
	}
}