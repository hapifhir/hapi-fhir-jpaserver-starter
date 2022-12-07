package ca.uhn.fhir.jpa.starter.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;

import ca.uhn.fhir.jpa.starter.AppProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Import(AppProperties.class)
@Service
public class BigQueryService {
	
	@Autowired
	  AppProperties appProperties;

	public ResponseEntity<?> timeSpentOnScreen() {
		try {
			GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(appProperties.getGcp_credential_file_path()));
			BigQuery bigQuery = BigQueryOptions.newBuilder().setCredentials(credentials).build().getService();
			String getScreenViewQuery = getSQLQueryStringFromFile(appProperties.getSql_screen_time_file_path());
			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(getScreenViewQuery).build();
			Job queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).build());
			List<LinkedHashMap<String, Object>> response = new ArrayList<>();
			queryJob = queryJob.waitFor();
			if (queryJob == null) {
				return ResponseEntity.ok("Error: Job no longer exists");
			}
			if (queryJob.getStatus().getError() != null) {
				return ResponseEntity.ok("Error: Job no longer exists");
			}
			TableResult result = queryJob.getQueryResults();

			for (FieldValueList row : result.iterateAll()) {
				LinkedHashMap<String, Object> map = new LinkedHashMap<>();
				map.put("screen", row.get("screen").getStringValue());
				map.put("min_time_spent", row.get("min_time_spent").getLongValue());
				map.put("max_time_spent", row.get("max_time_spent").getLongValue());
				map.put("avg_time_spent", row.get("avg_time_spent").getDoubleValue());
				map.put("avg_time_comparison", row.get("avg_time_comparison").getDoubleValue());
				response.add(map);
			}
			return ResponseEntity.ok(response);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			return ResponseEntity.ok("Error: Unable to fetch screen view information");
		} catch (IOException ex) {
			ex.printStackTrace();
			return ResponseEntity.ok("Error: Unable to find file");
		}
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
