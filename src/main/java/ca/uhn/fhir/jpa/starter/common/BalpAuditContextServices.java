package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ca.uhn.fhir.storage.interceptor.balp.IBalpAuditContextServices;
import joptsimple.internal.Strings;
import org.hl7.fhir.r4.model.Reference;

import javax.annotation.Nonnull;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class BalpAuditContextServices implements IBalpAuditContextServices {

	/**
	 * Here we are just hard-coding a simple display name. In a real implementation
	 * we should use the actual identity of the requesting client.
	 */
	@Nonnull
	@Override
	public Reference getAgentClientWho(RequestDetails theRequestDetails) {

		String userAgent = theRequestDetails.getHeader("User-Agent");
		if (isBlank(userAgent)) {
			userAgent = "Unknown User Agent";
		}
		Reference client = new Reference();
		client.setDisplay(userAgent);

		return client;
	}

	/**
	 * Here we are just hard-coding a simple display name. In a real implementation
	 * we should use the actual identity of the requesting user.
	 */
	@Nonnull
	@Override
	public Reference getAgentUserWho(RequestDetails theRequestDetails) {
		Reference user = new Reference();
		user.setDisplay("["+getNetworkAddress(theRequestDetails)+"] "+getUsername(theRequestDetails));

		return user;
	}

	/**
	 * Provide the requesting network address to include in the AuditEvent
	 * <p>
	 * Because this is a public server and these audit events will be visible
	 * to the outside world, we mask the latter half of the requesting IP
	 * address in order to not leak the identity of our users.
	 */
	@Override
	public String getNetworkAddress(RequestDetails theRequestDetails) {
		ServletRequestDetails srd = (ServletRequestDetails) theRequestDetails;

		String remoteAddr = defaultString(srd.getServletRequest().getRemoteAddr(), "Unable to retrieve IP address");

		String[] parts = remoteAddr.split("\\.");
		// Obscure part of the IP address
		if (parts.length >= 4) {
			parts[2] = "X";
			parts[3] = "X";
		}
		return Strings.join(parts, ".");
	}

	/**
	 * Provide the requesting users username in the AuditEvent
	 */
	private String getUsername(RequestDetails theRequestDetails) {
		ServletRequestDetails srd = (ServletRequestDetails) theRequestDetails;

        return defaultString(srd.getServletRequest().getHeader("X-USERNAME"), "Unknown User");
	}
}
