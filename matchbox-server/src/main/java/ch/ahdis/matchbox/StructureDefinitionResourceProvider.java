package ch.ahdis.matchbox;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class StructureDefinitionResourceProvider extends ConformanceResourceProvider<StructureDefinition> {

	public StructureDefinitionResourceProvider() {
		super("StructureDefinition");
	}
	
	@Override
	public Class<StructureDefinition> getResourceType() {
		return StructureDefinition.class;
	}

}
