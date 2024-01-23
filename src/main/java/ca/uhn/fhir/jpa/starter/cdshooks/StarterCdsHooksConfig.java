package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.jpa.starter.cr.CrConfigCondition;
import ca.uhn.fhir.jpa.starter.cr.CrProperties;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsHooksDaoAuthorizationSvc;
import ca.uhn.hapi.fhir.cdshooks.config.CdsHooksConfig;
import ca.uhn.hapi.fhir.cdshooks.svc.CdsHooksContextBooter;
import ca.uhn.hapi.fhir.cdshooks.svc.cr.CdsCrSettings;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional({CdsHooksConfigCondition.class, CrConfigCondition.class})
@Import(CdsHooksConfig.class)
public class StarterCdsHooksConfig {
	@Bean
	public CdsHooksProperties cdsHooksProperties() {
		return new CdsHooksProperties();
	}

	@Bean
	public CdsCrSettings cdsCrSettings(CdsHooksProperties cdsHooksProperties) {
		CdsCrSettings settings = CdsCrSettings.getDefault();
		settings.setClientIdHeaderName(cdsHooksProperties.getClientIdHeaderName());
		return settings;
	}

	@Bean
	public CdsHooksContextBooter cdsHooksContextBooter() {
		// ourLog.info("No Spring Context provided.  Assuming all CDS Services will be registered dynamically.");
		return new CdsHooksContextBooter();
	}

	public static class CdsHooksDaoAuthorizationSvc implements ICdsHooksDaoAuthorizationSvc {
		@Override
		public void authorizePreShow(IBaseResource theResource) {}
	}

	@Bean
	public ProviderConfiguration providerConfiguration(CdsHooksProperties cdsProperties, CrProperties crProperties) {
		return new ProviderConfiguration(cdsProperties, crProperties);
	}

	@Bean
	ICdsHooksDaoAuthorizationSvc cdsHooksDaoAuthorizationSvc() {
		return new CdsHooksDaoAuthorizationSvc();
	}

	@Bean
	public ServletRegistrationBean<CdsHooksServlet> cdsHooksRegistrationBean(AutowireCapableBeanFactory beanFactory) {
		CdsHooksServlet cdsHooksServlet = new CdsHooksServlet();
		beanFactory.autowireBean(cdsHooksServlet);

		ServletRegistrationBean<CdsHooksServlet> registrationBean = new ServletRegistrationBean<>();
		registrationBean.setName("cds-hooks servlet");
		registrationBean.setServlet(cdsHooksServlet);
		registrationBean.addUrlMappings("/cds-services/*");
		registrationBean.setLoadOnStartup(1);
		return registrationBean;
	}
}
