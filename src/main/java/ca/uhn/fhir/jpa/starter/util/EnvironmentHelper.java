package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.jpa.config.HapiFhirLocalContainerEntityManagerFactoryBean;
import ca.uhn.fhir.jpa.search.HapiHSearchAnalysisConfigurers;
import ca.uhn.fhir.jpa.search.elastic.ElasticsearchHibernatePropertiesBuilder;
import org.apache.lucene.util.Version;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.search.backend.elasticsearch.cfg.ElasticsearchBackendSettings;
import org.hibernate.search.backend.elasticsearch.index.IndexStatus;
import org.hibernate.search.backend.lucene.cfg.LuceneBackendSettings;
import org.hibernate.search.backend.lucene.cfg.LuceneIndexSettings;
import org.hibernate.search.backend.lucene.lowlevel.directory.impl.LocalFileSystemDirectoryProvider;
import org.hibernate.search.engine.cfg.BackendSettings;
import org.hibernate.search.mapper.orm.automaticindexing.session.AutomaticIndexingSynchronizationStrategyNames;
import org.hibernate.search.mapper.orm.cfg.HibernateOrmMapperSettings;
import org.hibernate.search.mapper.orm.schema.management.SchemaManagementStrategyName;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Objects.requireNonNullElse;

public class EnvironmentHelper {

	public static Properties getHibernateProperties(
			ConfigurableEnvironment environment, ConfigurableListableBeanFactory myConfigurableListableBeanFactory) {
		Properties properties = new Properties();
		Map<String, Object> jpaProps = getPropertiesStartingWith(environment, "spring.jpa.properties");
		for (Map.Entry<String, Object> entry : jpaProps.entrySet()) {
			String strippedKey = entry.getKey().replace("spring.jpa.properties.", "");
			properties.put(strippedKey, entry.getValue().toString());
		}

		// Spring Boot Autoconfiguration defaults
		properties.putIfAbsent(AvailableSettings.SCANNER, "org.hibernate.boot.archive.scan.internal.DisabledScanner");
		properties.putIfAbsent(
				AvailableSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
		properties.putIfAbsent(
				AvailableSettings.PHYSICAL_NAMING_STRATEGY, CamelCaseToUnderscoresNamingStrategy.class.getName());
		// TODO The bean factory should be added as parameter but that requires that it can be injected from the
		// entityManagerFactory bean from xBaseConfig
		// properties.putIfAbsent(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(beanFactory));

		// hapi-fhir-jpaserver-base "sensible defaults"
		Map<String, Object> hapiJpaPropertyMap = new HapiFhirLocalContainerEntityManagerFactoryBean(
						myConfigurableListableBeanFactory)
				.getJpaPropertyMap();
		hapiJpaPropertyMap.forEach(properties::putIfAbsent);

		// hapi-fhir-jpaserver-starter defaults
		properties.putIfAbsent(AvailableSettings.FORMAT_SQL, false);
		properties.putIfAbsent(AvailableSettings.SHOW_SQL, false);
		properties.putIfAbsent(AvailableSettings.HBM2DDL_AUTO, "update");
		properties.putIfAbsent(AvailableSettings.STATEMENT_BATCH_SIZE, 20);
		properties.putIfAbsent(AvailableSettings.USE_QUERY_CACHE, false);
		properties.putIfAbsent(AvailableSettings.USE_SECOND_LEVEL_CACHE, false);
		properties.putIfAbsent(AvailableSettings.USE_STRUCTURED_CACHE, false);
		properties.putIfAbsent(AvailableSettings.USE_MINIMAL_PUTS, false);

		// Hibernate Search defaults
		properties.putIfAbsent(HibernateOrmMapperSettings.ENABLED, false);
		if (Boolean.parseBoolean(String.valueOf(properties.get(HibernateOrmMapperSettings.ENABLED)))) {
			if (isElasticsearchEnabled(environment)) {
				properties.putIfAbsent(
						BackendSettings.backendKey(BackendSettings.TYPE), ElasticsearchBackendSettings.TYPE_NAME);
			} else {
				properties.putIfAbsent(
						BackendSettings.backendKey(BackendSettings.TYPE), LuceneBackendSettings.TYPE_NAME);
			}

			if (properties
					.get(BackendSettings.backendKey(BackendSettings.TYPE))
					.equals(LuceneBackendSettings.TYPE_NAME)) {
				properties.putIfAbsent(
						BackendSettings.backendKey(LuceneIndexSettings.DIRECTORY_TYPE),
						LocalFileSystemDirectoryProvider.NAME);
				properties.putIfAbsent(
						BackendSettings.backendKey(LuceneIndexSettings.DIRECTORY_ROOT), "target/lucenefiles");
				properties.putIfAbsent(
						BackendSettings.backendKey(LuceneBackendSettings.ANALYSIS_CONFIGURER),
						HapiHSearchAnalysisConfigurers.HapiLuceneAnalysisConfigurer.class.getName());
				properties.putIfAbsent(
						BackendSettings.backendKey(LuceneBackendSettings.LUCENE_VERSION), Version.LATEST);

			} else if (properties
					.get(BackendSettings.backendKey(BackendSettings.TYPE))
					.equals(ElasticsearchBackendSettings.TYPE_NAME)) {
				ElasticsearchHibernatePropertiesBuilder builder = new ElasticsearchHibernatePropertiesBuilder();
				IndexStatus requiredIndexStatus =
						environment.getProperty("elasticsearch.required_index_status", IndexStatus.class);
				builder.setRequiredIndexStatus(requireNonNullElse(requiredIndexStatus, IndexStatus.YELLOW));
				builder.setHosts(getElasticsearchServerUrl(environment));
				builder.setUsername(getElasticsearchServerUsername(environment));
				builder.setPassword(getElasticsearchServerPassword(environment));
				builder.setProtocol(getElasticsearchServerProtocol(environment));
				SchemaManagementStrategyName indexSchemaManagementStrategy = environment.getProperty(
						"elasticsearch.schema_management_strategy", SchemaManagementStrategyName.class);
				builder.setIndexSchemaManagementStrategy(
						requireNonNullElse(indexSchemaManagementStrategy, SchemaManagementStrategyName.CREATE));
				Boolean refreshAfterWrite =
						environment.getProperty("elasticsearch.debug.refresh_after_write", Boolean.class);
				if (refreshAfterWrite == null || !refreshAfterWrite) {
					builder.setDebugIndexSyncStrategy(AutomaticIndexingSynchronizationStrategyNames.ASYNC);
				} else {
					builder.setDebugIndexSyncStrategy(AutomaticIndexingSynchronizationStrategyNames.READ_SYNC);
				}
				builder.setDebugPrettyPrintJsonLog(requireNonNullElse(
						environment.getProperty("elasticsearch.debug.pretty_print_json_log", Boolean.class), false));
				builder.apply(properties);

			} else {
				throw new UnsupportedOperationException("Unsupported Hibernate Search backend: "
						+ properties.get(BackendSettings.backendKey(BackendSettings.TYPE)));
			}
		}

		return properties;
	}

	public static String getElasticsearchServerUrl(ConfigurableEnvironment environment) {
		return environment.getProperty("elasticsearch.rest_url", String.class);
	}

	public static String getElasticsearchServerProtocol(ConfigurableEnvironment environment) {
		return environment.getProperty("elasticsearch.protocol", String.class, "http");
	}

	public static String getElasticsearchServerUsername(ConfigurableEnvironment environment) {
		return environment.getProperty("elasticsearch.username");
	}

	public static String getElasticsearchServerPassword(ConfigurableEnvironment environment) {
		return environment.getProperty("elasticsearch.password");
	}

	public static Boolean isElasticsearchEnabled(ConfigurableEnvironment environment) {
		if (environment.getProperty("elasticsearch.enabled", Boolean.class) != null) {
			return environment.getProperty("elasticsearch.enabled", Boolean.class);
		} else {
			return false;
		}
	}

	public static Map<String, Object> getPropertiesStartingWith(ConfigurableEnvironment aEnv, String aKeyPrefix) {
		Map<String, Object> result = new HashMap<>();

		Map<String, Object> map = getAllProperties(aEnv);

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();

			if (key.startsWith(aKeyPrefix)) {
				result.put(key, entry.getValue());
			}
		}

		return result;
	}

	public static Map<String, Object> getAllProperties(ConfigurableEnvironment aEnv) {
		Map<String, Object> result = new HashMap<>();
		aEnv.getPropertySources().forEach(ps -> addAll(result, getAllProperties(ps)));
		return result;
	}

	public static Map<String, Object> getAllProperties(PropertySource<?> aPropSource) {
		Map<String, Object> result = new HashMap<>();

		if (aPropSource instanceof CompositePropertySource) {
			CompositePropertySource cps = (CompositePropertySource) aPropSource;
			cps.getPropertySources().forEach(ps -> addAll(result, getAllProperties(ps)));
			return result;
		}

		if (aPropSource instanceof EnumerablePropertySource<?>) {
			EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) aPropSource;
			Arrays.asList(ps.getPropertyNames()).forEach(key -> result.put(key, ps.getProperty(key)));
			return result;
		}

		return result;
	}

	private static void addAll(Map<String, Object> aBase, Map<String, Object> aToBeAdded) {
		for (Map.Entry<String, Object> entry : aToBeAdded.entrySet()) {
			if (aBase.containsKey(entry.getKey())) {
				continue;
			}

			aBase.put(entry.getKey(), entry.getValue());
		}
	}
}
