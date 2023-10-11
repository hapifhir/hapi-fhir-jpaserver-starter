package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.cr.common.IRepositoryFactory;
import ca.uhn.fhir.cr.config.ProviderLoader;
import ca.uhn.fhir.cr.config.ProviderSelector;
import ca.uhn.fhir.cr.r4.IActivityDefinitionProcessorFactory;
import ca.uhn.fhir.cr.r4.ICareGapsServiceFactory;
import ca.uhn.fhir.cr.r4.ICqlExecutionServiceFactory;
import ca.uhn.fhir.cr.r4.IMeasureServiceFactory;
import ca.uhn.fhir.cr.r4.IPlanDefinitionProcessorFactory;
import ca.uhn.fhir.cr.r4.IQuestionnaireProcessorFactory;
import ca.uhn.fhir.cr.r4.IQuestionnaireResponseProcessorFactory;
import ca.uhn.fhir.cr.r4.ISubmitDataProcessorFactory;
import ca.uhn.fhir.cr.r4.activitydefinition.ActivityDefinitionApplyProvider;
import ca.uhn.fhir.cr.r4.cqlexecution.CqlExecutionOperationProvider;
import ca.uhn.fhir.cr.r4.measure.CareGapsOperationProvider;
import ca.uhn.fhir.cr.r4.measure.MeasureOperationsProvider;
import ca.uhn.fhir.cr.r4.measure.SubmitDataProvider;
import ca.uhn.fhir.cr.r4.plandefinition.PlanDefinitionApplyProvider;
import ca.uhn.fhir.cr.r4.plandefinition.PlanDefinitionPackageProvider;
import ca.uhn.fhir.cr.r4.questionnaire.QuestionnairePackageProvider;
import ca.uhn.fhir.cr.r4.questionnaire.QuestionnairePopulateProvider;
import ca.uhn.fhir.cr.r4.questionnaireresponse.QuestionnaireResponseExtractProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cr.activitydefinition.r4.ActivityDefinitionProcessor;
import org.opencds.cqf.fhir.cr.cql.r4.R4CqlExecutionService;
import org.opencds.cqf.fhir.cr.measure.CareGapsProperties;
import org.opencds.cqf.fhir.cr.measure.MeasureEvaluationOptions;
import org.opencds.cqf.fhir.cr.measure.r4.R4CareGapsService;
import org.opencds.cqf.fhir.cr.measure.r4.R4MeasureService;
import org.opencds.cqf.fhir.cr.measure.r4.R4SubmitDataService;
import org.opencds.cqf.fhir.cr.plandefinition.r4.PlanDefinitionProcessor;
import org.opencds.cqf.fhir.cr.questionnaire.r4.QuestionnaireProcessor;
import org.opencds.cqf.fhir.cr.questionnaireresponse.r4.QuestionnaireResponseProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
public class CrR4Config {

	@Bean
	IMeasureServiceFactory r4MeasureServiceFactory(
		IRepositoryFactory theRepositoryFactory, MeasureEvaluationOptions theEvaluationOptions) {
		return rd -> new R4MeasureService(theRepositoryFactory.create(rd), theEvaluationOptions);
	}

	@Bean
	ISubmitDataProcessorFactory r4SubmitDataProcessorFactory(IRepositoryFactory theRepositoryFactory) {
		return rd -> new R4SubmitDataService(theRepositoryFactory.create(rd));
	}

	@Bean
	ICqlExecutionServiceFactory r4CqlExecutionServiceFactory(
		IRepositoryFactory theRepositoryFactory, EvaluationSettings theEvaluationSettings) {
		return rd -> new R4CqlExecutionService(theRepositoryFactory.create(rd), theEvaluationSettings);
	}

	@Bean
	CqlExecutionOperationProvider r4CqlExecutionOperationProvider() {
		return new CqlExecutionOperationProvider();
	}

	@Bean
	ICareGapsServiceFactory careGapsServiceFactory(
		IRepositoryFactory theRepositoryFactory,
		CareGapsProperties theCareGapsProperties,
		MeasureEvaluationOptions theMeasureEvaluationOptions,
		@Qualifier("cqlExecutor") Executor theExecutor) {
		return rd -> new R4CareGapsService(
			theCareGapsProperties,
			theRepositoryFactory.create(rd),
			theMeasureEvaluationOptions,
			theExecutor,
			rd.getFhirServerBase());
	}

	@Bean
	CareGapsOperationProvider r4CareGapsOperationProvider() {
		return new CareGapsOperationProvider();
	}

	@Bean
	SubmitDataProvider r4SubmitDataProvider() {
		return new SubmitDataProvider();
	}

	@Bean
	MeasureOperationsProvider r4MeasureOperationsProvider() {
		return new MeasureOperationsProvider();
	}

	@Bean
	IActivityDefinitionProcessorFactory r4ActivityDefinitionProcessorFactory(
		IRepositoryFactory theRepositoryFactory, EvaluationSettings theEvaluationSettings) {
		return rd -> new ActivityDefinitionProcessor(
			theRepositoryFactory.create(rd), theEvaluationSettings);
	}

	@Bean
	IPlanDefinitionProcessorFactory r4PlanDefinitionProcessorFactory(
		IRepositoryFactory theRepositoryFactory, EvaluationSettings theEvaluationSettings) {
		return rd -> new PlanDefinitionProcessor(
			theRepositoryFactory.create(rd), theEvaluationSettings);
	}

	@Bean
	IQuestionnaireProcessorFactory r4QuestionnaireProcessorFactory(
		IRepositoryFactory theRepositoryFactory, EvaluationSettings theEvaluationSettings) {
		return rd -> new QuestionnaireProcessor(
			theRepositoryFactory.create(rd), theEvaluationSettings);
	}

	@Bean
	IQuestionnaireResponseProcessorFactory r4QuestionnaireResponseProcessorFactory(
		IRepositoryFactory theRepositoryFactory, EvaluationSettings theEvaluationSettings) {
		return rd -> new QuestionnaireResponseProcessor(
			theRepositoryFactory.create(rd), theEvaluationSettings);
	}

	@Bean
	ActivityDefinitionApplyProvider r4ActivityDefinitionApplyProvider() {
		return new ActivityDefinitionApplyProvider();
	}

	@Bean
	PlanDefinitionApplyProvider r4PlanDefinitionApplyProvider() {
		return new PlanDefinitionApplyProvider();
	}

	@Bean
	QuestionnaireResponseExtractProvider
	r4QuestionnaireResponseExtractProvider() {
		return new QuestionnaireResponseExtractProvider();
	}

	@Bean
	PlanDefinitionPackageProvider r4PlanDefinitionPackageProvider() {
		return new PlanDefinitionPackageProvider();
	}

	@Bean
	QuestionnairePackageProvider r4QuestionnairePackageProvider() {
		return new QuestionnairePackageProvider();
	}

	@Bean
	QuestionnairePopulateProvider r4QuestionnairePopulateProvider() {
		return new QuestionnairePopulateProvider();
	}

	@Bean
	public ProviderLoader r4PdLoader(
		ApplicationContext theApplicationContext, FhirContext theFhirContext, RestfulServer theRestfulServer) {

		var selector = new ProviderSelector(
			theFhirContext,
			Map.of(
				FhirVersionEnum.R4,
				Arrays.asList(
					MeasureOperationsProvider.class,
					SubmitDataProvider.class,
					CareGapsOperationProvider.class,
					CqlExecutionOperationProvider.class,
					ActivityDefinitionApplyProvider.class,
					PlanDefinitionApplyProvider.class,
					QuestionnaireResponseExtractProvider.class,
					QuestionnairePackageProvider.class,
					PlanDefinitionPackageProvider.class,
					QuestionnairePopulateProvider.class)));

		return new ProviderLoader(theRestfulServer, theApplicationContext, selector);
	}
}