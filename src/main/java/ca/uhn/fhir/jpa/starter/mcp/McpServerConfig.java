package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.McpBridge;
import ca.uhn.fhir.rest.server.McpCdsBridge;
import ca.uhn.fhir.rest.server.McpFhirBridge;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsServiceRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// https://mcp-cn.ssshooter.com/sdk/java/mcp-server#sse-servlet
// https://www.baeldung.com/spring-ai-model-context-protocol-mcp
// https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/weather/manual-webflux-server/src/main/java/org/springframework/ai/mcp/sample/server/McpServerConfig.java
// https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-stdio-server/src/main/java/org/springframework/ai/mcp/sample/server
// https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/sampling/mcp-weather-webmvc-server/src/main/java/org/springframework/ai/mcp/sample/server/WeatherService.java
// https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html
@Configuration
@ConditionalOnProperty(
		prefix = "spring.ai.mcp.server",
		name = {"enabled"},
		havingValue = "true")
public class McpServerConfig {

	@Bean
	public McpSyncServer syncServer(
			List<McpBridge> mcpBridges, HttpServletStreamableServerTransportProvider transportProvider) {
		return McpServer.sync(transportProvider)
				.tools(mcpBridges.stream()
						.flatMap(bridge -> bridge.generateTools().stream())
						.toList())
				.build();
	}

	@Bean
	public McpFhirBridge mcpFhirBridge(RestfulServer restfulServer) {
		return new McpFhirBridge(restfulServer);
	}

	@Bean
	@ConditionalOnProperty(
			prefix = "hapi.fhir.cr",
			name = {"enabled"},
			havingValue = "true")
	public McpCdsBridge mcpCdsBridge(FhirContext fhirContext, ICdsServiceRegistry cdsServiceRegistry) {
		return new McpCdsBridge(fhirContext, cdsServiceRegistry);
	}

	@Bean
	public HttpServletStreamableServerTransportProvider servletSseServerTransportProvider() {
		return HttpServletStreamableServerTransportProvider.builder()
				.disallowDelete(false)
				.mcpEndpoint("/mcp/message")
				.objectMapper(new ObjectMapper())
				.contextExtractor((serverRequest, context) -> context)
				.build();
	}

	@Bean
	public ServletRegistrationBean customServletBean(HttpServletStreamableServerTransportProvider transportProvider) {
		return new ServletRegistrationBean<>(transportProvider, "/mcp/message", "/sse");
	}
}
