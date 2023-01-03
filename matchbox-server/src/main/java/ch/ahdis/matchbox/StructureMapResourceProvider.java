package ch.ahdis.matchbox;

import org.hl7.fhir.r4.model.StructureMap;

public class StructureMapResourceProvider extends ConformanceResourceProvider<StructureMap> {

	public StructureMapResourceProvider() {
		super("StructureMap");
	}
	
	@Override
	public Class<StructureMap> getResourceType() {
		return StructureMap.class;
	}

}
