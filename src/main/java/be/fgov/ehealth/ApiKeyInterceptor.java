package be.fgov.ehealth;


import be.fgov.ehealth.entities.Tenants;
import be.fgov.ehealth.repository.TenantRepository;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.model.api.TagList;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import com.google.common.base.Strings;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Interceptor
public class ApiKeyInterceptor implements IServerInterceptor {

	@Autowired
	private TenantRepository tenantRepository;

	@Override
	public boolean handleException(RequestDetails requestDetails, BaseServerResponseException e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		return true;
	}

	@Override
	public boolean incomingRequestPostProcessed(RequestDetails requestDetails, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
		Map<String, String[]> params = requestDetails.getParameters();
		List<String> keyList = params.get("api_key")==null?new ArrayList<>():Arrays.stream(params.get("api_key")).toList();


		keyList.stream().filter(x->!Strings.isNullOrEmpty(x)).findFirst().ifPresentOrElse(s -> {

				Tenants tenant = tenantRepository.getTenantByApiKey(s);
				if (tenant == null){
					throw new AuthenticationException("No such api_key!");
				}

		}, () -> {
			throw new AuthenticationException("Please provide api_key!");
		});

		requestDetails.removeParameter("api_key");

		return true;
	}

	@Override
	public void incomingRequestPreHandled(RestOperationTypeEnum restOperationTypeEnum, RequestDetails requestDetails) {

	}

	@Override
	public boolean incomingRequestPreProcessed(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		return true;
	}

	@Override
	public boolean outgoingResponse(RequestDetails requestDetails) {
		return true;
	}

	@Override
	public boolean outgoingResponse(RequestDetails requestDetails, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
		return true;
	}

	@Override
	public boolean outgoingResponse(RequestDetails requestDetails, IBaseResource iBaseResource) {
		return true;
	}

	@Override
	public boolean outgoingResponse(RequestDetails requestDetails, IBaseResource iBaseResource, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
		return true;
	}

	@Override
	public boolean outgoingResponse(RequestDetails requestDetails, ResponseDetails responseDetails, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
		return true;
	}

	@Override
	public boolean outgoingResponse(RequestDetails requestDetails, TagList tagList) {
		return true;
	}

	@Override
	public boolean outgoingResponse(RequestDetails requestDetails, TagList tagList, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException {
		return true;
	}

	@Override
	public BaseServerResponseException preProcessOutgoingException(RequestDetails requestDetails, Throwable throwable, HttpServletRequest httpServletRequest) throws ServletException {
		return null;
	}

	@Override
	public void processingCompletedNormally(ServletRequestDetails servletRequestDetails) {

	}
	
}
