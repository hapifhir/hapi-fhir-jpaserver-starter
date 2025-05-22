package ch.ahdis.matchbox.config;

import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import ch.ahdis.matchbox.validation.McpValidationService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "spring.ai.mcp.server",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = true
)
public class MatchboxMcpConfig {

    @Bean(name = "validationToolCallbackProvider")
    public ToolCallbackProvider validationToolCallbackProvider(final McpValidationService mcpValidationService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpValidationService)
                .build();
    }

    @Bean
    public McpValidationService mcpValidationService(final MatchboxEngineSupport matchboxEngineSupport,
                                                     final CliContext cliContext) {
        return new McpValidationService(matchboxEngineSupport, cliContext);
    }
}
