package ca.uhn.fhir.jpa.starter.ResourceProvider;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BundlePlainProvider {

	private final IFhirResourceDao<Patient> patientDao;
	private final IFhirResourceDao<Organization> orgDao;
	private final IFhirResourceDao<Practitioner> pracDao;
	private final IFhirResourceDao<ImagingStudy> imageDao;

	private static final Logger log = LoggerFactory.getLogger(BundlePlainProvider.class);


	public BundlePlainProvider(IFhirResourceDao<Patient> patientDao, IFhirResourceDao<Organization> orgDao, IFhirResourceDao<Practitioner> pracDao, IFhirResourceDao<ImagingStudy> imageDao) {
		this.patientDao = patientDao;
		this.orgDao = orgDao;
		this.pracDao = pracDao;
		this.imageDao = imageDao;
	}


	/**
	 * Create method to create a bundle
	 * usage: POST to /fhir with JSON body with bundle resource
	 *
	 * @param theBundle
	 *    Resource of the to be created bundle
	 * @return
	 *    DAO create method outcome
	 */
	@Transaction
	public Bundle createPatientFromBundle(@TransactionParam Bundle theBundle, RequestDetails theRequestDetails) {
		String uuid = UUID.randomUUID().toString();
		Extension ext = new Extension();
		ext.setUrl("https://example.com/extensions#pseudonym");
		ext.setValue(new UuidType(uuid));
		Bundle createdBundle = new Bundle();

		if (theBundle.hasEntry()) {
			for (Bundle.BundleEntryComponent entry : theBundle.getEntry()) {
				IBaseResource createdResource = null;

				if (entry.getResource() instanceof Patient patient) {
					patient.addExtension(ext);
					createdResource = patientDao.create(patient, theRequestDetails).getResource();
				}
				else if (entry.getResource() instanceof Organization organization) {
					createdResource = orgDao.create(organization, theRequestDetails).getResource();
				}
				else if (entry.getResource() instanceof Practitioner practitioner) {
					createdResource = pracDao.create(practitioner, theRequestDetails).getResource();
				}
				else if (entry.getResource() instanceof ImagingStudy imagingStudy) {
					createdResource = imageDao.create(imagingStudy, theRequestDetails).getResource();
				}
				else {
					log.error("Resource not created");
				}

				if (createdResource != null) {
					Bundle.BundleEntryComponent newEntry = new Bundle.BundleEntryComponent();
					newEntry.setResource((Resource) createdResource);
					createdBundle.addEntry(newEntry);
				}
			}
		}

		return createdBundle;
	}
}
