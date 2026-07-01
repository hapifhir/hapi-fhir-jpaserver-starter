package ca.uhn.fhir.jpa.starter.mcp;

import io.modelcontextprotocol.spec.McpSchema.Tool;

import java.util.List;
import java.util.Map;

public class ToolFactory {

  public static Tool readFhirResource() {
    return Tool.builder(
        "read-fhir-resource",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type", "string",
              "description", "type of the FHIR conformance resource to read"
            ),
            "id", Map.of(
              "type", "string",
              "description", "id of the resource to read"
            )
          )
        )
      )
      .description("Read an individual FHIR resource")
      .build();
  }

  public static Tool createFhirResource() {
    return Tool.builder(
        "create-fhir-resource",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type", "string",
              "description", "Type of the FHIR conformance resource to create"
            ),
            "resource", Map.of(
              "type", "object",
              "description", "Resource content in JSON format"
            ),
            "headers", Map.of(
              "type",
              "object",
              "description",
              "Headers for create request.\nAvailable headers: If-None-Exist header for conditional create where the value is search param string.\nFor example: {\"If-None-Exist\": \"active=false\"}"
            )
          ),
          "required", List.of("resourceType", "resource")
        )
      )
      .description("Create a new FHIR resource")
      .build();
  }

  public static Tool updateFhirResource() {
    return Tool.builder(
        "update-fhir-resource",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type", "string",
              "description", "Type of the FHIR conformance resource to update"
            ),
            "id", Map.of(
              "type", "string",
              "description", "ID of the resource to update"
            ),
            "resource", Map.of(
              "type", "object",
              "description", "Updated resource content in JSON format"
            )
          ),
          "required", List.of("resourceType", "id", "resource")
        )
      )
      .description("Update an existing FHIR resource")
      .build();
  }

  public static Tool conditionalUpdateFhirResource() {
    return Tool.builder(
        "conditional-update-fhir-resource",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type", "string",
              "description", "Type of the FHIR conformance resource to update"
            ),
            "resource", Map.of(
              "type", "object",
              "description", "Updated resource content in JSON format"
            ),
            "query", Map.of(
              "type",
              "string",
              "description",
              "Query string with search params separate by \",\". For example: \"_id=pt-1,name=ivan\". Uses for conditional update."
            ),
            "headers", Map.of(
              "type",
              "object",
              "description",
              "Headers for create request.\nAvailable headers: If-None-Match header for conditional update where the value is ETag.\nFor example: {\"If-None-Match\": \"12345\"}"
            )
          ),
          "required", List.of("resourceType", "resource")
        )
      )
      .description("Conditional update an existing FHIR resource")
      .build();
  }

  public static Tool conditionalPatchFhirResource() {
    return Tool.builder(
        "conditional-patch-fhir-resource",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type", "string",
              "description", "Type of the FHIR conformance resource to patch"
            ),
            "resource", Map.of(
              "type", "object",
              "description", "Resource content to patch in JSON format"
            ),
            "query", Map.of(
              "type",
              "string",
              "description",
              "Query string with search params separate by \",\". For example: \"_id=pt-1,name=ivan\". Uses for conditional patch."
            ),
            "headers", Map.of(
              "type",
              "object",
              "description",
              "Headers for create request.\nAvailable headers: If-None-Match header for conditional patch where the value is ETag.\nFor example: {\"If-None-Match\": \"12345\"}"
            )
          ),
          "required", List.of("resourceType", "resource")
        )
      )
      .description("Conditional patch an existing FHIR resource")
      .build();
  }

  public static Tool patchFhirResource() {
    return Tool.builder(
        "patch-fhir-resource",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type", "string",
              "description", "Type of the FHIR conformance resource to patch"
            ),
            "id", Map.of(
              "type", "string",
              "description", "ID of the FHIR resource to patch"
            ),
            "resource", Map.of(
              "type", "object",
              "description", "Resource content to patch in JSON format"
            )
          ),
          "required", List.of("resourceType", "id", "resource")
        )
      )
      .description("Patch an existing FHIR resource")
      .build();
  }

  public static Tool deleteFhirResource() {
    return Tool.builder(
        "delete-fhir-resource",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type", "string",
              "description", "Type of the FHIR conformance resource to delete"
            ),
            "id", Map.of(
              "type", "string",
              "description", "ID of the resource to delete"
            )
          ),
          "required", List.of("resourceType", "id")
        )
      )
      .description("Delete an existing FHIR resource")
      .build();
  }

  public static Tool searchFhirResources() {
    return Tool.builder(
        "search-fhir-resources",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type", "string",
              "description", "Type of the FHIR conformance resource to search"
            ),
            "query", Map.of(
              "type", "string",
              "description", "Query string with search params separate by \",\". For example: \"_id=pt-1,name=ivan\""
            )
          ),
          "required", List.of("resourceType", "query")
        )
      )
      .description("Search an existing FHIR resources")
      .build();
  }

  public static Tool createFhirTransaction() {
    return Tool.builder(
        "create-fhir-transaction",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "resourceType", Map.of(
              "type",
              "string",
              "description",
              "A Bundle resource type with type 'transaction' containing multiple FHIR conformance resources"
            ),
            "resource", Map.of(
              "type", "object",
              "description", "A FHIR Bundle Resource content in JSON format"
            )
          ),
          "required", List.of("resourceType", "resource")
        )
      )
      .description("Create a FHIR transaction")
      .build();
  }

  // TODO Add a tool for the CDS Hooks discovery endpoint
  // Alternatively, should each service be a separate tool?

  // TODO Add other fields from https://cds-hooks.hl7.org/STU2/#http-request-1
  // TODO Context here is for the patient-view hook, https://cds-hooks.hl7.org/hooks/STU1/patient-view.html#context

  public static Tool callCdsHook() {
    return Tool.builder(
        "call-cds-hook",
        Map.of(
          "type", "object",
          "properties", Map.of(
            "service", Map.of(
              "type", "string",
              "description", "The CDS Service to call."
            ),
            "hook", Map.of(
              "type", "string",
              "description", "The hook that triggered this CDS Service call."
            ),
            "hookInstance", Map.of(
              "type", "string",
              "description", "A universally unique identifier (UUID) for this particular hook call."
            ),
            "hookContext", Map.of(
              "type", "object",
              "description", "Hook-specific contextual data that the CDS service will need.",
              "properties", Map.of(
                "userId", Map.of(
                  "type", "string",
                  "description", "The id of the current user. Must be in the format [ResourceType]/[id]."
                ),
                "patientId", Map.of(
                  "type", "string",
                  "description", "The FHIR Patient.id of the current patient in context"
                ),
                "encounterId", Map.of(
                  "type", "string",
                  "description", "The FHIR Encounter.id of the current encounter in context."
                )
              )
            ),
            "prefetch", Map.of(
              "type", "object",
              "description", "Additional data to prefetch for the CDS service call."
            )
          ),
          "required", List.of("service", "hook", "hookInstance", "hookContext")
        )
      )
      .description("Call a CDS Hook")
      .build();
  }
}
