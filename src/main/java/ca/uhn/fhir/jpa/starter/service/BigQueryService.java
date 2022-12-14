package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.model.AnalyticComparison;
import ca.uhn.fhir.jpa.starter.model.AnalyticItem;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;

import ca.uhn.fhir.jpa.starter.AppProperties;

import com.iprd.fhir.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Import(AppProperties.class)
@Service
public class BigQueryService {
	
	@Autowired
	  AppProperties appProperties;

	public List<AnalyticItem> timeSpentOnScreenAnalyticItems() {
		List<AnalyticItem> timeAnalyticItems = new ArrayList<>();
		try {
			GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(appProperties.getGcp_credential_file_path()));
			BigQuery bigQuery = BigQueryOptions.newBuilder().setCredentials(credentials).build().getService();
			String getScreenViewQuery = getSQLQueryStringFromFile(appProperties.getSql_screen_time_file_path());
			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(getScreenViewQuery).build();
			Job queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).build());

			queryJob = queryJob.waitFor();
			if (queryJob == null) {
				return null;
			}
			if (queryJob.getStatus().getError() != null) {
				return null;
			}
			TableResult result = queryJob.getQueryResults();

			for (FieldValueList row : result.iterateAll()) {
				AnalyticItem analyticItem = new AnalyticItem();
				analyticItem.setTitle(getTitleFromScreenName(row.get("screen").getStringValue()));
				analyticItem.setComparison_value(row.get("avg_time_comparison").getBooleanValue() ? AnalyticComparison.POSITIVE_DOWN : AnalyticComparison.NEGATIVE_UP);
				analyticItem.setValue(convertMilliSecondsToMinutes(row.get("avg_time_spent").getDoubleValue()));
				timeAnalyticItems.add(analyticItem);
			}
			return timeAnalyticItems;
		} catch (InterruptedException | IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private String getTitleFromScreenName(String screenName) {
		return "Average time spent per patient for "+ Utils.convertToTitleCaseSplitting(screenName) +" this week";
	}

	private String convertMilliSecondsToMinutes(Double milliseconds) {
		int seconds = (int) Math.floor((milliseconds / 1000) % 60);
		int minutes = (int) Math.floor((milliseconds / 1000 / 60) % 60);
		return minutes + " min " + seconds + " secs";
	}

	private String getSQLQueryStringFromFile(String filePath) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = bufferedReader.readLine()) != null)
		{
			stringBuilder.append(line);
		}
		return stringBuilder.toString();
	}
}
