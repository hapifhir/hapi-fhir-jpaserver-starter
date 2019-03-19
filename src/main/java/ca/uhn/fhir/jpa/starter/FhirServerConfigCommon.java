package ca.uhn.fhir.jpa.starter;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.instance.model.Subscription;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.model.entity.ModelConfig;
import ca.uhn.fhir.jpa.subscription.module.subscriber.email.IEmailSender;
import ca.uhn.fhir.jpa.subscription.module.subscriber.email.JavaMailEmailSender;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

/**
 * This is the primary configuration file for the example server
 */
@Configuration
@EnableTransactionManagement()
public class FhirServerConfigCommon {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FhirServerConfigCommon.class);

	private Boolean allowContainsSearches = HapiProperties.getAllowContainsSearches();
	private Boolean allowMultipleDelete = HapiProperties.getAllowMultipleDelete();
	private Boolean allowExternalReferences = HapiProperties.getAllowExternalReferences();
	private Boolean expungeEnabled = HapiProperties.getExpungeEnabled();
	private Boolean allowPlaceholderReferences = HapiProperties.getAllowPlaceholderReferences();
	private Boolean subscriptionRestHookEnabled = HapiProperties.getSubscriptionRestHookEnabled();
	private Boolean subscriptionEmailEnabled = HapiProperties.getSubscriptionEmailEnabled();
	private Boolean allowOverrideDefaultSearchParams = HapiProperties.getAllowOverrideDefaultSearchParams();
	private String emailFrom = HapiProperties.getEmailFrom();
	private Boolean emailEnabled = HapiProperties.getEmailEnabled();
	private String emailHost = HapiProperties.getEmailHost();
	private Integer emailPort = HapiProperties.getEmailPort();
	private String emailUsername = HapiProperties.getEmailUsername();
	private String emailPassword = HapiProperties.getEmailPassword();

	public FhirServerConfigCommon() {
		ourLog.info("Server configured to " + (this.allowContainsSearches ? "allow" : "deny") + " contains searches");
		ourLog.info("Server configured to " + (this.allowMultipleDelete ? "allow" : "deny") + " multiple deletes");
		ourLog.info("Server configured to " + (this.allowExternalReferences ? "allow" : "deny") + " external references");
		ourLog.info("Server configured to " + (this.expungeEnabled ? "enable" : "disable") + " expunges");
		ourLog.info("Server configured to " + (this.allowPlaceholderReferences ? "allow" : "deny") + " placeholder references");
		ourLog.info("Server configured to " + (this.allowOverrideDefaultSearchParams ? "allow" : "deny") + " overriding default search params");

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
	public DaoConfig daoConfig() {
		DaoConfig retVal = new DaoConfig();

		retVal.setAllowContainsSearches(this.allowContainsSearches);
		retVal.setAllowMultipleDelete(this.allowMultipleDelete);
		retVal.setAllowExternalReferences(this.allowExternalReferences);
		retVal.setExpungeEnabled(this.expungeEnabled);
		retVal.setAutoCreatePlaceholderReferenceTargets(this.allowPlaceholderReferences);
		retVal.setEmailFromAddress(this.emailFrom);

		Integer maxFetchSize = HapiProperties.getMaximumFetchSize();
		retVal.setFetchSizeDefaultMaximum(maxFetchSize);
		ourLog.info("Server configured to have a maximum fetch size of " + (maxFetchSize == Integer.MAX_VALUE? "'unlimited'": maxFetchSize));
		
		// Subscriptions are enabled by channel type
		if (HapiProperties.getSubscriptionRestHookEnabled()) {
			ourLog.info("Enabling REST-hook subscriptions");
			retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.RESTHOOK);
		}
		if (HapiProperties.getSubscriptionEmailEnabled()) {
			ourLog.info("Enabling email subscriptions");
			retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.EMAIL);
		}
		if (HapiProperties.getSubscriptionWebsocketEnabled()) {
			ourLog.info("Enabling websocket subscriptions");
			retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.WEBSOCKET);
		}

		return retVal;
	}

	@Bean
	public ModelConfig modelConfig() {
		ModelConfig modelConfig = new ModelConfig();
		modelConfig.setAllowContainsSearches(this.allowContainsSearches);
		modelConfig.setAllowExternalReferences(this.allowExternalReferences);
		modelConfig.setDefaultSearchParamsCanBeOverridden(this.allowOverrideDefaultSearchParams);
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
	 * 
	 * A URL to a remote database could also be placed here, along with login credentials and other properties supported by BasicDataSource.
	 */
	@Bean(destroyMethod = "close")
	public BasicDataSource dataSource() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		return DataSourceHolder.INSTANCE.getDataSource();
	}


	/**
	 * Do some fancy logging to create a nice access log that has details about each incoming request.
	 */
	public IServerInterceptor loggingInterceptor() {
		LoggingInterceptor retVal = new LoggingInterceptor();
		retVal.setLoggerName(HapiProperties.getLoggerName());
		retVal.setMessageFormat(HapiProperties.getLoggerFormat());
		retVal.setErrorMessageFormat(HapiProperties.getLoggerErrorFormat());
		retVal.setLogExceptions(HapiProperties.getLoggerLogExceptions());
		return retVal;
	}

	/**
	 * This interceptor adds some pretty syntax highlighting in responses when a browser is detected
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	public IServerInterceptor responseHighlighterInterceptor() {
		ResponseHighlighterInterceptor retVal = new ResponseHighlighterInterceptor();
		return retVal;
	}

	@Bean()
	public IEmailSender emailSender() {
		if (this.emailEnabled) {
			JavaMailEmailSender retVal = new JavaMailEmailSender();

			retVal.setSmtpServerHostname(this.emailHost);
			retVal.setSmtpServerPort(this.emailPort);
			retVal.setSmtpServerUsername(this.emailUsername);
			retVal.setSmtpServerPassword(this.emailPassword);

			return retVal;
		}

		return null;
	}
}
