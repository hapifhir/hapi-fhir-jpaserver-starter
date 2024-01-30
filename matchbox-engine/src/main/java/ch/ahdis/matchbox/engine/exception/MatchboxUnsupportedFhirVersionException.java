package ch.ahdis.matchbox.engine.exception;

import ca.uhn.fhir.context.FhirVersionEnum;

/**
 * Exception thrown when Matchbox encounters an unsupported FHIR version.
 *
 * @author Quentin Ligier
 **/
public class MatchboxUnsupportedFhirVersionException extends RuntimeException {

	private static final long serialVersionUID = 7688818929276948718L;

	public MatchboxUnsupportedFhirVersionException(final String source, final FhirVersionEnum version) {
		super(String.format("Unsupported FHIR version %s in %s", version, source));
	}
}
