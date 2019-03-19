package ca.uhn.fhir.jpa.starter.tenant;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class TenantFilter implements Filter {

	private static final String TENANT_HEADER = "X-TenantID";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String tenant = httpRequest.getHeader(TENANT_HEADER);
		if (!StringUtils.isEmpty(tenant)) {
			TenantContext.setCurrentTenant(tenant);

		}
		// pass the request along the filter chain
		chain.doFilter(request, response);

		TenantContext.clear();
	}

	@Override
	public void destroy() {
	}

}
