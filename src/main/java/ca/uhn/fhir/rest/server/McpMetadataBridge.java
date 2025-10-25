package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.jpa.starter.mcp.SearchTypeDescriptions;
import ca.uhn.fhir.jpa.starter.mcp.ToolFactory;
import ca.uhn.fhir.rest.api.IResourceSupportedSvc;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.rest.server.util.ResourceSearchParams;
import ca.uhn.fhir.util.UrlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides Model Context Protocol tools that expose server metadata such as resources, data types,
 * search parameters, and validation helpers.
 */
public class McpMetadataBridge implements McpBridge {

	private static final Logger ourLog = LoggerFactory.getLogger(McpMetadataBridge.class);
	private static final String DEFAULT_STORE_NAME = "default";
	private static final Set<String> GENERAL_SEARCH_PARAMS = Set.of(
		"_id",
		"_lastUpdated",
		"_profile",
		"_security",
		"_tag",
		"_text",
		"_content",
		"_list",
		"_has",
		"_include",
		"_revinclude",
		"_count",
		"_sort",
		"_summary",
		"_elements",
		"_since",
		"_type",
		"_total");

	private final RestfulServer myRestfulServer;
	private final FhirContext myFhirContext;
	private final ca.uhn.fhir.util.FhirTerser myTerser;
	private final ca.uhn.fhir.context.support.IValidationSupport myValidationSupport;
	private final IResourceSupportedSvc myResourceSupportedSvc;
	private final ISearchParamRegistry mySearchParamRegistry;

	public McpMetadataBridge(
		RestfulServer restfulServer,
		ca.uhn.fhir.context.support.IValidationSupport validationSupport,
		IResourceSupportedSvc resourceSupportedSvc,
		ISearchParamRegistry searchParamRegistry) {
		myRestfulServer = restfulServer;
		myFhirContext = restfulServer.getFhirContext();
		myTerser = myFhirContext.newTerser();
		myValidationSupport = validationSupport;
		myResourceSupportedSvc = resourceSupportedSvc;
		mySearchParamRegistry = searchParamRegistry;
	}

	@Override
	public List<McpServerFeatures.SyncToolSpecification> generateTools() {
		try {
			return List.of(
				buildSpecification(ToolFactory.getStoreList(), this::handleGetStoreList),
				buildSpecification(ToolFactory.getResourceList(), this::handleGetResourceList),
				buildSpecification(ToolFactory.getResourceDefinition(), this::handleGetResourceDefinition),
				buildSpecification(ToolFactory.getDataTypeList(), this::handleGetDataTypeList),
				buildSpecification(ToolFactory.getDataTypeDefinition(), this::handleGetDataTypeDefinition),
				buildSpecification(ToolFactory.getSearchTypeList(), this::handleGetSearchTypeList),
				buildSpecification(ToolFactory.getSearchTypeDefinition(), this::handleGetSearchTypeDefinition),
				buildSpecification(ToolFactory.getSearchParameters(), this::handleGetSearchParameters),
				buildSpecification(ToolFactory.validateTypeSearch(), this::handleValidateTypeSearch));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private McpServerFeatures.SyncToolSpecification buildSpecification(
		io.modelcontextprotocol.spec.McpSchema.Tool tool,
		Function<McpSchema.CallToolRequest, McpSchema.CallToolResult> handler) {
		return new McpServerFeatures.SyncToolSpecification.Builder()
			.tool(tool)
			.callHandler((exchange, request) -> handler.apply(request))
			.build();
	}

	private McpSchema.CallToolResult handleGetStoreList(McpSchema.CallToolRequest request) {
		var version = myFhirContext.getVersion().getVersion().getFhirVersionString();
		var baseUrl = determineServerBaseUrl();
		var message = String.format(
			Locale.ROOT,
			"%s: URL is %s, FHIR version is %s",
			DEFAULT_STORE_NAME,
			baseUrl,
			version);
		return toTextResult(List.of(message));
	}

	private McpSchema.CallToolResult handleGetResourceList(McpSchema.CallToolRequest request) {

		Set<String> resources = myFhirContext.getResourceTypes().stream()
			.filter(myResourceSupportedSvc::isSupported)
			.collect(Collectors.toCollection(TreeSet::new));
		if (resources.isEmpty()) {
			return toErrorResult("No resource providers are registered on this server.");
		}
		return toTextResult(new ArrayList<>(resources));
	}

	private McpSchema.CallToolResult handleGetResourceDefinition(McpSchema.CallToolRequest request) {
		String resourceName = getStringArgument(request, "resourceType");
		if (StringUtils.isBlank(resourceName)) {
			return toErrorResult("Resource name is missing or not provided and is required.");
		}
		if (!myResourceSupportedSvc.isSupported(resourceName)) {
			return toErrorResult("The provided resource name did not resolve into a supported resource on this server.");
		}

		Optional<String> definition = describeStructure(resourceName, Set.of("resource"));
		return definition
			.map(desc -> toTextResult(List.of(desc)))
			.orElseGet(() -> toErrorResult("Unable to locate a StructureDefinition for resource " + resourceName + "."));
	}

	private McpSchema.CallToolResult handleGetDataTypeList(McpSchema.CallToolRequest request) {
		Set<String> dataTypes = myFhirContext.getElementDefinitions().stream()
			.filter(def -> {
				BaseRuntimeElementDefinition.ChildTypeEnum childType = def.getChildType();
				return childType == BaseRuntimeElementDefinition.ChildTypeEnum.COMPOSITE_DATATYPE
					|| childType == BaseRuntimeElementDefinition.ChildTypeEnum.PRIMITIVE_DATATYPE;
			})
			.map(BaseRuntimeElementDefinition::getName)
			.filter(name -> !myFhirContext.getResourceTypes().contains(name))
			.collect(Collectors.toCollection(TreeSet::new));
		if (dataTypes.isEmpty()) {
			return toErrorResult("No FHIR data types were discovered for the current server version.");
		}
		return toTextResult(new ArrayList<>(dataTypes));
	}

	private McpSchema.CallToolResult handleGetDataTypeDefinition(McpSchema.CallToolRequest request) {
		String datatypeName = getStringArgument(request, "datatypeName");
		if (StringUtils.isBlank(datatypeName)) {
			return toErrorResult("Data type name is missing or not provided and is required.");
		}
		Optional<String> definition = describeStructure(datatypeName, Set.of("complex-type", "primitive-type"));
		return definition
			.map(desc -> toTextResult(List.of(desc)))
			.orElseGet(() -> toErrorResult("The provided data type name did not resolve on this server."));
	}

	private McpSchema.CallToolResult handleGetSearchTypeList(McpSchema.CallToolRequest request) {
		return toTextResult(SearchTypeDescriptions.codes());
	}

	private McpSchema.CallToolResult handleGetSearchTypeDefinition(McpSchema.CallToolRequest request) {
		String searchType = getStringArgument(request, "searchType");
		if (StringUtils.isBlank(searchType)) {
			return toErrorResult("Search type is missing or not provided and is required.");
		}
		return SearchTypeDescriptions.describe(searchType)
			.map(desc -> toTextResult(List.of(desc)))
			.orElseGet(() -> toErrorResult("The provided search type did not resolve on this server."));
	}

	private McpSchema.CallToolResult handleGetSearchParameters(McpSchema.CallToolRequest request) {
		String resourceName = getStringArgument(request, "resourceType");
		if (StringUtils.isBlank(resourceName)) {
			return toErrorResult("Resource name is missing or not provided and is required.");
		}
		if (!myResourceSupportedSvc.isSupported(resourceName)) {
			return toErrorResult("The provided resource name did not resolve into a supported resource on this server.");
		}

		List<String> searchParameters = collectSearchParameterDescriptions(resourceName);
		if (searchParameters.isEmpty()) {
			return toTextResult(List.of("No search parameters were found for resource " + resourceName + "."));
		}
		return toTextResult(searchParameters);
	}

	private McpSchema.CallToolResult handleValidateTypeSearch(McpSchema.CallToolRequest request) {
		String resourceName = getStringArgument(request, "resourceType");
		String searchString = getStringArgument(request, "searchString");

		if (StringUtils.isBlank(resourceName)) {
			return toErrorResult("Resource name is missing or not provided and is required.");
		}
		if (StringUtils.isBlank(searchString)) {
			return toErrorResult("Search string is missing or not provided and is required.");
		}

		ResourceSearchParams activeSearchParams = mySearchParamRegistry.getRuntimeSearchParams(
				resourceName, ISearchParamRegistry.SearchParamLookupContextEnum.SEARCH);
		if (activeSearchParams == null || activeSearchParams.size() == 0) {
			return toErrorResult("No search parameters were found for resource " + resourceName + ".");
		}
		Map<String, RuntimeSearchParam> activeParams = activeSearchParams.getSearchParamNames().stream()
				.collect(Collectors.toMap(Function.identity(), activeSearchParams::get));

		Map<String, String[]> rawParams;
		try {
			rawParams = UrlUtil.parseQueryString(searchString);
		} catch (Exception e) {
			return toErrorResult("Unable to parse search string: " + e.getMessage());
		}

		Map<String, List<String>> parsedParams = new LinkedHashMap<>();
		rawParams.forEach((key, values) -> parsedParams.put(key, values == null ? List.of() : Arrays.asList(values)));

		if (parsedParams.isEmpty()) {
			return toErrorResult("No search parameters were detected in the provided search string.");
		}

		List<String> detailMessages = new ArrayList<>();
		int invalidCount = 0;

		for (Map.Entry<String, List<String>> entry : parsedParams.entrySet()) {
			String rawName = entry.getKey();
			List<String> values = entry.getValue() == null ? List.of("") : entry.getValue();

			String baseName = rawName;
			int modifierIndex = rawName.indexOf(':');
			if (modifierIndex > 0) {
				baseName = rawName.substring(0, modifierIndex);
			}

			String normalizedBase = normalizeGeneralParameter(baseName);
			boolean isGeneral = GENERAL_SEARCH_PARAMS.contains(normalizedBase);
			RuntimeSearchParam searchParam = activeParams.get(baseName);

			boolean isValid = searchParam != null || isGeneral;
			if (!isValid) {
				invalidCount++;
			}

			String detail;
			if (searchParam != null) {
				detail = "Recognized search parameter of type " + searchParam.getParamType().getCode();
			} else if (isGeneral) {
				detail = "Recognized server-wide parameter.";
			} else {
				detail = "Unknown parameter for resource " + resourceName + ".";
			}

			String combinedValues = String.join(",", values);
			detailMessages.add(String.format(
				Locale.ROOT,
				"'%s=%s': %s%s",
				rawName,
				combinedValues,
				isValid ? "Valid" : "Invalid",
				" - " + detail));
		}

		int total = detailMessages.size();
		String overall;
		boolean isError = invalidCount > 0;
		if (invalidCount == 0) {
			overall = String.format(
				Locale.ROOT, "All %d search parameters are valid for %s.", total, resourceName);
		} else {
			overall = String.format(
				Locale.ROOT,
				"%d of %d search parameters are invalid for %s.",
				invalidCount,
				total,
				resourceName);
		}

		List<String> responses = new ArrayList<>();
		responses.add(overall);
		responses.addAll(detailMessages);
		return toTextResult(responses, isError);
	}

	private List<String> collectSearchParameterDescriptions(String resourceName) {
		List<String> results = new ArrayList<>();

		ResourceSearchParams registryParams = mySearchParamRegistry.getRuntimeSearchParams(
			resourceName, ISearchParamRegistry.SearchParamLookupContextEnum.SEARCH);
		if (registryParams != null && registryParams.size() > 0) {
			results = registryParams.values().stream()
				.sorted(Comparator.comparing(RuntimeSearchParam::getName, String.CASE_INSENSITIVE_ORDER))
				.map(this::formatRuntimeSearchParam)
				.collect(Collectors.toCollection(ArrayList::new));
		}

		if (!results.isEmpty()) {
			return results;
		}

		List<? extends IBaseResource> searchParameters = myValidationSupport.fetchAllSearchParameters();
		if (searchParameters != null && !searchParameters.isEmpty()) {
			for (IBaseResource searchParameter : searchParameters) {
				List<?> bases = myTerser.getValues(searchParameter, "base");
				boolean matches = bases.stream()
					.filter(IPrimitiveType.class::isInstance)
					.map(IPrimitiveType.class::cast)
					.map(IPrimitiveType::getValueAsString)
					.anyMatch(resourceName::equalsIgnoreCase);
				if (!matches) {
					continue;
				}
				String code = defaultString(myTerser.getSinglePrimitiveValueOrNull(searchParameter, "code"), "Unnamed");
				String type = defaultString(myTerser.getSinglePrimitiveValueOrNull(searchParameter, "type"), "unknown");
				String description = defaultString(
					myTerser.getSinglePrimitiveValueOrNull(searchParameter, "description"), "");
				results.add(String.format(Locale.ROOT, "%s (%s): %s", code, type, description));
			}
		}

		if (!results.isEmpty()) {
			return results.stream().sorted().collect(Collectors.toList());
		}

		RuntimeResourceDefinition resourceDefinition = myFhirContext.getResourceDefinition(resourceName);
		return resourceDefinition.getSearchParams().stream()
			.sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
			.map(this::formatRuntimeSearchParam)
			.collect(Collectors.toList());
	}

	private String formatRuntimeSearchParam(RuntimeSearchParam param) {
		String name = param.getName();
		String description = StringUtils.defaultString(param.getDescription(), "");
		return String.format(
			Locale.ROOT,
			"%s (%s): %s",
			name,
			param.getParamType().getCode(),
			description);
	}

	private Optional<String> describeStructure(String typeName, Set<String> expectedKinds) {
		List<? extends IBaseResource> structures = myValidationSupport.fetchAllStructureDefinitions();
		if (structures == null) {
			return Optional.empty();
		}

		for (IBaseResource structure : structures) {
			String kind = lowerOrNull(myTerser.getSinglePrimitiveValueOrNull(structure, "kind"));
			if (kind == null || !expectedKinds.contains(kind)) {
				continue;
			}

			String type = myTerser.getSinglePrimitiveValueOrNull(structure, "type");
			if (!typeName.equalsIgnoreCase(StringUtils.defaultString(type))) {
				continue;
			}

			String title = defaultString(myTerser.getSinglePrimitiveValueOrNull(structure, "title"), "");
			String description = defaultString(myTerser.getSinglePrimitiveValueOrNull(structure, "description"), "");
			String purpose = defaultString(myTerser.getSinglePrimitiveValueOrNull(structure, "purpose"), "");
			String url = defaultString(myTerser.getSinglePrimitiveValueOrNull(structure, "url"), "");
			String version = defaultString(myTerser.getSinglePrimitiveValueOrNull(structure, "version"), "");

			StringBuilder builder = new StringBuilder();
			builder.append(typeName);
			if (!title.isBlank()) {
				builder.append(": ").append(title);
			}
			if (!description.isBlank()) {
				builder.append("\nDescription: ").append(description);
			}
			if (!purpose.isBlank()) {
				builder.append("\nPurpose: ").append(purpose);
			}
			if (!url.isBlank()) {
				builder.append("\nURL: ").append(url);
			}
			if (!version.isBlank()) {
				builder.append("\nVersion: ").append(version);
			}
			return Optional.of(builder.toString());
		}

		return Optional.empty();
	}

	private String determineServerBaseUrl() {
		try {
			HttpServletRequest request = buildMockRequest();
			ServletRequestDetails requestDetails = new ServletRequestDetails();
			requestDetails.setServletRequest(request);
			requestDetails.setServer(myRestfulServer);
			return myRestfulServer.getServerBaseForRequest(requestDetails);
		} catch (Exception e) {
			ourLog.debug("Unable to determine server base URL", e);
			return "(base URL unavailable in MCP context)";
		}
	}

	private HttpServletRequest buildMockRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/metadata");
		request.setScheme("http");
		request.setServerName("localhost");
		request.setServerPort(8080);
		request.setContextPath(Optional.ofNullable(myRestfulServer.getServletContext())
			.map(ctx -> StringUtils.defaultString(ctx.getContextPath(), ""))
			.orElse(""));
		return request;
	}

	private static McpSchema.CallToolResult toTextResult(List<String> lines) {
		return toTextResult(lines, false);
	}

	private static McpSchema.CallToolResult toTextResult(List<String> lines, boolean isError) {
		String content = String.join("\n", lines);
		McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
		if (isError) {
			builder.isError(true);
		}
		return builder.addTextContent(content).build();
	}

	private static McpSchema.CallToolResult toErrorResult(String message) {
		return McpSchema.CallToolResult.builder()
			.isError(true)
			.addTextContent(message)
			.build();
	}

	private String getStringArgument(McpSchema.CallToolRequest request, String name) {
		if (request.arguments() == null) {
			return null;
		}
		Object value = request.arguments().get(name);
		return value instanceof String ? (String) value : null;
	}

	private static String defaultString(String value, String defaultVal) {
		return value == null ? defaultVal : value;
	}

	private static String lowerOrNull(String value) {
		return value == null ? null : value.toLowerCase(Locale.ROOT);
	}

	private static String normalizeGeneralParameter(String baseName) {
		if (baseName == null) {
			return "";
		}
		if (baseName.startsWith("_include")) {
			return "_include";
		}
		if (baseName.startsWith("_revinclude")) {
			return "_revinclude";
		}
		return baseName;
	}
}
