package ca.uhn.fhir.jpa.starter.smart.interceptors.scope.resource;

import ca.uhn.fhir.jpa.starter.smart.SmartScope;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.interceptor.auth.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ResourceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@ConditionalOnProperty(prefix = "hapi.fhir", name = "smart_enabled", havingValue = "true")
@Configuration
public class UserAuthorizationInterceptor extends ResourceAuthorizationInterceptor{

	public UserAuthorizationInterceptor(JwtDecoder jwtDecoder) {
		super(jwtDecoder);
	}

	@Override
	public List<IAuthRule> buildRules(RequestDetails theRequestDetails) {
		Jwt token = getJwtToken(theRequestDetails);

		if(token == null){
			return Collections.emptyList();
		}

		Set<SmartScope> smartScopes = getSmartScopes(token);
		IAuthRuleBuilder rules = new RuleBuilder();

		if (smartScopes.stream().anyMatch(SmartScope::isUserScope)) {
			String userId = token.getSubject();
			rules = filterToUserScopes(rules, userId, smartScopes);
			rules.denyAll().andThen();
		}

		return rules.build();
	}

	protected void filterToUserScope(String userId, SmartScope smartScope, IAuthRuleBuilder rules) {
		switch (smartScope.getOperation()) {
			case "*":
				applyUserScopeResourceClassifier(rules.allow().read(), userId, smartScope);
				applyUserScopeResourceClassifier(rules.allow().write(), userId, smartScope);
				applyUserScopeResourceClassifier(rules.allow().delete(), userId, smartScope);
				applyUserScopeConditionalResourceClassifier(rules.allow().createConditional(), smartScope);
				applyUserScopeConditionalResourceClassifier(rules.allow().updateConditional(), smartScope);
				applyUserScopeConditionalResourceClassifier(rules.allow().deleteConditional(), smartScope);
				// instance and type level operations may read, alter or delete data, should restrict to "*" scope
				applyUserScopeOperationResourceClassifier(rules, userId, smartScope);
				break;
			case "read":
				applyUserScopeResourceClassifier(rules.allow().read(), userId, smartScope);
				break;
			case "write":
				applyUserScopeResourceClassifier(rules.allow().write(), userId, smartScope);
				applyUserScopeResourceClassifier(rules.allow().delete(), userId, smartScope);
				applyUserScopeConditionalResourceClassifier(rules.allow().createConditional(), smartScope);
				applyUserScopeConditionalResourceClassifier(rules.allow().updateConditional(), smartScope);
				applyUserScopeConditionalResourceClassifier(rules.allow().deleteConditional(), smartScope);
				break;
			default:
				throw new NotImplementedOperationException("Scope operation " + smartScope.getOperation() + " not supported.");
		}
	}

	protected void applyUserScopeResourceClassifier(IAuthRuleBuilderRuleOp ruleOp, String userId, SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			ruleOp.allResources().withAnyId().andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				ruleOp.resourcesOfType(theType).withAnyId().andThen();
			} catch (FHIRException e) {
				throw new NotImplementedOperationException("Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}

	protected void applyUserScopeConditionalResourceClassifier(IAuthRuleBuilderRuleConditional ruleOp, SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			ruleOp.allResources().andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				ruleOp.resourcesOfType(theType).andThen();
			} catch (FHIRException e) {
				throw new NotImplementedOperationException("Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}

	protected void applyUserScopeOperationResourceClassifier(IAuthRuleBuilder rules, String userId, SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			rules.allow().operation().withAnyName().atAnyLevel();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				rules.allow().operation().withAnyName().onInstancesOfType(theType).andAllowAllResponses().andThen().
					allow().operation().withAnyName().onType(theType);
			} catch (FHIRException e) {
				throw new NotImplementedOperationException("Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}

	protected IAuthRuleBuilder filterToUserScopes(IAuthRuleBuilder rules, String userId, Set<SmartScope> smartScopes) {
		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isUserScope()) {
				filterToUserScope(userId, smartScope, rules);
			}
		}

		return rules;
	}

}
