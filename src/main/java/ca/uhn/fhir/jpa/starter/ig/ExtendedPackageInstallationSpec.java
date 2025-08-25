package ca.uhn.fhir.jpa.starter.ig;

import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

public class ExtendedPackageInstallationSpec extends PackageInstallationSpec {

	public Set<String> getAdditionalResourceFolders() {
		return additionalResourceFolders;
	}

	public void setAdditionalResourceFolders(Set<String> additionalResourceFolders) {
		this.additionalResourceFolders = additionalResourceFolders;
	}

	@Schema(
			description =
					"Specifies folder names containing additional resources to load. These folders will be scanned for resources to include during installation.")
	@JsonProperty("additionalResourceFolders")
	private Set<String> additionalResourceFolders;
}
