package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.rest.server.MCPBridge;
import ca.uhn.fhir.rest.server.RestfulServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

// https://mcp-cn.ssshooter.com/sdk/java/mcp-server#sse-servlet
// https://www.baeldung.com/spring-ai-model-context-protocol-mcp
// https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/weather/manual-webflux-server/src/main/java/org/springframework/ai/mcp/sample/server/McpServerConfig.java
// https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-stdio-server/src/main/java/org/springframework/ai/mcp/sample/server
// https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/sampling/mcp-weather-webmvc-server/src/main/java/org/springframework/ai/mcp/sample/server/WeatherService.java

@Configuration
@ConditionalOnProperty(
		prefix = "spring.ai.mcp.server",
		name = {"enabled"},
		havingValue = "true")
public class McpServerConfig {

	@Bean
	public MCPBridge mcpBridge(RestfulServer restfulServer, CallToolResultFactory callToolResultFactory) {
		return new MCPBridge(restfulServer, callToolResultFactory);
	}

	@Bean
	public McpSyncServer syncServer(
			MCPBridge mcpBridge, HttpServletStreamableServerTransportProvider transportProvider) {
		return McpServer.sync(transportProvider)
				.tools(mcpBridge.generateTools())
				.build();
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
