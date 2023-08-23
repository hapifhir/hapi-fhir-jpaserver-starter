package ca.uhn.fhir.jpa.starter.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.OAuth2Helper;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizedList;
import ca.uhn.fhir.rest.server.interceptor.auth.SearchNarrowingInterceptor;

@Interceptor
public class CustomSearchNarrowingInterceptor extends SearchNarrowingInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(CustomSearchNarrowingInterceptor.class);
  private final AppProperties config;

	public CustomSearchNarrowingInterceptor(AppProperties config) {
		super();
		this.config = config;
	}

  @Override
  protected AuthorizedList buildAuthorizedList(RequestDetails theRequestDetails) {
    if (isUsingOAuth(theRequestDetails)) {
      String patientId = OAuth2Helper.getClaimAsString(theRequestDetails, "patient");
      if (!Strings.isNullOrEmpty(patientId)) {
        logger.debug("Patient claim specified in authorization token; adding patient compartment to narrow search");
        String compartment = "Patient/" + patientId;
        return new AuthorizedList().addCompartment(compartment);
      }
    }
    return new AuthorizedList();
  }

	private boolean isUsingOAuth(RequestDetails theRequest) {
		return isOAuthEnabled() && OAuth2Helper.hasToken(theRequest);
	}

	private boolean isOAuthEnabled() {
		return config.getOauth().getEnabled();
	}
}
