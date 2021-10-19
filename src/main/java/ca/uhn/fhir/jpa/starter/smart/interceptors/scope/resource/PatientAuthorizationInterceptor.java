package ca.uhn.fhir.jpa.starter.smart.interceptors.scope.resource;

import ca.uhn.fhir.jpa.starter.smart.SmartScope;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.interceptor.auth.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResourceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ConditionalOnProperty(prefix = "hapi.fhir", name = "smart_enabled", havingValue = "true")
@Configuration
public class PatientAuthorizationInterceptor extends ResourceScopedAuthorizationInterceptor {

	public static final String LAUNCH_CONTEXT_PATIENT_PARAM_NAME = "patient";
	private static final String PATIENT_RESOURCE_NAME = "Patient";

	public PatientAuthorizationInterceptor(JwtDecoder jwtDecoder) {
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

		if (smartScopes.stream().anyMatch(SmartScope::isPatientScope)) {
			Map<String, Object> claims = token.getClaims();
			String patientId = (String) claims.get(LAUNCH_CONTEXT_PATIENT_PARAM_NAME);
			rules = filterToPatientScopes(rules, patientId, smartScopes);
		}
//		rules = rules.deny().read().resourcesOfType(PATIENT_RESOURCE_NAME).withAnyId().andThen();
		return rules.build();
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

	protected IAuthRuleBuilder filterToPatientScopes(IAuthRuleBuilder rules, String patientId, Set<SmartScope> smartScopes) {
		IIdType patientIId = new IdType(PATIENT_RESOURCE_NAME, patientId);

		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isPatientScope()) {
				if (patientId == null || patientId.isEmpty()) {
					throw new SecurityException("For patient scope, a launch context parameter indicating the in-context patient is required, but none was found.");
				}

				filterToPatientScope(patientIId, smartScope, rules);
			}
		}

		return rules;
	}

	protected void applyPatientScopeResourceClassifier(IAuthRuleBuilderRuleOp ruleOp, IIdType patientId, SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			ruleOp.allResources().inCompartment(PATIENT_RESOURCE_NAME, patientId).andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				ruleOp.resourcesOfType(theType).inCompartment(PATIENT_RESOURCE_NAME, patientId).andThen();
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
		if (smartScope.getResource().equalsIgnoreCase(PATIENT_RESOURCE_NAME)) {
			rules.allow().operation().withAnyName().onInstance(patientIdType);
		}
	}


}
