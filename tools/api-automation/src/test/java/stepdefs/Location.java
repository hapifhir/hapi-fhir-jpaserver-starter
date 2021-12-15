package stepdefs;

import config.EndpointURLs;
import config.EnvGlobals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import general.ReusableFunctions;
import payloads.LocationPayload;
import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class Location {

    public static final String ORGANIZATION_LOCATION_NAME= "MOH HQ";
    public static final String FACILITY_LOCATION_NAME= "Good Health Clinic";

    @Given("I Set POST Location service api endpoint")
    public void i_Set_POST_Location_service_api_endpoint() {
        endPoint = EndpointURLs.LOCATION_ORGANIZATION_URL;
        RequestPayLoad = LocationPayload.createLocation(ORGANIZATION_LOCATION_NAME);
    }

    @Then("I receive valid Response for POST Location service")
    public void i_receive_valid_Response_for_POST_Location_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.LocationOrgId = ReusableFunctions.getResponsePath("id");
        validation.Location.validatePostResponse(ORGANIZATION_LOCATION_NAME);
    }

    @Given("I Set GET Location service api endpoint")
    public void i_Set_GET_Location_service_api_endpoint() {
        endPoint = EndpointURLs.GET_LOCATION_ORGANIZATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.LocationOrgId);
    }


    @Then("I receive valid Response for GET facility Location service")
    public void i_receive_valid_Response_for_GET_facility_Location_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Location.validatePostResponse(FACILITY_LOCATION_NAME);
        validation.Location.validateLocationId(EnvGlobals.LocationOrgId);

    }

    @Then("I receive valid Response for GET Location service")
    public void i_receive_valid_Response_for_GET_Location_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Location.validatePostResponse(ORGANIZATION_LOCATION_NAME);
        validation.Location.validateLocationId(EnvGlobals.LocationOrgId);

    }

    @Then("I receive valid Response for GET Location service with specific state")
    public void i_receive_valid_Response_for_GET_Location_service_with_specific_state() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Location.validateLocation();

    }

    @Given("I Set PUT Location service api endpoint")
    public void i_Set_PUT_Location_service_api_endpoint() {
        endPoint = EndpointURLs.GET_LOCATION_ORGANIZATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.LocationOrgId);
        RequestPayLoad = LocationPayload.updateLocation(EnvGlobals.LocationOrgId,ORGANIZATION_LOCATION_NAME);
    }

    @Then("I receive valid Response for PUT Location service")
    public void i_receive_valid_Response_for_PUT_Location_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Location.validateLocationTelecom();
    }

    @Given("I Set GET Location service api endpoint with invalid id")
    public void i_Set_GET_Location_service_api_endpoint_with_invalid_id() {
        endPoint = EndpointURLs.GET_LOCATION_ORGANIZATION_URL;
        endPoint= String.format(endPoint, "01");
    }

    @Then("I receive Invalid Response for GET Location service")
    public void i_receive_Invalid_Response_for_GET_Location_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_NOT_FOUND);
    }

    @Given("I Set Location service api endpoint for specific state")
    public void i_Set_Location_service_api_endpoint_for_specific_state() {
        endPoint = EndpointURLs.GET_LOCATION_BY_STATE;
    }


    @Given("I Set POST Facility Location service api endpoint")
    public void i_Set_POST_Facility_Location_service_api_endpoint() {
        endPoint = EndpointURLs.LOCATION_ORGANIZATION_URL;
        RequestPayLoad = LocationPayload.createLocation(FACILITY_LOCATION_NAME);
    }

    @Then("I receive valid Response for POST facility Location service")
    public void i_receive_valid_Response_for_POST_facility_Location_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.LocationOrgId = ReusableFunctions.getResponsePath("id");
    }

    @Given("I Set PUT Facility Location service api endpoint")
    public void i_Set_PUT_Facility_Location_service_api_endpoint() {
        endPoint = EndpointURLs.GET_LOCATION_ORGANIZATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.LocationOrgId);
        RequestPayLoad = LocationPayload.updateLocation(EnvGlobals.LocationOrgId,FACILITY_LOCATION_NAME);
    }

    @Then("I receive valid Response for PUT facility Location service")
    public void i_receive_valid_Response_for_PUT_facility_Location_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
    }


}
