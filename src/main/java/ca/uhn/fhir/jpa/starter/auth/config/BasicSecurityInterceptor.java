package ca.uhn.fhir.jpa.starter.auth.config;

import ca.uhn.fhir.jpa.starter.auth.service.AuthService;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import graphql.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@ConditionalOnProperty(prefix = "hapi.fhir.ngsa.auth", name = "enabled", havingValue = "true")
@Component
public class BasicSecurityInterceptor extends AuthorizationInterceptor {

	@Autowired
	private AuthService authService;

	public BasicSecurityInterceptor() {
		super();
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

		String token = theRequestDetails.getHeader(Constants.HEADER_AUTHORIZATION);
		if (token == null) {
			return new RuleBuilder().denyAll().build();
		}
		if (!token.startsWith(Constants.HEADER_AUTHORIZATION_VALPREFIX_BEARER)) {
			return new RuleBuilder().denyAll().build();
		}

		Pair<Boolean, String> verify = authService.isTokenValid(token.substring(7));

		if (verify.first) {
			return new RuleBuilder().allowAll().build();
		} else {
			return new RuleBuilder().denyAll().build();
		}
	}

}