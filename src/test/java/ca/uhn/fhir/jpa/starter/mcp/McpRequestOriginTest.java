package ca.uhn.fhir.jpa.starter.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class McpRequestOriginTest {

	@Test
	void takesSchemeHostPortAndTheProxyHeadersForThem() {
		var request = new MockHttpServletRequest("POST", "/mcp/messages");
		request.setScheme("http");
		request.setServerName("backend");
		request.setServerPort(8080);
		request.addHeader("X-Forwarded-Host", "fhir.example.org");
		request.addHeader("X-Forwarded-Ssl", "on");
		request.addHeader("X-Forwarded-Prefix", "/ai");
		request.addHeader("Authorization", "Bearer secret");

		var origin = McpRequestOrigin.from(request);

		assertThat(origin.scheme()).isEqualTo("http");
		assertThat(origin.serverName()).isEqualTo("backend");
		assertThat(origin.serverPort()).isEqualTo(8080);
		// X-Forwarded-Prefix describes the MCP route at the proxy, not the FHIR base, so it is not passed on
		assertThat(origin.forwardedHeaders())
				.containsOnlyKeys("X-Forwarded-Host", "X-Forwarded-Ssl")
				.containsEntry("X-Forwarded-Host", "fhir.example.org")
				.containsEntry("X-Forwarded-Ssl", "on");
	}

	@Test
	void nullHeadersMeanNoProxyHeaders() {
		assertThat(new McpRequestOrigin("http", "localhost", 8080, null).forwardedHeaders())
				.isEmpty();
	}
}
