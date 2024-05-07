package earth.angelson.security.cache;


import earth.angelson.security.dto.RoleAttachmentsDTO;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class TokenCacheService {

	private final RestTemplate restTemplate;
	private final String url;

	public TokenCacheService(String url) {
		this.restTemplate = new RestTemplate();
		this.url = url;
	}

	@Cacheable(value = "jwtTokenCache", cacheManager = "caffeineCacheManager")
	public List<IAuthRule> getData(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", token);

		// Create HttpEntity with headers
		HttpEntity<RoleAttachmentsDTO> entity = new HttpEntity<>(headers);

		ResponseEntity<RoleAttachmentsDTO> response =
			restTemplate.exchange(url, HttpMethod.GET, entity, RoleAttachmentsDTO.class);


		if (response.getBody() != null) {
			var builder = new RuleBuilder().build();
			var role = response.getBody();
			role.getRoles().stream().forEach(roleWithRuleDTO -> {
				roleWithRuleDTO.getRules().forEach(rule -> {
					switch (rule.getOperation()) {
						case "READ": {
							builder.addAll(new RuleBuilder()
								.allow()
								.read()
								.allResources()
								.withAnyId()
								.build());
							break;
						}
						case "WRITE": {
							builder.addAll(new RuleBuilder()
								.allow()
								.write()
								.allResources()
								.withAnyId()
								.build());
							break;
						}
						case "ALL": {
							builder.addAll(new RuleBuilder().allowAll().build());
							break;
						}
					}

				});
			});
			return builder;
		}

		// By default, deny everything. This should never get hit, but it's
		// good to be defensive
		return new RuleBuilder().denyAll().build();
	}
}
