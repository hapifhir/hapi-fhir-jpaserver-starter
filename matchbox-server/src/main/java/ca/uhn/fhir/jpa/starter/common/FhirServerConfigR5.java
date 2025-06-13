package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.config.r5.JpaR5Config;
import ca.uhn.fhir.jpa.dao.BaseHapiFhirResourceDao;
import ca.uhn.fhir.jpa.dao.JpaResourceDao;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.annotations.OnMatchboxOnlyOneEnginePresent;
import ca.uhn.fhir.jpa.starter.annotations.OnR5Condition;
import ca.uhn.fhir.jpa.validation.ValidatorPolicyAdvisor;
import ca.uhn.fhir.jpa.validation.ValidatorResourceFetcher;
import ch.ahdis.matchbox.config.MatchboxJpaConfig;
import ch.ahdis.matchbox.mappinglanguage.StructureMapListProvider;
import ch.ahdis.matchbox.packages.ImplementationGuideProviderR5;
import ch.ahdis.matchbox.providers.*;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResourceProvider;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import ch.ahdis.matchbox.validation.ValidationProvider;
import ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProviderR5;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProviderR5;
import ch.ahdis.matchbox.terminology.CodeSystemCodeValidationProvider;
import ch.ahdis.matchbox.terminology.ValueSetCodeValidationProvider;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.StructureMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Conditional(OnR5Condition.class)
@Import({
	MatchboxJpaConfig.class,
	JpaR5Config.class
})
public class FhirServerConfigR5 {
	/**
	 * We override the paging provider definition so that we can customize the
	 * default/max page sizes for search results. You can set these however you
	 * want, although very large page sizes will require a lot of RAM.
	 */
	@Autowired
	AppProperties appProperties;

	@Autowired
	FhirContext fhirContext;

	@Autowired
	private ApplicationContext context;

	@Bean
	public ValidationProvider validationProvider() {
		return new ValidationProvider();
	}

	@Bean
	public QuestionnaireAssembleProviderR5 assembleProvider() {
		return new QuestionnaireAssembleProviderR5();
	}

	@Bean
	public QuestionnaireResponseExtractProviderR5 questionnaireResponseProvider(final MatchboxEngineSupport matchboxEngineSupport) {
		return new QuestionnaireResponseExtractProviderR5(matchboxEngineSupport);
	}

	@Bean(name = "myImplementationGuideDaoR5")
	public IFhirResourceDao<ImplementationGuide> daoImplementationGuideR5() {
		BaseHapiFhirResourceDao<ImplementationGuide> retVal;
		retVal = new JpaResourceDao<>();
		retVal.setResourceType(ImplementationGuide.class);
		retVal.setContext(fhirContext);
		return retVal;
	}

	@Bean(name = "myImplementationGuideRpR5")
	@Primary
	public ImplementationGuideProviderR5 rpImplementationGuideR5() {
		ImplementationGuideProviderR5 retVal = new ImplementationGuideProviderR5();
		retVal.setContext(fhirContext);
//    retVal.setDao(daoImplementationGuideR5());
		return retVal;
	}

	@Bean(name = "myQuestionnaireRpR5")
	@Primary
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	public QuestionnaireResourceProvider rpQuestionnaireR5() {
		QuestionnaireResourceProvider retVal;
		retVal = new  QuestionnaireResourceProvider();
		return retVal;
	}

	@Bean(name = "myValueSetRpR5")
	@Primary
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	public ValueSetResourceProvider rpValueSetR5() {
		ValueSetResourceProvider retVal = new ValueSetResourceProvider();
		return retVal;
	}

	@Bean(name = "myCodeSystemRpR5")
	@Primary
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	public CodeSystemResourceProvider rpCodeSystem4() {
		CodeSystemResourceProvider retVal = new CodeSystemResourceProvider();
		return retVal;
	}

	@Bean(name = "myConceptMapRpR5")
	@Primary
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	public ConceptMapResourceProvider rpConceptMap4() {
		ConceptMapResourceProvider retVal = new ConceptMapResourceProvider();
		return retVal;
	}

	@Bean(name = "myStructureDefintionRpR5")
	@Primary
	public StructureDefinitionResourceProvider rpStructureDefintion4() {
		StructureDefinitionResourceProvider retVal = new StructureDefinitionResourceProvider();
		return retVal;
	}

	@Bean(name = "myStructureMapDaoR5")
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	public IFhirResourceDao<StructureMap> daoStructureMapR5() {

		BaseHapiFhirResourceDao<StructureMap> retVal;
		retVal = new JpaResourceDao<>();
		retVal.setResourceType(StructureMap.class);
		retVal.setContext(fhirContext);
		return retVal;
	}

	@Bean(name = "myStructureMapRpR5")
	@Primary
	@Conditional(OnMatchboxOnlyOneEnginePresent.class)
	public StructureMapTransformProvider rpStructureMapR5() {
		StructureMapTransformProvider retVal;
		retVal = new StructureMapTransformProvider();
//    retVal.setContext(fhirContext);
//    retVal.setDao(daoStructureMapR5());
		return retVal;
	}

  @Bean
  public StructureMapListProvider structureMapListProvider(final MatchboxEngineSupport matchboxEngineSupport) {
    return new StructureMapListProvider(matchboxEngineSupport);
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
	public CodeSystemCodeValidationProvider codeSystemCodeValidationProvider(final FhirContext fhirContext) {
		return new CodeSystemCodeValidationProvider(fhirContext);
	}

	@Bean
	public ValueSetCodeValidationProvider valueSetCodeValidationProvider(final FhirContext fhirContext) {
		return new ValueSetCodeValidationProvider(fhirContext);
	}
}
