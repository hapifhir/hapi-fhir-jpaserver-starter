package ca.uhn.fhir.jpa.starter.smart.exception;

public class InvalidSmartOperationException extends RuntimeException{

	public InvalidSmartOperationException(String message) {
		super(message);
	}
}
