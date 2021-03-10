package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.UriType;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;

import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.r4.JpaConformanceProviderR4;
import ca.uhn.fhir.jpa.searchparam.registry.ISearchParamRegistry;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;

@Configuration
public class CustomServerCapabilityStatementProviderR4 extends JpaConformanceProviderR4 {

	private static final String EXTENSION_MAP = System.getenv("OAUTH_CAPABILITY_EXTENSION");
	
	private CapabilityStatement capabilityStatement;

	public CustomServerCapabilityStatementProviderR4 () {
		super();
	}
	
	public CustomServerCapabilityStatementProviderR4(@Nonnull RestfulServer theRestfulServer, @Nonnull IFhirSystemDao<Bundle, Meta> theSystemDao, @Nonnull DaoConfig theDaoConfig, @Nonnull ISearchParamRegistry theSearchParamRegistry) {
		super(theRestfulServer, theSystemDao, theDaoConfig, theSearchParamRegistry);
	}
	
	
	@Override
	public CapabilityStatement getServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
		capabilityStatement = super.getServerConformance(theRequest, theRequestDetails);
		capabilityStatement.getRest().get(0).setSecurity(getSecurityComponent());
		return capabilityStatement;
	}
	
	private static CapabilityStatementRestSecurityComponent getSecurityComponent() {
		Gson json = new Gson();
		Map<String, String> oauthUrl = json.fromJson(EXTENSION_MAP, HashMap.class);
		CapabilityStatementRestSecurityComponent security = new CapabilityStatementRestSecurityComponent();
		List<Extension> extensions = new ArrayList<Extension>();
		oauthUrl.entrySet().forEach(entry -> {
			extensions.add(new Extension(entry.getValue(), new UriType(entry.getKey())));
		});
		List<Extension> extensionsList = new ArrayList<Extension>();
		extensionsList.add((Extension) new Extension(
				new UriType("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris"))
						.setExtension(extensions));
		security.setExtension(extensionsList);
		return security;
	}
}
