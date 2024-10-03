package ca.uhn.fhir.jpa.starter.services;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.r5.model.UuidType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Service
public class CommonServices {
	private static final Logger log = LoggerFactory.getLogger(CommonServices.class);

	private final AppProperties appProperties;

	public CommonServices(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	/**
	 *
	 * @param oldPseudonym
	 *    Pseudonym that is given to exchange service
	 * @return
	 *    Pseudonym that is returned by exchange service
	 * @throws Exception
	 * 	Throws exception when HttpClient or parsing fails or when status code is not 200
	 */
	public UuidType exchangePseudonym(UuidType oldPseudonym) throws Exception {
		log.info("Exchanging pseudonym {}", oldPseudonym.getValue());
		String sourcePseudonym = oldPseudonym.getValue();
		String endpoint = appProperties.getPseudonymExchangeService().getEndpoint();
		String targetProviderId = appProperties.getPseudonymExchangeService().getTargetProviderId();

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		// Manually create the JSON string
		String json = "{"
			+ "\"target_provider_id\": \"" + targetProviderId + "\","
			+ "\"source_pseudonym\": \"" + sourcePseudonym + "\""
			+ "}";

		HttpPost request = buildRequest(endpoint + "/exchange", json);

		CloseableHttpResponse response = httpClient.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			log.error("Failed to exchange pseudonym");
			throw new Exception(EntityUtils.toString(response.getEntity()));
		}
		String responseString = EntityUtils.toString(response.getEntity());
		// Regex to retrieve only the UUID
		String pseudonym = responseString.replaceAll(".*\"pseudonym\"\\s*:\\s*\"(.*?)\".*", "$1");
		log.info("Exchanged pseudonym {}", pseudonym);
		httpClient.close(); // Ensure the client is closed
		return new UuidType(pseudonym);
	}

	/**
	 *
	 * @return
	 *    Pseudonym that is returned by register service
	 * @throws Exception
	 * 	Throws exception when HttpClient or parsing fails or when status code is not 200
	 */
	public String registerPseudonym() throws Exception {
		log.info("Registering pseudonym");
		String bsnHash = "15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225";
		String endpoint = appProperties.getPseudonymExchangeService().getEndpoint();
		String targetProviderId = appProperties.getPseudonymExchangeService().getTargetProviderId();

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		// Manually create the JSON string
		String json = "{"
			+ "\"provider_id\": \"" + targetProviderId + "\","
			+ "\"bsn_hash\": \"" + bsnHash + "\""
			+ "}";

		HttpPost request = buildRequest(endpoint + "/register", json);

		CloseableHttpResponse response = httpClient.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			log.error("Failed to register pseudonym");
			throw new Exception(EntityUtils.toString(response.getEntity()));
		}
		String responseString = EntityUtils.toString(response.getEntity());
		// Regex to retrieve only the UUID
		String pseudonym = responseString.replaceAll(".*\"pseudonym\"\\s*:\\s*\"(.*?)\".*", "$1");
		log.info("Registered pseudonym {}", pseudonym);
		httpClient.close(); // Ensure the client is closed
		return pseudonym;
	}

	/**
	 *
	 * @param pseudonym
	 *    Pseudonym that is given to the referral service
	 * @throws Exception
	 * 	Throws exception when HttpClient or parsing fails or when status code is not 200
	 */
	public void createReferral(String pseudonym) throws Exception {
		log.info("Creating referral for pseudonym {}", pseudonym);
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String endpoint = appProperties.addReferralService().getEndpoint();

		// Manually create the JSON string
		String json = "{"
			+ "\"pseudonym\": \"" + pseudonym + "\","
			+ "\"data_domain\": \"beeldbank\","
			+ "\"ura_number\": \"12345678\","
			+ "\"requesting_uzi_number\": \"iets\""
			+ "}";

		HttpPost request = buildRequest(endpoint, json);
		CloseableHttpResponse response = httpClient.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			log.error("Failed to create referral for pseudonym");
			throw new Exception(EntityUtils.toString(response.getEntity()));
		}
		httpClient.close(); // Ensure the client is closed
	}

	/**
	 *
	 * @param uri
	 *    Uri of endpoint to reach
	 * @param body
	 * 	JSON body of the request
	 * @return
	 *    The created HttpPost request
	 * @throws UnsupportedEncodingException
	 * 	Throws exception when parsing the body into a StringEntity fails
	 */
	private HttpPost buildRequest(String uri, String body) throws UnsupportedEncodingException {
		HttpPost request = new HttpPost(uri);
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-Type", "application/json");
		StringEntity params = new StringEntity(body);
		request.setEntity(params);
		return request;
	}
}
