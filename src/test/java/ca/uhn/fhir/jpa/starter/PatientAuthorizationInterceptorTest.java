package ca.uhn.fhir.jpa.starter;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
class PatientAuthorizationInterceptorTest {

	@MockBean
	private JwtDecoder mockJwtDecoder;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void testGetPatientPermissions_noJwtTokenProvided(){
		// ACT
		ResponseEntity<String> entity=restTemplate.getForEntity("/fhir/Patient/123",String.class);
		// ASSERT
		assertEquals(HttpStatus.FORBIDDEN, entity.getStatusCode());
	}

	@Test
	void testGetPatientPermissions_jwtTokenProvidedDoesNotContainPatientReadPermissions(){
		// ARRANGE
		String mockJwtToken = "I.am.JWT";
		String mockHeader = "Bearer " + mockJwtToken;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", mockHeader);

		HashMap<String, Object> claims = new HashMap<>();
		claims.put("scope","patient/*.read");
		Jwt mockJwt = new Jwt("someValue", Instant.now(), Instant.now().plusSeconds(120),getJwtHeaders(), claims);
		when(mockJwtDecoder.decode(mockJwtToken)).thenReturn(mockJwt);

		// ACT
		ResponseEntity<String> entity=restTemplate.getRestTemplate().getForEntity("/fhir/Patient/123",String.class, headers);
		// ASSERT
		assertEquals(HttpStatus.OK, entity.getStatusCode());
	}

	@Test
	void testGetPatientPermissions_providedJwtContainsPatientReadPermissions(){
		// ARRANGE
		String mockJwtToken = "I.am.JWT";
		String mockHeader = "Bearer " + mockJwtToken;

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", mockHeader);

		HashMap<String, Object> claims = new HashMap<>();
		claims.put("scope","patient/*.read");
		claims.put("patient","123");
		Jwt mockJwt = new Jwt("someValue", Instant.now(), Instant.now().plusSeconds(120),getJwtHeaders(), claims);
		when(mockJwtDecoder.decode(mockJwtToken)).thenReturn(mockJwt);

		// ACT
		ResponseEntity<String> entity=restTemplate.exchange(
			"/fhir/Patient/123", HttpMethod.GET, new HttpEntity<>(null, headers),
			String.class);;

		// ASSERT
		assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
	}

//	@Test
//	void testGetPatientPermissions_jwtTokenProvidedDoesNotContainPatientReadPermissions(){
//		// ACT
//		ResponseEntity<String> entity=restTemplate.getForEntity("/fhir/Patient/123",String.class);
//		// ASSERT
//		assertEquals(HttpStatus.FORBIDDEN, entity.getStatusCode());
//	}


	private Map<String, Object> getJwtHeaders(){
		Map<String, Object> jwtHeaders = new HashMap<>();
		jwtHeaders.put("kid", "rand");
		jwtHeaders.put("typ", "JWT");
		jwtHeaders.put("alg", "RS256");
		return jwtHeaders;
	}

}
