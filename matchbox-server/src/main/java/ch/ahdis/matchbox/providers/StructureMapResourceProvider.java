package ch.ahdis.matchbox.providers;

import org.hl7.fhir.r4.model.StructureMap;

public class StructureMapResourceProvider extends ConformancePackageResourceProvider<StructureMap, org.hl7.fhir.r4b.model.StructureMap, org.hl7.fhir.r5.model.StructureMap> {

	public StructureMapResourceProvider() {
		super(StructureMap.class, org.hl7.fhir.r4b.model.StructureMap.class, org.hl7.fhir.r5.model.StructureMap.class);
	}

}
