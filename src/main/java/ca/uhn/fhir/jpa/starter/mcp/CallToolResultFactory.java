package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CallToolResultFactory {

	@Autowired
	private FhirContext fhirContext;

	public McpSchema.CallToolResult success(String resourceType, Interaction interaction, Object response, int status) {
		Map<String, Object> payload = Map.of(
				"resourceType", resourceType,
				"interaction", interaction,
				"response", fhirContext.newJsonParser().encodeResourceToString((IBaseResource) response),
				"status", status);

		ObjectMapper objectMapper = new ObjectMapper();
		String jacksonData;
		try {
			jacksonData = objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return McpSchema.CallToolResult.builder()
				.addContent(new McpSchema.TextContent(jacksonData))
				.build();
	}

	public McpSchema.CallToolResult failure(String message) {
		return McpSchema.CallToolResult.builder()
				.isError(true)
				.addTextContent(message)
				.build();
	}
}
