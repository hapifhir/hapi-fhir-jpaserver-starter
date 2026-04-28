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

import java.util.List;
import java.util.Optional;

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
					buildSpecification(ToolFactory.createFhirResource(), Interaction.CREATE),
					buildSpecification(ToolFactory.readFhirResource(), Interaction.READ),
					buildSpecification(ToolFactory.updateFhirResource(), Interaction.UPDATE),
					buildSpecification(ToolFactory.deleteFhirResource(), Interaction.DELETE),
					buildSpecification(ToolFactory.conditionalPatchFhirResource(), Interaction.PATCH),
					buildSpecification(ToolFactory.searchFhirResources(), Interaction.SEARCH),
					buildSpecification(ToolFactory.conditionalUpdateFhirResource(), Interaction.UPDATE),
					buildSpecification(ToolFactory.patchFhirResource(), Interaction.PATCH),
					buildSpecification(ToolFactory.createFhirTransaction(), Interaction.TRANSACTION));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private McpServerFeatures.SyncToolSpecification buildSpecification(
			McpSchema.Tool tool, Interaction interaction) {
		return new McpServerFeatures.SyncToolSpecification.Builder()
				.tool(tool)
				.callHandler((exchange, request) -> getToolResult(request, interaction))
				.build();
	}

	private String getContextPath() {
		try {
			return Optional.ofNullable(restfulServer.getServletContext())
					.map(ctx -> ctx.getContextPath() != null ? ctx.getContextPath() : "")
					.orElse("");
		} catch (IllegalStateException e) {
			return "";
		}
	}

	private McpSchema.CallToolResult getToolResult(McpSchema.CallToolRequest contextMap, Interaction interaction) {

		var response = new MockHttpServletResponse();
		var request =
				new RequestBuilder(fhirContext, contextMap.arguments(), interaction, getContextPath()).buildRequest();

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
