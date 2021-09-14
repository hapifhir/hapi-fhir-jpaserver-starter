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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.validation.JpaValidationSupportChain;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.util.ParametersUtil;
import ca.uhn.fhir.util.StopWatch;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IInstanceValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;

/**
 * Operation $validate
 */
public class ValidationProvider {

  @Autowired
  protected IInstanceValidatorModule instanceValidator;
  
  @Autowired
  protected JpaValidationSupportChain validationSupportChain;

  @Autowired
  protected FhirContext myFhirCtx;

  @Autowired
  protected DefaultProfileValidationSupport defaultProfileValidationSuport;

	@Autowired
	@Qualifier("myJpaValidationSupport")
	protected IValidationSupport myJpaValidationSupport;
	
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationProvider.class);

  @Operation(name = "$validate", manualRequest = true, idempotent = true, returnParameters = {
      @OperationParam(name = "return", type = IBase.class, min = 1, max = 1) })
  public IBaseResource validate(HttpServletRequest theRequest) {
    log.info("$validate");
    ArrayList<SingleValidationMessage> addedValidationMessages = new ArrayList<>();

    StopWatch sw = new StopWatch();
    sw.startTask("Total");

    String profile = null;

    ValidationOptions validationOptions = new ValidationOptions();
    if (theRequest.getParameter("profile") != null) {
      profile = theRequest.getParameter("profile");
    }

    if (profile != null) {
      if (myJpaValidationSupport.fetchStructureDefinition(profile) == null
          && defaultProfileValidationSuport.fetchStructureDefinition(profile) == null) {
        return getValidationMessageProfileNotSupported(profile);
      }
      validationOptions.addProfileIfNotBlank(profile);
    }

    byte[] bytes = null;
    String contentString = "";
    try {
      bytes = IOUtils.toByteArray(theRequest.getInputStream());
      if (bytes.length > 2 && bytes[0] == -17 && bytes[1] == -69 && bytes[2] == -65) {
        byte[] dest = new byte[bytes.length - 3];
        System.arraycopy(bytes, 3, dest, 0, bytes.length - 3);
        bytes = dest;
        SingleValidationMessage m = new SingleValidationMessage();
        m.setSeverity(ResultSeverityEnum.WARNING);
        m.setMessage(
            "Resource content has a UTF-8 BOM marking, skipping BOM, see https://en.wikipedia.org/wiki/Byte_order_mark");
        m.setLocationCol(0);
        m.setLocationLine(0);
        addedValidationMessages.add(m);
      }
      contentString = new String(bytes);
    } catch (IOException e) {
    }

    if (contentString.length() == 0) {
      SingleValidationMessage m = new SingleValidationMessage();
      m.setSeverity(ResultSeverityEnum.ERROR);
      m.setMessage("No resource provided in http body");
      m.setLocationCol(0);
      m.setLocationLine(0);
      addedValidationMessages.add(m);
      return new ValidationResultWithExtensions(myFhirCtx, addedValidationMessages).toOperationOutcome();
    } else {
      log.info(contentString);
    }

    String sha3Hex = new DigestUtils("SHA3-256").digestAsHex(contentString + (profile != null ? profile : ""));

    EncodingEnum encoding = EncodingEnum.forContentType(theRequest.getContentType());
    if (encoding == null) {
      encoding = EncodingEnum.detectEncoding(contentString);
    }
    
    FhirValidator validator = myFhirCtx.newValidator();
    validator.registerValidatorModule(instanceValidator);

    // the $validate operation can be called in different ways, see
    // https://www.hl7.org/fhir/resource-operation-validate.html
    // HTTP Body ---- HTTP Header Parameter
    // Resource profile = specified --> return OperationOutcome
    // Parameters profile = specified --> return OperationOutcome
    // Resource profile not specified --> return OperationOutcome
    // Parameters profile not specified --> return Parameters/OperationOutcome
    ValidationResult result = null;
    IBaseResource resource = null;
    try {
      // we still parse to catch wrongli formatted 
      resource = encoding.newParser(myFhirCtx).parseResource(contentString);
    } catch (DataFormatException e) {
      return getValidationMessageDataFormatException(e);
    }
    if (resource!=null && "Parameters".equals(resource.fhirType()) && profile == null) {
//      IBaseParameters parameters = (IBaseParameters) resource;
// https://github.com/ahdis/matchbox/issues/11
      Parameters parameters = (Parameters) resource;
      IBaseResource resourceInParam = null;
      for (ParametersParameterComponent compoment: parameters.getParameter()) {
        if ("resource".equals(compoment.getName())) {
          resourceInParam = compoment.getResource();
          break;
        }
      }
      if (resourceInParam!=null && resourceInParam.fhirType().contentEquals("Bundle") ) {
         Bundle bundle = (Bundle) resourceInParam;
         for (BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource()!=null && entry.getResource().getId()==null) {
              if (entry.getFullUrl()!=null && entry.getFullUrl().startsWith("urn:uuid:")) {
                entry.setId(entry.getFullUrl().substring(9));
              }
            }
           
         }
      }
      List<String> profiles = ParametersUtil.getNamedParameterValuesAsString(myFhirCtx, parameters, "profile");
      if (profiles != null && profiles.size() == 1) {
        profile = profiles.get(0);
      }
//      List<IBase> paramChildElems = ParametersUtil.getNamedParameters(myFhirCtx, parameters, "resource");
//      if (paramChildElems != null && paramChildElems.size() == 1) {
//        IBase param = paramChildElems.get(0);
//        BaseRuntimeElementCompositeDefinition<?> nextParameterDef = (BaseRuntimeElementCompositeDefinition<?>) myFhirCtx
//            .getElementDefinition(param.getClass());
//        BaseRuntimeChildDefinition nameChild = nextParameterDef.getChildByName("resource");
//        List<IBase> resourceValues = nameChild.getAccessor().getValues(param);
//        if (resourceValues != null && resourceValues.size() == 1) {
//          resourceInParam = (IBaseResource) resourceValues.get(0);
//          // we get dom-3: 'If the resource is contained in another resource, it SHALL be referred to from elsewhere in the resource or SHALL refer to the containing resource' 'If the resource is contained in another resource, it SHALL be referred to from elsewhere in the resource or SHALL refer to the containing resource' ( (unmatched: 2))
//          // there seems to be backpointer to the Parmeterresource
//          String serialized = encoding.newParser(myFhirCtx).encodeResourceToString(resourceInParam);
//          resourceInParam = encoding.newParser(myFhirCtx).parseResource(serialized);
//          log.info(serialized);
//        }
//      }
      if (resourceInParam != null) {
        validationOptions = new ValidationOptions();
        if (profile != null) {
          if (myJpaValidationSupport.fetchStructureDefinition(profile) == null
              && defaultProfileValidationSuport.fetchStructureDefinition(profile) == null) {
            return getValidationMessageProfileNotSupported(profile);
          }
          validationOptions.addProfileIfNotBlank(profile);
        }
        result = validator.validateWithResult(resourceInParam, validationOptions);
        IBaseResource operationOutcome = getOperationOutcome(sha3Hex, addedValidationMessages, sw, profile, result);
        IBaseParameters returnParameters = ParametersUtil.newInstance(myFhirCtx);
        ParametersUtil.addParameterToParameters(myFhirCtx, returnParameters, "return", operationOutcome);
        return returnParameters;
      } else {
        // we have a validation for a Parameter but not a resource inside, fall back to validate only Parameter
        result = validator.validateWithResult(contentString, validationOptions);
      }
    } else {
      result = validator.validateWithResult(contentString, validationOptions);
    }
    return getOperationOutcome(sha3Hex, addedValidationMessages, sw, profile, result);
  }

  private IBaseResource getOperationOutcome(String id, ArrayList<SingleValidationMessage> addedValidationMessages,
      StopWatch sw, String profile, ValidationResult result) {
    sw.endCurrentTask();
    
    log.info("Validation time: "+sw.toString());

    if (profile != null) {
      SingleValidationMessage m = new SingleValidationMessage();
      m.setSeverity(ResultSeverityEnum.INFORMATION);
      m.setMessage("Validation for profile " + profile + " "
          + (result.getMessages().size() == 0 ? "No Issues detected. " : "") + sw.formatTaskDurations());
      m.setLocationCol(0);
      m.setLocationLine(0);
      addedValidationMessages.add(m);
    }
    addedValidationMessages.addAll(result.getMessages());

    IBaseResource operationOutcome = new ValidationResultWithExtensions(myFhirCtx, addedValidationMessages)
        .toOperationOutcome();
    operationOutcome.setId(id);
    
    log.info(this.myFhirCtx.newXmlParser().encodeResourceToString(operationOutcome));

    return operationOutcome;
  }

  private IBaseResource getValidationMessageProfileNotSupported(String profile) {
    SingleValidationMessage m = new SingleValidationMessage();
    m.setSeverity(ResultSeverityEnum.ERROR);
    m.setMessage("Validation for profile " + profile
        + " not supported by this server, but additional ig's could be configured.");
    m.setLocationCol(0);
    m.setLocationLine(0);
    ArrayList<SingleValidationMessage> newValidationMessages = new ArrayList<>();
    newValidationMessages.add(m);
    return (new ValidationResult(myFhirCtx, newValidationMessages)).toOperationOutcome();
  }

  private IBaseResource getValidationMessageDataFormatException(DataFormatException e) {
    SingleValidationMessage m = new SingleValidationMessage();
    m.setSeverity(ResultSeverityEnum.FATAL);
    m.setMessage(e.getMessage());
    m.setLocationCol(0);
    m.setLocationLine(0);
    ArrayList<SingleValidationMessage> newValidationMessages = new ArrayList<>();
    newValidationMessages.add(m);
    return (new ValidationResult(myFhirCtx, newValidationMessages)).toOperationOutcome();
  }

}
