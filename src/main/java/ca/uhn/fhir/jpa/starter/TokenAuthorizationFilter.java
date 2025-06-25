package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.starter.model.JWTPayload;
import com.iprd.fhir.utils.Validation;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TokenAuthorizationFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.logger(TokenAuthorizationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String path = request.getRequestURI();
		if (!path.startsWith("/iprd/web/")) {
			logger.debug("Bypassing TokenAuthorizationFilter for path: {}");
			filterChain.doFilter(request, response);
			return;
		}

		logger.debug("Applying TokenAuthorizationFilter for path: {}");
		String token = request.getHeader("Authorization");
		if (token == null || !token.startsWith("Bearer ")) {
			logger.warn("No valid Bearer token provided");
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.getWriter().write("Access denied: Missing or invalid Bearer token");
			return;
		}

		JWTPayload jwtPayload = Validation.getJWTToken(token.replace("Bearer ", ""));
		if (jwtPayload == null) {
			logger.warn("Invalid JWT token provided");
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.getWriter().write("Access denied: Invalid JWT token");
			return;
		}

		String userType = jwtPayload.getUser_type();
		String userName = jwtPayload.getName() != null ? jwtPayload.getName() : jwtPayload.getPreferred_username();
		if (!"web".equalsIgnoreCase(userType)) {
			logger.warn(String.format("Access denied for user: %s, user_type: %s",
				userName != null ? userName : "unknown",
				userType != null ? userType : "null"));
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.getWriter().write("Access denied for the User");
			return;
		}

		filterChain.doFilter(request, response);
	}
}