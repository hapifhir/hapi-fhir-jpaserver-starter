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
		int csvDataSize = csvLineContent.length;
		if(csvDataSize == 9) 
		{
			System.out.println("CSV Validation Success");
		}
		return true;
	}
	
	public static boolean validationHcwCsvLine(String[] hcwCsvData) {
		int csvDataSize = hcwCsvData.length;
		if(csvDataSize == 16)
		{
			System.out.println("HCW CSV Validate success");
		}
		return true;
	}
	
	public static String getPractitionerRoleIdByToken(String token) {
		try {
			String[] chunks = token.split("\\.");
			String payload = new String(Base64.decode(chunks[1], Base64.DEFAULT));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES , false);
			JWTPayload jwtPayload = mapper.readValue(payload, JWTPayload.class);
			return jwtPayload.getPractitionerRoleId();
		} catch (JsonProcessingException | IndexOutOfBoundsException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
}
