package ch.ahdis.matchbox;

import java.util.Optional;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.batch2.jobs.imprt.BulkDataImportProvider;
import ca.uhn.fhir.batch2.jobs.reindex.ReindexProvider;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.config.ThreadPoolFactoryConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.binary.interceptor.BinaryStorageInterceptor;
import ca.uhn.fhir.jpa.binary.provider.BinaryAccessProvider;
import ca.uhn.fhir.jpa.bulk.export.provider.BulkDataExportProvider;
import ca.uhn.fhir.jpa.delete.ThreadSafeResourceDeleterSvc;
import ca.uhn.fhir.jpa.model.sched.ISchedulerService;
import ca.uhn.fhir.jpa.model.sched.ScheduledJobDefinition;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.partition.PartitionManagementProvider;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.provider.SubscriptionTriggeringProvider;
import ca.uhn.fhir.jpa.provider.TerminologyUploaderProvider;
import ca.uhn.fhir.jpa.rp.r4.ImplementationGuideResourceProvider;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.common.StarterJpaConfig;
import ca.uhn.fhir.mdm.provider.MdmProviderLoader;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProvider;
import ch.ahdis.fhir.hapi.jpa.validation.ValidationProvider;
import ch.ahdis.matchbox.interceptor.ImplementationGuidePackageInterceptor;
import ch.ahdis.matchbox.interceptor.MappingLanguageInterceptor;
import ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProvider;

@Configuration
@Import(ThreadPoolFactoryConfig.class)
public class MatchboxJpaConfig extends StarterJpaConfig {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MatchboxJpaConfig.class);

	@Autowired
	QuestionnaireAssembleProvider assembleProvider;

	@Autowired
	QuestionnaireResourceProvider questionnaireProvider;

	@Autowired
	QuestionnaireResponseExtractProvider questionnaireResponseProvider;

	@Autowired
	private IHapiPackageCacheManager myPackageCacheManager;

	@Autowired
	protected FhirContext myFhirContext;

	@Autowired
	private ISchedulerService mySvc;

	@Autowired
	private ImplementationGuideResourceProvider implementationGuideResourceProvider;

	@Autowired
	private ValidationProvider validationProvider;
	
	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;
	
	@Autowired
	protected ConceptMapResourceProvider conceptMapProvider;

	@Autowired
	protected CodeSystemResourceProvider codeSystemProvider;

	@Autowired
	protected ValueSetResourceProvider valueSetProvider;

	@Autowired
	protected StructureDefinitionResourceProvider structureDefinitionProvider;

	@Autowired
	protected StructureMapTransformProvider structureMapTransformProvider;

	// removed GraphQlProvider
	// removed IVAldiationSupport
	
	@Bean
	public RestfulServer restfulServer(IFhirSystemDao<?, ?> fhirSystemDao, AppProperties appProperties,
			DaoRegistry daoRegistry, Optional<MdmProviderLoader> mdmProviderProvider, IJpaSystemProvider jpaSystemProvider,
			ResourceProviderFactory resourceProviderFactory, DaoConfig daoConfig, ISearchParamRegistry searchParamRegistry,
			DatabaseBackedPagingProvider databaseBackedPagingProvider,
			LoggingInterceptor loggingInterceptor, Optional<TerminologyUploaderProvider> terminologyUploaderProvider,
			Optional<SubscriptionTriggeringProvider> subscriptionTriggeringProvider,
			Optional<CorsInterceptor> corsInterceptor, IInterceptorBroadcaster interceptorBroadcaster,
			Optional<BinaryAccessProvider> binaryAccessProvider, BinaryStorageInterceptor binaryStorageInterceptor,
			BulkDataExportProvider bulkDataExportProvider, BulkDataImportProvider bulkDataImportProvider,
			ReindexProvider reindexProvider,
			PartitionManagementProvider partitionManagementProvider,
			IPackageInstallerSvc packageInstallerSvc, ThreadSafeResourceDeleterSvc theThreadSafeResourceDeleterSvc) {

		RestfulServer fhirServer = super.restfulServer(fhirSystemDao, appProperties, daoRegistry, mdmProviderProvider,
				jpaSystemProvider, resourceProviderFactory, daoConfig, searchParamRegistry,
				databaseBackedPagingProvider, loggingInterceptor, terminologyUploaderProvider, subscriptionTriggeringProvider,
				corsInterceptor, interceptorBroadcaster, binaryAccessProvider, binaryStorageInterceptor, null,
				null, bulkDataExportProvider, bulkDataImportProvider, null, reindexProvider,
				partitionManagementProvider,  null, packageInstallerSvc, theThreadSafeResourceDeleterSvc);

		fhirServer.registerInterceptor(new MappingLanguageInterceptor(matchboxEngineSupport));
		fhirServer.registerInterceptor(new ImplementationGuidePackageInterceptor(myPackageCacheManager, myFhirContext));
		fhirServer.registerInterceptor(new MatchboxValidationInterceptor(this.myFhirContext,structureDefinitionProvider));
		fhirServer.registerProviders(implementationGuideResourceProvider, validationProvider, questionnaireProvider, questionnaireResponseProvider,
				assembleProvider, conceptMapProvider, codeSystemProvider, valueSetProvider, structureDefinitionProvider, structureMapTransformProvider);
		
		if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages().booleanValue()
				&& appProperties.getImplementationGuides() != null) {
			((ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProvider) implementationGuideResourceProvider)
					.loadAll(true);
			if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages().booleanValue()) {
				System.exit(0);
			}
		}
		
		fhirServer.setServerConformanceProvider(new MatchboxCapabilityStatementProvider(fhirServer, structureDefinitionProvider));
		
		ScheduledJobDefinition jobDefinition = new ScheduledJobDefinition();
		jobDefinition.setId(this.getClass().getName());
		jobDefinition.setJobClass(ImplementationGuideProvider.class);
		mySvc.scheduleLocalJob(DateUtils.MILLIS_PER_MINUTE, jobDefinition);

		return fhirServer;
	}
	
	@Bean
	public MatchboxEngineSupport getMachboxEngineSupport() {
		return new MatchboxEngineSupport();
	}


}
