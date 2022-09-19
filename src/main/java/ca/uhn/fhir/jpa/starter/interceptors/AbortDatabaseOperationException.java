package ca.uhn.fhir.jpa.starter.interceptors;

public class AbortDatabaseOperationException extends RuntimeException {
  public AbortDatabaseOperationException() {}
}
