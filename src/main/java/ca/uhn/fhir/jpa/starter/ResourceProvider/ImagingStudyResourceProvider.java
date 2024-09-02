package ca.uhn.fhir.jpa.starter.ResourceProvider;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class ImagingStudyResourceProvider implements IResourceProvider {

	private static final Logger log = LoggerFactory.getLogger(ImagingStudyResourceProvider.class);
	private final IFhirResourceDao<ImagingStudy> imagingStudyDao;
	private final IFhirResourceDao<Patient> patientDao;

	public ImagingStudyResourceProvider(IFhirResourceDao<ImagingStudy> imagingStudyDao, IFhirResourceDao<Patient> patientDao) {
		this.imagingStudyDao = imagingStudyDao;
		this.patientDao = patientDao;
	}


	@Search
	public List<ImagingStudy> searchStudy(@RequiredParam(name = "pseudonym") UuidType pseudonym, RequestDetails theRequestDetails) {
		List<ImagingStudy> studies = new ArrayList<>();

		PatientResourceProvider patientResourceProvider = new PatientResourceProvider(patientDao);
		List<Patient> patientList = patientResourceProvider.searchPatientByPseudonym(pseudonym, theRequestDetails);

		log.info(patientList.toString());

		// Find all imagingstudies with reference to the patients in patientlist
		// Loop through each patient and search for ImagingStudies that reference them
		for (Patient patient : patientList) {
			// Construct the search parameter to find ImagingStudies by patient reference
			SearchParameterMap paramMap = new SearchParameterMap();
			paramMap.add(ImagingStudy.SP_PATIENT, new ReferenceParam(patient.getIdElement().getValue()));

			// Execute the search using the ImagingStudy DAO
			IBundleProvider results = imagingStudyDao.search(paramMap, theRequestDetails);

			// Add all matching ImagingStudy resources to the studies list
			if (results!=null) {
				results.getResources(0, results.size()).forEach(resource -> studies.add((ImagingStudy) resource));
			}
		}
		return studies;
	}



	@Override
	public Class<ImagingStudy> getResourceType() {
		return ImagingStudy.class;
	}
}
