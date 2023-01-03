package ch.ahdis.matchbox;

import org.hl7.fhir.r4.model.ConceptMap;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class ConceptMapResourceProvider extends ConformanceResourceProvider<ConceptMap> {

	public ConceptMapResourceProvider() {
		super("ConceptMap");
	}
	
	@Override
	public Class<ConceptMap> getResourceType() {
		return ConceptMap.class;
	}

}
