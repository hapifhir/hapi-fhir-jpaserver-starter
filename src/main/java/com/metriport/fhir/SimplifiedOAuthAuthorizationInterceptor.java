package com.metriport.fhir;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationConstants;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

/**
 * A simplified AuthorizationInterceptor.
 * Assumes this server is behind API Gateway or similar upstream service, and
 * its accessed with a tenant in the path.
 */
@SuppressWarnings("ConstantConditions")
@Interceptor(order = AuthorizationConstants.ORDER_AUTH_INTERCEPTOR)
public class SimplifiedOAuthAuthorizationInterceptor extends AuthorizationInterceptor {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory
			.getLogger(SimplifiedOAuthAuthorizationInterceptor.class);

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

		String params = this.convertMapToString(theRequestDetails.getParameters());
		ourLog.info("[AUTH] Validating request {}", params);

		return new RuleBuilder()
				.allowAll()
				.build();
	}

	private String convertMapToString(Map<String, String[]> map) {
		String mapAsString = map.keySet().stream()
				.map(key -> key + "=" + this.convertStringArrayToString(map.get(key), ","))
				.collect(Collectors.joining(", ", "{", "}"));
		return mapAsString;
	}

	private String convertStringArrayToString(String[] strArr, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (String str : strArr)
			sb.append(str).append(delimiter);
		return sb.substring(0, sb.length() - 1);
	}
}