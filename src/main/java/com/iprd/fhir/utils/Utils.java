package com.iprd.fhir.utils;

import com.iprd.report.FhirPath;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Utils {

	public static String convertToTitleCaseSplitting(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		return Arrays
			.stream(text.split("_"))
			.map(word -> word.isEmpty()
				? word
				: Character.toTitleCase(word.charAt(0)) + word
				.substring(1)
				.toLowerCase())
			.collect(Collectors.joining(" "));
	}

	public static String md5Bytes(byte[] bytes){
		String digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(bytes);
			//converting byte array to Hexadecimal String
			StringBuilder sb = new StringBuilder(2*hash.length);
			for(byte b : hash){
				sb.append(String.format("%02x", b&0xff));
			}
			digest = sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return digest;
	}
	
	public static String getStringFromFhirPath(FhirPath fhirPath) {
		String fhirPathString = "";
		if(fhirPath.getOperand().isEmpty()) {
			return fhirPathString;
		}
		else if(fhirPath.getOperand().size() == 1) {
			return fhirPath.getOperand().get(0);
		}
		else if(fhirPath.getOperator() == null && fhirPath.getOperand().size() > 1) {
			throw new IllegalArgumentException("Multiple Operands passed without operator");
		}
		fhirPathString = fhirPath.getOperator() + "," + String.join(",",fhirPath.getOperand());
		return fhirPathString;
	}

	public static String getMd5StringFromFhirPath(FhirPath fhirPath) {
		String fhirPathString = getStringFromFhirPath(fhirPath);
		return md5Bytes(fhirPathString.getBytes(StandardCharsets.UTF_8));
	}
	
	public static String getMd5KeyForLineCacheMd5(FhirPath fhirPath, Integer lineId, Integer chartId) {
		String combinedString = getStringFromFhirPath(fhirPath)+"_"+lineId.toString()+"_"+chartId.toString();
		return  md5Bytes(combinedString.getBytes(StandardCharsets.UTF_8));
	}
}
