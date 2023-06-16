package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.cr.config.CrDstu3Config;
import ca.uhn.fhir.cr.dstu3.IActivityDefinitionProcessorFactory;
import ca.uhn.fhir.cr.dstu3.IPlanDefinitionProcessorFactory;
import ca.uhn.fhir.cr.dstu3.IQuestionnaireProcessorFactory;
import ca.uhn.fhir.cr.dstu3.IQuestionnaireResponseProcessorFactory;
import ca.uhn.fhir.cr.dstu3.activitydefinition.ActivityDefinitionOperationsProvider;
import ca.uhn.fhir.cr.dstu3.plandefinition.PlanDefinitionOperationsProvider;
import ca.uhn.fhir.cr.dstu3.questionnaire.QuestionnaireOperationsProvider;
import ca.uhn.fhir.cr.dstu3.questionnaireresponse.QuestionnaireResponseOperationsProvider;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.annotations.OnDSTU3Condition;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.opencds.cqf.cql.evaluator.CqlOptions;
import org.opencds.cqf.cql.evaluator.activitydefinition.dstu3.ActivityDefinitionProcessor;
import org.opencds.cqf.cql.evaluator.library.EvaluationSettings;
import org.opencds.cqf.cql.evaluator.plandefinition.dstu3.PlanDefinitionProcessor;
import org.opencds.cqf.cql.evaluator.questionnaire.dstu3.QuestionnaireProcessor;
import org.opencds.cqf.cql.evaluator.questionnaireresponse.dstu3.QuestionnaireResponseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;

@Configuration
@Conditional({ OnDSTU3Condition.class, CrConfigCondition.class })
@Import({ CrDstu3Config.class })
public class StarterCrDstu3Config {
	private static final Logger ourLogger = LoggerFactory.getLogger(StarterCrDstu3Config.class);
	@Bean
	public PostInitProviderRegisterer postInitProviderRegisterer(RestfulServer theRestfulServer,
			ResourceProviderFactory theResourceProviderFactory) {
		return new PostInitProviderRegisterer(theRestfulServer, theResourceProviderFactory);
	}

	@Bean
	public CrOperationProviderFactory crOperationProviderFactory() {
		return new CrOperationProviderFactory();
	}

	@Bean
	public CrOperationProviderLoader crOperationProviderLoader(FhirContext theFhirContext,
			ResourceProviderFactory theResourceProviderFactory,
			CrOperationProviderFactory theCrOperationProviderFactory,
			PostInitProviderRegisterer thePostInitProviderRegister) {
		return new CrOperationProviderLoader(theFhirContext, theResourceProviderFactory, theCrOperationProviderFactory,
				thePostInitProviderRegister);
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
	@Primary
	@Bean
	public CqlOptions cqlOptions(AppProperties theAppProperties) {
		return theAppProperties.getCqlOptions();
	}

	@Primary
	@Bean
	public CqlTranslatorOptions cqlTranslatorOptions(FhirContext theFhirContext, AppProperties theAppProperties) {
		CqlTranslatorOptions options = theAppProperties.getCqlTranslatorOptions();

		if (theFhirContext.getVersion().getVersion().isOlderThan(FhirVersionEnum.R4)
			&& (options.getCompatibilityLevel().equals("1.5") || options.getCompatibilityLevel().equals("1.4"))) {
			ourLogger.warn("{} {} {}",
				"This server is configured to use CQL version > 1.4 and FHIR version <= DSTU3.",
				"Most available CQL content for DSTU3 and below is for CQL versions 1.3.",
				"If your CQL content causes translation errors, try setting the CQL compatibility level to 1.3");
		}

		return options;
	}
}
