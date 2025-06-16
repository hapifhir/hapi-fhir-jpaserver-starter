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
			return false;
		}
		// 4 columns for both bulk import and update: recipientEmail,scheduleType,emailSubject,orgId
		return data.length == 4;
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
