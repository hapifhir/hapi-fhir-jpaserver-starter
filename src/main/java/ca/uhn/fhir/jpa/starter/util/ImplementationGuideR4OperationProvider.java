package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class ImplementationGuideR4OperationProvider {

	IPackageInstallerSvc packageInstallerSvc;

	public ImplementationGuideR4OperationProvider(IPackageInstallerSvc packageInstallerSvc) {
		this.packageInstallerSvc = packageInstallerSvc;
	}

	@Operation(name = "$install")
	public Parameters install(@OperationParam(name = "implementationGuide", max = 1) Base64BinaryType implementationGuide) {
		try {

			packageInstallerSvc.install(toPackageInstallationSpec(implementationGuide.getValue()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new Parameters();
	}

	public static PackageInstallationSpec toPackageInstallationSpec(byte[] npmPackageAsByteArray) throws IOException {
		NpmPackage npmPackage = NpmPackage.fromPackage(new ByteArrayInputStream(npmPackageAsByteArray));
		return new PackageInstallationSpec().setName(npmPackage.name()).setPackageContents(npmPackageAsByteArray).setVersion(npmPackage.version()).setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL).setFetchDependencies(false);
	}

}
