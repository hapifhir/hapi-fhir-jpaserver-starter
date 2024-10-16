package ca.uhn.fhir.jpa.starter.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                        .csrf(csrf -> csrf.disable())
                        .authorizeHttpRequests(auth -> auth
                                    .requestMatchers("fhir/**").permitAll()
                                    .requestMatchers(HttpMethod.POST, "/performlogin").permitAll()
                                    .anyRequest().authenticated())
                        .formLogin(login -> login
                                    .loginPage("/login").permitAll()
                                    .defaultSuccessUrl("/", true)
                                    .failureUrl("/login?error=true"))
                        .sessionManagement(session -> session
                                    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

            return http.build();
      }
}
