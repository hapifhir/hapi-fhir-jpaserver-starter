package ca.uhn.fhir.jpa.starter.ResourceProvider;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.starter.services.CommonServices;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Resource provider contains one or more methods which have been annotated with special annotations indicating
 * which RESTful operation that method supports.
 * SearchStudy requires a pseudonym from the LRS, this pseudonym is then exchanged for a provider unique pseudonym
 * after which ImagingStudies are retrieved that belong to patient with that pseudonym
 */
@Component
public class ImagingStudyResourceProvider implements IResourceProvider {

	private static final Logger log = LoggerFactory.getLogger(ImagingStudyResourceProvider.class);
	private final IFhirResourceDao<ImagingStudy> imagingStudyDao;
	private final IFhirResourceDao<Patient> patientDao;
	private final CommonServices commonServices;


	public ImagingStudyResourceProvider(IFhirResourceDao<ImagingStudy> imagingStudyDao, IFhirResourceDao<Patient> patientDao, CommonServices commonServices) {
		this.imagingStudyDao = imagingStudyDao;
		this.patientDao = patientDao;
		this.commonServices = commonServices;

	}

	/**
	 * Search method to search for ImagingStudies belonging to a patient
	 * usage: GET /fhir/ImagingStudy/_search?pseudonym=677b33c7-30e0-4fe1-a740-87fd73c4dfaf
	 *
	 * @param pseudonym
	 *    Query parameter called pseudonym of type UuidType is the only search criteria.
	 * @param theRequestDetails
	 * 	Contains the details of the request
	 * @return
	 *    Returns a list of ImagingStudies belonging to the patient
	 * @throws Exception
	 * 	When pseudonym cannot be registered
	 */
	@Search
	public List<ImagingStudy> searchStudy(@RequiredParam(name = "pseudonym") UuidType pseudonym, RequestDetails theRequestDetails) throws Exception {
		// First exchange pseudonym for provider unique pseudonym
		UuidType newPseudonym = commonServices.exchangePseudonym(pseudonym);
		PatientResourceProvider patientResourceProvider = new PatientResourceProvider(patientDao, commonServices);
		// Get patients by pseudonym
		List<Patient> patientList = patientResourceProvider.searchPatientByPseudonym(newPseudonym, theRequestDetails);
		List<ImagingStudy> allStudies = imagingStudyDao.searchForResources(new SearchParameterMap(), theRequestDetails);

		List<ImagingStudy> filteredstudies = new ArrayList<>();
		for (Patient patient : patientList) {
			log.info(patient.getIdElement().getValue()); // Patient/3/_history/1
			// remove everything after 3 with regex
			Matcher matcher = Pattern.compile("^(Patient/\\d+)").matcher(patient.getIdElement().getValue());
			String result = matcher.find() ? matcher.group(1) : "";
			log.info(result); // Patient/3

			for (ImagingStudy imagingStudy : allStudies) {
				if (imagingStudy.getSubject().getReference().equals(result)) {
					log.info("ImagingStudy {}", imagingStudy.getIdElement().getValue());
					filteredstudies.add(imagingStudy);
				}
			}
		}

		for (ImagingStudy imagingStudy : filteredstudies) {
			log.info("Imagestudy references: {}", imagingStudy.getSubject().getReference());
		}
		return filteredstudies;
	}

	/**
	 * Create method to create ImagingStudies
	 * usage: POST /fhir/ImagingStudy  Place FHIR resource of ImagingStudy in JSON body
	 *
	 * @param theImagingStudy
	 *    Resource parameter for ImagingStudy.
	 * @param theRequestDetails
	 * 	Contains the details of the request
	 * @return
	 *    Returns the method outcome of the DAO create method
	 * @throws Exception
	 * 	When pseudonym cannot be registered
	 */
	@Create
	public MethodOutcome createImagingStudy(@ResourceParam ImagingStudy theImagingStudy, RequestDetails theRequestDetails) throws Exception {
		String referencePatient = theImagingStudy.getSubject().getReference();
		Patient patient = patientDao.read(new IdType(referencePatient), theRequestDetails);
		Extension ext = patient.getExtension().get(0);
		String patientPseudonym = ext.getValueUuidType().getValue();

		commonServices.createReferral(patientPseudonym);

		return imagingStudyDao.create(theImagingStudy, theRequestDetails);
	}


	@Override
	public Class<ImagingStudy> getResourceType() {
		return ImagingStudy.class;
	}
}
