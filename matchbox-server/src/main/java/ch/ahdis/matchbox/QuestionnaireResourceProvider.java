package ch.ahdis.matchbox;

import org.hl7.fhir.r4.model.Questionnaire;

import javax.servlet.http.HttpServletRequest;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ch.ahdis.matchbox.engine.MatchboxEngine;


public class QuestionnaireResourceProvider extends ConformanceResourceProvider<Questionnaire> {

	public QuestionnaireResourceProvider() {
		super("Questionnaire");
	}
	
	@Override
	public Class<Questionnaire> getResourceType() {
		return Questionnaire.class;
	}

	@Override
	public MethodOutcome create(HttpServletRequest theRequest, Questionnaire theResource, String theConditional,
		RequestDetails theRequestDetails) {
	  // FIXME: is default correct: we would need to derive the package for new url
	  MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine("default", false);
	  Resource existing = matchboxEngine.getCanonicalResource(theResource.getUrl());
	  if (existing !=null) {
		  matchboxEngine.dropResource("Questionnaire", existing.getId());
	  }
	  matchboxEngine.addCanonicalResource(theResource);
	  MethodOutcome methodOutcome = new MethodOutcome();
	  methodOutcome.setCreated(true);
	  methodOutcome.setResource(theResource);
	  return methodOutcome;
	}
  
	@Override
	public MethodOutcome update(HttpServletRequest theRequest, Questionnaire theResource, IIdType theId,
		String theConditional, RequestDetails theRequestDetails) {
	  MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(theResource.getUrl(), false);
	  Resource existing = matchboxEngine.getCanonicalResource(theResource.getUrl());
	  if (existing !=null) {
		  matchboxEngine.dropResource("Questionnaire", existing.getId());
	  }
	  matchboxEngine.addCanonicalResource(theResource);
	  MethodOutcome methodOutcome = new MethodOutcome();
	  methodOutcome.setCreated(true);
	  methodOutcome.setResource(theResource);
	  return methodOutcome;
	}
  

}
