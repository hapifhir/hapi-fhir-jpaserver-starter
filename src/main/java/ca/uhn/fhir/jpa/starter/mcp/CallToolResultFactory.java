package ca.uhn.fhir.jpa.starter.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CallToolResultFactory {

	/**
	 * @param location the Location header of the response, if any. When the response has no body, e.g. for a
	 *                 conditional create that matched an existing resource, it identifies the resource.
	 */
	public static McpSchema.CallToolResult success(
			String resourceType, Interaction interaction, String response, int status, String location) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("resourceType", resourceType);
		payload.put("interaction", interaction);
		payload.put("response", response);
		payload.put("status", status);
		if (location != null) {
			payload.put("location", location);
		}

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
