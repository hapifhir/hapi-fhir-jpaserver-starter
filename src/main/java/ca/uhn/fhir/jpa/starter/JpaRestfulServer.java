package ca.uhn.fhir.jpa.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ch.ahdis.matchbox.spring.boot.autoconfigure.MutableHttpServletRequest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void handleRequest(RequestTypeEnum theRequestType, HttpServletRequest theRequest,
      HttpServletResponse theResponse) throws ServletException, IOException {
    MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(theRequest);
    super.handleRequest(theRequestType, mutableRequest, theResponse);
  }
  
  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    // Add your own customization here

  }

}
