package ca.uhn.fhir.jpa.starter.tenancy;

import ca.uhn.fhir.i18n.HapiLocalizer;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.tenant.ITenantIdentificationStrategy;
import ca.uhn.fhir.util.UrlPathTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderTenantIdentificationStrategy implements ITenantIdentificationStrategy {
    private static final Logger ourLog = LoggerFactory.getLogger(HeaderTenantIdentificationStrategy.class);
		public String defaultTenantHeaderValue;
		public String headerKey;

    public HeaderTenantIdentificationStrategy(String headerKey, String defaultTenantHeaderValue) {
			this.headerKey = headerKey;
			this.defaultTenantHeaderValue = defaultTenantHeaderValue;
    }

    public void extractTenant(UrlPathTokenizer theUrlPathTokenizer, RequestDetails theRequestDetails) {
        String host = theRequestDetails.getHeader(this.headerKey);
        if (host != null) {
						ourLog.info(host);
						if (host.equals(this.defaultTenantHeaderValue)) {
							theRequestDetails.setTenantId("DEFAULT");
							return;
						}
            theRequestDetails.setTenantId(host);
						return;
        }

				HapiLocalizer localizer = theRequestDetails.getServer().getFhirContext().getLocalizer();
				String var10002 = Msg.code(307);
				throw new InvalidRequestException(var10002 + localizer.getMessage(RestfulServer.class, "rootRequest.multitenant", new Object[0]));
    }

    public String massageServerBaseUrl(String theFhirServerBase, RequestDetails theRequestDetails) {
        Validate.notNull(theRequestDetails.getTenantId(), "theTenantId is not populated on this request", new Object[0]);
        return theFhirServerBase + "/";
    }
}