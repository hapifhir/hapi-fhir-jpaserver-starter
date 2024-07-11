package ca.uhn.fhir.jpa.starter.anonymization;

public interface Anonymizer {

	Boolean isAnonymized(String token);

	Double anonymize(Double value, Double minPercent, Double maxPercent);
}
