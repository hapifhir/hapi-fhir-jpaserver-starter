package ch.ahdis.matchbox;

import org.hl7.fhir.r4.model.CodeSystem;

public class CodeSystemResourceProvider extends ConformancePackageResourceProvider<CodeSystem, org.hl7.fhir.r4b.model.CodeSystem, org.hl7.fhir.r5.model.CodeSystem> {

	public CodeSystemResourceProvider() {
		super(CodeSystem.class, org.hl7.fhir.r4b.model.CodeSystem.class, org.hl7.fhir.r5.model.CodeSystem.class);
	}

}
