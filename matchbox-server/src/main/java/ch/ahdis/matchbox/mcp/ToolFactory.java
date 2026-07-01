package ch.ahdis.matchbox.mcp;

import io.modelcontextprotocol.spec.McpSchema.Tool;

import java.util.List;
import java.util.Map;

public class ToolFactory {

	public static Tool validateFhirResource() {
		return Tool.builder(
				"validate-fhir-resource",
				Map.of(
					"type", "object",
					"properties", Map.of(
						"resource", Map.of(
							"type", "string",
							"description", "The FHIR resource or logical model to validate in XML or JSON format"
						),
						"profile", Map.of(
							"type", "string",
							"description", "The FHIR profile to validate against"
						),
						"validationparams", Map.of(
							"type",
							"string",
							"description",
							"Additional validation parameters separated by ',' (comma). For example: 'txServer=http://tx.fhir.org,txUseEcosystem=false'."
						)
					),
					"required", List.of("resource", "profile")
				))
			.description("Validate a FHIR resource or logical model against a profile and return a FHIR OperationOutcome " +
								 "indicating the result of the validation")
			.build();
	}

	public static Tool listFhirImplementationGuides() {
		return Tool.builder(
				"list-fhir-igs",
				Map.of(
					"type", "object",
					"properties", Map.of(
						"includeVersions", Map.of(
							"type", "boolean",
							"description", "include older versions of the installed FHIR Implementation Guides, defaults to false"
						)
					)
				)
			)
			.description("List FHIR Implementation Guides available for validation")
			.build();
	}

	public static Tool listFhirProfilesToValidateFor() {
		return Tool.builder(
				"list-fhir-profiles-to-validate-for",
				Map.of(
					"type", "object",
					"properties", Map.of(
						"ig", Map.of(
							"type",
							"string",
							"description",
							"package id to list profiles for, if not provided all profiles for the latest ig versions are listed"
						),
						"resourceType", Map.of(
							"type",
							"string",
							"description",
							"FHIR resource type list profiles for, if not provided all profiles for all resources and logical models are listed"
						)
					)
				)
			)
			.description("List FHIR Profiles available for validation")
			.build();
	}

	public static Tool listValidationParameters() {
		return Tool.builder(
				"list-validation-parameters",
				Map.of(
					"type", "object",
					"properties", Map.of()
				)
			)
			.description("List additional available parameters for validation")
			.build();
	}
}
