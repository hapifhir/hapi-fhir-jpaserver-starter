package stepdefs;

import config.EndpointURLs;
import config.EnvGlobals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.runtime.Env;
import general.ReusableFunctions;
import payloads.HealthCareService;

import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class HealthCareServices {

    @Given("I Set POST Health Care Service service api endpoint")
    public void i_Set_POST_Health_Care_Service_service_api_endpoint() {
        endPoint = EndpointURLs.HEALTHCARESERVICES_URL;
        RequestPayLoad = HealthCareService.createHealthCareService(EnvGlobals.managingOrgId,EnvGlobals.LocationOrgId);
    }

    @Then("I receive valid Response for POST Health Care Service service")
    public void i_receive_valid_Response_for_POST_Health_Care_Service_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.healthCaeServiceId = ReusableFunctions.getResponsePath("id");
        validation.HealthCareService.validatePostResponse(EnvGlobals.managingOrgId,EnvGlobals.LocationOrgId);

    }

    @Given("I Set GET Health Care Service api endpoint")
    public void i_Set_GET_Health_Care_Service_api_endpoint() {
        endPoint = EndpointURLs.GET_HEALTHCARESERVICES_URL;
        endPoint= String.format(endPoint, EnvGlobals.healthCaeServiceId);
    }

    @Then("I receive valid Response for GET Health Care Service service")
    public void i_receive_valid_Response_for_GET_Health_Care_Service_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.HealthCareService.validatePostResponse(EnvGlobals.managingOrgId,EnvGlobals.LocationOrgId);
        validation.HealthCareService.validateHealthCareId(EnvGlobals.healthCaeServiceId);
    }

    @Then("I receive valid Response for GET Health Care Service service for specific location")
    public void i_receive_valid_Response_for_GET_Health_Care_Service_service_for_specific_loc() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.HealthCareService.validateLocation(EnvGlobals.LocationOrgId);
    }

    @Then("I receive valid Response for GET Health Care Service service for specific Organization")
    public void i_receive_valid_Response_for_GET_Health_Care_Service_service_for_specific_org() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.HealthCareService.validateOrganization(EnvGlobals.managingOrgId);
    }

    @Given("I Set GET Health Care Service api endpoint for specific location")
    public void i_Set_GET_Health_Care_Service_api_endpoint_for_specific_location() {
        endPoint = EndpointURLs.GET_HEALTHCARESERVICES_BY_LOCATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.LocationOrgId);
    }

    @Given("I Set GET Health Care Service api endpoint for specific Organization")
    public void i_Set_GET_Health_Care_Service_api_endpoint_for_specific_Organization() {
        endPoint = EndpointURLs.GET_HEALTHCARESERVICES_BY_ORGANIZATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.managingOrgId);
    }

    @Given("I Set PUT Facility Health Care Service api endpoint")
    public void i_Set_PUT_Facility_Health_Care_Service_api_endpoint() {
        endPoint = EndpointURLs.GET_HEALTHCARESERVICES_URL;
        endPoint= String.format(endPoint, EnvGlobals.healthCaeServiceId);
        RequestPayLoad = HealthCareService.updateHealthCareService(EnvGlobals.managingOrgId,EnvGlobals.LocationOrgId,EnvGlobals.healthCaeServiceId);
    }

    @Then("I receive valid Response for PUT Health Care Service service")
    public void i_receive_valid_Response_for_PUT_Health_Care_Service_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.HealthCareService.validateAvailabilityExceptions();
    }

    @Given("I Set GET HealthCare service api endpoint with invalid id")
    public void i_Set_GET_HealthCare_service_api_endpoint_with_invalid_id() {
        endPoint = EndpointURLs.GET_HEALTHCARESERVICES_URL;
        endPoint= String.format(endPoint, "01");
    }

    @Then("I receive Invalid Response for GET HealthCare service")
    public void i_receive_HealthCare_Response_for_GET_Organization_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_NOT_FOUND);
    }



}
