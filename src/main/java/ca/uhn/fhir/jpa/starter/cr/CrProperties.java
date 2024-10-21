package ca.uhn.fhir.jpa.starter.cr;

public class CrProperties {
	private Boolean enabled;

	private CareGapsProperties careGaps = new CareGapsProperties();
	private CqlProperties cql = new CqlProperties();

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public CareGapsProperties getCareGaps() {
		return careGaps;
	}

	public void setCareGaps(CareGapsProperties careGaps) {
		this.careGaps = careGaps;
	}

	public CqlProperties getCql() {
		return cql;
	}

	public void setCql(CqlProperties cql) {
		this.cql = cql;
	}
}
