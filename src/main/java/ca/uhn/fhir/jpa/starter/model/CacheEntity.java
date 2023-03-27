package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import java.sql.Date;
import java.util.UUID;

@Entity
@Table(name = "cache", indexes = {
	    @Index(columnList = "indicator,date,org_id", name = "indicator_date_org") })
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CacheEntity {

	@Id
	@Column(name = "id",  columnDefinition = "VARCHAR(36)")
	private String id;

	@Column(name = "org_id", nullable = false)
	private String orgId;

	
	@Column(name = "indicator", nullable = false)
	private String indicator;

	@Column(name = "date", nullable = false)
	private Date date;

	@Column(name = "value", nullable = false)
	private Double value;
	

	@Column(name = "lastUpdated", nullable = true)
	private Date lastUpdated;
	

	// Default constructor required for entity.
	public CacheEntity() {	}

	public CacheEntity(String orgId, String indicator, Date date, Double value,Date lastUpdated) {
		this.id = UUID.randomUUID().toString();
		this.orgId = orgId;
		this.indicator = indicator;
		this.date = date;
		this.value = value;
		this.lastUpdated = lastUpdated;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
