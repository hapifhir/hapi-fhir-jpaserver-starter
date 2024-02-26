package ca.uhn.fhir.jpa.starter.interceptor;

import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.AppProperties;

import java.util.ArrayList;
import java.util.List;

@Interceptor
public class CapabilityStatementCustomizer {

    private AppProperties config;

    public CapabilityStatementCustomizer(AppProperties config) {
        this.config = config;
    }

    @Hook(Pointcut.SERVER_CAPABILITY_STATEMENT_GENERATED)
    public void customize(IBaseConformance theCapabilityStatement) {
 
        // Cast to the appropriate version
        CapabilityStatement cs = (CapabilityStatement) theCapabilityStatement;
 
        if (config.getOauth().getEnabled()) {

            // Customize the CapabilityStatement to add a security extension
            cs
                .getRestFirstRep()
                .setSecurity(getSecurityComponent());
        }
    }

	private CapabilityStatementRestSecurityComponent getSecurityComponent() {
		CapabilityStatementRestSecurityComponent security = new CapabilityStatementRestSecurityComponent();
		
        List<Extension> extensions = new ArrayList<>();
		extensions.add(new Extension(
            "authorize", new UriType(config.getOauth().getAuthorize_url())));
		extensions.add(new Extension(
            "token", new UriType(config.getOauth().getToken_url())));
		extensions.add(new Extension(
            "manage", new UriType(config.getOauth().getManage_url())));
		
        List<Extension> extensionsList = new ArrayList<>();
		extensionsList.add((Extension) new Extension(
				new UriType("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris"))
						.setExtension(extensions));
		security.setExtension(extensionsList);
		return security;
	}
}
