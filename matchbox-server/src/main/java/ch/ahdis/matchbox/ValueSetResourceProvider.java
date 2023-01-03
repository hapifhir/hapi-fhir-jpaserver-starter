package ch.ahdis.matchbox;

import org.hl7.fhir.r4.model.ValueSet;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class ValueSetResourceProvider extends ConformanceResourceProvider<ValueSet> {

	public ValueSetResourceProvider() {
		super("ValueSet");
	}
	
	@Override
	public Class<ValueSet> getResourceType() {
		return ValueSet.class;
	}

}
