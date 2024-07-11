package ca.uhn.fhir.jpa.starter.anonymization;

public class AnonymizerContext {
	DashboardDataAnonymizer dashboardDataAnonymizer = new DashboardDataAnonymizer();

	public Boolean isAnonymized(String token){
		return dashboardDataAnonymizer.isAnonymized(token);
	}

	public Double anonymize(Boolean isAnonymizationEnabled, Double value,Double minPercent,Double maxPercent){
		if (isAnonymizationEnabled)
			return dashboardDataAnonymizer.anonymize(value, minPercent, maxPercent);
		return value;
	}
}
