package stepdefs;

import config.EndpointURLs;
import config.EnvGlobals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.runtime.Env;
import general.ReusableFunctions;
import payloads.OrganizationPayload;
import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class Organization {
    public static final String MANAGING_ORGANIZATION_NAME= "Ministry of Health";
    public static final String FACILITY_ORGANIZATION_NAME= "Good Health Clinic";

    @Given("I Set POST Organization service api endpoint")
    public void i_Set_POST_Organization_service_api_endpoint() {
        endPoint = EndpointURLs.MANAGING_ORGANIZATION_URL;
        RequestPayLoad = OrganizationPayload.createOrganization(MANAGING_ORGANIZATION_NAME);
    }
    @Then("I receive valid Response for POST Organization service")
    public void i_receive_valid_Response_for_POST_Organization_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.managingOrgId = ReusableFunctions.getResponsePath("id");
        validation.ManagingOrg.validatePostResponse(MANAGING_ORGANIZATION_NAME);
    }
    @Given("I Set GET Organization service api endpoint")
    public void i_Set_GET_Organization_service_api_endpoint() {
        endPoint = EndpointURLs.GET_MANAGING_ORGANIZATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.managingOrgId);
    }
    @Given("I Set GET Organization service api endpoint for specific state")
    public void i_Set_GET_Organization_service_api_endpoint_for_specific_state() {
        endPoint = EndpointURLs.GET_ORGANIZATION_BY_LOCATION;
    }


    @Then("I receive valid Response for GET Organization service")
    public void i_receive_valid_Response_for_GET_Organization_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.ManagingOrg.validatePostResponse(MANAGING_ORGANIZATION_NAME);
        validation.ManagingOrg.validateOrganizationId(EnvGlobals.managingOrgId);
    }


    @Then("I receive valid Response for GET facility Organization service")
    public void i_receive_valid_Response_for_GET_facility_Organization_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.ManagingOrg.validatePostResponse(FACILITY_ORGANIZATION_NAME);
        validation.ManagingOrg.validateOrganizationId(EnvGlobals.managingOrgId);
    }

    @Given("I Set PUT Organization service api endpoint")
    public void i_Set_PUT_Organization_service_api_endpoint() {
        endPoint = EndpointURLs.GET_MANAGING_ORGANIZATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.managingOrgId);
        RequestPayLoad = OrganizationPayload.updateOrganization(EnvGlobals.managingOrgId,MANAGING_ORGANIZATION_NAME);
    }

    @Then("I receive valid Response for PUT Organization service")
    public void i_receive_valid_Response_for_PUT_Organization_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.ManagingOrg.validatePostResponse(MANAGING_ORGANIZATION_NAME);
        validation.ManagingOrg.validateOrganizationId(EnvGlobals.managingOrgId);
        validation.ManagingOrg.validateOrganizationAddress();
    }
    @Given("I Set GET Organization service api endpoint with invalid id")
    public void i_Set_GET_Organization_service_api_endpoint_with_invalid_id() {
        endPoint = EndpointURLs.GET_MANAGING_ORGANIZATION_URL;
        endPoint= String.format(endPoint, "01");
    }

    @Then("I receive Invalid Response for GET Organization service")
    public void i_receive_Invalid_Response_for_GET_Organization_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_NOT_FOUND);
    }

    @Given("I Set POST Facility Organization service api endpoint")
    public void i_Set_POST_Facility_Organization_service_api_endpoint() {
        endPoint = EndpointURLs.MANAGING_ORGANIZATION_URL;
        RequestPayLoad = OrganizationPayload.createOrganization(FACILITY_ORGANIZATION_NAME);
    }


    @Given("I Set PUT Facility Organization service api endpoint")
    public void i_Set_PUT_Facility_Organization_service_api_endpoint() {
        endPoint = EndpointURLs.GET_MANAGING_ORGANIZATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.managingOrgId);
        RequestPayLoad = OrganizationPayload.updateOrganization(EnvGlobals.managingOrgId,FACILITY_ORGANIZATION_NAME);
    }

    @Then("I receive valid Response for POST facility Organization service")
    public void i_receive_valid_Response_for_POST_facility_Organization_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.managingOrgId = ReusableFunctions.getResponsePath("id");
        validation.ManagingOrg.validatePostResponse(FACILITY_ORGANIZATION_NAME);
    }


    @Then("I receive valid Response for PUT facility Organization service")
    public void i_receive_valid_Response_for_PUT_facility_Organization_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.ManagingOrg.validatePostResponse(FACILITY_ORGANIZATION_NAME);
    }

    @Then("I receive valid Response for GET Organization service with specific Location")
    public void i_receive_valid_Response_for_GET_Organization_service_with_specific_Location() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.ManagingOrg.validateLocation();
    }


}
