package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider;

public class CustomServerCapabilityStatementProviderR4 extends ServerCapabilityStatementProvider {
	private static final String OAUTH_TOKEN_URL = System.getenv("OAUTH_TOKEN_URL");
	private static final String OAUTH_MANAGE_URL = System.getenv("OAUTH_MANAGE_URL");
	
	private CapabilityStatement capabilityStatement;

	public CustomServerCapabilityStatementProviderR4(RestfulServer theServer) {
		super(theServer);
	}
	
	@Override
	public IBaseConformance getServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
		capabilityStatement =  (CapabilityStatement) super.getServerConformance(theRequest, theRequestDetails);
		capabilityStatement.getRest().get(0).setSecurity(getSecurityComponent());
		return capabilityStatement;
	}
	
	private static CapabilityStatementRestSecurityComponent getSecurityComponent() {
		CapabilityStatementRestSecurityComponent security = new CapabilityStatementRestSecurityComponent();
		List<Extension> extensions = new ArrayList<Extension>();
		if (!ObjectUtils.isEmpty(OAUTH_TOKEN_URL)) {
			extensions.add(new Extension("token", new UriType(OAUTH_TOKEN_URL)));
		}
		if (!ObjectUtils.isEmpty(OAUTH_MANAGE_URL)) {			
			extensions.add(new Extension("manage", new UriType(OAUTH_MANAGE_URL)));
		}
		List<Extension> extensionsList = new ArrayList<Extension>();
		extensionsList.add((Extension) new Extension(
				new UriType("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris"))
						.setExtension(extensions));
		security.setExtension(extensionsList);
		return security;
	}
}
