package ca.uhn.fhir.jpa.starter.smart.interceptors.scope.resource;

import ca.uhn.fhir.jpa.starter.smart.SmartScope;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
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

}
