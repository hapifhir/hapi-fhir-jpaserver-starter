package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.Application;
import ca.uhn.fhir.jpa.starter.mcp.CallToolResultFactory;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.McpRequestOrigin;
import ca.uhn.fhir.jpa.starter.mcp.RequestBuilder;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.rest.api.Constants;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

		return List.of(
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.createFhirResource())
						.callHandler((exchange, request) -> getToolResult(exchange, request, Interaction.CREATE))
						.build(),
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.readFhirResource())
						.callHandler((exchange, request) -> getToolResult(exchange, request, Interaction.READ))
						.build(),
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.updateFhirResource())
						.callHandler((exchange, request) -> getToolResult(exchange, request, Interaction.UPDATE))
						.build(),
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.deleteFhirResource())
						.callHandler((exchange, request) -> getToolResult(exchange, request, Interaction.DELETE))
						.build(),
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.conditionalPatchFhirResource())
						.callHandler(
								(exchange, request) -> getToolResult(exchange, request, Interaction.CONDITIONAL_PATCH))
						.build(),
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.searchFhirResources())
						.callHandler((exchange, request) -> getToolResult(exchange, request, Interaction.SEARCH))
						.build(),
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.conditionalUpdateFhirResource())
						.callHandler(
								(exchange, request) -> getToolResult(exchange, request, Interaction.CONDITIONAL_UPDATE))
						.build(),
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.patchFhirResource())
						.callHandler((exchange, request) -> getToolResult(exchange, request, Interaction.PATCH))
						.build(),
				new McpServerFeatures.SyncToolSpecification.Builder()
						.tool(ToolFactory.createFhirTransaction())
						.callHandler((exchange, request) -> getToolResult(exchange, request, Interaction.TRANSACTION))
						.build());
	}

	private McpSchema.CallToolResult getToolResult(
			McpSyncServerExchange exchange, McpSchema.CallToolRequest contextMap, Interaction interaction) {

		var response = new MockHttpServletResponse();
		String contextPath = restfulServer.getServletContext() != null
				? restfulServer.getServletContext().getContextPath()
				: "";
		Map<String, Object> arguments = contextMap.arguments() != null ? contextMap.arguments() : Map.of();

		// Invalid arguments (e.g. a missing resourceType or id) are reported as a tool error, not as a JSON-RPC error
		MockHttpServletRequest request;
		try {
			request = new RequestBuilder(
							fhirContext,
							arguments,
							interaction,
							contextPath,
							Application.FHIR_SERVLET_PATH,
							McpRequestOrigin.from(exchange))
					.buildRequest();
		} catch (IllegalArgumentException e) {
			logger.warn("Invalid arguments for {}: {}", interaction, e.getMessage());
			return CallToolResultFactory.failure("Invalid arguments: " + e.getMessage());
		}

		try {
			restfulServer.handleRequest(interaction.asRequestType(), request, response);
			var status = response.getStatus();
			var body = response.getContentAsString();

			if (status >= 200 && status < 300) {
				// A successful response can have no body, e.g. a conditional create that matched an existing
				// resource. The Location header then identifies the resource.
				return CallToolResultFactory.success(
						Objects.toString(arguments.get("resourceType"), ""),
						interaction,
						body,
						status,
						response.getHeader(Constants.HEADER_LOCATION));
			} else {
				return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return CallToolResultFactory.failure("Unexpected error: " + e.getMessage());
		}
	}
}
