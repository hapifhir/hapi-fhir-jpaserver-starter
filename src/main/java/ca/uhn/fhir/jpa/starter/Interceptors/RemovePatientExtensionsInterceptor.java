package ca.uhn.fhir.jpa.starter.Interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Patient;

import java.util.Collections;

/**
 * This class intercepts outgoing responses and removes patient extensions since they contain pseudonyms and
 * pseudonyms are only supposed to be used internally.
 * This class removes the extension from Patient resources and Bundle resources that contain Patients.
 */
@Interceptor
public class RemovePatientExtensionsInterceptor {

	@Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
	public void removeExtensions(IBaseResource resource) {
		if (resource instanceof Patient patient) {
			if (patient.hasExtension()) {
				patient.setExtension(Collections.emptyList());
			}
		}
		if (resource instanceof Bundle bundle) {
			if (bundle.hasEntry()) {
				for (Bundle.BundleEntryComponent entry: bundle.getEntry()){
					if (entry.getResource() instanceof Patient patient) {
						if (patient.hasExtension()) {
							patient.setExtension(Collections.emptyList());
						}
					}
				}
			}
		}
	}
}