package ca.uhn.fhir.jpa.starter.service;

import java.util.List;
import java.util.Map;

public class DataResultJava {
	private String summaryResult;
	private List<Map<String,String>> dailyResult;

	private String categoryId;
	public DataResultJava(String categoryId,String summaryResult,List<Map<String,String>> dailyResult){
		this.summaryResult = summaryResult;
		this.dailyResult = dailyResult;
		this.categoryId = categoryId;
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

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
}

