package ch.ahdis.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.method.*;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Charsets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.impl.BaseHttpClientInvocation;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.r4.model.CapabilityStatement;


/**
 * ValidationClient extends the Generice
 * @author oliveregger
 *
 */
public class GenericFhirClient extends GenericClient{

  static final public String testServer = "http://localhost:8080/matchbox/fhir";

  public GenericFhirClient(FhirContext theContext, String theServerBase) {
    super(theContext, null, theServerBase, null);
    setDontValidateConformance(true);
  }

  public GenericFhirClient(FhirContext theContext) {
    this(theContext, testServer);
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

	public static BaseHttpClientInvocation createOperationInvocation(FhirContext theContext, String theOperationName, String theInput, Map<String, List<String>> urlParams) {
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

  /**
   * Performs the $validate operation with a direct POST (see http://hl7.org/fhir/resource-operation-validate.html#examples)
   * and the profile specified as a parameter (not the Parameters syntact).
   * @param theContents content to validate
   * @param theProfile optional: profile to validate against
   * @return
   */
  public IBaseOperationOutcome validate(String theContents, String theProfile) {
    setEncoding(EncodingEnum.detectEncoding(theContents));
    Map<String, List<String>> theExtraParams = null;
    if (theProfile!=null) {
      theExtraParams = new HashMap<String, List<String>>();
      List<String> profiles = new ArrayList<String>();
      profiles.add(theProfile);
      theExtraParams.put("profile", profiles);
    }
    OutcomeResponseHandler binding = new OutcomeResponseHandler();
    BaseHttpClientInvocation clientInvoke = createOperationInvocation(getFhirContext(), "$validate", theContents, theExtraParams);
    MethodOutcome resp = invokeClient(getFhirContext(), binding, clientInvoke, null, null, false, null, null, null, null, null);
    return resp.getOperationOutcome();
  }


  private String getStructureMapTransformOperation(Map<String, List<String>> urlParams) {
    StringBuilder b = new StringBuilder();
    b.append("StructureMap/$transform");
    BaseHttpClientInvocation.appendExtraParamsWithQuestionMark(urlParams, b, b.indexOf("?") == -1);
    return b.toString();
  }

  public BaseHttpClientInvocation createStructureMapTransformInvocation(FhirContext theContext, String theInput, Map<String, List<String>> urlParams) {
    return new HttpPostClientInvocation(theContext, theInput, false, getStructureMapTransformOperation(urlParams));
  }

  public BaseHttpClientInvocation createStructureMapTransformInvocation(FhirContext theContext, IBaseResource resource, Map<String, List<String>> urlParams) {
    return new HttpPostClientInvocation(theContext, resource, getStructureMapTransformOperation(urlParams));
  }


  /**
   * Performs the $transform operation with a direct POST and returning a Resource
   * @return
   */
  public IBaseResource convert(String theContents, EncodingEnum contentEndoding, String sourceMapUrl, String acceptHeader) {
    setEncoding(contentEndoding);
    Map<String, List<String>> theExtraParams = null;
    if (sourceMapUrl!=null) {
      theExtraParams = new HashMap<String, List<String>>();
      List<String> urls = new ArrayList<String>();
      urls.add(sourceMapUrl);
      theExtraParams.put("source", urls);
    }
    ResourceResponseHandler<IBaseResource> binding = new ResourceResponseHandler<IBaseResource>();
    BaseHttpClientInvocation clientInvoke = createStructureMapTransformInvocation(getFhirContext(), theContents, theExtraParams);
    return invokeClient(getFhirContext(), binding, clientInvoke, null, null, false, null, null, null, acceptHeader, null);
  }

  private final class StringResponseHandler implements IClientResponseHandler<String> {

    @Override
    public String invokeClient(String theResponseMimeType, InputStream theResponseInputStream, int theResponseStatusCode, Map<String, List<String>> theHeaders)
      throws IOException, BaseServerResponseException {
      return IOUtils.toString(theResponseInputStream, Charsets.UTF_8);
    }
  }

  /**
   * Performs the $transform operation with a direct POST and returning a Resource 
   * @return
   */
  public String convert(IBaseResource resource, EncodingEnum contentEndoding, String sourceMapUrl, String acceptHeader) {
    setEncoding(contentEndoding);
    Map<String, List<String>> theExtraParams = null;
    if (sourceMapUrl!=null) {
      theExtraParams = new HashMap<String, List<String>>();
      List<String> urls = new ArrayList<String>();
      urls.add(sourceMapUrl);
      theExtraParams.put("source", urls);
    }
    BaseHttpClientInvocation clientInvoke = createStructureMapTransformInvocation(getFhirContext(), resource, theExtraParams);
    return invokeClient(getFhirContext(), new StringResponseHandler(), clientInvoke, null, null, false, null, null, null, acceptHeader, null);
  }
  
	public CapabilityStatement retrieveCapabilityStatement() {
		IGenericClient client = getFhirContext().newRestfulGenericClient(testServer);
		CapabilityStatement capabilityStatement = client.capabilities().ofType(CapabilityStatement.class).execute();
		return capabilityStatement;
	}

}
