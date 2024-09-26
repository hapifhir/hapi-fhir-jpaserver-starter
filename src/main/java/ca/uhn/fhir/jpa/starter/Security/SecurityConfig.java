package ca.uhn.fhir.jpa.starter.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
            .authorizeHttpRequests(authorize -> authorize
                  .requestMatchers("/login").permitAll()
                  .requestMatchers("/").permitAll()

                  .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form
                  .loginPage("/login")
                  .failureHandler(authenticationFailureHandler())
                  .permitAll())
            .logout(logout -> logout.permitAll());

      return http.build();
   }

   @Bean
   public AuthenticationManager authManager(HttpSecurity http) throws Exception {
      AuthenticationManagerBuilder authenticationManagerBuilder = http
            .getSharedObject(AuthenticationManagerBuilder.class);
      authenticationManagerBuilder.inMemoryAuthentication()
            .withUser("fhir_user1")
            .password(passwordEncoder().encode("password"))
            .roles("USER");

      return authenticationManagerBuilder.build();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }

   @Bean
   public AuthenticationFailureHandler authenticationFailureHandler() {
      return (request, response, exception) -> response.sendRedirect("/login?error=true");
   }
}
