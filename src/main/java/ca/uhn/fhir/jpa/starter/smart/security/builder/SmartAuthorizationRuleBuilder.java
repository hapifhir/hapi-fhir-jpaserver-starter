package ca.uhn.fhir.jpa.starter.smart.security.builder;

import ca.uhn.fhir.jpa.starter.smart.model.SmartClinicalScope;
import ca.uhn.fhir.jpa.starter.smart.util.SmartResourceMapping;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConditionalOnProperty(prefix = "hapi.fhir", name = "smart_enabled", havingValue = "true")
@Component
public abstract class SmartAuthorizationRuleBuilder {

	private final Map<String, SmartResourceMapping> resourceMap;

	protected SmartAuthorizationRuleBuilder(){
		resourceMap = new HashMap<>();
	}

	protected SmartAuthorizationRuleBuilder(Map<String, SmartResourceMapping> resourceMap) {
		this.resourceMap = resourceMap;
	}

	public abstract List<IAuthRule> buildRules(String launchCtx, SmartClinicalScope smartClinicalScope);

	public void registerResource(String smartScope, SmartResourceMapping smartResourceMapping){
		resourceMap.putIfAbsent(smartScope, smartResourceMapping);
	}

	public boolean deregisterResource(String smartScope){
		return resourceMap.remove(smartScope) != null;
	}

	public boolean hasRegisteredResource(String smartScope){
		return resourceMap.containsKey(smartScope);
	}

	public String getLaunchCtxName(String smartScope){
		return resourceMap.get(smartScope).getLaunchCtxLabel();
	}

	public String getCompartmentResource(String smartScope) {return resourceMap.get(smartScope).getResourceType();}
}
