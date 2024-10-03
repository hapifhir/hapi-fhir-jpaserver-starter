package ca.uhn.fhir.jpa.starter.Interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class intercepts outgoing responses and removes patient extensions since they contain pseudonyms and
 * pseudonyms are only supposed to be used internally.
 * This class removes the extension from Patient resources and Bundle resources that contain Patients.
 */
@Interceptor
public class RemovePatientExtensionsInterceptor {

	private static final Logger log = LoggerFactory.getLogger(RemovePatientExtensionsInterceptor.class);

	/**
	 * This function hooks into the server outgoing responses and removes
	 * any pseudonym extensions that patients might have.
	 *
	 * @param resource
	 * 	The resource of the outgoing response
	 */
	@Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
	public void checkForPatient(IBaseResource resource) {
		log.info("Removing patient pseudonym extensions");
		if (resource instanceof Patient patient) {
			removeExtension(patient);
		}
		if (resource instanceof Bundle bundle) {
			if (bundle.hasEntry()) {
				for (Bundle.BundleEntryComponent entry: bundle.getEntry()){
					if (entry.getResource() instanceof Patient patient) {
						removeExtension(patient);
					}
				}
			}
		}
	}

	/**
	 * @param patient
	 * 	The patient that could have a pseudonym extension
	 */
	private void removeExtension(Patient patient) {
		// Retrieve the list of current extensions, excluding the one with the specific URL
		List<Extension> updatedExtensions = patient.getExtension().stream()
			.filter(ext -> !ext.getUrl().equals("https://example.com/extensions#pseudonym"))
			.toList();
		patient.setExtension(updatedExtensions);
		log.info("Patient pseudonym extensions removed");
	}
}