package ca.uhn.fhir.jpa.starter.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;

import ca.uhn.fhir.jpa.starter.AppProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Import(AppProperties.class)
@Service
public class BigQueryService {
	
	@Autowired
	  AppProperties appProperties;

	public ResponseEntity<List<LinkedHashMap<String, Object>>> timeSpentOnScreen() throws Exception {

		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(appProperties.getGcp_credential_file_path()));
		BigQuery bigQuery = BigQueryOptions.newBuilder().setCredentials(credentials).build().getService();
		final String GET_SCREEN_VIEW_INFO =
			"WITH screenView AS" +
				"  (" +
				"    SELECT event_timestamp AS event_timestamp," +
				"    (SELECT value.string_value FROM UNNEST(event_params) WHERE key=\"firebase_screen\") AS screen," +
				"    (SELECT value.int_value FROM UNNEST(event_params) WHERE key=\"engagement_time_msec\") AS time_spent\n" +
				"    FROM `"+appProperties.getGcp_event_table_name()+"` WHERE event_name=\"screen_view\" AND app_info.id=\"com.iprd.anc.nigeriaoyo\"" +
				"  )" +
				"  SELECT screen,MIN(time_spent) AS min_time_spent,MAX(time_spent)AS max_time_spent,ROUND(AVG(time_spent),2) AS avg_time_spent " +
				"  FROM screenView " +
				"  WHERE time_spent IS NOT NULL AND screen IS NOT NULL" +
				"  GROUP BY screen;";
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(GET_SCREEN_VIEW_INFO).build();
		Job queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).build());
		queryJob = queryJob.waitFor();
		if (queryJob == null) {
			throw new Exception("Job no longer exists");
		}
		if (queryJob.getStatus().getError() != null) {
			throw new Exception(queryJob.getStatus().getError().toString());
		}
		List<LinkedHashMap<String, Object>> response = new ArrayList<>();
		TableResult result = queryJob.getQueryResults();

		for (FieldValueList row : result.iterateAll()) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<>();
			map.put("screen", row.get("screen").getStringValue());
			map.put("min_time_spent", row.get("min_time_spent").getLongValue());
			map.put("max_time_spent", row.get("max_time_spent").getLongValue());
			map.put("avg_time_spent", row.get("avg_time_spent").getDoubleValue());
			response.add(map);
		}
		return ResponseEntity.ok(response);
	}
}
