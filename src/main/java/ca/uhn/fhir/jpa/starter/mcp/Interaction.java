package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.rest.api.RequestTypeEnum;

public enum Interaction {
	CALL_CDS_HOOK("call-cds-hook"),
	SEARCH("search"),
	READ("read"),
	CREATE("create"),
	UPDATE("update"),
	DELETE("delete"),
	PATCH("patch"),
	CONDITIONAL_UPDATE("conditional-update"),
	CONDITIONAL_PATCH("conditional-patch"),
	TRANSACTION("transaction");

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
			case CREATE, TRANSACTION, CALL_CDS_HOOK -> RequestTypeEnum.POST;
			case UPDATE, CONDITIONAL_UPDATE -> RequestTypeEnum.PUT;
			case DELETE -> RequestTypeEnum.DELETE;
			case PATCH, CONDITIONAL_PATCH -> RequestTypeEnum.PATCH;
		};
	}
}
