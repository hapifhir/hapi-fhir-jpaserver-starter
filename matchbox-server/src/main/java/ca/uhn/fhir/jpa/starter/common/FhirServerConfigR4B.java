package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.config.r4b.JpaR4BConfig;
import ca.uhn.fhir.jpa.dao.JpaResourceDao;
import ca.uhn.fhir.jpa.starter.annotations.OnMatchboxOnlyOneEnginePresent;
import ca.uhn.fhir.jpa.starter.annotations.OnR4BCondition;
import ca.uhn.fhir.jpa.starter.annotations.OnStatisticsEnabled;
import ch.ahdis.matchbox.config.MatchboxJpaConfig;
import ch.ahdis.matchbox.packages.ImplementationGuideProviderR4B;
import ch.ahdis.matchbox.questionnaire.QuestionnaireAssembleProviderR4B;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProviderR4B;
import ch.ahdis.matchbox.statistics.OperationOutcomeResourceProviderR4B;
import ch.ahdis.matchbox.statistics.SearchParameterResourceProviderR4B;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import org.hl7.fhir.r4b.model.OperationOutcome;
import org.hl7.fhir.r4b.model.SearchParameter;
import org.hl7.fhir.r4b.model.ImplementationGuide;
import org.hl7.fhir.r4b.model.StructureMap;
import org.springframework.context.annotation.*;

@Configuration
@Conditional(OnR4BCondition.class)
@Import({MatchboxJpaConfig.class, JpaR4BConfig.class})
public class FhirServerConfigR4B {

  private final FhirContext fhirContext;

  public FhirServerConfigR4B(final FhirContext fhirContext) {
    this.fhirContext = fhirContext;
  }

  @Bean
  public QuestionnaireAssembleProviderR4B assembleProvider() {
    return new QuestionnaireAssembleProviderR4B();
  }

  @Bean
  public QuestionnaireResponseExtractProviderR4B questionnaireResponseProvider(final MatchboxEngineSupport matchboxEngineSupport) {
    return new QuestionnaireResponseExtractProviderR4B(matchboxEngineSupport);
  }

  @Bean
  public IFhirResourceDao<ImplementationGuide> daoImplementationGuideR4B() {
    final var retVal = new JpaResourceDao<ImplementationGuide>();
    retVal.setResourceType(ImplementationGuide.class);
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean
  @Primary
  public ImplementationGuideProviderR4B rpImplementationGuideR4B() {
    ImplementationGuideProviderR4B retVal = new ImplementationGuideProviderR4B();
    retVal.setContext(fhirContext);
//     retVal.setDao(daoImplementationGuideR4());
    return retVal;
  }

  @Bean
  @Conditional(OnMatchboxOnlyOneEnginePresent.class)
  public IFhirResourceDao<StructureMap> daoStructureMapR4B() {
    final var retVal = new JpaResourceDao<StructureMap>();
    retVal.setResourceType(StructureMap.class);
    retVal.setContext(fhirContext);
    return retVal;
  }

  @Bean
  @Primary
  @Conditional(OnStatisticsEnabled.class)
  public OperationOutcomeResourceProviderR4B rpOperationOutcomeR4B(final IFhirResourceDao<OperationOutcome> operationOutcomeDao,
                                                                   final FhirContext fhirContext) {
    final var retVal = new OperationOutcomeResourceProviderR4B();
    retVal.setContext(fhirContext);
    retVal.setDao(operationOutcomeDao);
    return retVal;
  }

  @Bean
  @Primary
  @Conditional(OnStatisticsEnabled.class)
  public SearchParameterResourceProviderR4B rpSearchParameterR4B(final IFhirResourceDao<SearchParameter> searchParameterDao,
                                                                 final FhirContext fhirContext) {
    final var retVal = new SearchParameterResourceProviderR4B();
    retVal.setContext(fhirContext);
    retVal.setDao(searchParameterDao);
    return retVal;
  }
}
