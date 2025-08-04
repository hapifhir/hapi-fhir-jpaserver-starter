package ca.uhn.fhir.jpa.starter.mcp;

public enum Interaction {
	SEARCH("search"),
	READ("read"),
	CREATE("create"),
	UPDATE("update"),
	DELETE("delete"),
	PATCH("patch");

	private final String name;

	Interaction(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static Interaction fromString(String s) {
		for (Interaction i : values()) {
			if (i.name.equalsIgnoreCase(s)) return i;
		}
		throw new IllegalArgumentException("Unknown interaction: " + s);
	}
}
