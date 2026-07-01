package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.jpa.starter.mcp.CallToolResultFactory;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.RequestBuilder;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ch.ahdis.matchbox.CliContext;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class McpFhirBridge implements McpBridge {

  private static final Logger logger = LoggerFactory.getLogger(McpFhirBridge.class);

  private final RestfulServer restfulServer;
  private final CliContext cliContext;

  public McpFhirBridge(RestfulServer restfulServer, CliContext cliContext) {
    this.restfulServer = restfulServer;
    this.cliContext = cliContext;
  }

  public List<McpServerFeatures.SyncToolSpecification> generateTools() {
    if (!cliContext.getOnlyOneEngine()) {
      return Collections.emptyList();
    }
    
    final var tools = new ArrayList<McpServerFeatures.SyncToolSpecification>(9);
    tools.add(buildToolSpecification(ToolFactory.readFhirResource(), Interaction.READ));
    tools.add(buildToolSpecification(ToolFactory.searchFhirResources(), Interaction.SEARCH));
    if (!cliContext.isHttpReadOnly()) {
      tools.add(buildToolSpecification(ToolFactory.createFhirResource(), Interaction.CREATE));
      tools.add(buildToolSpecification(ToolFactory.updateFhirResource(), Interaction.UPDATE));
      tools.add(buildToolSpecification(ToolFactory.conditionalUpdateFhirResource(), Interaction.UPDATE));
      tools.add(buildToolSpecification(ToolFactory.deleteFhirResource(), Interaction.DELETE));
      tools.add(buildToolSpecification(ToolFactory.patchFhirResource(), Interaction.PATCH));
      tools.add(buildToolSpecification(ToolFactory.conditionalPatchFhirResource(), Interaction.PATCH));
      tools.add(buildToolSpecification(ToolFactory.createFhirTransaction(), Interaction.TRANSACTION));
    }
    return tools;
  }

  private McpServerFeatures.SyncToolSpecification buildToolSpecification(final McpSchema.Tool tool,
                                                                         final Interaction interaction) {
    return new McpServerFeatures.SyncToolSpecification(
      tool,
      (exchange, request) -> getToolResult(request, interaction)
    );
  }

  private McpSchema.CallToolResult getToolResult(final McpSchema.CallToolRequest toolRequest,
                                                 final Interaction interaction) {
    final var arguments = toolRequest.arguments();
    final var response = new MockHttpServletResponse();
    final var request = new RequestBuilder(restfulServer, arguments, interaction).buildRequest();

    try {
      restfulServer.handleRequest(interaction.asRequestType(), request, response);
      final var status = response.getStatus();
      final var body = response.getContentAsString();

      if (status >= 200 && status < 300) {
        if (body.isBlank()) {
          return CallToolResultFactory.failure("Empty successful response for " + interaction);
        }

        return CallToolResultFactory.success(
          arguments.get("resourceType").toString(), interaction, body, status);
      } else {
        return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return CallToolResultFactory.failure("Unexpected error: " + e.getMessage());
    }
  }
}
