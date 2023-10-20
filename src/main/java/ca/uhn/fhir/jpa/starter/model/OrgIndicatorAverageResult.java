package ca.uhn.fhir.jpa.starter.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
public class OrgIndicatorAverageResult {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String orgId;
	private String indicator;
	private Double averageValue;

	public OrgIndicatorAverageResult() {
		// Default constructor
	}

	public OrgIndicatorAverageResult(String orgId, String indicator, Double averageValue) {
		this.orgId = orgId;
		this.indicator = indicator;
		this.averageValue = averageValue;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public Double getAverageValue() {
		return averageValue;
	}

	public void setAverageValue(Double averageValue) {
		this.averageValue = averageValue;
	}

}
