package ch.ahdis.matchbox;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.server.RestfulServer;
import ch.ahdis.matchbox.spring.boot.autoconfigure.MutableHttpServletRequest;
import ch.ahdis.matchbox.util.VersionUtil;

public class MatchboxRestfulServer extends RestfulServer {
  
  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MatchboxRestfulServer.class);


  private static final long serialVersionUID = 1L;

  public MatchboxRestfulServer(FhirContext fhirContext) {
    super(fhirContext);
  }

  @Override
  protected void handleRequest(RequestTypeEnum theRequestType, HttpServletRequest theRequest,
      HttpServletResponse theResponse) throws ServletException, IOException {

	  getServerConformanceMethod().setCacheMillis(0L);

    MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(theRequest);
    super.handleRequest(theRequestType, mutableRequest, theResponse);
  }
  
  @Override
  protected void initialize() throws ServletException {
    super.initialize();
    ourLog.info(VersionUtil.getPoweredBy());
  }

}
