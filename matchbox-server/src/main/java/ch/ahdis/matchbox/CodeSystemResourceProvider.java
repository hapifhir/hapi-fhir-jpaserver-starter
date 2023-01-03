package ch.ahdis.matchbox;

import org.hl7.fhir.r4.model.CodeSystem;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class CodeSystemResourceProvider extends ConformanceResourceProvider<CodeSystem> {

	public CodeSystemResourceProvider() {
		super("CodeSystem");
	}
	
	@Override
	public Class<CodeSystem> getResourceType() {
		return CodeSystem.class;
	}

}
