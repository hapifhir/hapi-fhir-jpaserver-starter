package ca.uhn.fhir.jpa.starter.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@TestConfiguration
public class TestElasticsearchClientConfig {

    @Bean(destroyMethod = "close")
    RestClient elasticRestClient(Environment environment) {
        String uri = environment.getProperty("spring.elasticsearch.uris");
        if (!StringUtils.hasText(uri)) {
            throw new IllegalStateException("spring.elasticsearch.uris must be set for tests");
        }

        HttpHost host = HttpHost.create(uri);
        RestClientBuilder builder = RestClient.builder(host);

        String username = environment.getProperty("spring.elasticsearch.username");
        String password = environment.getProperty("spring.elasticsearch.password");
        if (StringUtils.hasText(username)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        return builder.build();
    }

    @Bean(destroyMethod = "close")
    ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    @ConditionalOnMissingBean
    ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
