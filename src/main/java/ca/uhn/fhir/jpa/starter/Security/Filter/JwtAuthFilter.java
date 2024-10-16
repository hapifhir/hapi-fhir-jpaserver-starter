/* package ca.uhn.fhir.jpa.starter.Security.Filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = (Authentication) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");

        if (authentication != null) {

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Authentication successful for user: " + authentication.getName());
        } else {
            System.out.println("No authentication found in session.");
        }

        filterChain.doFilter(request, response);
    }
}
 */