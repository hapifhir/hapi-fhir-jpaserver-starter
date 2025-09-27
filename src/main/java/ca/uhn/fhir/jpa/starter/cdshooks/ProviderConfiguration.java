package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.jpa.starter.cr.CqlRuntimeProperties;

public class ProviderConfiguration {
	private final String clientIdHeaderName;
	private final boolean cqlLoggingEnabled;

	public ProviderConfiguration(boolean cqlLoggingEnabled, String clientIdHeaderName) {
		this.cqlLoggingEnabled = cqlLoggingEnabled;
		this.clientIdHeaderName = clientIdHeaderName;
	}

	public ProviderConfiguration(CdsHooksProperties cdsProperties, CqlRuntimeProperties cqlRuntimeProperties) {
		this(cqlRuntimeProperties.isDebugLoggingEnabled(), cdsProperties.getClientIdHeaderName());
	}

	public String getClientIdHeaderName() {
		return this.clientIdHeaderName;
	}

	public boolean getCqlLoggingEnabled() {
		return this.cqlLoggingEnabled;
	}
}
