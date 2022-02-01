package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.cql.common.provider.CqlProviderLoader;
import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.binstore.BinaryStorageInterceptor;
import ca.uhn.fhir.jpa.bulk.export.provider.BulkDataExportProvider;
import ca.uhn.fhir.jpa.graphql.GraphQLProvider;
import ca.uhn.fhir.jpa.interceptor.CascadingDeleteInterceptor;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.partition.PartitionManagementProvider;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.provider.JpaCapabilityStatementProvider;
import ca.uhn.fhir.jpa.provider.JpaConformanceProviderDstu2;
import ca.uhn.fhir.jpa.provider.SubscriptionTriggeringProvider;
import ca.uhn.fhir.jpa.provider.TerminologyUploaderProvider;
import ca.uhn.fhir.jpa.provider.dstu3.JpaConformanceProviderDstu3;
import ca.uhn.fhir.jpa.provider.ValueSetOperationProvider;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.subscription.util.SubscriptionDebugLogInterceptor;
import ca.uhn.fhir.mdm.provider.MdmProviderLoader;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.narrative2.NullNarrativeGenerator;
import ca.uhn.fhir.rest.openapi.OpenApiInterceptor;
import ca.uhn.fhir.rest.server.ApacheProxyAddressStrategy;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.FhirPathFilterInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.partition.RequestTenantPartitionInterceptor;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import ca.uhn.fhir.rest.server.tenant.UrlBaseTenantIdentificationStrategy;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;

public class BaseJpaRestfulServer extends RestfulServer {
  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BaseJpaRestfulServer.class);

	private static final long serialVersionUID = 1L;
  @Autowired
  DaoRegistry daoRegistry;
  @Autowired
  DaoConfig daoConfig;
  @Autowired
  ISearchParamRegistry searchParamRegistry;
  @Autowired
  IFhirSystemDao fhirSystemDao;
  @Autowired
  ResourceProviderFactory resourceProviderFactory;
  @Autowired
  IJpaSystemProvider jpaSystemProvider;
  @Autowired
  IInterceptorBroadcaster interceptorBroadcaster;
  @Autowired
  DatabaseBackedPagingProvider databaseBackedPagingProvider;
  @Autowired
  IInterceptorService interceptorService;
  @Autowired
  IValidatorModule validatorModule;
  @Autowired
  Optional<GraphQLProvider> graphQLProvider;
  @Autowired
  BulkDataExportProvider bulkDataExportProvider;
  @Autowired
  PartitionManagementProvider partitionManagementProvider;
  @Autowired
  ValueSetOperationProvider valueSetOperationProvider;
  @Autowired
  BinaryStorageInterceptor binaryStorageInterceptor;
  @Autowired
  IPackageInstallerSvc packageInstallerSvc;
  @Autowired
  AppProperties appProperties;
  @Autowired
  ApplicationContext myApplicationContext;
  @Autowired(required = false)
  IRepositoryValidationInterceptorFactory factory;
  // These are set only if the features are enabled
  @Autowired
  Optional<CqlProviderLoader> cqlProviderLoader;
  @Autowired
  Optional<MdmProviderLoader> mdmProviderProvider;

  @Autowired
  private IValidationSupport myValidationSupport;

  public BaseJpaRestfulServer() {
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    /*
     * Create a FhirContext object that uses the version of FHIR
     * specified in the properties file.
     */
    // Customize supported resource types
    List<String> supportedResourceTypes = appProperties.getSupported_resource_types();

    if (!supportedResourceTypes.isEmpty() && !supportedResourceTypes.contains("SearchParameter")) {
      supportedResourceTypes.add("SearchParameter");
      daoRegistry.setSupportedResourceTypes(supportedResourceTypes);
    }

    setFhirContext(fhirSystemDao.getContext());

    /*
     * Order matters - the MDM provider registers itself on the resourceProviderFactory - hence the loading must be done
     * ahead of provider registration
     */
    if(appProperties.getMdm_enabled())
    	mdmProviderProvider.get().loadProvider();

    registerProviders(resourceProviderFactory.createProviders());
    registerProvider(jpaSystemProvider);

    /*
     * The conformance provider exports the supported resources, search parameters, etc for
     * this server. The JPA version adds resourceProviders counts to the exported statement, so it
     * is a nice addition.
     *
     * You can also create your own subclass of the conformance provider if you need to
     * provide further customization of your server's CapabilityStatement
     */

    FhirVersionEnum fhirVersion = fhirSystemDao.getContext().getVersion().getVersion();
    if (fhirVersion == FhirVersionEnum.DSTU2) {

      JpaConformanceProviderDstu2 confProvider = new JpaConformanceProviderDstu2(this, fhirSystemDao,
        daoConfig);
      confProvider.setImplementationDescription("HAPI FHIR DSTU2 Server");
      setServerConformanceProvider(confProvider);
    } else {
      if (fhirVersion == FhirVersionEnum.DSTU3) {

        JpaConformanceProviderDstu3 confProvider = new JpaConformanceProviderDstu3(this, fhirSystemDao,
          daoConfig, searchParamRegistry);
        confProvider.setImplementationDescription("HAPI FHIR DSTU3 Server");
        setServerConformanceProvider(confProvider);
      } else if (fhirVersion == FhirVersionEnum.R4) {

				JpaCapabilityStatementProvider confProvider = new JpaCapabilityStatementProvider(this, fhirSystemDao,
					daoConfig, searchParamRegistry, myValidationSupport);
        confProvider.setImplementationDescription("HAPI FHIR R4 Server");
        setServerConformanceProvider(confProvider);
      } else if (fhirVersion == FhirVersionEnum.R5) {

				JpaCapabilityStatementProvider confProvider = new JpaCapabilityStatementProvider(this, fhirSystemDao,
					daoConfig, searchParamRegistry, myValidationSupport);
        confProvider.setImplementationDescription("HAPI FHIR R5 Server");
        setServerConformanceProvider(confProvider);
      } else {
        throw new IllegalStateException();
      }
    }

    /*
     * ETag Support
     */

    if (appProperties.getEtag_support_enabled() == false)
      setETagSupport(ETagSupportEnum.DISABLED);


    /*
     * This server tries to dynamically generate narratives
     */
    FhirContext ctx = getFhirContext();
    INarrativeGenerator theNarrativeGenerator =
      appProperties.getNarrative_enabled() ?
      new DefaultThymeleafNarrativeGenerator() :
      new NullNarrativeGenerator();
    ctx.setNarrativeGenerator(theNarrativeGenerator);

    /*
     * Default to JSON and pretty printing
     */
    setDefaultPrettyPrint(appProperties.getDefault_pretty_print());

    /*
     * Default encoding
     */
    setDefaultResponseEncoding(appProperties.getDefault_encoding());

    /*
     * This configures the server to page search results to and from
     * the database, instead of only paging them to memory. This may mean
     * a performance hit when performing searches that return lots of results,
     * but makes the server much more scalable.
     */

    setPagingProvider(databaseBackedPagingProvider);

    /*
     * This interceptor formats the output using nice colourful
     * HTML output when the request is detected to come from a
     * browser.
     */
    ResponseHighlighterInterceptor responseHighlighterInterceptor = new ResponseHighlighterInterceptor();
    this.registerInterceptor(responseHighlighterInterceptor);

    if (appProperties.getFhirpath_interceptor_enabled()) {
      registerInterceptor(new FhirPathFilterInterceptor());
    }

    /*
     * Add some logging for each request
     */
    LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
    loggingInterceptor.setLoggerName(appProperties.getLogger().getName());
    loggingInterceptor.setMessageFormat(appProperties.getLogger().getFormat());
    loggingInterceptor.setErrorMessageFormat(appProperties.getLogger().getError_format());
    loggingInterceptor.setLogExceptions(appProperties.getLogger().getLog_exceptions());
    this.registerInterceptor(loggingInterceptor);

    /*
     * If you are hosting this server at a specific DNS name, the server will try to
     * figure out the FHIR base URL based on what the web container tells it, but
     * this doesn't always work. If you are setting links in your search bundles that
     * just refer to "localhost", you might want to use a server address strategy:
     */
    String serverAddress = appProperties.getServer_address();
    if (!Strings.isNullOrEmpty(serverAddress)) {
      setServerAddressStrategy(new HardcodedServerAddressStrategy(serverAddress));
    } else if (appProperties.getUse_apache_address_strategy()) {
      boolean useHttps = appProperties.getUse_apache_address_strategy_https();
      setServerAddressStrategy(useHttps ? ApacheProxyAddressStrategy.forHttps() :
                    ApacheProxyAddressStrategy.forHttp());
    } else {
      setServerAddressStrategy(new IncomingRequestAddressStrategy());
    }

    /*
     * If you are using DSTU3+, you may want to add a terminology uploader, which allows
     * uploading of external terminologies such as Snomed CT. Note that this uploader
     * does not have any security attached (any anonymous user may use it by default)
     * so it is a potential security vulnerability. Consider using an AuthorizationInterceptor
     * with this feature.
     */
    if (ctx.getVersion().getVersion().isEqualOrNewerThan(FhirVersionEnum.DSTU3)) { // <-- ENABLED RIGHT NOW
      registerProvider(myApplicationContext.getBean(TerminologyUploaderProvider.class));
    }

    // If you want to enable the $trigger-subscription operation to allow
    // manual triggering of a subscription delivery, enable this provider
    if (true) { // <-- ENABLED RIGHT NOW
      registerProvider(myApplicationContext.getBean(SubscriptionTriggeringProvider.class));
    }

    // Define your CORS configuration. This is an example
    // showing a typical setup. You should customize this
    // to your specific needs
    if (appProperties.getCors() != null) {
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
      ourLog.info("CORS allows the following origins: " + String.join(", ", allAllowedCORSOrigins));

      config.addExposedHeader("Location");
      config.addExposedHeader("Content-Location");
      config.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
      config.setAllowCredentials(appProperties.getCors().getAllow_Credentials());

      // Create the interceptor and register it
      CorsInterceptor interceptor = new CorsInterceptor(config);
      registerInterceptor(interceptor);
    } else {
    	ourLog.info("CORS is disabled on this server");
	 }

    // If subscriptions are enabled, we want to register the interceptor that
    // will activate them and match results against them
    if (appProperties.getSubscription() != null) {
      // Subscription debug logging
      interceptorService.registerInterceptor(new SubscriptionDebugLogInterceptor());
    }

    // Cascading deletes


    if (appProperties.getAllow_cascading_deletes()) {
      CascadingDeleteInterceptor cascadingDeleteInterceptor = new CascadingDeleteInterceptor(ctx,
        daoRegistry, interceptorBroadcaster);
      getInterceptorService().registerInterceptor(cascadingDeleteInterceptor);
    }

    // Binary Storage
    if (appProperties.getBinary_storage_enabled()) {
      getInterceptorService().registerInterceptor(binaryStorageInterceptor);
    }

    // Validation

    if (validatorModule != null) {
      if (appProperties.getValidation().getRequests_enabled()) {
        RequestValidatingInterceptor interceptor = new RequestValidatingInterceptor();
        interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
        interceptor.setValidatorModules(Collections.singletonList(validatorModule));
        registerInterceptor(interceptor);
      }
      if (appProperties.getValidation().getResponses_enabled()) {
        ResponseValidatingInterceptor interceptor = new ResponseValidatingInterceptor();
        interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
        interceptor.setValidatorModules(Collections.singletonList(validatorModule));
        registerInterceptor(interceptor);
      }
    }

    // GraphQL
    if (appProperties.getGraphql_enabled()) {
      if (fhirVersion.isEqualOrNewerThan(FhirVersionEnum.DSTU3)) {
        registerProvider(graphQLProvider.get());
      }
    }

    if (appProperties.getAllowed_bundle_types() != null) {
      daoConfig.setBundleTypesAllowedForStorage(appProperties.getAllowed_bundle_types().stream().map(BundleType::toCode).collect(Collectors.toSet()));
    }

    daoConfig.setDeferIndexingForCodesystemsOfSize(appProperties.getDefer_indexing_for_codesystems_of_size());

	 if (appProperties.getOpenapi_enabled()) {
		registerInterceptor(new OpenApiInterceptor());
	}

    // Bulk Export
    if (appProperties.getBulk_export_enabled()) {
      registerProvider(bulkDataExportProvider);
    }

    // valueSet Operations i.e $expand
    registerProvider(valueSetOperationProvider);

    // Partitioning
    if (appProperties.getPartitioning() != null) {
      registerInterceptor(new RequestTenantPartitionInterceptor());
      setTenantIdentificationStrategy(new UrlBaseTenantIdentificationStrategy());
      registerProviders(partitionManagementProvider);
    }

    if (appProperties.getClient_id_strategy() == DaoConfig.ClientIdStrategyEnum.ANY) {
      daoConfig.setResourceServerIdStrategy(DaoConfig.IdStrategyEnum.UUID);
      daoConfig.setResourceClientIdStrategy(appProperties.getClient_id_strategy());
    }
    //Parallel Batch GET execution settings
  	 daoConfig.setBundleBatchPoolSize(appProperties.getBundle_batch_pool_size());
  	 daoConfig.setBundleBatchPoolSize(appProperties.getBundle_batch_pool_max_size());

    if (appProperties.getImplementationGuides() != null) {
      Map<String, AppProperties.ImplementationGuide> guides = appProperties.getImplementationGuides();
      for (Map.Entry<String, AppProperties.ImplementationGuide> guide : guides.entrySet()) {
			PackageInstallationSpec packageInstallationSpec = new PackageInstallationSpec()
				.setPackageUrl(guide.getValue().getUrl())
				.setName(guide.getValue().getName())
				.setVersion(guide.getValue().getVersion())
				.setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL);
			if(appProperties.getInstall_transitive_ig_dependencies()) {
				packageInstallationSpec.setFetchDependencies(true);
				packageInstallationSpec.setDependencyExcludes(ImmutableList.of("hl7.fhir.r2.core", "hl7.fhir.r3.core", "hl7.fhir.r4.core", "hl7.fhir.r5.core"));
			}
			packageInstallerSvc.install(packageInstallationSpec);
      }
    }

    if(factory != null) {
		 interceptorService.registerInterceptor(factory.buildUsingStoredStructureDefinitions());
	 }


    if (appProperties.getLastn_enabled()) {
      daoConfig.setLastNEnabled(true);
    }

	 daoConfig.setStoreResourceInLuceneIndex(appProperties.getStore_resource_in_lucene_index_enabled());
    daoConfig.getModelConfig().setNormalizedQuantitySearchLevel(appProperties.getNormalized_quantity_search_level());
	 daoConfig.getModelConfig().setIndexOnContainedResources(appProperties.getEnable_index_contained_resource());
  }
}
