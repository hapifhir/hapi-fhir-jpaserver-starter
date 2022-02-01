package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/** Shared configuration for Elasticsearch */
@Configuration
public class ElasticsearchConfig {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ElasticsearchConfig.class);

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Bean()
	public ElasticsearchSvcImpl elasticsearchSvc() {
		if (EnvironmentHelper.isElasticsearchEnabled(configurableEnvironment)) {
			String elasticsearchUrl = EnvironmentHelper.getElasticsearchServerUrl(configurableEnvironment);
			if (elasticsearchUrl.startsWith("http")) {
				elasticsearchUrl =elasticsearchUrl.substring(elasticsearchUrl.indexOf("://") + 3);
			}
			String elasticsearchProtocol = EnvironmentHelper.getElasticsearchServerProtocol(configurableEnvironment);
			String elasticsearchUsername = EnvironmentHelper.getElasticsearchServerUsername(configurableEnvironment);
			String elasticsearchPassword = EnvironmentHelper.getElasticsearchServerPassword(configurableEnvironment);
			ourLog.info("Configuring elasticsearch {} {}", elasticsearchProtocol, elasticsearchUrl);
			return new ElasticsearchSvcImpl(elasticsearchProtocol, elasticsearchUrl, elasticsearchUsername, elasticsearchPassword);
		} else {
			return null;
		}
	}
}
