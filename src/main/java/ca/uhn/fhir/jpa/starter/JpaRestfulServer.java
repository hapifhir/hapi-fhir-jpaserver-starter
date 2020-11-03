package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.starter.config.IlaraHealthSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  @Autowired
  IlaraHealthSecurityInterceptor ilaraHealthSecurityInterceptor;

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    // Add your own customization here
    this.registerInterceptor(ilaraHealthSecurityInterceptor);

  }

}
