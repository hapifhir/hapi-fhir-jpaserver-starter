package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import ca.uhn.fhir.jpa.starter.util.EnvironmentHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/** Shared configuration for Elasticsearch */
@Configuration
public class ElasticsearchConfig {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ElasticsearchConfig.class);

	@Bean
	public ElasticsearchSvcImpl elasticsearchSvc(ConfigurableEnvironment configurableEnvironment) {
		if (EnvironmentHelper.isElasticsearchEnabled(configurableEnvironment)) {
			String elasticsearchUrl = EnvironmentHelper.getElasticsearchServerUrl(configurableEnvironment);
			if (elasticsearchUrl.startsWith("http")) {
				elasticsearchUrl = elasticsearchUrl.substring(elasticsearchUrl.indexOf("://") + 3);
			}
			String elasticsearchProtocol = EnvironmentHelper.getElasticsearchServerProtocol(configurableEnvironment);
			String elasticsearchUsername = EnvironmentHelper.getElasticsearchServerUsername(configurableEnvironment);
			String elasticsearchPassword = EnvironmentHelper.getElasticsearchServerPassword(configurableEnvironment);
			ourLog.info("Configuring elasticsearch {} {}", elasticsearchProtocol, elasticsearchUrl);
			return new ElasticsearchSvcImpl(
					elasticsearchProtocol, elasticsearchUrl, elasticsearchUsername, elasticsearchPassword);
		} else {
			return null;
		}
	}
}
