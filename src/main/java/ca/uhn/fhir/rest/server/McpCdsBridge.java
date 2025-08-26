package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.cdshooks.CdsHooksRequest;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.rest.api.server.cdshooks.CdsServiceRequestContextJson;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsServiceRegistry;
import ca.uhn.hapi.fhir.cdshooks.api.json.CdsServiceResponseJson;
import ca.uhn.hapi.fhir.cdshooks.module.CdsHooksObjectMapperFactory;
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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class McpCdsBridge implements McpBridge {

	private static final Logger logger = LoggerFactory.getLogger(McpCdsBridge.class);

	private final ICdsServiceRegistry cdsServiceRegistry;
	private final ObjectMapper objectMapper;
	private final FhirContext fhirContext;

	public McpCdsBridge(FhirContext fhirContext, ICdsServiceRegistry cdsServiceRegistry) {
		this.fhirContext = fhirContext;
		this.cdsServiceRegistry = cdsServiceRegistry;
		this.objectMapper = new CdsHooksObjectMapperFactory(fhirContext).newMapper();
	}

	public List<McpServerFeatures.SyncToolSpecification> generateTools() {

		try {
			return List.of(new McpServerFeatures.SyncToolSpecification.Builder()
					.tool(ToolFactory.callCdsHook())
					.callHandler((exchange, request) -> getToolResult(request, Interaction.CALL_CDS_HOOK))
					.build());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private McpSchema.CallToolResult getToolResult(McpSchema.CallToolRequest contextMap, Interaction interaction) {

		if (interaction == Interaction.CALL_CDS_HOOK) return invokeCDS(contextMap.arguments());
		throw new UnsupportedOperationException("Unsupported interaction: " + interaction);
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
