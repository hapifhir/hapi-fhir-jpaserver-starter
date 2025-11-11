package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.util.BundleUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.SearchParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.fhir.cr.hapi.config.RepositoryConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, NicknameServiceConfig.class, RepositoryConfig.class}, properties = {"spring.datasource.url=jdbc:h2:mem:dbr4", "hapi.fhir.fhir_version=r4", "hibernate.search.enabled=true", "spring.ai.mcp.server.enabled=true",})
public class McpTests {

	@LocalServerPort
	private int port;
	private FhirContext ourCtx;
	private IGenericClient ourClient;

	@BeforeEach
	void beforeEach() {

		ourCtx = FhirContext.forR4();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		ourClient = ourCtx.newRestfulGenericClient("http://localhost:" + port + "/fhir/");
	}

	@Test
	public void mcpTests() throws JsonProcessingException {


		var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port).endpoint("/mcp/messages").build();
		var client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).capabilities(McpSchema.ClientCapabilities.builder().roots(true)      // Enable roots capability
			.sampling().build()).build();
		var initializationResult = client.initialize();

		var tools = client.listTools().tools();
		assertThat(tools).isNotEmpty();

		var searchToolName = ToolFactory.searchFhirResources().name();
		var createToolName = ToolFactory.createFhirResource().name();
		var getStoreListName = ToolFactory.getStoreList().name();
		var getResourceListName = ToolFactory.getResourceList().name();
		var getResourceDefinitionName = ToolFactory.getResourceDefinition().name();
		var getDataTypeListName = ToolFactory.getDataTypeList().name();
		var getDataTypeDefinitionName = ToolFactory.getDataTypeDefinition().name();
		var getSearchTypeListName = ToolFactory.getSearchTypeList().name();
		var getSearchTypeDefinitionName = ToolFactory.getSearchTypeDefinition().name();
		var getSearchParametersName = ToolFactory.getSearchParameters().name();
		var validateTypeSearchName = ToolFactory.validateTypeSearch().name();

	assertThat(tools.stream().filter(tool -> tool.name().equals(searchToolName)).findFirst().get()).isNotNull();
	assertThat(tools.stream().filter(tool -> tool.name().equals(createToolName)).findFirst().get()).isNotNull();
	assertThat(tools.stream().anyMatch(tool -> tool.name().equals(getStoreListName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(getResourceListName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(getResourceDefinitionName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(getDataTypeListName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(getDataTypeDefinitionName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(getSearchTypeListName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(getSearchTypeDefinitionName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(getSearchParametersName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(validateTypeSearchName))).isTrue();


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
		var responseBundle = ourCtx.newJsonParser().parseResource(Bundle.class, embeddedResponseBundle.toString());
		var entries = BundleUtil.toListOfEntries(ourCtx, responseBundle);
		assertThat(entries.size()).isEqualTo(1);

		var storeListResult = client.callTool(new McpSchema.CallToolRequest.Builder().name(getStoreListName).build());
		assertThat(storeListResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) storeListResult.content().get(0)).text()).contains("default");

		var resourceListResult = client.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getResourceListName)
			.arguments(Map.of("store", "default"))
			.build());
		assertThat(resourceListResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) resourceListResult.content().get(0)).text()).contains("Patient");

		var resourceDefinitionResult = client.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getResourceDefinitionName)
			.arguments(Map.of("resourceType", "Patient"))
			.build());
		assertThat(resourceDefinitionResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) resourceDefinitionResult.content().get(0)).text()).contains("Patient");

		var dataTypeListResult = client.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getDataTypeListName)
			.arguments(Map.of("store", "default"))
			.build());
		assertThat(dataTypeListResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) dataTypeListResult.content().get(0)).text()).contains("HumanName");

		var dataTypeDefinitionResult = client.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getDataTypeDefinitionName)
			.arguments(Map.of("datatypeName", "HumanName"))
			.build());
		assertThat(dataTypeDefinitionResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) dataTypeDefinitionResult.content().get(0)).text()).contains("HumanName");


		var searchTypeListResult = client.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getSearchTypeListName)
			.build());
		assertThat(searchTypeListResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) searchTypeListResult.content().get(0)).text()).contains("token");

		var searchTypeDefinitionResult = client.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getSearchTypeDefinitionName)
			.arguments(Map.of("searchType", "token"))
			.build());
		assertThat(searchTypeDefinitionResult.isError()).isFalse();
	assertThat(((McpSchema.TextContent) searchTypeDefinitionResult.content().get(0)).text()).contains("token");

		createSearchParameterOnServer();
	var searchParametersResult = client.callTool(new McpSchema.CallToolRequest.Builder()
		.name(getSearchParametersName)
			.arguments(Map.of("store", "default", "resourceType", "DocumentReference"))
			.build());
		assertThat(searchParametersResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) searchParametersResult.content().get(0)).text()).contains("identifier");

		var validateTypeSearchResult = client.callTool(new McpSchema.CallToolRequest.Builder()
			.name(validateTypeSearchName)
			.arguments(Map.of("store", "default", "resourceType", "DocumentReference", "searchString", "identifier=urn:something|uncleScrooge&_id=example"))
			.build());
		assertThat(validateTypeSearchResult.isError()).isFalse();
	assertThat(((McpSchema.TextContent) validateTypeSearchResult.content().get(0)).text()).contains("All");

	assertThat(ToolFactory.getStoreList().inputSchema()).isNotNull();
	assertThat(ToolFactory.getSearchTypeList().inputSchema()).isNotNull();

	client.closeGracefully();
}

	private void createSearchParameterOnServer() {
		String searchParameterText = """
			{
			  "resourceType": "SearchParameter",
			  "id": "binary-reference",
			  "url": "http://xyz.ai/SearchParameter/binary-reference",
			  "base": [
			    "DocumentReference"
			  ],
			  "version": "1.0.0",
			  "name": "binary-reference",
			  "status": "active",
			  "publisher": "XYZ",
			  "description": "A search parameter to reference the Binary in a DocumentReference",
			  "code": "binary-reference",
			  "type": "reference",
			  "expression": "DocumentReference.content.attachment.extension.where(url='http://xyz.ai/StructureDefinition/binary-reference').value",
			  "xpathUsage": "normal"
			}
			
			""";
		var sp = ourCtx.newJsonParser().parseResource(SearchParameter.class, searchParameterText);
		ourClient.create().resource(sp).execute();
	}
}
