package ch.ahdis.matchbox.questionnaire;

import ca.uhn.fhir.rest.annotation.Operation;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

import java.io.IOException;

/**
 * $extract Operation for QuestionnaireResponse Resources
 */
public class QuestionnaireResponseExtractProviderR4 extends QuestionnaireResponseExtractProvider {

	public QuestionnaireResponseExtractProviderR4(final MatchboxEngineSupport matchboxEngineSupport) {
		super(matchboxEngineSupport);
	}

	@Operation(name = "$extract", type = QuestionnaireResponse.class, manualResponse = true, manualRequest = true)
	public void extract(final HttpServletRequest theServletRequest, final HttpServletResponse theServletResponse)
		throws IOException {
		super.extract(theServletRequest, theServletResponse);
	}
}
