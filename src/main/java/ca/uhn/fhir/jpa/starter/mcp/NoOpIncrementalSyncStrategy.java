package ca.uhn.fhir.jpa.starter.mcp;

import java.util.Collections;
import java.util.Map;

public class NoOpIncrementalSyncStrategy implements IncrementalSyncStrategy {
	@Override
	public Map<String, Object> enrichConfigForSync(Map<String, Object> baseConfig) {
		return baseConfig;
	}

	@Override
	public Map<String, Object> extractNextState(Object parsedResponse) {
		return Collections.emptyMap();
	}
}
