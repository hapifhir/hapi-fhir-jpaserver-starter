package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.jpa.starter.cr.CrProperties;

public class ProviderConfiguration {
	private final String clientIdHeaderName;
	private final boolean cqlLoggingEnabled;

	public ProviderConfiguration(boolean cqlLoggingEnabled, String clientIdHeaderName) {
		this.cqlLoggingEnabled = cqlLoggingEnabled;
		this.clientIdHeaderName = clientIdHeaderName;
	}

	public ProviderConfiguration(CdsHooksProperties cdsProperties, CrProperties crProperties) {
		this(crProperties.getCql().getRuntime().isDebugLoggingEnabled(), cdsProperties.getClientIdHeaderName());
	}

	public String getClientIdHeaderName() {
		return this.clientIdHeaderName;
	}

	public boolean getCqlLoggingEnabled() {
		return this.cqlLoggingEnabled;
	}
}
