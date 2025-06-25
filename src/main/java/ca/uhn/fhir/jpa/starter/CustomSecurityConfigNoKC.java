package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.starter.service.FixNullReferenceInBundle;
import interceptor.SignatureInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.http.HttpMethod.*;

@Configuration
public class CustomSecurityConfigNoKC extends WebSecurityConfigurerAdapter {
	private static final String CORS_ALLOWED_HEADERS =
            "origin,content-type,accept,x-requested-with,Authorization,Access-Control-Allow-Credentials,kid";

	private String opensrpAllowedSources = "http://testhost.dashboard:3000/,http://localhost:3000/,https://oclink.io/,https://opencampaignlink.org/";

	private static final long corsMaxAge = 3600;


	@Autowired
	AppProperties appProperties;

	@Autowired
	private FixNullReferenceInBundle fixNullReferenceInBundle;

	@Autowired
	private TokenAuthorizationFilter tokenAuthorizationFilter;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterBefore(tokenAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilter(new SignatureInterceptor(appProperties, fixNullReferenceInBundle))
			.cors()
			.and()
			.authorizeRequests()
			.antMatchers("/iprd/web/**").authenticated()
			.anyRequest().permitAll()
			.and()
			.csrf().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(opensrpAllowedSources.split(",")));
		configuration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), DELETE.name(), OPTIONS.name()));
		configuration.setAllowedHeaders(Arrays.asList(CORS_ALLOWED_HEADERS.split(",")));
		configuration.setMaxAge(corsMaxAge);
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}