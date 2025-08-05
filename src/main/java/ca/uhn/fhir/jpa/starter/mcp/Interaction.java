package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.rest.api.RequestTypeEnum;

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

	public RequestTypeEnum asRequestType() {
		return switch (this) {
			case SEARCH, READ -> RequestTypeEnum.GET;
			case CREATE -> RequestTypeEnum.POST;
			case UPDATE -> RequestTypeEnum.PUT;
			case DELETE -> RequestTypeEnum.DELETE;
			case PATCH -> RequestTypeEnum.PATCH;
		};
	}

	public static Interaction fromString(String s) {
		for (Interaction i : values()) {
			if (i.name.equalsIgnoreCase(s)) return i;
		}
		throw new IllegalArgumentException("Unknown interaction: " + s);
	}
}
