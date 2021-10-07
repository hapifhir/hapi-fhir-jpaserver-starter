/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 * <p>
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package ca.uhn.fhir.jpa.starter.smart;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.interceptor.auth.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResourceFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScopeBasedAuthorizationInterceptor extends AuthorizationInterceptor {
	public static final String LAUNCH_CONTEXT_PATIENT_PARAM_NAME = "patient";
	private static final String RULE_PATIENT_SCOPE_DEFAULT_DENY = "DENY ALL patient, resource or operation access if not explicitly granted in authorized scope";

	public ScopeBasedAuthorizationInterceptor() {
		this.setFlags(AuthorizationFlagsEnum.NO_NOT_PROACTIVELY_BLOCK_COMPARTMENT_READ_ACCESS);
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// if the user is not authenticated, we can't do any authorization
		if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
			return new RuleBuilder().allowAll().build();
		}

		Jwt token = ((JwtAuthenticationToken) authentication).getToken();

		Set<SmartScope> smartScopes = getSmartScopes(token);

		IAuthRuleBuilder rules = new RuleBuilder();

		// if no access limiting scopes, then allow all
		boolean isSmartScope = false;
		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isUserScope() || smartScope.isPatientScope()) isSmartScope = true;
			break;
		}
		if (!isSmartScope) {
			return new RuleBuilder().allowAll().build();
		}

		Map<String, Object> claims = token.getClaims();

		String patientId = (String) claims.get(LAUNCH_CONTEXT_PATIENT_PARAM_NAME);
		String userId = token.getSubject();

		rules = filterToUserScopes(rules, userId, smartScopes);
		rules = filterToPatientScopes(rules, patientId, smartScopes);

		rules.allow().metadata().andThen();
		rules.denyAll(RULE_PATIENT_SCOPE_DEFAULT_DENY).andThen();
		return rules.build();
	}

	protected IAuthRuleBuilder filterToUserScopes(IAuthRuleBuilder rules, String userId, Set<SmartScope> smartScopes) {
		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isUserScope()) {
				filterToUserScope(userId, smartScope, rules);
			}
		}

		return rules;
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

	protected IAuthRuleBuilder filterToPatientScopes(IAuthRuleBuilder rules, String patientId, Set<SmartScope> smartScopes) {
		IIdType patientIId = new IdType("Patient", patientId);

		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isPatientScope()) {
				if (patientId == null || patientId.isEmpty()) {
					throw new SecurityException("For patient scope, a launch context parameter indicating the in-context" + " patient is required, but none was found.");
				}

				filterToPatientScope(patientIId, smartScope, rules);
			}
		}

		return rules;
	}

	protected void filterToPatientScope(IIdType patientId, SmartScope smartScope, IAuthRuleBuilder rules) {
		switch (smartScope.getOperation()) {
			case "*":
				applyPatientScopeResourceClassifier(rules.allow().read(), patientId, smartScope);
				applyPatientScopeResourceClassifier(rules.allow().write(), patientId, smartScope);
				applyPatientScopeResourceClassifier(rules.allow().delete(), patientId, smartScope);
				applyPatientScopeConditionalResourceClassifier(rules.allow().createConditional(), smartScope);
				applyPatientScopeConditionalResourceClassifier(rules.allow().updateConditional(), smartScope);
				applyPatientScopeConditionalResourceClassifier(rules.allow().deleteConditional(), smartScope);
				// resource operations (type or instance level) may read, alter or delete data, should restrict to "*" scope
				applyPatientScopeOperationResourceClassifier(rules, patientId, smartScope);
				break;
			case "read":
				applyPatientScopeResourceClassifier(rules.allow().read(), patientId, smartScope);
				break;
			case "write":
				applyPatientScopeResourceClassifier(rules.allow().write(), patientId, smartScope);
				applyPatientScopeResourceClassifier(rules.allow().delete(), patientId, smartScope);
				applyPatientScopeConditionalResourceClassifier(rules.allow().createConditional(), smartScope);
				applyPatientScopeConditionalResourceClassifier(rules.allow().updateConditional(), smartScope);
				applyPatientScopeConditionalResourceClassifier(rules.allow().deleteConditional(), smartScope);
				break;
			default:
				throw new NotImplementedOperationException("Scope operation " + smartScope.getOperation() + " not supported.");
		}
	}

	protected void applyPatientScopeResourceClassifier(IAuthRuleBuilderRuleOp ruleOp, IIdType patientId, SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			ruleOp.allResources().inCompartment("Patient", patientId).andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				ruleOp.resourcesOfType(theType).inCompartment("Patient", patientId).andThen();
			} catch (FHIRException e) {
				throw new NotImplementedOperationException("Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}

	protected void applyPatientScopeConditionalResourceClassifier(IAuthRuleBuilderRuleConditional ruleOp, SmartScope smartScope) {
		return;
	}

	protected void applyPatientScopeOperationResourceClassifier(IAuthRuleBuilder rules, IIdType patientIdType, SmartScope smartScope) {
		//cannot fully restrict access to type and instance level operations by patient id
		if (smartScope.getResource().equalsIgnoreCase("Patient")) {
			rules.allow().operation().withAnyName().onInstance(patientIdType);
		}
	}

	private Set<SmartScope> getSmartScopes(Jwt token) {
		Set<SmartScope> scopes = new HashSet<>();

		for (String scope : token.getClaimAsStringList("scope")) {
			scopes.add(new SmartScope(scope));
		}

		return scopes;
	}
}
