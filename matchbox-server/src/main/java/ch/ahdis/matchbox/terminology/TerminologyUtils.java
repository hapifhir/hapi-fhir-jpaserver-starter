package ch.ahdis.matchbox.terminology;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class TerminologyUtils {

	/**
	 * This class is not instantiable.
	 */
	private TerminologyUtils() {
	}

	public static Parameters mapCodingToSuccessfulParameters(final Coding coding) {
		final var parameters = new Parameters();
		parameters.setParameter("result", true);
		if (coding.hasVersion()) {
			parameters.setParameter("version", coding.getVersionElement());
		}
		if (coding.hasCode()) {
			parameters.setParameter("code", coding.getCodeElement());
		}
		if (coding.hasSystem()) {
			parameters.setParameter("system", coding.getSystemElement());
		}
		if (coding.hasDisplay()) {
			parameters.setParameter("display", coding.getDisplayElement());
		}
		return parameters;
	}

	public static Parameters mapCodeErrorToParameters(final String message) {
		final var parameters = new Parameters();
		parameters.setParameter("result", false);
		parameters.setParameter("message", message);
		final var oo = new OperationOutcome();
		oo.addIssue()
			.setSeverity(OperationOutcome.IssueSeverity.ERROR)
			.setCode(OperationOutcome.IssueType.CODEINVALID)
			.setDetails(new CodeableConcept().setText(message));
		parameters.addParameter().setName("issues").setResource(oo);
		return parameters;
	}

	public static OperationOutcome mapErrorToOperationOutcome(final String message) {
		final var oo = new OperationOutcome();
		oo.addIssue()
			.setSeverity(OperationOutcome.IssueSeverity.ERROR)
			.setCode(OperationOutcome.IssueType.INVALID)
			.setDetails(new CodeableConcept().setText(message));
		return oo;
	}
}
