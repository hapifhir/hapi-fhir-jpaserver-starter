package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.binstore.DatabaseBlobBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.config.HibernatePropertiesProvider;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.config.PartitionSettings.CrossPartitionReferenceMode;
import ca.uhn.fhir.jpa.model.entity.ModelConfig;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.JpaHibernatePropertiesProvider;
import ca.uhn.fhir.jpa.subscription.channel.subscription.SubscriptionDeliveryHandlerFactory;
import ca.uhn.fhir.jpa.subscription.match.deliver.email.EmailSenderImpl;
import ca.uhn.fhir.jpa.subscription.match.deliver.email.IEmailSender;
import ca.uhn.fhir.rest.server.mail.IMailSvc;
import ca.uhn.fhir.rest.server.mail.MailConfig;
import ca.uhn.fhir.rest.server.mail.MailSvc;
import com.google.common.base.Strings;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.dstu2.model.Subscription;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is the primary configuration file for the example server
 */
@Configuration
@EnableTransactionManagement
public class FhirServerConfigCommon {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FhirServerConfigCommon.class);


  public FhirServerConfigCommon(AppProperties appProperties) {
    ourLog.info("Server configured to " + (appProperties.getAllow_contains_searches() ? "allow" : "deny") + " contains searches");
    ourLog.info("Server configured to " + (appProperties.getAllow_multiple_delete() ? "allow" : "deny") + " multiple deletes");
    ourLog.info("Server configured to " + (appProperties.getAllow_external_references() ? "allow" : "deny") + " external references");
    ourLog.info("Server configured to " + (appProperties.getDao_scheduling_enabled() ? "enable" : "disable") + " DAO scheduling");
    ourLog.info("Server configured to " + (appProperties.getDelete_expunge_enabled() ? "enable" : "disable") + " delete expunges");
    ourLog.info("Server configured to " + (appProperties.getExpunge_enabled() ? "enable" : "disable") + " expunges");
    ourLog.info("Server configured to " + (appProperties.getAllow_override_default_search_params() ? "allow" : "deny") + " overriding default search params");
    ourLog.info("Server configured to " + (appProperties.getAuto_create_placeholder_reference_targets() ? "allow" : "disable") + " auto-creating placeholder references");

    if (appProperties.getSubscription().getEmail() != null) {
      AppProperties.Subscription.Email email = appProperties.getSubscription().getEmail();
      ourLog.info("Server is configured to enable email with host '" + email.getHost() + "' and port " + email.getPort());
      ourLog.info("Server will use '" + email.getFrom() + "' as the from email address");

      if (!Strings.isNullOrEmpty(email.getUsername())) {
        ourLog.info("Server is configured to use username '" + email.getUsername() + "' for email");
      }

      if (!Strings.isNullOrEmpty(email.getPassword())) {
        ourLog.info("Server is configured to use a password for email");
      }
    }

    if (appProperties.getSubscription().getResthook_enabled()) {
      ourLog.info("REST-hook subscriptions enabled");
    }

    if (appProperties.getSubscription().getEmail() != null) {
      ourLog.info("Email subscriptions enabled");
    }

    if (appProperties.getEnable_index_contained_resource() == Boolean.TRUE) {
        ourLog.info("Indexed on contained resource enabled");
      }
  }

  /**
   * Configure FHIR properties around the the JPA server via this bean
   */
  @Bean
  public DaoConfig daoConfig(AppProperties appProperties) {
    DaoConfig daoConfig = new DaoConfig();

    daoConfig.setIndexMissingFields(appProperties.getEnable_index_missing_fields() ? DaoConfig.IndexEnabledEnum.ENABLED : DaoConfig.IndexEnabledEnum.DISABLED);
    daoConfig.setAutoCreatePlaceholderReferenceTargets(appProperties.getAuto_create_placeholder_reference_targets());
    daoConfig.setEnforceReferentialIntegrityOnWrite(appProperties.getEnforce_referential_integrity_on_write());
    daoConfig.setEnforceReferentialIntegrityOnDelete(appProperties.getEnforce_referential_integrity_on_delete());
    daoConfig.setAllowContainsSearches(appProperties.getAllow_contains_searches());
    daoConfig.setAllowMultipleDelete(appProperties.getAllow_multiple_delete());
    daoConfig.setAllowExternalReferences(appProperties.getAllow_external_references());
    daoConfig.setSchedulingDisabled(!appProperties.getDao_scheduling_enabled());
    daoConfig.setDeleteExpungeEnabled(appProperties.getDelete_expunge_enabled());
    daoConfig.setExpungeEnabled(appProperties.getExpunge_enabled());
    if(appProperties.getSubscription() != null && appProperties.getSubscription().getEmail() != null)
      daoConfig.setEmailFromAddress(appProperties.getSubscription().getEmail().getFrom());

    Integer maxFetchSize =  appProperties.getMax_page_size();
    daoConfig.setFetchSizeDefaultMaximum(maxFetchSize);
    ourLog.info("Server configured to have a maximum fetch size of " + (maxFetchSize == Integer.MAX_VALUE ? "'unlimited'" : maxFetchSize));

    Long reuseCachedSearchResultsMillis = appProperties.getReuse_cached_search_results_millis();
    daoConfig.setReuseCachedSearchResultsForMillis(reuseCachedSearchResultsMillis);
    ourLog.info("Server configured to cache search results for {} milliseconds", reuseCachedSearchResultsMillis);


    Long retainCachedSearchesMinutes = appProperties.getRetain_cached_searches_mins();
    daoConfig.setExpireSearchResultsAfterMillis(retainCachedSearchesMinutes * 60 * 1000);

    if(appProperties.getSubscription() != null) {
      // Subscriptions are enabled by channel type
      if (appProperties.getSubscription().getResthook_enabled()) {
        ourLog.info("Enabling REST-hook subscriptions");
        daoConfig.addSupportedSubscriptionType(org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.RESTHOOK);
      }
      if (appProperties.getSubscription().getEmail() != null) {
        ourLog.info("Enabling email subscriptions");
        daoConfig.addSupportedSubscriptionType(org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.EMAIL);
      }
      if (appProperties.getSubscription().getWebsocket_enabled()) {
        ourLog.info("Enabling websocket subscriptions");
        daoConfig.addSupportedSubscriptionType(org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.WEBSOCKET);
      }
    }

    daoConfig.setFilterParameterEnabled(appProperties.getFilter_search_enabled());
	 daoConfig.setAdvancedHSearchIndexing(appProperties.getAdvanced_lucene_indexing());
	 daoConfig.setTreatBaseUrlsAsLocal(new HashSet<>(appProperties.getLocal_base_urls()));

	      if (appProperties.getLastn_enabled()) {
      daoConfig.setLastNEnabled(true);
    }

	  if(appProperties.getInline_resource_storage_below_size() != 0){
		  daoConfig.setInlineResourceTextBelowSize(appProperties.getInline_resource_storage_below_size());
	  }

	  daoConfig.setStoreResourceInHSearchIndex(appProperties.getStore_resource_in_lucene_index_enabled());
	  daoConfig.getModelConfig().setNormalizedQuantitySearchLevel(appProperties.getNormalized_quantity_search_level());
	  daoConfig.getModelConfig().setIndexOnContainedResources(appProperties.getEnable_index_contained_resource());



    if (appProperties.getAllowed_bundle_types() != null) {
      daoConfig.setBundleTypesAllowedForStorage(appProperties.getAllowed_bundle_types().stream().map(BundleType::toCode).collect(Collectors.toSet()));
    }

	  daoConfig.setDeferIndexingForCodesystemsOfSize(appProperties.getDefer_indexing_for_codesystems_of_size());


    if (appProperties.getClient_id_strategy() == DaoConfig.ClientIdStrategyEnum.ANY) {
		 daoConfig.setResourceServerIdStrategy(DaoConfig.IdStrategyEnum.UUID);
		 daoConfig.setResourceClientIdStrategy(appProperties.getClient_id_strategy());
    }
    //Parallel Batch GET execution settings
	  daoConfig.setBundleBatchPoolSize(appProperties.getBundle_batch_pool_size());
	  daoConfig.setBundleBatchPoolSize(appProperties.getBundle_batch_pool_max_size());


    return daoConfig;
  }

  @Bean
  public YamlPropertySourceLoader yamlPropertySourceLoader() {
    return new YamlPropertySourceLoader();
  }

  @Bean
  public PartitionSettings partitionSettings(AppProperties appProperties) {
    PartitionSettings retVal = new PartitionSettings();

    // Partitioning
    if (appProperties.getPartitioning() != null) {
      retVal.setPartitioningEnabled(true);
      retVal.setIncludePartitionInSearchHashes(appProperties.getPartitioning().getPartitioning_include_in_search_hashes());
      if(appProperties.getPartitioning().getAllow_references_across_partitions()) {
        retVal.setAllowReferencesAcrossPartitions(CrossPartitionReferenceMode.ALLOWED_UNQUALIFIED);
      } else {
        retVal.setAllowReferencesAcrossPartitions(CrossPartitionReferenceMode.NOT_ALLOWED);
      }
    }

    return retVal;
  }


  @Primary
  @Bean
  public HibernatePropertiesProvider jpaStarterDialectProvider(LocalContainerEntityManagerFactoryBean myEntityManagerFactory) {
    return new JpaHibernatePropertiesProvider(myEntityManagerFactory);
  }

  @Bean
  public ModelConfig modelConfig(AppProperties appProperties, DaoConfig daoConfig) {
    ModelConfig modelConfig = daoConfig.getModelConfig();
    modelConfig.setAllowContainsSearches(appProperties.getAllow_contains_searches());
    modelConfig.setAllowExternalReferences(appProperties.getAllow_external_references());
    modelConfig.setDefaultSearchParamsCanBeOverridden(appProperties.getAllow_override_default_search_params());
    if(appProperties.getSubscription() != null && appProperties.getSubscription().getEmail() != null)
      modelConfig.setEmailFromAddress(appProperties.getSubscription().getEmail().getFrom());

    modelConfig.setNormalizedQuantitySearchLevel(appProperties.getNormalized_quantity_search_level());

    modelConfig.setIndexOnContainedResources(appProperties.getEnable_index_contained_resource());
    modelConfig.setIndexIdentifierOfType(appProperties.getEnable_index_of_type());
    return modelConfig;
  }

  @Lazy
  @Bean
  public IBinaryStorageSvc binaryStorageSvc(AppProperties appProperties) {
    DatabaseBlobBinaryStorageSvcImpl binaryStorageSvc = new DatabaseBlobBinaryStorageSvcImpl();

    if (appProperties.getMax_binary_size() != null) {
      binaryStorageSvc.setMaximumBinarySize(appProperties.getMax_binary_size());
    }

    return binaryStorageSvc;
  }

  @Bean
  public IEmailSender emailSender(AppProperties appProperties, Optional<SubscriptionDeliveryHandlerFactory> subscriptionDeliveryHandlerFactory) {
    if (appProperties.getSubscription() != null && appProperties.getSubscription().getEmail() != null) {
		 MailConfig mailConfig = new MailConfig();

      AppProperties.Subscription.Email email = appProperties.getSubscription().getEmail();
      mailConfig.setSmtpHostname(email.getHost());
      mailConfig.setSmtpPort(email.getPort());
      mailConfig.setSmtpUsername(email.getUsername());
      mailConfig.setSmtpPassword(email.getPassword());
      mailConfig.setSmtpUseStartTLS(email.getStartTlsEnable());

		 IMailSvc mailSvc = new MailSvc(mailConfig);
		 IEmailSender emailSender = new EmailSenderImpl(mailSvc);

		subscriptionDeliveryHandlerFactory.ifPresent(theSubscriptionDeliveryHandlerFactory -> theSubscriptionDeliveryHandlerFactory.setEmailSender(emailSender));

      return emailSender;
    }

    return null;
  }
}
