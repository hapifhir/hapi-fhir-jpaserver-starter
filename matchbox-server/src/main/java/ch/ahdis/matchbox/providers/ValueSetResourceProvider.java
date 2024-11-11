package ch.ahdis.matchbox.providers;

import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetResourceProvider extends ConformancePackageResourceProvider<ValueSet, org.hl7.fhir.r4b.model.ValueSet, org.hl7.fhir.r5.model.ValueSet> {

	public ValueSetResourceProvider() {
		super(ValueSet.class, org.hl7.fhir.r4b.model.ValueSet.class, org.hl7.fhir.r5.model.ValueSet.class);
	}

}
