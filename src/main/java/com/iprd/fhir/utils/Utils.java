package com.iprd.fhir.utils;

import com.iprd.report.FhirPath;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
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

	public static String getMd5StringFromFhirPath(FhirPath fhirPath) {
		String fhirPathString = "";
		if(fhirPath.getOperand().isEmpty()) {
			return md5Bytes(fhirPathString.getBytes(StandardCharsets.UTF_8));
		}
		if(fhirPath.getOperand().size() == 1) {
			return md5Bytes(fhirPath.getOperand().get(0).getBytes(StandardCharsets.UTF_8));
		}
		if(fhirPath.getOperator() == null && fhirPath.getOperand().size() > 1) {
			return md5Bytes(fhirPathString.getBytes(StandardCharsets.UTF_8));
		}
		fhirPathString = fhirPath.getOperator() + "," + String.join(",",fhirPath.getOperand());
		return md5Bytes(fhirPathString.getBytes(StandardCharsets.UTF_8));
	}
}
