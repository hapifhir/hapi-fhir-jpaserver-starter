package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.searchparam.config.NicknameServiceConfig;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import com.google.gson.Gson;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opencds.cqf.fhir.cr.hapi.config.RepositoryConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behind a proxy, with HAPI's ApacheProxyAddressStrategy, the URLs in MCP tool results must point to the same public
 * FHIR base as those of a direct REST request with the same proxy headers.
 */
@ActiveProfiles("test")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {Application.class, NicknameServiceConfig.class, RepositoryConfig.class},
		properties = {
			"spring.datasource.url=jdbc:h2:mem:dbr4mcpproxy",
			"hapi.fhir.fhir_version=r4",
			"spring.ai.mcp.server.enabled=true",
			"hapi.fhir.use_apache_address_strategy=true"
		})
class McpProxyIT {

	@LocalServerPort
	private int port;

	static Stream<Arguments> proxyHeaders() {
		return Stream.of(
				// The proxy names the public host and protocol
				Arguments.of(
						Map.of("X-Forwarded-Host", "fhir.example.org", "X-Forwarded-Proto", "https", "X-Forwarded-Port", "443"),
						"https://fhir.example.org/fhir"),
				// The proxy keeps the Host header and only signals TLS
				Arguments.of(Map.of("X-Forwarded-Ssl", "on"), "https://localhost"));
	}

	@ParameterizedTest
	@MethodSource("proxyHeaders")
	void mcpReturnsTheSameBaseUrlAsRest(Map<String, String> theProxyHeaders, String theExpectedBaseStart)
			throws Exception {
		var restRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/fhir/Patient"))
				.header("Content-Type", "application/fhir+json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"resourceType\":\"Patient\"}"));
		theProxyHeaders.forEach(restRequest::header);
		var restResponse = HttpClient.newHttpClient().send(restRequest.build(), HttpResponse.BodyHandlers.ofString());
		assertThat(restResponse.statusCode()).as(restResponse.body()).isEqualTo(201);
		var restLocation = restResponse.headers().firstValue("Location").orElseThrow();

		var mcpRequest = HttpRequest.newBuilder();
		theProxyHeaders.forEach(mcpRequest::header);
		var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port)
				.endpoint("/mcp/messages")
				.requestBuilder(mcpRequest)
				.build();
		var client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).build();
		try {
			client.initialize();
			var result = client.callTool(new McpSchema.CallToolRequest.Builder()
					.name(ToolFactory.createFhirResource().name())
					.arguments(Map.of("resourceType", "Patient", "resource", "{\"resourceType\":\"Patient\"}"))
					.build());
			assertThat(result.isError()).as(result.toString()).isFalse();
			var text = ((McpSchema.TextContent) result.content().get(0)).text();
			var mcpLocation =
					new Gson().fromJson(text, LinkedHashMap.class).get("location").toString();

			assertThat(base(restLocation)).startsWith(theExpectedBaseStart);
			assertThat(base(mcpLocation)).isEqualTo(base(restLocation));
		} finally {
			client.closeGracefully();
		}
	}

	/**
	 * @return the FHIR base of a Location such as https://host/fhir/Patient/1/_history/1
	 */
	private static String base(String theLocation) {
		return theLocation.substring(0, theLocation.indexOf("/Patient/"));
	}
}
