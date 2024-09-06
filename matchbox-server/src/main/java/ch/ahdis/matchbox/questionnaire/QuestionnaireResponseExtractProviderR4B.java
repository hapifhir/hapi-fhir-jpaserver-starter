package ch.ahdis.matchbox.questionnaire;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hl7.fhir.r4b.model.QuestionnaireResponse;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;

import ca.uhn.fhir.rest.annotation.Operation;
import ch.ahdis.matchbox.engine.MatchboxEngine;

/**
 * $extract Operation for QuestionnaireResponse Resource
 *s
 */
public class QuestionnaireResponseExtractProviderR4B extends QuestionnaireResponseExtractProvider {
	  

  @Operation(name = "$extract", type = QuestionnaireResponse.class, manualResponse = true, manualRequest = true)
  public void extract(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
    String contentType = theServletRequest.getContentType();   
    MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine("default", null, true, false);
    org.hl7.fhir.r5.elementmodel.Element src = Manager.parseSingle(matchboxEngine.getContext(), theServletRequest.getInputStream(),
        contentType.contains("xml") ? FhirFormat.XML : FhirFormat.JSON);
     extract(src, theServletRequest, theServletResponse); 
  }


}
