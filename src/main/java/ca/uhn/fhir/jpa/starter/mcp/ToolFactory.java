package ca.uhn.fhir.jpa.starter.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

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
			"anyOf": [{"type": "object"}, {"type": "string"}],
			"description": "Resource content in JSON format. To keep decimal values exactly as written (e.g. 1.50), pass it as a JSON string: some clients round numbers in JSON objects."
			},
			"headers": {
			"type": "object",
			"description": "Headers for create request.\\nAvailable headers: If-None-Exist header for conditional create where the value is search param string.\\nFor example: {\\"If-None-Exist\\": \\"active=false\\"}\\nValues are URL-decoded, so write a literal \\"+\\" as \\"%2B\\"."
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
			"anyOf": [{"type": "object"}, {"type": "string"}],
			"description": "Updated resource content in JSON format. To keep decimal values exactly as written (e.g. 1.50), pass it as a JSON string: some clients round numbers in JSON objects."
			},
			"headers": {
			"type": "object",
			"description": "Headers for the request.\\nAvailable headers: If-Match for a version-aware update, where the value is the version the update is based on. It fails if the resource has another version.\\nFor example: {\\"If-Match\\": \\"2\\"}"
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
			"anyOf": [{"type": "object"}, {"type": "string"}],
			"description": "Updated resource content in JSON format. To keep decimal values exactly as written (e.g. 1.50), pass it as a JSON string: some clients round numbers in JSON objects."
			},
			"query": {
			"type": "string",
			"description": "Search params that identify the resource for a conditional update, separated by \\"&\\" as in a FHIR search URL. For example: \\"identifier=http://example.org/mrn|12345\\". Values are URL-decoded, so write a literal \\"+\\" as \\"%2B\\"."
			}
		},
		"required": ["resourceType", "resource", "query"]
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
			"anyOf": [{"type": "object"}, {"type": "array"}, {"type": "string"}],
			"description": "Patch document in JSON format: a FHIR Patch Parameters resource with one \\"operation\\" parameter per change (https://hl7.org/fhir/fhirpatch.html), or a JSON Patch array (RFC 6902). Not the modified resource. To keep decimal values exactly as written (e.g. 1.50), use FHIR Patch and pass it as a JSON string: some clients round numbers in JSON objects, and JSON Patch values are applied as floating point numbers."
			},
			"query": {
			"type": "string",
			"description": "Search params that identify the resource for a conditional patch, separated by \\"&\\" as in a FHIR search URL. For example: \\"identifier=http://example.org/mrn|12345\\". Values are URL-decoded, so write a literal \\"+\\" as \\"%2B\\"."
			}
		},
		"required": ["resourceType", "resource", "query"]
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
			"anyOf": [{"type": "object"}, {"type": "array"}, {"type": "string"}],
			"description": "Patch document in JSON format: a FHIR Patch Parameters resource with one \\"operation\\" parameter per change (https://hl7.org/fhir/fhirpatch.html), or a JSON Patch array (RFC 6902). Not the modified resource. To keep decimal values exactly as written (e.g. 1.50), use FHIR Patch and pass it as a JSON string: some clients round numbers in JSON objects, and JSON Patch values are applied as floating point numbers."
			},
			"headers": {
			"type": "object",
			"description": "Headers for the request.\\nAvailable headers: If-Match for a version-aware patch, where the value is the version the patch is based on. It fails if the resource has another version.\\nFor example: {\\"If-Match\\": \\"2\\"}"
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
			"anyOf": [{"type": "string"}, {"type": "object"}],
			"description": "Query string with search params separated by \\"&\\", as in a FHIR search URL. For example: \\"_id=pt-1&name=ivan\\". A comma inside a value means OR, e.g. \\"name=ivan,peter\\"."
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
			"anyOf": [{"type": "object"}, {"type": "string"}],
			"description": "A FHIR Bundle Resource content in JSON format. To keep decimal values exactly as written (e.g. 1.50), pass it as a JSON string: some clients round numbers in JSON objects."
			}
		},
		"required": ["resourceType", "resource"]
		}
		""";

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

	public static Tool readFhirResource() {
		return new Tool.Builder()
				.name("read-fhir-resource")
				.description("Read an individual FHIR resource")
				.inputSchema(mapper.readValue(READ_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool createFhirResource() {
		return new Tool.Builder()
				.name("create-fhir-resource")
				.description("Create a new FHIR resource")
				.inputSchema(mapper.readValue(CREATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool updateFhirResource() {
		return new Tool.Builder()
				.name("update-fhir-resource")
				.description("Update an existing FHIR resource")
				.inputSchema(mapper.readValue(UPDATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool conditionalUpdateFhirResource() {
		return new Tool.Builder()
				.name("conditional-update-fhir-resource")
				.description(
						"Conditional update of the FHIR resource that matches a search query. If no resource matches, the resource is created.")
				.inputSchema(mapper.readValue(CONDITIONAL_UPDATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool conditionalPatchFhirResource() {
		return new Tool.Builder()
				.name("conditional-patch-fhir-resource")
				.description("Conditional patch an existing FHIR resource")
				.inputSchema(mapper.readValue(CONDITIONAL_PATCH_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool patchFhirResource() {
		return new Tool.Builder()
				.name("patch-fhir-resource")
				.description("Patch an existing FHIR resource")
				.inputSchema(mapper.readValue(PATCH_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool deleteFhirResource() {
		return new Tool.Builder()
				.name("delete-fhir-resource")
				.description("Delete an existing FHIR resource")
				.inputSchema(mapper.readValue(DELETE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool searchFhirResources() {
		return new Tool.Builder()
				.name("search-fhir-resources")
				.description("Search an existing FHIR resources")
				.inputSchema(mapper.readValue(SEARCH_FHIR_RESOURCES_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool createFhirTransaction() {
		return new Tool.Builder()
				.name("create-fhir-transaction")
				.description("Create a FHIR transaction")
				.inputSchema(mapper.readValue(CREATE_FHIR_TRANSACTION_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool callCdsHook() {
		return new Tool.Builder()
				.name("call-cds-hook")
				.description("Call a CDS Hook")
				.inputSchema(mapper.readValue(CALL_CDS_HOOK_SCHEMA_2_0_1, McpSchema.JsonSchema.class))
				.build();
	}

	public static final JsonMapper mapper = JsonMapper.builder()
			.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
			.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
			.enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
			.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build();
}
