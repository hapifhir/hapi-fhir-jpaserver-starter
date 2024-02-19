package ch.ahdis.matchbox.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementCompositeDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.impl.BaseHttpClientInvocation;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.client.method.HttpPostClientInvocation;
import ca.uhn.fhir.rest.client.method.IClientResponseHandler;
import ca.uhn.fhir.rest.client.method.MethodUtil;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.util.ParametersUtil;


/**
 * ValidationClient extends the GenericClient
 * @author oliveregger
 *
 */
public class ValidationClient extends GenericClient{
  

  public ValidationClient(FhirContext theContext, String theServerBase) {
    super(theContext, null, theServerBase, null);
    setDontValidateConformance(true);
    theContext.getRestfulClientFactory().setSocketTimeout(600 * 1000);
  }
    
  private final class OutcomeResponseHandler implements IClientResponseHandler<MethodOutcome> {

    private OutcomeResponseHandler() {
      super();
    }

    @Override
    public MethodOutcome invokeClient(String theResponseMimeType, InputStream theResponseInputStream, int theResponseStatusCode, Map<String, List<String>> theHeaders) throws BaseServerResponseException {
      MethodOutcome response = MethodUtil.process2xxResponse(getFhirContext(), theResponseStatusCode, theResponseMimeType, theResponseInputStream, theHeaders);
      response.setCreatedUsingStatusCode(theResponseStatusCode);
      response.setResponseHeaders(theHeaders);
      return response;
    }
  }

  public static BaseHttpClientInvocation createValidationInvocation(FhirContext theContext, String theOperationName, String theInput, Map<String, List<String>> urlParams) {
		StringBuilder b = new StringBuilder();
		if (b.length() > 0) {
			b.append('/');
		}
		if (!theOperationName.startsWith("$")) {
			b.append("$");
		}
		b.append(theOperationName);
		BaseHttpClientInvocation.appendExtraParamsWithQuestionMark(urlParams, b, b.indexOf("?") == -1);
		return new HttpPostClientInvocation(theContext, theInput, false, b.toString());
	}

	private static Optional<? extends IPrimitiveType<?>> getNameValue(
			IBase nextParameter, BaseRuntimeElementCompositeDefinition<?> theNextParameterDef) {
		BaseRuntimeChildDefinition nameChild = theNextParameterDef.getChildByName("name");
		List<IBase> nameValues = nameChild.getAccessor().getValues(nextParameter);
		return nameValues.stream()
				.filter(t -> t instanceof IPrimitiveType<?>)
				.map(t -> ((IPrimitiveType<?>) t))
				.findFirst();
	}


  private  List<String> getParameterNamesFirstLevel(FhirContext theCtx, IBaseParameters theParameters) {
    RuntimeResourceDefinition resDef = theCtx.getResourceDefinition(theParameters.getClass());
		BaseRuntimeChildDefinition parameterChild = resDef.getChildByName("parameter");
		List<IBase> parameterReps = parameterChild.getAccessor().getValues(theParameters);
    if (parameterReps!=null)
  		return parameterReps.stream()
				.map(t -> getNameValue(t, (BaseRuntimeElementCompositeDefinition<?>) theCtx.getElementDefinition(t.getClass())).get().getValueAsString()).toList();
    return null;
	}

    /**
   * Performs the $validate operation with a direct POST (see http://hl7.org/fhir/resource-operation-validate.html#examples)
   * and the profile specified as a parameter, additional parameters can be provided
   * @param theContents content to validate
   * @param theProfile optional: profile to validate against
   * @return
   */
  public IBaseOperationOutcome validate(String theContents, String theProfile) {
    return validate(theContents, theProfile, null);
  }


  /**
   * Performs the $validate operation with a direct POST (see http://hl7.org/fhir/resource-operation-validate.html#examples)
   * and the profile specified as a parameter, additional parameters can be provided
   * @param theContents content to validate
   * @param theProfile optional: profile to validate against
   * @param parameters optional: additional validation parameters
   * @return
   */
  public IBaseOperationOutcome validate(String theContents, String theProfile, IBaseParameters parameters) {
    setEncoding(EncodingEnum.detectEncoding(theContents));
    Map<String, List<String>> theExtraParams = null;
    if (theProfile!=null || parameters!=null) {
      theExtraParams = new HashMap<String, List<String>>();
      if (theProfile != null) {
        List<String> profiles = new ArrayList<String>();
        profiles.add(theProfile);
        theExtraParams.put("profile", profiles);
      }
      if (parameters!=null) {
        List<String> parametersLevel1 = getParameterNamesFirstLevel(getFhirContext(), parameters);
        if (parametersLevel1 != null) {
          for (String parameter : parametersLevel1) {
            theExtraParams.put(parameter, ParametersUtil.getNamedParameterValuesAsString(getFhirContext(), parameters,  parameter));
          }
        }
      }
    }
    OutcomeResponseHandler binding = new OutcomeResponseHandler();
    BaseHttpClientInvocation clientInvoke = createValidationInvocation(getFhirContext(), "$validate", theContents, theExtraParams);
    MethodOutcome resp = invokeClient(getFhirContext(), binding, clientInvoke, null, null, false, null, null, null, null, null);
    return resp.getOperationOutcome();
  }
  
}
