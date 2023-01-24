package ch.ahdis.matchbox;

import org.hl7.fhir.r4.model.Questionnaire;


public class QuestionnaireResourceProvider extends ConformanceResourceProvider<Questionnaire> {

	public QuestionnaireResourceProvider() {
		super("Questionnaire");
	}
	
	@Override
	public Class<Questionnaire> getResourceType() {
		return Questionnaire.class;
	}

}
