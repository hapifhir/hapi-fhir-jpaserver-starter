package ca.uhn.fhir.jpa.starter;

import java.util.List;

import com.iprd.report.model.FilterItem;
import com.iprd.report.model.definition.ANCDailySummaryConfig;
import com.iprd.report.model.definition.BarChartDefinition;
import com.iprd.report.model.definition.IndicatorItem;
import com.iprd.report.model.definition.LineChart;
import com.iprd.report.model.definition.PieChartDefinition;
import com.iprd.report.model.definition.TabularItem;


public class DashboardConfigContainer {
	public List<FilterItem> getFilterItems() {
		return filterItems;
	}
	public void setFilterItems(List<FilterItem> filterItems) {
		this.filterItems = filterItems;
	}
	public List<BarChartDefinition> getBarChartDefinitions() {
		return barChartDefinitions;
	}
	public void setBarChartDefinitions(List<BarChartDefinition> barChartDefinitions) {
		this.barChartDefinitions = barChartDefinitions;
	}
	public List<PieChartDefinition> getPieChartDefinitions() {
		return pieChartDefinitions;
	}
	public void setPieChartDefinitions(List<PieChartDefinition> pieChartDefinitions) {
		this.pieChartDefinitions = pieChartDefinitions;
	}
	public List<LineChart> getLineCharts() {
		return lineCharts;
	}
	public void setLineCharts(List<LineChart> lineCharts) {
		this.lineCharts = lineCharts;
	}
	public List<TabularItem> getTabularItems() {
		return tabularItems;
	}
	public void setTabularItems(List<TabularItem> tabularItems) {
		this.tabularItems = tabularItems;
	}
	public List<IndicatorItem> getScoreCardIndicatorItems() {
		return scoreCardIndicatorItems;
	}
	public void setScoreCardIndicatorItems(List<IndicatorItem> scoreCardIndicatorItems) {
		this.scoreCardIndicatorItems = scoreCardIndicatorItems;
	}
	public List<IndicatorItem> getAnalyticsIndicatorItems() {
		return analyticsIndicatorItems;
	}
	public void setAnalyticsIndicatorItems(List<IndicatorItem> analyticsIndicatorItems) {
		this.analyticsIndicatorItems = analyticsIndicatorItems;
	}
	public ANCDailySummaryConfig getAncDailySummaryConfig() {
		return ancDailySummaryConfig;
	}
	public void setAncDailySummaryConfig(ANCDailySummaryConfig ancDailySummaryConfig) {
		this.ancDailySummaryConfig = ancDailySummaryConfig;
	}
	private List<FilterItem> filterItems;
	private List<BarChartDefinition> barChartDefinitions;
	private List<PieChartDefinition> pieChartDefinitions;
	private List<LineChart> lineCharts;
	private List<TabularItem> tabularItems;
	private List<IndicatorItem> scoreCardIndicatorItems;
	private List<IndicatorItem> analyticsIndicatorItems;
	private ANCDailySummaryConfig ancDailySummaryConfig;
}
