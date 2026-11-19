package ca.uhn.fhir.jpa.starter.elastic;

import org.apache.http.HttpStatus;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Reports Elasticsearch in /actuator/health through the 8.x {@link RestClient} built by {@link ElasticsearchConfig}.
 * Spring Boot's own indicator only works with the Rest5Client of its auto-configuration, which the starter excludes.
 * Mirrors Spring Boot's ElasticsearchRestClientHealthIndicator: a red cluster is OUT_OF_SERVICE, any other cluster
 * status is UP, and a failed request is DOWN.
 */
public class ElasticsearchHealthIndicator extends AbstractHealthIndicator {

	private static final String RED_STATUS = "red";

	private final RestClient myClient;

	private final JsonParser myJsonParser;

	public ElasticsearchHealthIndicator(RestClient theClient) {
		super("Elasticsearch health check failed");
		myClient = theClient;
		myJsonParser = JsonParserFactory.getJsonParser();
	}

	@Override
	protected void doHealthCheck(Health.Builder theBuilder) throws Exception {
		Response response = myClient.performRequest(new Request("GET", "/_cluster/health/"));
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			theBuilder.down();
			theBuilder.withDetail("statusCode", response.getStatusLine().getStatusCode());
			theBuilder.withDetail("warnings", response.getWarnings());
			return;
		}
		try (InputStream inputStream = response.getEntity().getContent()) {
			Map<String, Object> clusterHealth =
					myJsonParser.parseMap(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
			theBuilder.status(RED_STATUS.equals(clusterHealth.get("status")) ? Status.OUT_OF_SERVICE : Status.UP);
			theBuilder.withDetails(clusterHealth);
		}
	}
}
