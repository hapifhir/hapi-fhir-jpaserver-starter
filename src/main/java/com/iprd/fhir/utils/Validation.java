package com.iprd.fhir.utils;

public class Validation {
	
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
}
