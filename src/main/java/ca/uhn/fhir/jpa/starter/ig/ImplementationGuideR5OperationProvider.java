package ca.uhn.fhir.jpa.starter.ig;

import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.starter.annotations.OnR5Condition;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.hl7.fhir.r5.model.Base64BinaryType;
import org.hl7.fhir.r5.model.Parameters;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Conditional({OnR5Condition.class, IgConfigCondition.class})
@Service
public class ImplementationGuideR5OperationProvider {

	IPackageInstallerSvc packageInstallerSvc;

	public ImplementationGuideR5OperationProvider(IPackageInstallerSvc packageInstallerSvc) {
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
}
