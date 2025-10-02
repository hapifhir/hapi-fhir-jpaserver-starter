package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.McpBridge;
import ca.uhn.fhir.rest.server.McpCdsBridge;
import ca.uhn.fhir.rest.server.McpFhirBridge;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsServiceRegistry;
import ca.uhn.hapi.fhir.cdshooks.module.CdsHooksObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
@Import(McpServerStreamableHttpProperties.class)
public class McpServerConfig {

	private static final String SSE_ENDPOINT = "/sse";
	private static final String SSE_MESSAGE_ENDPOINT = "/mcp/message";

	@Bean
	public List<McpServerFeatures.SyncToolSpecification> syncServer(List<McpBridge> mcpBridges) {
		return mcpBridges.stream()
				.flatMap(bridge -> bridge.generateTools().stream())
				.toList();
	}

	@Bean
	public McpFhirBridge mcpFhirBridge(RestfulServer restfulServer) {
		return new McpFhirBridge(restfulServer);
	}

	@Bean
	@ConditionalOnProperty(
			prefix = "hapi.fhir.cdshooks",
			name = {"enabled"},
			havingValue = "true")
	public McpCdsBridge mcpCdsBridge(FhirContext fhirContext, ICdsServiceRegistry cdsServiceRegistry) {

		return new McpCdsBridge(
				fhirContext, cdsServiceRegistry, new CdsHooksObjectMapperFactory(fhirContext).newMapper());
	}

	@Bean
	public HttpServletStreamableServerTransportProvider servletSseServerTransportProvider(
			McpServerStreamableHttpProperties properties) {

		return HttpServletStreamableServerTransportProvider.builder()
				.disallowDelete(false)
				.mcpEndpoint(properties.getMcpEndpoint())
				.jsonMapper(new JacksonMcpJsonMapper(new ObjectMapper()))
				// .contextExtractor((serverRequest, context) -> context)
				.build();
	}

	@Bean
	public ServletRegistrationBean customServletBean(
			HttpServletStreamableServerTransportProvider transportProvider,
			McpServerStreamableHttpProperties properties) {
		return new ServletRegistrationBean<>(transportProvider, properties.getMcpEndpoint(), SSE_ENDPOINT);
	}
}
