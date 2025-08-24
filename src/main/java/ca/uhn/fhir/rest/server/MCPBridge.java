package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.cdshooks.CdsHooksRequest;
import ca.uhn.fhir.jpa.starter.mcp.CallToolResultFactory;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.RequestBuilder;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.rest.api.server.cdshooks.CdsServiceRequestContextJson;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsServiceRegistry;
import ca.uhn.hapi.fhir.cdshooks.api.json.CdsServiceResponseJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.opencds.cqf.fhir.cr.hapi.config.test.TestCdsHooksConfig.CDS_HOOKS_OBJECT_MAPPER_FACTORY;

@Component
public class MCPBridge {

	private static final Logger logger = LoggerFactory.getLogger(MCPBridge.class);

	private final ICdsServiceRegistry cdsServiceRegistry;

	private final ObjectMapper objectMapper;

	private final RestfulServer restfulServer;
	private final FhirContext fhirContext;
	private final CallToolResultFactory callToolResultFactory;

	public MCPBridge(
			RestfulServer restfulServer,
			CallToolResultFactory callToolResultFactory,
			ICdsServiceRegistry cdsServiceRegistry,
			@Qualifier(CDS_HOOKS_OBJECT_MAPPER_FACTORY) ObjectMapper objectMapper) {
		this.restfulServer = restfulServer;
		this.fhirContext = restfulServer.getFhirContext();
		this.callToolResultFactory = callToolResultFactory;
		this.cdsServiceRegistry = cdsServiceRegistry;
		this.objectMapper = objectMapper;
	}

	public List<McpServerFeatures.SyncToolSpecification> generateTools() {

		try {
			return List.of(
					// TODO Add CDS Hooks tool only if CR & CDS Hooks are enabled (CDS Hooks depends on CR)
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.callCdsHook())
							.callHandler((exchange, request) -> getToolResult(request, Interaction.CALL_CDS_HOOK))
							.build(),
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

		if (interaction == Interaction.CALL_CDS_HOOK) return invokeCDS(contextMap.arguments());
		else return invokeFhirService(contextMap.arguments(), interaction);
	}

	private McpSchema.CallToolResult invokeFhirService(Map<String, Object> contextMap, Interaction interaction) {
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

				return callToolResultFactory.success(
						contextMap.get("resourceType").toString(), interaction, parsed, status);
			} else {
				return callToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return callToolResultFactory.failure("Dispatch error: " + e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return McpSchema.CallToolResult.builder()
					.isError(true)
					.addTextContent("Unexpected error: " + e.getMessage())
					.build();
		}
	}

	private McpSchema.CallToolResult invokeCDS(Map<String, Object> contextMap) {
		CdsHooksRequest cdsInvocation = constructCdsHooksRequest(contextMap);
		CdsServiceResponseJson serviceResponseJson =
				cdsServiceRegistry.callService(contextMap.get("service").toString(), cdsInvocation);
		String jsonResponse = constructMCPResponse(serviceResponseJson);

		return McpSchema.CallToolResult.builder()
				.addContent(new McpSchema.TextContent(jsonResponse))
				.build();
	}

	private String constructMCPResponse(CdsServiceResponseJson serviceResponseJson) {
		// Copy from CdsHooksServlet, including the comment below
		// Using GSON pretty print format as Jackson's is ugly
		try {
			return new GsonBuilder()
					.disableHtmlEscaping()
					.setPrettyPrinting()
					.create()
					.toJson(JsonParser.parseString(objectMapper.writeValueAsString(serviceResponseJson)));
		} catch (JsonSyntaxException | JsonProcessingException e) {
			// TODO Return MCP Error
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	private @NotNull CdsHooksRequest constructCdsHooksRequest(Map<String, Object> contextMap) {

		// TODO Build up CDS Hooks request JSON from contextMap
		CdsHooksRequest request = new CdsHooksRequest();
		request.setHook(contextMap.get("hook").toString());
		request.setHookInstance(contextMap.get("hookInstance").toString());

		// Context
		CdsServiceRequestContextJson context = new CdsServiceRequestContextJson();
		var hookContext = (Map<String, String>) contextMap.get("hookContext");
		if (hookContext.containsKey("userId")) {
			context.put("userId", hookContext.get("userId").toString());
		}
		if (hookContext.containsKey("patientId")) {
			context.put("patientId", hookContext.get("patientId").toString());
		}
		if (hookContext.containsKey("encounterId")) {
			context.put("encounterId", hookContext.get("encounterId").toString());
		}
		request.setContext(context);

		// Prefetch
		if (contextMap.containsKey("prefetch")) {
			Object prefetch = contextMap.get("prefetch");
			Map<String, Object> prefetchMap = (Map<String, Object>) prefetch;
			for (Map.Entry<String, Object> entry : prefetchMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();

				// Object is a String -> Object map
				// Use a standard JSON library to convert it
				IBaseResource resource = fhirContext.newJsonParser().parseResource(new Gson().toJson(value));
				request.addPrefetch(key, resource);
			}
		}

		return request;
	}
}
