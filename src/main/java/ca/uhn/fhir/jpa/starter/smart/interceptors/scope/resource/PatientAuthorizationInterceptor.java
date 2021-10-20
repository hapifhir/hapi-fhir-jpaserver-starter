package ca.uhn.fhir.jpa.starter.smart.interceptors.scope.resource;

import ca.uhn.fhir.jpa.starter.smart.SmartScope;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ConditionalOnProperty(prefix = "hapi.fhir", name = "smart_enabled", havingValue = "true")
@Configuration
public class PatientAuthorizationInterceptor extends ResourceScopedAuthorizationInterceptor {

	public static final String LAUNCH_CONTEXT_PATIENT_PARAM_NAME = "patient";
	private static final String PATIENT_RESOURCE_NAME = "Patient";
	private static final String DENY_REQUESTS_WITH_NO_PATIENT_ID_RULE = "Deny ALL patient requests if no patient id is passed!";
	private static final String DENY_UNAUTHENTICATED_REQUESTS_RULE = "Deny ALL patient requests if no authorization information is given!";
	public PatientAuthorizationInterceptor(JwtDecoder jwtDecoder) {
		super(jwtDecoder);
	}

	@Override
	public List<IAuthRule> buildRules(RequestDetails theRequestDetails) {
		Jwt token = getJwtToken(theRequestDetails);

		if(token == null){
			return new RuleBuilder().denyAll(DENY_UNAUTHENTICATED_REQUESTS_RULE).build();
		}

		Set<SmartScope> smartScopes = getSmartScopes(token);

		IAuthRuleBuilder rules = new RuleBuilder();

		if (smartScopes.stream().anyMatch(scope -> scope.isResourceScope(PATIENT_RESOURCE_NAME))) {
			Map<String, Object> claims = token.getClaims();
			String patientId = (String) claims.get(LAUNCH_CONTEXT_PATIENT_PARAM_NAME);
			try{
				rules = filterToResourceScope(rules, patientId, smartScopes, PATIENT_RESOURCE_NAME);
			} catch (SecurityException e){
				rules.denyAll(DENY_REQUESTS_WITH_NO_PATIENT_ID_RULE);
			}
		}
//		rules = rules.deny().read().resourcesOfType(PATIENT_RESOURCE_NAME).withAnyId().andThen();
		return rules.build();
	}



}
