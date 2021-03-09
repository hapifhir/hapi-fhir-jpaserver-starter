package ca.uhn.fhir.jpa.starter;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public class OAuth2Helper {
	private static final Logger logger = LoggerFactory.getLogger(OAuth2Helper.class);
	private static final String CLIENT = System.getenv("OAUTH_CLIENT_NAME");

	protected String getJwtKeyId(String token) {
		String tokenHeader = token.split("\\.")[0];
		tokenHeader = new String(Base64.getDecoder().decode(tokenHeader.getBytes()));
		String kid = null;
		try {
			JSONObject obj = new JSONObject(tokenHeader);
			kid = obj.getString("kid");
		} catch (JSONException e) {
			logger.error("Unexpected exception parsing JWT token", e);
		}
		return kid;
	}

	// The Base64 strings that come from a JWKS need some manipilation before they
	// can be decoded, so we do that here
	protected byte[] base64Decode(String base64) throws IOException {
		base64 = base64.replaceAll("-", "+");
		base64 = base64.replaceAll("_", "/");
		switch (base64.length() % 4) // Pad with trailing '='s
		{
		case 0:
			break; // No pad chars in this case
		case 2:
			base64 += "==";
			break; // Two pad chars
		case 3:
			base64 += "=";
			break; // One pad char
		default:
			throw new RuntimeException("Illegal base64url string!");
		}
		return Base64.getDecoder().decode(base64);
	}

	protected JWTVerifier getJWTVerifier(DecodedJWT jwt, PublicKey publicKey) {
		Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
		JWTVerifier verifier = JWT.require(algorithm).withIssuer(jwt.getIssuer()).build();
		return verifier;
	}

	protected PublicKey getJwtPublicKey(String kid, String oauthUrl) {
		PublicKey publicKey = null;
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> exchange = restTemplate.exchange(oauthUrl + "/certs", HttpMethod.GET, null,
				String.class);

		String response = exchange.getBody();
		try {
			String modulusStr = null;
			String exponentStr = null;
			JSONObject obj = new JSONObject(response);
			JSONArray keylist = obj.getJSONArray("keys");
			for (int i = 0; i < keylist.length(); i++) {
				JSONObject key = keylist.getJSONObject(i);
				String id = key.getString("kid");
				if (kid.equals(id)) {
					modulusStr = key.getString("n");
					exponentStr = key.getString("e");
				}
			}

			BigInteger modulus = new BigInteger(1, base64Decode(modulusStr));
			BigInteger publicExponent = new BigInteger(1, base64Decode(exponentStr));

			try {
				KeyFactory kf = KeyFactory.getInstance("RSA");
				return kf.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} catch (JWTVerificationException e) {
			logger.warn("Authorization failed - unable to verify token");
		} catch (Exception e) {
			logger.error("Unexpected error verifying OAuth2 token", e);
		}

		return publicKey;
	}
	
	protected ArrayList<String> getRoles(DecodedJWT jwt) {
		Claim claim = jwt.getClaim("resource_access");
		HashMap<String, HashMap<String, ArrayList<String>>> resources = claim.as(HashMap.class);
		HashMap<String, ArrayList<String>> clientMap = resources.getOrDefault(CLIENT, new HashMap<String, ArrayList<String>>());
		ArrayList<String> roles = clientMap.getOrDefault("roles", new ArrayList<String>());
		return roles;
	}

}