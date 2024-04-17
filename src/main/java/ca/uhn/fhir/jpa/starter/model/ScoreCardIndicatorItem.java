package ca.uhn.fhir.jpa.starter.model;

import com.iprd.report.model.definition.IndicatorItem;

import java.util.List;

public class ScoreCardIndicatorItem {

	private String categoryId;

	private List<IndicatorItem> indicators;

	public ScoreCardIndicatorItem() {}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public List<IndicatorItem> getIndicators() {
		return indicators;
	}

	public void setIndicators(List<IndicatorItem> indicators) {
		this.indicators = indicators;
	}
	public ScoreCardIndicatorItem(String categoryId, List<IndicatorItem> indicators) {
		this.categoryId = categoryId;
		this.indicators = indicators;
	}
}
