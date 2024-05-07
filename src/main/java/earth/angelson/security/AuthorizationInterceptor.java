package earth.angelson.security;

import earth.angelson.security.cache.TokenCacheService;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;

import java.util.List;

@SuppressWarnings("ConstantConditions")
public class AuthorizationInterceptor extends ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor {

	private final TokenCacheService tokenCacheService;

	public AuthorizationInterceptor(TokenCacheService tokenCacheService) {
		this.tokenCacheService = tokenCacheService;
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		String authHeader = theRequestDetails.getHeader("Authorization");

		//todo if service return empty unauthorized request 403
		return tokenCacheService.getData(authHeader);
	}
}
