package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.mcp.CallToolResultFactory;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.RequestBuilder;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class MCPBridge {

	private final RestfulServer restfulServer;
	private final FhirContext fhirContext;
	private final ObjectMapper mapper = new ObjectMapper();

	public MCPBridge(RestfulServer restfulServer) {
		this.restfulServer = restfulServer;
		this.fhirContext = restfulServer.getFhirContext();
	}

	public List<McpServerFeatures.SyncToolSpecification> generateTools() {

		try {
			return List.of(
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.createFhirResource(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.CREATE)),
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.readFhirResource(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.READ)),
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.updateFhirResource(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.UPDATE)),
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.deleteFhirResource(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.DELETE)),
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.conditionalPatchFhirResource(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.PATCH)),
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.searchFhirResources(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.SEARCH)),
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.conditionalUpdateFhirResource(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.UPDATE)),
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.patchFhirResource(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.PATCH)));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private McpSchema.CallToolResult getToolResult(Map<String, Object> contextMap, Interaction interaction) {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new RequestBuilder(fhirContext, contextMap, interaction).buildRequest();

		try {
			restfulServer.handleRequest(interaction.asRequestType(), request, response);
			int status = response.getStatus();
			String body = response.getContentAsString();

			if (status >= 200 && status < 300) {
				if (body.isBlank()) {
					return McpSchema.CallToolResult.builder()
							.isError(true)
							.addTextContent("Empty successful response for " + interaction)
							.build();
				}
				IBaseResource parsed = fhirContext.newJsonParser().parseResource(body);

				return CallToolResultFactory.success(
						contextMap.get("resourceType").toString(), interaction, parsed, status);
			} else {
				return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
			}
		} catch (IOException e) {
			return CallToolResultFactory.failure("Dispatch error: " + e.getMessage());
		} catch (Exception e) {
			return McpSchema.CallToolResult.builder()
					.isError(true)
					.addTextContent("Unexpected error: " + e.getMessage())
					.build();
		}
	}
}
