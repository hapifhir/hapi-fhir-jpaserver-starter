package ca.uhn.fhir.jpa.starter.util;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;

public class OAuth2Helper {
	private static final String BEARER_PREFIX = "BEARER ";
	private static RSAPublicKey publicKey = null;

	private OAuth2Helper() {}

	public static String getToken(RequestDetails theRequest) {
		Validate.notNull(theRequest, "theRequest must not be null");
		String auth = theRequest.getHeader(HttpHeaders.AUTHORIZATION);
		return auth.substring(BEARER_PREFIX.length());
	}

	public static void verify(DecodedJWT jwt, String jwksUrl) throws IllegalArgumentException,
			NoSuchAlgorithmException, InvalidKeySpecException, TokenExpiredException, JWTVerificationException {
		Validate.notNull(jwt, "jwt must not be null");
		Validate.notBlank(jwksUrl, "jwksUrl must not be blank");
		// TODO: storage of cached public keys need to be a map by kid. there may be more than 1.
		// TODO: Add an expiration for the caching of the key
		// 	// TODO: JwkProvider??
		// 	// https://github.com/auth0/java-jwt/blob/master/EXAMPLES.md
		if (publicKey == null) {
			publicKey = getKey(jwt.getKeyId(), jwksUrl);
		}
		Algorithm algorithm = getAlgorithm(jwt, publicKey);
		JWTVerifier verifier = JWT.require(algorithm).build();
		verifier.verify(jwt);
	}

	public static RSAPublicKey getKey(String kid, String jwksUrl) throws RestClientException,
			NoSuchAlgorithmException, InvalidKeySpecException {
		Validate.notBlank(kid, "kid must not be blank");
		Validate.notBlank(jwksUrl, "jwksUrl must not be blank");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(jwksUrl, String.class);
		String rawE = null;
		String rawN = null;
		JSONObject jwks = new JSONObject(response.getBody());
		JSONArray keys = jwks.getJSONArray("keys");
		for (int i = 0; i < keys.length(); i++) {
			JSONObject key = keys.getJSONObject(i);
			if (kid.equals(key.get("kid"))) {
				rawE = key.getString("e");
				rawN = key.getString("n");
				break;
			}
		}
		BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(rawE));
		BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(rawN));
		KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
		return (RSAPublicKey)kf.generatePublic(publicKeySpec);
	}

	public static List<String> getClientRoles(DecodedJWT jwt, String clientId) {
		Validate.notNull(jwt, "jwt must not be null");
		Validate.notBlank(clientId, "clientId must not be blank");
		Claim claim = jwt.getClaim("resource_access");
		HashMap<String, HashMap<String, ArrayList<String>>> resources = claim.as(HashMap.class);
		HashMap<String, ArrayList<String>> clientMap = resources.getOrDefault(clientId, new HashMap<String, ArrayList<String>>());
		return clientMap.getOrDefault("roles", new ArrayList<String>());
	}

	public static String getClaimAsString(RequestDetails theRequest, String name) {
		Validate.notNull(theRequest, "theRequest must not be null");
		Validate.notBlank(name, "name must not be blank");
		String token = getToken(theRequest);
		DecodedJWT jwt = JWT.decode(token);
		return getClaimAsString(jwt, name);
	}

	public static String getClaimAsString(DecodedJWT jwt, String name) {
		Validate.notNull(jwt, "jwt must not be null");
		Validate.notBlank(name, "name must not be blank");
		return jwt.getClaim(name).asString();
	}

	public static boolean canBeInPatientCompartment(String resourceName) {
		Validate.notBlank(resourceName, "resourceName must not be blank");
		/*
		 * For Bundle Request resourceType would be null.
		 * For now we allow all bundle operations this will apply normal rules from authorization intercepter
		 */
		if (ObjectUtils.isEmpty(resourceName)) {
			return true;
		}
		FhirContext ctx = FhirContext.forR4();
		RuntimeResourceDefinition data = ctx.getResourceDefinition(resourceName);
		List<RuntimeSearchParam> compartmentList = data.getSearchParamsForCompartmentName("Patient");
		return !compartmentList.isEmpty();
	}

	public static boolean hasToken(RequestDetails theRequest) {
		Validate.notNull(theRequest, "theRequest must not be null");
		String token = theRequest.getHeader(HttpHeaders.AUTHORIZATION);
		return (!ObjectUtils.isEmpty(token) && token.toUpperCase().startsWith(BEARER_PREFIX));
	}

	private static Algorithm getAlgorithm(DecodedJWT jwt, Object publicKey) throws NoSuchAlgorithmException {
		Validate.notNull(jwt, "jwt must not be null");
		Validate.notNull(publicKey, "publicKey must not be null");
		String alg = jwt.getAlgorithm();
		switch (alg) {
			case "HS256":
				return Algorithm.HMAC256((String)publicKey);
			case "HS384":
				return Algorithm.HMAC384((String)publicKey);
			case "HS512":
				return Algorithm.HMAC512((String)publicKey);
			case "RS256":
				return Algorithm.RSA256((RSAPublicKey)publicKey, null);
			case "RS384":
				return Algorithm.RSA384((RSAPublicKey)publicKey, null);
			case "RS512":
				return Algorithm.RSA512((RSAPublicKey)publicKey, null);
			case "ES256":
				return Algorithm.ECDSA256((ECPublicKey)publicKey, null);
			case "ES384":
				return Algorithm.ECDSA384((ECPublicKey)publicKey, null);
			case "ES512":
				return Algorithm.ECDSA512((ECPublicKey)publicKey, null);
			case "PS256":
			case "PS384":
			default:
				throw new NoSuchAlgorithmException("Algorithm is not supported by this library.");
		}
	}
}
