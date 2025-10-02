package ca.uhn.fhir.jpa.starter.auth.service;

import ca.uhn.fhir.jpa.starter.auth.models.AuthApiResponse;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class AuthRestTemplate {

	private final RestTemplate restTemplate;

	public AuthRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public AuthApiResponse verifyToken(String url) throws RestClientException {
		return restTemplate.getForObject(url, AuthApiResponse.class);
	}
}
