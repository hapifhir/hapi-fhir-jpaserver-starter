package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.api.config.JpaStorageSettings;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.binstore.DatabaseBinaryContentStorageSvcImpl;
import ca.uhn.fhir.jpa.config.HibernatePropertiesProvider;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.config.PartitionSettings.CrossPartitionReferenceMode;
import ca.uhn.fhir.jpa.model.config.SubscriptionSettings;
import ca.uhn.fhir.jpa.model.entity.StorageSettings;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.JpaHibernatePropertiesProvider;
import ca.uhn.fhir.jpa.subscription.match.deliver.email.EmailSenderImpl;
import ca.uhn.fhir.jpa.subscription.match.deliver.email.IEmailSender;
import ca.uhn.fhir.rest.server.mail.MailConfig;
import ca.uhn.fhir.rest.server.mail.MailSvc;
import com.google.common.base.Strings;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashSet;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * This is the primary configuration file for the example server
 */
@Configuration
@EnableTransactionManagement
public class FhirServerConfigCommon {

	private static final Logger ourLog = LoggerFactory.getLogger(FhirServerConfigCommon.class);

	public FhirServerConfigCommon(AppProperties appProperties) {
		ourLog.info("Server configured to " + (appProperties.getAllow_contains_searches() ? "allow" : "deny")
				+ " contains searches");
		ourLog.info("Server configured to " + (appProperties.getAllow_multiple_delete() ? "allow" : "deny")
				+ " multiple deletes");
		ourLog.info("Server configured to " + (appProperties.getAllow_external_references() ? "allow" : "deny")
				+ " external references");
		ourLog.info("Server configured to " + (appProperties.getDao_scheduling_enabled() ? "enable" : "disable")
				+ " DAO scheduling");
		ourLog.info("Server configured to " + (appProperties.getDelete_expunge_enabled() ? "enable" : "disable")
				+ " delete expunges");
		ourLog.info(
				"Server configured to " + (appProperties.getExpunge_enabled() ? "enable" : "disable") + " expunges");
		ourLog.info(
				"Server configured to " + (appProperties.getAllow_override_default_search_params() ? "allow" : "deny")
						+ " overriding default search params");
		ourLog.info("Server configured to "
				+ (appProperties.getAuto_create_placeholder_reference_targets() ? "allow" : "disable")
				+ " auto-creating placeholder references");
		ourLog.info(
				"Server configured to auto-version references at paths {}",
				appProperties.getAuto_version_reference_at_paths());

		if (appProperties.getSubscription().getEmail() != null) {
			AppProperties.Subscription.Email email =
					appProperties.getSubscription().getEmail();
			ourLog.info("Server is configured to enable email with host '" + email.getHost() + "' and port "
					+ email.getPort());
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

		ourLog.info("Server configured to " + (appProperties.getPre_expand_value_sets() ? "enable" : "disable")
				+ " value set pre-expansion");
		ourLog.info(
				"Server configured to " + (appProperties.getEnable_task_pre_expand_value_sets() ? "enable" : "disable")
						+ " value set pre-expansion task");
		ourLog.info("Server configured for pre-expand value set default count of "
				+ (appProperties.getPre_expand_value_sets_default_count().toString()));
		ourLog.info("Server configured for pre-expand value set max count of "
				+ (appProperties.getPre_expand_value_sets_max_count().toString()));
		ourLog.info("Server configured for maximum expansion size of "
				+ (appProperties.getMaximum_expansion_size().toString()));
	}

	@Bean
	public SubscriptionSettings subscriptionSettings(AppProperties appProperties) {
		SubscriptionSettings subscriptionSettings = new SubscriptionSettings();
		if (appProperties.getSubscription() != null) {
			if (appProperties.getSubscription().getEmail() != null)
				subscriptionSettings.setEmailFromAddress(
						appProperties.getSubscription().getEmail().getFrom());

			// Subscriptions are enabled by channel type
			if (appProperties.getSubscription().getResthook_enabled()) {
				ourLog.info("Enabling REST-hook subscriptions");
				subscriptionSettings.addSupportedSubscriptionType(
						org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.RESTHOOK);
			}
			if (appProperties.getSubscription().getEmail() != null) {
				ourLog.info("Enabling email subscriptions");
				subscriptionSettings.addSupportedSubscriptionType(
						org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.EMAIL);
			}
			if (appProperties.getSubscription().getWebsocket_enabled()) {
				ourLog.info("Enabling websocket subscriptions");
				subscriptionSettings.addSupportedSubscriptionType(
						org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.WEBSOCKET);
			}
			if (appProperties.getSubscription().getPolling_interval_ms() != null) {
				ourLog.info(
						"Setting subscription polling interval to {} ms",
						appProperties.getSubscription().getPolling_interval_ms());
				subscriptionSettings.setSubscriptionIntervalInMs(
						appProperties.getSubscription().getPolling_interval_ms());
			}
			if (appProperties.getSubscription().getImmediately_queued()) {
				ourLog.info("Subscription update will be queued immediately");
				subscriptionSettings.setSubscriptionChangeQueuedImmediately(
						appProperties.getSubscription().getImmediately_queued());
			}
		}
		if (appProperties.getMdm_enabled()) {
			// MDM requires the subscription of type message
			ourLog.info("Enabling message subscriptions");
			subscriptionSettings.addSupportedSubscriptionType(
					org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.MESSAGE);
		}
		return subscriptionSettings;
	}
	/**
	 * Configure FHIR properties around the JPA server via this bean
	 */
	@Bean
	public JpaStorageSettings jpaStorageSettings(AppProperties appProperties) {
		JpaStorageSettings jpaStorageSettings = new JpaStorageSettings();

		jpaStorageSettings.setPreExpandValueSets(appProperties.getPre_expand_value_sets());
		jpaStorageSettings.setEnableTaskPreExpandValueSets(appProperties.getEnable_task_pre_expand_value_sets());
		jpaStorageSettings.setPreExpandValueSetsMaxCount(appProperties.getPre_expand_value_sets_max_count());
		jpaStorageSettings.setPreExpandValueSetsDefaultCount(appProperties.getPre_expand_value_sets_default_count());
		jpaStorageSettings.setMaximumExpansionSize(appProperties.getMaximum_expansion_size());

		jpaStorageSettings.setIndexMissingFields(
				appProperties.getEnable_index_missing_fields()
						? StorageSettings.IndexEnabledEnum.ENABLED
						: StorageSettings.IndexEnabledEnum.DISABLED);
		jpaStorageSettings.setAutoCreatePlaceholderReferenceTargets(
				appProperties.getAuto_create_placeholder_reference_targets());
		jpaStorageSettings.setMassIngestionMode(appProperties.getMass_ingestion_mode_enabled());
		jpaStorageSettings.setAutoVersionReferenceAtPaths(appProperties.getAuto_version_reference_at_paths());
		jpaStorageSettings.setEnforceReferentialIntegrityOnWrite(
				appProperties.getEnforce_referential_integrity_on_write());
		jpaStorageSettings.setEnforceReferentialIntegrityOnDelete(
				appProperties.getEnforce_referential_integrity_on_delete());
		jpaStorageSettings.setAllowContainsSearches(appProperties.getAllow_contains_searches());
		jpaStorageSettings.setAllowMultipleDelete(appProperties.getAllow_multiple_delete());
		jpaStorageSettings.setAllowExternalReferences(appProperties.getAllow_external_references());
		jpaStorageSettings.setSchedulingDisabled(!appProperties.getDao_scheduling_enabled());
		jpaStorageSettings.setIndexStorageOptimized(appProperties.getIndex_storage_optimized());
		jpaStorageSettings.setMatchUrlCacheEnabled(appProperties.getMatch_url_cache_enabled());
		jpaStorageSettings.setDeleteEnabled(appProperties.getDelete_enabled());
		jpaStorageSettings.setDeleteExpungeEnabled(appProperties.getDelete_expunge_enabled());
		jpaStorageSettings.setExpungeEnabled(appProperties.getExpunge_enabled());
		jpaStorageSettings.setLanguageSearchParameterEnabled(appProperties.getLanguage_search_parameter_enabled());
		jpaStorageSettings.setValidateResourceStatusForPackageUpload(
				appProperties.getValidate_resource_status_for_package_upload());
		jpaStorageSettings.setIndexOnUpliftedRefchains(appProperties.getUpliftedRefchains_enabled());

		if (!appProperties.getSearch_prefetch_thresholds().isEmpty()) {
			jpaStorageSettings.setSearchPreFetchThresholds(appProperties.getSearch_prefetch_thresholds());
		}

		Integer maxFetchSize = appProperties.getMax_page_size();
		jpaStorageSettings.setFetchSizeDefaultMaximum(maxFetchSize);
		ourLog.info("Server configured to have a maximum fetch size of "
				+ (maxFetchSize == Integer.MAX_VALUE ? "'unlimited'" : maxFetchSize));

		Long reuseCachedSearchResultsMillis = appProperties.getReuse_cached_search_results_millis();
		jpaStorageSettings.setReuseCachedSearchResultsForMillis(reuseCachedSearchResultsMillis);
		ourLog.info("Server configured to cache search results for {} milliseconds", reuseCachedSearchResultsMillis);

		Long retainCachedSearchesMinutes = appProperties.getRetain_cached_searches_mins();
		jpaStorageSettings.setExpireSearchResultsAfterMillis(retainCachedSearchesMinutes * 60 * 1000);

		jpaStorageSettings.setFilterParameterEnabled(appProperties.getFilter_search_enabled());
		jpaStorageSettings.setHibernateSearchIndexSearchParams(appProperties.getAdvanced_lucene_indexing());
		jpaStorageSettings.setHibernateSearchIndexFullText(appProperties.getSearch_index_full_text_enabled());
		jpaStorageSettings.setTreatBaseUrlsAsLocal(new HashSet<>(appProperties.getLocal_base_urls()));
		jpaStorageSettings.setTreatReferencesAsLogical(new HashSet<>(appProperties.getLogical_urls()));

		if (appProperties.getLastn_enabled()) {
			jpaStorageSettings.setLastNEnabled(true);
		}

		if (appProperties.getInline_resource_storage_below_size() != 0) {
			jpaStorageSettings.setInlineResourceTextBelowSize(appProperties.getInline_resource_storage_below_size());
		}

		jpaStorageSettings.setStoreResourceInHSearchIndex(appProperties.getStore_resource_in_lucene_index_enabled());
		jpaStorageSettings.setNormalizedQuantitySearchLevel(appProperties.getNormalized_quantity_search_level());
		jpaStorageSettings.setIndexOnContainedResources(appProperties.getEnable_index_contained_resource());

		if (appProperties.getAllowed_bundle_types() != null) {
			jpaStorageSettings.setBundleTypesAllowedForStorage(appProperties.getAllowed_bundle_types().stream()
					.map(BundleType::toCode)
					.collect(Collectors.toSet()));
		}

		jpaStorageSettings.setDeferIndexingForCodesystemsOfSize(
				appProperties.getDefer_indexing_for_codesystems_of_size());

		jpaStorageSettings.setResourceClientIdStrategy(appProperties.getClient_id_strategy());
		ourLog.info("Server configured to use '" + appProperties.getClient_id_strategy() + "' Client ID Strategy");

		// Set and/or recommend default Server ID Strategy of UUID when using the ANY Client ID Strategy
		if (appProperties.getClient_id_strategy() == JpaStorageSettings.ClientIdStrategyEnum.ANY) {
			if (appProperties.getServer_id_strategy() == null) {
				ourLog.info("Defaulting server to use '" + JpaStorageSettings.IdStrategyEnum.UUID
						+ "' Server ID Strategy when using the '" + JpaStorageSettings.ClientIdStrategyEnum.ANY
						+ "' Client ID Strategy");
				appProperties.setServer_id_strategy(JpaStorageSettings.IdStrategyEnum.UUID);
			} else if (appProperties.getServer_id_strategy() != JpaStorageSettings.IdStrategyEnum.UUID) {
				ourLog.warn("WARNING: '" + JpaStorageSettings.IdStrategyEnum.UUID
						+ "' Server ID Strategy is highly recommended when using the '"
						+ JpaStorageSettings.ClientIdStrategyEnum.ANY + "' Client ID Strategy");
			}
		}
		if (appProperties.getServer_id_strategy() != null) {
			jpaStorageSettings.setResourceServerIdStrategy(appProperties.getServer_id_strategy());
			ourLog.info("Server configured to use '" + appProperties.getServer_id_strategy() + "' Server ID Strategy");
		}

		// to Disable the Resource History
		jpaStorageSettings.setResourceDbHistoryEnabled(appProperties.getResource_dbhistory_enabled());

		// Parallel Batch GET execution settings
		jpaStorageSettings.setBundleBatchPoolSize(appProperties.getBundle_batch_pool_size());
		jpaStorageSettings.setBundleBatchPoolSize(appProperties.getBundle_batch_pool_max_size());

		// Set store meta source information
		ourLog.debug("Server configured to Store Meta Source: {}", appProperties.getStore_meta_source_information());
		jpaStorageSettings.setStoreMetaSourceInformation(appProperties.getStore_meta_source_information());

		storageSettings(appProperties, jpaStorageSettings);
		return jpaStorageSettings;
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
			boolean databasePartitionModeEnabled =
					defaultIfNull(appProperties.getPartitioning().getDatabase_partition_mode_enabled(), Boolean.FALSE);
			Integer defaultPartitionId = appProperties.getPartitioning().getDefault_partition_id();
			if (databasePartitionModeEnabled) {
				retVal.setDatabasePartitionMode(true);
				defaultPartitionId = defaultIfNull(defaultPartitionId, 0);
			}
			retVal.setDefaultPartitionId(defaultPartitionId);
			retVal.setIncludePartitionInSearchHashes(
					appProperties.getPartitioning().getPartitioning_include_in_search_hashes());
			if (appProperties.getPartitioning().getAllow_references_across_partitions()) {
				retVal.setAllowReferencesAcrossPartitions(CrossPartitionReferenceMode.ALLOWED_UNQUALIFIED);
			} else {
				retVal.setAllowReferencesAcrossPartitions(CrossPartitionReferenceMode.NOT_ALLOWED);
			}
			retVal.setConditionalCreateDuplicateIdentifiersEnabled(
					appProperties.getPartitioning().getConditional_create_duplicate_identifiers_enabled());

			ourLog.info(
					"""
					Partitioning is enabled on this server. Settings:
					 * Database Partition Mode Enabled: {}
					 * Default Partition ID           : {}
					 * Cross-Partition References     : {}""",
					databasePartitionModeEnabled,
					defaultPartitionId,
					retVal.getAllowReferencesAcrossPartitions());

		} else {
			ourLog.info("Partitioning is not enabled on this server");
		}

		return retVal;
	}

	@Bean
	public PartitionModeConfigurer partitionModeConfigurer() {
		return new PartitionModeConfigurer();
	}

	@Primary
	@Bean
	public HibernatePropertiesProvider jpaStarterDialectProvider(
			LocalContainerEntityManagerFactoryBean myEntityManagerFactory) {
		return new JpaHibernatePropertiesProvider(myEntityManagerFactory);
	}

	protected StorageSettings storageSettings(AppProperties appProperties, JpaStorageSettings jpaStorageSettings) {
		jpaStorageSettings.setAllowContainsSearches(appProperties.getAllow_contains_searches());
		jpaStorageSettings.setAllowExternalReferences(appProperties.getAllow_external_references());
		jpaStorageSettings.setDefaultSearchParamsCanBeOverridden(
				appProperties.getAllow_override_default_search_params());

		jpaStorageSettings.setNormalizedQuantitySearchLevel(appProperties.getNormalized_quantity_search_level());

		jpaStorageSettings.setIndexOnContainedResources(appProperties.getEnable_index_contained_resource());
		jpaStorageSettings.setIndexIdentifierOfType(appProperties.getEnable_index_of_type());
		return jpaStorageSettings;
	}

	@Lazy
	@Bean
	public IBinaryStorageSvc binaryStorageSvc(AppProperties appProperties) {
		DatabaseBinaryContentStorageSvcImpl binaryStorageSvc = new DatabaseBinaryContentStorageSvcImpl();

		if (appProperties.getMax_binary_size() != null) {
			binaryStorageSvc.setMaximumBinarySize(appProperties.getMax_binary_size());
		}

		return binaryStorageSvc;
	}

	@Bean
	public IEmailSender emailSender(AppProperties appProperties) {
		if (appProperties.getSubscription() != null
				&& appProperties.getSubscription().getEmail() != null) {

			return buildEmailSender(appProperties.getSubscription().getEmail());
		}

		// Return a dummy anonymous function instead of null. Spring does not like null beans.
		// TODO Get the signature of
		// ca.uhn.fhir.jpa.subscription.channel.subscription.SubscriptionDeliveryHandlerFactory
		//  changed so it does not require an instance of an IEmailSender
		return theDetails -> {};
	}

	private static IEmailSender buildEmailSender(AppProperties.Subscription.Email email) {

		return new EmailSenderImpl(new MailSvc(new MailConfig()
				.setSmtpHostname(email.getHost())
				.setSmtpPort(email.getPort())
				.setSmtpUsername(email.getUsername())
				.setSmtpPassword(email.getPassword())
				.setSmtpUseStartTLS(email.getStartTlsEnable())));
	}
}
