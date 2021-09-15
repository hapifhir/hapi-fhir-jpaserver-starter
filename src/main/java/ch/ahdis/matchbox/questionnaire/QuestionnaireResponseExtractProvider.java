package ch.ahdis.matchbox.questionnaire;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.mappinglanguage.ConvertingWorkerContext;
import ch.ahdis.matchbox.mappinglanguage.ElementModelSorter;
import ch.ahdis.matchbox.mappinglanguage.TransformSupportServices;

/**
 * $extract Operation for QuestionnaireResponse Resource
 *
 */
public class QuestionnaireResponseExtractProvider  {

  public final static String TARGET_STRUCTURE_MAP = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-targetStructureMap";

  @Autowired
  protected FhirContext myFhirCtx;
	
  @Autowired
  protected ConvertingWorkerContext baseWorkerContext;
  
  @Autowired
  private DaoRegistry myDaoRegistry;
  

  @Operation(name = "$extract", type = QuestionnaireResponse.class, manualRequest = true, manualResponse = true, idempotent = true)
  public void extract(@IdParam IdType theQuestionnaireResponseId, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
	  
	  QuestionnaireResponse input = myDaoRegistry.getResourceDao(QuestionnaireResponse.class).read(theQuestionnaireResponseId);
	  if (input == null) throw new ResourceNotFoundException(theQuestionnaireResponseId);

	  
	    // parse QuestionnaireResponse from request body
	    org.hl7.fhir.r5.elementmodel.Element src = convertToElementModel(baseWorkerContext, input);

	    extract(baseWorkerContext, src, theServletRequest, theServletResponse);
  }

  
  @Operation(name = "$extract", type = QuestionnaireResponse.class, manualResponse = true, manualRequest = true)
  public void extract(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
    
    String contentType = theServletRequest.getContentType();   

    // parse QuestionnaireResponse from request body
    org.hl7.fhir.r5.elementmodel.Element src = Manager.parse(baseWorkerContext, theServletRequest.getInputStream(),
        contentType.contains("xml") ? FhirFormat.XML : FhirFormat.JSON);
    extract(baseWorkerContext, src, theServletRequest, theServletResponse); 
  }

   public void extract(ConvertingWorkerContext workerContext, org.hl7.fhir.r5.elementmodel.Element src, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
	 StructureMapUtilities utils = new StructureMapUtilities(workerContext,
	     new TransformSupportServices(workerContext, new ArrayList<Base>()));
	   
	 Set<String> highestRankedAcceptValues = RestfulServerUtils
		        .parseAcceptHeaderAndReturnHighestRankedOptions(theServletRequest);

    String responseContentType = Constants.CT_FHIR_XML_NEW;
    if (highestRankedAcceptValues.contains(Constants.CT_FHIR_JSON_NEW)) {
      responseContentType = Constants.CT_FHIR_JSON_NEW;
    }
    // get canonical URL of questionnaire
    String questionnaireUri = src.getChildValue("questionnaire");
    if (questionnaireUri == null)
      throw new UnprocessableEntityException("No questionnaire canonical URL given.");

    // fetch corresponding questionnaire
    //workerContext.load(org.hl7.fhir.r4.model.Questionnaire.class, questionnaireUri);
    Questionnaire questionnaire = workerContext.fetchResource(Questionnaire.class, questionnaireUri);
    if (questionnaire == null)
      throw new UnprocessableEntityException(
          "Could not fetch questionnaire with canonical URL '" + questionnaireUri + "'");

    // get targetStructureMap extension from questionnaire
    Extension targetStructureMapExtension = questionnaire.getExtensionByUrl(TARGET_STRUCTURE_MAP);
    if (targetStructureMapExtension == null)
      throw new UnprocessableEntityException("No sdc-questionnaire-targetStructureMap extension found in resource");
    String mapUrl = targetStructureMapExtension.getValue().primitiveValue();

    // fetch structure map to use
    //workerContext.loadMap(mapUrl);
    org.hl7.fhir.r5.model.StructureMap map = workerContext.getTransform(mapUrl);
    if (map == null) {
      throw new UnprocessableEntityException("Map not available with canonical url " + mapUrl);
    }

    // create target resource of structure map
    org.hl7.fhir.r5.elementmodel.Element r = getTargetResourceFromStructureMap(workerContext, map);
    if (r == null) {
      throw new UnprocessableEntityException(
          "Target Structure can not be resolved from map, is the corresponding implmentation guide provided?");
    }

    // transform
    utils.transform(null, src, map, r);
    ElementModelSorter.sort(r);

    // return result
    theServletResponse.setContentType(responseContentType);
    theServletResponse.setCharacterEncoding("UTF-8");
    ServletOutputStream output = theServletResponse.getOutputStream();

    if (output != null) {
      if (output != null && responseContentType.equals(Constants.CT_FHIR_JSON_NEW))
        new org.hl7.fhir.r5.elementmodel.JsonParser(workerContext).compose(r, output, OutputStyle.PRETTY, null);
      else
        new ch.ahdis.matchbox.mappinglanguage.XmlParser(workerContext).compose(r, output, OutputStyle.PRETTY, null);
    }
    theServletResponse.getOutputStream().close();

  }

  private org.hl7.fhir.r5.elementmodel.Element getTargetResourceFromStructureMap(IWorkerContext workerContext, 
      org.hl7.fhir.r5.model.StructureMap map) {
    String targetTypeUrl = null;
    for (StructureMapStructureComponent component : map.getStructure()) {
      if (component.getMode() == org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.TARGET) {
        targetTypeUrl = component.getUrl();
        break;
      }
    }

    if (targetTypeUrl == null)
      throw new FHIRException("Unable to determine resource URL for target type");

    StructureDefinition structureDefinition = null;
    for (StructureDefinition sd : workerContext.getStructures()) {
      if (sd.getUrl().equalsIgnoreCase(targetTypeUrl)) {
        structureDefinition = sd;
        break;
      }
    }
    if (structureDefinition == null)
      throw new FHIRException("Unable to determine StructureDefinition for target type");

    return Manager.build(workerContext, structureDefinition);
  }
  
  /**
	 * convert R4 resources to element model
	 * @param inputResource
	 * @return
	 */
	private org.hl7.fhir.r5.elementmodel.Element convertToElementModel(IWorkerContext workerContext, Resource inputResource) {
		 String inStr = FhirContext.forR4Cached().newJsonParser().encodeResourceToString(inputResource);
		 
		 try {
	       return Manager.parse(workerContext, new ByteArrayInputStream(inStr.getBytes()), FhirFormat.JSON);
		 } catch (IOException e) {
			 throw new UnprocessableEntityException("Cannot convert resource to element model");
		 }	 
	}

}
