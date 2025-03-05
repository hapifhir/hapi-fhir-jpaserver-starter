package ca.uhn.fhir.jpa.starter.ig;

import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import org.hl7.fhir.utilities.npm.NpmPackage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface IImplementationGuideOperationProvider {
	static PackageInstallationSpec toPackageInstallationSpec(byte[] npmPackageAsByteArray) throws IOException {
		NpmPackage npmPackage = NpmPackage.fromPackage(new ByteArrayInputStream(npmPackageAsByteArray));
		return new PackageInstallationSpec().setName(npmPackage.name()).setPackageContents(npmPackageAsByteArray).setVersion(npmPackage.version()).setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL).setFetchDependencies(false);
	}

	// The following declaration is the one that counts but cannot be used across different versions as stating Base64BinaryType would bind to a separate version
	// Parameters install(@OperationParam(name = "npmContent",min = 1, max = 1) Base64BinaryType implementationGuide);
	// Parameters uninstall(@OperationParam(name = "name", min = 1, max = 1) String name, @OperationParam(name = "version", min = 1, max = 1) String version) ;
}
