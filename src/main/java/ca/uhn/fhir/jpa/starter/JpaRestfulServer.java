package ca.uhn.fhir.jpa.starter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import org.springframework.context.annotation.Import;

@WebServlet(urlPatterns = {"/fhir/*"})
@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer(AppProperties appProperties) {
    super(appProperties);
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    // Add your own customization here

  }

}
