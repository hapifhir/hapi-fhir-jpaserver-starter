package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.SyncToolSpecGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;


//https://mcp-cn.ssshooter.com/sdk/java/mcp-server#sse-servlet
//https://www.baeldung.com/spring-ai-model-context-protocol-mcp
//https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/weather/manual-webflux-server/src/main/java/org/springframework/ai/mcp/sample/server/McpServerConfig.java
//https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-stdio-server/src/main/java/org/springframework/ai/mcp/sample/server
//https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/sampling/mcp-weather-webmvc-server/src/main/java/org/springframework/ai/mcp/sample/server/WeatherService.java

@Configuration
@EnableWebMvc
public class McpServerConfig implements WebMvcConfigurer {

    @Bean
    public SyncToolSpecGenerator syncToolSpecGenerator(RestfulServer restfulServer, FhirContext fhirContext, Optional<IncrementalSyncStrategy> incrementalSyncStrategy) {
        return new SyncToolSpecGenerator(restfulServer, fhirContext, incrementalSyncStrategy);
    }

    @Bean
    public McpSyncServer syncServer(McpSyncServer mcpSyncServer, SyncToolSpecGenerator syncToolSpecGenerator) {


        syncToolSpecGenerator.getSyncTools().stream().forEach(mcpSyncServer::addTool);
        return mcpSyncServer;
    }

    /*@Bean
	@Primary
    public McpSyncServer mcpSyncServer(McpServerTransportProvider transportProvider, SyncToolSpecGenerator syncToolSpecGenerator) { // @formatter:off


		var tools = syncToolSpecGenerator.getSyncTools();
		// Create the server with both tool and resource capabilities
		McpSyncServer server = McpServer.sync(transportProvider)
			.serverInfo("MCP Demo FHIR Server", "1.0.0")
			.capabilities(McpSchema.ServerCapabilities.builder()
				.tools(true)
				.prompts(true)
				.logging()
				.build())
			.tools(tools)
			.instructions("This is a demo server for the Model Context Protocol (MCP).")
			.build();


		// Send logging notifications
		server.loggingNotification(McpSchema.LoggingMessageNotification.builder()
			.level(McpSchema.LoggingLevel.INFO)
			.logger("custom-logger")
			.data("Server initialized")
			.build());

		return server;
	}*/

	@Bean
	public HttpServletSseServerTransportProvider servletSseServerTransportProvider() {
		return new HttpServletSseServerTransportProvider(new ObjectMapper(), "/mcp/message");
	}

	@Bean
	public ServletRegistrationBean customServletBean(HttpServletSseServerTransportProvider transportProvider) {
		var servetRegistrationBean = new ServletRegistrationBean<>(transportProvider, "/mcp/message" ,"/sse");
		return servetRegistrationBean;
		//return new ServletRegistrationBean(transportProvider);
	}
}
