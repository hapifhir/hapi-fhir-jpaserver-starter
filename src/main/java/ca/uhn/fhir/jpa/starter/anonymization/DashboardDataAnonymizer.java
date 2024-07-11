package ca.uhn.fhir.jpa.starter.anonymization;

import com.iprd.fhir.utils.Validation;

public class DashboardDataAnonymizer implements Anonymizer  {
	@Override
	public Boolean isAnonymized(String token) {
		Boolean isAnonymizationEnabled = Validation.getJWTToken(token).getAnonymization();
		if (isAnonymizationEnabled != null)
			return isAnonymizationEnabled;
		else
			return false;
	}

	@Override
	public Double anonymize(Double value, Double minPercent, Double maxPercent) {
		try {
			double noisePercent = Randomizer.getRandom(minPercent, maxPercent);
			double noiseValue = (value * noisePercent / 100);
			double anonymizedValue = value + noiseValue;
			return (double) Math.round(anonymizedValue);
		} catch (ArithmeticException e) {
			e.printStackTrace();
			return value;
		}
	}
}
