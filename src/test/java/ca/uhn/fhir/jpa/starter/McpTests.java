package ca.uhn.fhir.jpa.starter;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

public class McpTests {

	@Test
	@Disabled
	public void mcpTests() {

		// Configure sampling handler
		Function<McpSchema.CreateMessageRequest, McpSchema.CreateMessageResult> samplingHandler = request -> {
			// Sampling implementation that interfaces with LLM
			//return new McpSchema.CreateMessageResult(response);
			return null;
		};

		HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder("http://localhost:8080/sse").build();
		// Create a sync client with custom configuration
		McpSchema.Role response = null;
		McpSyncClient client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).capabilities(McpSchema.ClientCapabilities.builder().roots(true)      // Enable roots capability
			.sampling()       // Enable sampling capability

			.build())
			//.sampling(samplingHandler)
			.build();

// Initialize connection
		client.initialize();

// List available tools
		McpSchema.ListToolsResult tools = client.listTools();

// Call a tool
		McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest("calculator", Map.of("operation", "add", "a", 2, "b", 3)));

// List and read resources
		McpSchema.ListResourcesResult resources = client.listResources();
		McpSchema.ReadResourceResult resource = client.readResource(new McpSchema.ReadResourceRequest("resource://uri"));

// List and use prompts
		McpSchema.ListPromptsResult prompts = client.listPrompts();
		McpSchema.GetPromptResult prompt = client.getPrompt(new McpSchema.GetPromptRequest("greeting", Map.of("name", "Spring")));

// Add/remove roots
		//client.addRoot(new McpSchema.Root("file:///path", "description"));
		//client.removeRoot("file:///path");

		//client.callTool()
// Close client
		client.closeGracefully();
	}
}
