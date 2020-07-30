package ca.uhn.fhir.jpa.starter;

import java.util.List;

import org.springframework.util.StringUtils;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

@Interceptor
public class CustomAuthorizationInterceptor extends AuthorizationInterceptor {

	private static final String HEADER_NAME = "x-api-key";
	private static final String VALIDATE_API_KEY = "VALIDATE_API_KEY";
	private static final String API_KEY = "API_KEY";

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		try {

			boolean validateApiKey = Boolean.parseBoolean(System.getenv(VALIDATE_API_KEY));
			String token = theRequestDetails.getHeader(HEADER_NAME);

			if (!validateApiKey) {
				return authorizeRequest();
			} else if (!StringUtils.isEmpty(token) && token.equals(System.getenv(API_KEY))) {
				return authorizeRequest();
			} else {				
				return (theRequestDetails.getOperation().equals(RestOperationTypeEnum.METADATA.getCode()))  ? authorizeRequest() : denyRequest();
			}
		} catch (Exception e) {
			return denyRequest();
		}
	}

	private List<IAuthRule> denyRequest() {
		return new RuleBuilder().denyAll().build();
	}

	private List<IAuthRule> authorizeRequest() {
		return new RuleBuilder().allowAll().build();
	}
}
