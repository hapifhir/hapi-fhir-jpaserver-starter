package ca.uhn.fhir.jpa.starter.interceptor;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;

import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.OAuth2Helper;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;

/*
 * NOTE: The reason we are using a consent interceptor is to add filtering for Tasks resources that
 * are NOT in the patient compartment (prior to R5). When OAuth is enabled and a patient claim exists
 * in the authorization token, we don't allow operations on resources that refer to other patients.
 * For resources in the patient compartment, the authorization interceptor handles it.
 */
@Service
public class CustomConsentService implements IConsentService {
  private static final Logger logger = LoggerFactory.getLogger(CustomConsentService.class);
  private final DaoRegistry daoRegistry;
  private final AppProperties config;

  public CustomConsentService(DaoRegistry daoRegistry, AppProperties config) {
    this.daoRegistry = daoRegistry;
    this.config = config;
  }

  @Override
  public ConsentOutcome canSeeResource(RequestDetails theRequestDetails, IBaseResource theResource,
      IConsentContextServices theContextServices) {

    if (!isUsingOAuth(theRequestDetails)
        || !isTaskRequest(theRequestDetails)) {
      return ConsentOutcome.PROCEED;
    }
    String patientId = getPatientClaim(theRequestDetails);
    if (Strings.isNullOrEmpty(patientId)) {
      return ConsentOutcome.PROCEED;
    }

    boolean proceed = isResourceForPatient(theResource, patientId);
    if (logger.isDebugEnabled()) {
      String decision = proceed ? "Allowing" : "Denying";
      logger.debug("{} canSeeResource for '{}' resource '{}'", decision, theRequestDetails.getResourceName(), theResource.getIdElement());
    }
    return proceed ? ConsentOutcome.PROCEED : ConsentOutcome.REJECT;
  }

  @Override
  public ConsentOutcome startOperation(RequestDetails theRequestDetails,
      IConsentContextServices theContextServices) {

    if (!isUsingOAuth(theRequestDetails)
        || !isTaskRequest(theRequestDetails)) {
      return ConsentOutcome.PROCEED;
    }
    String patientId = getPatientClaim(theRequestDetails);
    if (Strings.isNullOrEmpty(patientId)) {
      return ConsentOutcome.PROCEED;
    }

    switch (theRequestDetails.getRequestType()) {
      case POST:
        return startPostOperation(theRequestDetails, patientId);
      case PUT:
        return startPutOperation(theRequestDetails, patientId);
      case PATCH:
        return startPatchOperation(theRequestDetails, patientId);
      case DELETE:
        return startDeleteOperation(theRequestDetails, patientId);
      default:
        // The other types should be handled by canSeeResource
        return ConsentOutcome.PROCEED;
    }
  }

  private boolean isUsingOAuth(RequestDetails theRequest) {
    return isOAuthEnabled() && OAuth2Helper.hasToken(theRequest);
  }

  private boolean isOAuthEnabled() {
    return config.getOauth().getEnabled();
  }

  private boolean isTaskRequest(RequestDetails theRequest) {
    String resourceName = theRequest.getResourceName();
    if (!Strings.isNullOrEmpty(resourceName) && resourceName.equalsIgnoreCase("Task")) {
      // Need to test for null because a bundle will return a null value
      return true;
    }
    return false;
  }

  private String getPatientClaim(RequestDetails theRequestDetails) {
    return OAuth2Helper.getClaimAsString(theRequestDetails, "patient");
  }

  private ConsentOutcome startPostOperation(RequestDetails theRequestDetails, String patientId) {
    boolean proceed = isResourceForPatient(theRequestDetails.getResource(), patientId);
    if (logger.isDebugEnabled()) {
      String decision = proceed ? "Allowing" : "Denying";
      logger.debug("{} POST operation", decision);
    }
    return proceed ? ConsentOutcome.PROCEED : ConsentOutcome.REJECT;
  }

  private ConsentOutcome startPutOperation(RequestDetails theRequestDetails, String patientId) {
    // Check for the persistent resource first to ensure that a 404 error is returned when trying
    // to update any resource that is NOT for the patient in the token. Otherwise, this operation
    // would be inconsistent the behavior of canSeeResource which simply "hides" resources rather
    // than returning a 403.
    boolean proceed = isResourceForPatient(getPersistentResource(theRequestDetails), patientId)
      && isResourceForPatient(theRequestDetails.getResource(), patientId);
    if (logger.isDebugEnabled()) {
      String decision = proceed ? "Allowing" : "Denying";
      logger.debug("{} PUT operation", decision);
    }
    return proceed ? ConsentOutcome.PROCEED : ConsentOutcome.REJECT;
  }

  private ConsentOutcome startPatchOperation(RequestDetails theRequestDetails, String patientId) {
    // Check for the persistent resource first to ensure that a 404 error is returned when trying
    // to update any resource that is NOT for the patient in the token. Otherwise, this operation
    // would be inconsistent the behavior of canSeeResource which simply "hides" resources rather
    // than returning a 403.
    boolean proceed = isResourceForPatient(getPersistentResource(theRequestDetails), patientId)
      && isPatchRequestBodyValid(theRequestDetails, patientId);
    if (logger.isDebugEnabled()) {
      String decision = proceed ? "Allowing" : "Denying";
      logger.debug("{} PATCH operation", decision);
    }
    return proceed ? ConsentOutcome.PROCEED : ConsentOutcome.REJECT;
  }

  private ConsentOutcome startDeleteOperation(RequestDetails theRequestDetails, String patientId) {
    boolean proceed = isResourceForPatient(getPersistentResource(theRequestDetails), patientId);
    if (logger.isDebugEnabled()) {
      String decision = proceed ? "Allowing" : "Denying";
      logger.debug("{} DELETE operation", decision);
    }
    return proceed ? ConsentOutcome.PROCEED : ConsentOutcome.REJECT;
  }

  private IBaseResource getPersistentResource(RequestDetails theRequestDetails) {
    String[] parts = theRequestDetails.getRequestPath().split("/");
    return daoRegistry.getResourceDao(parts[0]).read(new IdType(parts[1]), theRequestDetails);
  }

  private boolean isResourceForPatient(IBaseResource theResource, String patientId) {
    if (theResource == null) {
      return false;
    }
    return isResourcePropertyForPatient(theResource, "for", patientId);
  }

  private boolean isResourcePropertyForPatient(IBaseResource theResource, String propertyName, String patientId) {
    try {
      Property property = ((Resource)theResource).getNamedProperty(propertyName);
      Reference reference = (Reference)property.getValues().get(0);
      if (reference.getReference().equalsIgnoreCase("Patient/" + patientId)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Property name '{}' is a reference for the patient in the token", propertyName);
        }
        return true;
      }
    } catch (RuntimeException e) {
      logger.error("Unexpected error checking patient reference for property: {}", e.getMessage());
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Property name '{}' is NOT a reference for the patient in the token", propertyName);
    }
    return false;
  }

  private boolean isPatchRequestBodyValid(RequestDetails theRequestDetails, String patientId) {
    String contentType = theRequestDetails.getHeader("content-type");
    if (contentType.equalsIgnoreCase("application/json-patch+json")) {
      return isJsonPatchRequestBodyValid(theRequestDetails, patientId);
    }

    // No other patch types supported
    logger.warn("Invalid Content-Type for PATCH operation: {}", contentType);
    return false;
  }

  private boolean isJsonPatchRequestBodyValid(RequestDetails theRequestDetails, String patientId) {
    try {
      DocumentContext json = JsonPath.parse(new String(theRequestDetails.loadRequestContents()));
      return isJsonPatchRequestPropertyValid(json, "for", patientId);
    } catch (RuntimeException e) {
      logger.error("Unexpected error checking JSON Patch request body: {}", e.getMessage());
    }
    return false;
  }

  private boolean isJsonPatchRequestPropertyValid(DocumentContext json, String propertyName, String patientId) {
    String pathValue = String.format("/%s/reference", propertyName);
    Filter filter = filter(
      where("path").is(pathValue).and("value").ne("Patient/" + patientId)
    );
    List<String> invalidOperations = json.read("$[?]", filter);
    boolean proceed = invalidOperations.isEmpty();
    if (logger.isDebugEnabled()) {
      logger.debug("Property name '{}' is NOT a reference for the patient in the token", propertyName);
    }
    return proceed;
  }
}
