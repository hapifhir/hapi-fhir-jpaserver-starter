package ch.ahdis.matchbox.questionnaire;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r5.model.Property;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent;
import org.hl7.fhir.r5.terminologies.ConceptMapEngine;
import org.hl7.fhir.r5.utils.structuremap.ITransformerServices;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.mappinglanguage.ConvertingWorkerContext;
import ch.ahdis.matchbox.mappinglanguage.TransformSupportServices;
import ch.ahdis.matchbox.provider.SimpleWorkerContextProvider;

public class QuestionnaireResponseProvider  {

  public final static String TARGET_STRUCTURE_MAP = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-targetStructureMap";

  @Autowired
  protected FhirContext myFhirCtx;
	
  @Autowired
  protected ConvertingWorkerContext workerContext;
  
  @Operation(name = "$extract", type = QuestionnaireResponse.class, manualResponse = true, manualRequest = true)
  public void extract(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {

    StructureMapUtilities utils = new StructureMapUtilities(workerContext,
        new TransformSupportServices(workerContext, new ArrayList<Base>()));
    String contentType = theServletRequest.getContentType();

    Set<String> highestRankedAcceptValues = RestfulServerUtils
        .parseAcceptHeaderAndReturnHighestRankedOptions(theServletRequest);

    String responseContentType = Constants.CT_FHIR_XML_NEW;
    if (highestRankedAcceptValues.contains(Constants.CT_FHIR_JSON_NEW)) {
      responseContentType = Constants.CT_FHIR_JSON_NEW;
    }

    // parse QuestionnaireResponse from request body
    org.hl7.fhir.r5.elementmodel.Element src = Manager.parse(workerContext, theServletRequest.getInputStream(),
        contentType.contains("xml") ? FhirFormat.XML : FhirFormat.JSON);

    // get canonical URL of questionnaire
    String questionnaireUri = src.getChildValue("questionnaire");
    if (questionnaireUri == null)
      throw new UnprocessableEntityException("No questionnaire canonical URL given.");

    // fetch corresponding questionnaire
    workerContext.load(org.hl7.fhir.r4.model.Questionnaire.class, questionnaireUri);
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
    workerContext.loadMap(mapUrl);
    org.hl7.fhir.r5.model.StructureMap map = workerContext.getTransform(mapUrl);
    if (map == null) {
      throw new UnprocessableEntityException("Map not available with canonical url " + mapUrl);
    }

    // create target resource of structure map
    org.hl7.fhir.r5.elementmodel.Element r = getTargetResourceFromStructureMap(map);
    if (r == null) {
      throw new UnprocessableEntityException(
          "Target Structure can not be resolved from map, is the corresponding implmentation guide provided?");
    }

    // transform
    utils.transform(null, src, map, r);

    // return result
    theServletResponse.setContentType(responseContentType);
    theServletResponse.setCharacterEncoding("UTF-8");
    ServletOutputStream output = theServletResponse.getOutputStream();

    if (output != null) {
      if (output != null && responseContentType.equals(Constants.CT_FHIR_JSON_NEW))
        new org.hl7.fhir.r5.elementmodel.JsonParser(workerContext).compose(r, output, OutputStyle.PRETTY, null);
      else
        new org.hl7.fhir.r5.elementmodel.XmlParser(workerContext).compose(r, output, OutputStyle.PRETTY, null);
    }
    theServletResponse.getOutputStream().close();

  }

  private org.hl7.fhir.r5.elementmodel.Element getTargetResourceFromStructureMap(
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

}
