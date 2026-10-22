package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import org.junit.jupiter.api.Test;
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

	@Test
	void searchIncludesServletContextPath() {
		var req = build(Map.of("resourceType", "Patient"), Interaction.SEARCH, "/eps");

		assertThat(req.getRequestURI()).isEqualTo("/eps/Patient");
		assertThat(req.getContextPath()).isEqualTo("/eps");
	}

	@Test
	void instanceLevelInteractionsIncludeServletContextPath() {
		var args = new HashMap<String, Object>();
		args.put("resourceType", "Patient");
		args.put("id", "123");
		args.put("resource", "{\"resourceType\":\"Patient\",\"id\":\"123\"}");

		assertThat(build(args, Interaction.READ, "/eps").getRequestURI()).isEqualTo("/eps/Patient/123");
		assertThat(build(args, Interaction.UPDATE, "/eps").getRequestURI()).isEqualTo("/eps/Patient/123");
		assertThat(build(args, Interaction.DELETE, "/eps").getRequestURI()).isEqualTo("/eps/Patient/123");
		assertThat(build(args, Interaction.PATCH, "/eps").getRequestURI()).isEqualTo("/eps/Patient/123");
	}

	@Test
	void transactionPostsToContextRoot() {
		var req = build(Map.of("resource", "{\"resourceType\":\"Bundle\",\"type\":\"transaction\"}"), Interaction.TRANSACTION, "/eps");

		assertThat(req.getMethod()).isEqualTo("POST");
		assertThat(req.getRequestURI()).isEqualTo("/eps/");
		assertThat(req.getContextPath()).isEqualTo("/eps");
	}

	@Test
	void trailingSlashAndNullContextPathAreNormalised() {
		assertThat(build(Map.of("resourceType", "Patient"), Interaction.SEARCH, "/eps/").getRequestURI())
				.isEqualTo("/eps/Patient");
		assertThat(build(Map.of("resourceType", "Patient"), Interaction.SEARCH, null).getRequestURI())
				.isEqualTo("/Patient");
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
