package ca.uhn.fhir.jpa.starter.cr;

import org.opencds.cqf.fhir.utility.client.TerminologyServerClientSettings;

public class CrProperties {
	private Boolean enabled;

	private CareGapsProperties careGaps = new CareGapsProperties();
	private CqlProperties cql = new CqlProperties();

	private TerminologyServerClientSettings terminologyServerClientSettings = new TerminologyServerClientSettings();

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

	public TerminologyServerClientSettings getTerminologyServerClientSettings() {
		return terminologyServerClientSettings;
	}

	public void setTerminologyServerClientSettings(TerminologyServerClientSettings terminologyServerClientSettings) {
		this.terminologyServerClientSettings = terminologyServerClientSettings;
	}
}
