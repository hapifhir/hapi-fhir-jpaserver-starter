package ca.uhn.fhir.jpa.starter.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Component
public class CallToolResultFactory {

	public static McpSchema.CallToolResult success(
			String resourceType, Interaction interaction, String response, int status) {
		Map<String, Object> payload = Map.of(
				"resourceType", resourceType,
				"interaction", interaction,
				"response", response,
				"status", status);

		String jacksonData = JsonMapper.shared().writeValueAsString(payload);

		return McpSchema.CallToolResult.builder()
				.addContent(new McpSchema.TextContent(jacksonData))
				.build();
	}

	public static McpSchema.CallToolResult failure(String message) {
		return McpSchema.CallToolResult.builder()
				.isError(true)
				.addTextContent(message)
				.build();
	}
}
