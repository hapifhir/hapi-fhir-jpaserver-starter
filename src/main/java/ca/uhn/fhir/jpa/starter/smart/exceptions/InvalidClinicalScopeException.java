package ca.uhn.fhir.jpa.starter.smart.exceptions;

public class InvalidClinicalScopeException extends RuntimeException{

	public InvalidClinicalScopeException(String message) {
		super(message);
	}
}
