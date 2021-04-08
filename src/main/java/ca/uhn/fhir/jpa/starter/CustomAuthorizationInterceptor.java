package ca.uhn.fhir.jpa.starter;

import java.security.PublicKey;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

@Interceptor
public class CustomAuthorizationInterceptor extends AuthorizationInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(CustomAuthorizationInterceptor.class);
	private static final String OAUTH_URL = System.getenv("OAUTH_URL");
	private static final String OAUTH_ENABLED = System.getenv("OAUTH_ENABLED");
	private static final String APIKEY_ENABLED = System.getenv("APIKEY_ENABLED");
	private static final String APIKEY_HEADER = "x-api-key";
	private static final String APIKEY = System.getenv("APIKEY");
	private static final String TOKEN_PREFIX = "BEARER ";
	private static final String OAUTH_USER_ROLE = System.getenv("OAUTH_USER_ROLE");
	private static final String OAUTH_CLIENT_ID = System.getenv("OAUTH_CLIENT_ID");
	private static final String OAUTH_ADMIN_ROLE = System.getenv("OAUTH_ADMIN_ROLE");
	private static PublicKey publicKey = null;
	private static OAuth2Helper oAuth2Helper = new OAuth2Helper();

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequest) {
		try {

			if (theRequest.getRequestPath().equals(RestOperationTypeEnum.METADATA.getCode())) {
				return allowAll();
			}

			if (!isOAuthEnabled() && !isApiKeyEnabled()) {
				logger.warn("APIKEY and OAuth2 authentication are disabled");
				return allowAll();
			}

			if (isOAuthEnabled() && isOAuthHeaderPresent(theRequest)) {
				logger.info("Auhorizing via OAuth");
				return authorizeOAuth(theRequest);
			}

			if (isApiKeyEnabled() && isApiKeyHeaderPresent(theRequest)) {
				logger.info("Auhorizing via X-API-KEY");
				return authorizeApiKey(theRequest);
			}
		} catch (Exception e) {
			logger.info("Unexpected authorization error", e);
			return denyAll();
		}

		logger.warn("Authorization failure - fall through");
		return denyAll();
	}

	private List<IAuthRule> denyAll() {
		return new RuleBuilder().denyAll().build();
	}

	private List<IAuthRule> allowAll() {
		return new RuleBuilder().allowAll().build();
	}

	private List<IAuthRule> authorizeOAuth(RequestDetails theRequest) throws Exception {
		String token = theRequest.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.isEmpty(token)) {
			logger.info("Authorization failure - missing authorization header");
			return denyAll();
		}

		if (!token.toUpperCase().startsWith(TOKEN_PREFIX)) {
			logger.info("Authorization failure - invalid authorization header");
			return denyAll();
		}

		token = token.substring(TOKEN_PREFIX.length());

		try {
			DecodedJWT jwt = JWT.decode(token);
			String kid = oAuth2Helper.getJwtKeyId(token);
			publicKey = StringUtils.isEmpty(publicKey) ? oAuth2Helper.getJwtPublicKey(kid, OAUTH_URL) : publicKey;
			JWTVerifier verifier = oAuth2Helper.getJWTVerifier(jwt, publicKey);
			jwt = verifier.verify(token);
			
			if (theRequest.getRequestType().equals(RequestTypeEnum.DELETE)) {
				if (oAuth2Helper.hasClientRole(jwt, OAUTH_CLIENT_ID, OAUTH_ADMIN_ROLE)) {
					return allowAll();
				}
			} else if (oAuth2Helper.hasClientRole(jwt, OAUTH_CLIENT_ID, OAUTH_USER_ROLE)) {
				return allowAll();
			}
		} catch (TokenExpiredException e) {
			logger.info("Authorization failure - token has expired");
		} catch (Exception e) {
			logger.info("Unexpected exception verifying token", e);
		}

		logger.info("Authentication failure");
		return denyAll();
	}

	private Boolean isOAuthEnabled() {
		return ((OAUTH_ENABLED != null) && Boolean.parseBoolean(OAUTH_ENABLED));
	}

	private Boolean isOAuthHeaderPresent(RequestDetails theRequest) {
		String token = theRequest.getHeader(HttpHeaders.AUTHORIZATION);
		return (!StringUtils.isEmpty(token));
	}
	
	private Boolean isApiKeyEnabled() {
		return ((APIKEY_ENABLED != null) && Boolean.parseBoolean(APIKEY_ENABLED));
	}

	private Boolean isApiKeyHeaderPresent(RequestDetails theRequest) {
		String apiKey = theRequest.getHeader(APIKEY_HEADER);
		return (!StringUtils.isEmpty(apiKey));
	}

	private List<IAuthRule> authorizeApiKey(RequestDetails theRequest) {
		String apiKey = theRequest.getHeader(APIKEY_HEADER);
		if (StringUtils.isEmpty(apiKey)) {
			logger.info("Authorization failure - missing X-API-KEY header");
			return denyAll();
		}

		if (apiKey.equals(APIKEY)) {
			return allowAll();
		}

		logger.info("Authorization failure - invalid X-API-KEY header");
		return denyAll();
	}

	public static String getTokenPrefix() {
		return TOKEN_PREFIX;
	}
}
