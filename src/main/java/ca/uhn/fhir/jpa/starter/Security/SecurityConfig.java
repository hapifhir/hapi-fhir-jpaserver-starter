package ca.uhn.fhir.jpa.starter.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

      private final JwtConverter jwtConverter;

      public SecurityConfig(JwtConverter jwtConverter) {
            this.jwtConverter = jwtConverter;
      }

      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                        .csrf(csrf -> csrf.disable())
                        .authorizeHttpRequests(auth -> auth
                                    .anyRequest().authenticated())
                        .oauth2Login(t -> t.defaultSuccessUrl("/", true))
                        .oauth2ResourceServer(oauth2 -> oauth2
                                    .jwt(jwt -> jwt
                                                .jwtAuthenticationConverter(jwtConverter)));

            return http.build();
      }
}
