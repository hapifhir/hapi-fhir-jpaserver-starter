package ca.uhn.fhir.jpa.starter.mcp;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;


public class ToolFactory {

	public static final ObjectMapper mapper = new ObjectMapper()
			.enable(JsonParser.Feature.ALLOW_COMMENTS)
			.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
			.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
			.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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

	private static final String STORE_NAME_PROPERTY =
			"""
			"store": {
				"type": "string",
				"description": "Name of the store or tenant to service this request"
			}
			""";

	private static final String RESOURCE_NAME_PROPERTY =
			"""
			"resourceType": {
				"type": "string",
				"description": "Name of the FHIR resource type for this request"
			}
			""";

	private static final String GET_RESOURCE_LIST_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			%s
		},
		"required": ["store"]
		}
		""".formatted(STORE_NAME_PROPERTY);

	private static final String GET_DATA_TYPE_LIST_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			%s
		},
		"required": ["store"]
		}
		""".formatted(STORE_NAME_PROPERTY);

	private static final String GET_SEARCH_PARAMETERS_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			%s,
			%s
		},
		"required": ["store", "resourceType"]
		}
		""".formatted(STORE_NAME_PROPERTY, RESOURCE_NAME_PROPERTY);

	private static final String GET_RESOURCE_DEFINITION_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			%s
		},
		"required": ["resourceType"]
		}
		""".formatted(RESOURCE_NAME_PROPERTY);

	private static final String GET_DATA_TYPE_DEFINITION_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"datatypeName": {
				"type": "string",
				"description": "Name of the FHIR data type for this request"
			}
		},
		"required": ["datatypeName"]
		}
		""";

	private static final String GET_SEARCH_TYPE_DEFINITION_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"searchType": {
				"type": "string",
				"description": "FHIR search type to describe"
			}
		},
		"required": ["searchType"]
		}
		""";

	private static final String VALIDATE_TYPE_SEARCH_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			%s,
			%s,
			"searchString": {
				"type": "string",
				"description": "FHIR type search request to validate"
			}
		},
		"required": ["store", "resourceType", "searchString"]
		}
		""".formatted(STORE_NAME_PROPERTY, RESOURCE_NAME_PROPERTY);

private static final McpSchema.JsonSchema EMPTY_OBJECT_SCHEMA = createEmptyObjectSchema();

private static McpSchema.JsonSchema createEmptyObjectSchema() {
	try {
		return mapper.readValue(
				"""
				{
				"type": "object",
				"properties": {}
				}
				""",
				McpSchema.JsonSchema.class);
	} catch (JsonProcessingException e) {
		throw new IllegalStateException("Unable to construct empty object schema", e);
	}
}

	// TODO Add a tool for the CDS Hooks discovery endpoint
	// Alternatively, should each service be a separate tool?

	// TODO Add other fields from https://cds-hooks.hl7.org/STU2/#http-request-1
	// TODO Context here is for the patient-view hook, https://cds-hooks.hl7.org/hooks/STU1/patient-view.html#context
	private static final String CALL_CDS_HOOK_SCHEMA_2_0_1 =
			"""
		{
		"type": "object",
		"properties": {
			"service": {
				"type": "string",
				"description": "The CDS Service to call."
			},
			"hook": {
				"type": "string",
				"description": "The hook that triggered this CDS Service call."
			},
			"hookInstance": {
				"type": "string",
				"description": "A universally unique identifier (UUID) for this particular hook call."
			},
			"hookContext": {
				"type": "object",
				"description": "Hook-specific contextual data that the CDS service will need.",
				"properties": {
					"userId": {
						"type": "string",
						"description": "The id of the current user. Must be in the format [ResourceType]/[id]."
					},
					"patientId": {
						"type": "string",
						"description": "The FHIR Patient.id of the current patient in context"
					},
					"encounterId": {
						"type": "string",
						"description": "The FHIR Encounter.id of the current encounter in context."
					}
				}
			},
			"prefetch": {
				"type": "object",
				"description": "Additional data to prefetch for the CDS service call."
			}
		},
		"required": ["service", "hook", "hookInstance", "hookContext"]
		}
		""";

	public static Tool readFhirResource() throws JsonProcessingException {
		return new Tool.Builder()
				.name("read-fhir-resource")
				.description("Read an individual FHIR resource")
				.inputSchema(mapper.readValue(READ_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool createFhirResource() throws JsonProcessingException {
		return new Tool.Builder()
				.name("create-fhir-resource")
				.description("Create a new FHIR resource")
				.inputSchema(mapper.readValue(CREATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool updateFhirResource() throws JsonProcessingException {
		return new Tool.Builder()
				.name("update-fhir-resource")
				.description("Update an existing FHIR resource")
				.inputSchema(mapper.readValue(UPDATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool conditionalUpdateFhirResource() throws JsonProcessingException {
		return new Tool.Builder()
				.name("conditional-update-fhir-resource")
				.description("Conditional update an existing FHIR resource")
				.inputSchema(mapper.readValue(CONDITIONAL_UPDATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool conditionalPatchFhirResource() throws JsonProcessingException {
		return new Tool.Builder()
				.name("conditional-patch-fhir-resource")
				.description("Conditional patch an existing FHIR resource")
				.inputSchema(mapper.readValue(CONDITIONAL_PATCH_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool patchFhirResource() throws JsonProcessingException {
		return new Tool.Builder()
				.name("patch-fhir-resource")
				.description("Patch an existing FHIR resource")
				.inputSchema(mapper.readValue(PATCH_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool deleteFhirResource() throws JsonProcessingException {
		return new Tool.Builder()
				.name("delete-fhir-resource")
				.description("Delete an existing FHIR resource")
				.inputSchema(mapper.readValue(DELETE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool searchFhirResources() throws JsonProcessingException {
		return new Tool.Builder()
				.name("search-fhir-resources")
				.description("Search an existing FHIR resources")
				.inputSchema(mapper.readValue(SEARCH_FHIR_RESOURCES_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool createFhirTransaction() throws JsonProcessingException {
		return new Tool.Builder()
				.name("create-fhir-transaction")
				.description("Create a FHIR transaction")
				.inputSchema(mapper.readValue(CREATE_FHIR_TRANSACTION_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool callCdsHook() throws JsonProcessingException {
		return new Tool.Builder()
				.name("call-cds-hook")
				.description("Call a CDS Hook")
				.inputSchema(mapper.readValue(CALL_CDS_HOOK_SCHEMA_2_0_1, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool getStoreList() {
		return new Tool.Builder()
				.name("get-store-list")
				.description("Gets the list of FHIR stores, the base URL of the FHIR store, and their FHIR Versions that are configured on this server.")
				.inputSchema(EMPTY_OBJECT_SCHEMA)
				.build();
	}

	public static Tool getResourceList() throws JsonProcessingException {
		return new Tool.Builder()
				.name("get-resource-list")
				.description("Gets the list of FHIR resources supported by the specified resource store.")
				.inputSchema(mapper.readValue(GET_RESOURCE_LIST_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool getResourceDefinition() throws JsonProcessingException {
		return new Tool.Builder()
				.name("get-resource-definition")
				.description("Gets the definition of a FHIR resource.")
				.inputSchema(mapper.readValue(GET_RESOURCE_DEFINITION_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool getDataTypeList() throws JsonProcessingException {
		return new Tool.Builder()
				.name("get-data-type-list")
				.description("Gets the list of FHIR data types known in the specified resource store.")
				.inputSchema(mapper.readValue(GET_DATA_TYPE_LIST_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool getDataTypeDefinition() throws JsonProcessingException {
		return new Tool.Builder()
				.name("get-data-type-definition")
				.description("Gets the definition of a FHIR data type.")
				.inputSchema(mapper.readValue(GET_DATA_TYPE_DEFINITION_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool getSearchTypeList() {
		return new Tool.Builder()
				.name("get-search-type-list")
				.description("Gets the list of FHIR search types (not FHIR Data Types).")
				.inputSchema(EMPTY_OBJECT_SCHEMA)
				.build();
	}

	public static Tool getSearchTypeDefinition() throws JsonProcessingException {
		return new Tool.Builder()
				.name("get-search-type-definition")
				.description("Gets the definition of a FHIR search type.")
				.inputSchema(mapper.readValue(GET_SEARCH_TYPE_DEFINITION_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool getSearchParameters() throws JsonProcessingException {
		return new Tool.Builder()
				.name("get-search-parameters")
				.description("Get the FHIR Search Parameters for a resource in a specified store.")
				.inputSchema(mapper.readValue(GET_SEARCH_PARAMETERS_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool validateTypeSearch() throws JsonProcessingException {
		return new Tool.Builder()
				.name("validate-type-search")
				.description("Validate a FHIR search request against a resource type in the store.")
				.inputSchema(mapper.readValue(VALIDATE_TYPE_SEARCH_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}
}
