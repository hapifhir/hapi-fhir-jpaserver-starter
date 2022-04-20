package ca.uhn.fhir.jpa.starter;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.hl7.fhir.common.hapi.validation.support.UnknownCodeSystemWarningValidationSupport;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r5.utils.validation.constants.BestPracticeWarningLevel;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;

import ca.uhn.fhir.batch2.jobs.reindex.ReindexAppCtx;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.batch2.JpaBatch2Config;
import ca.uhn.fhir.jpa.config.JpaConfig;
import ca.uhn.fhir.jpa.config.r4.JpaR4Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.jpa.starter.cql.StarterCqlR4Config;
import ca.uhn.fhir.jpa.term.api.ITermReadSvcR4;
import ca.uhn.fhir.jpa.validation.ValidatorPolicyAdvisor;
import ca.uhn.fhir.jpa.validation.ValidatorResourceFetcher;
import ca.uhn.fhir.validation.IInstanceValidatorModule;
import ch.ahdis.fhir.hapi.jpa.validation.CachingValidationSupport;
import ch.ahdis.fhir.hapi.jpa.validation.ExtTermReadSvcR4;
import ch.ahdis.fhir.hapi.jpa.validation.ExtUnknownCodeSystemWarningValidationSupport;
import ch.ahdis.fhir.hapi.jpa.validation.JpaExtendedValidationSupportChain;
import ch.ahdis.fhir.hapi.jpa.validation.ValidationProvider;
import ch.ahdis.matchbox.mappinglanguage.ConvertingWorkerContext;
import ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnairePopulateProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProvider;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;

@Configuration
@Conditional(OnR4Condition.class)
@Import({ StarterJpaConfig.class, JpaR4Config.class, StarterCqlR4Config.class, ElasticsearchConfig.class, ReindexAppCtx.class, JpaBatch2Config.class })
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

  @PostConstruct
  public void initSettings() {
    // FIXME OE 2022_600_notnecessaryanymore?
//    if(appProperties.getSearch_coord_core_pool_size() != null) {
//		 setSearchCoordCorePoolSize(appProperties.getSearch_coord_core_pool_size());
//	 }
//	  if(appProperties.getSearch_coord_max_pool_size() != null) {
//		  setSearchCoordMaxPoolSize(appProperties.getSearch_coord_max_pool_size());
//	  }
//	  if(appProperties.getSearch_coord_queue_capacity() != null) {
//		  setSearchCoordQueueCapacity(appProperties.getSearch_coord_queue_capacity());
//	  }
  }

  @Autowired
  private ConfigurableEnvironment configurableEnvironment;

  // FIXME OE 2022_600_notnecessaryanymore? @Override
  @Bean(autowire = Autowire.BY_TYPE)
  public ITermReadSvcR4 terminologyService() {
    return new ExtTermReadSvcR4();
  }

  @Bean(autowire = Autowire.BY_TYPE)
  public ValidationProvider validationProvider() {
    return new ValidationProvider();
  }

  @Bean
  public UnknownCodeSystemWarningValidationSupport unknownCodeSystemWarningValidationSupport() {
    return new ExtUnknownCodeSystemWarningValidationSupport(fhirContext);
  }

  @Bean
  public QuestionnairePopulateProvider questionnaireProvider() {
    return new QuestionnairePopulateProvider();
  }

  @Bean
  public QuestionnaireAssembleProvider assembleProvider() {
    return new QuestionnaireAssembleProvider();
  }

  @Bean
  public QuestionnaireResponseExtractProvider questionnaireResponseProvider() {
    return new QuestionnaireResponseExtractProvider();
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
  public ca.uhn.fhir.jpa.rp.r4.StructureMapResourceProvider rpStructureMapR4() {
    ca.uhn.fhir.jpa.rp.r4.StructureMapResourceProvider retVal;
    retVal = new StructureMapTransformProvider();
    retVal.setContext(fhirContext);
    retVal.setDao(daoStructureMapR4());
    return retVal;
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
    retVal.setDao(daoImplementationGuideR4());
    return retVal;
  }

  @Bean
  public ConvertingWorkerContext simpleWorkerContext() {
    try {
      ConvertingWorkerContext conv = new ConvertingWorkerContext(this.validationSupportChain());
      return conv;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // FIXME OE 2022_600_notnecessaryanymore see ValidationSupportConfig

  @Bean(name = JpaConfig.JPA_VALIDATION_SUPPORT_CHAIN)
  public JpaExtendedValidationSupportChain jpaValidationSupportChain() {
    return new JpaExtendedValidationSupportChain(fhirContext);
  }

  @Bean(name = "myInstanceValidator")
  public IInstanceValidatorModule instanceValidator() {
    FhirInstanceValidator val = new FhirInstanceValidator(validationSupportChain());
    val.setValidatorResourceFetcher(jpaValidatorResourceFetcher());
    val.setValidatorPolicyAdvisor(jpaValidatorPolicyAdvisor());
    val.setBestPracticeWarningLevel(BestPracticeWarningLevel.Warning);
    return val;
  }
  
  @Bean
  public ValidatorResourceFetcher jpaValidatorResourceFetcher() {
    return new ValidatorResourceFetcher();
  }
  
  @Bean
  public ValidatorPolicyAdvisor jpaValidatorPolicyAdvisor() {
    return new ValidatorPolicyAdvisor();
  }

  public CachingValidationSupport validationSupportChain() {
    CachingValidationSupport.CacheTimeouts cacheTimeouts = CachingValidationSupport.CacheTimeouts.defaultValues()
        .setTranslateCodeMillis(1000).setMiscMillis(10000).setValidateCodeMillis(10000);
    return new CachingValidationSupport(jpaValidationSupportChain(), cacheTimeouts);
  }


  @Bean
  public MatchboxPackageInstallerImpl packageInstaller() {
    return new MatchboxPackageInstallerImpl();
  }
  


}
