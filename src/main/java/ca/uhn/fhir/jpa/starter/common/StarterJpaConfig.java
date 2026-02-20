package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.jpa.api.IDaoRegistry;
import ca.uhn.fhir.jpa.api.config.JpaStorageSettings;
import ca.uhn.fhir.jpa.api.config.ThreadPoolFactoryConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.config.r4.JpaR4Config;
import ca.uhn.fhir.jpa.config.util.HapiEntityManagerFactoryUtil;
import ca.uhn.fhir.jpa.config.util.ResourceCountCacheUtil;
import ca.uhn.fhir.jpa.dao.FulltextSearchSvcImpl;
import ca.uhn.fhir.jpa.dao.IFulltextSearchSvc;
import ca.uhn.fhir.jpa.dao.search.HSearchSortHelperImpl;
import ca.uhn.fhir.jpa.dao.search.IHSearchSortHelper;
import ca.uhn.fhir.jpa.delete.ThreadSafeResourceDeleterSvc;
import ca.uhn.fhir.jpa.interceptor.CascadingDeleteInterceptor;
import ca.uhn.fhir.jpa.interceptor.UserRequestRetryVersionConflictsInterceptor;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor;
import ca.uhn.fhir.jpa.provider.DaoRegistryResourceSupportedSvc;
import ca.uhn.fhir.jpa.provider.DiffProvider;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.provider.JpaCapabilityStatementProvider;
import ca.uhn.fhir.jpa.provider.TerminologyUploaderProvider;
import ca.uhn.fhir.jpa.provider.ValueSetOperationProvider;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.search.IStaleSearchDeletingSvc;
import ca.uhn.fhir.jpa.search.StaleSearchDeletingSvcImpl;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.common.validation.IRepositoryValidationInterceptorFactory;
import ca.uhn.fhir.jpa.starter.interceptor.APIKeyInterceptor;
import ca.uhn.fhir.jpa.starter.terminology.TerminologyCapabilityInterceptor;
import ca.uhn.fhir.jpa.util.ResourceCountCache;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative2.NullNarrativeGenerator;
import ca.uhn.fhir.rest.api.IResourceSupportedSvc;
import ca.uhn.fhir.rest.openapi.OpenApiInterceptor;
import ca.uhn.fhir.rest.server.ApacheProxyAddressStrategy;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IServerConformanceProvider;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.FhirPathFilterInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import com.google.common.base.Strings;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.search.mapper.orm.cfg.HibernateOrmMapperSettings;
import org.hl7.fhir.common.hapi.validation.support.TxResourceValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.web.cors.CorsConfiguration;

import java.util.*;
import javax.sql.DataSource;

import static ca.uhn.fhir.jpa.starter.common.validation.IRepositoryValidationInterceptorFactory.ENABLE_REPOSITORY_VALIDATING_INTERCEPTOR;

@Configuration
@Import({ThreadPoolFactoryConfig.class, JpaR4Config.class})
public class StarterJpaConfig {

	@Bean
	public IFulltextSearchSvc fullTextSearchSvc() {
		return new FulltextSearchSvcImpl();
	}

	@Bean
	public IStaleSearchDeletingSvc staleSearchDeletingSvc() {
		return new StaleSearchDeletingSvcImpl();
	}

	/**
	 * Customize the default/max page sizes for search results. You can set these however
	 * you want, although very large page sizes will require a lot of RAM.
	 */
	@Bean
	public DatabaseBackedPagingProvider databaseBackedPagingProvider(AppProperties appProperties) {
		DatabaseBackedPagingProvider pagingProvider = new DatabaseBackedPagingProvider();
		pagingProvider.setDefaultPageSize(appProperties.getDefault_page_size());
		pagingProvider.setMaximumPageSize(appProperties.getMax_page_size());
		return pagingProvider;
	}

	@Bean
	public IResourceSupportedSvc resourceSupportedSvc(IDaoRegistry theDaoRegistry) {
		return new DaoRegistryResourceSupportedSvc(theDaoRegistry);
	}

	@Bean(name = "myResourceCountsCache")
	public ResourceCountCache resourceCountsCache(IFhirSystemDao<?, ?> theSystemDao) {
		return ResourceCountCacheUtil.newResourceCountCache(theSystemDao);
	}

	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			JpaProperties theJpaProperties,
			DataSource myDataSource,
			ConfigurableListableBeanFactory myConfigurableListableBeanFactory,
			FhirContext theFhirContext,
			JpaStorageSettings theStorageSettings) {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
				HapiEntityManagerFactoryUtil.newEntityManagerFactory(
						myConfigurableListableBeanFactory, theFhirContext, theStorageSettings);

		// Spring Boot Autoconfiguration defaults
		theJpaProperties
				.getProperties()
				.putIfAbsent(AvailableSettings.SCANNER, "org.hibernate.boot.archive.scan.internal.DisabledScanner");
		theJpaProperties
				.getProperties()
				.putIfAbsent(AvailableSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
		theJpaProperties
				.getProperties()
				.putIfAbsent(
						AvailableSettings.PHYSICAL_NAMING_STRATEGY,
						CamelCaseToUnderscoresNamingStrategy.class.getName());

		// Hibernate Search defaults
		theJpaProperties.getProperties().putIfAbsent(AvailableSettings.FORMAT_SQL, "false");
		theJpaProperties.getProperties().putIfAbsent(AvailableSettings.SHOW_SQL, "false");
		theJpaProperties.getProperties().putIfAbsent(AvailableSettings.HBM2DDL_AUTO, "update");
		theJpaProperties.getProperties().putIfAbsent(AvailableSettings.STATEMENT_BATCH_SIZE, "20");
		theJpaProperties.getProperties().putIfAbsent(AvailableSettings.USE_QUERY_CACHE, "false");
		theJpaProperties.getProperties().putIfAbsent(AvailableSettings.USE_SECOND_LEVEL_CACHE, "false");
		theJpaProperties.getProperties().putIfAbsent(AvailableSettings.USE_STRUCTURED_CACHE, "false");
		theJpaProperties.getProperties().putIfAbsent(AvailableSettings.USE_MINIMAL_PUTS, "false");

		// Hibernate Search defaults
		theJpaProperties.getProperties().putIfAbsent(HibernateOrmMapperSettings.ENABLED, "false");

		entityManagerFactoryBean.setPersistenceUnitName("HAPI_PU");
		entityManagerFactoryBean.setJpaPropertyMap(theJpaProperties.getProperties());
		entityManagerFactoryBean.setDataSource(myDataSource);

		return entityManagerFactoryBean;
	}

	@Bean
	@Primary
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager retVal = new JpaTransactionManager();
		retVal.setEntityManagerFactory(entityManagerFactory);
		return retVal;
	}

	@Bean
	public IHSearchSortHelper hSearchSortHelper(ISearchParamRegistry mySearchParamRegistry) {
		return new HSearchSortHelperImpl(mySearchParamRegistry);
	}

	@Bean
	@ConditionalOnProperty(prefix = "hapi.fhir", name = ENABLE_REPOSITORY_VALIDATING_INTERCEPTOR, havingValue = "true")
	public RepositoryValidatingInterceptor repositoryValidatingInterceptor(
			IRepositoryValidationInterceptorFactory factory) {
		return factory.buildUsingStoredStructureDefinitions();
	}

	@Bean
	public LoggingInterceptor loggingInterceptor(AppProperties appProperties) {

		/*
		 * Add some logging for each request
		 */

		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		loggingInterceptor.setLoggerName(appProperties.getLogger().getName());
		loggingInterceptor.setMessageFormat(appProperties.getLogger().getFormat());
		loggingInterceptor.setErrorMessageFormat(appProperties.getLogger().getError_format());
		loggingInterceptor.setLogExceptions(appProperties.getLogger().getLog_exceptions());
		return loggingInterceptor;
	}

	@Bean
	public CorsInterceptor corsInterceptor() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedHeader(HttpHeaders.ORIGIN);
		config.addAllowedHeader(HttpHeaders.ACCEPT);
		config.addAllowedHeader(HttpHeaders.CONTENT_TYPE);
		config.addAllowedHeader(HttpHeaders.CACHE_CONTROL);

		config.addAllowedOriginPattern("*");
		config.addExposedHeader("Location");
		config.addExposedHeader("Content-Location");
		config.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "HEAD"));
		config.setAllowCredentials(false);

		return new CorsInterceptor(config);
	}

	@Bean
	public RestfulServer restfulServer(
			IFhirSystemDao<?, ?> fhirSystemDao,
			AppProperties appProperties,
			DaoRegistry daoRegistry,
			IJpaSystemProvider jpaSystemProvider,
			ResourceProviderFactory resourceProviderFactory,
			JpaStorageSettings jpaStorageSettings,
			ISearchParamRegistry searchParamRegistry,
			IValidationSupport theValidationSupport,
			DatabaseBackedPagingProvider databaseBackedPagingProvider,
			LoggingInterceptor loggingInterceptor,
			TerminologyUploaderProvider terminologyUploaderProvider,
			Optional<CorsInterceptor> corsInterceptor,
			IInterceptorBroadcaster interceptorBroadcaster,
			IValidatorModule validatorModule,
			ValueSetOperationProvider theValueSetOperationProvider,
			Optional<RepositoryValidatingInterceptor> repositoryValidatingInterceptor,
			ThreadSafeResourceDeleterSvc theThreadSafeResourceDeleterSvc,
			DiffProvider diffProvider,
			TxResourceValidationSupport txResourceValidationSupport,
			APIKeyInterceptor theAPIKeyInterceptor) {
		RestfulServer fhirServer = new RestfulServer(fhirSystemDao.getContext());

		if (theValidationSupport instanceof ValidationSupportChain chain) {
			chain.addValidationSupport(txResourceValidationSupport);
		}

		daoRegistry.setSupportedResourceTypes(Arrays.asList("CodeSystem", "ValueSet", "ConceptMap", "SearchParameter"));

		if (appProperties.getNarrative_enabled()) {
			fhirSystemDao.getContext().setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
		} else {
			fhirSystemDao.getContext().setNarrativeGenerator(new NullNarrativeGenerator());
		}

		fhirServer.registerProviders(resourceProviderFactory.createProviders());
		fhirServer.registerProvider(jpaSystemProvider);
		fhirServer.setServerConformanceProvider(calculateConformanceProvider(
				fhirSystemDao, fhirServer, jpaStorageSettings, searchParamRegistry, theValidationSupport));

		/*
		 * ETag Support
		 */

		if (!appProperties.getEtag_support_enabled()) fhirServer.setETagSupport(ETagSupportEnum.DISABLED);

		/*
		 * Default to JSON and pretty printing
		 */
		fhirServer.setDefaultPrettyPrint(appProperties.getDefault_pretty_print());

		/*
		 * Default encoding
		 */
		fhirServer.setDefaultResponseEncoding(appProperties.getDefault_encoding());

		/*
		 * This configures the server to page search results to and from
		 * the database, instead of only paging them to memory. This may mean
		 * a performance hit when performing searches that return lots of results,
		 * but makes the server much more scalable.
		 */

		fhirServer.setPagingProvider(databaseBackedPagingProvider);

		/*
		 * This interceptor formats the output using nice colourful
		 * HTML output when the request is detected to come from a
		 * browser.
		 */
		fhirServer.registerInterceptor(new ResponseHighlighterInterceptor());

		if (appProperties.getFhirpath_interceptor_enabled()) {
			fhirServer.registerInterceptor(new FhirPathFilterInterceptor());
		}

		fhirServer.registerInterceptor(loggingInterceptor);
		fhirServer.registerInterceptor(new TerminologyCapabilityInterceptor());
  		fhirServer.registerInterceptor(theAPIKeyInterceptor);

		/*
		 * If you are hosting this server at a specific DNS name, the server will try to
		 * figure out the FHIR base URL based on what the web container tells it, but
		 * this doesn't always work. If you are setting links in your search bundles that
		 * just refer to "localhost", you might want to use a server address strategy:
		 */
		String serverAddress = appProperties.getServer_address();
		if (!Strings.isNullOrEmpty(serverAddress)) {
			fhirServer.setServerAddressStrategy(new HardcodedServerAddressStrategy(serverAddress));
		} else if (appProperties.getUse_apache_address_strategy()) {
			boolean useHttps = appProperties.getUse_apache_address_strategy_https();
			fhirServer.setServerAddressStrategy(
					useHttps ? ApacheProxyAddressStrategy.forHttps() : ApacheProxyAddressStrategy.forHttp());
		} else {
			fhirServer.setServerAddressStrategy(new IncomingRequestAddressStrategy());
		}

		fhirServer.registerProvider(terminologyUploaderProvider);

		corsInterceptor.ifPresent(fhirServer::registerInterceptor);

		if (appProperties.getAllow_cascading_deletes()) {
			CascadingDeleteInterceptor cascadingDeleteInterceptor = new CascadingDeleteInterceptor(
					fhirSystemDao.getContext(), daoRegistry, interceptorBroadcaster, theThreadSafeResourceDeleterSvc);
			fhirServer.registerInterceptor(cascadingDeleteInterceptor);
		}

		// Validation

		if (validatorModule != null) {
			if (appProperties.getValidation().getRequests_enabled()) {
				RequestValidatingInterceptor interceptor = new RequestValidatingInterceptor();
				interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
				interceptor.setValidatorModules(Collections.singletonList(validatorModule));
				fhirServer.registerInterceptor(interceptor);
			}
			if (appProperties.getValidation().getResponses_enabled()) {
				ResponseValidatingInterceptor interceptor = new ResponseValidatingInterceptor();
				interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
				interceptor.setValidatorModules(Collections.singletonList(validatorModule));
				fhirServer.registerInterceptor(interceptor);
			}
		}

		if (appProperties.getOpenapi_enabled()) {
			fhirServer.registerInterceptor(new OpenApiInterceptor());
		}

		// valueSet Operations i.e $expand
		fhirServer.registerProvider(theValueSetOperationProvider);

		// Validation
		repositoryValidatingInterceptor.ifPresent(fhirServer::registerInterceptor);

		// Diff Provider
		fhirServer.registerProvider(diffProvider);

		if (appProperties.getUserRequestRetryVersionConflictsInterceptorEnabled()) {
			fhirServer.registerInterceptor(new UserRequestRetryVersionConflictsInterceptor());
		}

		return fhirServer;
	}

	public static IServerConformanceProvider<?> calculateConformanceProvider(
			IFhirSystemDao fhirSystemDao,
			RestfulServer fhirServer,
			JpaStorageSettings jpaStorageSettings,
			ISearchParamRegistry searchParamRegistry,
			IValidationSupport theValidationSupport) {
		JpaCapabilityStatementProvider confProvider = new JpaCapabilityStatementProvider(
				fhirServer, fhirSystemDao, jpaStorageSettings, searchParamRegistry, theValidationSupport);
		confProvider.setImplementationDescription("HAPI FHIR R4 Server");
		return confProvider;
	}
}
