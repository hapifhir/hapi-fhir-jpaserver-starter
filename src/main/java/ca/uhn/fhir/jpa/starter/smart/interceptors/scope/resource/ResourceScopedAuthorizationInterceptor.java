package ca.uhn.fhir.jpa.starter.smart.interceptors.scope.resource;

import ca.uhn.fhir.jpa.starter.smart.SmartScope;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilderRuleConditional;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilderRuleOp;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResourceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ConditionalOnProperty(prefix = "hapi.fhir", name = "smart_enabled", havingValue = "true")
@Configuration
public abstract class ResourceScopedAuthorizationInterceptor {

	private final JwtDecoder jwtDecoder;

	public ResourceScopedAuthorizationInterceptor(JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
	}

	public abstract List<IAuthRule> buildRules(RequestDetails details);


	protected Jwt getJwtToken(RequestDetails requestDetails) {
		String authHeader = requestDetails.getHeader("Authorization");
		if (authHeader == null || authHeader.isEmpty()) {
			return null;
		}

		return jwtDecoder.decode(authHeader.replace("Bearer ", ""));
	}

	protected Set<SmartScope> getSmartScopes(Jwt token) {
		Set<SmartScope> smartScopes = new HashSet<>();
		String[] scopes = token.getClaimAsString("scope").split(" ");

		for (String scope : scopes) {
			smartScopes.add(new SmartScope(scope));
		}

		return smartScopes;
	}

	protected IAuthRuleBuilder filterToResourceScope(IAuthRuleBuilder rules, String launchContext, Set<SmartScope> smartScopes, String resourceName) {
		IIdType resourceId = new IdType(resourceName, launchContext);

		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isResourceScope(resourceName)) {
				if (launchContext == null || launchContext.isEmpty()) {
					throw new SecurityException("For "+smartScope+" scope, launch context "+launchContext+" is required, but none was found.");
				}

				rules = filterToResourceScope(resourceId, smartScope, rules, resourceName);
			}
		}

		return rules;
	}

	protected IAuthRuleBuilder filterToResourceScope(IIdType resourceId, SmartScope smartScope, IAuthRuleBuilder rules, String resourceName) {
		switch (smartScope.getOperation()) {
			case "*":
				applyResourceScopeClassifier(rules.allow().read(), resourceId, smartScope, resourceName);
				applyResourceScopeClassifier(rules.allow().write(), resourceId, smartScope, resourceName);
				applyResourceScopeClassifier(rules.allow().delete(), resourceId, smartScope, resourceName);
				applyResourceScopeConditionalClassifier(rules.allow().createConditional(), smartScope);
				applyResourceScopeConditionalClassifier(rules.allow().updateConditional(), smartScope);
				applyResourceScopeConditionalClassifier(rules.allow().deleteConditional(), smartScope);
				// resource operations (type or instance level) may read, alter or delete data, should restrict to "*" scope
				applyResourceScopeOperationClassifier(rules, resourceId, smartScope, resourceName);
				break;
			case "read":
				applyResourceScopeClassifier(rules.allow().read(), resourceId, smartScope, resourceName);
				break;
			case "write":
				applyResourceScopeClassifier(rules.allow().write(), resourceId, smartScope, resourceName);
				applyResourceScopeClassifier(rules.allow().delete(), resourceId, smartScope, resourceName);
				applyResourceScopeConditionalClassifier(rules.allow().createConditional(), smartScope);
				applyResourceScopeConditionalClassifier(rules.allow().updateConditional(), smartScope);
				applyResourceScopeConditionalClassifier(rules.allow().deleteConditional(), smartScope);
				break;
			default:
				throw new NotImplementedOperationException("Scope operation " + smartScope.getOperation() + " not supported.");
		}
		return rules;
	}

	protected void applyResourceScopeClassifier(IAuthRuleBuilderRuleOp ruleOp, IIdType resourceId, SmartScope smartScope, String resourceName) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			ruleOp.allResources().inCompartment(resourceName, resourceId).andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				ruleOp.resourcesOfType(theType).inCompartment(resourceName, resourceId).andThen();
			} catch (FHIRException e) {
				throw new NotImplementedOperationException("Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}

	protected void applyResourceScopeConditionalClassifier(IAuthRuleBuilderRuleConditional ruleOp, SmartScope smartScope) {
		return;
	}

	protected void applyResourceScopeOperationClassifier(IAuthRuleBuilder rules, IIdType idType, SmartScope smartScope, String resourceName) {
		//cannot fully restrict access to type and instance level operations by patient id
		if (smartScope.getResource().equalsIgnoreCase(resourceName)) {
			rules.allow().operation().withAnyName().onInstance(idType);
		}
	}
}
