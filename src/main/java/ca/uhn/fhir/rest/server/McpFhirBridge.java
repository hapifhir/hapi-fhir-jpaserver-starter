package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.mcp.CallToolResultFactory;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.RequestBuilder;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpFhirBridge implements McpBridge {

	private static final Logger logger = LoggerFactory.getLogger(McpFhirBridge.class);

	private final RestfulServer restfulServer;
	private final FhirContext fhirContext;

	public McpFhirBridge(RestfulServer restfulServer) {
		this.restfulServer = restfulServer;
		this.fhirContext = restfulServer.getFhirContext();
	}

	public List<McpServerFeatures.SyncToolSpecification> generateTools() {

		try {
			return List.of(
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.createFhirResource())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.CREATE))
							.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.readFhirResource())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.READ))
							.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.updateFhirResource())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.UPDATE))
							.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.deleteFhirResource())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.DELETE))
							.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.conditionalPatchFhirResource())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.PATCH))
							.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.searchFhirResources())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.SEARCH))
							.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.conditionalUpdateFhirResource())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.UPDATE))
							.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.patchFhirResource())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.PATCH))
							.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.createFhirTransaction())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.TRANSACTION))
							.build());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private McpSchema.CallToolResult getToolResult(McpSchema.CallToolRequest contextMap, Interaction interaction) {

		var response = new MockHttpServletResponse();
		var request = new RequestBuilder(fhirContext, contextMap.arguments(), interaction).buildRequest();

		try {
			restfulServer.handleRequest(interaction.asRequestType(), request, response);
			var status = response.getStatus();
			var body = response.getContentAsString();

			if (status >= 200 && status < 300) {
				if (body.isBlank()) {
					return CallToolResultFactory.failure("Empty successful response for " + interaction);
				}

				return CallToolResultFactory.success(
						contextMap.arguments().get("resourceType").toString(), interaction, body, status);
			} else {
				return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return CallToolResultFactory.failure("Unexpected error: " + e.getMessage());
		}
	}
}
