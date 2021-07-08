package ca.uhn.fhir.jpa.starter;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private DaoRegistry daoRegistry;

  public CustomConsentService() {}

  public CustomConsentService(DaoRegistry daoRegistry) {
    this.daoRegistry = daoRegistry;
  }

  @Override
  public ConsentOutcome startOperation(RequestDetails theRequestDetails,
      IConsentContextServices theContextServices) {
    /*
     * Returning authorized if there is no Authorization header present or if requested resource is
     * present in Patient Compartment For both these cases all the consent logic is in authorization
     * intercepter rules.
     */
    if (ObjectUtils.isEmpty(theRequestDetails.getHeader("Authorization"))
        || oAuth2Helper.canBeInPatientCompartment(theRequestDetails.getResourceName())) {
      return ConsentOutcome.AUTHORIZED;
    }
    String patientId = getPatientFromToken(theRequestDetails);
    String resourceName = null;
    IBaseResource existingResource = null;
    boolean proceed = false;
    switch (theRequestDetails.getRequestType().toString()) {
      case "POST":
        resourceName = theRequestDetails.getResourceName();
        proceed = isResourceValid(resourceName, patientId, theRequestDetails.getResource());
        break;
      case "PUT":
        resourceName = theRequestDetails.getResourceName();
        existingResource = getResourceFromDB(theRequestDetails.getRequestPath());
        if (isResourceValid(resourceName, patientId, existingResource)) {
          proceed = isResourceValid(resourceName, patientId, theRequestDetails.getResource());
        }
        break;
      case "PATCH":
        resourceName = theRequestDetails.getResourceName();
        existingResource = getResourceFromDB(theRequestDetails.getRequestPath());
        if (isResourceValid(resourceName, patientId, existingResource)) {
          // As Patch request body doesn't contain any Resource we need to handle it
          // differently
          proceed = isPatchRequestBodyValid(resourceName,
              new String(theRequestDetails.getRequestContentsIfLoaded()));
        }
        break;
      default:
        proceed = true;
        break;
    }
    return proceed ? ConsentOutcome.PROCEED : ConsentOutcome.REJECT;
  }

  @Override
  public ConsentOutcome canSeeResource(RequestDetails theRequestDetails, IBaseResource theResource,
      IConsentContextServices theContextServices) {
    /*
     * Returning authorized if there is no Authorization header present or if requested resource is
     * present in Patient Compartment For both this cases all the consent logic is in authorization
     * intercepter rules
     */
    if (ObjectUtils.isEmpty(theRequestDetails.getHeader("Authorization"))
        || oAuth2Helper.canBeInPatientCompartment(theRequestDetails.getResourceName())) {
      return ConsentOutcome.AUTHORIZED;
    }
    String patientId = getPatientFromToken(theRequestDetails);
    String resourceName = theResource.getClass().getSimpleName();
    return isResourceValid(resourceName, patientId, theResource) ? ConsentOutcome.PROCEED
        : ConsentOutcome.REJECT;
  }

  private IBaseResource getResourceFromDB(String requestedPath) {
    String[] requestDetail = requestedPath.split("/");
    return daoRegistry.getResourceDao(requestDetail[0]).read(new IdType(requestDetail[1]));
  }

  private boolean isResourceValid(String resourceName, String patientId,
      IBaseResource theResource) {
    if (patientId != null) {
      String patientRef = "Patient/" + patientId;
      switch (resourceName) {
        case "Task":
          return isReferanceValid((Resource) theResource, patientRef, "for");
        default:
          return false;
      }
    }
    return true;
  }

  private boolean isReferanceValid(Resource theResource, String patientRef,
      String refPropertyName) {
    try {
      List<Base> refList = theResource.getNamedProperty(refPropertyName).getValues();
      for (Base ref : refList) {
        if (((Reference)ref).getReference().equals(patientRef)) {
          return true;
        }
      }
      return false;
    } catch (Exception e) {
      log.error("Unable to find patient reference in " + theResource.getClass().getCanonicalName()
          + " resource");
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

  /*
   * For Patch Request we take Request body(byte array) and convert to json string Then using
   * ObjectMapper we can convert json string to List of key value pairs(map object)
   */
  @SuppressWarnings("unchecked")
  private boolean isPatchRequestBodyValid(String resourceName, String content) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      List<Map<String, String>> opreationList = mapper.readValue(content, List.class);
      switch (resourceName) {
        case "Task":
          return isPatchRequestValid(opreationList, "/for/reference");
        default:
          return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  /*
   * Here we check if request performs any patch operation type (add, insert, delete, replace or
   * move) on reference. If so we deny request by returning false
   */
  private boolean isPatchRequestValid(List<Map<String, String>> opreationList, String refPath) {
    for (Map<String, String> opreation : opreationList) {
      if (opreation.get("path").equals(refPath))
        return false;
    }
    return true;
  }
}
