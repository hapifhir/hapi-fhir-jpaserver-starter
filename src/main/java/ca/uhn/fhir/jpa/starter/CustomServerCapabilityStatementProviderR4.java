package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
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
	private String myImplementationDescription;
	
	public CustomServerCapabilityStatementProviderR4(RestfulServer theServer,String myImplementationDescription) {
		super(theServer);
		this.myImplementationDescription = myImplementationDescription;
	}
	
	@Override
	public IBaseConformance getServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
		capabilityStatement =  (CapabilityStatement) super.getServerConformance(theRequest, theRequestDetails);
		capabilityStatement.getRest().get(0).setSecurity(getSecurityComponent());
		capabilityStatement.setImplementation(new CapabilityStatementImplementationComponent().setDescription(myImplementationDescription));
		return capabilityStatement;
	}

	private static CapabilityStatementRestSecurityComponent getSecurityComponent() {
		CapabilityStatementRestSecurityComponent security = new CapabilityStatementRestSecurityComponent();
		List<Extension> extensions = new ArrayList<Extension>();
		extensions.add(new Extension("token", new UriType(OAUTH_TOKEN_URL)));
		extensions.add(new Extension("manage", new UriType(OAUTH_MANAGE_URL)));
		List<Extension> extensionsList = new ArrayList<Extension>();
		extensionsList.add((Extension) new Extension(
				new UriType("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris"))
						.setExtension(extensions));
		security.setExtension(extensionsList);
		return security;
	}
}
