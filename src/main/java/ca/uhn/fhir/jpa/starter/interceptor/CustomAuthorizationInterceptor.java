package ca.uhn.fhir.jpa.starter.interceptor;

import java.security.GeneralSecurityException;
import java.util.List;

import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Strings;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.ApiKeyHelper;
import ca.uhn.fhir.jpa.starter.util.OAuth2Helper;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

@Interceptor
public class CustomAuthorizationInterceptor extends AuthorizationInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(CustomAuthorizationInterceptor.class);

	private final AppProperties config;

	public CustomAuthorizationInterceptor(AppProperties config) {
		super();
		this.config = config;
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequest) {
		if (!isAnyEnabled()) {
			return authorizedRule();
		}

		try {
			if (Constants.URL_TOKEN_METADATA.equals(theRequest.getRequestPath())) {
				return unauthorizedRule();
			}

			if (isUsingOAuth(theRequest)) {
				return authorizeOAuth(theRequest);
			}
			if (isUsingApiKey(theRequest)) {
				return authorizeApiKey(theRequest);
			}
			logger.warn("Authorization failed - no authorization supplied");
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("Unexpected exception during authorization: {}", e.getMessage());
		}

		throw new AuthenticationException("Missing or invalid authorization");
	}

	private boolean isAnyEnabled() {
		return isOAuthEnabled() || isApiKeyEnabled();
	}

	private boolean isUsingOAuth(RequestDetails theRequest) {
		return isOAuthEnabled() && OAuth2Helper.hasToken(theRequest);
	}

	private boolean isUsingApiKey(RequestDetails theRequest) {
		return isApiKeyEnabled() && ApiKeyHelper.hasApiKey(theRequest);
	}

	private List<IAuthRule> authorizedRule() {
		return new RuleBuilder()
			.allowAll()
			.build();
	}

	private List<IAuthRule> unauthorizedRule() {
		// By default, deny everything except the metadata request
		return new RuleBuilder()
			.allow().metadata().andThen()
			.denyAll()
			.build();
	}

	private List<IAuthRule> authorizeOAuth(RequestDetails theRequest) throws AuthenticationException {
		logger.info("Authorizing via OAuth2");
		String token = OAuth2Helper.getToken(theRequest);
		try {
			DecodedJWT jwt = JWT.decode(token);
			String jwksUrl = config.getOauth().getJwks_url();
			OAuth2Helper.verify(jwt, jwksUrl);

			List<String> clientRoles = OAuth2Helper.getClientRoles(jwt, getOAuthClientId());
			if (clientRoles.isEmpty()) {
				logger.warn("Authorization failure - token doesn't have any client roles");
				return unauthorizedRule();
			}

			// The only difference between the admin role and the user role is that the admin role
			// allows DELETE requests. It still needs to enforce a patient claim, if one exists.
			if (theRequest.getRequestType().equals(RequestTypeEnum.DELETE)
					&& !clientRoles.contains(getOAuthAdminRole())) {
				logger.warn("Authorization failure - token doesn't have the admin role required for delete");
				return unauthorizedRule();
			}

			if (clientRoles.contains(getOAuthAdminRole()) || clientRoles.contains(getOAuthUserRole())) {
				String patientId = OAuth2Helper.getClaimAsString(jwt, "patient");
				if (Strings.isNullOrEmpty(patientId)) {
					logger.debug("No patient claim specified in authorization token");
					return authorizedRule();
				} else {
					logger.debug("Patient claim specified in in authorization token; will use patient compartment rules");
					return authorizedInPatientCompartmentRule(theRequest, patientId);
				}
			}

			logger.warn("Authorization failure - token doesn't have the required client roles");
			return unauthorizedRule();
		} catch (RuntimeException|GeneralSecurityException e) {
			logger.warn("Authentication failure - unable to decode or verify token: {}", e.getMessage());
			throw new AuthenticationException("Invalid authorization header", e);
		}
	}

	private List<IAuthRule> authorizedInPatientCompartmentRule(RequestDetails theRequestDetails, String patientId) {
		if (OAuth2Helper.canBeInPatientCompartment(theRequestDetails.getResourceName())) {
			IdType patientIdType = new IdType("Patient", patientId);
			return new RuleBuilder()
				.allow().read().allResources().inCompartment("Patient", patientIdType).andThen()
				.allow().patch().allRequests().andThen()
				.allow().write().allResources().inCompartment("Patient", patientIdType).andThen()
				.allow().delete().allResources().inCompartment("Patient", patientIdType).andThen()
				.allow().transaction().withAnyOperation().andApplyNormalRules().andThen()
				.denyAll()
				.build();
		}
		return authorizedRule();
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

	private boolean isApiKeyEnabled() {
		return config.getApikey().getEnabled();
	}

	private String getApiKey() {
		return config.getApikey().getKey();
	}

	private List<IAuthRule> authorizeApiKey(RequestDetails theRequest) throws AuthenticationException {
		logger.info("Authorizing via API Key");
		if (ApiKeyHelper.isAuthorized(theRequest, getApiKey())) {
			return authorizedRule();
		}

		logger.warn("API key authorization failure - invalid x-api-key specified");
		throw new AuthenticationException("Invalid x-api-key");
	}
}
