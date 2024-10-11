package ca.uhn.fhir.jpa.starter;


//import interceptor.SignatureInterceptor;
//import org.hibernate.annotations.common.util.impl.LoggerFactory;
//import org.keycloak.adapters.KeycloakConfigResolver;
//import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
//import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
//import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
//import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
//import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
//import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.config.ConfigurableBeanFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Scope;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
//import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
//import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import ca.uhn.fhir.jpa.starter.service.FixNullReferenceInBundle;
//
//import java.util.Arrays;
//
//import static org.springframework.http.HttpMethod.*;
//
////@ConditionalOnProperty(prefix = "keycloak", name = "enabled", havingValue = "true", matchIfMissing = true)
//@Configuration
//public class CustomSecurityConfigNoKC extends WebSecurityConfigurerAdapter {
//	private static final String CORS_ALLOWED_HEADERS =
//            "origin,content-type,accept,x-requested-with,Authorization,Access-Control-Allow-Credentials,kid";
//
//    private String opensrpAllowedSources = "http://testhost.dashboard:3000/,http://localhost:3000/,https://oclink.io/,https://opencampaignlink.org/";
//
//    private long corsMaxAge = 60;
//
//    private static final org.jboss.logging.Logger logger = LoggerFactory.logger(CustomSecurityConfigNoKC.class);
//
//
//    @Autowired
//    AppProperties appProperties;
//
//	@Autowired
//	private FixNullReferenceInBundle fixNullReferenceInBundle;
//
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//        .addFilter(new SignatureInterceptor(appProperties, fixNullReferenceInBundle))
//        .cors()
//        .and()
//        .authorizeRequests()
//        .anyRequest().permitAll()
//        .and()
//        .csrf().disable()
//        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//    }
//
//	@Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList(opensrpAllowedSources.split(",")));
//        configuration.setAllowedMethods(
//                Arrays.asList(GET.name(), POST.name(), PUT.name(), DELETE.name(),OPTIONS.name()));
//        configuration.setAllowedHeaders(Arrays.asList(CORS_ALLOWED_HEADERS.split(",")));
//        configuration.setMaxAge(corsMaxAge);
//        configuration.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}
