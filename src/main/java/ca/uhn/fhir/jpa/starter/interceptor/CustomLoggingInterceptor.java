package ca.uhn.fhir.jpa.starter.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.MDC;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

@Interceptor
public class CustomLoggingInterceptor {
	private static final String X_CORRELATION_ID = "X-Correlation-Id";


	@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
	public boolean incomingRequestPreProcessed(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		String corelationId = ObjectUtils.isNotEmpty(httpServletRequest.getHeader(X_CORRELATION_ID))
				? httpServletRequest.getHeader(X_CORRELATION_ID)
				: UUID.randomUUID().toString();
		MDC.put(X_CORRELATION_ID, corelationId);
		return true;
	}

	@Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
	public boolean incomingRequestPostProcessed1(RequestDetails requestDetails,
			ServletRequestDetails servletRequestDetails, IBaseResource iBaseResource, ResponseDetails responseDetails,
			HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		httpServletResponse.addHeader(X_CORRELATION_ID, MDC.get(X_CORRELATION_ID));
		return true;
	}

	@Hook(Pointcut.SERVER_PROCESSING_COMPLETED)
	public void incomingRequestPostProcessed(RequestDetails requestDetails,
			ServletRequestDetails servletRequestDetails) {
		MDC.clear();
	}
}
