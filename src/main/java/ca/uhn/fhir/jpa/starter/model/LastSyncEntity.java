package ca.uhn.fhir.jpa.starter.model;

import java.sql.Timestamp;

import javax.persistence.*;

@Entity
@Table(name = "last_sync_status")
public class LastSyncEntity {
	@Id
	@GeneratedValue
	private long id;

	@Column(name = "org_id", nullable = false)
	private String orgId;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "envs", nullable = false)
	private String env;

	@Column(name = "start_date_time", nullable = false)
	private Timestamp startDateTime;

	@Column(name = "end_date_time", nullable = true)
	private Timestamp endDateTime;

	public LastSyncEntity() {}

	public LastSyncEntity(String orgId, String status, String env, Timestamp  startDateTime, Timestamp endDateTime) {
		this.orgId = orgId;
		this.status = status;
		this.env = env;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}


	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public Timestamp getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Timestamp startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Timestamp getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(Timestamp endDateTime) {
		this.endDateTime = endDateTime;
	}
}
