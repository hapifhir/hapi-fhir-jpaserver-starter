package ca.uhn.fhir.jpa.starter.ig;

import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class ExtendedPackageInstallationSpec extends PackageInstallationSpec {

	public List<String> getAdditionalResourceFolders() {
		return additionalResourceFolders;
	}

	public void setAdditionalResourceFolders(List<String> additionalResourceFolders) {
		this.additionalResourceFolders = additionalResourceFolders;
	}

	@Schema(
		description =
			"If resources are being installed individually, this is list provides the resource types to install. By default, all conformance resources will be installed.")
	@JsonProperty("additionalResourceFolders")
	private List<String> additionalResourceFolders;
}
