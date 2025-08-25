package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.rest.server.MCPBridge;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsServiceRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.opencds.cqf.fhir.cr.hapi.config.test.TestCdsHooksConfig.CDS_HOOKS_OBJECT_MAPPER_FACTORY;

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
			RestfulServer restfulServer,
			CallToolResultFactory callToolResultFactory,
			HttpServletStreamableServerTransportProvider transportProvider,
			ICdsServiceRegistry cdsServiceRegistry,
			@Qualifier(CDS_HOOKS_OBJECT_MAPPER_FACTORY) ObjectMapper objectMapper) {
		return McpServer.sync(transportProvider)
				.tools(new MCPBridge(restfulServer, callToolResultFactory, cdsServiceRegistry, objectMapper)
						.generateTools())
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
