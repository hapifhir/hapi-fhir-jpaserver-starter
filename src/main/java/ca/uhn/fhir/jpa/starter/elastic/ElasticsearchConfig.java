package ca.uhn.fhir.jpa.starter.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;

/**
 * Custom Elasticsearch configuration that creates the ElasticsearchClient bean
 * without the sniffer. This is used when the default Spring Boot autoconfiguration
 * is excluded.
 */
@Configuration
@Conditional(ElasticConfigCondition.class)
public class ElasticsearchConfig {

	@Bean
	public RestClient elasticsearchRestClient(ElasticsearchProperties properties) {
		List<String> uris = properties.getUris();

		HttpHost[] hosts = uris.stream()
				.map(URI::create)
				.map(uri -> new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()))
				.toArray(HttpHost[]::new);

		RestClientBuilder builder = RestClient.builder(hosts);

		// Configure authentication if credentials are provided
		if (properties.getUsername() != null && properties.getPassword() != null) {
			BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(
					AuthScope.ANY, new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));

			builder.setHttpClientConfigCallback(
					httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
		}

		// Configure connection and socket timeouts if needed
		builder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
				.setConnectTimeout(
						properties.getConnectionTimeout() != null
								? (int) properties.getConnectionTimeout().toMillis()
								: 5000)
				.setSocketTimeout(
						properties.getSocketTimeout() != null
								? (int) properties.getSocketTimeout().toMillis()
								: 60000));

		return builder.build();
	}

	@Bean
	public ElasticsearchClient elasticsearchClient(RestClient restClient) {
		RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		return new ElasticsearchClient(transport);
	}
}
