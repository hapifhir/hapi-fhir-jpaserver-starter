package ca.uhn.fhir.jpa.starter;

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
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@ServletComponentScan(basePackageClasses = {JpaRestfulServer.class}, basePackages = "ca.uhn.fhir.jpa.starter")
@SpringBootApplication(exclude = ElasticsearchRestClientAutoConfiguration.class)
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {

    System.setProperty("spring.batch.job.enabled", "false");
    SpringApplication.run(Application.class, args);

    //Server is now accessible at eg. http://localhost:8080/hapi-fhir-jpaserver/fhir/metadata
    //UI is now accessible at http://localhost:8080/hapi-fhir-jpaserver/
  }

  @Override
  protected SpringApplicationBuilder configure(
    SpringApplicationBuilder builder) {
    return builder.sources(Application.class);
  }

  @Autowired
  AutowireCapableBeanFactory beanFactory;

  @Bean
  public ServletRegistrationBean hapiServletRegistration() {
    ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
    JpaRestfulServer jpaRestfulServer = new JpaRestfulServer();
    beanFactory.autowireBean(jpaRestfulServer);
    servletRegistrationBean.setServlet(jpaRestfulServer);
    servletRegistrationBean.addUrlMappings("/hapi-fhir-jpaserver/fhir/*");
    servletRegistrationBean.setLoadOnStartup(1);
    return servletRegistrationBean;
  }

  @Bean
  public ServletRegistrationBean overlayRegistrationBean() {

    AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext();
    annotationConfigWebApplicationContext.register(FhirTesterConfig.class);
    DispatcherServlet dispatcherServlet = new DispatcherServlet(annotationConfigWebApplicationContext);
    dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);
    dispatcherServlet.setContextConfigLocation(FhirTesterConfig.class.getName());

    ServletRegistrationBean registrationBean = new ServletRegistrationBean();
    registrationBean.setServlet(dispatcherServlet);
    registrationBean.addUrlMappings("/hapi-fhir-jpaserver/*", "/*");
    registrationBean.setLoadOnStartup(1);
    return registrationBean;

  }
}
