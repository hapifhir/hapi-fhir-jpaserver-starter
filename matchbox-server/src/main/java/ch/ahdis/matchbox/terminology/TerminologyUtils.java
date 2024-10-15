package ch.ahdis.matchbox.terminology;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Parameters;

import javax.annotation.Nullable;

/**
 * Some utilities for the internal terminology server.
 *
 * @author Quentin Ligier
 **/
public class TerminologyUtils {

	/**
	 * This class is not instantiable.
	 */
	private TerminologyUtils() {
	}

	public static Parameters mapCodeToSuccessfulParameters(final String code) {
		final var parameters = new Parameters();
		parameters.setParameter("result", true);
		if (code != null) {
			parameters.setParameter("code", code);
		}
		return parameters;
	}

	public static Parameters createSuccessfulResponseParameters(final Coding coding) {
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

	public static Parameters createErrorResponseParameters(final String message,
																			 @Nullable final Coding coding,
																			 @Nullable final CodeableConcept codeableConcept) {
		final var parameters = new Parameters();
		parameters.setParameter("result", false);
		parameters.setParameter("message", message);

		if (coding != null) {
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
		}

		if (codeableConcept != null) {
			parameters.setParameter("codeableConcept", codeableConcept);
		}

		final var oo = new OperationOutcome();
		oo.addIssue()
			.setSeverity(OperationOutcome.IssueSeverity.ERROR)
			.setCode(OperationOutcome.IssueType.CODEINVALID)
			.setDetails(new CodeableConcept()
								.addCoding(new Coding()
												  .setSystem("http://hl7.org/fhir/tools/CodeSystem/tx-issue-type")
												  .setCode("not-in-vs"))
								.setText(message))
			.addLocation("Coding.code")
			.addExpression("Coding.code");

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
