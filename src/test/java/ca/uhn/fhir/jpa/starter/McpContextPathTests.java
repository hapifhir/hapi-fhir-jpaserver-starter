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
class McpContextPathTests {

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
			var patients =
					BundleUtil.toListOfResourcesOfType(FHIR_CONTEXT, responseAs(search, Bundle.class), Patient.class);
			assertThat(patients).hasSize(1);

			var id = patients.get(0).getIdElement().getIdPart();
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

	private static <T extends IBaseResource> T responseAs(McpSchema.CallToolResult result, Class<T> type) {
		var response = new Gson().fromJson(text(result), LinkedHashMap.class).get("response");
		return FHIR_CONTEXT.newJsonParser().parseResource(type, response.toString());
	}
}
