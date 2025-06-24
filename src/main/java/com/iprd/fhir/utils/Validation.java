package com.iprd.fhir.utils;

import android.util.Base64;
import ca.uhn.fhir.jpa.starter.model.JWTPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation {

	private static final Logger logger = LoggerFactory.getLogger(Validation.class);
	
	public static boolean validateClinicAndStateCsvLine(String[] csvLineContent) {
		return csvLineContent.length == 15;
	}
	
	public static boolean validationHcwCsvLine(String[] hcwCsvData) {
		return hcwCsvData.length == 18;
	}

	public static boolean validationDashboardUserCsvLine(String[] hcwCsvData) {
		return hcwCsvData.length == 13;
	}

	public static boolean validateEmailScheduleCsvLine(String[] data) {
		if (data == null) {
			logger.warn("Invalid CSV line: Data is null");
			return false;
		}
		// 5 columns expected: recipientEmail,scheduleType,emailSubject,orgId,adminOrg
		if (data.length != 5) {
			logger.warn("Invalid CSV line: Expected 5 columns (recipientEmail,scheduleType,emailSubject,orgId,adminOrg), got {}", data.length);
			return false;
		}
		// Basic validation for non-empty values
		for (int i = 0; i < data.length; i++) {
			if (data[i] == null || data[i].trim().isEmpty()) {
				logger.warn("Invalid CSV line: Column {} is empty or null", i);
				return false;
			}
		}
		return true;
	}

	public static JWTPayload getJWTToken(String token) {
		try {
			String[] chunks = token.split("\\.");
			String payload = new String(Base64.decode(chunks[1], Base64.DEFAULT));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES , false);
			JWTPayload jwtPayload = mapper.readValue(payload, JWTPayload.class);
			return jwtPayload;
		} catch (JsonProcessingException | IndexOutOfBoundsException | NullPointerException e) {
//			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
}
