package ch.ahdis.matchbox.config;

import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import ch.ahdis.matchbox.validation.McpValidationService;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;

import java.util.List;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.rest.server.RestfulServer;

@Configuration
@ConditionalOnProperty(
        prefix = "spring.ai.mcp.server",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = false
)
public class MatchboxMcpConfig {

    private static final String SSE_ENDPOINT = "/sse";
	private static final String SSE_MESSAGE_ENDPOINT = "/mcp/message";

    // 	@Bean
	// public McpSyncServer syncServer(
	// 		List<McpBridge> mcpBridges, McpStreamableServerTransportProvider transportProvider) {
	// 	return McpServer.sync(transportProvider)
	// 			.tools(mcpBridges.stream()
	// 					.flatMap(bridge -> bridge.generateTools().stream())
	// 					.toList())
	// 			.build();
	// }

    @Bean
    public McpSyncServer syncServer(
			McpStreamableServerTransportProvider transportProvider, final MatchboxEngineSupport matchboxEngineSupport,
                                                     final CliContext cliContext) {
        ToolCallback[] toolCallbacks = ToolCallbacks.from(new McpValidationService(matchboxEngineSupport, cliContext));
        List<SyncToolSpecification> tools = McpToolUtils.toSyncToolSpecifications(toolCallbacks);
		return McpServer.sync(transportProvider).tools(tools).build();
	}

    // @Bean(name = "validationToolCallbackProvider")
    // public ToolCallbackProvider validationToolCallbackProvider(final McpValidationService mcpValidationService) {
    //     return MethodToolCallbackProvider.builder()
    //             .toolObjects(mcpValidationService)
    //             .build();
    // }

    // @Bean
    // public McpValidationService mcpValidationService(final MatchboxEngineSupport matchboxEngineSupport,
    //                                                  final CliContext cliContext) {
    //     return new McpValidationService(matchboxEngineSupport, cliContext);
    // }

    @Bean
	public HttpServletStreamableServerTransportProvider servletSseServerTransportProvider(
			/*McpServerProperties properties*/ ) {

		return HttpServletStreamableServerTransportProvider.builder()
				.disallowDelete(false)
				.mcpEndpoint(SSE_MESSAGE_ENDPOINT)
				.objectMapper(new ObjectMapper())
				// .contextExtractor((serverRequest, context) -> context)
				.build();
	}

	@Bean
	public ServletRegistrationBean customServletBean(
			HttpServletStreamableServerTransportProvider transportProvider /*, McpServerProperties properties*/) {
		return new ServletRegistrationBean<>(transportProvider, SSE_MESSAGE_ENDPOINT, SSE_ENDPOINT);
	}


}
