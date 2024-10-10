package ca.uhn.fhir.jpa.starter.Security.Filter;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import ca.uhn.fhir.jpa.starter.Security.JwtConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    public final JwtConverter jwtConverter;

    public JwtAuthFilter(JwtConverter jwtConverter) {
        this.jwtConverter = jwtConverter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String accessToken = request.getHeader("Authorization");
        if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")) {
            try {
                accessToken = accessToken.substring(7);
                Jwt jwt = jwtConverter.parseToken(accessToken);
                if (jwt != null) {
                    JwtAuthenticationToken authenticationToken = jwtConverter.convert(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    System.out.println("Authentication successful for user: " + authenticationToken.getName());
                }
            } catch (Exception e) {
                System.err.println("Invalid JWT Token: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
