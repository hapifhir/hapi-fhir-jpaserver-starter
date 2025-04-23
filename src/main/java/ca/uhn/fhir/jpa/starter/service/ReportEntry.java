package ca.uhn.fhir.jpa.starter.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ReportEntry {
	private String date;
	private String state;
	private String lga;
	private String ward;
	private String facility;
	private Map<String, String> indicatorValues;
}
