package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.starter.ResourceProvider.BundlePlainProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.List;

public class EnablePlainProvider extends RestfulServer {
	public EnablePlainProvider(IFhirResourceDao<Patient> patientDao,
								 IFhirResourceDao<Organization> orgDao,
								 IFhirResourceDao<Practitioner> pracDao,
								 IFhirResourceDao<ImagingStudy> imageDao
	) {
		registerProvider(new BundlePlainProvider(patientDao, orgDao, pracDao, imageDao));

		List<IResourceProvider> resourceProviders = new ArrayList<>();
		registerProviders(resourceProviders);
	}
}
