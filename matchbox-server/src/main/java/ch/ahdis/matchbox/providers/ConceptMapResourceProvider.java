package ch.ahdis.matchbox.providers;

import org.hl7.fhir.r4.model.ConceptMap;


public class ConceptMapResourceProvider extends ConformancePackageResourceProvider<ConceptMap, org.hl7.fhir.r4b.model.ConceptMap, org.hl7.fhir.r5.model.ConceptMap> {

	public ConceptMapResourceProvider() {
		super(ConceptMap.class, org.hl7.fhir.r4b.model.ConceptMap.class, org.hl7.fhir.r5.model.ConceptMap.class);
	}

}
