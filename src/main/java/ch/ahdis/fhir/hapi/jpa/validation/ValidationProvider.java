package ch.ahdis.fhir.hapi.jpa.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * #%L
 * Matchbox Server
 * %%
 * Copyright (C) 2018 - 2020 ahdis
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.utils.IResourceValidator.BestPracticeWarningLevel;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementCompositeDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.packages.NpmJpaValidationSupport;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.util.ParametersUtil;
import ca.uhn.fhir.util.StopWatch;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;

/**
 * Operation $validate
 */
public class ValidationProvider {

  @Autowired
  protected IValidationSupport myValidationSupport;

  @Autowired
  protected FhirContext myFhirCtx;
  
  @Autowired
  protected NpmJpaValidationSupport npmJpaValidationSuport;

  @Autowired
  protected DefaultProfileValidationSupport defaultProfileValidationSuport;

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationProvider.class);

  @Operation(name = "$validate", manualRequest=true, idempotent = true, returnParameters = {
    @OperationParam(name = "return", type = IBase.class, min = 1, max = 1) })
    public IBaseResource validate(HttpServletRequest theRequest) {
    log.debug("$validate");
    ArrayList<SingleValidationMessage> addedValidationMessages = new ArrayList<>();
    
    StopWatch sw = new StopWatch();
    sw.startTask("Total");

    String profile = null;

    ValidationOptions validationOptions = new ValidationOptions();
    if (theRequest.getParameter("profile")!=null) {
      profile= theRequest.getParameter("profile");
    }
    
    if (profile != null) {
      if (npmJpaValidationSuport.fetchStructureDefinition(profile)==null && 
          defaultProfileValidationSuport.fetchStructureDefinition(profile) == null) {
        return getValidationMessageProfileNotSupported(profile);
      }
      validationOptions.addProfileIfNotBlank(profile);
    }

    byte[] bytes = null;
    String contentString = "";
    try {
      bytes = IOUtils.toByteArray(theRequest.getInputStream());
      contentString = new String(bytes);
    } catch (IOException e) {
    }
    // ValidationResult result = 
    
    if (contentString.length()==0) {
      SingleValidationMessage m = new SingleValidationMessage();
      m.setSeverity(ResultSeverityEnum.ERROR);
      m.setMessage("No resource provided in http body");
      m.setLocationCol(0);
      m.setLocationLine(0);
      addedValidationMessages.add(m);
      return new ValidationResultWithExtensions(myFhirCtx, addedValidationMessages).toOperationOutcome();
    }

    EncodingEnum encoding = EncodingEnum.forContentType(theRequest.getContentType());
    if (encoding == null) {
      encoding = EncodingEnum.detectEncoding(contentString);
    }
    
    FhirValidator validatorModule = myFhirCtx.newValidator();
    
    FhirInstanceValidator instanceValidator = new FhirInstanceValidator(myValidationSupport);
    instanceValidator.setBestPracticeWarningLevel(BestPracticeWarningLevel.Ignore);
    validatorModule.registerValidatorModule(instanceValidator);
    
    
    // the $validate operation can be called in different ways, see https://www.hl7.org/fhir/resource-operation-validate.html 
    // HTTP Body ---- HTTP Header Paramet 
    // Resource       profile = specified --> return OperationOutcome
    // Parameters     profile = specified --> return OperationOutcome
    // Resource       profile not specified --> return OperationOutcome
    // Parameters     profile not specified --> return Parameters/OperationOutcome

      IBaseResource resource = encoding.newParser(myFhirCtx).parseResource(contentString);
      ValidationResult result = null;
      if ("Parameters".equals(resource.fhirType()) && profile==null) {
        IBaseParameters parameters = (IBaseParameters) resource;
        IBaseResource resourceInParam = null;
        List<String> profiles = ParametersUtil.getNamedParameterValuesAsString(myFhirCtx, parameters, "profile");
        if (profiles!=null && profiles.size()==1) {
          profile = profiles.get(0);
        }
        List<IBase> paramChildElems = ParametersUtil.getNamedParameters(myFhirCtx, parameters, "resource");
        if (paramChildElems!=null && paramChildElems.size()==1) {
          IBase param = paramChildElems.get(0);
          BaseRuntimeElementCompositeDefinition<?> nextParameterDef = (BaseRuntimeElementCompositeDefinition<?>) myFhirCtx.getElementDefinition(param.getClass());
          BaseRuntimeChildDefinition nameChild = nextParameterDef.getChildByName("resource");
          List<IBase> resourceValues = nameChild.getAccessor().getValues(param);
          if (resourceValues!=null && resourceValues.size()==1) {
            resourceInParam = (IBaseResource) resourceValues.get(0);
          }
        }
        if (resourceInParam!=null) {
          validationOptions = new ValidationOptions();
          if (profile != null) {
            if (npmJpaValidationSuport.fetchStructureDefinition(profile)==null && 
                defaultProfileValidationSuport.fetchStructureDefinition(profile) == null) {
              return getValidationMessageProfileNotSupported(profile);
            }
            validationOptions.addProfileIfNotBlank(profile);
          }
          result = validatorModule.validateWithResult(resourceInParam, validationOptions);
          
          IBaseResource operationOutcome = getOperationOutcome(addedValidationMessages, sw, profile, result);
          
          IBaseParameters returnParameters = ParametersUtil.newInstance(myFhirCtx);
          ParametersUtil.addParameterToParameters(myFhirCtx, returnParameters, "return", operationOutcome);
          return returnParameters;
        }
      } else {
         result = validatorModule.validateWithResult(contentString, validationOptions);
      }
    return getOperationOutcome(addedValidationMessages, sw, profile, result);
  }

  private IBaseResource getOperationOutcome(ArrayList<SingleValidationMessage> addedValidationMessages, StopWatch sw,
      String profile, ValidationResult result) {
    sw.endCurrentTask();

    if (profile != null) {
      SingleValidationMessage m = new SingleValidationMessage();
      m.setSeverity(ResultSeverityEnum.INFORMATION);
      m.setMessage("Validation for profile "+ profile +" " + (result.getMessages().size()==0 ? "No Issues detected. " : "") + sw.formatTaskDurations());
      m.setLocationCol(0);
      m.setLocationLine(0);
      addedValidationMessages.add(m);
    }
    addedValidationMessages.addAll(result.getMessages());

    return new ValidationResultWithExtensions(myFhirCtx, addedValidationMessages).toOperationOutcome();
  }

  private IBaseResource getValidationMessageProfileNotSupported(String profile) {
    SingleValidationMessage m = new SingleValidationMessage();
    m.setSeverity(ResultSeverityEnum.ERROR);
    m.setMessage("Validation for profile "+ profile + " not supported by this server, but additional ig's could be configured.");
    m.setLocationCol(0);
    m.setLocationLine(0);
    ArrayList<SingleValidationMessage> newValidationMessages = new ArrayList<>();
    newValidationMessages.add(m);
    return (new ValidationResult(myFhirCtx, newValidationMessages)).toOperationOutcome();
  }

}
