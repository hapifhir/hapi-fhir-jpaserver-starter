package ca.uhn.fhir.jpa.starter.smart.interceptors.scope;

import ca.uhn.fhir.jpa.starter.smart.SmartClinicalScope;
import ca.uhn.fhir.jpa.starter.smart.interceptors.scope.resource.CompartmentAuthorizationRuleBuilder;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationFlagsEnum;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.*;

@ConditionalOnProperty(prefix = "hapi.fhir", name = "smart_enabled", havingValue = "true")
@Configuration
public class CompartmentAuthorizationInterceptor extends AuthorizationInterceptor {

	private final Map<String, String> resourceMapping;
	private static final String RULE_DENY_ALL_UNKNOWN_REQUESTS = "Deny all requests that do not match any pre-defined rules";
	private static final String RULE_DENY_UNAUTHORIZED_REQUESTS = "No JWT given";

	private final JwtDecoder jwtDecoder;


	public CompartmentAuthorizationInterceptor(Map<String, String> resourceMapping, JwtDecoder jwtDecoder) {
		this.setFlags(AuthorizationFlagsEnum.NO_NOT_PROACTIVELY_BLOCK_COMPARTMENT_READ_ACCESS);
		this.resourceMapping = resourceMapping;
		this.jwtDecoder = jwtDecoder;
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		Jwt token = getJwtToken(theRequestDetails);

		if(token == null){
			return new RuleBuilder().denyAll(RULE_DENY_UNAUTHORIZED_REQUESTS).build();
		}

		List<IAuthRule> ruleList = new ArrayList<>(new RuleBuilder().allow().metadata().build());
		Set<SmartClinicalScope> scopes = getSmartScopes(token);
		Map<String, Object> claims = token.getClaims();

		for (SmartClinicalScope scope:scopes) {
			String compartmentName = scope.getCompartment();
			if (compartmentName != null && !compartmentName.isEmpty() && resourceMapping.get(compartmentName) != null){
				String launchCtxName = resourceMapping.get(compartmentName);
				String launchCtx = (String) claims.get(launchCtxName);
				ruleList.addAll(CompartmentAuthorizationRuleBuilder.buildRules(launchCtx, scope));
			}
		}
		ruleList.addAll(new RuleBuilder().denyAll(RULE_DENY_ALL_UNKNOWN_REQUESTS).build());
		return ruleList;
	}

	protected Jwt getJwtToken(RequestDetails requestDetails) {
		String authHeader = requestDetails.getHeader("Authorization");
		if (authHeader == null || authHeader.isEmpty()) {
			return null;
		}

		return jwtDecoder.decode(authHeader.replace("Bearer ", ""));
	}

	protected static Set<SmartClinicalScope> getSmartScopes(Jwt token) {
		Set<SmartClinicalScope> smartClinicalScopes = new HashSet<>();
		String[] scopes = token.getClaimAsString("scope").split(" ");

		for (String scope : scopes) {
			smartClinicalScopes.add(new SmartClinicalScope(scope));
		}

		return smartClinicalScopes;
	}

}