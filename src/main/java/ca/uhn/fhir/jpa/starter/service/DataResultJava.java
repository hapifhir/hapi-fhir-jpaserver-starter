package ca.uhn.fhir.jpa.starter.service;

import java.util.List;
import java.util.Map;

public class DataResultJava {
	private String summaryResult;
	private List<Map<String,String>> dailyResult;

	public DataResultJava(String summaryResult,List<Map<String,String>> dailyResult){
		this.summaryResult = summaryResult;
		this.dailyResult = dailyResult;
	}
	public String getSummaryResult() {
		return summaryResult;
	}

	public void setSummaryResult(String summaryResult) {
		this.summaryResult = summaryResult;
	}

	public List<Map<String, String>> getDailyResult() {
		return dailyResult;
	}

	public void setDailyResult(List<Map<String, String>> dailyResult) {
		this.dailyResult = dailyResult;
	}


}
