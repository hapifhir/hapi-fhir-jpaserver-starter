package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.api.config.JpaStorageSettings;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.binstore.DatabaseBlobBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.config.HibernatePropertiesProvider;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.config.PartitionSettings.CrossPartitionReferenceMode;
import ca.uhn.fhir.jpa.model.entity.StorageSettings;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.JpaHibernatePropertiesProvider;
import ca.uhn.fhir.jpa.subscription.match.deliver.email.EmailSenderImpl;
import ca.uhn.fhir.jpa.subscription.match.deliver.email.IEmailSender;
import ca.uhn.fhir.rest.server.mail.MailConfig;
import ca.uhn.fhir.rest.server.mail.MailSvc;
import com.google.common.base.Strings;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * This is the primary configuration file for the example server
 */
@Configuration
@EnableTransactionManagement
public class FhirServerConfigCommon {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FhirServerConfigCommon.class);

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
	}

	/**
	 * Configure FHIR properties around the the JPA server via this bean
	 */
	@Bean
	public JpaStorageSettings jpaStorageSettings(AppProperties appProperties) {
		JpaStorageSettings jpaStorageSettings = new JpaStorageSettings();

		jpaStorageSettings.setIndexMissingFields(
				appProperties.getEnable_index_missing_fields()
						? StorageSettings.IndexEnabledEnum.ENABLED
						: StorageSettings.IndexEnabledEnum.DISABLED);
		jpaStorageSettings.setAutoCreatePlaceholderReferenceTargets(
				appProperties.getAuto_create_placeholder_reference_targets());
		jpaStorageSettings.setAutoVersionReferenceAtPaths(appProperties.getAuto_version_reference_at_paths());
		jpaStorageSettings.setEnforceReferentialIntegrityOnWrite(
				appProperties.getEnforce_referential_integrity_on_write());
		jpaStorageSettings.setEnforceReferentialIntegrityOnDelete(
				appProperties.getEnforce_referential_integrity_on_delete());
		jpaStorageSettings.setAllowContainsSearches(appProperties.getAllow_contains_searches());
		jpaStorageSettings.setAllowMultipleDelete(appProperties.getAllow_multiple_delete());
		jpaStorageSettings.setAllowExternalReferences(appProperties.getAllow_external_references());
		jpaStorageSettings.setSchedulingDisabled(!appProperties.getDao_scheduling_enabled());
		jpaStorageSettings.setDeleteExpungeEnabled(appProperties.getDelete_expunge_enabled());
		jpaStorageSettings.setExpungeEnabled(appProperties.getExpunge_enabled());
		if (appProperties.getSubscription() != null
				&& appProperties.getSubscription().getEmail() != null)
			jpaStorageSettings.setEmailFromAddress(
					appProperties.getSubscription().getEmail().getFrom());

		Integer maxFetchSize = appProperties.getMax_page_size();
		jpaStorageSettings.setFetchSizeDefaultMaximum(maxFetchSize);
		ourLog.info("Server configured to have a maximum fetch size of "
				+ (maxFetchSize == Integer.MAX_VALUE ? "'unlimited'" : maxFetchSize));

		Long reuseCachedSearchResultsMillis = appProperties.getReuse_cached_search_results_millis();
		jpaStorageSettings.setReuseCachedSearchResultsForMillis(reuseCachedSearchResultsMillis);
		ourLog.info("Server configured to cache search results for {} milliseconds", reuseCachedSearchResultsMillis);

		Long retainCachedSearchesMinutes = appProperties.getRetain_cached_searches_mins();
		jpaStorageSettings.setExpireSearchResultsAfterMillis(retainCachedSearchesMinutes * 60 * 1000);

		if (appProperties.getSubscription() != null) {
			// Subscriptions are enabled by channel type
			if (appProperties.getSubscription().getResthook_enabled()) {
				ourLog.info("Enabling REST-hook subscriptions");
				jpaStorageSettings.addSupportedSubscriptionType(
						org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.RESTHOOK);
			}
			if (appProperties.getSubscription().getEmail() != null) {
				ourLog.info("Enabling email subscriptions");
				jpaStorageSettings.addSupportedSubscriptionType(
						org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.EMAIL);
			}
			if (appProperties.getSubscription().getWebsocket_enabled()) {
				ourLog.info("Enabling websocket subscriptions");
				jpaStorageSettings.addSupportedSubscriptionType(
						org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.WEBSOCKET);
			}
		}

		jpaStorageSettings.setFilterParameterEnabled(appProperties.getFilter_search_enabled());
		jpaStorageSettings.setAdvancedHSearchIndexing(appProperties.getAdvanced_lucene_indexing());
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

		if (appProperties.getClient_id_strategy() == JpaStorageSettings.ClientIdStrategyEnum.ANY) {
			jpaStorageSettings.setResourceServerIdStrategy(JpaStorageSettings.IdStrategyEnum.UUID);
			jpaStorageSettings.setResourceClientIdStrategy(appProperties.getClient_id_strategy());
		}
		// Parallel Batch GET execution settings
		jpaStorageSettings.setBundleBatchPoolSize(appProperties.getBundle_batch_pool_size());
		jpaStorageSettings.setBundleBatchPoolSize(appProperties.getBundle_batch_pool_max_size());

		if (appProperties.getMdm_enabled()) {
			// MDM requires the subscription of type message
			ourLog.info("Enabling message subscriptions");
			jpaStorageSettings.addSupportedSubscriptionType(
					org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.MESSAGE);
		}

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
			retVal.setIncludePartitionInSearchHashes(
					appProperties.getPartitioning().getPartitioning_include_in_search_hashes());
			if (appProperties.getPartitioning().getAllow_references_across_partitions()) {
				retVal.setAllowReferencesAcrossPartitions(CrossPartitionReferenceMode.ALLOWED_UNQUALIFIED);
			} else {
				retVal.setAllowReferencesAcrossPartitions(CrossPartitionReferenceMode.NOT_ALLOWED);
			}
		}

		return retVal;
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
		if (appProperties.getSubscription() != null
				&& appProperties.getSubscription().getEmail() != null)
			jpaStorageSettings.setEmailFromAddress(
					appProperties.getSubscription().getEmail().getFrom());

		jpaStorageSettings.setNormalizedQuantitySearchLevel(appProperties.getNormalized_quantity_search_level());

		jpaStorageSettings.setIndexOnContainedResources(appProperties.getEnable_index_contained_resource());
		jpaStorageSettings.setIndexIdentifierOfType(appProperties.getEnable_index_of_type());
		return jpaStorageSettings;
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
