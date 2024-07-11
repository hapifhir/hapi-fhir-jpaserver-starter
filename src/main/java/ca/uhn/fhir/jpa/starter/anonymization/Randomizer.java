package ca.uhn.fhir.jpa.starter.anonymization;

import java.util.Random;

public class Randomizer {

	static Double getRandom(Double min, Double max) {
		Random random = new Random();
		double randomValue = random.nextDouble();

		// Randomly decide whether to add or subtract
		if (random.nextBoolean()) {
			// Try to subtract
			double result = min + (max - min) * randomValue - randomValue * (max - min);
			// Ensure the result is not negative
			if (result >= 0) {
				return result;
			}
		}

		// If subtraction is not possible or not chosen, add
		return min + (max - min) * randomValue + randomValue * (max - min);
	}
}
