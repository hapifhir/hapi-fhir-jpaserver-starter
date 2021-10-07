package ca.uhn.fhir.jpa.starter.smart;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

// Originally from https://github.com/igia/igia-fhir-autoconfigure

@ConditionalOnProperty(prefix = "spring", name = "security")
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
		http.authorizeRequests().anyRequest().permitAll();
	}// @formatter:on

	@Bean
	JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwt().getJwkSetUri()).build();
		return jwtDecoder;
	}
}
