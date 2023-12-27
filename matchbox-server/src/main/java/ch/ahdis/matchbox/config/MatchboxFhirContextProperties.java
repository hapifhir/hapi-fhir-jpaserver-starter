package ch.ahdis.matchbox.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * The properties in 'matchbox.fhir.context'.
 *
 * @author Quentin Ligier
 **/
@Component
@ConfigurationProperties(prefix = "matchbox.fhir.context")
public class MatchboxFhirContextProperties {

	private @Nullable Map<String, List<String>> suppressWarnInfo;

	public @Nullable Map<String, List<String>> getSuppressWarnInfo() {
		return this.suppressWarnInfo;
	}

	public void setSuppressWarnInfo(final @Nullable Map<String, List<String>> suppressWarnInfo) {
		this.suppressWarnInfo = suppressWarnInfo;
	}
}
