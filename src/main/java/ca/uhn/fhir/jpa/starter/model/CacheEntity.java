package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import java.sql.Date;

@Entity
@Table(name = "cache")
public class CacheEntity {

	@Id
	@GeneratedValue
	private long id;

	@Column(name = "org_id", nullable = false)
	private String orgId;

	@Column(name = "indicator", nullable = false)
	private String indicator;

	@Column(name = "date", nullable = false)
	private Date date;

	@Column(name = "value", nullable = false)
	private Integer value;
	
	// Default constructor required for entity.
	public CacheEntity() {	}

	public CacheEntity(String orgId, String indicator, Date date, Integer value) {
		this.orgId = orgId;
		this.indicator = indicator;
		this.date = date;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}
}
