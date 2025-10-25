package ca.uhn.fhir.jpa.starter.mcp;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides descriptive metadata for the standard FHIR search parameter types.
 */
public final class SearchTypeDescriptions {

	private record Entry(
			String code, String argumentFormat, boolean allowsPrefixes, String definition, String usageExamples) {

		private String formatted() {
			StringBuilder builder = new StringBuilder();
			builder.append(code);
			if (!argumentFormat.isBlank()) {
				builder.append(": ").append(argumentFormat);
			}
			builder.append("\n* Allows prefixes: ").append(allowsPrefixes ? "Yes" : "No");
			if (!definition.isBlank()) {
				builder.append("\n* Definition: ").append(definition);
			}
			if (!usageExamples.isBlank()) {
				builder.append("\n* Examples:\n").append(usageExamples);
			}
			return builder.toString();
		}
	}

	private static final Map<String, Entry> ENTRIES = Map.ofEntries(
			Map.entry(
					"number",
					new Entry(
							"number",
							"decimal",
							true,
							"Numeric search parameter supporting equality and prefix comparisons against integer or decimal values.",
							"GET [base]/Observation?value-quantity=gt5.4\nGET [base]/MedicationStatement?quantity=2")),
			Map.entry(
					"date",
					new Entry(
							"date",
							"yyyy-mm-ddThh:mm:ss[Z|(+|-)hh:mm]",
							true,
							"Date or dateTime search parameter. Supports comparisons across ranges using prefixes (eq, gt, lt, ge, le, sa, eb, ap).",
							"GET [base]/Encounter?date=ge2023-01-01\nGET [base]/Observation?date=ap2023-03-01")),
			Map.entry(
					"string",
					new Entry(
							"string",
							"Plain text",
							false,
							"Case-insensitive string search. Matches on whole words and prefixes unless modifiers such as :exact or :contains are used.",
							"GET [base]/Patient?family=smith\nGET [base]/Patient?given:contains=ann")),
			Map.entry(
					"token",
					new Entry(
							"token",
							"system|code or code",
							false,
							"Token search across coded elements or identifiers. Supports matching on system|code combinations or text.",
							"GET [base]/Patient?identifier=http://example.org|12345\nGET [base]/Observation?code=http://loinc.org|8480-6")),
			Map.entry(
					"reference",
					new Entry(
							"reference",
							"[type/]id or absolute URL",
							false,
							"Reference to another resource by literal reference, identifier, or canonical URL.",
							"GET [base]/Observation?subject=Patient/123\nGET [base]/Observation?encounter=Encounter/456")),
			Map.entry(
					"composite",
					new Entry(
							"composite",
							"[component1]$[component2]$...",
							false,
							"Combines multiple other search parameters into a single query value. All component constraints must match the same resource repetition.",
							"GET [base]/Observation?component-code-value-quantity=http://loinc.org|8480-6$lt60\nGET [base]/DiagnosticReport?result.code-value-quantity=http://loinc.org|2823-3$gt5.4|http://unitsofmeasure.org|mmol/L")),
			Map.entry(
					"quantity",
					new Entry(
							"quantity",
							"number|system|code",
							true,
							"Quantity search that supports units via UCUM system/code tuples as well as numeric prefixes.",
							"GET [base]/Observation?value-quantity=5.4|http://unitsofmeasure.org|mg\nGET [base]/MedicationRequest?dose-quantity=le2.5|http://unitsofmeasure.org|mg")),
			Map.entry(
					"uri",
					new Entry(
							"uri",
							"Absolute or relative URI",
							false,
							"URI search parameter that matches canonical or literal URIs. Useful for profile, capability or concept URLs.",
							"GET [base]/StructureDefinition?url=https://example.org/StructureDefinition/my-profile\nGET [base]/Observation?code=http://loinc.org")),
			Map.entry(
					"special",
					new Entry(
							"special",
							"Server defined",
							false,
							"Special search parameter type used for complex cases where matching rules are defined by the server.",
							"Usage depends on the individual server and parameter definition.")));

	private SearchTypeDescriptions() {}

	public static List<String> codes() {
		return ENTRIES.keySet().stream().sorted().collect(Collectors.toList());
	}

	public static Optional<String> describe(String code) {
		if (code == null) {
			return Optional.empty();
		}
		Entry entry = ENTRIES.get(code.toLowerCase(Locale.ROOT));
		return Optional.ofNullable(entry).map(Entry::formatted);
	}

	public static List<String> formattedEntries() {
		return ENTRIES.values().stream()
				.sorted(Comparator.comparing(Entry::code))
				.map(Entry::formatted)
				.collect(Collectors.toUnmodifiableList());
	}
}
