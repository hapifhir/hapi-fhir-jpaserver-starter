package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.gclient.ICreateTyped;
import ca.uhn.fhir.rest.gclient.IReadExecutable;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static ca.uhn.fhir.jpa.starter.PatientAuthorizationInterceptorTest.ACCESS_DENIED_DUE_TO_SCOPE_RULE_EXCEPTION_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
class KeycloakSmartIT {

	private static int keyCloakPort;
	private static String keyCloakUrl;

	@LocalServerPort
	private int port;

	@Autowired
	private IFhirResourceDao<Patient> patientResourceDao;

	@Autowired
	DaoConfig daoConfig;

	private IGenericClient client;
	private FhirContext ctx;

	@BeforeAll
	static void setUp() {
//		FhirStarterTestContainers.getPostgreSQLContainer();
//		keyCloakUrl = "http://localhost:"+FhirStarterTestContainers.getKeycloakContainer().getMappedPort(8080)+"/auth/realms/smart/protocol/openid-connect/token";
		keyCloakUrl = "http://localhost:8081/auth/realms/smart/protocol/openid-connect/token";

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
	void getPatientAsNurse_nurseHasPermissionsToViewPatient() throws JSONException {
		// ARRNAGE
		String mockId = "101";
		Patient expectedResource = getPatient(mockId);

		String jwt = getJwtToken("nurse", "nurse", "patient/*.read");

		IReadExecutable<IBaseResource> patientReadExecutable = client.read().resource("Patient").withId(mockId).withAdditionalHeader("Authorization", "Bearer " + jwt);
		IBaseResource actualResource = patientReadExecutable.execute();

		// ASSERT
		assertEquals(expectedResource.getIdElement().getIdPart(), actualResource.getIdElement().getIdPart());
		patientResourceDao.delete(expectedResource.getIdElement());
	}

	@Test
	void getPatientAsNurse_wrongPatientId() throws JSONException {
		// ARRNAGE
		String mockId = "6";
		Patient expectedResource = getPatient(mockId);

		String jwt = getJwtToken("nurse", "nurse", "patient/*.read");

		IReadExecutable<IBaseResource> patientReadExecutable = client.read().resource("Patient").withId(mockId).withAdditionalHeader("Authorization", "Bearer " + jwt);

		// ACT
		ForbiddenOperationException forbiddenOperationException = assertThrows(ForbiddenOperationException.class, patientReadExecutable::execute);

		// ASSERT
		assertEquals(ACCESS_DENIED_DUE_TO_SCOPE_RULE_EXCEPTION_MESSAGE, forbiddenOperationException.getMessage());
		patientResourceDao.delete(expectedResource.getIdElement());
	}


	@Test
	void getPatientAsNurse_nurseDoesNotHasPermissionToWrite() throws JSONException {
		// ARRNAGE
		Patient updatedResource = getPatient("101");

		String jwt = getJwtToken("nurse", "nurse", "patient/*.read");
		Observation observation = new Observation();
		observation.setSubject(new Reference(updatedResource.getIdElement()));

		ICreateTyped observationCreateExecutable = client.create().resource(observation).withAdditionalHeader("Authorization", "Bearer " + jwt);

		// ACT
		ForbiddenOperationException forbiddenOperationException = assertThrows(ForbiddenOperationException.class, observationCreateExecutable::execute);


		// ASSERT
		assertEquals(ACCESS_DENIED_DUE_TO_SCOPE_RULE_EXCEPTION_MESSAGE, forbiddenOperationException.getMessage());


		patientResourceDao.delete(updatedResource.getIdElement());
	}

	@Test
	void getPatientAsNurse_nurseHasPermissionToWrite() throws JSONException {
		// ARRNAGE
		Patient updatedResource = getPatient("101");

		String jwt = getJwtToken("nurse", "nurse", "patient/*.write");
		Observation observation = new Observation();
		observation.setSubject(new Reference(updatedResource.getIdElement()));

		ICreateTyped observationCreateExecutable = client.create().resource(observation).withAdditionalHeader("Authorization", "Bearer " + jwt);

		// ACT
		MethodOutcome outcome=observationCreateExecutable.execute();

		// ASSERT
		assertTrue(outcome.getCreated());
	}

	@Test
	void getPatientAsNurse_nurseHasAllPermissions() throws JSONException {
		// ARRNAGE
		Patient expectedResource = getPatient("101");

		String jwt = getJwtToken("nurse", "nurse", "patient/*.*");
		Observation observation = new Observation();
		observation.setSubject(new Reference(expectedResource.getIdElement()));

		IReadExecutable<IBaseResource> patientReadExecutable = client.read().resource("Patient").withId("101").withAdditionalHeader("Authorization", "Bearer " + jwt);
		ICreateTyped observationCreateExecutable = client.create().resource(observation).withAdditionalHeader("Authorization", "Bearer " + jwt);
		IBaseResource actualResource = patientReadExecutable.execute();
		// ACT
		MethodOutcome outcome=observationCreateExecutable.execute();

		// ASSERT
		assertEquals(expectedResource.getIdElement().getIdPart(), actualResource.getIdElement().getIdPart());
		assertTrue(outcome.getCreated());
	}



	private Patient getPatient(String id) {
		Patient patient = new Patient();
		patient.setId(id);
		return (Patient) patientResourceDao.update(patient).getResource();
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
		ResponseEntity<String> response = restTemplate.postForEntity(keyCloakUrl, request, String.class);
		JSONObject jsonObject = new JSONObject(response.getBody());


		return (String) jsonObject.get("access_token");
	}


}
