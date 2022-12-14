package ca.uhn.fhir.jpa.starter.model;

public class AnalyticItem {

	private String title;
	private String value;
	private AnalyticComparison comparison_value;

	public AnalyticItem() {}

	public AnalyticItem(String title, String value, AnalyticComparison comparison_value) {
		this.title = title;
		this.value = value;
		this.comparison_value = comparison_value;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public AnalyticComparison getComparison_value() {
		return comparison_value;
	}

	public void setComparison_value(AnalyticComparison comparison_value) {
		this.comparison_value = comparison_value;
	}
}

