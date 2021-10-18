package ca.uhn.fhir.jpa.starter.smart.interceptors.scope;

import ca.uhn.fhir.jpa.starter.smart.interceptors.scope.resource.ResourceAuthorizationInterceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationFlagsEnum;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty(prefix = "hapi.fhir", name = "smart_enabled", havingValue = "true")
@Configuration
public class ParentAuthorizationInterceptor extends AuthorizationInterceptor {

	private final List<ResourceAuthorizationInterceptor> resourceInterceptors;
	private static final String RULE_DENY_ALL_UNKNOWN_REQUESTS = "Deny all requests that do not match any pre-defined rules";

	public ParentAuthorizationInterceptor(@Autowired List<ResourceAuthorizationInterceptor> resourceInterceptors) {
		this.setFlags(AuthorizationFlagsEnum.NO_NOT_PROACTIVELY_BLOCK_COMPARTMENT_READ_ACCESS);
		this.resourceInterceptors = resourceInterceptors;
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		List<IAuthRule> ruleList = new ArrayList<>(new RuleBuilder().allow().metadata().build());
		for (ResourceAuthorizationInterceptor resourceInterceptor:resourceInterceptors) {
			ruleList.addAll(resourceInterceptor.buildRules(theRequestDetails));
		}
		ruleList.addAll(new RuleBuilder().denyAll(RULE_DENY_ALL_UNKNOWN_REQUESTS).build());
		return ruleList;
	}
}