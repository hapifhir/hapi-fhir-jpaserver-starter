package ca.uhn.fhir.jpa.starter.smart.security;

import ca.uhn.fhir.jpa.starter.smart.exception.InvalidClinicalScopeException;
import ca.uhn.fhir.jpa.starter.smart.exception.InvalidSmartOperationException;
import ca.uhn.fhir.jpa.starter.smart.model.SmartClinicalScope;
import ca.uhn.fhir.jpa.starter.smart.security.builder.SmartAuthorizationRuleBuilder;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationFlagsEnum;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.*;

public class SmartScopeAuthorizationInterceptor extends AuthorizationInterceptor {

	private final List<SmartAuthorizationRuleBuilder> ruleBuilders;
	public static final String RULE_DENY_ALL_UNKNOWN_REQUESTS = "Deny all requests that do not match any pre-defined rules";
	public static final String RULE_DENY_UNAUTHORIZED_REQUESTS = "No JWT given";


	private final JwtDecoder jwtDecoder;


	public SmartScopeAuthorizationInterceptor(List<SmartAuthorizationRuleBuilder> ruleBuilders, JwtDecoder jwtDecoder) {
		this.setFlags(AuthorizationFlagsEnum.NO_NOT_PROACTIVELY_BLOCK_COMPARTMENT_READ_ACCESS);
		this.ruleBuilders = ruleBuilders;
		this.jwtDecoder = jwtDecoder;
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		Jwt token = getJwtToken(theRequestDetails);

		List<IAuthRule> ruleList = new ArrayList<>(new RuleBuilder().allow().metadata().build());

		if (token == null) {
			return ruleList;
		}

		try{
			Set<SmartClinicalScope> scopes = getSmartScopes(token);
			Map<String, Object> claims = token.getClaims();

			for (SmartClinicalScope scope : scopes) {
				String compartmentName = scope.getCompartment();
				if (compartmentName != null && !compartmentName.isEmpty()) {
					ruleBuilders.stream().filter(smartAuthorizationRuleBuilder -> smartAuthorizationRuleBuilder.hasRegisteredResource(compartmentName)).forEach(smartAuthorizationRuleBuilder -> {
						String launchCtxName = smartAuthorizationRuleBuilder.getLaunchCtxName(compartmentName);
						String launchCtx = (String) claims.get(launchCtxName);
						ruleList.addAll(smartAuthorizationRuleBuilder.buildRules(launchCtx, scope));
					});
				}
			}
			ruleList.addAll(new RuleBuilder().denyAll(RULE_DENY_ALL_UNKNOWN_REQUESTS).build());
		} catch (InvalidClinicalScopeException | InvalidSmartOperationException e){
			ruleList.addAll(new RuleBuilder().denyAll(e.getMessage()).build());
		}
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