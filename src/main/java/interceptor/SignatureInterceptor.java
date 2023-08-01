package interceptor;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.iprd.fhir.utils.FhirUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.iprd.fhir.utils.Validation;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.model.JWTPayload;

@Component
public class SignatureInterceptor extends GenericFilterBean{

	private final AppProperties appProperties;
	private Map<String, byte[]> publicKeyCache = new HashMap<>(); // Local cache to store public keys

	public SignatureInterceptor(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		if(
			httpServletRequest.getRequestURI().contains("/iprd/user/") ||
			httpServletRequest.getRequestURI().contains("/actuator/health")
			)
		{
			chain.doFilter(request, response);
			return;
		} 
		//Get the signature from the header
		String signatureHeader = httpServletRequest.getHeader("Signature");
		//Get the token from the header
		String token = httpServletRequest.getHeader("Authorization");
		//Get the timestamp from the header
		String timeStampHeader = httpServletRequest.getHeader("Timestamp");
		//Get the Key I'd from the header
		String keyId = httpServletRequest.getHeader("kid");
		JWTPayload tokenPayload= Validation.getJWTToken(token);
		if(tokenPayload == null) {
			httpServletResponse.setHeader("error-message", "Token parse failure");
			httpServletResponse.setStatus(401);
			return;
		}
		String practitionerRoleId = tokenPayload.getPractitionerRoleId();
		String dashboardKeyId = FhirUtils.KeyId.DASHBOARD.name();
		if (practitionerRoleId != null) {
			//Get user role from the JWT token
			 List<String> userRole =  tokenPayload.getClientRoles();
			//If admin then skip the signature process and allow for all the api calls (for dev user)
			String devUserRole = appProperties.getDev_user_role();
			//For backward compatibility of no role and not logged in. Will allow user to login to app to get a new token
			String contextPath = httpServletRequest.getRequestURI();
			if(userRole==null) {
				//This is odd, unless mapper is not set
				httpServletResponse.setHeader("error-message", "User role is null");
				httpServletResponse.setStatus(401);
				return;
			}
			if(!userRole.contains(devUserRole)){
				if (signatureHeader == null || signatureHeader.isEmpty()) {
					httpServletResponse.setHeader("error-message", "Missing Signature header");	
					httpServletResponse.setStatus(400);
					return;
				}
				try {
					boolean isVerified = getPublicKeyAndVerify(signatureHeader, token, timeStampHeader, keyId);
					if (!isVerified) {
						httpServletResponse.setHeader("error-message", "Invalid Signature "+signatureHeader);
						httpServletResponse.setStatus(401);
						return;
					}
				} catch (Exception e) {
					httpServletResponse.setHeader("error-message", "Issue in Key");
					httpServletResponse.setStatus(401);
					logger.warn(ExceptionUtils.getStackTrace(e));
					return;
				}
			}
		}

		// Add a check to avoid DELETE and PUT API calls when keyId is "Dashboard"
		if (dashboardKeyId.equals(keyId) && (httpServletRequest.getMethod().equals("DELETE") || httpServletRequest.getMethod().equals("PUT") || httpServletRequest.getMethod().equals("POST"))) {
			httpServletResponse.setHeader("error-message", "Invalid API call");
			httpServletResponse.setStatus(403); // Forbidden status code
			return;
		}

		chain.doFilter(request, response);
	}

	public byte[] readPublicKeyFile(String keyId) throws IOException {
		FhirUtils.KeyId keyType = FhirUtils.KeyId.valueOf(keyId);
		if (publicKeyCache.containsKey(keyId)) {
		// Return the cached public key if available
			return publicKeyCache.get(keyId);
		} else {
		// Read the public key file and store it in cache
			byte[] publicKey;
			switch (keyType) {
				case APPCLIENT:
					publicKey = Base64.getDecoder().decode(new String(Files.readAllBytes(Paths.get(appProperties.getPk_file()))));
					break;
				case DASHBOARD:
					publicKey = Base64.getDecoder().decode(new String(Files.readAllBytes(Paths.get(appProperties.getDashboard_public_key_file()))));
					break;
				default:
					return null; // Return null for unknown keyId
			}
			publicKeyCache.put(keyId, publicKey); // Store the public key in cache
			return publicKey;
		}
	}

	private Boolean getPublicKeyAndVerify(String signatureHeader, String token, String timeStampHeader, String keyId) throws Exception {
		long currentTimestamp = Instant.now().getEpochSecond();
		long receivedTimeStamp = Long.parseLong(timeStampHeader);
		long timeDifference = Math.abs(currentTimestamp - receivedTimeStamp);
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		//check if the timestamp is within 10 minutes of the current timestamp
		try {
			if(timeDifference <= appProperties.getApi_request_max_time()){
				String messageToVerify = practitionerRoleId.concat(timeStampHeader);
				//get the decoded public key in bytes
				byte[] publicKeyByte = readPublicKeyFile(keyId);
				//get the decoded signature key in bytes
				String urlDecoded = java.net.URLDecoder.decode(signatureHeader,StandardCharsets.UTF_8.name());
				byte[] decodedSignature = Base64.getDecoder().decode(urlDecoded.getBytes(StandardCharsets.UTF_8.name()));
				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByte);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				PublicKey publicKey = keyFactory.generatePublic(keySpec);
				Signature signature = Signature.getInstance("SHA256withRSA");
				//init public key to verify the signature
				signature.initVerify(publicKey);
				//update the signature with the message
				signature.update(messageToVerify.getBytes());
				Boolean verified = signature.verify(decodedSignature);
				if(!verified) {
					logger.warn(signatureHeader +" "+ token+" "+timeStampHeader+" "+keyId);
				}
				return verified;
			} else{
				logger.warn(signatureHeader +" "+ token+" "+timeStampHeader+" "+keyId);
				return false;
			}	
		}catch(Exception e) {
			return false;
		}
	}
}
