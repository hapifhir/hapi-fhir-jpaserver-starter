package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;

import com.google.gson.Gson;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RequestBuilder {

	private final String resourceType;
	private final Interaction interaction;
	private final Map<String, Object> config;
	private final String serverAddress;
	private final String servletContextPath;
	private final FhirContext fhirContext;
	/**
	 * Constructs a RequestBuilder for a specific FHIR interaction.
	 *
	 * @param fhirContext the FHIR context
	 * @param contextMap  a map containing configuration parameters, including 'resourceType'
	 * @param interaction the type of interaction (e.g., SEARCH, READ, CREATE, etc.)
	 */
	public RequestBuilder(RestfulServer restfulServer, Map<String, Object> contextMap, Interaction interaction) {
		this.config = contextMap;
		if (interaction == Interaction.TRANSACTION || interaction == Interaction.VALIDATE) this.resourceType = "";
		else if (contextMap.get("resourceType") instanceof String rt && !rt.isBlank()) this.resourceType = rt;
		else throw new IllegalArgumentException("Missing or invalid 'resourceType' in contextMap");

		this.interaction = interaction;
		String serverAddressUrl = ((HardcodedServerAddressStrategy) restfulServer.getServerAddressStrategy()).getValue();
		serverAddressUrl = serverAddressUrl.substring(serverAddressUrl.indexOf("//")+2);
		this.serverAddress = serverAddressUrl.substring(0, serverAddressUrl.indexOf("/"));
		this.servletContextPath = restfulServer.getServletContext().getContextPath();

		this.fhirContext = restfulServer.getFhirContext();
	}

	public MockHttpServletRequest buildRequest() {
		String basePath = servletContextPath + "/fhir/" + resourceType;
		String method;
		MockHttpServletRequest req;

		switch (interaction) {
			case SEARCH -> {
				method = "GET";
				req = new MockHttpServletRequest(method, basePath);
				req.setServerName(this.serverAddress);
				req.setServletPath("/fhir");

				Map<?, ?> sp = null;
				if (config.get("query") instanceof Map<?, ?> q) {
					sp = q;
				} else if (config.get("searchParams") instanceof Map<?, ?> s) {
					sp = s;
				}
				if (sp != null) {
					sp.forEach((k, v) -> req.addParameter(k.toString(), v.toString()));
				}
			}
			case READ -> {
				method = "GET";
				String id = requireString();
				req = new MockHttpServletRequest(method, basePath + "/" + id);
				req.setServerName(this.serverAddress);
				req.setServletPath("/fhir");
			}
			case CREATE, TRANSACTION -> {
				method = "POST";
				req = new MockHttpServletRequest(method, basePath);
				req.setServerName(this.serverAddress);
				req.setServletPath("/fhir");
				applyResourceBody(req);
			}
			case UPDATE -> {
				method = "PUT";
				String id = requireString();
				req = new MockHttpServletRequest(method, basePath + "/" + id);
				req.setServerName(this.serverAddress);
				req.setServletPath("/fhir");
				applyResourceBody(req);
			}
			case DELETE -> {
				method = "DELETE";
				String id = requireString();
				req = new MockHttpServletRequest(method, basePath + "/" + id);
				req.setServerName(this.serverAddress);
				req.setServletPath("/fhir");
			}
			case PATCH -> {
				method = "PATCH";
				String id = requireString();
				req = new MockHttpServletRequest(method, basePath + "/" + id);
				req.setServerName(this.serverAddress);
				req.setServletPath("/fhir");
				applyPatchBody(req);
			}
			case VALIDATE -> {
				method = "POST";

				String uriValidate = servletContextPath+ "/fhir/$validate";
				req = new MockHttpServletRequest(method, uriValidate);
				req.setServerName(this.serverAddress);
				req.setServletPath("/fhir");

				String content = (String) config.get("resource");
				req.setContent(content.getBytes(StandardCharsets.UTF_8));
				String profile = (String) config.get("profile");
				if (profile != null && !profile.isBlank()) {
					req.addParameter("profile", profile);
				}
				Map<?, ?> sp = null;
				if (config.get("query") instanceof Map<?, ?> q) {
					sp = q;
				} else if (config.get("searchParams") instanceof Map<?, ?> s) {
					sp = s;
				}
				if (sp != null) {
					sp.forEach((k, v) -> req.addParameter(k.toString(), v.toString()));
				}
				req.addHeader("Content-Type", "text/plain; charset=UTF-8");
			}
			default -> throw new IllegalArgumentException("Unsupported interaction: " + interaction);
		}

		if (req.getContentType() == null) {
			req.addHeader("Content-Type", "application/fhir+json");
		}

		req.addHeader("Accept", "application/fhir+json");
		return req;
	}

	private void applyResourceBody(MockHttpServletRequest req) {
		Object resourceObj = config.get("resource");
		String json;
		if (resourceObj instanceof Map<?, ?>) json = new Gson().toJson(resourceObj, Map.class);
		else if (resourceObj instanceof String) json = resourceObj.toString();
		else throw new IllegalArgumentException("Unsupported resource body type: " + resourceObj.getClass());
		req.setContent(json.getBytes(StandardCharsets.UTF_8));
	}

	private void applyPatchBody(MockHttpServletRequest req) {
		Object patchBody = config.get("resource");
		if (patchBody == null) {
			throw new IllegalArgumentException("Missing 'resource' for patch interaction");
		}
		String content;
		if (patchBody instanceof String s) {
			content = s;
		} else if (patchBody instanceof IBaseResource r) {
			content = fhirContext.newJsonParser().encodeResourceToString(r);
		} else {
			throw new IllegalArgumentException("Unsupported patch body type: " + patchBody.getClass());
		}
		req.setContent(content.getBytes(StandardCharsets.UTF_8));
	}

	private String requireString() {
		Object val = config.get("id");
		if (!(val instanceof String s) || s.isBlank()) {
			throw new IllegalArgumentException("Missing or invalid '" + "id" + "'");
		}
		return s;
	}
}
