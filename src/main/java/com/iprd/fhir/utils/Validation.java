package com.iprd.fhir.utils;

public class Validation {
	
	public static boolean validateClinicAndStateCsvLine(String[] csvLineContent) {
		int csvDataSize = csvLineContent.length;
		if(csvDataSize == 9) 
		{
			System.out.println("Validation Success");
		}
		return true;
	}
}
