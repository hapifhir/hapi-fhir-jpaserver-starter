package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.UrlUtil;
import com.google.gson.Gson;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestBuilder {

	private final FhirContext fhirContext;
	private final String resourceType;
	private final Interaction interaction;
	private final Map<String, Object> config;
	private final String contextPath;

	public RequestBuilder(FhirContext fhirContext, Map<String, Object> contextMap, Interaction interaction) {
		this(fhirContext, contextMap, interaction, "");
	}

	/**
	 * Constructs a RequestBuilder for a specific FHIR interaction.
	 *
	 * @param fhirContext the FHIR context
	 * @param contextMap  a map containing configuration parameters, including 'resourceType'
	 * @param interaction the type of interaction (e.g., SEARCH, READ, CREATE, etc.)
	 */
	public RequestBuilder(
			FhirContext fhirContext, Map<String, Object> contextMap, Interaction interaction, String contextPath) {
		this.config = contextMap;
		// RestfulServer strips the servlet context path (e.g. "/eps") from the request URI,
		// so the mock request must carry it too, otherwise "/Patient" becomes "ient".
		String cp = contextPath == null ? "" : contextPath.trim();
		if (cp.endsWith("/")) cp = cp.substring(0, cp.length() - 1);
		this.contextPath = cp;
		if (interaction == Interaction.TRANSACTION) this.resourceType = "";
		else if (contextMap.get("resourceType") instanceof String rt && !rt.isBlank()) this.resourceType = rt;
		else throw new IllegalArgumentException("Missing or invalid 'resourceType' in contextMap");

		this.interaction = interaction;
		this.fhirContext = fhirContext;
	}

	public MockHttpServletRequest buildRequest() {
		String basePath = contextPath + "/" + resourceType;
		String method;
		MockHttpServletRequest req;

		switch (interaction) {
			case SEARCH -> {
				method = "GET";
				req = new MockHttpServletRequest(method, basePath);
				queryParameters().forEach(p -> req.addParameter(p[0], p[1]));
			}
			case READ -> {
				method = "GET";
				String id = requireString();
				req = new MockHttpServletRequest(method, basePath + "/" + id);
			}
			case CREATE, TRANSACTION -> {
				method = "POST";
				req = new MockHttpServletRequest(method, basePath);
				applyResourceBody(req);
			}
			case UPDATE -> {
				method = "PUT";
				String id = requireString();
				req = new MockHttpServletRequest(method, basePath + "/" + id);
				applyResourceBody(req);
			}
			case DELETE -> {
				method = "DELETE";
				String id = requireString();
				req = new MockHttpServletRequest(method, basePath + "/" + id);
			}
			case PATCH -> {
				method = "PATCH";
				String id = requireString();
				req = new MockHttpServletRequest(method, basePath + "/" + id);
				applyPatchBody(req);
			}
			case CONDITIONAL_UPDATE -> {
				method = "PUT";
				req = new MockHttpServletRequest(method, basePath);
				requireQueryParameters("conditional update").forEach(p -> req.addParameter(p[0], p[1]));
				applyResourceBody(req);
			}
			case CONDITIONAL_PATCH -> {
				method = "PATCH";
				req = new MockHttpServletRequest(method, basePath);
				requireQueryParameters("conditional patch").forEach(p -> req.addParameter(p[0], p[1]));
				applyPatchBody(req);
			}
			default -> throw new IllegalArgumentException("Unsupported interaction: " + interaction);
		}

		req.setContextPath(contextPath);
		req.setContentType("application/fhir+json");
		req.addHeader("Accept", "application/fhir+json");

		// Mirror the parameters added above into the query string: HAPI derives conditional URLs from it, and
		// HAPI FHIR 9 reads GET search parameters only from the query string, not from the request parameter map
		// (hapi-fhir#8769). This is done here rather than inside the switch so it applies however the parameters
		// were added.
		if (req.getQueryString() == null && !req.getParameterMap().isEmpty()) {
			req.setQueryString(toQueryString(req.getParameterMap()));
		}
		return req;
	}

	private static String toQueryString(Map<String, String[]> theParameters) {
		StringBuilder query = new StringBuilder();
		theParameters.forEach((name, values) -> {
			for (String value : values) {
				if (!query.isEmpty()) {
					query.append('&');
				}
				query.append(UrlUtil.escapeUrlParam(name)).append('=').append(UrlUtil.escapeUrlParam(value));
			}
		});
		return query.toString();
	}

	private void applyResourceBody(MockHttpServletRequest req) {
		Object resourceObj = config.get("resource");
		if (resourceObj == null) {
			throw new IllegalArgumentException("Missing 'resource'");
		}
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
		} else if (patchBody instanceof Map<?, ?>) {
			// The tool schemas declare 'resource' as a JSON object, e.g. a FHIRPath Patch Parameters resource
			content = new Gson().toJson(patchBody, Map.class);
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

	/**
	 * Search parameters from 'query', or from 'searchParams' if there is no 'query'. They can be given as a map, where
	 * a list value becomes a repeated parameter, or as a query string such as "name=Silva&_count=5", which is decoded
	 * the same way HAPI decodes the query string of a request.
	 */
	private List<String[]> queryParameters() {
		Object query = config.get("query") != null ? config.get("query") : config.get("searchParams");
		List<String[]> parameters = new ArrayList<>();
		if (query instanceof Map<?, ?> map) {
			map.forEach((name, value) -> {
				Collection<?> values = value instanceof Collection<?> c ? c : Collections.singletonList(value);
				for (Object v : values) {
					if (v == null) {
						throw new IllegalArgumentException("Missing value for query parameter '" + name + "'");
					}
					parameters.add(new String[] {name.toString(), v.toString()});
				}
			});
		} else if (query instanceof String str && !str.isBlank()) {
			UrlUtil.parseQueryString(str).forEach((name, values) -> {
				for (String v : values) {
					parameters.add(new String[] {name, v});
				}
			});
		}
		return parameters;
	}

	/**
	 * Conditional update and patch identify the target resource only by the query ({@code PUT [type]?[query]}).
	 */
	private List<String[]> requireQueryParameters(String operation) {
		List<String[]> parameters = queryParameters();
		if (parameters.isEmpty()) {
			throw new IllegalArgumentException("Missing 'query' for " + operation);
		}
		return parameters;
	}
}
