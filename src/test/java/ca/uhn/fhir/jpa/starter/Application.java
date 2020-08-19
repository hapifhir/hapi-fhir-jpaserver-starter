package ca.uhn.fhir.jpa.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan(basePackageClasses = {JpaRestfulServer.class})
@SpringBootApplication(exclude = ElasticsearchRestClientAutoConfiguration.class)
public class Application {

  public static void main(String[] args) {

    System.setProperty("spring.profiles.active", "r4");
    System.setProperty("spring.batch.job.enabled", "false");
    SpringApplication.run(Application.class, args);

    //Server is now accessible at eg. http://localhost:8080/metadata
  }
}
