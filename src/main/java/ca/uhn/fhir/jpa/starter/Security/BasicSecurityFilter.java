package ca.uhn.fhir.jpa.starter.Security;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BasicSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // Allow access to the login page
        if (request.getRequestURI().equals("/login") || request.getRequestURI().equals("/login?error=true")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check for the Authorization header
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header");
            return;
        }

        String base64 = authHeader.substring("Basic ".length());
        String base64decoded = new String(Base64.decodeBase64(base64));
        String[] parts = base64decoded.split(":");

        if (parts.length != 2) {
            sendUnauthorizedResponse(response, "Invalid Authorization header format");
            return;
        }

        String username = parts[0];
        String password = parts[1];

        // Validate credentials (this is a placeholder)
        if (!username.equals("fhir_user1") || !password.equals("password")) {
            sendUnauthorizedResponse(response, "Invalid username or password");
            return;
        }

        // Continue the filter chain if authentication is successful
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"myRealm\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
