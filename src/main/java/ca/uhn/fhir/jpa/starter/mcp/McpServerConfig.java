package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.rest.server.MCPBridge;
import ca.uhn.fhir.rest.server.RestfulServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// https://mcp-cn.ssshooter.com/sdk/java/mcp-server#sse-servlet
// https://www.baeldung.com/spring-ai-model-context-protocol-mcp
// https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/weather/manual-webflux-server/src/main/java/org/springframework/ai/mcp/sample/server/McpServerConfig.java
// https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-stdio-server/src/main/java/org/springframework/ai/mcp/sample/server
// https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/sampling/mcp-weather-webmvc-server/src/main/java/org/springframework/ai/mcp/sample/server/WeatherService.java

@Configuration
@EnableWebMvc
public class McpServerConfig implements WebMvcConfigurer {

	@Bean
	public MCPBridge mcpBridge(RestfulServer restfulServer, CallToolResultFactory callToolResultFactory) {
		return new MCPBridge(restfulServer, callToolResultFactory);
	}

	@Bean
	public McpSyncServer syncServer(McpSyncServer mcpSyncServer, MCPBridge mcpBridge) {

		mcpBridge.generateTools().stream().forEach(mcpSyncServer::addTool);
		return mcpSyncServer;
	}

	@Bean
	public HttpServletSseServerTransportProvider servletSseServerTransportProvider() {
		return new HttpServletSseServerTransportProvider(new ObjectMapper(), "/mcp/message");
	}

	@Bean
	public ServletRegistrationBean customServletBean(HttpServletSseServerTransportProvider transportProvider) {
		var servetRegistrationBean = new ServletRegistrationBean<>(transportProvider, "/mcp/message", "/sse");
		return servetRegistrationBean;
		// return new ServletRegistrationBean(transportProvider);
	}
}
