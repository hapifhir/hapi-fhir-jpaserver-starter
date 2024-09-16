package ca.uhn.fhir.jpa.starter.ResourceProvider;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.r5.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
	private final AppProperties appProperties;

	public ImagingStudyResourceProvider(IFhirResourceDao<ImagingStudy> imagingStudyDao, IFhirResourceDao<Patient> patientDao, AppProperties appProperties) {
		this.imagingStudyDao = imagingStudyDao;
		this.patientDao = patientDao;
		this.appProperties = appProperties;

		if (appProperties.getPseudonymExchangeService() == null) {
			throw new IllegalStateException("Pseudonym service not set");
		}
	}

	/**
	 * Search method to search for ImagingStudies belonging to a patient
	 * usage: GET /fhir/ImagingStudy/_search?pseudonym=677b33c7-30e0-4fe1-a740-87fd73c4dfaf
	 *
	 * @param pseudonym
	 *    Query parameter called pseudonym of type UuidType is the only search criteria.
	 * @return
	 *    Returns a list of ImagingStudies belonging to the patient
	 */
	@Search
	public List<ImagingStudy> searchStudy(@RequiredParam(name = "pseudonym") UuidType pseudonym, RequestDetails theRequestDetails) {

		try {
			// First exchange pseudonym for provider unique pseudonym
			UuidType newPseudonym = exchangePseudonym(pseudonym);
			PatientResourceProvider patientResourceProvider = new PatientResourceProvider(patientDao);
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
			// Filter studies that reference patients in patientList
			return filteredstudies;

		} catch (Exception e) {
			log.error(e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 * Search method to search for ImagingStudies belonging to a patient
	 * usage: GET /fhir/ImagingStudy/_search?pseudonym=677b33c7-30e0-4fe1-a740-87fd73c4dfaf
	 *
	 * @param oldPseudonym
	 *    Pseudonym that is given to exchange service
	 * @return
	 *    Pseudonym that is returned by exchange service
	 */
	private UuidType exchangePseudonym(UuidType oldPseudonym) {
		String sourcePseudonym = oldPseudonym.getValue();
		String endpoint = appProperties.getPseudonymExchangeService().getEndpoint();
		String targetProviderId = appProperties.getPseudonymExchangeService().getTargetProviderId();

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPost request = new HttpPost(endpoint);

			// Manually create the JSON string
			String json = "{"
				+ "\"target_provider_id\": \"" + targetProviderId + "\","
				+ "\"source_pseudonym\": \"" + sourcePseudonym + "\""
				+ "}";

			StringEntity params = new StringEntity(json);
			request.addHeader("accept", "application/json");
			request.addHeader("Content-Type", "application/json");
			request.setEntity(params);

			CloseableHttpResponse response = httpClient.execute(request);
			String responseString = EntityUtils.toString(response.getEntity());
			// Regex to retrieve only the UUID
			String pseudonym = responseString.replaceAll(".*\"pseudonym\"\\s*:\\s*\"(.*?)\".*", "$1");
			httpClient.close(); // Ensure the client is closed
			return new UuidType(pseudonym);
		} catch (Exception ex) {
			log.info("{} Stacktrace: {}", ex.getMessage(), Arrays.toString(ex.getStackTrace()));
			return null;
		}
	}

	@Override
	public Class<ImagingStudy> getResourceType() {
		return ImagingStudy.class;
	}
}
