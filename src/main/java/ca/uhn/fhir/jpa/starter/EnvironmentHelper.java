package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.search.elastic.ElasticsearchHibernatePropertiesBuilder;
import org.hibernate.search.elasticsearch.cfg.ElasticsearchIndexStatus;
import org.hibernate.search.elasticsearch.cfg.IndexSchemaManagementStrategy;
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

    properties.putIfAbsent("hibernate.search.model_mapping", "ca.uhn.fhir.jpa.search.LuceneSearchMappingFactory");
    properties.putIfAbsent("hibernate.format_sql", "false");
    properties.putIfAbsent("hibernate.show_sql", "false");
    properties.putIfAbsent("hibernate.hbm2ddl.auto", "update");
    properties.putIfAbsent("hibernate.jdbc.batch_size", "20");
    properties.putIfAbsent("hibernate.cache.use_query_cache", "false");
    properties.putIfAbsent("hibernate.cache.use_second_level_cache", "false");
    properties.putIfAbsent("hibernate.cache.use_structured_entries", "false");
    properties.putIfAbsent("hibernate.cache.use_minimal_puts", "false");
    properties.putIfAbsent("hibernate.search.default.directory_provider", "filesystem");
    properties.putIfAbsent("hibernate.search.default.indexBase", "target/lucenefiles");
    properties.putIfAbsent("hibernate.search.lucene_version", "LUCENE_CURRENT");

    for (Map.Entry<String, Object> entry : jpaProps.entrySet()) {
      String strippedKey = entry.getKey().replace("spring.jpa.properties.", "");
      properties.put(strippedKey, entry.getValue().toString());
    }


    if (environment.getProperty("elasticsearch.enabled", Boolean.class) != null
      && environment.getProperty("elasticsearch.enabled", Boolean.class) == true) {
      ElasticsearchHibernatePropertiesBuilder builder = new ElasticsearchHibernatePropertiesBuilder();
      ElasticsearchIndexStatus requiredIndexStatus = environment.getProperty("elasticsearch.required_index_status", ElasticsearchIndexStatus.class);
      if (requiredIndexStatus == null) {
        builder.setRequiredIndexStatus(ElasticsearchIndexStatus.YELLOW);
      } else {
        builder.setRequiredIndexStatus(requiredIndexStatus);
      }

      builder.setRestUrl(getElasticsearchServerUrl(environment));
      builder.setUsername(getElasticsearchServerUsername(environment));
      builder.setPassword(getElasticsearchServerPassword(environment));
      IndexSchemaManagementStrategy indexSchemaManagementStrategy = environment.getProperty("elasticsearch.schema_management_strategy", IndexSchemaManagementStrategy.class);
      if (indexSchemaManagementStrategy == null) {
        builder.setIndexSchemaManagementStrategy(IndexSchemaManagementStrategy.CREATE);
      } else {
        builder.setIndexSchemaManagementStrategy(indexSchemaManagementStrategy);
      }
      //    pretty_print_json_log: false
      Boolean refreshAfterWrite = environment.getProperty("elasticsearch.debug.refresh_after_write", Boolean.class);
      if (refreshAfterWrite == null) {
        builder.setDebugRefreshAfterWrite(false);
      } else {
        builder.setDebugRefreshAfterWrite(refreshAfterWrite);
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
