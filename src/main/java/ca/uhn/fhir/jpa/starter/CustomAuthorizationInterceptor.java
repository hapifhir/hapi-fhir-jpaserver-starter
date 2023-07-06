package ca.uhn.fhir.jpa.starter;

import java.security.PublicKey;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private AppProperties config;
	private static final String APIKEY_HEADER = "x-api-key";
	private static PublicKey publicKey = null;
	private static OAuth2Helper oAuth2Helper = new OAuth2Helper();
	private static BasicAuthHelper basicAuthHelper = new BasicAuthHelper();

	public CustomAuthorizationInterceptor(AppProperties config) {
		super();
		this.config = config;
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequest) {
		try {

			if (theRequest.getRequestPath().equals(RestOperationTypeEnum.METADATA.getCode())) {
				return allowAll();
			}

			if (!isOAuthEnabled() && !isApiKeyEnabled() && !isBasicAuthEnabled()) {
				logger.warn("APIKEY, basicAuth and OAuth2 authentication are disabled");
				return allowAll();
			}

			if (isOAuthEnabled() && oAuth2Helper.isOAuthHeaderPresent(theRequest)) {
				logger.info("Authorizing via OAuth");
				return authorizeOAuth(theRequest);
			}

			if (isApiKeyEnabled() && isApiKeyHeaderPresent(theRequest)) {
				logger.info("Authorizing via X-API-KEY");
				return authorizeApiKey(theRequest);
			}
			if (isBasicAuthEnabled() && isBasicAuthHeaderPresent(theRequest)) {
				logger.info("Authorizing via basic auth");
				return authorizeBasicAuth(theRequest);
			}
		} catch (Exception e) {
			logger.warn("Unexpected authorization error :{}", e.getMessage());
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
		String token = OAuth2Helper.getToken(theRequest);
		try {
			DecodedJWT jwt = JWT.decode(token);
			String kid = oAuth2Helper.getJwtKeyId(token);
			if (ObjectUtils.isEmpty(publicKey)) {
				publicKey = oAuth2Helper.getJwtPublicKey(kid, config.getOauth().getJwks_url());
			}
			JWTVerifier verifier = oAuth2Helper.getJWTVerifier(jwt, publicKey);
			jwt = verifier.verify(token);
			if (theRequest.getRequestType().equals(RequestTypeEnum.DELETE)) {
			  if (oAuth2Helper.hasClientRole(jwt, getOAuthClientId(), getOAuthAdminRole())) {
			    return allowAll();
			  }
			} else if (oAuth2Helper.hasClientRole(jwt, getOAuthClientId(), getOAuthUserRole())) {
			  String patientId = getPatientFromToken(jwt);
			  return ObjectUtils.isEmpty(patientId) ? allowAll() : allowForClaimResourceId(theRequest,patientId);
			}
		} catch (TokenExpiredException e) {
			logger.warn("Authentication failure - token has expired");
		} catch (Exception e) {
			logger.warn("Unexpected exception verifying token: {}", e.getMessage());
		}

		logger.warn("Authentication failure");
		return denyAll();
	}

	private List<IAuthRule> allowForClaimResourceId(RequestDetails theRequestDetails,String patientId) {
		if (oAuth2Helper.canBeInPatientCompartment(theRequestDetails.getResourceName())) {
			return new RuleBuilder()
		            .allow().read().allResources().inCompartment("Patient", new IdType("Patient", patientId)).andThen()
		            .allow().write().allResources().inCompartment("Patient", new IdType("Patient", patientId)).andThen()
		            .allow().transaction().withAnyOperation().andApplyNormalRules().andThen()
		            .allow().patch().allRequests().andThen()
		            .denyAll()
		            .build();
		}
		return allowAll();
	}

	private String getPatientFromToken(DecodedJWT jwt) {
		return oAuth2Helper.getPatientReferenceFromToken(jwt, "patient");
	}

	private boolean isOAuthEnabled() {
		return config.getOauth().getEnabled();
	}

	private String getOAuthClientId() {
		return config.getOauth().getClient_id();
	}

	private String getOAuthUserRole() {
		return config.getOauth().getUser_role();
	}

	private String getOAuthAdminRole() {
		return config.getOauth().getAdmin_role();
	}

	private boolean isBasicAuthEnabled() {
		return config.getBasic_auth().getEnabled();
	}

	private String getBasicAuthUsername() {
		return config.getBasic_auth().getUsername();
	}

	private String getBasicAuthPassword() {
		return config.getBasic_auth().getPassword();
	}

	private boolean isBasicAuthHeaderPresent(RequestDetails theRequest) {
		return BasicAuthHelper.isBasicAuthHeaderPresent(theRequest);
	}

	private boolean isApiKeyEnabled() {
		return config.getApikey().getEnabled();
	}

	private boolean isApiKeyHeaderPresent(RequestDetails theRequest) {
		String apiKey = theRequest.getHeader(APIKEY_HEADER);
		return (!ObjectUtils.isEmpty(apiKey));
	}

	private String getApiKey() {
		return config.getApikey().getKey();
	}

	private List<IAuthRule> authorizeApiKey(RequestDetails theRequest) {
		String apiKey = theRequest.getHeader(APIKEY_HEADER);
		if (ObjectUtils.isEmpty(apiKey)) {
			logger.info("Authorization failure - missing X-API-KEY header");
			return denyAll();
		}

		if (apiKey.equals(getApiKey())) {
			return allowAll();
		}

		logger.info("Authorization failure - invalid X-API-KEY header");
		return denyAll();
	}

	private List<IAuthRule> authorizeBasicAuth(RequestDetails theRequest) {
		String username = getBasicAuthUsername();
		String password = getBasicAuthPassword();
		String credentials = BasicAuthHelper.getCredentials(theRequest);
		if (basicAuthHelper.isValid(username, password, credentials)) {
			return allowAll();
		}
		logger.info("Authorization failure - invalid credentials");
		return denyAll();
	}
}
