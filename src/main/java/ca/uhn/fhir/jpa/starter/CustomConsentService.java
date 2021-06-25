package ca.uhn.fhir.jpa.starter;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.springframework.stereotype.Service;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;

@Service
public class CustomConsentService implements IConsentService {

  private static final String OAUTH_CLAIM_NAME = "patient";

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(CustomConsentService.class);

  private OAuth2Helper oAuth2Helper = new OAuth2Helper();
  DaoRegistry daoRegistry;

  public CustomConsentService() {}

  public CustomConsentService(DaoRegistry daoRegistry) {
    this.daoRegistry = daoRegistry;
  }

  @Override
  public ConsentOutcome startOperation(RequestDetails theRequestDetails,
      IConsentContextServices theContextServices) {
    if (oAuth2Helper.checkInPatientCompartment(theRequestDetails.getResourceName())) {
      return ConsentOutcome.AUTHORIZED;
    }
    if (theRequestDetails.getHeader("Authorization") != null) {
      String patientId = getPatientFromToken(theRequestDetails);
      String resourceName = null;
      boolean proceed = false;
      switch (theRequestDetails.getRequestType().toString()) {
        case "POST":
          resourceName = theRequestDetails.getResourceName();
          proceed = validateResource(resourceName, patientId, theRequestDetails.getResource());
          break;
        case "PUT":
          resourceName = theRequestDetails.getResourceName();
          IBaseResource putResource = getResourceFromDB(theRequestDetails.getRequestPath());
          if (validateResource(resourceName, patientId, putResource)) {
            proceed = validateResource(resourceName, patientId, theRequestDetails.getResource());
          }
          break;
        case "PATCH":
          resourceName = theRequestDetails.getResourceName();
          IBaseResource patchResource = getResourceFromDB(theRequestDetails.getRequestPath());
          proceed = validateResource(resourceName, patientId, patchResource);
          break;
        case "GET":
          resourceName = theRequestDetails.getResourceName();
          if (theRequestDetails.getRequestPath().split("/").length > 1) {
            IBaseResource getResource = getResourceFromDB(theRequestDetails.getRequestPath());
            proceed = validateResource(resourceName, patientId, getResource);
          }
          proceed = true;
          break;
        default:
          proceed = true;
          break;
      }
      return proceed ? ConsentOutcome.PROCEED : ConsentOutcome.REJECT;
    }
    return ConsentOutcome.AUTHORIZED;
  }

  @Override
  public ConsentOutcome canSeeResource(RequestDetails theRequestDetails, IBaseResource theResource,
      IConsentContextServices theContextServices) {
    if (oAuth2Helper.checkInPatientCompartment(theRequestDetails.getResourceName())) {
      return ConsentOutcome.AUTHORIZED;
    }
    String patientId = getPatientFromToken(theRequestDetails);
    String resourceName = theResource.getClass().getSimpleName();
    return validateResource(resourceName, patientId, theResource) ? ConsentOutcome.PROCEED
        : ConsentOutcome.REJECT;
  }

  private IBaseResource getResourceFromDB(String requestedPath) {
    String[] requestDetail = requestedPath.split("/");
    return daoRegistry.getResourceDao(requestDetail[0]).read(new IdType(requestDetail[1]));
  }


  private boolean validateResource(String resourceName, String patientId,
      IBaseResource theResource) {
    if (patientId != null) {
      String patientRef = "Patient/" + patientId;
      switch (resourceName) {
        case "Task":
          return validateTaskResource((Task) theResource, patientRef);
        default:
          return false;
      }
    }
    return true;
  }

  private boolean validateTaskResource(Task task, String patientRef) {
    try {
      Reference ref = (Reference) task.getNamedProperty("for").getValues().get(0);
      if (ref.getReference().equals(patientRef)) {
        return true;
      }
      return false;
    } catch (Exception e) {
      log.error("Unable to find patient reference in Task resource");
      return false;
    }
  }

  private String getPatientFromToken(RequestDetails theRequestDetails) {
    String token = theRequestDetails.getHeader("Authorization");
    token = token.substring(CustomAuthorizationInterceptor.getTokenPrefix().length());
    DecodedJWT jwt = JWT.decode(token);
    String patRefId = oAuth2Helper.getPatientReferenceFromToken(jwt, OAUTH_CLAIM_NAME);
    return patRefId;
  }
}
