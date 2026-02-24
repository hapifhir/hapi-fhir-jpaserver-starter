package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.config.r5.JpaR5Config;
import ca.uhn.fhir.jpa.dao.JpaResourceDao;
import ca.uhn.fhir.jpa.starter.annotations.OnMatchboxOnlyOneEnginePresent;
import ca.uhn.fhir.jpa.starter.annotations.OnR5Condition;
import ca.uhn.fhir.jpa.starter.annotations.OnStatisticsEnabled;
import ch.ahdis.matchbox.config.MatchboxJpaConfig;
import ch.ahdis.matchbox.packages.ImplementationGuideProviderR5;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProviderR5;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProviderR5;
import ch.ahdis.matchbox.statistics.OperationOutcomeResourceProviderR5;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.StructureMap;
import org.springframework.context.annotation.*;

@Configuration
@Conditional(OnR5Condition.class)
@Import({
  MatchboxJpaConfig.class,
  JpaR5Config.class
})
public class FhirServerConfigR5 {

  private final FhirContext fhirContext;

  public FhirServerConfigR5(final FhirContext fhirContext) {
    this.fhirContext = fhirContext;
  }

  @Bean
  public QuestionnaireAssembleProviderR5 assembleProvider() {
    return new QuestionnaireAssembleProviderR5();
  }

  @Bean
  public QuestionnaireResponseExtractProviderR5 questionnaireResponseProvider(final MatchboxEngineSupport matchboxEngineSupport) {
    return new QuestionnaireResponseExtractProviderR5(matchboxEngineSupport);
  }

  @Bean
  public IFhirResourceDao<ImplementationGuide> daoImplementationGuideR5() {
    final var retVal = new JpaResourceDao<ImplementationGuide>();
    retVal.setResourceType(ImplementationGuide.class);
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean
  @Primary
  public ImplementationGuideProviderR5 rpImplementationGuideR5() {
    final var retVal = new ImplementationGuideProviderR5();
    retVal.setContext(fhirContext);
//    retVal.setDao(daoImplementationGuideR5());
    return retVal;
  }

  @Bean
  @Conditional(OnMatchboxOnlyOneEnginePresent.class)
  public IFhirResourceDao<StructureMap> daoStructureMapR5() {
    final var retVal = new JpaResourceDao<StructureMap>();
    retVal.setResourceType(StructureMap.class);
    retVal.setContext(fhirContext);
    return retVal;
  }
  
  @Bean
  @Primary
  @Conditional(OnStatisticsEnabled.class)
  public OperationOutcomeResourceProviderR5 rpOperationOutcomeR5(final IFhirResourceDao<OperationOutcome> operationOutcomeDao,
                                                                 final FhirContext fhirContext) {
    final var retVal = new OperationOutcomeResourceProviderR5();
    retVal.setContext(fhirContext);
    retVal.setDao(operationOutcomeDao);
    return retVal;
  }
}
