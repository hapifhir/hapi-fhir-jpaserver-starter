package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.batch2.config.Batch2JobRegisterer;
import ca.uhn.fhir.batch2.jobs.export.BulkDataExportProvider;
import ca.uhn.fhir.batch2.jobs.imprt.BulkDataImportProvider;
import ca.uhn.fhir.batch2.jobs.reindex.ReindexProvider;
import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.interceptor.model.RequestPartitionId;
import ca.uhn.fhir.jpa.api.IDaoRegistry;
import ca.uhn.fhir.jpa.api.config.JpaStorageSettings;
import ca.uhn.fhir.jpa.api.config.ThreadPoolFactoryConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.binary.interceptor.BinaryStorageInterceptor;
import ca.uhn.fhir.jpa.binary.provider.BinaryAccessProvider;
import ca.uhn.fhir.jpa.config.util.HapiEntityManagerFactoryUtil;
import ca.uhn.fhir.jpa.config.util.ResourceCountCacheUtil;
import ca.uhn.fhir.jpa.dao.FulltextSearchSvcImpl;
import ca.uhn.fhir.jpa.dao.IFulltextSearchSvc;
import ca.uhn.fhir.jpa.dao.TransactionProcessor;
import ca.uhn.fhir.jpa.dao.search.HSearchSortHelperImpl;
import ca.uhn.fhir.jpa.dao.search.IHSearchSortHelper;
import ca.uhn.fhir.jpa.delete.ThreadSafeResourceDeleterSvc;
import ca.uhn.fhir.jpa.graphql.GraphQLProvider;
import ca.uhn.fhir.jpa.interceptor.CascadingDeleteInterceptor;
import ca.uhn.fhir.jpa.interceptor.UserRequestRetryVersionConflictsInterceptor;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor;
import ca.uhn.fhir.jpa.ips.provider.IpsOperationProvider;
import ca.uhn.fhir.jpa.model.config.SubscriptionSettings;
import ca.uhn.fhir.jpa.packages.AdditionalResourcesParser;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.provider.DaoRegistryResourceSupportedSvc;
import ca.uhn.fhir.jpa.provider.DiffProvider;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.provider.JpaCapabilityStatementProvider;
import ca.uhn.fhir.jpa.provider.JpaConformanceProviderDstu2;
import ca.uhn.fhir.jpa.provider.SubscriptionTriggeringProvider;
import ca.uhn.fhir.jpa.provider.TerminologyUploaderProvider;
import ca.uhn.fhir.jpa.provider.ValueSetOperationProvider;
import ca.uhn.fhir.jpa.provider.dstu3.JpaConformanceProviderDstu3;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.search.IStaleSearchDeletingSvc;
import ca.uhn.fhir.jpa.search.StaleSearchDeletingSvcImpl;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.annotations.OnCorsPresent;
import ca.uhn.fhir.jpa.starter.annotations.OnImplementationGuidesPresent;
import ca.uhn.fhir.jpa.starter.common.validation.IRepositoryValidationInterceptorFactory;
import ca.uhn.fhir.jpa.starter.ig.ExtendedPackageInstallationSpec;
import ca.uhn.fhir.jpa.starter.ig.IImplementationGuideOperationProvider;
import ca.uhn.fhir.jpa.subscription.util.SubscriptionDebugLogInterceptor;
import ca.uhn.fhir.jpa.util.ResourceCountCache;
import ca.uhn.fhir.mdm.provider.MdmProviderLoader;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative2.NullNarrativeGenerator;
import ca.uhn.fhir.rest.api.IResourceSupportedSvc;
import ca.uhn.fhir.rest.api.server.SystemRequestDetails;
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
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
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
// allow users to configure custom packages to scan for additional beans
@ComponentScan(basePackages = {"${hapi.fhir.custom-bean-packages:}"})
@Import(ThreadPoolFactoryConfig.class)
public class StarterJpaConfig {

	private static final Logger ourLog = LoggerFactory.getLogger(StarterJpaConfig.class);

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

	@Bean("packageInstaller")
	@Primary
	@Conditional(OnImplementationGuidesPresent.class)
	public IPackageInstallerSvc packageInstaller(
			AppProperties appProperties,
			IPackageInstallerSvc packageInstallerSvc,
			Batch2JobRegisterer batch2JobRegisterer,
			FhirContext fhirContext,
			TransactionProcessor transactionProcessor,
			IHapiPackageCacheManager iHapiPackageCacheManager) {

		batch2JobRegisterer.start();

		if (appProperties.getImplementationGuides() != null) {
			Map<String, ExtendedPackageInstallationSpec> guides = appProperties.getImplementationGuides();
			for (Map.Entry<String, ExtendedPackageInstallationSpec> guidesEntry : guides.entrySet()) {
				ExtendedPackageInstallationSpec packageInstallationSpec = guidesEntry.getValue();
				if (appProperties.getInstall_transitive_ig_dependencies()) {

					packageInstallationSpec
							.addDependencyExclude("hl7.fhir.r2.core")
							.addDependencyExclude("hl7.fhir.r3.core")
							.addDependencyExclude("hl7.fhir.r4.core")
							.addDependencyExclude("hl7.fhir.r5.core");
				}

				packageInstallerSvc.install(packageInstallationSpec);

				Set<String> extraResources = packageInstallationSpec.getAdditionalResourceFolders();
				packageInstallationSpec.setPackageContents(iHapiPackageCacheManager
						.loadPackageContents(packageInstallationSpec.getName(), packageInstallationSpec.getVersion())
						.getBytes());

				if (extraResources != null && !extraResources.isEmpty()) {
					IBaseBundle transaction = AdditionalResourcesParser.bundleAdditionalResources(
							extraResources, packageInstallationSpec, fhirContext);
					transactionProcessor.transaction(
							new SystemRequestDetails().setRequestPartitionId(RequestPartitionId.defaultPartition()),
							transaction,
							false);
				}
			}
		}
		return packageInstallerSvc;
	}

	@Bean
	@Conditional(OnCorsPresent.class)
	public CorsInterceptor corsInterceptor(AppProperties appProperties) {
		// Define your CORS configuration. This is an example
		// showing a typical setup. You should customize this
		// to your specific needs
		ourLog.info("CORS is enabled on this server");
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedHeader(HttpHeaders.ORIGIN);
		config.addAllowedHeader(HttpHeaders.ACCEPT);
		config.addAllowedHeader(HttpHeaders.CONTENT_TYPE);
		config.addAllowedHeader(HttpHeaders.AUTHORIZATION);
		config.addAllowedHeader(HttpHeaders.CACHE_CONTROL);
		config.addAllowedHeader("x-fhir-starter");
		config.addAllowedHeader("X-Requested-With");
		config.addAllowedHeader("Prefer");

		List<String> allAllowedCORSOrigins = appProperties.getCors().getAllowed_origin();
		allAllowedCORSOrigins.forEach(config::addAllowedOriginPattern);
		ourLog.info("CORS allows the following origins: {}", String.join(", ", allAllowedCORSOrigins));

		config.addExposedHeader("Location");
		config.addExposedHeader("Content-Location");
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
		config.setAllowCredentials(appProperties.getCors().getAllow_Credentials());

		// Create the interceptor and register it
		return new CorsInterceptor(config);
	}

	@Bean
	public RestfulServer restfulServer(
			IFhirSystemDao<?, ?> fhirSystemDao,
			AppProperties appProperties,
			DaoRegistry daoRegistry,
			Optional<MdmProviderLoader> mdmProviderProvider,
			IJpaSystemProvider jpaSystemProvider,
			ResourceProviderFactory resourceProviderFactory,
			JpaStorageSettings jpaStorageSettings,
			SubscriptionSettings subscriptionSettings,
			ISearchParamRegistry searchParamRegistry,
			IValidationSupport theValidationSupport,
			DatabaseBackedPagingProvider databaseBackedPagingProvider,
			LoggingInterceptor loggingInterceptor,
			Optional<TerminologyUploaderProvider> terminologyUploaderProvider,
			Optional<SubscriptionTriggeringProvider> subscriptionTriggeringProvider,
			Optional<CorsInterceptor> corsInterceptor,
			IInterceptorBroadcaster interceptorBroadcaster,
			Optional<BinaryAccessProvider> binaryAccessProvider,
			BinaryStorageInterceptor binaryStorageInterceptor,
			IValidatorModule validatorModule,
			Optional<GraphQLProvider> graphQLProvider,
			BulkDataExportProvider bulkDataExportProvider,
			BulkDataImportProvider bulkDataImportProvider,
			ValueSetOperationProvider theValueSetOperationProvider,
			ReindexProvider reindexProvider,
			Optional<RepositoryValidatingInterceptor> repositoryValidatingInterceptor,
			IPackageInstallerSvc packageInstallerSvc,
			ThreadSafeResourceDeleterSvc theThreadSafeResourceDeleterSvc,
			ApplicationContext appContext,
			Optional<IpsOperationProvider> theIpsOperationProvider,
			Optional<IImplementationGuideOperationProvider> implementationGuideOperationProvider,
			DiffProvider diffProvider) {
		RestfulServer fhirServer = new RestfulServer(fhirSystemDao.getContext());

		List<String> supportedResourceTypes = appProperties.getSupported_resource_types();

		if (!supportedResourceTypes.isEmpty()) {
			if (!supportedResourceTypes.contains("SearchParameter")) {
				supportedResourceTypes.add("SearchParameter");
			}
			daoRegistry.setSupportedResourceTypes(supportedResourceTypes);
		}

		if (appProperties.getNarrative_enabled()) {
			fhirSystemDao.getContext().setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
		} else {
			fhirSystemDao.getContext().setNarrativeGenerator(new NullNarrativeGenerator());
		}

		if (appProperties.getMdm_enabled()) mdmProviderProvider.get().loadProvider();

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

		implementationGuideOperationProvider.ifPresent(fhirServer::registerProvider);

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

		/*
		 * If you are using DSTU3+, you may want to add a terminology uploader, which allows
		 * uploading of external terminologies such as Snomed CT. Note that this uploader
		 * does not have any security attached (any anonymous user may use it by default)
		 * so it is a potential security vulnerability. Consider using an AuthorizationInterceptor
		 * with this feature.
		 */
		if (fhirSystemDao
				.getContext()
				.getVersion()
				.getVersion()
				.isEqualOrNewerThan(FhirVersionEnum.DSTU3)) { // <-- ENABLED RIGHT NOW
			fhirServer.registerProvider(terminologyUploaderProvider.get());
		}

		// If you want to enable the $trigger-subscription operation to allow
		// manual triggering of a subscription delivery, enable this provider
		if (true) { // <-- ENABLED RIGHT NOW
			fhirServer.registerProvider(subscriptionTriggeringProvider.get());
		}

		corsInterceptor.ifPresent(fhirServer::registerInterceptor);

		if (!subscriptionSettings.getSupportedSubscriptionTypes().isEmpty()) {
			// Subscription debug logging
			fhirServer.registerInterceptor(new SubscriptionDebugLogInterceptor());
		}

		if (appProperties.getAllow_cascading_deletes()) {
			CascadingDeleteInterceptor cascadingDeleteInterceptor = new CascadingDeleteInterceptor(
					fhirSystemDao.getContext(), daoRegistry, interceptorBroadcaster, theThreadSafeResourceDeleterSvc);
			fhirServer.registerInterceptor(cascadingDeleteInterceptor);
		}

		// Binary Storage
		if (appProperties.getBinary_storage_enabled() && binaryAccessProvider.isPresent()) {
			fhirServer.registerProvider(binaryAccessProvider.get());
			fhirServer.registerInterceptor(binaryStorageInterceptor);
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

		// GraphQL
		if (appProperties.getGraphql_enabled()) {
			if (fhirSystemDao.getContext().getVersion().getVersion().isEqualOrNewerThan(FhirVersionEnum.DSTU3)) {
				fhirServer.registerProvider(graphQLProvider.get());
			}
		}

		if (appProperties.getOpenapi_enabled()) {
			fhirServer.registerInterceptor(new OpenApiInterceptor());
		}

		// Bulk Export
		if (appProperties.getBulk_export_enabled()) {
			fhirServer.registerProvider(bulkDataExportProvider);
		}

		// Bulk Import
		if (appProperties.getBulk_import_enabled()) {
			fhirServer.registerProvider(bulkDataImportProvider);
		}

		// valueSet Operations i.e $expand
		fhirServer.registerProvider(theValueSetOperationProvider);

		// reindex Provider $reindex
		fhirServer.registerProvider(reindexProvider);

		// Validation
		repositoryValidatingInterceptor.ifPresent(fhirServer::registerInterceptor);

		// Diff Provider
		fhirServer.registerProvider(diffProvider);

		// register custom interceptors
		registerCustomInterceptors(fhirServer, appContext, appProperties.getCustomInterceptorClasses());

		// register the IPS Provider
		theIpsOperationProvider.ifPresent(fhirServer::registerProvider);

		if (appProperties.getUserRequestRetryVersionConflictsInterceptorEnabled()) {
			fhirServer.registerInterceptor(new UserRequestRetryVersionConflictsInterceptor());
		}

		// register custom providers
		registerCustomProviders(fhirServer, appContext, appProperties.getCustomProviderClasses());

		return fhirServer;
	}

	/**
	 * check the properties for custom interceptor classes and registers them.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void registerCustomInterceptors(
			RestfulServer fhirServer, ApplicationContext theAppContext, List<String> customInterceptorClasses) {

		if (customInterceptorClasses == null) {
			return;
		}

		for (String className : customInterceptorClasses) {
			Class clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new ConfigurationException("Interceptor class was not found on classpath: " + className, e);
			}

			// first check if the class is a Bean in the app context
			Object interceptor = null;
			try {
				interceptor = theAppContext.getBean(clazz);
				ourLog.info("registering custom interceptor as bean: {}", className);
			} catch (NoSuchBeanDefinitionException ex) {
				// no op - if it's not a bean we'll try to create it
			}

			// if not a bean, instantiate the interceptor via reflection
			if (interceptor == null) {
				try {
					interceptor = clazz.getConstructor().newInstance();
					ourLog.info("registering custom interceptor as pojo: {}", className);
				} catch (Exception e) {
					throw new ConfigurationException("Unable to instantiate interceptor class : " + className, e);
				}
			}
			fhirServer.registerInterceptor(interceptor);
		}
	}

	/**
	 * check the properties for custom provider classes and registers them.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void registerCustomProviders(
			RestfulServer fhirServer, ApplicationContext theAppContext, List<String> customProviderClasses) {

		if (customProviderClasses == null) {
			return;
		}

		for (String className : customProviderClasses) {
			Class clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new ConfigurationException("Provider class was not found on classpath: " + className, e);
			}

			// first check if the class is a Bean in the app context
			Object provider = null;
			try {
				provider = theAppContext.getBean(clazz);
				ourLog.info("registering custom provider as bean: {}", className);
			} catch (NoSuchBeanDefinitionException ex) {
				// no op - if it's not a bean we'll try to create it
			}

			// if not a bean, instantiate the interceptor via reflection
			if (provider == null) {
				try {
					provider = clazz.getConstructor().newInstance();
					ourLog.info("registering custom provider as pojo: {}", className);
				} catch (Exception e) {
					throw new ConfigurationException("Unable to instantiate provider class : " + className, e);
				}
			}
			fhirServer.registerProvider(provider);
		}
	}

	public static IServerConformanceProvider<?> calculateConformanceProvider(
			IFhirSystemDao fhirSystemDao,
			RestfulServer fhirServer,
			JpaStorageSettings jpaStorageSettings,
			ISearchParamRegistry searchParamRegistry,
			IValidationSupport theValidationSupport) {
		FhirVersionEnum fhirVersion = fhirSystemDao.getContext().getVersion().getVersion();
		if (fhirVersion == FhirVersionEnum.DSTU2) {
			JpaConformanceProviderDstu2 confProvider =
					new JpaConformanceProviderDstu2(fhirServer, fhirSystemDao, jpaStorageSettings);
			confProvider.setImplementationDescription("HAPI FHIR DSTU2 Server");
			return confProvider;
		} else if (fhirVersion == FhirVersionEnum.DSTU3) {

			JpaConformanceProviderDstu3 confProvider =
					new JpaConformanceProviderDstu3(fhirServer, fhirSystemDao, jpaStorageSettings, searchParamRegistry);
			confProvider.setImplementationDescription("HAPI FHIR DSTU3 Server");
			return confProvider;
		} else if (fhirVersion == FhirVersionEnum.R4) {

			JpaCapabilityStatementProvider confProvider = new JpaCapabilityStatementProvider(
					fhirServer, fhirSystemDao, jpaStorageSettings, searchParamRegistry, theValidationSupport);
			confProvider.setImplementationDescription("HAPI FHIR R4 Server");
			return confProvider;
		} else if (fhirVersion == FhirVersionEnum.R4B) {

			JpaCapabilityStatementProvider confProvider = new JpaCapabilityStatementProvider(
					fhirServer, fhirSystemDao, jpaStorageSettings, searchParamRegistry, theValidationSupport);
			confProvider.setImplementationDescription("HAPI FHIR R4B Server");
			return confProvider;
		} else if (fhirVersion == FhirVersionEnum.R5) {

			JpaCapabilityStatementProvider confProvider = new JpaCapabilityStatementProvider(
					fhirServer, fhirSystemDao, jpaStorageSettings, searchParamRegistry, theValidationSupport);
			confProvider.setImplementationDescription("HAPI FHIR R5 Server");
			return confProvider;
		} else {
			throw new IllegalStateException();
		}
	}
}
