package ca.uhn.fhir.jpa.starter.smart.config;

import ca.uhn.fhir.jpa.starter.smart.security.SmartScopeAuthorizationInterceptor;
import ca.uhn.fhir.jpa.starter.smart.security.builder.SmartAuthorizationRuleBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;

@ConditionalOnProperty(prefix = "hapi.fhir", name = "smart_enabled", havingValue = "true")
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {// @formatter:off
		/*http.cors().disable()
			.authorizeRequests()
			.antMatchers(HttpMethod.GET, "/user/info", "/api/foos/**")
			.hasAuthority("SCOPE_read")
			.antMatchers(HttpMethod.POST, "/api/foos")
			.hasAuthority("SCOPE_write")
			.anyRequest()
			.authenticated()
			.and()
			.oauth2ResourceServer()
			.jwt();*/
		http.authorizeRequests().anyRequest().permitAll().and().csrf().disable();
	}// @formatter:on

	@Bean
	@Lazy
	JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
		OAuth2ResourceServerProperties.Jwt jwtConfiguration = properties.getJwt();
		return NimbusJwtDecoder.withJwkSetUri(jwtConfiguration.getJwkSetUri()).build();
	}

	@Bean
	@Lazy
	public SmartScopeAuthorizationInterceptor smartScopeAuthorizationInterceptor(List<SmartAuthorizationRuleBuilder> ruleBuilders, JwtDecoder jwtDecoder){
		return new SmartScopeAuthorizationInterceptor(ruleBuilders, jwtDecoder);
	}

}
