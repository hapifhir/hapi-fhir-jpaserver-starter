package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.cr.common.IRepositoryFactory;
import ca.uhn.fhir.cr.config.CrR4Config;
import ca.uhn.fhir.cr.r4.IActivityDefinitionProcessorFactory;
import ca.uhn.fhir.cr.r4.IPlanDefinitionProcessorFactory;
import ca.uhn.fhir.cr.r4.IQuestionnaireProcessorFactory;
import ca.uhn.fhir.cr.r4.IQuestionnaireResponseProcessorFactory;
import ca.uhn.fhir.cr.r4.activitydefinition.ActivityDefinitionOperationsProvider;
import ca.uhn.fhir.cr.r4.plandefinition.PlanDefinitionOperationsProvider;
import ca.uhn.fhir.cr.r4.questionnaire.QuestionnaireOperationsProvider;
import ca.uhn.fhir.cr.r4.questionnaireresponse.QuestionnaireResponseOperationsProvider;
import ca.uhn.fhir.cr.repo.HapiFhirRepository;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import org.opencds.cqf.cql.evaluator.activitydefinition.r4.ActivityDefinitionProcessor;
import org.opencds.cqf.cql.evaluator.library.EvaluationSettings;
import org.opencds.cqf.cql.evaluator.plandefinition.r4.PlanDefinitionProcessor;
import org.opencds.cqf.cql.evaluator.questionnaire.r4.QuestionnaireProcessor;
import org.opencds.cqf.cql.evaluator.questionnaireresponse.r4.QuestionnaireResponseProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Map;

@Configuration
@Conditional({OnR4Condition.class, CrConfigCondition.class})
@Import({CrR4Config.class})
public class StarterCrR4Config {
	@Bean
	CrOperationFactory crOperationFactory() {
		return new CrOperationFactory();
	}

	@Bean
	CrOperationLoader crOperationLoader(FhirContext theFhirContext, ResourceProviderFactory theResourceProviderFactory,
													CrOperationFactory theCrlProviderFactory) {
		return new CrOperationLoader(theFhirContext, theResourceProviderFactory, theCrlProviderFactory);
	}
	@Bean
	public QuestionnaireOperationsProvider myR4QuestionnaireOperationsProvider() {
		return new QuestionnaireOperationsProvider();
	}

	@Bean
	public PlanDefinitionOperationsProvider r4PlanDefinitionOperationsProvider() {
		return new PlanDefinitionOperationsProvider();
	}

	@Bean
	public QuestionnaireResponseOperationsProvider myR4QuestionnaireResponseOperationsProvider() {
		return new QuestionnaireResponseOperationsProvider();
	}

	@Bean
	public ActivityDefinitionOperationsProvider myR4ActivityDefinitionOperationsProvider() {
		return new ActivityDefinitionOperationsProvider();
	}


	@Bean
	IActivityDefinitionProcessorFactory myR4ActivityDefinitionProcessorFactory(
		EvaluationSettings theEvaluationSettings) {
		return r -> new ActivityDefinitionProcessor(r,
			theEvaluationSettings);
	}

	@Bean
	IQuestionnaireResponseProcessorFactory myR4QuestionnaireResponseProcessorFactory() {
		return r -> new QuestionnaireResponseProcessor(r);
	}

	@Bean
	IQuestionnaireProcessorFactory myR4QuestionnaireProcessorFactory() {
		return r -> new QuestionnaireProcessor(r);
	}

	@Bean
	IPlanDefinitionProcessorFactory myR4PlanDefinitionProcessorFactory(
		EvaluationSettings theEvaluationSettings) {
		return r -> new PlanDefinitionProcessor(r, theEvaluationSettings);
	}

	@Bean
	IRepositoryFactory repositoryFactory(DaoRegistry theDaoRegistry) {
		return rd -> new HapiFhirRepository(theDaoRegistry, rd, (RestfulServer) rd.getServer());
	}
}
