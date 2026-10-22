package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
}
