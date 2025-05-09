package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ch.ahdis.matchbox.mappinglanguage.StructureMapListProvider;
import ch.ahdis.matchbox.providers.CodeSystemResourceProvider;
import ch.ahdis.matchbox.providers.ConceptMapResourceProvider;
import ch.ahdis.matchbox.packages.ImplementationGuideProviderR4B;
import ch.ahdis.matchbox.terminology.CodeSystemCodeValidationProvider;
import ch.ahdis.matchbox.terminology.ValueSetCodeValidationProvider;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.config.r4b.JpaR4BConfig;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.annotations.OnR4BCondition;
import ca.uhn.fhir.jpa.validation.ValidatorPolicyAdvisor;
import ca.uhn.fhir.jpa.validation.ValidatorResourceFetcher;
import ch.ahdis.matchbox.validation.ValidationProvider;
import ch.ahdis.matchbox.config.MatchboxJpaConfig;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResourceProvider;
import ch.ahdis.matchbox.providers.StructureDefinitionResourceProvider;
import ch.ahdis.matchbox.providers.ValueSetResourceProvider;
import ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProviderR4B;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProviderR4B;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Conditional(OnR4BCondition.class)
@Import({ MatchboxJpaConfig.class, JpaR4BConfig.class})
public class FhirServerConfigR4B {

  /**
   * We override the paging provider definition so that we can customize the
   * default/max page sizes for search results. You can set these however you
   * want, although very large page sizes will require a lot of RAM.
   */
  @Autowired
  AppProperties appProperties;

  @Autowired
  FhirContext fhirContext;


  @Bean
  public ValidationProvider validationProvider() {
    return new ValidationProvider();
  }


  @Bean
  public QuestionnaireAssembleProviderR4B assembleProvider() {
    return new QuestionnaireAssembleProviderR4B();
  }

  @Bean
  public QuestionnaireResponseExtractProviderR4B questionnaireResponseProvider(final MatchboxEngineSupport matchboxEngineSupport) {
    return new QuestionnaireResponseExtractProviderR4B(matchboxEngineSupport);
  }

  @Bean(name = "myImplementationGuideDaoR4B")
  public IFhirResourceDao<org.hl7.fhir.r4b.model.ImplementationGuide> daoImplementationGuideR4B() {

    ca.uhn.fhir.jpa.dao.BaseHapiFhirResourceDao<org.hl7.fhir.r4b.model.ImplementationGuide> retVal;
    retVal = new ca.uhn.fhir.jpa.dao.JpaResourceDao<org.hl7.fhir.r4b.model.ImplementationGuide>();
    retVal.setResourceType(org.hl7.fhir.r4b.model.ImplementationGuide.class);
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean(name = "myImplementationGuideRpR4V")
  @Primary
  public ImplementationGuideProviderR4B rpImplementationGuideR4() {
	  ImplementationGuideProviderR4B retVal = new ImplementationGuideProviderR4B();
	  retVal.setContext(fhirContext);
//     retVal.setDao(daoImplementationGuideR4());
     return retVal;
  }
  
  @Bean(name = "myQuestionnaireRpR4B")
  @Primary
  public QuestionnaireResourceProvider rpQuestionnaireR4B() {
    QuestionnaireResourceProvider retVal;
    retVal = new QuestionnaireResourceProvider();
    return retVal;
  }
  
  @Bean(name = "myValueSetRpR4B")
  @Primary
  public ValueSetResourceProvider rpValueSetR4B() {
  	ValueSetResourceProvider retVal = new ValueSetResourceProvider();
    return retVal;
  }

  @Bean(name = "myCodeSystemRpR4B")
  @Primary
  public CodeSystemResourceProvider rpCodeSystem4B() {
  	CodeSystemResourceProvider retVal = new CodeSystemResourceProvider();
    return retVal;
  }
  
  @Bean(name = "myConceptMapRpR4B")
  @Primary
  public ConceptMapResourceProvider rpConceptMap4B() {
  	ConceptMapResourceProvider retVal = new ConceptMapResourceProvider();
    return retVal;
  }

  @Bean(name = "myStructureDefintionRpR4B")
  @Primary
  public StructureDefinitionResourceProvider rpStructureDefintion4B() {
  	StructureDefinitionResourceProvider retVal = new StructureDefinitionResourceProvider();
    return retVal;
  }

  @Bean(name = "myStructureMapDaoR4B")
  public IFhirResourceDao<org.hl7.fhir.r4b.model.StructureMap> daoStructureMapR4() {

    ca.uhn.fhir.jpa.dao.BaseHapiFhirResourceDao<org.hl7.fhir.r4b.model.StructureMap> retVal;
    retVal = new ca.uhn.fhir.jpa.dao.JpaResourceDao<org.hl7.fhir.r4b.model.StructureMap>();
    retVal.setResourceType(org.hl7.fhir.r4b.model.StructureMap.class);
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean(name = "myStructureMapRpR4B")
  @Primary
  public ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider rpStructureMapR4B() {
  	StructureMapTransformProvider retVal;
    retVal = new StructureMapTransformProvider();
//    retVal.setContext(fhirContext);
//    retVal.setDao(daoStructureMapR4());
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
