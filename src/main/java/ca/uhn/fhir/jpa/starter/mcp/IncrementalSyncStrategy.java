package ca.uhn.fhir.jpa.starter.mcp;

import java.util.Collections;
import java.util.Map;

public interface IncrementalSyncStrategy {
    Map<String, Object> enrichConfigForSync(Map<String, Object> baseConfig);
    Map<String, Object> extractNextState(Object parsedResponse);
}

