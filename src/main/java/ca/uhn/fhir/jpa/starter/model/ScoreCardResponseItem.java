package ca.uhn.fhir.jpa.starter.model;

import com.iprd.report.model.data.ScoreCardItem;

import java.util.List;

public class ScoreCardResponseItem {
	private String categoryId;

	private List<ScoreCardItem> scoreCardItemList;

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public List<ScoreCardItem> getScoreCardItemList() {
		return scoreCardItemList;
	}

	public void setScoreCardItemList(List<ScoreCardItem> scoreCardItemList) {
		this.scoreCardItemList = scoreCardItemList;
	}
}
