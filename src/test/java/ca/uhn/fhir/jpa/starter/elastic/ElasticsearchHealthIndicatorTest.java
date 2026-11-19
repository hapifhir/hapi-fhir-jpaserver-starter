package ca.uhn.fhir.jpa.starter.elastic;

import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.io.IOException;
import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElasticsearchHealthIndicatorTest {

	private final RestClient myClient = mock(RestClient.class);

	@Test
	void greenClusterIsUp() throws IOException {
		respondWith(200, "{\"cluster_name\":\"docker-cluster\",\"status\":\"green\"}");

		Health health = new ElasticsearchHealthIndicator(myClient).health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("cluster_name", "docker-cluster");
	}

	@Test
	void redClusterIsOutOfService() throws IOException {
		respondWith(200, "{\"cluster_name\":\"docker-cluster\",\"status\":\"red\"}");

		Health health = new ElasticsearchHealthIndicator(myClient).health();

		assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
	}

	@Test
	void unexpectedStatusCodeIsDown() throws IOException {
		respondWith(204, "");

		Health health = new ElasticsearchHealthIndicator(myClient).health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("statusCode", 204);
	}

	@Test
	void unreachableClusterIsDown() throws IOException {
		when(myClient.performRequest(any(Request.class))).thenThrow(new ConnectException("Connection refused"));

		Health health = new ElasticsearchHealthIndicator(myClient).health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("error", "java.net.ConnectException: Connection refused");
	}

	private void respondWith(int theStatusCode, String theBody) throws IOException {
		StatusLine statusLine = mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(theStatusCode);
		Response response = mock(Response.class);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(new StringEntity(theBody, ContentType.APPLICATION_JSON));
		when(myClient.performRequest(any(Request.class))).thenReturn(response);
	}
}
