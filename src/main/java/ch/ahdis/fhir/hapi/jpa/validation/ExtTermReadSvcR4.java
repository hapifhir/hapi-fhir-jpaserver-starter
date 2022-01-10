package ch.ahdis.fhir.hapi.jpa.validation;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.jpa.api.dao.IDao;
import ca.uhn.fhir.jpa.term.TermReadSvcR4;
import ca.uhn.fhir.util.CoverageIgnore;
import ca.uhn.fhir.util.FhirVersionIndependentConcept;

public class ExtTermReadSvcR4 extends TermReadSvcR4 {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExtTermReadSvcR4.class);
  
  @Autowired
  private PlatformTransactionManager myTransactionManager;


  @Override
	public synchronized void preExpandDeferredValueSetsToTerminologyTables() {
      ourLog.info("Skipping scheduled pre-expansion to rely on InMemoryTerminologyServerValidationSupport. Be aware: codes in expanded ValueSets are not validated against a terminology server");
  }
  
  @CoverageIgnore
  @Override
  public IValidationSupport.CodeValidationResult validateCode(ValidationSupportContext theValidationSupportContext, ConceptValidationOptions theOptions, String theCodeSystem, String theCode, String theDisplay, String theValueSetUrl) {

    if (isNotBlank(theValueSetUrl)) {
      return validateCodeInValueSet(theValidationSupportContext, theOptions, theValueSetUrl, theCodeSystem, theCode, theDisplay);
    }

    TransactionTemplate txTemplate = new TransactionTemplate(myTransactionManager);
    txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    Optional<FhirVersionIndependentConcept> codeOpt = txTemplate.execute(t -> findCode(theCodeSystem, theCode).map(c -> new FhirVersionIndependentConcept(theCodeSystem, c.getCode())));

    if (codeOpt != null && codeOpt.isPresent()) {
      FhirVersionIndependentConcept code = codeOpt.get();
      if (!theOptions.isValidateDisplay() || (isNotBlank(code.getDisplay()) && isNotBlank(theDisplay) && code.getDisplay().equals(theDisplay))) {
        return new CodeValidationResult()
          .setCode(code.getCode())
          .setDisplay(code.getDisplay());
      } else {
        return createFailureCodeValidationResult(theCodeSystem, theCode, code.getSystemVersion(), " - Concept Display \"" + code.getDisplay() + "\" does not match expected \"" + code.getDisplay() + "\"").setDisplay(code.getDisplay());
      }
    }

    return createFailureCodeValidationResult(theCodeSystem, theCode, null, " - Code can not be found in CodeSystem");
  }

  IValidationSupport.CodeValidationResult validateCodeInValueSet(ValidationSupportContext theValidationSupportContext, ConceptValidationOptions theValidationOptions, String theValueSetUrl, String theCodeSystem, String theCode, String theDisplay) {
    IBaseResource valueSet = theValidationSupportContext.getRootValidationSupport().fetchValueSet(theValueSetUrl);

    // If we don't have a PID, this came from some source other than the JPA
    // database, so we don't need to check if it's pre-expanded or not
    if (valueSet instanceof IAnyResource) {
      Long pid = IDao.RESOURCE_PID.get((IAnyResource) valueSet);
      if (pid != null) {
        if (isValueSetPreExpandedForCodeValidation(valueSet)) {
          return validateCodeIsInPreExpandedValueSet(theValidationOptions, valueSet, theCodeSystem, theCode, theDisplay, null, null);
        }
      }
    }

    CodeValidationResult retVal;
    if (valueSet != null) {
      retVal = new ExtInMemoryTerminologyServerValidationSupport(myContext).validateCodeInValueSet(theValidationSupportContext, theValidationOptions, theCodeSystem, theCode, theDisplay, valueSet);
    } else {
      String append = " - Unable to locate ValueSet[" + theValueSetUrl + "]";
      retVal = createFailureCodeValidationResult(theCodeSystem, theCode, null, append);
    }

    return retVal;

  }
  
  private CodeValidationResult createFailureCodeValidationResult(String theSystem, String theCode, String theCodeSystemVersion, String theAppend) {
    return new CodeValidationResult()
      .setSeverity(IssueSeverity.ERROR)
      .setCodeSystemVersion(theCodeSystemVersion)
      .setMessage("Unable to validate code " + theSystem + "#" + theCode + theAppend);
  }


}
