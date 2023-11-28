package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.jpa.starter.cr.CrProperties;

public class ProviderConfiguration {

	public static final ProviderConfiguration DEFAULT_PROVIDER_CONFIGURATION =
			new ProviderConfiguration(false, "client_id");

	private final String clientIdHeaderName;
	private final boolean cqlLoggingEnabled;

	public ProviderConfiguration(boolean cqlLoggingEnabled, String clientIdHeaderName) {
		this.cqlLoggingEnabled = cqlLoggingEnabled;
		this.clientIdHeaderName = clientIdHeaderName;
	}

	public ProviderConfiguration(CdsHooksProperties cdsProperties, CrProperties crProperties) {
		this.clientIdHeaderName = cdsProperties.getClientIdHeaderName();
		this.cqlLoggingEnabled = crProperties.isCqlRuntimeDebugLoggingEnabled();
	}

	public String getClientIdHeaderName() {
		return this.clientIdHeaderName;
	}

	public boolean getCqlLoggingEnabled() {
		return this.cqlLoggingEnabled;
	}
}
