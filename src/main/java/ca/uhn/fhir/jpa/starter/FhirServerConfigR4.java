package ca.uhn.fhir.jpa.starter;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.UnknownCodeSystemWarningValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.config.BaseJavaConfigR4;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.jpa.starter.cql.StarterCqlR4Config;
import ca.uhn.fhir.jpa.term.api.ITermReadSvcR4;
import ca.uhn.fhir.jpa.validation.JpaValidationSupportChain;
import ch.ahdis.fhir.hapi.jpa.validation.ExtTermReadSvcR4;
import ch.ahdis.fhir.hapi.jpa.validation.ExtUnknownCodeSystemWarningValidationSupport;
import ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProvider;
import ch.ahdis.fhir.hapi.jpa.validation.JpaExtendedValidationSupportChain;
import ch.ahdis.fhir.hapi.jpa.validation.ValidationProvider;
import ch.ahdis.matchbox.mappinglanguage.ConvertingWorkerContext;
import ch.ahdis.matchbox.mappinglanguage.StructureMapTransformProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnairePopulateProvider;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProvider;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;

@Configuration
@Conditional(OnR4Condition.class)
@Import(StarterCqlR4Config.class)
public class FhirServerConfigR4 extends BaseJavaConfigR4 {

  @Autowired
  private DataSource myDataSource;

  /**
   * We override the paging provider definition so that we can customize
   * the default/max page sizes for search results. You can set these however
   * you want, although very large page sizes will require a lot of RAM.
   */
  @Autowired
  AppProperties appProperties;
  
  @Autowired
  FhirContext fhirContext;

  @PostConstruct
  public void initSettings() {
    if(appProperties.getSearch_coord_core_pool_size() != null) {
		 setSearchCoordCorePoolSize(appProperties.getSearch_coord_core_pool_size());
	 }
	  if(appProperties.getSearch_coord_max_pool_size() != null) {
		  setSearchCoordMaxPoolSize(appProperties.getSearch_coord_max_pool_size());
	  }
	  if(appProperties.getSearch_coord_queue_capacity() != null) {
		  setSearchCoordQueueCapacity(appProperties.getSearch_coord_queue_capacity());
	  }
  }

  @Override
  public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
    DatabaseBackedPagingProvider pagingProvider = super.databaseBackedPagingProvider();
    pagingProvider.setDefaultPageSize(appProperties.getDefault_page_size());
    pagingProvider.setMaximumPageSize(appProperties.getMax_page_size());
    return pagingProvider;
  }

  @Autowired
  private ConfigurableEnvironment configurableEnvironment;

  @Override
  @Bean()
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean retVal = super.entityManagerFactory();
    retVal.setPersistenceUnitName("HAPI_PU");

    try {
      retVal.setDataSource(myDataSource);
    } catch (Exception e) {
      throw new ConfigurationException("Could not set the data source due to a configuration issue", e);
    }

    retVal.setJpaProperties(EnvironmentHelper.getHibernateProperties(configurableEnvironment));
    return retVal;
  }

  @Bean
  @Primary
  public JpaTransactionManager hapiTransactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager retVal = new JpaTransactionManager();
        retVal.setEntityManagerFactory(entityManagerFactory);
        return retVal;
    }
  
  @Override
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
		return new ExtUnknownCodeSystemWarningValidationSupport(fhirContext());
	}

  
  @Bean
  public QuestionnairePopulateProvider questionnaireProvider() {
	  return new QuestionnairePopulateProvider();
  }

  @Bean
  public QuestionnaireResponseExtractProvider questionnaireResponseProvider() {
	  return new QuestionnaireResponseExtractProvider();
  }


  @Override
  public ca.uhn.fhir.jpa.rp.r4.StructureMapResourceProvider rpStructureMapR4() {
    ca.uhn.fhir.jpa.rp.r4.StructureMapResourceProvider retVal;
    retVal = new StructureMapTransformProvider();
    retVal.setContext(fhirContextR4());
    retVal.setDao(daoStructureMapR4());
    return retVal;
  }

  @Override
  public ca.uhn.fhir.jpa.rp.r4.ImplementationGuideResourceProvider rpImplementationGuideR4() {
    ca.uhn.fhir.jpa.rp.r4.ImplementationGuideResourceProvider retVal;
    retVal = new ch.ahdis.fhir.hapi.jpa.validation.ImplementationGuideProvider();
    retVal.setContext(fhirContextR4());
    retVal.setDao(daoImplementationGuideR4());
    return retVal;
  }

//  @Bean
//  public IInstanceValidatorModule instanceValidator() {
//	  return new FhirInstanceValidator();
//  }

  
  @Bean
  public ConvertingWorkerContext simpleWorkerContext() {
    try {
		  ConvertingWorkerContext conv = new ConvertingWorkerContext(this.jpaValidationSupportChain().getValidationSupport());
		  return conv;
	  } catch (IOException e) {
		  throw new RuntimeException(e);
	  }
  }

  @Bean()
  public ElasticsearchSvcImpl elasticsearchSvc() {
    if (EnvironmentHelper.isElasticsearchEnabled(configurableEnvironment)) {
		 String elasticsearchUrl = EnvironmentHelper.getElasticsearchServerUrl(configurableEnvironment);
		 String elasticsearchHost;
		 if (elasticsearchUrl.startsWith("http")) {
			 elasticsearchHost = elasticsearchUrl.substring(elasticsearchUrl.indexOf("://") + 3, elasticsearchUrl.lastIndexOf(":"));
		 } else {
			 elasticsearchHost = elasticsearchUrl.substring(0, elasticsearchUrl.indexOf(":"));
		 }

      String elasticsearchUsername = EnvironmentHelper.getElasticsearchServerUsername(configurableEnvironment);
      String elasticsearchPassword = EnvironmentHelper.getElasticsearchServerPassword(configurableEnvironment);
      int elasticsearchPort = Integer.parseInt(elasticsearchUrl.substring(elasticsearchUrl.lastIndexOf(":")+1));
      return new ElasticsearchSvcImpl(elasticsearchHost, elasticsearchPort, elasticsearchUsername, elasticsearchPassword);
    } else {
      return null;
    }
  }

  @Bean(name = JPA_VALIDATION_SUPPORT_CHAIN)
	public JpaExtendedValidationSupportChain jpaValidationSupportChain() {
		return new JpaExtendedValidationSupportChain(fhirContext());
	}
  
  @Bean
  public MatchboxPackageInstallerImpl packageInstaller() {
	  return new MatchboxPackageInstallerImpl();
  }
}


