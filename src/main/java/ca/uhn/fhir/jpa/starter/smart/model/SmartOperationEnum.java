package ca.uhn.fhir.jpa.starter.smart.model;

import ca.uhn.fhir.jpa.starter.smart.exception.InvalidSmartOperationException;

import java.util.Arrays;

public enum SmartOperationEnum {
	WRITE("write"),
	READ("read"),
	ALL("*");

	private final String operation;

	SmartOperationEnum(final String operation) {
		this.operation = operation;
	}

	public static SmartOperationEnum findByValue(String value) {
		return Arrays.stream(SmartOperationEnum.values()).filter(smartOperationEnum -> smartOperationEnum.operation.equalsIgnoreCase(value)).findFirst().orElseThrow(() ->new InvalidSmartOperationException(value+" is not a legal operation"));
	}

	public String getOperation() {
		return operation;
	}
}
