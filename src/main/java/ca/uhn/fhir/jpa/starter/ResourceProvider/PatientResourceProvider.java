package ca.uhn.fhir.jpa.starter.ResourceProvider;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3._1999.xhtml.Li;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class PatientResourceProvider implements IResourceProvider {

	private static final Logger log = LoggerFactory.getLogger(PatientResourceProvider.class);
	private final IFhirResourceDao<Patient> patientDao;

	public PatientResourceProvider(IFhirResourceDao<Patient> patientDao) {
		this.patientDao = patientDao;
	}

	@Create
	public MethodOutcome createPatient(@ResourceParam Patient thePatient, RequestDetails theRequestDetails) {
		String uuid = UUID.randomUUID().toString();
		Extension ext = new Extension();
		ext.setUrl("http://example.com/extensions#pseudonym");
		ext.setValue(new UuidType(uuid));
		thePatient.addExtension(ext);
		return patientDao.create(thePatient, theRequestDetails);
	}

	@Search
	public List<Patient> searchPatientByPseudonym(@RequiredParam(name = "pseudonym") UuidType pseudonym, RequestDetails theRequestDetails) {
		SearchParameterMap params = new SearchParameterMap();
		List<Patient> returnList = new ArrayList<Patient>();
		List<Patient> patientList = patientDao.searchForResources(params, theRequestDetails);
		for (Patient patient : patientList) {
			List<Extension> nonModExts = patient.getExtension();
			for (Extension ext : nonModExts) {
				log.info("{} ::: {}", ext.getValue().toString(), pseudonym.getValue());
				if (Objects.equals(ext.getValue().primitiveValue(), pseudonym.getValue())){
					returnList.add(patient);
				}
			}
		}
		return returnList;
	}


	@Override
	public Class<Patient> getResourceType() {
		return Patient.class;
	}
}
