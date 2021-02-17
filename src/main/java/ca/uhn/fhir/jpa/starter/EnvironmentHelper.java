package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.search.HapiLuceneAnalysisConfigurer;
import ca.uhn.fhir.jpa.search.elastic.ElasticsearchHibernatePropertiesBuilder;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.hibernate.search.backend.elasticsearch.index.IndexStatus;
import org.hibernate.search.backend.lucene.cfg.LuceneBackendSettings;
import org.hibernate.search.backend.lucene.cfg.LuceneIndexSettings;
import org.hibernate.search.engine.cfg.BackendSettings;
import org.hibernate.search.mapper.orm.automaticindexing.session.AutomaticIndexingSynchronizationStrategyNames;
import org.hibernate.search.mapper.orm.cfg.HibernateOrmMapperSettings;
import org.hibernate.search.mapper.orm.schema.management.SchemaManagementStrategyName;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EnvironmentHelper {

  public static Properties getHibernateProperties(ConfigurableEnvironment environment) {
    Properties properties = new Properties();

    Map<String, Object> jpaProps = getPropertiesStartingWith(environment, "spring.jpa.properties");
    properties.putIfAbsent("hibernate.format_sql", "false");
    properties.putIfAbsent("hibernate.show_sql", "false");
    properties.putIfAbsent("hibernate.hbm2ddl.auto", "update");
    properties.putIfAbsent("hibernate.jdbc.batch_size", "20");
    properties.putIfAbsent("hibernate.cache.use_query_cache", "false");
    properties.putIfAbsent("hibernate.cache.use_second_level_cache", "false");
    properties.putIfAbsent("hibernate.cache.use_structured_entries", "false");
    properties.putIfAbsent("hibernate.cache.use_minimal_puts", "false");

    if (jpaProps.getOrDefault("spring.jpa.properties.hibernate.search.enabled", "false").toString() == "true") {
		 properties.putIfAbsent(HibernateOrmMapperSettings.ENABLED, true);
		 properties.putIfAbsent(BackendSettings.backendKey(LuceneIndexSettings.DIRECTORY_TYPE), "local-filesystem");
		 properties.putIfAbsent(BackendSettings.backendKey(LuceneIndexSettings.DIRECTORY_ROOT), "target/lucenefiles");
		 properties.putIfAbsent(BackendSettings.backendKey(BackendSettings.TYPE), "lucene");
		 properties.putIfAbsent(BackendSettings.backendKey(LuceneBackendSettings.ANALYSIS_CONFIGURER), HapiLuceneAnalysisConfigurer.class.getName());
		 properties.putIfAbsent(BackendSettings.backendKey(LuceneBackendSettings.LUCENE_VERSION), "LUCENE_CURRENT");
	 } else {
    	properties.putIfAbsent(HibernateOrmMapperSettings.ENABLED, false);
	 }

    for (Map.Entry<String, Object> entry : jpaProps.entrySet()) {
      String strippedKey = entry.getKey().replace("spring.jpa.properties.", "");
      properties.put(strippedKey, entry.getValue().toString());
    }


    if (environment.getProperty("elasticsearch.enabled", Boolean.class) != null
      && environment.getProperty("elasticsearch.enabled", Boolean.class) == true) {
      ElasticsearchHibernatePropertiesBuilder builder = new ElasticsearchHibernatePropertiesBuilder();
      IndexStatus requiredIndexStatus = environment.getProperty("elasticsearch.required_index_status", IndexStatus.class);
      if (requiredIndexStatus == null) {
        builder.setRequiredIndexStatus(IndexStatus.YELLOW);
      } else {
        builder.setRequiredIndexStatus(requiredIndexStatus);
      }

      builder.setRestUrl(getElasticsearchServerUrl(environment));
      builder.setUsername(getElasticsearchServerUsername(environment));
      builder.setPassword(getElasticsearchServerPassword(environment));
		builder.setProtocol(getElasticsearchServerProtocol(environment));
		 SchemaManagementStrategyName indexSchemaManagementStrategy = environment.getProperty("elasticsearch.schema_management_strategy", SchemaManagementStrategyName.class);
      if (indexSchemaManagementStrategy == null) {
        builder.setIndexSchemaManagementStrategy(SchemaManagementStrategyName.CREATE);
      } else {
        builder.setIndexSchemaManagementStrategy(indexSchemaManagementStrategy);
      }
      //    pretty_print_json_log: false
      Boolean refreshAfterWrite = environment.getProperty("elasticsearch.debug.refresh_after_write", Boolean.class);
      if (refreshAfterWrite == null || refreshAfterWrite == false) {
      	builder.setDebugIndexSyncStrategy(AutomaticIndexingSynchronizationStrategyNames.ASYNC);
      } else {
			builder.setDebugIndexSyncStrategy(AutomaticIndexingSynchronizationStrategyNames.READ_SYNC);
      }
      //    pretty_print_json_log: false
      Boolean prettyPrintJsonLog = environment.getProperty("elasticsearch.debug.pretty_print_json_log", Boolean.class);
      if (prettyPrintJsonLog == null) {
        builder.setDebugPrettyPrintJsonLog(false);
      } else {
        builder.setDebugPrettyPrintJsonLog(prettyPrintJsonLog);
      }
      builder.apply(properties);
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

  public static Map<String, Object> getPropertiesStartingWith(ConfigurableEnvironment aEnv,
                                                              String aKeyPrefix) {
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
