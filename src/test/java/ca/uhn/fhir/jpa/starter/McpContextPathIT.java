package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.util.BundleUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.fhir.cr.hapi.config.RepositoryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MCP tools must keep working when the server runs under a servlet context path
 * ({@code server.servlet.context-path}). Previously the MCP bridge built its in-memory
 * request without the context path, while RestfulServer still subtracted the length of the
 * real one from the request URI: "/r5" turned "Patient" into "tient" (HAPI-0302), and a path
 * longer than the resource type failed outright.
 *
 * <p>The same scenarios run against a short and a nested context path.
 */
@ActiveProfiles("test")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {Application.class, NicknameServiceConfig.class, RepositoryConfig.class},
		properties = {
			"hapi.fhir.fhir_version=r4",
			"hibernate.search.enabled=true",
			"spring.ai.mcp.server.enabled=true",
		})
class McpContextPathIT {

	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4Cached();

	@Nested
	@TestPropertySource(
			properties = {"server.servlet.context-path=/r5", "spring.datasource.url=jdbc:h2:mem:dbr4mcpctxshort"})
	class ShortContextPath extends Scenarios {}

	@Nested
	@TestPropertySource(
			properties = {
				"server.servlet.context-path=/apps/fhir-server",
				"spring.datasource.url=jdbc:h2:mem:dbr4mcpctxnested"
			})
	class NestedContextPath extends Scenarios {}

	abstract class Scenarios {

		@LocalServerPort
		private int port;

		@Value("${server.servlet.context-path}")
		private String contextPath;

		private McpSyncClient client;

		@BeforeEach
		void connect() {
			var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port)
					.endpoint(contextPath + "/mcp/messages")
					.build();
			client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).build();
			client.initialize();
		}

		@AfterEach
		void disconnect() {
			client.closeGracefully();
		}

		@Test
		void createSearchAndReadWithPlainResourceType() throws JsonProcessingException {
			var create = createPatient("Silva");
			assertThat(create.isError()).as(text(create)).isFalse();
			createPatient("Costa");

			var search = callTool(
					ToolFactory.searchFhirResources().name(),
					Map.of("resourceType", "Patient", "query", Map.of("family", "Silva")));
			assertThat(search.isError()).as(text(search)).isFalse();
			var bundle = responseAs(search, Bundle.class);
			var patients = BundleUtil.toListOfResourcesOfType(FHIR_CONTEXT, bundle, Patient.class);
			assertThat(patients).hasSize(1);

			// The URLs contain the context path and the FHIR servlet path, as for a direct REST request
			var id = patients.get(0).getIdElement().getIdPart();
			var fhirBase = "http://localhost:" + port + contextPath + Application.FHIR_SERVLET_PATH;
			assertThat(bundle.getEntryFirstRep().getFullUrl()).isEqualTo(fhirBase + "/Patient/" + id);
			assertThat(bundle.getLink(Bundle.LINK_SELF).getUrl()).startsWith(fhirBase + "/Patient?");
			var read = callTool(ToolFactory.readFhirResource().name(), Map.of("resourceType", "Patient", "id", id));
			assertThat(read.isError()).as(text(read)).isFalse();
			assertThat(responseAs(read, Patient.class).getNameFirstRep().getFamily())
					.isEqualTo("Silva");
		}

		@Test
		void searchQueryStringIsApplied() throws JsonProcessingException {
			createPatient("Pereira");
			createPatient("Santos");

			var search = callTool(
					ToolFactory.searchFhirResources().name(),
					Map.of("resourceType", "Patient", "query", "family=Pereira"));
			assertThat(search.isError()).as(text(search)).isFalse();
			var patients =
					BundleUtil.toListOfResourcesOfType(FHIR_CONTEXT, responseAs(search, Bundle.class), Patient.class);
			assertThat(patients).hasSize(1);
			assertThat(patients.get(0).getNameFirstRep().getFamily()).isEqualTo("Pereira");
		}

		@Test
		void conditionalUpdateUpdatesTheMatchingResource() throws JsonProcessingException {
			createPatient("Oliveira");
			var id = searchPatients("family=Oliveira").get(0).getIdElement().getIdPart();

			var update = callTool(
					ToolFactory.conditionalUpdateFhirResource().name(),
					Map.of(
							"resourceType",
							"Patient",
							"query",
							"family=Oliveira",
							"resource",
							"{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Oliveira\",\"given\":[\"Ana\"]}]}"));
			assertThat(update.isError()).as(text(update)).isFalse();

			var read = callTool(ToolFactory.readFhirResource().name(), Map.of("resourceType", "Patient", "id", id));
			var patient = responseAs(read, Patient.class);
			assertThat(patient.getNameFirstRep().getGivenAsSingleString()).isEqualTo("Ana");
			assertThat(patient.getMeta().getVersionId()).isEqualTo("2");
		}

		@Test
		void conditionalPatchPatchesTheMatchingResource() throws JsonProcessingException {
			createPatient("Ferreira");
			// FHIRPath Patch, passed as a JSON object as the tool schema declares it
			var fhirPathPatch = new Gson()
					.fromJson(
							"""
							{"resourceType": "Parameters", "parameter": [{"name": "operation", "part": [
							  {"name": "type", "valueCode": "add"},
							  {"name": "path", "valueString": "Patient"},
							  {"name": "name", "valueString": "birthDate"},
							  {"name": "value", "valueDate": "1970-01-01"}]}]}
							""",
							Map.class);

			var patch = callTool(
					ToolFactory.conditionalPatchFhirResource().name(),
					Map.of("resourceType", "Patient", "query", "family=Ferreira", "resource", fhirPathPatch));
			assertThat(patch.isError()).as(text(patch)).isFalse();

			var patients = searchPatients("family=Ferreira");
			assertThat(patients).hasSize(1);
			assertThat(patients.get(0).getBirthDateElement().getValueAsString()).isEqualTo("1970-01-01");
		}

		@Test
		void plainUpdateUsesTheIdAndIgnoresAQuery() throws JsonProcessingException {
			createPatient("Lima");
			createPatient("Rocha");
			var lima = searchPatients("family=Lima").get(0).getIdElement().getIdPart();
			var rocha = searchPatients("family=Rocha").get(0).getIdElement().getIdPart();

			var update = callTool(
					ToolFactory.updateFhirResource().name(),
					Map.of(
							"resourceType",
							"Patient",
							"id",
							lima,
							"query",
							"family=Rocha",
							"resource",
							"{\"resourceType\":\"Patient\",\"id\":\"" + lima
									+ "\",\"name\":[{\"family\":\"Lima\",\"given\":[\"Rui\"]}]}"));
			assertThat(update.isError()).as(text(update)).isFalse();

			assertThat(readPatient(lima).getNameFirstRep().getGivenAsSingleString()).isEqualTo("Rui");
			assertThat(readPatient(rocha).getMeta().getVersionId()).isEqualTo("1");
		}

		@Test
		void conditionalUpdateCreatesTheResourceWhenNothingMatches() throws JsonProcessingException {
			var update = callTool(
					ToolFactory.conditionalUpdateFhirResource().name(),
					Map.of(
							"resourceType",
							"Patient",
							"query",
							"family=Almeida",
							"resource",
							Map.of("resourceType", "Patient", "name", List.of(Map.of("family", "Almeida")))));
			assertThat(update.isError()).as(text(update)).isFalse();
			assertThat(payload(update).get("status")).isEqualTo(201.0);

			assertThat(searchPatients("family=Almeida")).hasSize(1);
		}

		@Test
		void ifMatchUpdateFailsForAnotherVersionAndSucceedsForTheCurrentOne() throws JsonProcessingException {
			createPatient("Carvalho");
			var id = searchPatients("family=Carvalho").get(0).getIdElement().getIdPart();
			// Each update changes the content, as HAPI does not create a new version for an unchanged resource
			var update = callTool(
					ToolFactory.updateFhirResource().name(),
					Map.of("resourceType", "Patient", "id", id, "resource", carvalho(id, "Ana")));
			assertThat(update.isError()).as(text(update)).isFalse();

			var staleUpdate = callTool(
					ToolFactory.updateFhirResource().name(),
					Map.of(
							"resourceType",
							"Patient",
							"id",
							id,
							"resource",
							carvalho(id, "Rui"),
							"headers",
							Map.of("If-Match", "1")));
			assertThat(staleUpdate.isError()).isTrue();
			assertThat(text(staleUpdate)).contains("FHIR server error 409");
			assertThat(readPatient(id).getMeta().getVersionId()).isEqualTo("2");

			var currentUpdate = callTool(
					ToolFactory.updateFhirResource().name(),
					Map.of(
							"resourceType",
							"Patient",
							"id",
							id,
							"resource",
							carvalho(id, "Rui"),
							"headers",
							Map.of("If-Match", "2")));
			assertThat(currentUpdate.isError()).as(text(currentUpdate)).isFalse();
			var patient = readPatient(id);
			assertThat(patient.getMeta().getVersionId()).isEqualTo("3");
			assertThat(patient.getNameFirstRep().getGivenAsSingleString()).isEqualTo("Rui");
		}

		private Map<String, Object> carvalho(String id, String given) {
			return Map.of(
					"resourceType",
					"Patient",
					"id",
					id,
					"name",
					List.of(Map.of("family", "Carvalho", "given", List.of(given))));
		}

		@Test
		void conditionalCreateReturnsTheExistingResourceWithoutABody() throws JsonProcessingException {
			createPatient("Gomes");
			var id = searchPatients("family=Gomes").get(0).getIdElement().getIdPart();

			var create = callTool(
					ToolFactory.createFhirResource().name(),
					Map.of(
							"resourceType",
							"Patient",
							"resource",
							Map.of("resourceType", "Patient", "name", List.of(Map.of("family", "Gomes"))),
							"headers",
							Map.of("If-None-Exist", "family=Gomes")));
			assertThat(create.isError()).as(text(create)).isFalse();
			var payload = payload(create);
			assertThat(payload.get("status")).isEqualTo(200.0);
			assertThat((String) payload.get("response")).isEmpty();
			assertThat(payload.get("location").toString()).contains("/Patient/" + id + "/_history/");

			assertThat(searchPatients("family=Gomes")).hasSize(1);
		}

		@Test
		void headersOutsideTheAllowListAreReportedAsToolErrors()  {
			var create = callTool(
					ToolFactory.createFhirResource().name(),
					Map.of(
							"resourceType",
							"Patient",
							"resource",
							Map.of("resourceType", "Patient", "name", List.of(Map.of("family", "Ribeiro"))),
							"headers",
							Map.of("Host", "example.org")));

			assertThat(create.isError()).isTrue();
			assertThat(text(create))
					.isEqualTo("Invalid arguments: Unsupported header 'Host' for create, allowed: [If-None-Exist]");
			assertThat(searchPatients("family=Ribeiro")).isEmpty();
		}

		@Test
		void objectValuesInASearchQueryAreReportedAsToolErrors() {
			var search = callTool(
					ToolFactory.searchFhirResources().name(),
					Map.of(
							"resourceType",
							"Patient",
							"query",
							Map.of("identifier", Map.of("system", "urn:something"))));

			assertThat(search.isError()).isTrue();
			assertThat(text(search)).startsWith("Invalid arguments").contains("'identifier'");
		}

		@Test
		void invalidArgumentsAreReportedAsToolErrors()  {
			var read = callTool(ToolFactory.readFhirResource().name(), Map.of("resourceType", "Patient"));

			assertThat(read.isError()).isTrue();
			assertThat(text(read)).contains("Invalid arguments").contains("'id'");

			var readWithEmptyId =
					callTool(ToolFactory.readFhirResource().name(), Map.of("resourceType", "Patient", "id", ""));

			assertThat(readWithEmptyId.isError()).isTrue();
			assertThat(text(readWithEmptyId)).contains("Invalid arguments").contains("'id'");
		}

		private Patient readPatient(String id)  {
			var read = callTool(ToolFactory.readFhirResource().name(), Map.of("resourceType", "Patient", "id", id));
			assertThat(read.isError()).as(text(read)).isFalse();
			return responseAs(read, Patient.class);
		}

		private List<Patient> searchPatients(String query)  {
			var search = callTool(
					ToolFactory.searchFhirResources().name(), Map.of("resourceType", "Patient", "query", query));
			assertThat(search.isError()).as(text(search)).isFalse();
			return BundleUtil.toListOfResourcesOfType(FHIR_CONTEXT, responseAs(search, Bundle.class), Patient.class);
		}

		private McpSchema.CallToolResult createPatient(String family) throws JsonProcessingException {
			return callTool(
					ToolFactory.createFhirResource().name(),
					Map.of(
							"resourceType",
							"Patient",
							"resource",
							"{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"" + family + "\"}]}"));
		}

		private McpSchema.CallToolResult callTool(String name, Map<String, Object> arguments) {
			return client.callTool(new McpSchema.CallToolRequest.Builder()
					.name(name)
					.arguments(arguments)
					.build());
		}
	}

	private static String text(McpSchema.CallToolResult result) {
		return ((McpSchema.TextContent) result.content().get(0)).text();
	}

	/**
	 * @return the JSON payload of a successful tool result: resourceType, interaction, response, status and location
	 */
	private static Map<?, ?> payload(McpSchema.CallToolResult result) {
		return new Gson().fromJson(text(result), LinkedHashMap.class);
	}

	private static <T extends IBaseResource> T responseAs(McpSchema.CallToolResult result, Class<T> type) {
		var response = new Gson().fromJson(text(result), LinkedHashMap.class).get("response");
		return FHIR_CONTEXT.newJsonParser().parseResource(type, response.toString());
	}
}
