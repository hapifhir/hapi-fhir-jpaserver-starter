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


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, NicknameServiceConfig.class, RepositoryConfig.class}, properties = {"spring.datasource.url=jdbc:h2:mem:dbr4", "hapi.fhir.fhir_version=r4", "hibernate.search.enabled=true", "spring.ai.mcp.server.enabled=true",})
public class McpTests {

	@LocalServerPort
	private int port;
	private FhirContext ourCtx;
	private IGenericClient ourClient;
	private io.modelcontextprotocol.client.McpSyncClient mcpClient;

	@BeforeEach
	void beforeEach() {
		ourCtx = FhirContext.forR4();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		ourClient = ourCtx.newRestfulGenericClient("http://localhost:" + port + "/fhir/");

		var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port).endpoint("/mcp/messages").build();
		mcpClient = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).capabilities(McpSchema.ClientCapabilities.builder().roots(true).sampling().build()).build();
		mcpClient.initialize();
	}

	@AfterEach
	void afterEach() {
		if (mcpClient != null) {
			mcpClient.closeGracefully();
		}
	}

	@Test
	public void mcpTests() throws JsonProcessingException {
		var tools = mcpClient.listTools().tools();
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

		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(searchToolName))).isTrue();
		assertThat(tools.stream().anyMatch(tool -> tool.name().equals(createToolName))).isTrue();
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
		assertThat(mcpClient.callTool(createMcpRequest).isError()).isFalse();

		var searchMcpRequest = new McpSchema.CallToolRequest.Builder().arguments(Map.of("operation", "search", "resourceType", "Patient", "query", "identifier=urn:something|uncleScrooge")).name(searchToolName).build();
		var searchResult = mcpClient.callTool(searchMcpRequest);
		assertThat(searchResult.isError()).isFalse();
		assertThat(searchResult.content().size()).isEqualTo(1);

		var content = ((McpSchema.TextContent) searchResult.content().get(0));
		var embeddedResponseBundle = new Gson().fromJson(content.text(), LinkedHashMap.class).get("response");
		var responseBundle = ourCtx.newJsonParser().parseResource(Bundle.class, embeddedResponseBundle.toString());
		var entries = BundleUtil.toListOfEntries(ourCtx, responseBundle);
		assertThat(entries.size()).isEqualTo(1);

		var storeListResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder().name(getStoreListName).build());
		assertThat(storeListResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) storeListResult.content().get(0)).text()).contains("default");

		var resourceListResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getResourceListName)
			.build());
		assertThat(resourceListResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) resourceListResult.content().get(0)).text()).contains("Patient");

		var resourceDefinitionResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getResourceDefinitionName)
			.arguments(Map.of("resourceType", "Patient"))
			.build());
		assertThat(resourceDefinitionResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) resourceDefinitionResult.content().get(0)).text()).contains("Patient");

		var dataTypeListResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getDataTypeListName)
			.build());
		assertThat(dataTypeListResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) dataTypeListResult.content().get(0)).text()).contains("HumanName");

		var dataTypeDefinitionResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getDataTypeDefinitionName)
			.arguments(Map.of("datatypeName", "HumanName"))
			.build());
		assertThat(dataTypeDefinitionResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) dataTypeDefinitionResult.content().get(0)).text()).contains("HumanName");

		var searchTypeListResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getSearchTypeListName)
			.build());
		assertThat(searchTypeListResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) searchTypeListResult.content().get(0)).text()).contains("token");

		var searchTypeDefinitionResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getSearchTypeDefinitionName)
			.arguments(Map.of("searchType", "token"))
			.build());
		assertThat(searchTypeDefinitionResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) searchTypeDefinitionResult.content().get(0)).text()).contains("token");

		createSearchParameterOnServer();
		var searchParametersResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(getSearchParametersName)
			.arguments(Map.of("resourceType", "DocumentReference"))
			.build());
		assertThat(searchParametersResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) searchParametersResult.content().get(0)).text()).contains("identifier");

		var validateTypeSearchResult = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(validateTypeSearchName)
			.arguments(Map.of("resourceType", "DocumentReference", "searchString", "identifier=urn:something|uncleScrooge&_id=example"))
			.build());
		assertThat(validateTypeSearchResult.isError()).isFalse();
		assertThat(((McpSchema.TextContent) validateTypeSearchResult.content().get(0)).text()).contains("All");

		assertThat(ToolFactory.getStoreList().inputSchema()).isNotNull();
		assertThat(ToolFactory.getSearchTypeList().inputSchema()).isNotNull();
	}

	// -- get-resource-definition error paths --

	@Test
	public void testGetResourceDefinition_missingResourceType() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.getResourceDefinition().name())
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("missing");
	}

	@Test
	public void testGetResourceDefinition_unsupportedResource() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.getResourceDefinition().name())
			.arguments(Map.of("resourceType", "BogusResource"))
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("did not resolve");
	}

	// -- get-data-type-definition error paths --

	@Test
	public void testGetDataTypeDefinition_missingDatatypeName() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.getDataTypeDefinition().name())
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("missing");
	}

	@Test
	public void testGetDataTypeDefinition_unknownType() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.getDataTypeDefinition().name())
			.arguments(Map.of("datatypeName", "BogusDataType"))
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("did not resolve");
	}

	// -- get-search-type-definition error paths --

	@Test
	public void testGetSearchTypeDefinition_missingSearchType() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.getSearchTypeDefinition().name())
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("missing");
	}

	@Test
	public void testGetSearchTypeDefinition_unknownType() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.getSearchTypeDefinition().name())
			.arguments(Map.of("searchType", "bogus"))
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("did not resolve");
	}

	// -- get-search-parameters error paths --

	@Test
	public void testGetSearchParameters_missingResourceType() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.getSearchParameters().name())
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("missing");
	}

	@Test
	public void testGetSearchParameters_unsupportedResource() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.getSearchParameters().name())
			.arguments(Map.of("resourceType", "BogusResource"))
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("did not resolve");
	}

	// -- validate-type-search error and edge-case paths --

	@Test
	public void testValidateTypeSearch_missingResourceType() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.validateTypeSearch().name())
			.arguments(Map.of("searchString", "name=test"))
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("Resource name is missing");
	}

	@Test
	public void testValidateTypeSearch_missingSearchString() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.validateTypeSearch().name())
			.arguments(Map.of("resourceType", "Patient"))
			.build());
		assertThat(result.isError()).isTrue();
		assertThat(textContent(result)).contains("Search string is missing");
	}

	@Test
	public void testValidateTypeSearch_invalidParams() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.validateTypeSearch().name())
			.arguments(Map.of("resourceType", "Patient", "searchString", "name=test&bogusParam=value"))
			.build());
		assertThat(result.isError()).isTrue();
		var text = textContent(result);
		assertThat(text).contains("1 of 2 search parameters are invalid");
		assertThat(text).contains("bogusParam");
		assertThat(text).contains("Unknown parameter");
	}

	@Test
	public void testValidateTypeSearch_withModifier() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.validateTypeSearch().name())
			.arguments(Map.of("resourceType", "Patient", "searchString", "name:exact=Smith"))
			.build());
		assertThat(result.isError()).isFalse();
		assertThat(textContent(result)).contains("All 1 search parameters are valid");
	}

	@Test
	public void testValidateTypeSearch_withGeneralParams() throws JsonProcessingException {
		var result = mcpClient.callTool(new McpSchema.CallToolRequest.Builder()
			.name(ToolFactory.validateTypeSearch().name())
			.arguments(Map.of("resourceType", "Patient", "searchString", "_count=10&_sort=name&_include:iterate=Patient:organization"))
			.build());
		assertThat(result.isError()).isFalse();
		var text = textContent(result);
		assertThat(text).contains("All 3 search parameters are valid");
		assertThat(text).contains("server-wide parameter");
	}

	private static String textContent(McpSchema.CallToolResult result) {
		return ((McpSchema.TextContent) result.content().get(0)).text();
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
