package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.util.BundleUtil;
import com.google.gson.Gson;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.fhir.cr.hapi.config.RepositoryConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, NicknameServiceConfig.class, RepositoryConfig.class}, properties = {"spring.datasource.url=jdbc:h2:mem:dbr4", "hapi.fhir.fhir_version=r4", "hibernate.search.enabled=true", "spring.ai.mcp.server.enabled=true",
	// Searches must not be answered from the search cache, otherwise an unfiltered search can reuse an earlier result
	"hapi.fhir.reuse_cached_search_results_millis=0",})
public class McpIT {

	@LocalServerPort
	private int port;

	@Test
	public void mcpTests() {

		var fhirContext = FhirContext.forR4();

		var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port).endpoint("/mcp/messages").build();
		var client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).capabilities(McpSchema.ClientCapabilities.builder().roots(true)      // Enable roots capability
			.sampling().build()).build();
		try {
			var initializationResult = client.initialize();

			var tools = client.listTools().tools();
			assertThat(tools).isNotEmpty();

			var searchToolName = ToolFactory.searchFhirResources().name();
			var createToolName = ToolFactory.createFhirResource().name();

			assertThat(tools.stream().filter(tool -> tool.name().equals(searchToolName)).findFirst().get()).isNotNull();
			assertThat(tools.stream().filter(tool -> tool.name().equals(createToolName)).findFirst().get()).isNotNull();


			var createMcpRequest = new McpSchema.CallToolRequest.Builder().arguments(Map.of("operation", "create", "resourceType", "Patient", "resource", """
				{
				  "resourceType": "Patient",
				  "id": "example",
				  "identifier": [
				    {
				      "system": "urn:something",
				      "value": "uncleScrooge"
				    }
				  ]
				}""")).name(createToolName).build();
			var createResult = client.callTool(createMcpRequest);
			var patientId = response(fhirContext, createResult).getIdElement().getIdPart();

			// The URLs in MCP responses must be the ones a REST client gets: same port and servlet path
			var fhirBase = "http://localhost:" + port + Application.FHIR_SERVLET_PATH;
			assertThat(payload(createResult).get("location")).isEqualTo(fhirBase + "/Patient/" + patientId + "/_history/1");

			var searchMcpRequest = new McpSchema.CallToolRequest.Builder().arguments(Map.of("operation", "search", "resourceType", "Patient", "query", "identifier=urn:something|uncleScrooge")).name(searchToolName).build();

			var searchResult = client.callTool(searchMcpRequest);
			assertThat(searchResult.isError()).isFalse();
			assertThat(searchResult.content().size()).isEqualTo(1);

			var content = ((McpSchema.TextContent) searchResult.content().get(0));
			var embeddedResponseBundle = new Gson().fromJson(content.text(), LinkedHashMap.class).get("response");
			var responseBundle = fhirContext.newJsonParser().parseResource(Bundle.class, embeddedResponseBundle.toString());
			var entries = BundleUtil.toListOfEntries(fhirContext, responseBundle);
			assertThat(entries.size()).isEqualTo(1);

			// With a second, non-matching Patient, an unfiltered search would return both
			var createOtherMcpRequest = new McpSchema.CallToolRequest.Builder().arguments(Map.of("resourceType", "Patient", "resource", """
				{
				  "resourceType": "Patient",
				  "identifier": [
				    {
				      "system": "urn:something",
				      "value": "uncleDonald"
				    }
				  ]
				}""")).name(createToolName).build();
			assertThat(client.callTool(createOtherMcpRequest).isError()).isFalse();

			var filteredSearchMcpRequest = new McpSchema.CallToolRequest.Builder().arguments(Map.of("resourceType", "Patient", "query", Map.of("identifier", "urn:something|uncleScrooge"))).name(searchToolName).build();
			var filteredSearchResult = client.callTool(filteredSearchMcpRequest);
			assertThat(filteredSearchResult.isError()).isFalse();
			var filteredContent = ((McpSchema.TextContent) filteredSearchResult.content().get(0));
			var filteredBundle = fhirContext.newJsonParser().parseResource(Bundle.class, new Gson().fromJson(filteredContent.text(), LinkedHashMap.class).get("response").toString());
			assertThat(BundleUtil.toListOfEntries(fhirContext, filteredBundle)).hasSize(1);

			// The same search through the REST API returns the same entry URLs
			var restBundle = fhirContext.newRestfulGenericClient(fhirBase).search().forResource(Patient.class)
				.where(Patient.IDENTIFIER.exactly().systemAndCode("urn:something", "uncleScrooge"))
				.returnBundle(Bundle.class).execute();
			assertThat(filteredBundle.getEntry()).extracting(Bundle.BundleEntryComponent::getFullUrl)
				.containsExactlyElementsOf(restBundle.getEntry().stream().map(Bundle.BundleEntryComponent::getFullUrl).toList())
				.containsExactly(fhirBase + "/Patient/" + patientId);
			assertThat(filteredBundle.getLink(Bundle.LINK_SELF).getUrl()).startsWith(fhirBase + "/Patient?");

			var read = call(ToolFactory.readFhirResource().name(), Map.of("resourceType", "Patient", "id", patientId));
			assertThat(response(fhirContext, client.callTool(read)).getIdElement().getIdPart()).isEqualTo(patientId);

			// Update with the resource as an object
			var update = call(ToolFactory.updateFhirResource().name(), Map.of("resourceType", "Patient", "id", patientId, "resource",
				Map.of("resourceType", "Patient", "id", patientId, "name", List.of(Map.of("family", "Duck")))));
			assertThat(((Patient) response(fhirContext, client.callTool(update))).getNameFirstRep().getFamily()).isEqualTo("Duck");

			// Patch with the FHIR Patch body as an object
			var operation = List.of(
				Map.of("name", "type", "valueCode", "replace"),
				Map.of("name", "path", "valueString", "Patient.name[0].family"),
				Map.of("name", "value", "valueString", "McDuck"));
			var patch = call(ToolFactory.patchFhirResource().name(), Map.of("resourceType", "Patient", "id", patientId, "resource",
				Map.of("resourceType", "Parameters", "parameter", List.of(Map.of("name", "operation", "part", operation)))));
			assertThat(((Patient) response(fhirContext, client.callTool(patch))).getNameFirstRep().getFamily()).isEqualTo("McDuck");

			// Patch with a JSON Patch array
			var jsonPatch = call(ToolFactory.patchFhirResource().name(), Map.of("resourceType", "Patient", "id", patientId, "resource",
				List.of(Map.of("op", "add", "path", "/active", "value", true))));
			assertThat(((Patient) response(fhirContext, client.callTool(jsonPatch))).getActive()).isTrue();

			// A version-aware update fails if the resource has another version
			var staleUpdate = call(ToolFactory.updateFhirResource().name(), Map.of("resourceType", "Patient", "id", patientId,
				"resource", Map.of("resourceType", "Patient", "id", patientId), "headers", Map.of("If-Match", "1")));
			var staleUpdateResult = client.callTool(staleUpdate);
			assertThat(staleUpdateResult.isError()).as(staleUpdateResult.toString()).isTrue();

			// A conditional create finds the existing Patient instead of creating another one
			var conditionalCreate = call(createToolName, Map.of("resourceType", "Patient",
				"resource", Map.of("resourceType", "Patient", "name", List.of(Map.of("family", "McDuck"))),
				"headers", Map.of("If-None-Exist", "family=McDuck")));
			var conditionalCreateResult = client.callTool(conditionalCreate);
			assertThat(conditionalCreateResult.isError()).as(conditionalCreateResult.toString()).isFalse();
			var conditionalCreatePayload = new Gson().fromJson(((McpSchema.TextContent) conditionalCreateResult.content().get(0)).text(), LinkedHashMap.class);
			assertThat(conditionalCreatePayload.get("status")).isEqualTo(200.0);
			assertThat(conditionalCreatePayload.get("location").toString()).startsWith(fhirBase + "/Patient/" + patientId + "/_history/");

			var transaction = call(ToolFactory.createFhirTransaction().name(), Map.of("resourceType", "Bundle", "resource",
				Map.of("resourceType", "Bundle", "type", "transaction", "entry", List.of(Map.of(
					"resource", Map.of("resourceType", "Patient"),
					"request", Map.of("method", "POST", "url", "Patient"))))));
			assertThat(((Bundle) response(fhirContext, client.callTool(transaction))).getType())
				.isEqualTo(Bundle.BundleType.TRANSACTIONRESPONSE);

			// Decimals in a resource passed as an object keep their precision
			var observation = call(createToolName, Map.of("resourceType", "Observation", "resource", Map.of(
				"resourceType", "Observation", "status", "final", "code", Map.of("text", "weight"),
				"valueQuantity", Map.of("value", new BigDecimal("1.50"), "unit", "kg"))));
			var createdObservation = (Observation) response(fhirContext, client.callTool(observation));
			assertThat(createdObservation.getValueQuantity().getValueElement().getValueAsString()).isEqualTo("1.50");
			// Passed as a JSON string, the resource reaches HAPI exactly as written, whatever the client does with numbers
			var observationAsString = call(createToolName, Map.of("resourceType", "Observation", "resource",
				"{\"resourceType\":\"Observation\",\"status\":\"final\",\"code\":{\"text\":\"weight\"},\"valueQuantity\":{\"value\":1.50,\"unit\":\"kg\"}}"));
			var createdFromString = (Observation) response(fhirContext, client.callTool(observationAsString));
			assertThat(createdFromString.getValueQuantity().getValueElement().getValueAsString()).isEqualTo("1.50");

			var delete = call(ToolFactory.deleteFhirResource().name(), Map.of("resourceType", "Patient", "id", patientId));
			var deleteResult = client.callTool(delete);
			assertThat(deleteResult.isError()).as(deleteResult.toString()).isFalse();
			// The deleted Patient is gone
			var readDeleted = client.callTool(read);
			assertThat(readDeleted.isError()).isTrue();
			assertThat(((McpSchema.TextContent) readDeleted.content().get(0)).text()).contains("410");
		} finally {
			client.closeGracefully();
		}
	}

	private static McpSchema.CallToolRequest call(String theToolName, Map<String, Object> theArguments) {
		return new McpSchema.CallToolRequest.Builder().name(theToolName).arguments(theArguments).build();
	}

	/**
	 * @return the JSON payload of a successful tool result: resourceType, interaction, response, status and location
	 */
	private static Map<?, ?> payload(McpSchema.CallToolResult theResult) {
		assertThat(theResult.isError()).as(theResult.toString()).isFalse();
		return new Gson().fromJson(((McpSchema.TextContent) theResult.content().get(0)).text(), LinkedHashMap.class);
	}

	/**
	 * @return the FHIR resource that the tool returned in the "response" field of its text content
	 */
	private static IBaseResource response(FhirContext theFhirContext, McpSchema.CallToolResult theResult) {
		assertThat(theResult.isError()).as(theResult.toString()).isFalse();
		var text = ((McpSchema.TextContent) theResult.content().get(0)).text();
		var response = new Gson().fromJson(text, LinkedHashMap.class).get("response").toString();
		return theFhirContext.newJsonParser().parseResource(response);
	}
}
