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
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.config.ThreadPoolFactoryConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.binary.interceptor.BinaryStorageInterceptor;
import ca.uhn.fhir.jpa.binary.provider.BinaryAccessProvider;
import ca.uhn.fhir.jpa.bulk.export.provider.BulkDataExportProvider;
import ca.uhn.fhir.jpa.delete.ThreadSafeResourceDeleterSvc;
import ca.uhn.fhir.jpa.graphql.GraphQLProvider;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor;
import ca.uhn.fhir.jpa.model.sched.ISchedulerService;
import ca.uhn.fhir.jpa.model.sched.ScheduledJobDefinition;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.partition.PartitionManagementProvider;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.provider.SubscriptionTriggeringProvider;
import ca.uhn.fhir.jpa.provider.TerminologyUploaderProvider;
import ca.uhn.fhir.jpa.provider.ValueSetOperationProvider;
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
import ca.uhn.fhir.validation.IValidatorModule;
import ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProvider;
import ch.ahdis.fhir.hapi.jpa.validation.ValidationProvider;
import ch.ahdis.matchbox.interceptor.ImplementationGuidePackageInterceptor;
import ch.ahdis.matchbox.interceptor.MappingLanguageInterceptor;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnairePopulateProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProvider;

@Configuration
@Import(ThreadPoolFactoryConfig.class)
public class MatchboxJpaConfig extends StarterJpaConfig {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MatchboxJpaConfig.class);

	@Autowired
	QuestionnairePopulateProvider questionnaireProvider;

	@Autowired
	QuestionnaireAssembleProvider assembleProvider;

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

	@Bean
	public RestfulServer restfulServer(IFhirSystemDao<?, ?> fhirSystemDao, AppProperties appProperties,
			DaoRegistry daoRegistry, Optional<MdmProviderLoader> mdmProviderProvider, IJpaSystemProvider jpaSystemProvider,
			ResourceProviderFactory resourceProviderFactory, DaoConfig daoConfig, ISearchParamRegistry searchParamRegistry,
			IValidationSupport theValidationSupport, DatabaseBackedPagingProvider databaseBackedPagingProvider,
			LoggingInterceptor loggingInterceptor, Optional<TerminologyUploaderProvider> terminologyUploaderProvider,
			Optional<SubscriptionTriggeringProvider> subscriptionTriggeringProvider,
			Optional<CorsInterceptor> corsInterceptor, IInterceptorBroadcaster interceptorBroadcaster,
			Optional<BinaryAccessProvider> binaryAccessProvider, BinaryStorageInterceptor binaryStorageInterceptor,
			IValidatorModule validatorModule, Optional<GraphQLProvider> graphQLProvider,
			BulkDataExportProvider bulkDataExportProvider, BulkDataImportProvider bulkDataImportProvider,
			ValueSetOperationProvider theValueSetOperationProvider, ReindexProvider reindexProvider,
			PartitionManagementProvider partitionManagementProvider,
			Optional<RepositoryValidatingInterceptor> repositoryValidatingInterceptor,
			IPackageInstallerSvc packageInstallerSvc, ThreadSafeResourceDeleterSvc theThreadSafeResourceDeleterSvc) {

		RestfulServer fhirServer = super.restfulServer(fhirSystemDao, appProperties, daoRegistry, mdmProviderProvider,
				jpaSystemProvider, resourceProviderFactory, daoConfig, searchParamRegistry, theValidationSupport,
				databaseBackedPagingProvider, loggingInterceptor, terminologyUploaderProvider, subscriptionTriggeringProvider,
				corsInterceptor, interceptorBroadcaster, binaryAccessProvider, binaryStorageInterceptor, validatorModule,
				graphQLProvider, bulkDataExportProvider, bulkDataImportProvider, theValueSetOperationProvider, reindexProvider,
				partitionManagementProvider, repositoryValidatingInterceptor, packageInstallerSvc, theThreadSafeResourceDeleterSvc);

		fhirServer.registerInterceptor(new MappingLanguageInterceptor());
		fhirServer.registerInterceptor(new ImplementationGuidePackageInterceptor(myPackageCacheManager, myFhirContext));
		fhirServer.registerProviders(validationProvider, questionnaireProvider, questionnaireResponseProvider,
				assembleProvider);

		if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages().booleanValue()
				&& appProperties.getImplementationGuides() != null) {
			((ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProvider) implementationGuideResourceProvider)
					.loadAll(true);
			if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages().booleanValue()) {
				System.exit(0);
			}
		}

		if (appProperties.getImplementationGuides() != null) {
			ScheduledJobDefinition jobDefinition = new ScheduledJobDefinition();
			jobDefinition.setId(this.getClass().getName());
			jobDefinition.setJobClass(ImplementationGuideProvider.class);
			mySvc.scheduleLocalJob(DateUtils.MILLIS_PER_MINUTE, jobDefinition);
		}

		return fhirServer;
	}

}
