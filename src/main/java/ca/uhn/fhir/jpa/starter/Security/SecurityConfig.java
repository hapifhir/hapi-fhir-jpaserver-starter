package ca.uhn.fhir.jpa.starter.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ca.uhn.fhir.jpa.starter.Security.Filter.JwtAuthFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

      @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
      private String jwkSetUri;

      private final JwtConverterProperties properties;

      public SecurityConfig(JwtConverterProperties properties) {
            this.properties = properties;
      }

      @Bean
      public JwtDecoder jwtDecoder() {
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
      }

      @Bean
      public JwtConverter jwtConverter(JwtDecoder jwtDecoder) {
            return new JwtConverter(properties, jwtDecoder);
      }

      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtConverter jwtConverter) throws Exception {
            http
                        .csrf(csrf -> csrf.disable())
                        .authorizeHttpRequests(auth -> auth
                                    .requestMatchers("/css/**","/resources/**", "/js/**", "/img/**").permitAll()
                                    .requestMatchers(HttpMethod.POST).permitAll()
                                    .anyRequest().authenticated())
                        .oauth2Login(login -> login
                                    .loginPage("/login").permitAll()
                                    .defaultSuccessUrl("/", true)
                                    .failureUrl("/login?error=true"))
                        .oauth2ResourceServer(oauth2 -> oauth2
                                    .jwt(jwt -> jwt
                                                .jwtAuthenticationConverter(jwtConverter)))
                        .addFilterBefore(new JwtAuthFilter(jwtConverter),
                                    UsernamePasswordAuthenticationFilter.class);

            return http.build();
      }
}
