package ca.uhn.fhir.jpa.starter.ig;

import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Conditional({OnR4Condition.class})
@ConditionalOnProperty(name = "hapi.fhir.ig_runtime_upload_enabled", havingValue = "true")
@Service
public class ImplementationGuideR4OperationProvider implements IImplementationGuideOperationProvider {

	final IPackageInstallerSvc packageInstallerSvc;

	public ImplementationGuideR4OperationProvider(IPackageInstallerSvc packageInstallerSvc) {
		this.packageInstallerSvc = packageInstallerSvc;
	}

	@Operation(name = "$install", typeName = "ImplementationGuide")
	public Parameters install(
			@OperationParam(name = "npmContent", min = 1, max = 1) Base64BinaryType implementationGuide) {
		try {

			packageInstallerSvc.install(
					IImplementationGuideOperationProvider.toPackageInstallationSpec(implementationGuide.getValue()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new Parameters();
	}

	@Operation(name = "$uninstall", typeName = "ImplementationGuide")
	public Parameters uninstall(
			@OperationParam(name = "name", min = 1, max = 1) String name,
			@OperationParam(name = "version", min = 1, max = 1) String version) {

		packageInstallerSvc.uninstall(
				new PackageInstallationSpec().setName(name).setVersion(version));
		return new Parameters();
	}
}
