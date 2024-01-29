package ch.ahdis.matchbox.engine.exception;

/**
 * Exception thrown when Matchbox encounters an unsupported FHIR version.
 *
 * @author Quentin Ligier
 **/
public class MatchboxUnsupportedFhirVersionException extends RuntimeException {

  private static final long serialVersionUID = 7688818929276948718L;

  public MatchboxUnsupportedFhirVersionException(String message) {
	 super(message);
  }

  public MatchboxUnsupportedFhirVersionException(String message, Throwable cause) {
	 super(message, cause);
  }
}
