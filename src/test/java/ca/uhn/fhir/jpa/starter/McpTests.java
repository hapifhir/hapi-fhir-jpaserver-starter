package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.util.BundleUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.fhir.cr.hapi.config.RepositoryConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, NicknameServiceConfig.class, RepositoryConfig.class}, properties = {"spring.datasource.url=jdbc:h2:mem:dbr4", "hapi.fhir.fhir_version=r4", "hibernate.search.enabled=true", "spring.ai.mcp.server.enabled=true",})
public class McpTests {

	@LocalServerPort
	private int port;

	@Test
	public void mcpTests() throws JsonProcessingException {

		var fhirContext = FhirContext.forR4();

		var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port).endpoint("/mcp/messages").build();
		var client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).capabilities(McpSchema.ClientCapabilities.builder().roots(true)      // Enable roots capability
			.sampling().build()).build();
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
		assertThat(client.callTool(createMcpRequest).isError()).isFalse();

		var searchMcpRequest = new McpSchema.CallToolRequest.Builder().arguments(Map.of("operation", "search", "resourceType", "Patient", "query", "identifier=urn:something|uncleScrooge")).name(searchToolName).build();

		var searchResult = client.callTool(searchMcpRequest);
		assertThat(searchResult.isError()).isFalse();
		assertThat(searchResult.content().size()).isEqualTo(1);

		var content = ((McpSchema.TextContent) searchResult.content().get(0));
		var embeddedResponseBundle = new Gson().fromJson(content.text(), LinkedHashMap.class).get("response");
		var responseBundle = fhirContext.newJsonParser().parseResource(Bundle.class, embeddedResponseBundle.toString());
		var entries = BundleUtil.toListOfEntries(fhirContext, responseBundle);
		assertThat(entries.size()).isEqualTo(1);

		client.closeGracefully();
	}
}
