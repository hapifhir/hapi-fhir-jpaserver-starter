package ca.uhn.fhir.jpa.starter.smart.exceptions;

public class InvalidSmartOperationException extends RuntimeException{

	public InvalidSmartOperationException(String message) {
		super(message);
	}
}
