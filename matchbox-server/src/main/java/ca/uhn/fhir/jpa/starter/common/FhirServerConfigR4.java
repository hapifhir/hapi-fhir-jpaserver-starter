package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.dao.data.IStatisticsDao;
import ca.uhn.fhir.jpa.rp.r4.OperationOutcomeResourceProvider;
import ch.ahdis.matchbox.packages.ImplementationGuideProviderR4;
import ch.ahdis.matchbox.statistics.StatisticsObservationProvider;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import ch.ahdis.matchbox.validation.ValidationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.config.r4.JpaR4Config;
import ca.uhn.fhir.jpa.dao.JpaResourceDao;
import ca.uhn.fhir.jpa.starter.annotations.OnMatchboxOnlyOneEnginePresent;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ch.ahdis.matchbox.config.MatchboxJpaConfig;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProviderR4;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProviderR4;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.context.annotation.*;

@Configuration
@Conditional(OnR4Condition.class)
@Import({MatchboxJpaConfig.class, JpaR4Config.class})
public class FhirServerConfigR4 {

  private final FhirContext fhirContext;

  public FhirServerConfigR4(final FhirContext fhirContext) {
    this.fhirContext = fhirContext;
  }

  @Bean
  public ValidationProvider validationProvider() {
    return new ValidationProvider();
  }

  @Bean
  public StatisticsObservationProvider statisticsObservationProvider() {
    return new StatisticsObservationProvider(null);
  }

  @Bean
  public QuestionnaireAssembleProviderR4 assembleProvider() {
    return new QuestionnaireAssembleProviderR4();
  }

  @Bean
  public QuestionnaireResponseExtractProviderR4 questionnaireResponseProvider(final MatchboxEngineSupport matchboxEngineSupport) {
    return new QuestionnaireResponseExtractProviderR4(matchboxEngineSupport);
  }

  @Bean(name = "myImplementationGuideDaoR4")
  public IFhirResourceDao<ImplementationGuide> daoImplementationGuideR4() {
    final var retVal = new JpaResourceDao<ImplementationGuide>();
    retVal.setResourceType(ImplementationGuide.class);
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean(name = "myOperationOutcomeDaoR4")
  public IFhirResourceDao<OperationOutcome> daoOperationOutcomeR4() {
    final var retVal = new JpaResourceDao<OperationOutcome>();
    retVal.setResourceType(OperationOutcome.class);
    retVal.setContext(fhirContext);
    return retVal;
  }
  
  @Bean(name = "myOperationOutcomeRpR4")
  @Primary
  public OperationOutcomeResourceProvider rpOperationOutcomeR4() {
    final var retVal = new OperationOutcomeResourceProvider();
    retVal.setContext(fhirContext);
    retVal.setDao(daoOperationOutcomeR4());
    return retVal;
  }


  @Bean(name = "myImplementationGuideRpR4")
  @Primary
  public ImplementationGuideProviderR4 rpImplementationGuideR4() {
    final var retVal = new ImplementationGuideProviderR4();
    retVal.setContext(fhirContext);
//     retVal.setDao(daoImplementationGuideR4());
    return retVal;
  }


  @Bean(name = "myStructureMapDaoR4")
  @Conditional(OnMatchboxOnlyOneEnginePresent.class)
  public IFhirResourceDao<StructureMap> daoStructureMapR4() {
    final var retVal = new JpaResourceDao<StructureMap>();
    retVal.setResourceType(StructureMap.class);
    retVal.setContext(fhirContext);
    return retVal;
  }
}
