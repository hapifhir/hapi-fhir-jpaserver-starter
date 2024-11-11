package ch.ahdis.matchbox.questionnaire;

import ch.ahdis.matchbox.providers.ConformancePackageResourceProvider;
import org.hl7.fhir.r4.model.Questionnaire;


public class QuestionnaireResourceProvider extends ConformancePackageResourceProvider<Questionnaire, org.hl7.fhir.r4b.model.Questionnaire, org.hl7.fhir.r5.model.Questionnaire> {

	public QuestionnaireResourceProvider() {
		super(Questionnaire.class, org.hl7.fhir.r4b.model.Questionnaire.class, org.hl7.fhir.r5.model.Questionnaire.class);
	}

}
