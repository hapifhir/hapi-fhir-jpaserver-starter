package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.hl7.fhir.r5.model.Base64BinaryType;
import org.hl7.fhir.r5.model.Parameters;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static ca.uhn.fhir.jpa.starter.util.ImplementationGuideR4OperationProvider.toPackageInstallationSpec;

@Service
public class ImplementationGuideR5OperationProvider {

	IPackageInstallerSvc packageInstallerSvc;

	public ImplementationGuideR5OperationProvider(IPackageInstallerSvc packageInstallerSvc) {
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


}
