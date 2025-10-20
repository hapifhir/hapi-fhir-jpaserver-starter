package ch.ahdis.matchbox.mcp;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ToolFactory {

	private static final String VALIDATE_FHIR_RESOURCE_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"resource": {
			"type": "string",
			"description": "The FHIR resource or logical model to validate in XML or JSON format"
			},
			"profile": {
			"type": "string",
			"description": "The FHIR profile to validate against"
			},
			"validationparams": {
			"type": "string",
			"description": "Additional validation parameters separated by \\",\\". For example: \\"txServer=http://tx.fhir.org,txUseEcosystem=false\\"."
			}
		},
		"required": ["resource", "profile"]
		}
		""";

	private static final String LIST_FHIR_IGS_SCHEMA =
			"""
		{
		"type": "object",
		"properties": {
			"includeVersions": {
				"type": "boolean",
				"description": "include older versions of the installed FHIR Implementation Guides, defaults to false"
				}
			}
		}
		""";

	private static final String LIST_PROFILES_SCHEMA =
		"""
		{
		"type": "object",
		"properties": {
			"ig": {
				"type": "string",
				"description": "package id to list profiles for, if not provided all profiles for the latest ig versions are listed"
			},
			"resourceType": {
				"type": "string",
				"description": "FHIR resource type list profiles for, if not provided all profiles for all resources and logical models are listed"
			}
		}
	}
		""";

	private static final String LIST_VALIDATIONPARAMETERS_SCHEMA =
		"""
		{
		"type": "object",
		"properties": {
			}
		}
		""";

	private static final String LIST_FHIR_IGS_OUTPUT_SCHEMA =
		"""
	{
	"$schema": "http://json-schema.org/draft-07/schema#",
	"title": "FHIR Bundle Resource",
	"type": "object",
	"required": [
		"resourceType",
		"type"
	],
	"properties": {
		"resourceType": {
		"type": "string",
		"const": "Bundle"
		},
		"id": {
		"type": "string",
		"description": "Logical id of this artifact"
		},
		"type": {
		"type": "string",
		"enum": [
			"searchset"
		]
		},
		"timestamp": {
		"type": "string",
		"format": "date-time",
		"description": "When the bundle was assembled"
		},
		"total": {
		"type": "integer",
		"minimum": 0,
		"description": "If search, the total number of matches"
		}
	},
	"additionalProperties": true
	}
	""";

	public static Tool validateFhirResource() throws JsonProcessingException {
		return new Tool.Builder()
				.name("validate-fhir-resource")
				.description("Validate a FHIR resource or logical model against a profile and return a FHIR OperationOutcome indicating the result of the validation")
				.inputSchema(mapper.readValue(VALIDATE_FHIR_RESOURCE_SCHEMA, McpSchema.JsonSchema.class))
				.build();
	}

	public static Tool listFhirImplementationGuides() throws JsonProcessingException {
		return new Tool.Builder()
				.name("list-fhir-igs")
				.description("List FHIR Implementation Guides available for validation")
				.inputSchema(mapper.readValue(LIST_FHIR_IGS_SCHEMA, McpSchema.JsonSchema.class))
//				.outputSchema(LIST_FHIR_IGS_OUTPUT_SCHEMA)
				.build();
	}

	public static Tool listFhirProfilesToValidateFor() throws JsonProcessingException {
		return new Tool.Builder()
				.name("list-fhir-profiles-to-validate-for")
				.description("List FHIR Profiles available for validation")
				.inputSchema(mapper.readValue(LIST_PROFILES_SCHEMA, McpSchema.JsonSchema.class))
//				.outputSchema(LIST_FHIR_IGS_OUTPUT_SCHEMA)
				.build();
	}

	public static Tool listValidationParameters() throws JsonProcessingException {
		return new Tool.Builder()
				.name("list-validation-parameters")
				.description("List additional available parameters for validation")
				.inputSchema(mapper.readValue(LIST_VALIDATIONPARAMETERS_SCHEMA, McpSchema.JsonSchema.class))
//				.outputSchema(LIST_FHIR_IGS_OUTPUT_SCHEMA)
				.build();
	}


	public static final ObjectMapper mapper = new ObjectMapper()
			.enable(JsonParser.Feature.ALLOW_COMMENTS)
			.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
			.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
			.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
