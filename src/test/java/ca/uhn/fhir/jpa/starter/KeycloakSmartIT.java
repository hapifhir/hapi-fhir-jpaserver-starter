package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.starter.conf.FhirStarterTestContainers;
import ca.uhn.fhir.rest.api.DeleteCascadeModeEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.gclient.ICreateTyped;
import ca.uhn.fhir.rest.gclient.IDeleteTyped;
import ca.uhn.fhir.rest.gclient.IReadExecutable;
import ca.uhn.fhir.rest.gclient.IUpdateExecutable;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static ca.uhn.fhir.jpa.starter.AuthorizationInterceptorTest.ACCESS_DENIED_DUE_TO_SCOPE_RULE_EXCEPTION_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(initializers = FhirStarterTestContainers.KeycloakContainerInitializer.class)
class KeycloakSmartIT {

	private static String tokenEndpoint = "/auth/realms/smart/protocol/openid-connect/token";
	private static String baseKeycloakUrl;

	// IDs returned as test data from Keycloak (src/test/resources/keycloak/realm.json#users)
	private static final String PATIENT_ATTRIBUTE = "101";
	private static final String PRACTITIONER_ATTRIBUTE = "102";

	@LocalServerPort
	private int port;

	@Autowired
	private IFhirResourceDao<Patient> patientResourceDao;
	@Autowired
	private IFhirResourceDao<Observation> observationResourceDao;
	@Autowired
	private IFhirResourceDao<Practitioner> practitionerResourceDao;

	@Autowired
	DaoConfig daoConfig;

	private IGenericClient client;
	private FhirContext ctx;


	@BeforeAll
	static void setUp() {
		FhirStarterTestContainers.getPostgreSQLContainer();
		baseKeycloakUrl = "http://localhost:"+FhirStarterTestContainers.getKeycloakContainer().getMappedPort(8080);
//		keyCloakUrl = "http://localhost:8081/auth/realms/smart/protocol/openid-connect/token";
	}

	@BeforeEach
	void beforeEach() {
		daoConfig.setResourceServerIdStrategy(DaoConfig.IdStrategyEnum.UUID);
		daoConfig.setResourceClientIdStrategy(DaoConfig.ClientIdStrategyEnum.ANY);
		ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ctx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		client = ctx.newRestfulGenericClient(ourServerBase);
	}

	@Test
	void getPatientAsPatient_patientHasPermissionsToViewPatient() throws JSONException {
		// ARRNAGE
		Patient expectedResource = getPatient(PATIENT_ATTRIBUTE);

		String jwt = getJwtToken("patient", "patient", "patient/*.read");

		// ACT
		IReadExecutable<IBaseResource> patientReadExecutable = client.read().resource("Patient").withId(PATIENT_ATTRIBUTE).withAdditionalHeader("Authorization", "Bearer " + jwt);
		IBaseResource actualResource = patientReadExecutable.execute();

		// ASSERT
		assertEquals(expectedResource.getIdElement().getIdPart(), actualResource.getIdElement().getIdPart());
	}

	@Test
	void getPatientAsPatient_wrongPatientId() throws JSONException {
		// ARRNAGE
		String mockId = "6";
		Patient createdResource = getPatient(mockId);

		String jwt = getJwtToken("patient", "patient", "patient/*.read");

		IReadExecutable<IBaseResource> patientReadExecutable = client.read().resource("Patient").withId(mockId).withAdditionalHeader("Authorization", "Bearer " + jwt);

		// ACT
		ForbiddenOperationException forbiddenOperationException = assertThrows(ForbiddenOperationException.class, patientReadExecutable::execute);

		// ASSERT
		assertEquals(ACCESS_DENIED_DUE_TO_SCOPE_RULE_EXCEPTION_MESSAGE, forbiddenOperationException.getMessage());

		// AFTER
		patientResourceDao.delete(createdResource.getIdElement());
	}


	@Test
	void addObservationAsPatient_patientDoesNotHasPermissionToWrite() throws JSONException {
		// ARRNAGE
		Patient updatedResource = getPatient(PATIENT_ATTRIBUTE);

		String jwt = getJwtToken("patient", "patient", "patient/*.read");
		Observation observation = new Observation();
		observation.setSubject(new Reference(updatedResource.getIdElement()));

		ICreateTyped observationCreateExecutable = client.create().resource(observation).withAdditionalHeader("Authorization", "Bearer " + jwt);

		// ACT
		ForbiddenOperationException forbiddenOperationException = assertThrows(ForbiddenOperationException.class, observationCreateExecutable::execute);

		// ASSERT
		assertEquals(ACCESS_DENIED_DUE_TO_SCOPE_RULE_EXCEPTION_MESSAGE, forbiddenOperationException.getMessage());

		// AFTER
		patientResourceDao.delete(updatedResource.getIdElement());
	}

	@Test
	void addObservationAsPatient_patientHasPermissionToWrite() throws JSONException {
		// ARRNAGE
		Patient updatedResource = getPatient(PATIENT_ATTRIBUTE);

		String jwt = getJwtToken("patient", "patient", "patient/*.write");
		Observation observation = new Observation();
		observation.setSubject(new Reference(updatedResource.getIdElement()));

		ICreateTyped observationCreateExecutable = client.create().resource(observation).withAdditionalHeader("Authorization", "Bearer " + jwt);

		// ACT
		MethodOutcome outcome=observationCreateExecutable.execute();

		// ASSERT
		assertTrue(outcome.getCreated());

		// AFTER
		observationResourceDao.delete(outcome.getResource().getIdElement());
		patientResourceDao.delete(updatedResource.getIdElement());
	}

	@Test
	void doOperationsAsPatient_patientHasAllPermissions() throws JSONException {
		// ARRNAGE
		Patient expectedResource = getPatient(PATIENT_ATTRIBUTE);

		String jwt = getJwtToken("patient", "patient", "patient/*.*");
		Observation observation = new Observation();
		observation.setSubject(new Reference(expectedResource.getIdElement()));

		IReadExecutable<IBaseResource> patientReadExecutable = client.read().resource("Patient").withId(PATIENT_ATTRIBUTE).withAdditionalHeader("Authorization", "Bearer " + jwt);
		ICreateTyped observationCreateExecutable = client.create().resource(observation).withAdditionalHeader("Authorization", "Bearer " + jwt);
		IBaseResource actualResource = patientReadExecutable.execute();
		// ACT
		MethodOutcome outcome=observationCreateExecutable.execute();

		// ASSERT
		assertEquals(expectedResource.getIdElement().getIdPart(), actualResource.getIdElement().getIdPart());
		assertTrue(outcome.getCreated());

		// AFTER
		observationResourceDao.delete(outcome.getResource().getIdElement());
		patientResourceDao.delete(expectedResource.getIdElement());
	}

	@Test
	void getPatientAsPractitioner_practitionerHasPermissionToRead() throws JSONException {
		// ARRNAGE
		Patient patient = getPatient(PATIENT_ATTRIBUTE);
		Practitioner practitioner = getPractitioner(PRACTITIONER_ATTRIBUTE);
		Reference practitionerReference = new Reference(practitioner.getIdElement());
		patientResourceDao.update(patient.addGeneralPractitioner(practitionerReference));

		String jwt = getJwtToken("practitioner", "practitioner", "patient/*.read user/*.read");

		// ACT
		IReadExecutable<IBaseResource> patientReadExecutable = client.read().resource("Patient").withId(PATIENT_ATTRIBUTE).withAdditionalHeader("Authorization", "Bearer " + jwt);
		IBaseResource actualResource = patientReadExecutable.execute();

		// ASSERT
		assertEquals(patient.getIdElement().getIdPart(), actualResource.getIdElement().getIdPart());

		// AFTER
		patientResourceDao.delete(patient.getIdElement());
		practitionerResourceDao.delete(practitioner.getIdElement());
	}

	@Test
	void modifyPatientAsPractitioner_practitionerHasPermissionToWrite() throws JSONException {
		// ARRNAGE
		Patient patient = getPatient(PATIENT_ATTRIBUTE);
		Reference patientReference = new Reference(patient.getIdElement());
		Practitioner practitioner = getPractitioner(PRACTITIONER_ATTRIBUTE);
		Reference practitionerReference = new Reference(practitioner.getIdElement());
		patient= (Patient) patientResourceDao.update(patient.addGeneralPractitioner(practitionerReference)).getResource();

		String mockName = "foo";
		patient.addName(new HumanName().setText(mockName));

		String jwt = getJwtToken("practitioner", "practitioner", "user/*.write patient/*.write");

		// ACT
		IUpdateExecutable patientUpdateExecutable = client.update().resource(patient).withId(patient.getIdElement()).withAdditionalHeader("Authorization","Bearer "+jwt);
		Patient actualPatient = (Patient) patientUpdateExecutable.execute().getResource();

		// ASSERT
		assertEquals(mockName,actualPatient.getName().get(0).getText());

		// AFTER
		patientResourceDao.delete(actualPatient.getIdElement());
		practitionerResourceDao.delete(practitioner.getIdElement());
	}

	@Test
	void deletePatientAsPractitioner_practitionerHasPermissionToWrite() throws JSONException {
		// ARRNAGE
		Patient patient = getPatient(PATIENT_ATTRIBUTE);
		Practitioner practitioner = getPractitioner(PRACTITIONER_ATTRIBUTE);
		Reference practitionerReference = new Reference(practitioner.getIdElement());
		patient= (Patient) patientResourceDao.update(patient.addGeneralPractitioner(practitionerReference)).getResource();

		String jwt = getJwtToken("practitioner", "practitioner", "user/*.write patient/*.write");

		// ACT
		IDeleteTyped patientDeleteExecutable = client.delete().resource(patient).withAdditionalHeader("Authorization","Bearer "+jwt).cascade(DeleteCascadeModeEnum.DELETE);

		// ASSERT
		assertDoesNotThrow(patientDeleteExecutable::execute);

		// AFTER
		practitionerResourceDao.delete(practitioner.getIdElement());
	}

	@Test
	void addObservationToPatientAsPractitioner_practitionerHasPermissionToWrite() throws JSONException {
		// ARRANGE
		Patient patient = getPatient(PATIENT_ATTRIBUTE);
		Reference patientReference = new Reference(patient.getIdElement());
		Practitioner practitioner = getPractitioner(PRACTITIONER_ATTRIBUTE);
		Reference practitionerReference = new Reference(practitioner.getIdElement());

		patient= (Patient) patientResourceDao.update(patient.addGeneralPractitioner(practitionerReference)).getResource();

		Observation mockObservation = new Observation().setSubject(patientReference);

		String jwt = getJwtToken("practitioner", "practitioner", "patient/*.write user/*.write");

		// ACT
		ICreateTyped observationCreate = client.create().resource(mockObservation).withAdditionalHeader("Authorization", "Bearer " + jwt);
		MethodOutcome outcome = observationCreate.execute();

		// ASSERT
		assertTrue(outcome.getCreated());

		// AFTER
		observationResourceDao.delete(outcome.getResource().getIdElement());
		patientResourceDao.delete(patient.getIdElement());
		practitionerResourceDao.delete(practitioner.getIdElement());
	}


	private Patient getPatient(String id) {
		Patient patient = new Patient();
		patient.setId(id);
		return (Patient) patientResourceDao.update(patient).getResource();
	}

	private Practitioner getPractitioner(String id){
		Practitioner practitioner = new Practitioner();
		practitioner.setId(id);
		return (Practitioner) practitionerResourceDao.update(practitioner).getResource();
	}

	private String getJwtToken(String userName, String password, String scope) throws JSONException {
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> tokenRequestMap = new LinkedMultiValueMap<String, String>() {{
			add("client_id", "smart-on-fhir");
			add("grant_type", "password");
			add("client_secret", "");
			add("username", userName);
			add("password", password);
			add("scope", scope);
		}};

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestMap, headers);

		// ACT
			ResponseEntity<String> response = restTemplate.postForEntity(baseKeycloakUrl+tokenEndpoint, request, String.class);
		JSONObject jsonObject = new JSONObject(response.getBody());


		return (String) jsonObject.get("access_token");
	}


}
