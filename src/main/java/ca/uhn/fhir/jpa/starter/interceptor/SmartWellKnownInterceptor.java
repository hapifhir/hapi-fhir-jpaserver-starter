package ca.uhn.fhir.jpa.starter.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.WellknownEndpointHelper;

@Interceptor
public class SmartWellKnownInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(SmartWellKnownInterceptor.class);
	private final AppProperties config;

  public SmartWellKnownInterceptor(AppProperties config) {
    super();
    this.config = config;
  }

  @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
  public boolean processIncomingRequest(HttpServletRequest request, HttpServletResponse response) {
    String uri = request.getRequestURI();
    if (uri.endsWith("/.well-known/smart-configuration")) {
      response.setContentType("application/json");
      response.setStatus(200);
      try {
        response.getWriter().write(WellknownEndpointHelper.getWellKnownJson(this.config));
        response.getWriter().flush();
      } catch (IOException e) {
        logger.error("Failed to write well-known smart-configuration to response", e);
      }
      return false;
    }
    return true;
  }
}
