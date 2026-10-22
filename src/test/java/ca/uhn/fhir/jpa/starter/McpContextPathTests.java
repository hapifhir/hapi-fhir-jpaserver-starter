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
import org.junit.jupiter.api.Test;
import org.opencds.cqf.fhir.cr.hapi.config.RepositoryConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MCP tools must keep working when the server runs under a servlet context path
 * ({@code server.servlet.context-path}). Previously the MCP bridge built its in-memory
 * request without the context path, so RestfulServer cut that many characters off the
 * resource type ("/eps" turned "Patient" into "ient", HAPI-0302).
 */
@ActiveProfiles("test")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {Application.class, NicknameServiceConfig.class, RepositoryConfig.class},
		properties = {
			"spring.datasource.url=jdbc:h2:mem:dbr4mcpctx",
			"hapi.fhir.fhir_version=r4",
			"hibernate.search.enabled=true",
			"spring.ai.mcp.server.enabled=true",
			"server.servlet.context-path=/eps",
		})
class McpContextPathTests {

	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4Cached();

	@LocalServerPort
	private int port;

	private McpSyncClient client;

	@BeforeEach
	void connect() {
		var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port)
				.endpoint("/eps/mcp/messages")
				.build();
		client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).build();
		client.initialize();
	}

	@AfterEach
	void disconnect() {
		client.closeGracefully();
	}

	@Test
	void crudAndSearchWorkUnderContextPath() throws JsonProcessingException {
		var create = client.callTool(new McpSchema.CallToolRequest.Builder()
				.name(ToolFactory.createFhirResource().name())
				.arguments(Map.of(
						"resourceType", "Patient",
						"resource", "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Silva\",\"given\":[\"Ana\"]}]}"))
				.build());
		assertThat(create.isError()).as(text(create)).isFalse();

		client.callTool(new McpSchema.CallToolRequest.Builder()
				.name(ToolFactory.createFhirResource().name())
				.arguments(Map.of(
						"resourceType", "Patient",
						"resource", "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Costa\"}]}"))
				.build());

		var search = client.callTool(new McpSchema.CallToolRequest.Builder()
				.name(ToolFactory.searchFhirResources().name())
				.arguments(Map.of("resourceType", "Patient", "query", Map.of("family", "Silva")))
				.build());
		assertThat(search.isError()).as(text(search)).isFalse();
		var entries = BundleUtil.toListOfResourcesOfType(FHIR_CONTEXT, responseAs(search, Bundle.class), Patient.class);
		assertThat(entries).hasSize(1);
		var id = entries.get(0).getIdElement().getIdPart();

		var read = client.callTool(new McpSchema.CallToolRequest.Builder()
				.name(ToolFactory.readFhirResource().name())
				.arguments(Map.of("resourceType", "Patient", "id", id))
				.build());
		assertThat(read.isError()).as(text(read)).isFalse();
		assertThat(responseAs(read, Patient.class).getNameFirstRep().getFamily()).isEqualTo("Silva");
	}

	@Test
	void searchQueryStringIsAppliedUnderContextPath() throws JsonProcessingException {
		for (var family : new String[] {"Pereira", "Santos"}) {
			client.callTool(new McpSchema.CallToolRequest.Builder()
					.name(ToolFactory.createFhirResource().name())
					.arguments(Map.of(
							"resourceType", "Patient",
							"resource", "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"" + family + "\"}]}"))
					.build());
		}

		var search = client.callTool(new McpSchema.CallToolRequest.Builder()
				.name(ToolFactory.searchFhirResources().name())
				.arguments(Map.of("resourceType", "Patient", "query", "family=Pereira"))
				.build());
		assertThat(search.isError()).as(text(search)).isFalse();
		var patients = BundleUtil.toListOfResourcesOfType(FHIR_CONTEXT, responseAs(search, Bundle.class), Patient.class);
		assertThat(patients).hasSize(1);
		assertThat(patients.get(0).getNameFirstRep().getFamily()).isEqualTo("Pereira");
	}

	private static String text(McpSchema.CallToolResult result) {
		return ((McpSchema.TextContent) result.content().get(0)).text();
	}

	private static <T extends IBaseResource> T responseAs(McpSchema.CallToolResult result, Class<T> type) {
		var response = new Gson().fromJson(text(result), LinkedHashMap.class).get("response");
		return FHIR_CONTEXT.newJsonParser().parseResource(type, response.toString());
	}
}
