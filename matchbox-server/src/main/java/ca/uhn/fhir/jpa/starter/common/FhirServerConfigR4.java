package ca.uhn.fhir.jpa.starter.common;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import ca.uhn.fhir.batch2.jobs.reindex.ReindexAppCtx;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.batch2.JpaBatch2Config;
import ca.uhn.fhir.jpa.config.r4.JpaR4Config;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.jpa.starter.cql.StarterCqlR4Config;
import ca.uhn.fhir.jpa.validation.ValidatorPolicyAdvisor;
import ca.uhn.fhir.jpa.validation.ValidatorResourceFetcher;
import ch.ahdis.fhir.hapi.jpa.validation.ValidationProvider;
import ch.ahdis.matchbox.CodeSystemResourceProvider;
import ch.ahdis.matchbox.ConceptMapResourceProvider;
import ch.ahdis.matchbox.MatchboxJpaConfig;
import ch.ahdis.matchbox.QuestionnaireResourceProvider;
import ch.ahdis.matchbox.StructureDefinitionResourceProvider;
import ch.ahdis.matchbox.ValueSetResourceProvider;
import ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProvider;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;

@Configuration
@Conditional(OnR4Condition.class)
@Import({ MatchboxJpaConfig.class, JpaR4Config.class})
public class FhirServerConfigR4 {

  /**
   * We override the paging provider definition so that we can customize the
   * default/max page sizes for search results. You can set these however you
   * want, although very large page sizes will require a lot of RAM.
   */
  @Autowired
  AppProperties appProperties;

  @Autowired
  FhirContext fhirContext;


  
//  @Bean
//  public ITermCodeSystemStorageSvc termCodeSystemStorageSvc() {
//    return new NullTermCodeSystemStorageSvcImpl();
//  }
  
//  @Bean
//  public ITermConceptMappingSvc termConceptMappingSvc() {
//    return new NullTermConceptMappingSvcImpl();
//  }

  @Bean(autowire = Autowire.BY_TYPE)
  public ValidationProvider validationProvider() {
    return new ValidationProvider();
  }


  @Bean
  public QuestionnaireAssembleProvider assembleProvider() {
    return new QuestionnaireAssembleProvider();
  }

  @Bean
  public QuestionnaireResponseExtractProvider questionnaireResponseProvider() {
    return new QuestionnaireResponseExtractProvider();
  }

  @Bean(name = "myImplementationGuideDaoR4")
  public IFhirResourceDao<org.hl7.fhir.r4.model.ImplementationGuide> daoImplementationGuideR4() {

    ca.uhn.fhir.jpa.dao.BaseHapiFhirResourceDao<org.hl7.fhir.r4.model.ImplementationGuide> retVal;
    retVal = new ca.uhn.fhir.jpa.dao.JpaResourceDao<org.hl7.fhir.r4.model.ImplementationGuide>();
    retVal.setResourceType(org.hl7.fhir.r4.model.ImplementationGuide.class);
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean(name = "myImplementationGuideRpR4")
  @Primary
  public ca.uhn.fhir.jpa.rp.r4.ImplementationGuideResourceProvider rpImplementationGuideR4() {
    ca.uhn.fhir.jpa.rp.r4.ImplementationGuideResourceProvider retVal;
    retVal = new ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProvider();
    retVal.setContext(fhirContext);
//    retVal.setDao(daoImplementationGuideR4());
    return retVal;
  }
  
  @Bean(name = "myQuestionnaireRpR4")
  @Primary
  public QuestionnaireResourceProvider rpQuestionnaireR4() {
    QuestionnaireResourceProvider retVal;
    retVal = new  ch.ahdis.matchbox.QuestionnaireResourceProvider();
    retVal.setContext(fhirContext);
//    retVal.setDao(daoImplementationGuideR4());
    return retVal;
  }
  
  @Bean(name = "myValueSetRpR4")
  @Primary
  public ch.ahdis.matchbox.ValueSetResourceProvider rpValueSetR4() {
  	ValueSetResourceProvider retVal = new ch.ahdis.matchbox.ValueSetResourceProvider();
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean(name = "myCodeSystemRpR4")
  @Primary
  public ch.ahdis.matchbox.CodeSystemResourceProvider rpCodeSystem4() {
  	CodeSystemResourceProvider retVal = new ch.ahdis.matchbox.CodeSystemResourceProvider();
    retVal.setContext(fhirContext);
    return retVal;
  }
  
  @Bean(name = "myConceptMapRpR4")
  @Primary
  public ch.ahdis.matchbox.ConceptMapResourceProvider rpConceptMap4() {
  	ConceptMapResourceProvider retVal = new ch.ahdis.matchbox.ConceptMapResourceProvider();
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean(name = "myStructureDefintionRpR4")
  @Primary
  public StructureDefinitionResourceProvider rpStructureDefintion4() {
  	StructureDefinitionResourceProvider retVal = new StructureDefinitionResourceProvider();
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean(name = "myStructureMapDaoR4")
  public IFhirResourceDao<org.hl7.fhir.r4.model.StructureMap> daoStructureMapR4() {

    ca.uhn.fhir.jpa.dao.BaseHapiFhirResourceDao<org.hl7.fhir.r4.model.StructureMap> retVal;
    retVal = new ca.uhn.fhir.jpa.dao.JpaResourceDao<org.hl7.fhir.r4.model.StructureMap>();
    retVal.setResourceType(org.hl7.fhir.r4.model.StructureMap.class);
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean(name = "myStructureMapRpR4")
  @Primary
  public ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider rpStructureMapR4() {
  	StructureMapTransformProvider retVal;
    retVal = new StructureMapTransformProvider();
    retVal.setContext(fhirContext);
//    retVal.setDao(daoStructureMapR4());
    return retVal;
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
  


}
