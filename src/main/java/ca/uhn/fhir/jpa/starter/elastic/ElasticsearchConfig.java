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
import org.elasticsearch.client.sniff.Sniffer;
import org.elasticsearch.client.sniff.SnifferBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;

/**
 * Custom Elasticsearch configuration that creates the ElasticsearchClient bean
 * without the sniffer. This is used when the default Spring Boot autoconfiguration
 * ({@code ElasticsearchRestClientAutoConfiguration}) is excluded.
 *
 * <p>That autoconfiguration is what enables Elasticsearch node sniffing: in Spring Boot 3
 * its {@code RestClientSnifferConfiguration} unconditionally registers a {@code Sniffer}
 * bean whenever the {@code elasticsearch-rest-client-sniffer} JAR is on the classpath (it
 * arrives transitively via hapi-fhir-jpaserver-base). The sniffer periodically queries
 * {@code _nodes/http} and then tries to talk to the node publish addresses directly, which
 * fails (repeated {@code ConnectException}) whenever ES sits behind a proxy/LB/k8s.
 *
 * <p>Because that autoconfiguration also provides the {@link ElasticsearchProperties} bean
 * via {@code @EnableConfigurationProperties}, we re-declare it here so this config keeps
 * working while the autoconfiguration stays excluded (see application-elastic.yaml).
 *
 * <p>Node sniffing is therefore off by default. If it is actually wanted it can be turned on
 * explicitly via {@code spring.elasticsearch.restclient.sniffer.enabled=true} (see
 * {@link #elasticsearchSniffer}). Note this only affects the {@link RestClient} used here
 * (i.e. {@link ElasticsearchBootSvcImpl}); Hibernate Search uses its own client and is
 * controlled separately via {@code hibernate.search.backend.discovery.enabled}.
 */
@Configuration
@Conditional(ElasticConfigCondition.class)
@EnableConfigurationProperties(ElasticsearchProperties.class)
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

	/**
	 * Optional Elasticsearch node sniffer, disabled by default. Enable with
	 * {@code spring.elasticsearch.restclient.sniffer.enabled=true}; the interval and
	 * delay-after-failure reuse the standard {@code spring.elasticsearch.restclient.sniffer.*}
	 * properties. Registered as a bean so it is closed on context shutdown.
	 */
	@Bean(destroyMethod = "close")
	@ConditionalOnProperty(prefix = "spring.elasticsearch.restclient.sniffer", name = "enabled", havingValue = "true")
	public Sniffer elasticsearchSniffer(RestClient restClient, ElasticsearchProperties properties) {
		ElasticsearchProperties.Restclient.Sniffer sniffer =
				properties.getRestclient().getSniffer();
		SnifferBuilder builder = Sniffer.builder(restClient);
		if (sniffer.getInterval() != null) {
			builder.setSniffIntervalMillis((int) sniffer.getInterval().toMillis());
		}
		if (sniffer.getDelayAfterFailure() != null) {
			builder.setSniffAfterFailureDelayMillis(
					(int) sniffer.getDelayAfterFailure().toMillis());
		}
		return builder.build();
	}
}
