package ca.uhn.fhir.jpa.starter;


import java.util.Arrays;

import javax.ws.rs.HEAD;

import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import interceptor.SignatureInterceptor;

import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.OPTIONS;

//@ConditionalOnProperty(prefix = "keycloak", name = "enabled", havingValue = "true", matchIfMissing = true)
@KeycloakConfiguration
public class CustomSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
	private static final String CORS_ALLOWED_HEADERS =
            "origin,content-type,accept,x-requested-with,Authorization,Access-Control-Allow-Credentials,kid";

    private String opensrpAllowedSources = "http://testhost.dashboard:3000/,http://localhost:3000/,https://oclink.io/,https://opencampaignlink.org/";

    private long corsMaxAge = 60;

    private static final org.jboss.logging.Logger logger = LoggerFactory.logger(CustomSecurityConfig.class);

    @Autowired 
    private KeycloakClientRequestFactory keycloakClientRequestFactory;

    @Autowired
    AppProperties appProperties;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {

        SimpleAuthorityMapper grantedAuthorityMapper = new SimpleAuthorityMapper();
        grantedAuthorityMapper.setPrefix("ROLE_");

        KeycloakAuthenticationProvider keycloakAuthenticationProvider =
                keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }
    
    @Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		// TODO Auto-generated method stub
    	return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

    @Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.addFilterBefore(new SignatureInterceptor(appProperties), KeycloakAuthenticationProcessingFilter.class);
        logger.info("Inside configure method");
        http.cors()
                .and()
                .authorizeRequests()
                .antMatchers("/**")
                .authenticated()
                .mvcMatchers("/logout.do")
                .permitAll()
                .and()
                .csrf()
                .ignoringAntMatchers("/fhir/**", "/iprd/**")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("logout.do", "GET"));
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(opensrpAllowedSources.split(",")));
        configuration.setAllowedMethods(
                Arrays.asList(GET.name(), POST.name(), PUT.name(), DELETE.name(),OPTIONS.name()));
        configuration.setAllowedHeaders(Arrays.asList(CORS_ALLOWED_HEADERS.split(",")));
        configuration.setMaxAge(corsMaxAge);
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KeycloakRestTemplate keycloakRestTemplate() {
        return new KeycloakRestTemplate(keycloakClientRequestFactory);
    }
}
