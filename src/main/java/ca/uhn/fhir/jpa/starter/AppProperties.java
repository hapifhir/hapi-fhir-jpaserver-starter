package ca.uhn.fhir.jpa.starter;


import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.api.config.DaoConfig.ClientIdStrategyEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.hl7.fhir.r4.model.Bundle;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "hapi.fhir")
@Configuration
@EnableConfigurationProperties
public class AppProperties {

  private Boolean empi_enabled = false;
  private Boolean allow_cascading_deletes = false;
  private Boolean allow_contains_searches = true;
  private Boolean allow_external_references = false;
  private Boolean allow_multiple_delete = false;
  private Boolean allow_override_default_search_params = true;
  private Boolean allow_placeholder_references = true;
  private Boolean auto_create_placeholder_reference_targets = true;
  private Boolean enable_index_missing_fields = false;
  private Boolean enforce_referential_integrity_on_delete = true;
  private Boolean enforce_referential_integrity_on_write = true;
  private Boolean etag_support_enabled = true;
  private Boolean expunge_enabled = true;
  private Boolean fhirpath_interceptor_enabled = false;
  private Boolean filter_search_enabled = true;
  private Boolean graphql_enabled = false;
  private Boolean binary_storage_enabled = false;
  private Boolean bulk_export_enabled = false;
  private Boolean default_pretty_print = true;
  private Integer default_page_size = 20;
  private Integer max_binary_size = null;
  private Integer max_page_size = Integer.MAX_VALUE;
  private Integer defer_indexing_for_codesystems_of_size = 100;
  private Long retain_cached_searches_mins = 60L;
  private Long reuse_cached_search_results_millis = 60000L;
  private String server_address = null;
  private EncodingEnum default_encoding = EncodingEnum.JSON;
  private FhirVersionEnum fhir_version = FhirVersionEnum.R4;
  private ClientIdStrategyEnum client_id_strategy = ClientIdStrategyEnum.ALPHANUMERIC;
  private List<String> supported_resource_types = new ArrayList<>();
  private List<Bundle.BundleType> allowed_bundle_types = null;

  private Validation validation = new Validation();
  private List<Tester> tester = ImmutableList.of(new Tester());
  private Logger logger = new Logger();
  private Subscription subscription = new Subscription();
  private Cors cors = null;
  private Partitioning partitioning = null;
  private List<ImplementationGuide> implementationGuides = null;

  public Integer getDefer_indexing_for_codesystems_of_size() {
    return defer_indexing_for_codesystems_of_size;
  }

  public void setDefer_indexing_for_codesystems_of_size(Integer defer_indexing_for_codesystems_of_size) {
    this.defer_indexing_for_codesystems_of_size = defer_indexing_for_codesystems_of_size;
  }

  public List<ImplementationGuide> getImplementationGuides() {
    return implementationGuides;
  }

  public void setImplementationGuides(List<ImplementationGuide> implementationGuides) {
    this.implementationGuides = implementationGuides;
  }

  public Partitioning getPartitioning() {
    return partitioning;
  }

  public void setPartitioning(Partitioning partitioning) {
    this.partitioning = partitioning;
  }

  public Boolean getEmpi_enabled() {
    return empi_enabled;
  }

  public void setEmpi_enabled(Boolean empi_enabled) {
    this.empi_enabled = empi_enabled;
  }


  public Cors getCors() {
    return cors;
  }

  public void setCors(Cors cors) {
    this.cors = cors;
  }

  public List<Bundle.BundleType> getAllowed_bundle_types() {
    return allowed_bundle_types;
  }

  public void setAllowed_bundle_types(List<Bundle.BundleType> allowed_bundle_types) {
    this.allowed_bundle_types = allowed_bundle_types;
  }

  public String getServer_address() {
    return server_address;
  }

  public void setServer_address(String server_address) {
    this.server_address = server_address;
  }

  public Subscription getSubscription() {
    return subscription;
  }

  public Boolean getDefault_pretty_print() {
    return default_pretty_print;
  }

  public void setDefault_pretty_print(Boolean default_pretty_print) {
    this.default_pretty_print = default_pretty_print;
  }

  public void setSubscription(Subscription subscription) {
    this.subscription = subscription;
  }

  public Validation getValidation() {
    return validation;
  }

  public void setValidation(Validation validation) {
    this.validation = validation;
  }

  public List<String> getSupported_resource_types() {
    return supported_resource_types;
  }

  public void setSupported_resource_types(List<String> supported_resource_types) {
    this.supported_resource_types = supported_resource_types;
  }

  public Logger getLogger() {
    return logger;
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public ClientIdStrategyEnum getClient_id_strategy() {
    return client_id_strategy;
  }

  public void setClient_id_strategy(
    ClientIdStrategyEnum client_id_strategy) {
    this.client_id_strategy = client_id_strategy;
  }

  public Boolean getAllow_cascading_deletes() {
    return allow_cascading_deletes;
  }

  public void setAllow_cascading_deletes(Boolean allow_cascading_deletes) {
    this.allow_cascading_deletes = allow_cascading_deletes;
  }

  public Boolean getAllow_contains_searches() {
    return allow_contains_searches;
  }

  public void setAllow_contains_searches(Boolean allow_contains_searches) {
    this.allow_contains_searches = allow_contains_searches;
  }

  public Boolean getAllow_external_references() {
    return allow_external_references;
  }

  public void setAllow_external_references(Boolean allow_external_references) {
    this.allow_external_references = allow_external_references;
  }

  public Boolean getAllow_multiple_delete() {
    return allow_multiple_delete;
  }

  public void setAllow_multiple_delete(Boolean allow_multiple_delete) {
    this.allow_multiple_delete = allow_multiple_delete;
  }

  public Boolean getAllow_override_default_search_params() {
    return allow_override_default_search_params;
  }

  public void setAllow_override_default_search_params(
    Boolean allow_override_default_search_params) {
    this.allow_override_default_search_params = allow_override_default_search_params;
  }

  public Boolean getAllow_placeholder_references() {
    return allow_placeholder_references;
  }

  public void setAllow_placeholder_references(Boolean allow_placeholder_references) {
    this.allow_placeholder_references = allow_placeholder_references;
  }

  public Boolean getAuto_create_placeholder_reference_targets() {
    return auto_create_placeholder_reference_targets;
  }

  public void setAuto_create_placeholder_reference_targets(
    Boolean auto_create_placeholder_reference_targets) {
    this.auto_create_placeholder_reference_targets = auto_create_placeholder_reference_targets;
  }

  public Integer getDefault_page_size() {
    return default_page_size;
  }

  public void setDefault_page_size(Integer default_page_size) {
    this.default_page_size = default_page_size;
  }

  public Boolean getEnable_index_missing_fields() {
    return enable_index_missing_fields;
  }

  public void setEnable_index_missing_fields(Boolean enable_index_missing_fields) {
    this.enable_index_missing_fields = enable_index_missing_fields;
  }

  public Boolean getEnforce_referential_integrity_on_delete() {
    return enforce_referential_integrity_on_delete;
  }

  public void setEnforce_referential_integrity_on_delete(
    Boolean enforce_referential_integrity_on_delete) {
    this.enforce_referential_integrity_on_delete = enforce_referential_integrity_on_delete;
  }

  public Boolean getEnforce_referential_integrity_on_write() {
    return enforce_referential_integrity_on_write;
  }

  public void setEnforce_referential_integrity_on_write(
    Boolean enforce_referential_integrity_on_write) {
    this.enforce_referential_integrity_on_write = enforce_referential_integrity_on_write;
  }

  public Boolean getEtag_support_enabled() {
    return etag_support_enabled;
  }

  public void setEtag_support_enabled(Boolean etag_support_enabled) {
    this.etag_support_enabled = etag_support_enabled;
  }

  public Boolean getExpunge_enabled() {
    return expunge_enabled;
  }

  public void setExpunge_enabled(Boolean expunge_enabled) {
    this.expunge_enabled = expunge_enabled;
  }

  public Boolean getFhirpath_interceptor_enabled() {
    return fhirpath_interceptor_enabled;
  }

  public void setFhirpath_interceptor_enabled(Boolean fhirpath_interceptor_enabled) {
    this.fhirpath_interceptor_enabled = fhirpath_interceptor_enabled;
  }

  public Boolean getFilter_search_enabled() {
    return filter_search_enabled;
  }

  public void setFilter_search_enabled(Boolean filter_search_enabled) {
    this.filter_search_enabled = filter_search_enabled;
  }

  public Boolean getGraphql_enabled() {
    return graphql_enabled;
  }

  public void setGraphql_enabled(Boolean graphql_enabled) {
    this.graphql_enabled = graphql_enabled;
  }

  public Boolean getBinary_storage_enabled() {
    return binary_storage_enabled;
  }

  public void setBinary_storage_enabled(Boolean binary_storage_enabled) {
    this.binary_storage_enabled = binary_storage_enabled;
  }

  public Boolean getBulk_export_enabled() {
    return bulk_export_enabled;
  }

  public void setBulk_export_enabled(Boolean bulk_export_enabled) {
    this.bulk_export_enabled = bulk_export_enabled;
  }

  public EncodingEnum getDefault_encoding() {
    return default_encoding;
  }

  public void setDefault_encoding(EncodingEnum default_encoding) {
    this.default_encoding = default_encoding;
  }

  public FhirVersionEnum getFhir_version() {
    return fhir_version;
  }

  public void setFhir_version(FhirVersionEnum fhir_version) {
    this.fhir_version = fhir_version;
  }

  public Integer getMax_binary_size() {
    return max_binary_size;
  }

  public void setMax_binary_size(Integer max_binary_size) {
    this.max_binary_size = max_binary_size;
  }

  public Integer getMax_page_size() {
    return max_page_size;
  }

  public void setMax_page_size(Integer max_page_size) {
    this.max_page_size = max_page_size;
  }

  public Long getRetain_cached_searches_mins() {
    return retain_cached_searches_mins;
  }

  public void setRetain_cached_searches_mins(Long retain_cached_searches_mins) {
    this.retain_cached_searches_mins = retain_cached_searches_mins;
  }

  public Long getReuse_cached_search_results_millis() {
    return reuse_cached_search_results_millis;
  }

  public void setReuse_cached_search_results_millis(Long reuse_cached_search_results_millis) {
    this.reuse_cached_search_results_millis = reuse_cached_search_results_millis;
  }

  public List<Tester> getTester() {
    return tester;
  }

  public void setTester(List<Tester> tester) {
    this.tester = tester;
  }

  public static class Cors {
    private Boolean allow_Credentials = true;
    private List<String> allowed_origin = ImmutableList.of("*");

    public List<String> getAllowed_origin() {
      return allowed_origin;
    }

    public void setAllowed_origin(List<String> allowed_origin) {
      this.allowed_origin = allowed_origin;
    }

    public Boolean getAllow_Credentials() {
      return allow_Credentials;
    }

    public void setAllow_Credentials(Boolean allow_Credentials) {
      this.allow_Credentials = allow_Credentials;
    }


  }

  public static class Logger {

    private String name = "fhirtest.access";
    private String error_format = "ERROR - ${requestVerb} ${requestUrl}";
    private String format = "Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}]";
    private Boolean log_exceptions = true;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getError_format() {
      return error_format;
    }

    public void setError_format(String error_format) {
      this.error_format = error_format;
    }

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }

    public Boolean getLog_exceptions() {
      return log_exceptions;
    }

    public void setLog_exceptions(Boolean log_exceptions) {
      this.log_exceptions = log_exceptions;
    }
  }


  public static class Tester {

    private String id = "home";
    private String name = "Local Tester";
    private String server_address = "http://localhost:8080/fhir";
    private Boolean refuse_to_fetch_third_party_urls = true;
    private FhirVersionEnum fhir_version = FhirVersionEnum.R4;

    public FhirVersionEnum getFhir_version() {
      return fhir_version;
    }

    public void setFhir_version(FhirVersionEnum fhir_version) {
      this.fhir_version = fhir_version;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getServer_address() {
      return server_address;
    }

    public void setServer_address(String server_address) {
      this.server_address = server_address;
    }

    public Boolean getRefuse_to_fetch_third_party_urls() {
      return refuse_to_fetch_third_party_urls;
    }

    public void setRefuse_to_fetch_third_party_urls(Boolean refuse_to_fetch_third_party_urls) {
      this.refuse_to_fetch_third_party_urls = refuse_to_fetch_third_party_urls;
    }
  }

  public static class ImplementationGuide
  {
    private String url;
    private String name;
    private String version;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }
  }

  public static class Validation {

    private Boolean requests_enabled = false;
    private Boolean responses_enabled = false;

    public Boolean getRequests_enabled() {
      return requests_enabled;
    }

    public void setRequests_enabled(Boolean requests_enabled) {
      this.requests_enabled = requests_enabled;
    }

    public Boolean getResponses_enabled() {
      return responses_enabled;
    }

    public void setResponses_enabled(Boolean responses_enabled) {
      this.responses_enabled = responses_enabled;
    }
  }

  public static class Partitioning {

    private Boolean partitioning_include_in_search_hashes = false;


    public Boolean getPartitioning_include_in_search_hashes() {
      return partitioning_include_in_search_hashes;
    }

    public void setPartitioning_include_in_search_hashes(Boolean partitioning_include_in_search_hashes) {
      this.partitioning_include_in_search_hashes = partitioning_include_in_search_hashes;
    }
  }

  public static class Subscription {

    public Boolean getResthook_enabled() {
      return resthook_enabled;
    }

    public void setResthook_enabled(Boolean resthook_enabled) {
      this.resthook_enabled = resthook_enabled;
    }

    public Boolean getWebsocket_enabled() {
      return websocket_enabled;
    }

    public void setWebsocket_enabled(Boolean websocket_enabled) {
      this.websocket_enabled = websocket_enabled;
    }

    private Boolean resthook_enabled = false;
    private Boolean websocket_enabled = false;
    private Email email = null;

    public Email getEmail() {
      return email;
    }

    public void setEmail(Email email) {
      this.email = email;
    }


    public static class Email {
      public String getFrom() {
        return from;
      }

      public void setFrom(String from) {
        this.from = from;
      }

      public String getHost() {
        return host;
      }

      public void setHost(String host) {
        this.host = host;
      }

      public Integer getPort() {
        return port;
      }

      public void setPort(Integer port) {
        this.port = port;
      }

      public String getUsername() {
        return username;
      }

      public void setUsername(String username) {
        this.username = username;
      }

      public String getPassword() {
        return password;
      }

      public void setPassword(String password) {
        this.password = password;
      }

      public Boolean getAuth() {
        return auth;
      }

      public void setAuth(Boolean auth) {
        this.auth = auth;
      }

      public Boolean getStartTlsEnable() {
        return startTlsEnable;
      }

      public void setStartTlsEnable(Boolean startTlsEnable) {
        this.startTlsEnable = startTlsEnable;
      }

      public Boolean getStartTlsRequired() {
        return startTlsRequired;
      }

      public void setStartTlsRequired(Boolean startTlsRequired) {
        this.startTlsRequired = startTlsRequired;
      }

      public Boolean getQuitWait() {
        return quitWait;
      }

      public void setQuitWait(Boolean quitWait) {
        this.quitWait = quitWait;
      }

      private String from;
      private String host;
      private Integer port = 25;
      private String username;
      private String password;
      private Boolean auth = false;
      private Boolean startTlsEnable = false;
      private Boolean startTlsRequired = false;
      private Boolean quitWait = false;
    }
  }
}
