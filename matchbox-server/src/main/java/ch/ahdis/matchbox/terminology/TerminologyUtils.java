package ch.ahdis.matchbox.terminology;

import org.hl7.fhir.r4.model.Coding;
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
		if (coding.hasCode()) {
			parameters.setParameter("code", coding.getCodeElement());
		}
		if (coding.hasSystem()) {
			parameters.setParameter("system", coding.getSystemElement());
		}
		if (coding.hasDisplay()) {
			parameters.setParameter("display", coding.getDisplayElement());
		}
		if (coding.hasVersion()) {
			parameters.setParameter("version", coding.getVersionElement());
		}
		return parameters;
	}
}
