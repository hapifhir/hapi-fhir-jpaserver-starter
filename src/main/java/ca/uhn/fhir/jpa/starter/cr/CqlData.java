package ca.uhn.fhir.jpa.starter.cr;

import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hapi.fhir.cr.cql.data")
public class CqlData {
	private RetrieveSettings.SEARCH_FILTER_MODE searchParameterMode = RetrieveSettings.SEARCH_FILTER_MODE.AUTO;
	private RetrieveSettings.PROFILE_MODE profileMode = RetrieveSettings.PROFILE_MODE.OFF;
	private RetrieveSettings.TERMINOLOGY_FILTER_MODE terminologyParameterMode =
			RetrieveSettings.TERMINOLOGY_FILTER_MODE.AUTO;

	public RetrieveSettings.SEARCH_FILTER_MODE getSearchParameterMode() {
		return searchParameterMode;
	}

	public void setSearchParameterMode(RetrieveSettings.SEARCH_FILTER_MODE searchParameterMode) {
		this.searchParameterMode = searchParameterMode;
	}

	public RetrieveSettings.PROFILE_MODE getProfileMode() {
		return profileMode;
	}

	public void setProfileMode(RetrieveSettings.PROFILE_MODE profileMode) {
		this.profileMode = profileMode;
	}

	public RetrieveSettings.TERMINOLOGY_FILTER_MODE getTerminologyParameterMode() {
		return terminologyParameterMode;
	}

	public void setTerminologyParameterMode(RetrieveSettings.TERMINOLOGY_FILTER_MODE terminologyParameterMode) {
		this.terminologyParameterMode = terminologyParameterMode;
	}

	public RetrieveSettings getRetrieveSettings() {
		var retrieveSettings = new RetrieveSettings();
		retrieveSettings.setSearchParameterMode(searchParameterMode);
		retrieveSettings.setProfileMode(profileMode);
		retrieveSettings.setTerminologyParameterMode(terminologyParameterMode);
		return retrieveSettings;
	}
}
