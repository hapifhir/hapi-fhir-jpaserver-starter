package ch.ahdis.matchbox;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import ca.uhn.fhir.jpa.dao.tx.IHapiTransactionService;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProviderR4;
import ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProviderR5;
import ch.ahdis.matchbox.interceptor.TerminologyCapabilitiesInterceptor;
import ch.ahdis.matchbox.terminology.CodeSystemCodeValidationProvider;
import ch.ahdis.matchbox.terminology.ValueSetCodeValidationProvider;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import ca.uhn.fhir.batch2.model.StatusEnum;
import ca.uhn.fhir.batch2.api.IJobCoordinator;
import ca.uhn.fhir.batch2.api.IJobPersistence;
import ca.uhn.fhir.batch2.api.JobOperationResultJson;
import ca.uhn.fhir.batch2.jobs.parameters.UrlPartitioner;
import ca.uhn.fhir.batch2.model.JobInstance;
import ca.uhn.fhir.batch2.model.JobInstanceStartRequest;
import ca.uhn.fhir.batch2.models.JobInstanceFetchRequest;
import ca.uhn.fhir.context.FhirContext;
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
import ca.uhn.fhir.jpa.config.util.ValidationSupportConfigUtil;
import ca.uhn.fhir.jpa.dao.data.IBatch2JobInstanceRepository;
import ca.uhn.fhir.jpa.dao.data.IBatch2WorkChunkRepository;
import ca.uhn.fhir.jpa.dao.mdm.MdmExpansionCacheSvc;
import ca.uhn.fhir.jpa.delete.ThreadSafeResourceDeleterSvc;
import ca.uhn.fhir.jpa.model.sched.ISchedulerService;
import ca.uhn.fhir.jpa.model.sched.ScheduledJobDefinition;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.partition.PartitionManagementProvider;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.common.StarterJpaConfig;
import ca.uhn.fhir.jpa.validation.JpaValidationSupportChain;
import ca.uhn.fhir.mdm.provider.MdmProviderLoader;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ch.ahdis.fhir.hapi.jpa.validation.ValidationProvider;
import ch.ahdis.matchbox.interceptor.ImplementationGuidePackageInterceptor;
import ch.ahdis.matchbox.interceptor.MappingLanguageInterceptor;
import ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProvider;
import org.springframework.data.domain.Page;

import ca.uhn.fhir.jpa.bulk.export.model.ExportPIDIteratorParameters;

import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;

@Configuration
@Import(ThreadPoolFactoryConfig.class)
public class MatchboxJpaConfig extends StarterJpaConfig {	

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MatchboxJpaConfig.class);

	@Autowired
	private Environment environment;

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

	@Autowired
	private ApplicationContext context;

	@Autowired(required = false)
	private ImplementationGuideProviderR4 implementationGuideResourceProviderR4;

	@Autowired(required = false)
	private ImplementationGuideProviderR5 implementationGuideResourceProviderR5;

	@Autowired
	private CodeSystemCodeValidationProvider codeSystemCodeValidationProvider;

	@Autowired
	private ValueSetCodeValidationProvider valueSetCodeValidationProvider;


	// removed GraphQlProvider
	// removed IVAldiationSupport
	
	@Bean
	public RestfulServer restfulServer(IFhirSystemDao<?, ?> fhirSystemDao, AppProperties appProperties,
			DaoRegistry daoRegistry, Optional<MdmProviderLoader> mdmProviderProvider, IJpaSystemProvider jpaSystemProvider,
			ResourceProviderFactory resourceProviderFactory, JpaStorageSettings daoConfig, ISearchParamRegistry searchParamRegistry,
			DatabaseBackedPagingProvider databaseBackedPagingProvider,
			LoggingInterceptor loggingInterceptor, 
			Optional<CorsInterceptor> corsInterceptor, IInterceptorBroadcaster interceptorBroadcaster,
			Optional<BinaryAccessProvider> binaryAccessProvider, BinaryStorageInterceptor binaryStorageInterceptor,
			PartitionManagementProvider partitionManagementProvider,
			IPackageInstallerSvc packageInstallerSvc, ThreadSafeResourceDeleterSvc theThreadSafeResourceDeleterSvc) {

		RestfulServer fhirServer = super.restfulServer(fhirSystemDao, appProperties, daoRegistry, mdmProviderProvider,
				jpaSystemProvider, resourceProviderFactory, daoConfig, searchParamRegistry,
				databaseBackedPagingProvider, loggingInterceptor, null, null,
				corsInterceptor, interceptorBroadcaster, binaryAccessProvider, binaryStorageInterceptor, null,
				null, null, null, null, null,
				partitionManagementProvider,  null, packageInstallerSvc, theThreadSafeResourceDeleterSvc);

		fhirServer.registerInterceptor(new MappingLanguageInterceptor(matchboxEngineSupport));
		fhirServer.registerInterceptor(new ImplementationGuidePackageInterceptor(myPackageCacheManager, myFhirContext));
		fhirServer.registerInterceptor(new MatchboxValidationInterceptor(this.myFhirContext,structureDefinitionProvider));
		fhirServer.registerInterceptor(new TerminologyCapabilitiesInterceptor());
		fhirServer.registerProviders(validationProvider, questionnaireProvider, questionnaireResponseProvider,
				assembleProvider, conceptMapProvider, codeSystemProvider, valueSetProvider, structureDefinitionProvider,
											  structureMapTransformProvider, codeSystemCodeValidationProvider,
											  valueSetCodeValidationProvider);


		ScheduledJobDefinition jobDefinition = new ScheduledJobDefinition();
		jobDefinition.setId(this.getClass().getName());

		switch (this.myFhirContext.getVersion().getVersion()) {
			case R4 -> {
				fhirServer.registerProviders(this.implementationGuideResourceProviderR4);
				jobDefinition.setJobClass(ImplementationGuideProviderR4.class);

				if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages()
					&& appProperties.getImplementationGuides() != null) {
					this.implementationGuideResourceProviderR4.loadAll(true);
					int exitCode = SpringApplication.exit(this.context, ()->0);
					System.exit(exitCode);
				}
			}
			case R5 -> {
				fhirServer.registerProviders(this.implementationGuideResourceProviderR5);
				jobDefinition.setJobClass(ImplementationGuideProviderR5.class);

				if (appProperties.getOnly_install_packages() != null && appProperties.getOnly_install_packages()
					&& appProperties.getImplementationGuides() != null) {
					this.implementationGuideResourceProviderR5.loadAll(true);
					int exitCode = SpringApplication.exit(this.context, ()->0);
					System.exit(exitCode);
				}
			}
			default -> throw new NotImplementedException("MatchboxJpaConfig: this FHIR version has no supported " +
																      "implementationGuideResourceProvider");
		}

		fhirServer.setServerConformanceProvider(new MatchboxCapabilityStatementProvider(this.myFhirContext,fhirServer, structureDefinitionProvider, getCliContext()));

		mySvc.scheduleLocalJob(DateUtils.MILLIS_PER_MINUTE, jobDefinition);

		return fhirServer;
	}

	@Bean
	public CliContext getCliContext() {
	  	return new CliContext(this.environment);
	}
	
	@Bean
	public MatchboxEngineSupport getMatchboxEngineSupport() {
		return new MatchboxEngineSupport();
	}

	@Bean
	public IJobCoordinator batch2JobCoordinator() {

		// return an implementation of the interface IJobCoordinator

		return new IJobCoordinator() {

			public Batch2JobStartResponse startInstance(RequestDetails theRequestDetails, JobInstanceStartRequest theStartRequest) throws InvalidRequestException {
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
		
			public List<JobInstance> getInstancesbyJobDefinitionIdAndEndedStatus(String theJobDefinitionId, @Nullable Boolean theEnded, int theCount, int theStart) {
				// fetch job instances by job definition id and ended status
				return null;
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
			public List<JobInstance> getJobInstancesByJobDefinitionIdAndStatuses(String theJobDefinitionId, Set<StatusEnum> theStatuses, int theCount, int theStart) {
				// fetch job instances by job definition id and statuses
				return null;
			}
		
			/**
			 * Fetches all jobs by job definition id
			 */
			public List<JobInstance> getJobInstancesByJobDefinitionId(String theJobDefinitionId, int theCount, int theStart) {
				// fetch job instances by job definition id
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
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Unimplemented method 'expandMdmResources'");
			}
			@Override
			public Iterator getResourcePidIterator(ExportPIDIteratorParameters theParams) {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Unimplemented method 'getResourcePidIterator'");
			}
		};
	}

	@Bean
	public MdmExpansionCacheSvc mdmExpansionCacheSvc() {
		return new MdmExpansionCacheSvc();
	}

	@Bean
	public CachingValidationSupport validationSupportChain(JpaValidationSupportChain theJpaValidationSupportChain) {
		return ValidationSupportConfigUtil.newCachingValidationSupport(theJpaValidationSupportChain);
	}

	@Bean
	public IJobPersistence batch2JobInstancePersister(IBatch2JobInstanceRepository theJobInstanceRepository, IBatch2WorkChunkRepository theWorkChunkRepository, IHapiTransactionService theTransactionService, EntityManager theEntityManager) {
		return new JpaJobPersistenceImpl(theJobInstanceRepository, theWorkChunkRepository, theTransactionService, theEntityManager);
	}



}
