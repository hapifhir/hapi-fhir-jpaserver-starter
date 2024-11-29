package ch.ahdis.matchbox.packages;

import ca.uhn.fhir.jpa.packages.PackageInstallOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.utils.ToolingExtensions;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * The provider of the $install-npm-package operation.
 *
 * @author Quentin Ligier
 **/
@Component
public class InstallNpmPackageProvider {

	private final MatchboxPackageInstallerImpl packageInstallerSvc;

	public InstallNpmPackageProvider(final MatchboxPackageInstallerImpl packageInstallerSvc) {
		this.packageInstallerSvc = packageInstallerSvc;
	}

	@Operation(name = "$install-npm-package", manualRequest = true, returnParameters = {
		@OperationParam(name = "return", typeName = "OperationOutcome", min = 1, max = 1)})
	public IBaseOperationOutcome installNpmPackage(final HttpServletRequest theRequest) {
		if (theRequest.getParameter("name") == null) {
			return this.getOoForError("The 'name' parameter must be provided");
		}
		if (theRequest.getParameter("version") == null) {
			return this.getOoForError("The 'version' parameter must be provided");
		}
		final byte[] npmPackage;
		try {
			npmPackage = theRequest.getInputStream().readAllBytes();
		} catch (final IOException e) {
			return this.getOoForError("Failed to read the package content: " + e.getMessage());
		}

		final var packageSpec = new PackageInstallationSpec()
			.setName(theRequest.getParameter("name"))
			.setVersion(theRequest.getParameter("version"))
			.setPackageUrl("data:application/gzip;base64," + Base64.getEncoder().encodeToString(npmPackage));
		final PackageInstallOutcomeJson installOutcome = this.packageInstallerSvc.install(packageSpec);

		final var oo = new OperationOutcome();
		for (final String message : installOutcome.getMessage()) {
			final var issue = oo.addIssue();
			issue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
			issue.setCode(OperationOutcome.IssueType.INFORMATIONAL);
			issue.setDiagnostics(message);
			issue.addExtension().setUrl(ToolingExtensions.EXT_ISSUE_SOURCE).setValue(new StringType("MatchboxPackageInstallerImpl"));
		}
		final var issue = oo.addIssue();
		issue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
		issue.setCode(OperationOutcome.IssueType.INFORMATIONAL);
		issue.setDiagnostics("Resources that were installed:\n" + installOutcome.getResourcesInstalled()
			.entrySet()
			.stream()
			.map(entry -> "- " + entry.getKey() + " (" + entry.getValue() + ")")
			.collect(Collectors.joining("\n"))
		);
		issue.addExtension().setUrl(ToolingExtensions.EXT_ISSUE_SOURCE).setValue(new StringType("MatchboxPackageInstallerImpl"));

		return oo;
	}

	private OperationOutcome getOoForError(final @NonNull String message) {
		final var oo = new OperationOutcome();
		final var issue = oo.addIssue();
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setCode(OperationOutcome.IssueType.EXCEPTION);
		issue.setDiagnostics(message);
		issue.addExtension().setUrl(ToolingExtensions.EXT_ISSUE_SOURCE).setValue(new StringType("InstallNpmPackageProvider"));
		return oo;
	}
}
