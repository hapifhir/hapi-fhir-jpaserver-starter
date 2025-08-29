package ca.uhn.fhir.rest.server;

import io.modelcontextprotocol.server.McpServerFeatures;

import java.util.List;

public interface McpBridge {
	List<McpServerFeatures.SyncToolSpecification> generateTools();
}
