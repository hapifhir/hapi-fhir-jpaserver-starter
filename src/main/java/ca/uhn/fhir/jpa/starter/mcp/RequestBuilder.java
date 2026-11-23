package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;
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
import java.util.Set;

public class RequestBuilder {

	/**
	 * The headers that the tool schemas offer, per interaction. Other headers are rejected, so that a tool call cannot
	 * set e.g. Host, Authorization or X-Forwarded-* on the request that HAPI handles in-process.
	 */
	private static final Map<Interaction, Set<String>> ALLOWED_HEADERS = Map.of(
			Interaction.CREATE, Set.of(Constants.HEADER_IF_NONE_EXIST),
			Interaction.UPDATE, Set.of(Constants.HEADER_IF_MATCH),
			Interaction.PATCH, Set.of(Constants.HEADER_IF_MATCH));

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
		String contentType = Constants.CT_FHIR_JSON_NEW;

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
				contentType = applyPatchBody(req);
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
				contentType = applyPatchBody(req);
			}
			default -> throw new IllegalArgumentException("Unsupported interaction: " + interaction);
		}

		req.setContextPath(contextPath);
		req.setContentType(contentType);
		req.addHeader("Accept", Constants.CT_FHIR_JSON_NEW);

		applyHeaders(req);

		// Mirror the parameters added above into the query string: HAPI derives conditional URLs from it, and
		// HAPI FHIR 9 reads GET search parameters only from the query string, not from the request parameter map
		// (hapi-fhir#8769). This is done here rather than inside the switch so it applies however the parameters
		// were added.
		if (req.getQueryString() == null && !req.getParameterMap().isEmpty()) {
			req.setQueryString(toQueryString(req.getParameterMap()));
		}
		return req;
	}

	/**
	 * Adds the headers from the tool arguments, e.g. If-None-Exist for a conditional create or If-Match for a
	 * version-aware update. Only the headers in {@link #ALLOWED_HEADERS} are accepted.
	 */
	private void applyHeaders(MockHttpServletRequest req) {
		Object headers = config.get("headers");
		if (headers == null) {
			return;
		}
		if (!(headers instanceof Map<?, ?> map)) {
			throw new IllegalArgumentException("'headers' must be a JSON object");
		}
		Set<String> allowed = ALLOWED_HEADERS.getOrDefault(interaction, Set.of());
		map.forEach((name, value) -> {
			String headerName = String.valueOf(name);
			if (allowed.stream().noneMatch(headerName::equalsIgnoreCase)) {
				throw new IllegalArgumentException("Unsupported header '" + headerName + "' for "
						+ interaction.getName() + ", allowed: " + allowed);
			}
			if (!(value instanceof String || value instanceof Number)) {
				throw new IllegalArgumentException("Header '" + headerName + "' must be a string or a number");
			}
			req.addHeader(headerName, value.toString());
		});
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

	/**
	 * @return the content type of the patch body: JSON Patch (RFC 6902) for a JSON array, otherwise FHIR Patch
	 */
	private String applyPatchBody(MockHttpServletRequest req) {
		Object patchBody = config.get("resource");
		if (patchBody == null) {
			throw new IllegalArgumentException("Missing 'resource' for patch interaction");
		}
		String content;
		if (patchBody instanceof String s) {
			content = s;
		} else if (patchBody instanceof Map<?, ?>) {
			// The tool schema allows the patch body as a JSON object, as for create and update
			content = new Gson().toJson(patchBody, Map.class);
		} else if (patchBody instanceof List<?>) {
			// A JSON Patch document is a JSON array of operations
			content = new Gson().toJson(patchBody, List.class);
		} else if (patchBody instanceof IBaseResource r) {
			content = fhirContext.newJsonParser().encodeResourceToString(r);
		} else {
			throw new IllegalArgumentException("Unsupported patch body type: " + patchBody.getClass());
		}
		req.setContent(content.getBytes(StandardCharsets.UTF_8));
		return content.stripLeading().startsWith("[") ? Constants.CT_JSON_PATCH : Constants.CT_FHIR_JSON_NEW;
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
		Object query = config.get("query");
		if (query == null || (query instanceof String s && s.isBlank())) {
			query = config.get("searchParams");
		}
		List<String[]> parameters = new ArrayList<>();
		if (query instanceof Map<?, ?> map) {
			map.forEach((name, value) -> {
				Collection<?> values = value instanceof Collection<?> c ? c : Collections.singletonList(value);
				for (Object v : values) {
					if (v == null) {
						throw new IllegalArgumentException("Missing value for query parameter '" + name + "'");
					}
					if (!(v instanceof String || v instanceof Number || v instanceof Boolean)) {
						throw new IllegalArgumentException(
								"Query parameter '" + name + "' must have a string, number or boolean value");
					}
					parameters.add(new String[] {name.toString(), v.toString()});
				}
			});
		} else if (query instanceof String str) {
			if (!str.isBlank()) {
				UrlUtil.parseQueryString(str).forEach((name, values) -> {
					for (String v : values) {
						parameters.add(new String[] {name, v});
					}
				});
			}
		} else if (query != null) {
			throw new IllegalArgumentException("'query' must be a query string or a JSON object");
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
