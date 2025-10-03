package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ch.ahdis.matchbox.mcp.ToolFactory;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.mcp.CallToolResultFactory;
import ca.uhn.fhir.jpa.starter.mcp.Interaction;
import ca.uhn.fhir.jpa.starter.mcp.RequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.hl7.fhir.r5.model.OperationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import java.rmi.server.Operation;
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
							.callHandler((exchange, request) -> getToolResult(request, Interaction.VALIDATE))
					 		.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.listFhirImplementationGuides())
							.callHandler((exchange, request) -> getFhirImplementationGuides(request, Interaction.SEARCH))
					 		.build(),
					new McpServerFeatures.SyncToolSpecification.Builder()
							.tool(ToolFactory.listFhirProfilesToValidateFor())
							.callHandler((exchange, request) -> getFhirProfilesToValidateFor(request, Interaction.READ))
					 		.build()
							);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private McpSchema.CallToolResult getFhirProfilesToValidateFor(McpSchema.CallToolRequest contextMap, Interaction interaction) {
		var response = new MockHttpServletResponse();
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
					.filter(targetProfile -> ig==null || ig.isEmpty() || (targetProfile.hasExtension("ig") && targetProfile.getExtensionByUrl("ig").getValueStringType().asStringValue().equals(ig)))
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

	private McpSchema.CallToolResult getToolResult(McpSchema.CallToolRequest contextMap, Interaction interaction) {

		var response = new MockHttpServletResponse();
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
