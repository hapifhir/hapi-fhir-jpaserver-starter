package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.binstore.DatabaseBlobBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.binstore.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.entity.ModelConfig;
import ca.uhn.fhir.jpa.subscription.channel.subscription.SubscriptionDeliveryHandlerFactory;
import ca.uhn.fhir.jpa.subscription.match.deliver.email.IEmailSender;
import ca.uhn.fhir.jpa.subscription.match.deliver.email.JavaMailEmailSender;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.dstu2.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.thymeleaf.util.Validate;

import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;

/**
 * This is the primary configuration file for the example server
 */
@Configuration
@EnableTransactionManagement
public class FhirServerConfigCommon {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FhirServerConfigCommon.class);



  private Boolean subscriptionRestHookEnabled = HapiProperties.getSubscriptionRestHookEnabled();
  private Boolean subscriptionEmailEnabled = HapiProperties.getSubscriptionEmailEnabled();

  private String emailFrom = HapiProperties.getEmailFrom();
  private Boolean emailEnabled = HapiProperties.getEmailEnabled();
  private String emailHost = HapiProperties.getEmailHost();
  private Integer emailPort = HapiProperties.getEmailPort();
  private String emailUsername = HapiProperties.getEmailUsername();
  private String emailPassword = HapiProperties.getEmailPassword();
  private Boolean emailAuth = HapiProperties.getEmailAuth();
  private Boolean emailStartTlsEnable = HapiProperties.getEmailStartTlsEnable();
  private Boolean emailStartTlsRequired = HapiProperties.getEmailStartTlsRequired();
  private Boolean emailQuitWait = HapiProperties.getEmailQuitWait();


  @Autowired
  private ApplicationContext appContext;

  public FhirServerConfigCommon(AppProperties appProperties) {
    ourLog.info("Server configured to " + (appProperties.getAllow_contains_searches() ? "allow" : "deny") + " contains searches");
    ourLog.info("Server configured to " + (appProperties.getAllow_multiple_delete() ? "allow" : "deny") + " multiple deletes");
    ourLog.info("Server configured to " + (appProperties.getAllow_external_references() ? "allow" : "deny") + " external references");
    ourLog.info("Server configured to " + (appProperties.getExpunge_enabled() ? "enable" : "disable") + " expunges");
    ourLog.info("Server configured to " + (appProperties.getAllow_placeholder_references() ? "allow" : "deny") + " placeholder references");
    ourLog.info("Server configured to " + (appProperties.getAllow_override_default_search_params() ? "allow" : "deny") + " overriding default search params");

    if (this.emailEnabled) {
      ourLog.info("Server is configured to enable email with host '" + this.emailHost + "' and port " + this.emailPort.toString());
      ourLog.info("Server will use '" + this.emailFrom + "' as the from email address");

      if (this.emailUsername != null && this.emailUsername.length() > 0) {
        ourLog.info("Server is configured to use username '" + this.emailUsername + "' for email");
      }

      if (this.emailPassword != null && this.emailPassword.length() > 0) {
        ourLog.info("Server is configured to use a password for email");
      }
    }

    if (this.subscriptionRestHookEnabled) {
      ourLog.info("REST-hook subscriptions enabled");
    }

    if (this.subscriptionEmailEnabled) {
      ourLog.info("Email subscriptions enabled");
    }
  }

  /**
   * Configure FHIR properties around the the JPA server via this bean
   */
  @Bean()
  public DaoConfig daoConfig(AppProperties appProperties) {
    DaoConfig retVal = new DaoConfig();

    retVal.setIndexMissingFields(appProperties.getEnable_index_missing_fields() ? DaoConfig.IndexEnabledEnum.ENABLED : DaoConfig.IndexEnabledEnum.DISABLED);
    retVal.setAutoCreatePlaceholderReferenceTargets(appProperties.getAuto_create_placeholder_reference_targets());
    retVal.setEnforceReferentialIntegrityOnWrite(appProperties.getEnforce_referential_integrity_on_write());
    retVal.setEnforceReferentialIntegrityOnDelete(appProperties.getEnforce_referential_integrity_on_delete());
    retVal.setAllowContainsSearches(appProperties.getAllow_contains_searches());
    retVal.setAllowMultipleDelete(appProperties.getAllow_multiple_delete());
    retVal.setAllowExternalReferences(appProperties.getAllow_external_references());
    retVal.setExpungeEnabled(appProperties.getExpunge_enabled());
    retVal.setAutoCreatePlaceholderReferenceTargets(appProperties.getAllow_placeholder_references());
    retVal.setEmailFromAddress(this.emailFrom);

    Integer maxFetchSize =  appProperties.getMax_page_size();
    retVal.setFetchSizeDefaultMaximum(maxFetchSize);
    ourLog.info("Server configured to have a maximum fetch size of " + (maxFetchSize == Integer.MAX_VALUE ? "'unlimited'" : maxFetchSize));

    Long reuseCachedSearchResultsMillis = appProperties.getReuse_cached_search_results_millis();
    retVal.setReuseCachedSearchResultsForMillis(reuseCachedSearchResultsMillis);
    ourLog.info("Server configured to cache search results for {} milliseconds", reuseCachedSearchResultsMillis);

    Long retainCachedSearchesMinutes = HapiProperties.getExpireSearchResultsAfterMins();
    retVal.setExpireSearchResultsAfterMillis(retainCachedSearchesMinutes * 60 * 1000);

    // Subscriptions are enabled by channel type
    if (appProperties.getSubscription().getResthook_enabled()) {
      ourLog.info("Enabling REST-hook subscriptions");
      retVal.addSupportedSubscriptionType(org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.RESTHOOK);
    }
    if (appProperties.getSubscription().getEmail_enabled()) {
      ourLog.info("Enabling email subscriptions");
      retVal.addSupportedSubscriptionType(org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.EMAIL);
    }
    if (appProperties.getSubscription().getWebsocket_enabled()) {
      ourLog.info("Enabling websocket subscriptions");
      retVal.addSupportedSubscriptionType(org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.WEBSOCKET);
    }

    retVal.setFilterParameterEnabled(appProperties.getFilter_search_enabled());

    return retVal;
  }

  @Bean
  public PartitionSettings partitionSettings(AppProperties appProperties) {
    PartitionSettings retVal = new PartitionSettings();

    // Partitioning
    if (HapiProperties.getPartitioningMultitenancyEnabled()) {
      retVal.setPartitioningEnabled(true);
    }

    return retVal;
  }


  @Bean
  public ModelConfig modelConfig(AppProperties appProperties) {
    ModelConfig modelConfig = new ModelConfig();
    modelConfig.setAllowContainsSearches(appProperties.getAllow_contains_searches());
    modelConfig.setAllowExternalReferences(appProperties.getAllow_external_references());
    modelConfig.setDefaultSearchParamsCanBeOverridden(appProperties.getAllow_override_default_search_params());
    modelConfig.setEmailFromAddress(this.emailFrom);

    // You can enable these if you want to support Subscriptions from your server
    if (this.subscriptionRestHookEnabled) {
      modelConfig.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.RESTHOOK);
    }

    if (this.subscriptionEmailEnabled) {
      modelConfig.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.EMAIL);
    }

    return modelConfig;
  }

  /**
   * The following bean configures the database connection. The 'url' property value of "jdbc:derby:directory:jpaserver_derby_files;create=true" indicates that the server should save resources in a
   * directory called "jpaserver_derby_files".
   * <p>
   * A URL to a remote database could also be placed here, along with login credentials and other properties supported by BasicDataSource.
   */
  /*@Bean(destroyMethod = "close")
  public BasicDataSource dataSource() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    BasicDataSource retVal = new BasicDataSource();
    Driver driver = (Driver) Class.forName(HapiProperties.getDataSourceDriver()).getConstructor().newInstance();
    retVal.setDriver(driver);
    retVal.setUrl(HapiProperties.getDataSourceUrl());
    retVal.setUsername(HapiProperties.getDataSourceUsername());
    retVal.setPassword(HapiProperties.getDataSourcePassword());
    retVal.setMaxTotal(HapiProperties.getDataSourceMaxPoolSize());
    return retVal;
  }*/

  @Lazy
  @Bean
  public IBinaryStorageSvc binaryStorageSvc() {
    DatabaseBlobBinaryStorageSvcImpl binaryStorageSvc = new DatabaseBlobBinaryStorageSvcImpl();

    if (HapiProperties.getMaxBinarySize() != null) {
      binaryStorageSvc.setMaximumBinarySize(HapiProperties.getMaxBinarySize());
    }

    return binaryStorageSvc;
  }

  @Bean()
  public IEmailSender emailSender() {
    if (this.emailEnabled) {
      JavaMailEmailSender retVal = new JavaMailEmailSender();

      retVal.setSmtpServerHostname(this.emailHost);
      retVal.setSmtpServerPort(this.emailPort);
      retVal.setSmtpServerUsername(this.emailUsername);
      retVal.setSmtpServerPassword(this.emailPassword);
      retVal.setAuth(this.emailAuth);
      retVal.setStartTlsEnable(this.emailStartTlsEnable);
      retVal.setStartTlsRequired(this.emailStartTlsRequired);
      retVal.setQuitWait(this.emailQuitWait);

      SubscriptionDeliveryHandlerFactory subscriptionDeliveryHandlerFactory = appContext.getBean(SubscriptionDeliveryHandlerFactory.class);
      Validate.notNull(subscriptionDeliveryHandlerFactory, "No subscription delivery handler");
      subscriptionDeliveryHandlerFactory.setEmailSender(retVal);


      return retVal;
    }

    return null;
  }
}
