package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ch.ahdis.matchbox.mcp.ToolFactory;
import ch.ahdis.matchbox.util.CrossVersionResourceUtils;
import ch.ahdis.matchbox.util.http.MatchboxFhirFormat;
import ca.uhn.fhir.jpa.starter.mcp.CallToolResultFactory;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.RequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.hl7.fhir.r5.model.OperationDefinition.OperationDefinitionParameterComponent;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.Enumerations.OperationParameterUse;
import org.hl7.fhir.r5.model.OperationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class McpMatchboxBridge implements McpBridge {

	private static final Logger logger = LoggerFactory.getLogger(McpMatchboxBridge.class);

	private final RestfulServer restfulServer;

	public McpMatchboxBridge(RestfulServer restfulServer) {
		this.restfulServer = restfulServer;
	}

	public List<McpServerFeatures.SyncToolSpecification> generateTools() {
		try {
			return List.of(
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.validateFhirResource())
							.callHandler((exchange, request) -> getValidationResult(request, Interaction.VALIDATE))
					 		.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.listFhirImplementationGuides())
							.callHandler((exchange, request) -> getFhirImplementationGuides(request, Interaction.SEARCH))
					 		.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.listFhirProfilesToValidateFor())
							.callHandler((exchange, request) -> getFhirProfilesToValidateFor(request, Interaction.READ))
					 		.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.listValidationParameters())
							.callHandler((exchange, request) -> getExtraValidationParameters(request, Interaction.READ))
					 		.build()
							);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private McpSchema.CallToolResult getFhirProfilesToValidateFor(McpSchema.CallToolRequest contextMap, Interaction interaction) {
		var response = new MockHttpServletResponse();
		final String resourceType = (String) contextMap.arguments().get("resourceType");
		// we need to overwrite it for calling the read interaction
		contextMap.arguments().put("resourceType", "OperationDefinition");
		contextMap.arguments().put("id", "-s-validate");
		final String ig = (String) contextMap.arguments().get("ig");
		var request = new RequestBuilder(restfulServer, contextMap.arguments(), interaction).buildRequest();
		try {
			restfulServer.handleRequest(interaction.asRequestType(), request, response);
			var status = response.getStatus();
			var body = response.getContentAsString();

			if (status >= 200 && status < 300) {
				if (body.isBlank()) {
					return CallToolResultFactory.failure("Empty successful response for " + interaction);
				}

				FhirContext fhirR5Context = FhirContext.forR5Cached();
				OperationDefinition operationDefinition = fhirR5Context.newJsonParser().parseResource(OperationDefinition.class, body);
				Object[] profiles = operationDefinition.getParameter().stream()
					.filter(p -> p.getName().equals("profile"))
					.map(p -> p.getTargetProfile())
					.flatMap(List::stream)
					.filter(targetProfile -> targetProfile.hasExtension("ig-current") && targetProfile.getExtensionByUrl("ig-current").getValueBooleanType().booleanValue())
					.filter(targetProfile -> ig==null || ig.isEmpty() || (targetProfile.hasExtension("ig-id") && targetProfile.getExtensionByUrl("ig-id").getValueStringType().asStringValue().equals(ig)))
					.filter(targetProfile -> resourceType==null || resourceType.isEmpty() || (targetProfile.hasExtension("sd-title") && targetProfile.getExtensionByUrl("sd-title").getValueStringType().asStringValue().startsWith(resourceType)))
					.map( targetProfile -> {
						var map = new HashMap<String, String>();
						map.put("title", targetProfile.getExtensionByUrl("sd-title").getValueStringType().asStringValue());
						map.put("profile", targetProfile.getValue());
						map.put("ig",  targetProfile.getExtensionByUrl("ig-id").getValueStringType().asStringValue());
						return map;
						} ).toArray();
		        return CallToolResultFactory.successPayload(profiles);
			} else {
				return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return CallToolResultFactory.failure("Unexpected error: " + e.getMessage());
		}
	}

	private McpSchema.CallToolResult getExtraValidationParameters(McpSchema.CallToolRequest contextMap, Interaction interaction) {
		var response = new MockHttpServletResponse();
		contextMap.arguments().put("resourceType", "OperationDefinition");
		contextMap.arguments().put("id", "-s-validate");
		var request = new RequestBuilder(restfulServer, contextMap.arguments(), interaction).buildRequest();
		try {
			restfulServer.handleRequest(interaction.asRequestType(), request, response);
			var status = response.getStatus();
			var body = response.getContentAsString();

			if (status >= 200 && status < 300) {
				if (body.isBlank()) {
					return CallToolResultFactory.failure("Empty successful response for " + interaction);
				}

				FhirContext fhirR5Context = FhirContext.forR5Cached();
				OperationDefinition operationDefinition = fhirR5Context.newJsonParser().parseResource(OperationDefinition.class, body);
				List<OperationDefinitionParameterComponent> parameters = operationDefinition.getParameter().stream()
					.filter(p -> OperationParameterUse.IN.equals(p.getUse()))
					.filter(p -> !"resource".equals(p.getName()))
					.filter(p -> !"profile".equals(p.getName()))
					.map(p -> {
						p.setUse(null);
						return p;
					}).toList();
				
				
				StringWriter result = new StringWriter();
				result.append("[ ");
				for (int i = 0; i < parameters.size(); i++) {
					CrossVersionResourceUtils.serializeR5(parameters.get(i), MatchboxFhirFormat.JSON, result);
					result.append(",");
				}
				result.append("]");

				String json = result.toString();
		        return CallToolResultFactory.successFhirBody(json);
			} else {
				return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return CallToolResultFactory.failure("Unexpected error: " + e.getMessage());
		}
	}

	private McpSchema.CallToolResult getFhirImplementationGuides(McpSchema.CallToolRequest contextMap, Interaction interaction) {
		var response = new MockHttpServletResponse();
		contextMap.arguments().put("resourceType", "ImplementationGuide");
		if (contextMap.arguments().containsKey("includeVersions")) {
			if (contextMap.arguments().get("includeVersions").equals("false")) {
				Map<String, Object> map = new java.util.HashMap<>();
				map.put("_tag", "http://matchbox.health/fhir/CodeSystem/tag|current");
				contextMap.arguments().put("query", map);
			}
			contextMap.arguments().remove("includeVersions");
		} else {
				Map<String, Object> map = new java.util.HashMap<>();
				map.put("_tag", "http://matchbox.health/fhir/CodeSystem/tag|current");
				contextMap.arguments().put("query", map);
		}
		var request = new RequestBuilder(restfulServer, contextMap.arguments(), interaction).buildRequest();
		try {
			restfulServer.handleRequest(interaction.asRequestType(), request, response);
			var status = response.getStatus();
			var body = response.getContentAsString();

			if (status >= 200 && status < 300) {
				if (body.isBlank()) {
					return CallToolResultFactory.failure("Empty successful response for " + interaction);
				}
				return CallToolResultFactory.successFhirBody(body);
			} else {
				return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return CallToolResultFactory.failure("Unexpected error: " + e.getMessage());
		}
	}	

	private McpSchema.CallToolResult getValidationResult(McpSchema.CallToolRequest contextMap, Interaction interaction) {

		var response = new MockHttpServletResponse();
		if (contextMap.arguments().containsKey("validationparams")) {
			Map<String, Object> map = new java.util.HashMap<>();
			String validationParams = (String) contextMap.arguments().get("validationparams");
			// Parse the validationParams string into a Map
			String[] params = validationParams.split(",");
			for (String param : params) {
				String[] keyValue = param.split("=");
				if (keyValue.length == 2) {
					map.put(keyValue[0].trim(), keyValue[1].trim());
				}
			}
			contextMap.arguments().put("query", map);
		} else {
			Map<String, Object> map = new java.util.HashMap<>();
			map.put("analyzeOutcomeWithAI", "false");
			contextMap.arguments().put("query", map);
		}

		var request = new RequestBuilder(restfulServer, contextMap.arguments(), interaction).buildRequest();

		try {
			restfulServer.handleRequest(interaction.asRequestType(), request, response);
			var status = response.getStatus();
			var body = response.getContentAsString();

			if (status >= 200 && status < 300) {
				if (body.isBlank()) {
					return CallToolResultFactory.failure("Empty successful response for " + interaction);
				}
				return CallToolResultFactory.successFhirBody(body);
			} else {
				return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return CallToolResultFactory.failure("Unexpected error: " + e.getMessage());
		}
	}
}
