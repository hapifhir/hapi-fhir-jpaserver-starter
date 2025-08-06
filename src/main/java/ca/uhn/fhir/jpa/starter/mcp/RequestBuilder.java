package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RequestBuilder {

	private final FhirContext fhirContext;
	private final String resourceType;
	private final Interaction interaction;
	private final Map<String, Object> config;
	private final ObjectMapper mapper = new ObjectMapper();
	private final String headers;
	private String resource;

	public RequestBuilder(FhirContext fhirContext, Map<String, Object> contextMap, Interaction interaction) {
		this.config = contextMap;
		if (interaction == Interaction.TRANSACTION) this.resourceType = "";
		else if (contextMap.get("resourceType") instanceof String rt && !rt.isBlank()) this.resourceType = rt;
		else throw new IllegalArgumentException("Missing or invalid 'resourceType' in contextMap");
		// this.resourceType = contextMap.get("resourceType") instanceof String rt ? rt : null;
		this.headers = contextMap.get("headers") instanceof String h ? h : null;
		this.resource = null;
		try {
			resource = mapper.writeValueAsString(contextMap.get("resource"));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		this.interaction = interaction;
		this.fhirContext = fhirContext;
	}

	public MockHttpServletRequest buildRequest() {
		String basePath = "/" + resourceType;
		String method;
		MockHttpServletRequest req;

		switch (interaction) {
			case SEARCH -> {
				method = "GET";
				req = new MockHttpServletRequest(method, basePath);
				if (config.get("searchParams") instanceof Map<?, ?> sp) {
					sp.forEach((k, v) -> req.addParameter(k.toString(), v.toString()));
				}
			}
			case READ -> {
				method = "GET";
				String id = requireString("id");
				req = new MockHttpServletRequest(method, basePath + "/" + id);
			}
			case CREATE, TRANSACTION -> {
				method = "POST";
				req = new MockHttpServletRequest(method, basePath);
				applyResourceBody(req, "resource");
			}
			case UPDATE -> {
				method = "PUT";
				String id = requireString("id");
				req = new MockHttpServletRequest(method, basePath + "/" + id);
				applyResourceBody(req, "resource");
			}
			case DELETE -> {
				method = "DELETE";
				String id = requireString("id");
				req = new MockHttpServletRequest(method, basePath + "/" + id);
			}
			case PATCH -> {
				method = "PATCH";
				String id = requireString("id");
				req = new MockHttpServletRequest(method, basePath + "/" + id);
				applyPatchBody(req);
			}
			default -> throw new IllegalArgumentException("Unsupported interaction: " + interaction);
		}

		req.setContentType("application/fhir+json");
		req.addHeader("Accept", "application/fhir+json");
		return req;
	}

	private void applyResourceBody(MockHttpServletRequest req, String key) {
		Object resourceObj = config.get(key);
		String json = new Gson().toJson(resourceObj, Map.class);
		req.setContent(json.getBytes(StandardCharsets.UTF_8));
	}

	private void applyPatchBody(MockHttpServletRequest req) {
		Object patchBody = config.get("patch");
		if (patchBody == null) {
			throw new IllegalArgumentException("Missing 'patch' for patch interaction");
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

	private String requireString(String key) {
		Object val = config.get(key);
		if (!(val instanceof String s) || s.isBlank()) {
			throw new IllegalArgumentException("Missing or invalid '" + key + "'");
		}
		return (String) val;
	}
}
