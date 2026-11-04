package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.UrlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestBuilderTest {

	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4Cached();

	private static MockHttpServletRequest build(Map<String, Object> args, Interaction interaction, String contextPath) {
		return new RequestBuilder(FHIR_CONTEXT, args, interaction, contextPath).buildRequest();
	}

	@Test
	void legacyConstructorKeepsRootPath() {
		var req = new RequestBuilder(FHIR_CONTEXT, Map.of("resourceType", "Patient"), Interaction.SEARCH).buildRequest();

		assertThat(req.getRequestURI()).isEqualTo("/Patient");
		assertThat(req.getContextPath()).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "/r5", "/fhir", "/apps/fhir-server"})
	void searchIncludesServletContextPath(String contextPath) {
		var req = build(Map.of("resourceType", "Patient"), Interaction.SEARCH, contextPath);

		assertThat(req.getRequestURI()).isEqualTo(contextPath + "/Patient");
		assertThat(req.getContextPath()).isEqualTo(contextPath);
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "/r5", "/fhir", "/apps/fhir-server"})
	void instanceLevelInteractionsIncludeServletContextPath(String contextPath) {
		var args = new HashMap<String, Object>();
		args.put("resourceType", "Patient");
		args.put("id", "123");
		args.put("resource", "{\"resourceType\":\"Patient\",\"id\":\"123\"}");

		var expected = contextPath + "/Patient/123";
		assertThat(build(args, Interaction.READ, contextPath).getRequestURI()).isEqualTo(expected);
		assertThat(build(args, Interaction.UPDATE, contextPath).getRequestURI()).isEqualTo(expected);
		assertThat(build(args, Interaction.DELETE, contextPath).getRequestURI()).isEqualTo(expected);
		assertThat(build(args, Interaction.PATCH, contextPath).getRequestURI()).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "/r5", "/apps/fhir-server"})
	void transactionPostsToContextRoot(String contextPath) {
		var req = build(
				Map.of("resource", "{\"resourceType\":\"Bundle\",\"type\":\"transaction\"}"),
				Interaction.TRANSACTION,
				contextPath);

		assertThat(req.getMethod()).isEqualTo("POST");
		assertThat(req.getRequestURI()).isEqualTo(contextPath + "/");
		assertThat(req.getContextPath()).isEqualTo(contextPath);
	}

	@ParameterizedTest
	@CsvSource(
			value = {"/r5/, /r5/Patient", "' /fhir ', /fhir/Patient", "/apps/fhir-server/, /apps/fhir-server/Patient"},
			ignoreLeadingAndTrailingWhitespace = false)
	void contextPathIsNormalised(String contextPath, String expectedUri) {
		assertThat(build(Map.of("resourceType", "Patient"), Interaction.SEARCH, contextPath).getRequestURI())
				.isEqualTo(expectedUri.trim());
	}

	@Test
	void nullContextPathMeansRoot() {
		var req = build(Map.of("resourceType", "Patient"), Interaction.SEARCH, null);

		assertThat(req.getRequestURI()).isEqualTo("/Patient");
		assertThat(req.getContextPath()).isEmpty();
	}

	@Test
	void searchAcceptsQueryMap() {
		var req = build(Map.of("resourceType", "Patient", "query", Map.of("family", "Silva")), Interaction.SEARCH, "");

		assertThat(req.getParameter("family")).isEqualTo("Silva");
	}

	@Test
	void searchAcceptsQueryString() {
		var req = build(
				Map.of("resourceType", "Patient", "query", "?identifier=urn:something%7Cabc&_count=5&name=Ana%20Silva"),
				Interaction.SEARCH,
				"");

		assertThat(req.getParameter("identifier")).isEqualTo("urn:something|abc");
		assertThat(req.getParameter("_count")).isEqualTo("5");
		assertThat(req.getParameter("name")).isEqualTo("Ana Silva");
	}

	@Test
	void searchAcceptsSearchParamsString() {
		var req = build(Map.of("resourceType", "Patient", "searchParams", "family=Silva&_summary"), Interaction.SEARCH, "");

		assertThat(req.getParameter("family")).isEqualTo("Silva");
		assertThat(req.getParameter("_summary")).isEmpty();
	}

	@Test
	void publishedSearchSchemaExampleAppliesEveryParameter() throws JsonProcessingException {
		var query = (Map<?, ?>) ToolFactory.searchFhirResources().inputSchema().properties().get("query");
		var description = query.get("description").toString();
		var matcher = Pattern.compile("For example: \"([^\"]+)\"").matcher(description);
		assertThat(matcher.find()).as(description).isTrue();
		var example = matcher.group(1);
		assertThat(example).isEqualTo("_id=pt-1&name=ivan");

		var req = build(Map.of("resourceType", "Patient", "query", example), Interaction.SEARCH, "");

		assertThat(req.getParameterMap()).containsOnlyKeys("_id", "name");
		assertThat(req.getParameter("_id")).isEqualTo("pt-1");
		assertThat(req.getParameter("name")).isEqualTo("ivan");
	}

	@Test
	void commasInsideValuesAreKeptForOrSearches() {
		var req = build(
				Map.of("resourceType", "Patient", "query", "name=ivan,peter&_id=pt-1,pt-2"), Interaction.SEARCH, "");

		assertThat(req.getParameterMap()).containsOnlyKeys("name", "_id");
		assertThat(req.getParameter("name")).isEqualTo("ivan,peter");
		assertThat(req.getParameter("_id")).isEqualTo("pt-1,pt-2");
	}

	@Test
	void searchParametersAreAlsoSetAsQueryString() {
		var req = build(
				Map.of("resourceType", "Patient", "query", "identifier=urn:something|abc&name=Ana Silva"),
				Interaction.SEARCH,
				"");

		var parsed = UrlUtil.parseQueryString(req.getQueryString());
		assertThat(parsed).containsOnlyKeys("identifier", "name");
		assertThat(parsed.get("identifier")).containsExactly("urn:something|abc");
		assertThat(parsed.get("name")).containsExactly("Ana Silva");
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "/fhir"})
	void conditionalUpdateTargetsTheResourceTypeWithTheQuery(String contextPath) {
		var args = new HashMap<String, Object>();
		args.put("resourceType", "Patient");
		args.put("query", "identifier=http://example.org/mrn|12345");
		args.put("resource", "{\"resourceType\":\"Patient\"}");

		var req = build(args, Interaction.CONDITIONAL_UPDATE, contextPath);

		assertThat(req.getMethod()).isEqualTo("PUT");
		assertThat(req.getRequestURI()).isEqualTo(contextPath + "/Patient");
		assertThat(UrlUtil.parseQueryString(req.getQueryString()).get("identifier"))
				.containsExactly("http://example.org/mrn|12345");
	}

	@Test
	void conditionalPatchTargetsTheResourceTypeWithTheQuery() {
		var args = Map.<String, Object>of(
				"resourceType", "Patient", "query", Map.of("family", "Silva"), "resource", "{\"resourceType\":\"Parameters\"}");

		var req = build(args, Interaction.CONDITIONAL_PATCH, "");

		assertThat(req.getMethod()).isEqualTo("PATCH");
		assertThat(req.getRequestURI()).isEqualTo("/Patient");
		assertThat(req.getParameter("family")).isEqualTo("Silva");
	}

	@Test
	void plainUpdateAndPatchUseTheIdAndIgnoreAQuery() {
		var args = new HashMap<String, Object>();
		args.put("resourceType", "Patient");
		args.put("id", "123");
		args.put("query", Map.of("family", "Silva"));
		args.put("resource", "{\"resourceType\":\"Patient\",\"id\":\"123\"}");

		for (var interaction : new Interaction[] {Interaction.UPDATE, Interaction.PATCH}) {
			var req = build(args, interaction, "");
			assertThat(req.getRequestURI()).as(interaction.name()).isEqualTo("/Patient/123");
			assertThat(req.getParameterMap()).as(interaction.name()).isEmpty();
			assertThat(req.getQueryString()).as(interaction.name()).isNull();
		}
	}

	@Test
	void conditionalUpdateAndPatchRequireAQuery() {
		var args = Map.<String, Object>of(
				"resourceType", "Patient", "id", "123", "resource", "{\"resourceType\":\"Patient\"}");

		for (var interaction : new Interaction[] {Interaction.CONDITIONAL_UPDATE, Interaction.CONDITIONAL_PATCH}) {
			assertThatThrownBy(() -> build(args, interaction, ""))
					.as(interaction.name())
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("'query'");
		}
	}

	@Test
	void plainUpdateAndPatchRequireAnId() {
		var args = Map.<String, Object>of(
				"resourceType", "Patient", "query", "family=Silva", "resource", "{\"resourceType\":\"Patient\"}");

		for (var interaction : new Interaction[] {Interaction.UPDATE, Interaction.PATCH}) {
			assertThatThrownBy(() -> build(args, interaction, ""))
					.as(interaction.name())
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("'id'");
		}
	}

	@Test
	void queryTakesPrecedenceOverSearchParamsInAnyForm() {
		var args = Map.<String, Object>of(
				"resourceType", "Patient", "query", "family=Silva", "searchParams", Map.of("name", "Ana"));

		var req = build(args, Interaction.SEARCH, "");

		assertThat(req.getParameterMap()).containsOnlyKeys("family");
	}

	@Test
	void listValuesInAQueryMapBecomeRepeatedParameters() {
		var args = Map.<String, Object>of(
				"resourceType", "Observation", "query", Map.of("date", List.of("ge2020-01-01", "le2020-12-31")));

		var req = build(args, Interaction.SEARCH, "");

		assertThat(req.getParameterValues("date")).containsExactly("ge2020-01-01", "le2020-12-31");
		assertThat(UrlUtil.parseQueryString(req.getQueryString()).get("date"))
				.containsExactly("ge2020-01-01", "le2020-12-31");
	}

	@Test
	void patchAcceptsAJsonObjectBody() {
		var args = Map.<String, Object>of(
				"resourceType", "Patient", "id", "123", "resource", Map.of("resourceType", "Parameters"));

		var req = build(args, Interaction.PATCH, "");

		assertThat(new String(req.getContentAsByteArray(), StandardCharsets.UTF_8))
				.isEqualTo("{\"resourceType\":\"Parameters\"}");
	}

	@Test
	void queryParameterWithoutValueIsRejected() {
		var query = new HashMap<String, Object>();
		query.put("name", null);

		assertThatThrownBy(() -> build(Map.of("resourceType", "Patient", "query", query), Interaction.SEARCH, ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("'name'");
	}

	@Test
	void missingResourceIsRejected() {
		assertThatThrownBy(() -> build(Map.of("resourceType", "Patient", "id", "123"), Interaction.UPDATE, ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("'resource'");
	}
}
