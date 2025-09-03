package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.cdshooks.CdsHooksRequest;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.rest.api.server.cdshooks.CdsServiceRequestContextJson;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsServiceRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
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

	public McpCdsBridge(FhirContext fhirContext, ICdsServiceRegistry cdsServiceRegistry, ObjectMapper objectMapper) {
		this.fhirContext = fhirContext;
		this.cdsServiceRegistry = cdsServiceRegistry;
		this.objectMapper = objectMapper;
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

		if (interaction != Interaction.CALL_CDS_HOOK)
			throw new UnsupportedOperationException("Unsupported interaction: " + interaction);

		var cdsInvocation = constructCdsHooksRequest(contextMap);
		var serviceResponseJson = cdsServiceRegistry.callService(
				contextMap.arguments().get("service").toString(), cdsInvocation);

		final String content;
		try {
			content = objectMapper.writeValueAsString(serviceResponseJson);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return McpSchema.CallToolResult.builder()
				.addContent(new McpSchema.TextContent(content))
				.build();
	}

	private @NotNull CdsHooksRequest constructCdsHooksRequest(McpSchema.CallToolRequest callToolRequest) {

		// TODO Build up CDS Hooks request JSON from contextMap
		var contextMap = callToolRequest.arguments();
		var request = new CdsHooksRequest();
		request.setHook(contextMap.get("hook").toString());
		request.setHookInstance(contextMap.get("hookInstance").toString());

		// Context
		var context = new CdsServiceRequestContextJson();
		Object hookContextObj = contextMap.get("hookContext");
		if (hookContextObj instanceof Map<?, ?> hookContext) {
			if (hookContext.containsKey("userId")) {
				context.put("userId", String.valueOf(hookContext.get("userId")));
			}
			if (hookContext.containsKey("patientId")) {
				context.put("patientId", String.valueOf(hookContext.get("patientId")));
			}
			if (hookContext.containsKey("encounterId")) {
				context.put("encounterId", String.valueOf(hookContext.get("encounterId")));
			}
		}
		request.setContext(context);

		// Prefetch
		if (contextMap.containsKey("prefetch")) {
			var prefetch = contextMap.get("prefetch");
			if (prefetch instanceof Map) {
				@SuppressWarnings("unchecked")
				var prefetchMap = (Map<String, Object>) prefetch;
				for (Map.Entry<String, Object> entry : prefetchMap.entrySet()) {
					var key = entry.getKey();
					var value = entry.getValue();

					// Object is a String -> Object map
					// Use a standard JSON library to convert it
					var resource = fhirContext.newJsonParser().parseResource(new Gson().toJson(value));
					request.addPrefetch(key, resource);
				}
			} else {
				logger.warn(
						"Prefetch object is not a Map: {}",
						prefetch == null ? "null" : prefetch.getClass().getName());
			}
		}

		return request;
	}
}
