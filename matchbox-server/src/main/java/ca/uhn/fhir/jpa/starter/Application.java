package ca.uhn.fhir.jpa.starter;

import ch.ahdis.matchbox.config.MatchboxStaticResourceConfig;
import ch.ahdis.matchbox.config.MatchboxMcpConfig;
import ch.ahdis.matchbox.spring.MatchboxEventListener;
import ch.ahdis.matchbox.terminology.RegistryWs;
import ch.ahdis.matchbox.validation.gazelle.GazelleValidationWs;

import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.jpa.starter.annotations.OnEitherVersion;
import ca.uhn.fhir.jpa.starter.common.FhirServerConfigR4;
import ca.uhn.fhir.jpa.starter.mdm.MdmConfig;
import ca.uhn.fhir.rest.server.RestfulServer;
import ch.ahdis.matchbox.config.MatchboxJpaConfig;

@ServletComponentScan(basePackageClasses = {RestfulServer.class})
@SpringBootApplication(exclude = {ElasticsearchRestClientAutoConfiguration.class})
@Import({
	MdmConfig.class,
	MatchboxJpaConfig.class,
	FhirServerConfigR4.class,
	MatchboxEventListener.class,
	GazelleValidationWs.class,
  RegistryWs.class,
  MatchboxStaticResourceConfig.class,
  MatchboxMcpConfig.class})
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {

    SpringApplication.run(Application.class, args);

    //Server is now accessible at eg. http://localhost:8080/fhir/metadata
    //UI is now accessible at http://localhost:8080/
  }

  @Override
  protected SpringApplicationBuilder configure(
    SpringApplicationBuilder builder) {
    return builder.sources(Application.class);
  }

  @Autowired
  AutowireCapableBeanFactory beanFactory;

  @Bean(name = "hapiServletRegistration")
  @Conditional(OnEitherVersion.class)
  public ServletRegistrationBean<RestfulServer> hapiServletRegistration(final RestfulServer restfulServer) {
    ServletRegistrationBean<RestfulServer> servletRegistrationBean = new ServletRegistrationBean<>();
    beanFactory.autowireBean(restfulServer);
    servletRegistrationBean.setServlet(restfulServer);
    servletRegistrationBean.addUrlMappings("/fhir/*");
    servletRegistrationBean.setLoadOnStartup(1);

    return servletRegistrationBean;
  }
}
