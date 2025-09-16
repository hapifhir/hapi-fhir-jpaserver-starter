package ca.uhn.fhir.jpa.starter.cr;

import org.opencds.cqf.fhir.cql.engine.terminology.TerminologySettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "hapi.fhir.cr.cql.terminology")
@Configuration
public class CqlTerminologyProperties {

	private TerminologySettings.VALUESET_EXPANSION_MODE valuesetExpansionMode =
			TerminologySettings.VALUESET_EXPANSION_MODE.AUTO;
	private TerminologySettings.VALUESET_MEMBERSHIP_MODE valuesetMembershipMode =
			TerminologySettings.VALUESET_MEMBERSHIP_MODE.AUTO;
	private TerminologySettings.CODE_LOOKUP_MODE codeLookupMode = TerminologySettings.CODE_LOOKUP_MODE.AUTO;
	private TerminologySettings.VALUESET_PRE_EXPANSION_MODE valueSetPreExpansionMode =
			TerminologySettings.VALUESET_PRE_EXPANSION_MODE.USE_IF_PRESENT;

	public void setValuesetExpansionMode(TerminologySettings.VALUESET_EXPANSION_MODE valuesetExpansionMode) {
		this.valuesetExpansionMode = valuesetExpansionMode;
	}

	public void setValuesetMembershipMode(TerminologySettings.VALUESET_MEMBERSHIP_MODE valuesetMembershipMode) {
		this.valuesetMembershipMode = valuesetMembershipMode;
	}

	public void setCodeLookupMode(TerminologySettings.CODE_LOOKUP_MODE codeLookupMode) {
		this.codeLookupMode = codeLookupMode;
	}

	public void setValueSetPreExpansionMode(TerminologySettings.VALUESET_PRE_EXPANSION_MODE valueSetPreExpansionMode) {
		this.valueSetPreExpansionMode = valueSetPreExpansionMode;
	}

	public TerminologySettings getTerminologySettings() {
		TerminologySettings settings = new TerminologySettings();
		settings.setValuesetExpansionMode(valuesetExpansionMode);
		settings.setValuesetMembershipMode(valuesetMembershipMode);
		settings.setCodeLookupMode(codeLookupMode);
		settings.setValuesetPreExpansionMode(valueSetPreExpansionMode);
		return settings;
	}
}
