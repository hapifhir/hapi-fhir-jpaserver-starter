package ch.ahdis.matchbox.config;

import ca.uhn.fhir.batch2.api.IJobCoordinator;
import ca.uhn.fhir.batch2.api.IJobPartitionProvider;
import ca.uhn.fhir.batch2.api.IJobPersistence;
import ca.uhn.fhir.batch2.api.JobOperationResultJson;
import ca.uhn.fhir.batch2.coordinator.DefaultJobPartitionProvider;
import ca.uhn.fhir.batch2.jobs.parameters.UrlPartitioner;
import ca.uhn.fhir.batch2.model.*;
import ca.uhn.fhir.batch2.models.JobInstanceFetchRequest;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.jpa.api.config.JpaStorageSettings;
import ca.uhn.fhir.jpa.api.config.ThreadPoolFactoryConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.batch.models.Batch2JobStartResponse;
import ca.uhn.fhir.jpa.batch2.JpaJobPersistenceImpl;
import ca.uhn.fhir.jpa.binary.interceptor.BinaryStorageInterceptor;
import ca.uhn.fhir.jpa.binary.provider.BinaryAccessProvider;
import ca.uhn.fhir.jpa.bulk.export.api.IBulkExportProcessor;
import ca.uhn.fhir.jpa.bulk.export.model.ExportPIDIteratorParameters;
import ca.uhn.fhir.jpa.dao.data.IBatch2JobInstanceRepository;
import ca.uhn.fhir.jpa.dao.data.IBatch2WorkChunkMetadataViewRepository;
import ca.uhn.fhir.jpa.dao.data.IBatch2WorkChunkRepository;
import ca.uhn.fhir.mdm.svc.MdmExpansionCacheSvc;
import ca.uhn.fhir.jpa.dao.tx.IHapiTransactionService;
import ca.uhn.fhir.jpa.delete.ThreadSafeResourceDeleterSvc;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.partition.IRequestPartitionHelperSvc;
import ca.uhn.fhir.jpa.partition.PartitionManagementProvider;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.searchparam.MatchUrlService;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.annotations.OnMatchboxOnlyOneEnginePresent;
import ca.uhn.fhir.jpa.starter.common.StarterJpaConfig;
import ca.uhn.fhir.jpa.validation.ValidatorPolicyAdvisor;
import ca.uhn.fhir.jpa.validation.ValidatorResourceFetcher;
import ca.uhn.fhir.mdm.provider.MdmProviderLoader;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.MatchboxRestfulServer;
import ch.ahdis.matchbox.interceptors.HttpReadOnlyInterceptor;
import ch.ahdis.matchbox.interceptors.MappingLanguageInterceptor;
import ch.ahdis.matchbox.interceptors.MatchboxValidationInterceptor;
import ch.ahdis.matchbox.mappinglanguage.StructureMapListProvider;
import ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider;
import ch.ahdis.matchbox.packages.*;
import ch.ahdis.matchbox.providers.*;
import ch.ahdis.matchbox.questionnaire.*;
import ch.ahdis.matchbox.statistics.StatisticsObservationProvider;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;
import ch.ahdis.matchbox.validation.ValidationProvider;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Configuration
@Import(ThreadPoolFactoryConfig.class)
@EnableConfigurationProperties(MatchboxFhirContextProperties.class)
public class MatchboxJpaConfig extends StarterJpaConfig {

	@Bean
	public MatchboxRestfulServer restfulServer(final IFhirSystemDao<?, ?> fhirSystemDao,
															 final AppProperties appProperties,
															 final CliContext cliContext,
															 final FhirContext fhirContext,
															 final ApplicationContext applicationContext,
															 final MatchboxFhirVersion matchboxFhirVersion,
															 final DaoRegistry daoRegistry,
															 final MatchboxEngineSupport matchboxEngineSupport,
															 final Optional<MdmProviderLoader> mdmProviderProvider,
															 final IJpaSystemProvider jpaSystemProvider,
															 final ResourceProviderFactory resourceProviderFactory,
															 final JpaStorageSettings daoConfig,
															 final ISearchParamRegistry searchParamRegistry,
															 final DatabaseBackedPagingProvider databaseBackedPagingProvider,
															 final LoggingInterceptor loggingInterceptor,
															 final Optional<CorsInterceptor> corsInterceptor,
															 final IInterceptorBroadcaster interceptorBroadcaster,
															 final Optional<BinaryAccessProvider> binaryAccessProvider,
															 final BinaryStorageInterceptor binaryStorageInterceptor,
															 final PartitionManagementProvider partitionManagementProvider,
															 final IPackageInstallerSvc packageInstallerSvc,
															 final ThreadSafeResourceDeleterSvc theThreadSafeResourceDeleterSvc,
															 final IHapiPackageCacheManager myPackageCacheManager,

															 // Matchbox providers
															 final InstallNpmPackageProvider installNpmPackageOperationProvider,
															 final StructureDefinitionResourceProvider structureDefinitionProvider,
															 final Optional<ValueSetResourceProvider> valueSetProvider,
															 final Optional<ConceptMapResourceProvider> conceptMapProvider,
															 final Optional<CodeSystemResourceProvider> codeSystemProvider,
															 final Optional<StructureMapTransformProvider> structureMapTransformProvider,
															 final StructureMapListProvider structureMapListProvider,
															 final Optional<QuestionnaireResourceProvider> questionnaireProvider,
															 final Optional<QuestionnaireAssembleProviderR4> assembleProviderR4,
															 final Optional<QuestionnaireAssembleProviderR4B> assembleProviderR4B,
															 final Optional<QuestionnaireAssembleProviderR5> assembleProviderR5,
															 final Optional<QuestionnaireResponseExtractProviderR4> questionnaireResponseProviderR4,
															 final Optional<QuestionnaireResponseExtractProviderR4B> questionnaireResponseProviderR4B,
															 final Optional<QuestionnaireResponseExtractProviderR5> questionnaireResponseProviderR5,
															 final Optional<ImplementationGuideProviderR4> implementationGuideResourceProviderR4,
															 final Optional<ImplementationGuideProviderR4B> implementationGuideResourceProviderR4B,
															 final Optional<ImplementationGuideProviderR5> implementationGuideResourceProviderR5,
															 final ValidationProvider validationProvider,
															 final StatisticsObservationProvider statisticsObservationProvider) {

		final var fhirServer = super.restfulServer(fhirSystemDao,
																 appProperties,
																 daoRegistry,
																 mdmProviderProvider,
																 jpaSystemProvider,
																 resourceProviderFactory,
																 daoConfig,
																 searchParamRegistry,
																 databaseBackedPagingProvider,
																 loggingInterceptor,
																 null,
																 null,
																 corsInterceptor,
																 interceptorBroadcaster,
																 binaryAccessProvider,
																 binaryStorageInterceptor,
																 null,
																 null,
																 null,
																 null,
																 null,
																 null,
																 partitionManagementProvider,
																 null,
																 packageInstallerSvc,
																 theThreadSafeResourceDeleterSvc);

		if (cliContext.isHttpReadOnly()) {
			fhirServer.registerInterceptor(new HttpReadOnlyInterceptor());
		}
		fhirServer.registerInterceptor(new MappingLanguageInterceptor(matchboxEngineSupport));
		fhirServer.registerInterceptor(new ImplementationGuidePackageInterceptor(myPackageCacheManager, fhirContext));
		fhirServer.registerInterceptor(new MatchboxValidationInterceptor(fhirContext));

		fhirServer.registerProviders(
			validationProvider,
			statisticsObservationProvider,
			structureDefinitionProvider,
			structureMapListProvider
		);

		registerOptionalProviders(
			fhirServer,
			questionnaireProvider,
			conceptMapProvider,
			codeSystemProvider,
			valueSetProvider,
			structureMapTransformProvider
		);

		if (!cliContext.isHttpReadOnly()) {
			// The operation $install-npm-package is enabled if the httpReadOnly mode is disabled
			fhirServer.registerProvider(installNpmPackageOperationProvider);
		}

		matchboxFhirVersion.execute(
			() -> {
				registerOptionalProviders(
					fhirServer,
					implementationGuideResourceProviderR4,
					assembleProviderR4,
					questionnaireResponseProviderR4
				);

				if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages()
					&& appProperties.getImplementationGuides() != null) {
					implementationGuideResourceProviderR4.get().loadAll(true);
					int exitCode = SpringApplication.exit(applicationContext, () -> 0);
					System.exit(exitCode);
				}
			},
			() -> {
				registerOptionalProviders(
					fhirServer,
					implementationGuideResourceProviderR4B,
					assembleProviderR4B,
					questionnaireResponseProviderR4B
				);

				if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages()
					&& appProperties.getImplementationGuides() != null) {
					implementationGuideResourceProviderR4B.get().loadAll(true);
					int exitCode = SpringApplication.exit(applicationContext, () -> 0);
					System.exit(exitCode);
				}
			},
			() -> {
				registerOptionalProviders(
					fhirServer,
					implementationGuideResourceProviderR5,
					assembleProviderR5,
					questionnaireResponseProviderR5
				);

				if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages()
					&& appProperties.getImplementationGuides() != null) {
					implementationGuideResourceProviderR5.get().loadAll(true);
					int exitCode = SpringApplication.exit(applicationContext, () -> 0);
					System.exit(exitCode);
				}
			}
		);

		fhirServer.setServerConformanceProvider(new MatchboxCapabilityStatementProvider(fhirContext,
																												  fhirServer,
																												  structureDefinitionProvider,
																												  cliContext,
																												  matchboxFhirVersion));

		return fhirServer;
	}

	@Bean
	public CliContext getCliContext(final Environment environment) {
		return new CliContext(environment);
	}

	@Bean
	public MatchboxEngineSupport getMatchboxEngineSupport(final MatchboxFhirContextProperties matchboxFhirContextProperties,
																			final CliContext cliContext,
																			@Value("${hapi.fhir.fhir_version}") final FhirVersionEnum serverFhirVersion) {
		return new MatchboxEngineSupport(matchboxFhirContextProperties, cliContext, serverFhirVersion);
	}

	@Bean
	public IJobCoordinator batch2JobCoordinator() {

		// return an implementation of the interface IJobCoordinator

		return new IJobCoordinator() {

			public Batch2JobStartResponse startInstance(RequestDetails theRequestDetails,
																	  JobInstanceStartRequest theStartRequest)
				throws InvalidRequestException {
				// start a job instance
				return null;
			}

			/**s
			 * Fetch details about a job instance
			 *
			 * @param theInstanceId The instance ID
			 * @return Returns the current instance details
			 * @throws ResourceNotFoundException If the instance ID can not be found
			 */
			public JobInstance getInstance(String theInstanceId) throws ResourceNotFoundException {
				// fetch details about a job instance
				return null;
			}

			/**
			 * Fetch all job instances
			 */
			public List<JobInstance> getInstances(int thePageSize, int thePageIndex) {
				// fetch all job instances
				return null;
			}

			/**
			 * Fetch recent job instances
			 */
			public List<JobInstance> getRecentInstances(int theCount, int theStart) {
				// fetch recent job instances
				return null;
			}

			public JobOperationResultJson cancelInstance(String theInstanceId) throws ResourceNotFoundException {
				// cancel a job instance
				return null;
			}

			public List<JobInstance> getInstancesbyJobDefinitionIdAndEndedStatus(String theJobDefinitionId,
																										@Nullable Boolean theEnded,
																										int theCount,
																										int theStart) {
				// fetch job instances by job definition id and ended status
				return List.of();
			}

			/**
			 * Fetches all job instances tht meet the FetchRequest criteria
			 * @param theFetchRequest - fetch request
			 * @return - page of job instances
			 */
			public Page<JobInstance> fetchAllJobInstances(JobInstanceFetchRequest theFetchRequest) {
				// fetch all job instances
				return null;
			}

			/**
			 * Fetches all job instances by job definition id and statuses
			 */
			public List<JobInstance> getJobInstancesByJobDefinitionIdAndStatuses(String theJobDefinitionId,
																										Set<StatusEnum> theStatuses,
																										int theCount,
																										int theStart) {
				// fetch job instances by job definition id and statuses
				return List.of();
			}

			/**
			 * Fetches all jobs by job definition id
			 */
			public List<JobInstance> getJobInstancesByJobDefinitionId(String theJobDefinitionId,
																						 int theCount,
																						 int theStart) {
				// fetch job instances by job definition id
				return List.of();
			}

			@Override
			public List<BatchWorkChunkStatusDTO> getWorkChunkStatus(final String s) {
				return List.of();
			}

			@Override
			public BatchInstanceStatusDTO getBatchInstanceStatus(final String s) {
				return null;
			}
		};
	}

	@Bean
	UrlPartitioner urlPartitioner() {
		return new UrlPartitioner(null, null);
	}

	// @Bean
	// public IBatch2JobRunner batch2JobRunner() {
	// 	return new Batch2JobRunnerImpl();
	// }

	@Bean
	@Primary
	public IBulkExportProcessor jpaBulkExportProcessor() {
		return new IBulkExportProcessor() {

			@Override
			public void expandMdmResources(List theResources) {
				throw new UnsupportedOperationException("Unimplemented method 'expandMdmResources'");
			}

			@Override
			public Iterator getResourcePidIterator(ExportPIDIteratorParameters theParams) {
				throw new UnsupportedOperationException("Unimplemented method 'getResourcePidIterator'");
			}

			@Override
			public Set<String> getPatientSetForGroupExport(ExportPIDIteratorParameters theParams) {
				throw new UnsupportedOperationException("Unimplemented method 'getPatientSetForGroupExport'");
			}

			@Override
			public Set<String> getPatientSetForPatientExport(ExportPIDIteratorParameters theParams) {
				throw new UnsupportedOperationException("Unimplemented method 'getPatientSetForPatientExport'");
			}
		};
	}

	@Bean
	public MdmExpansionCacheSvc mdmExpansionCacheSvc() {
		return new MdmExpansionCacheSvc();
	}

	@Bean
	public IJobPartitionProvider jobPartitionProvider(
		FhirContext theFhirContext,
		IRequestPartitionHelperSvc theRequestPartitionHelperSvc,
		MatchUrlService theMatchUrlService) {
		return new DefaultJobPartitionProvider(theFhirContext, theRequestPartitionHelperSvc, theMatchUrlService);
	}

	@Bean
	public IJobPersistence batch2JobInstancePersister(
		IBatch2JobInstanceRepository theJobInstanceRepository,
		IBatch2WorkChunkRepository theWorkChunkRepository,
		IBatch2WorkChunkMetadataViewRepository theWorkChunkMetadataViewRepo,
		IHapiTransactionService theTransactionService,
		EntityManager theEntityManager,
		IInterceptorBroadcaster theInterceptorBroadcaster) {
		return new JpaJobPersistenceImpl(
			theJobInstanceRepository,
			theWorkChunkRepository,
			theWorkChunkMetadataViewRepo,
			theTransactionService,
			theEntityManager,
			theInterceptorBroadcaster);
	}

	@Bean
	public InstallNpmPackageProvider installNpmPackageOperationProvider(final MatchboxPackageInstallerImpl packageInstallerSvc) {
		return new InstallNpmPackageProvider(packageInstallerSvc);
	}

	@Bean
	public MatchboxFhirVersion matchboxFhirVersion(final MatchboxEngineSupport matchboxEngineSupport) {
		return new MatchboxFhirVersion(matchboxEngineSupport.getServerFhirVersion());
	}

	@Bean
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	@Primary
	public StructureMapTransformProvider structureMapTransformProvider() {
		return new StructureMapTransformProvider();
	}

	@Bean
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	@Primary
	public ConceptMapResourceProvider conceptMapResourceProvider() {
		return new ConceptMapResourceProvider();
	}

	@Bean
	@Primary
	public StructureDefinitionResourceProvider structureDefinitionResourceProvider() {
		return new StructureDefinitionResourceProvider();
	}

	@Bean
	public StructureMapListProvider structureMapListProvider(final MatchboxEngineSupport matchboxEngineSupport,
																				final MatchboxFhirVersion matchboxFhirVersion) {
		return new StructureMapListProvider(matchboxEngineSupport, matchboxFhirVersion);
	}

	@Bean
	public ValidatorResourceFetcher jpaValidatorResourceFetcher() {
		return new ValidatorResourceFetcher();
	}

	@Bean
	public ValidatorPolicyAdvisor jpaValidatorPolicyAdvisor() {
		return new ValidatorPolicyAdvisor();
	}

	@Bean
	@Primary
	public MatchboxPackageInstallerImpl packageInstaller() {
		return new MatchboxPackageInstallerImpl();
	}

	@Bean
	public ValidationProvider validationProvider() {
		return new ValidationProvider();
	}

	@Bean
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	@Primary
	public QuestionnaireResourceProvider questionnaireResourceProvider() {
		return new QuestionnaireResourceProvider();
	}

	@Bean
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	@Primary
	public ValueSetResourceProvider valueSetResourceProvider() {
		return new ValueSetResourceProvider();
	}

	@Bean
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	@Primary
	public CodeSystemResourceProvider codeSystemResourceProvider() {
		return new CodeSystemResourceProvider();
	}

	private static void registerOptionalProvider(final MatchboxRestfulServer fhirServer,
																final Optional<Object> provider) {
		provider.ifPresent(fhirServer::registerProvider);
	}

	private static void registerOptionalProviders(final MatchboxRestfulServer fhirServer,
																 final Optional<?>... providers) {
		Stream.of(providers).forEach(opt -> opt.ifPresent(fhirServer::registerProvider));
	}
}
