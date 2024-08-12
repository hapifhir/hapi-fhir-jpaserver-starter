package ch.ahdis.matchbox.questionnaire;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.MatchboxEngineSupport;
import ch.ahdis.matchbox.engine.MatchboxEngine;

/**
 * $extract Operation for QuestionnaireResponse Resource
 *s
 */
public class QuestionnaireResponseExtractProviderR4  {
	
  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuestionnaireResponseExtractProviderR4.class);


  public final static String TARGET_STRUCTURE_MAP = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-targetStructureMap";

  @Autowired
  protected FhirContext myFhirCtx;
	
   
	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;

  

  @Operation(name = "$extract", type = QuestionnaireResponse.class, manualResponse = true, manualRequest = true)
  public void extract(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
    String contentType = theServletRequest.getContentType();   
    MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine("default", null, true, false);
    org.hl7.fhir.r5.elementmodel.Element src = Manager.parseSingle(matchboxEngine.getContext(), theServletRequest.getInputStream(),
        contentType.contains("xml") ? FhirFormat.XML : FhirFormat.JSON);
     extract(src, theServletRequest, theServletResponse); 
  }

   public void extract(org.hl7.fhir.r5.elementmodel.Element src, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
    Set<String> highestRankedAcceptValues = RestfulServerUtils
    .parseAcceptHeaderAndReturnHighestRankedOptions(theServletRequest);

    String responseContentType = Constants.CT_FHIR_XML_NEW;
    if (highestRankedAcceptValues.contains(Constants.CT_FHIR_JSON_NEW)) {
      responseContentType = Constants.CT_FHIR_JSON_NEW;
    }
    // patch for fhir-kit-client https://github.com/Vermonster/fhir-kit-client/pull/143
    if (highestRankedAcceptValues.contains(Constants.CT_FHIR_JSON)) {
      responseContentType = Constants.CT_FHIR_JSON_NEW;
    }

    // get canonical URL of questionnaire
    String questionnaireUri = src.getChildValue("questionnaire");
    if (questionnaireUri == null)
      throw new UnprocessableEntityException("No questionnaire canonical URL given.");


    MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(questionnaireUri, null, true, false);
    if (matchboxEngine == null)
      throw new UnprocessableEntityException(
          "Could not get matcbox-enginge with questionnaire with canonical URL '" + questionnaireUri + "'");

   
    Questionnaire questionnaire = (Questionnaire) matchboxEngine.getCanonicalResource(questionnaireUri, "4.0.1");
    if (questionnaire == null)
      throw new UnprocessableEntityException(
          "Could not fetch questionnaire with canonical URL '" + questionnaireUri + "'");

    // get targetStructureMap extension from questionnaire
    Extension targetStructureMapExtension = questionnaire.getExtensionByUrl(TARGET_STRUCTURE_MAP);
    if (targetStructureMapExtension == null)
      throw new UnprocessableEntityException("No sdc-questionnaire-targetStructureMap extension found in resource");
    String mapUrl = targetStructureMapExtension.getValue().primitiveValue();
    
    org.hl7.fhir.r5.model.StructureMap map  = matchboxEngine.getContext().fetchResource(org.hl7.fhir.r5.model.StructureMap.class, mapUrl);
    if (map == null) {
        throw new UnprocessableEntityException("Map not available with canonical url "+mapUrl);
    }

    org.hl7.fhir.r5.elementmodel.Element r = matchboxEngine.transform(src, map.getUrl(), null);
     
    theServletResponse.setContentType(responseContentType);
    theServletResponse.setCharacterEncoding("UTF-8");

    ServletOutputStream output = theServletResponse.getOutputStream();
    try {
      if (output != null) {
        if (responseContentType.equals(Constants.CT_FHIR_JSON_NEW))
          new org.hl7.fhir.r5.elementmodel.JsonParser(matchboxEngine.getContext()).compose(r, output, OutputStyle.PRETTY, null);
        else
          new org.hl7.fhir.r5.elementmodel.XmlParser(matchboxEngine.getContext()).compose(r, output, OutputStyle.PRETTY, null);
      }
    } catch(org.hl7.fhir.exceptions.FHIRException e) {
      log.error("Transform exception", e);
      output.write("Exception during Transform".getBytes());
    }
    theServletResponse.getOutputStream().close();
  }

}
