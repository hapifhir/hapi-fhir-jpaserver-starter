package ca.uhn.fhir.jpa.starter;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.gclient.ICreateTyped;
import ca.uhn.fhir.rest.gclient.IReadExecutable;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
class PatientAuthorizationInterceptorTest {

	private IGenericClient client;
	private FhirContext ctx;

	@MockBean
	private JwtDecoder mockJwtDecoder;

	@Autowired
	private IFhirResourceDao<Patient> resourceDao;

	@LocalServerPort
	private int port;

	@BeforeEach
	void setUp() {
		ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ctx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		client = ctx.newRestfulGenericClient(ourServerBase);
	}

	@Test
	void testBuildRules_readPatient_noJwtTokenProvided() {
		// ACT
		IReadExecutable<IBaseResource> patientReadExecutable= client.read().resource("Patient").withId("123");
		ForbiddenOperationException forbiddenOperationException=assertThrows(ForbiddenOperationException.class, patientReadExecutable::execute);

		// ASSERT
		assertEquals("HTTP 403 : Access denied by rule: Deny ALL patient requests if no authorization information is given!", forbiddenOperationException.getMessage());
	}

	@Test
	void testBuildRules_readPatient_jwtTokenContainsOnlyWriteScope() {
		// ARRANGE
		String mockId="123";

		String mockJwtToken = "I.am.JWT";
		String mockHeader = "Bearer " + mockJwtToken;

		HashMap<String, Object> claims = new HashMap<>();
		claims.put("scope", "patient/*.write");
		claims.put("patient", mockId);

		Jwt mockJwt = new Jwt("someValue", Instant.now(), Instant.now().plusSeconds(120), getJwtHeaders(), claims);
		when(mockJwtDecoder.decode(mockJwtToken)).thenReturn(mockJwt);

		// ACT
		IReadExecutable<IBaseResource> patientReadExecutable= client.read().resource("Patient").withId("123").withAdditionalHeader("Authorization", mockHeader);
		ForbiddenOperationException forbiddenOperationException=assertThrows(ForbiddenOperationException.class, patientReadExecutable::execute);

		// ASSERT
		assertEquals("HTTP 403 : Access denied by rule: Deny all requests that do not match any pre-defined rules", forbiddenOperationException.getMessage());
	}

	@ParameterizedTest
	@MethodSource("provideReadClaims")
	void testBuildRules_readPatient_jwtTokenContainsReadScopes_ButNotPatientId(Map<String, Object> claims) {
		// ARRANGE
		String mockJwtToken = "I.am.JWT";
		String mockHeader = "Bearer " + mockJwtToken;

		Jwt mockJwt = new Jwt("someValue", Instant.now(), Instant.now().plusSeconds(120), getJwtHeaders(), claims);
		when(mockJwtDecoder.decode(mockJwtToken)).thenReturn(mockJwt);

		// ACT
		IReadExecutable<IBaseResource> patientReadExecutable= client.read().resource("Patient").withId("123").withAdditionalHeader("Authorization", mockHeader);
		ForbiddenOperationException forbiddenOperationException=assertThrows(ForbiddenOperationException.class, patientReadExecutable::execute);

		// ASSERT
		assertEquals("HTTP 403 : Access denied by rule: Deny ALL patient requests if no patient id is passed!", forbiddenOperationException.getMessage());
		}

	@ParameterizedTest
	@MethodSource("provideReadClaims")
	void testBuildRules_readPatient_providedJwtContainsReadScopes_AndPatientId(Map<String, Object> claims) {
		// ARRANGE
		IBaseResource mockPatient=resourceDao.create(new Patient()).getResource();
		String mockId=mockPatient.getIdElement().getIdPart();

		String mockJwtToken = "I.am.JWT";
		String mockHeader = "Bearer " + mockJwtToken;

		claims.put("patient", mockId);

		Jwt mockJwt = new Jwt("someValue", Instant.now(), Instant.now().plusSeconds(120), getJwtHeaders(), claims);
		when(mockJwtDecoder.decode(mockJwtToken)).thenReturn(mockJwt);

		// ACT
		IReadExecutable<IBaseResource> patientReadExecutable= client.read().resource("Patient").withId(mockId).withAdditionalHeader("Authorization", mockHeader);
		IBaseResource patient=patientReadExecutable.execute();

		// ASSERT
		assertEquals(mockPatient.getIdElement().getIdPart(), patient.getIdElement().getIdPart());
	}

	@ParameterizedTest
	@MethodSource("provideWriteClaims")
	void testBuildRules_createPatient_providedJwtContainsWriteScopes_AndPatientId(Map<String, Object> claims) {
		// ARRANGE
		IBaseResource mockPatient=resourceDao.create(new Patient()).getResource();
		String mockId=mockPatient.getIdElement().getIdPart();

		String mockJwtToken = "I.am.JWT";
		String mockHeader = "Bearer " + mockJwtToken;

		claims.put("patient", mockId);

		Jwt mockJwt = new Jwt("someValue", Instant.now(), Instant.now().plusSeconds(120), getJwtHeaders(), claims);
		when(mockJwtDecoder.decode(mockJwtToken)).thenReturn(mockJwt);
		// ACT

		ICreateTyped patientUpdateExecutable= client.create().resource(new Patient()).withAdditionalHeader("Authorization", mockHeader);
		MethodOutcome patient=patientUpdateExecutable.execute();

		// ASSERT
		System.out.println(patient);
	}




	private static Stream<Arguments> provideReadClaims(){
		return Stream.of(
			Arguments.of(
				new HashMap<String, String>() {{
					put("scope", "patient/*.read");
				}}
		),
			Arguments.of(
				new HashMap<String, String>() {{
					put("scope", "patient/*.*");
				}}
			)
		);
	}

	private static Stream<Arguments> provideWriteClaims(){
		return Stream.of(
			Arguments.of(
				new HashMap<String, String>() {{
					put("scope", "patient/*.write patient/*.read");

				}}
			),
			Arguments.of(
				new HashMap<String, String>() {{
					put("scope", "patient/*.*");
				}}
			)
		);
	}


	private Map<String, Object> getJwtHeaders() {
		Map<String, Object> jwtHeaders = new HashMap<>();
		jwtHeaders.put("kid", "rand");
		jwtHeaders.put("typ", "JWT");
		jwtHeaders.put("alg", "RS256");
		return jwtHeaders;
	}

}
