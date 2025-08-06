package ca.uhn.fhir.jpa.starter.mcp;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ToolFactory {

	private static final String READ_FHIR_RESOURCE_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resourceType": {
			"type": "string",
			"description": "type of the resource to read"
			},
			"id": {
			"type": "string",
			"description": "id of the resource to read"
			}
		}
		
		}
		""";

	private static final String CREATE_FHIR_RESOURCE_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resourceType": {
			"type": "string",
			"description": "Type of the resource to create"
			},
			"resource": {
			"type": "object",
			"description": "Resource content in JSON format"
			},
			"headers": {
			"type": "object",
			"description": "Headers for create request.\\nAvailable headers: If-None-Exist header for conditional create where the value is search param string.\\nFor example: {\\"If-None-Exist\\": \\"active=false\\"}"
			}
		},
		"required": ["resourceType", "resource"]
		}
		""";

	private static final String UPDATE_FHIR_RESOURCE_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resourceType": {
			"type": "string",
			"description": "Type of the resource to update"
			},
			"id": {
			"type": "string",
			"description": "ID of the resource to update"
			},
			"resource": {
			"type": "object",
			"description": "Updated resource content in JSON format"
			}
		},
		"required": ["resourceType", "id", "resource"]
		}
		""";

	private static final String CONDITIONAL_UPDATE_FHIR_RESOURCE_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resourceType": {
			"type": "string",
			"description": "Type of the resource to update"
			},
			"resource": {
			"type": "object",
			"description": "Updated resource content in JSON format"
			},
			"query": {
			"type": "string",
			"description": "Query string with search params separate by \\",\\". For example: \\"_id=pt-1,name=ivan\\". Uses for conditional update."
			},
			"headers": {
			"type": "object",
			"description": "Headers for create request.\\nAvailable headers: If-None-Match header for conditional update where the value is ETag.\\nFor example: {\\"If-None-Match\\": \\"12345\\"}"
			}
		},
		"required": ["resourceType", "resource"]
		}
		""";

	private static final String CONDITIONAL_PATCH_FHIR_RESOURCE_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resourceType": {
			"type": "string",
			"description": "Type of the resource to patch"
			},
			"resource": {
			"type": "object",
			"description": "Resource content to patch in JSON format"
			},
			"query": {
			"type": "string",
			"description": "Query string with search params separate by \\",\\". For example: \\"_id=pt-1,name=ivan\\". Uses for conditional patch."
			},
			"headers": {
			"type": "object",
			"description": "Headers for create request.\\nAvailable headers: If-None-Match header for conditional patch where the value is ETag.\\nFor example: {\\"If-None-Match\\": \\"12345\\"}"
			}
		},
		"required": ["resourceType", "resource"]
		}
		""";

	private static final String PATCH_FHIR_RESOURCE_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resourceType": {
			"type": "string",
			"description": "Type of the resource to patch"
			},
			"id": {
			"type": "string",
			"description": "ID of the resource to patch"
			},
			"resource": {
			"type": "object",
			"description": "Resource content to patch in JSON format"
			}
		},
		"required": ["resourceType", "id", "resource"]
		}
		""";

	private static final String DELETE_FHIR_RESOURCE_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resourceType": {
			"type": "string",
			"description": "Type of the resource to delete"
			},
			"id": {
			"type": "string",
			"description": "ID of the resource to delete"
			}
		},
		"required": ["resourceType", "id"]
		}
		""";

	private static final String SEARCH_FHIR_RESOURCES_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resourceType": {
			"type": "string",
			"description": "Type of the resource to search"
			},
			"query": {
			"type": "string",
			"description": "Query string with search params separate by \\",\\". For example: \\"_id=pt-1,name=ivan\\""
			}
		},
		"required": ["resourceType", "query"]
		}
		""";

	private static final String CREATE_FHIR_TRANSACTION_SCHEMA =
			"""
	{
	"type": "object",
	"properties": {
		"resourceType": {
		"type": "string",
		"description": "A Bundle resource type with type 'transaction' containing multiple FHIR resources"
		},
		"resource": {
		"type": "object",
		"description": "A FHIR Bundle Resource content in JSON format"
		}
	},
	"required": ["resourceType", "resource"]
	}
	""";

	public static Tool readFhirResource() throws JsonProcessingException {
		return new Tool(
				"read-fhir-resource",
				"Read an individual FHIR resource",
				mapper.readValue(READ_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static Tool createFhirResource() throws JsonProcessingException {
		return new Tool(
				"create-fhir-resource",
				"Create a new FHIR resource",
				mapper.readValue(CREATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static Tool updateFhirResource() throws JsonProcessingException {
		return new Tool(
				"update-fhir-resource",
				"Update an existing FHIR resource",
				mapper.readValue(UPDATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static Tool conditionalUpdateFhirResource() throws JsonProcessingException {
		return new Tool(
				"conditional-update-fhir-resource",
				"Conditional update an existing FHIR resource",
				mapper.readValue(CONDITIONAL_UPDATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static Tool conditionalPatchFhirResource() throws JsonProcessingException {
		return new Tool(
				"conditional-patch-fhir-resource",
				"Conditional patch an existing FHIR resource",
				mapper.readValue(CONDITIONAL_PATCH_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static Tool patchFhirResource() throws JsonProcessingException {
		return new Tool(
				"patch-fhir-resource",
				"Patch an existing FHIR resource",
				mapper.readValue(PATCH_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static Tool deleteFhirResource() throws JsonProcessingException {
		return new Tool(
				"delete-fhir-resource",
				"Delete an existing FHIR resource",
				mapper.readValue(DELETE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static Tool searchFhirResources() throws JsonProcessingException {
		return new Tool(
				"search-fhir-resources",
				"Search an existing FHIR resources",
				mapper.readValue(SEARCH_FHIR_RESOURCES_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static Tool createFhirTransaction() throws JsonProcessingException {
		return new Tool(
				"create-fhir-transaction",
				"Create a FHIR transaction",
				mapper.readValue(CREATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class));
	}

	public static final ObjectMapper mapper = new ObjectMapper()
			.enable(JsonParser.Feature.ALLOW_COMMENTS)
			.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
			.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
			.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
