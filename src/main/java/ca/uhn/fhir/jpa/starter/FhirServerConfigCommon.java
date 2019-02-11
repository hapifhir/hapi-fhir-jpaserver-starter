package ca.uhn.fhir.jpa.starter;

import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;

import ca.uhn.fhir.jpa.model.entity.ModelConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.instance.model.Subscription;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.util.SubscriptionsRequireManualActivationInterceptorDstu3;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

/**
 * This is the primary configuration file for the example server
 */
@Configuration
@EnableTransactionManagement()
public class FhirServerConfigCommon {

	/**
	 * Configure FHIR properties around the the JPA server via this bean
	 */
	@Bean()
	public DaoConfig daoConfig() {
		DaoConfig retVal = new DaoConfig();
		retVal.setAllowMultipleDelete(HapiProperties.getAllowMultipleDelete());
		retVal.setAllowExternalReferences(HapiProperties.getAllowExternalReferences());
		retVal.setExpungeEnabled(HapiProperties.getExpungeEnabled());

		// You can enable these if you want to support Subscriptions from your server
		if (false) {
			retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.RESTHOOK);
		}
		if (false) {
			retVal.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.EMAIL);
		}

    return retVal;
	}

	@Bean
	public ModelConfig modelConfig() {
		return new ModelConfig();
	}

	/**
	 * The following bean configures the database connection. The 'url' property value of "jdbc:derby:directory:jpaserver_derby_files;create=true" indicates that the server should save resources in a
	 * directory called "jpaserver_derby_files".
	 * 
	 * A URL to a remote database could also be placed here, along with login credentials and other properties supported by BasicDataSource.
	 */
	@Bean(destroyMethod = "close")
	public BasicDataSource dataSource() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		BasicDataSource retVal = new BasicDataSource();
		Driver driver = (Driver) Class.forName(HapiProperties.getDataSourceDriver()).getConstructor().newInstance();
		retVal.setDriver(driver);
		retVal.setUrl(HapiProperties.getDataSourceUrl());
		retVal.setUsername(HapiProperties.getDataSourceUsername());
		retVal.setPassword(HapiProperties.getDataSourcePassword());
		return retVal;
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

}
