package ch.ahdis.matchbox.gazelle;

import ch.ahdis.matchbox.validation.gazelle.models.validation.Metadata;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationReport;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public abstract class AbstractGazelleTest {

	public static int countValidationFailures(final ValidationReport report) {
		return report.getCounters().getNumberOfFailedWithErrors() + report.getCounters().getNumberOfUnexpectedErrors();
	}

	public static String getMetadata(final ValidationReport report, final String metadataName) {
		for (final Metadata metadata : report.getAdditionalMetadata()) {
			if (metadataName.equals(metadata.getName())) {
				return metadata.getValue();
			}
		}
		return null;
	}

	public static String getSessionId(final ValidationReport report) {
		return getMetadata(report, "sessionId");
	}

	public static String getIg(final ValidationReport report) {
		return getMetadata(report, "ig");
	}

	public static String getTxServer(final ValidationReport report) {
		return getMetadata(report, "txServer");
	}

	public static String getContent(final String resourceName) throws IOException {
		return FileUtils.readFileToString(new ClassPathResource(resourceName).getFile(), StandardCharsets.UTF_8);
	}
}
