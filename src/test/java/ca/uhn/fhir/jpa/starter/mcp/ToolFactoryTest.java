package ca.uhn.fhir.jpa.starter.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolFactoryTest {

	private static List<McpSchema.Tool> allTools() {
		return List.of(
				ToolFactory.readFhirResource(),
				ToolFactory.createFhirResource(),
				ToolFactory.updateFhirResource(),
				ToolFactory.conditionalUpdateFhirResource(),
				ToolFactory.conditionalPatchFhirResource(),
				ToolFactory.patchFhirResource(),
				ToolFactory.deleteFhirResource(),
				ToolFactory.searchFhirResources(),
				ToolFactory.createFhirTransaction(),
				ToolFactory.callCdsHook());
	}

	/**
	 * An array-valued "type" (e.g. ["object", "string"]) is legal JSON Schema, but clients that map tool schemas onto
	 * a single-type dialect (such as Gemini function declarations) may reject the tool. The MCP Inspector warns about
	 * it ("type-union"). Unions are written as anyOf branches with a single type each.
	 */
	@Test
	void schemasDoNotUseTypeArrays() {
		List<String> typeArrays = new ArrayList<>();
		for (McpSchema.Tool tool : allTools()) {
			collectTypeArrays(tool.inputSchema(), tool.name() + ".inputSchema", typeArrays);
		}
		assertThat(typeArrays).isEmpty();
	}

	private static void collectTypeArrays(Object theNode, String thePath, List<String> theTypeArrays) {
		if (theNode instanceof Map<?, ?> map) {
			if (map.get("type") instanceof List<?>) {
				theTypeArrays.add(thePath);
			}
			map.forEach((key, value) -> collectTypeArrays(value, thePath + "." + key, theTypeArrays));
		} else if (theNode instanceof List<?> list) {
			for (int i = 0; i < list.size(); i++) {
				collectTypeArrays(list.get(i), thePath + "[" + i + "]", theTypeArrays);
			}
		}
	}
}
