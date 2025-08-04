package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.jpa.starter.mcp.*;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component
public class SyncToolSpecGenerator {

	private final RestfulServer restfulServer;
	private final FhirContext fhirContext;
	private final IncrementalSyncStrategy incrementalSyncStrategy;
	private final List<McpServerFeatures.SyncToolSpecification> syncTools;

	public SyncToolSpecGenerator(RestfulServer restfulServer,
										  FhirContext fhirContext,
										  Optional<IncrementalSyncStrategy> incrementalSyncStrategy) {
		this.restfulServer = restfulServer;
		this.fhirContext = fhirContext;
		this.incrementalSyncStrategy = incrementalSyncStrategy.orElse(new NoOpIncrementalSyncStrategy());
		syncTools = generateSyncTools(restfulServer.getResourceProviders());
	}

	public List<McpServerFeatures.SyncToolSpecification> generateSyncTools(List<IResourceProvider> providers) {
		return providers.stream()
			.flatMap(provider -> {
				String resourceType = provider.getResourceType().getSimpleName();
				Set<Interaction> supported = detectSupportedInteractions(provider);
				return supported.stream()
					.map(interaction -> buildSpecification(resourceType, interaction));
			})
			.collect(Collectors.toList());
	}

	private Set<Interaction> detectSupportedInteractions(Object provider) {
		Set<Interaction> found = new HashSet<>();
		for (var method : provider.getClass().getMethods()) {
			if (method.isAnnotationPresent(Search.class)) found.add(Interaction.SEARCH);
			if (method.isAnnotationPresent(Read.class)) found.add(Interaction.READ);
			if (method.isAnnotationPresent(Create.class)) found.add(Interaction.CREATE);
			if (method.isAnnotationPresent(Update.class)) found.add(Interaction.UPDATE);
			if (method.isAnnotationPresent(Delete.class)) found.add(Interaction.DELETE);
			if (method.isAnnotationPresent(Patch.class)) found.add(Interaction.PATCH);
		}
		return found;
	}

	String inputSchemaJson = """
		{
		  "$schema": "https://json-schema.org/draft/2020-12/schema",
		  "type": "object",
		  "properties": {
		    "resourceType": { "type": "string" },
		    "interaction": {
		      "type": "string",
		      "enum": ["search", "read", "create", "update", "delete", "patch"]
		    },
		    "id": { "type": "string" },
		    "resource": { "type": "object" },
		    "patch": { "type": "object" },
		    "searchParams": {
		      "type": "object",
		      "additionalProperties": { "type": "string" }
		    }
		  },
		  "required": ["resourceType", "interaction"],
		  "additionalProperties": false
		}
		""";

	private McpServerFeatures.SyncToolSpecification buildSpecification(String resourceType, Interaction interaction) {
		String toolName = String.format("sync-%s-%s", resourceType.toLowerCase(), interaction.getName());
		String description = String.format("%s %s resources via FHIR", capitalize(interaction.getName()), resourceType);

		McpSchema.Tool tool = new McpSchema.Tool(toolName, description, inputSchemaJson);

		Map<String, Object> baseConfig = Map.of(
			"resourceType", resourceType,
			"interaction", interaction.getName()
		);

		BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> call = (exchange, overrideConfig) -> {
			Map<String, Object> config = mergeConfigs(baseConfig, overrideConfig);
			config = incrementalSyncStrategy.enrichConfigForSync(config);

			String rt = (String) config.get("resourceType");
			String interName = (String) config.get("interaction");

			try {
				RequestBuilder builder = new RequestBuilder(fhirContext, rt, Interaction.fromString(interName), config);
				var request = builder.buildRequest();
				MockHttpServletResponse response = new MockHttpServletResponse();

				RequestTypeEnum requestType = mapToRequestTypeEnum(interName);
				restfulServer.handleRequest(requestType, request, response);

				int status = response.getStatus();
				String body = response.getContentAsString();

				if (status >= 200 && status < 300) {
					if (body == null || body.isBlank()) {
						return McpSchema.CallToolResult.builder().isError(true).addTextContent("Empty successful response for " + interName).build();
					}
					IBaseResource parsed = (IBaseResource) fhirContext.newJsonParser().parseResource(body);
					incrementalSyncStrategy.extractNextState(parsed);

					return CallToolResultFactory.success(rt, interName, parsed, status);
				} else {
					return CallToolResultFactory.failure(String.format("FHIR server error %d: %s", status, body));
				}
			} catch (/*ServletException |*/ IOException e) {
				return CallToolResultFactory.failure("Dispatch error: " + e.getMessage());
			} catch (Exception e) {
				return McpSchema.CallToolResult.builder().isError(true).addTextContent("Unexpected error: " + e.getMessage()).build();
			}
		};

		return new McpServerFeatures.SyncToolSpecification(tool, call);
	}

	private String capitalize(String s) {
		if (s == null || s.isEmpty()) return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private Map<String, Object> mergeConfigs(Map<String, Object> base, Map<String, Object> override) {
		if (override == null || override.isEmpty()) return base;
		Map<String, Object> merged = new HashMap<>(base);
		merged.putAll(override);
		return merged;
	}

	private RequestTypeEnum mapToRequestTypeEnum(String interaction) {
		return switch (interaction) {
			case "search", "read" -> RequestTypeEnum.GET;
			case "create" -> RequestTypeEnum.POST;
			case "update" -> RequestTypeEnum.PUT;
			case "delete" -> RequestTypeEnum.DELETE;
			case "patch" -> RequestTypeEnum.PATCH;
			default -> throw new IllegalArgumentException("Unknown interaction: " + interaction);
		};
	}

	public List<McpServerFeatures.SyncToolSpecification> getSyncTools() {
		return syncTools;
	}
}
