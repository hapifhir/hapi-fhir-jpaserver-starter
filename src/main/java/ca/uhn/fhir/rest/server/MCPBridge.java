package ca.uhn.fhir.rest.server;

import static org.opencds.cqf.fhir.cr.hapi.config.test.TestCdsHooksConfig.CDS_HOOKS_OBJECT_MAPPER_FACTORY;

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

import org.apache.jena.base.Sys;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.w3._1999.xhtml.I;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class MCPBridge {

	private static final Logger logger = LoggerFactory.getLogger(MCPBridge.class);

	@Autowired
	ICdsServiceRegistry cdsServiceRegistry;

	@Autowired
	@Qualifier(CDS_HOOKS_OBJECT_MAPPER_FACTORY)
	ObjectMapper objectMapper;

	private final RestfulServer restfulServer;
	private final FhirContext fhirContext;
	private final CallToolResultFactory callToolResultFactory;

	public MCPBridge(RestfulServer restfulServer, CallToolResultFactory callToolResultFactory) {
		this.restfulServer = restfulServer;
		this.fhirContext = restfulServer.getFhirContext();
		this.callToolResultFactory = callToolResultFactory;
	}

	public List<McpServerFeatures.SyncToolSpecification> generateTools() {

		try {
			return List.of(
					// TODO Add CDS Hooks tool only if CR & CDS Hooks are enabled (CDS Hooks depends on CR)
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.callCdsHook(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.CALL_CDS_HOOK)),
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
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.PATCH)),
					new McpServerFeatures.SyncToolSpecification(
							ToolFactory.createFhirTransaction(),
							(exchange, contextMap) -> getToolResult(contextMap, Interaction.TRANSACTION)));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private McpSchema.CallToolResult getToolResult(Map<String, Object> contextMap, Interaction interaction) {

		if (interaction == Interaction.CALL_CDS_HOOK) {

			// Print the keys of contextMap
			for (String key : contextMap.keySet()) {
				System.out.println("Context map key: " + key);
			}

			// TODO Build up CDS Hooks request JSON from contextMap
			CdsHooksRequest request = new CdsHooksRequest();
			request.setHook(contextMap.get("hook").toString());
			request.setHookInstance(contextMap.get("hookInstance").toString());

			// Context
			CdsServiceRequestContextJson context = new CdsServiceRequestContextJson();
			Map<String, String> hookContext = (Map<String, String>) contextMap.get("hookContext");
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
					String json = new Gson().toJson(value);
					System.out.println("Prefetch data for " + key + ": " + value);
					System.out.println("FHIR JSON: " + json);
					IBaseResource resource = fhirContext.newJsonParser().parseResource(json);
					request.addPrefetch(key, resource);
				}
			}

			try {
				String requestString = objectMapper.writeValueAsString(request);
				System.out.println("CDS Hooks request JSON: " + requestString);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			String service = contextMap.get("service").toString();
			CdsServiceResponseJson serviceResponseJson = cdsServiceRegistry.callService(service, request);

			// Copy from CdsHooksServlet, including the comment below
			// Using GSON pretty print format as Jackson's is ugly
			String jsonResponse = "";
			try {
				jsonResponse = new GsonBuilder()
						.disableHtmlEscaping()
						.setPrettyPrinting()
						.create()
						.toJson(JsonParser.parseString(objectMapper.writeValueAsString(serviceResponseJson)));
			} catch (JsonSyntaxException e) {
				// TODO Return MCP Error
				jsonResponse = "{\"error\": \"" + e.getMessage() + "\"}";
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Return MCP Error
				jsonResponse = "{\"error\": \"" + e.getMessage() + "\"}";
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}

			return McpSchema.CallToolResult.builder()
				.addContent(new McpSchema.TextContent(jsonResponse))
				.build();
		}

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
}
