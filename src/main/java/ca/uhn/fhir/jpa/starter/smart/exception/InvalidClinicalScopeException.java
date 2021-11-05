package ca.uhn.fhir.jpa.starter.smart.exception;

public class InvalidClinicalScopeException extends RuntimeException{

	public InvalidClinicalScopeException(String message) {
		super(message);
	}
}
