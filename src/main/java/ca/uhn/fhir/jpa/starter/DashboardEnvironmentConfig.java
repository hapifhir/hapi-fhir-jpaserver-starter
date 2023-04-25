package ca.uhn.fhir.jpa.starter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.jpa.starter.service.BigQueryService;
import com.iprd.report.model.data.BarChartItemDataCollection;
import com.iprd.report.model.data.BarComponentData;
import com.iprd.report.model.data.LineChartItem;
import com.iprd.report.model.data.LineChartItemCollection;
import com.iprd.report.model.data.PieChartItem;
import com.iprd.report.model.data.ScoreCardItem;
import com.iprd.report.model.definition.BarChartDefinition;
import com.iprd.report.model.definition.BarChartItemDefinition;
import com.iprd.report.model.definition.BarComponent;
import com.iprd.report.model.definition.IndicatorItem;
import com.iprd.report.model.definition.LineChart;
import com.iprd.report.model.definition.LineChartItemDefinition;
import com.iprd.report.model.definition.PieChartDefinition;
import com.iprd.report.model.definition.TabularItem;
import com.iprd.report.model.FilterItem;
import com.iprd.report.model.FilterOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.iprd.report.model.definition.ANCDailySummaryConfig;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(AppProperties.class)
@Configuration
public class DashboardEnvironmentConfig {

	private static final Logger logger = LoggerFactory.getLogger(DashboardEnvironmentConfig.class);

	@Autowired
	AppProperties appProperties;

	@Bean
	public Map<String, Map<ConfigDefinitionTypes, String>> getEnvToFilePathMapping() {
		Map<String, Map<ConfigDefinitionTypes, String>> directoryToFilesMap = new HashMap<>();
		File[] directories = new File(appProperties.getEnvs()).listFiles(File::isDirectory);
		if (directories != null) {
			for (File directory : directories) {
				Map<ConfigDefinitionTypes, String> fileNameToPathMap = new HashMap<>();
				File[] files = new File(directory.getAbsolutePath()).listFiles(File::isFile);
				if (files != null) {
					for (File file : files) {
						try {
							ConfigDefinitionTypes configDefinitionTypes = ConfigDefinitionTypes.valueOf(FilenameUtils.removeExtension(file.getName()));
							fileNameToPathMap.put(configDefinitionTypes, file.getAbsolutePath());
						} catch (IllegalArgumentException exception) {
							logger.warn(ExceptionUtils.getStackTrace(exception));
						}

					}
				}
				directoryToFilesMap.put(directory.getName(), fileNameToPathMap);
			}
		}
		return directoryToFilesMap;
	}

	@Bean
	public Map<String, DashboardConfigContainer> getDashboardEnvToConfigMap() {
		Map<String, DashboardConfigContainer> dashboardEnvToConfigMap = new HashMap<>();
		getEnvToFilePathMapping().forEach((env, definitionTypeToFilePathMap) -> {
			DashboardConfigContainer envConfigContainer = new DashboardConfigContainer();
			definitionTypeToFilePathMap.forEach((configType, filePath) -> {
				JsonReader reader;
				try {
					reader = new JsonReader(new FileReader(filePath));
					switch (configType) {
						case FILTER_DEFINITIONS: {
							envConfigContainer.setFilterItems(new Gson().fromJson(reader, new TypeToken<List<FilterItem>>() {
							}.getType()));
							break;
						}
						case SCORECARD_DEFINITIONS: {
							envConfigContainer.setScoreCardIndicatorItems(new Gson().fromJson(reader, new TypeToken<List<IndicatorItem>>() {
							}.getType()));
							break;
						}
						case ANALYTIC_DEFINITIONS: {
							envConfigContainer.setAnalyticsIndicatorItems(new Gson().fromJson(reader, new TypeToken<List<IndicatorItem>>() {
							}.getType()));
							break;
						}
						case LINECHART_DEFINITIONS: {
							envConfigContainer.setLineCharts(new Gson().fromJson(reader, new TypeToken<List<LineChart>>() {
							}.getType()));
							break;
						}
						case PIECHART_DEFINITIONS: {
							envConfigContainer.setPieChartDefinitions(new Gson().fromJson(reader, new TypeToken<List<PieChartDefinition>>() {
							}.getType()));
							break;
						}
						case BARCHART_DEFINITIONS: {
							envConfigContainer.setBarChartDefinitions(new Gson().fromJson(reader, new TypeToken<List<BarChartDefinition>>() {
							}.getType()));
							break;
						}
						case TABULARCHART_DEFINITIONS: {
							envConfigContainer.setTabularItems(new Gson().fromJson(reader, new TypeToken<List<TabularItem>>() {
							}.getType()));
							break;
						}
						case DAILY_SUMMARY_DEFINITIONS: {
							envConfigContainer.setAncDailySummaryConfig(new Gson().fromJson(reader, ANCDailySummaryConfig.class));
							break;
						}
					}
				} catch (FileNotFoundException e) {
					logger.warn(ExceptionUtils.getStackTrace(e));
				}
			});
			dashboardEnvToConfigMap.put(env, envConfigContainer);
		});
		return dashboardEnvToConfigMap;
	}

}
