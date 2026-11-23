package ca.uhn.fhir.jpa.starter.mcp;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Where an MCP request reached the server: scheme, host, port and the proxy headers. The MCP bridge passes this on to
 * the request it hands to HAPI in-process, so that HAPI builds the same URLs (Location, fullUrl, Bundle links) as for a
 * direct REST request.
 */
public record McpRequestOrigin(String scheme, String serverName, int serverPort, Map<String, String> forwardedHeaders) {

	/** Key of the origin in the MCP transport context */
	public static final String TRANSPORT_CONTEXT_KEY = McpRequestOrigin.class.getName();

	/**
	 * The proxy headers for scheme, host and port that HAPI's {@code ApacheProxyAddressStrategy} evaluates.
	 * {@code X-Forwarded-Prefix} is left out on purpose: on the MCP request it describes the path of the MCP route at
	 * the proxy, not the FHIR base path, and HAPI would use it as the FHIR base path. Behind a proxy that rewrites
	 * paths, {@code hapi.fhir.server_address} sets the FHIR base explicitly.
	 */
	static final List<String> FORWARDED_HEADERS =
			List.of("Forwarded", "X-Forwarded-Host", "X-Forwarded-Proto", "X-Forwarded-Port", "X-Forwarded-Ssl");

	public McpRequestOrigin {
		forwardedHeaders = forwardedHeaders == null ? Map.of() : Map.copyOf(forwardedHeaders);
	}

	public static McpRequestOrigin from(HttpServletRequest theRequest) {
		Map<String, String> forwardedHeaders = new LinkedHashMap<>();
		for (String name : FORWARDED_HEADERS) {
			String value = theRequest.getHeader(name);
			if (value != null) {
				forwardedHeaders.put(name, value);
			}
		}
		return new McpRequestOrigin(
				theRequest.getScheme(), theRequest.getServerName(), theRequest.getServerPort(), forwardedHeaders);
	}

	/**
	 * @return the origin that the transport stored for this MCP request, or {@code null} if there is none
	 */
	public static McpRequestOrigin from(McpSyncServerExchange theExchange) {
		if (theExchange == null || theExchange.transportContext() == null) {
			return null;
		}
		return theExchange.transportContext().get(TRANSPORT_CONTEXT_KEY) instanceof McpRequestOrigin origin
				? origin
				: null;
	}
}
