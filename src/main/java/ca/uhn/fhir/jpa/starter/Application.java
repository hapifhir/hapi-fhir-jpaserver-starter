package ca.uhn.fhir.jpa.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@ServletComponentScan(basePackageClasses = {JpaRestfulServer.class})
@SpringBootApplication(exclude = ElasticsearchRestClientAutoConfiguration.class)
public class Application {

  public static void main(String[] args) {

    System.setProperty("spring.profiles.active", "r4");
    System.setProperty("spring.batch.job.enabled", "false");
    SpringApplication.run(Application.class, args);

    //Server is now accessible at eg. http://localhost:8080/fhir/metadata
  }

  @Bean
  public ServletRegistrationBean servletRegistrationBean() {

    AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();

    ctx.register(FhirTesterConfig.class);

    DispatcherServlet api = new DispatcherServlet(ctx);
    api.setContextClass(AnnotationConfigWebApplicationContext.class);
    api.setContextConfigLocation(FhirTesterConfig.class.getName());
    ServletRegistrationBean registrationBean = new ServletRegistrationBean();
    registrationBean.setServlet(api);
    registrationBean.addUrlMappings("/*");
    registrationBean.setLoadOnStartup(1);
    return registrationBean;

  }
}
