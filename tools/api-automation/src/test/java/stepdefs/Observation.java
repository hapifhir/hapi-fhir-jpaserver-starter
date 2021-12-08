package stepdefs;

import config.EndpointURLs;
import config.EnvGlobals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import general.ReusableFunctions;
import payloads.ObservationPayload;

import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class Observation {


    @Given("I Set POST Observation service api endpoint")
    public void i_Set_POST_Observation_service_api_endpoint() {
        endPoint = EndpointURLs.OBSERVATION_URL;
        RequestPayLoad = ObservationPayload.createObservation(EnvGlobals.patientId);
    }

    @Then("I receive valid Response for POST Observation service")
    public void i_receive_valid_Response_for_POST_Observation_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.observationId = ReusableFunctions.getResponsePath("id");
        validation.Observation.validatePostResponse(EnvGlobals.patientId);
    }

    @Given("I Set GET Observation api endpoint")
    public void i_Set_GET_Observation_api_endpoint() {
        endPoint = EndpointURLs.GET_OBSERVATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.observationId);
    }

    @Then("I receive valid Response for GET Observation service")
    public void i_receive_valid_Response_for_GET_Observation_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Observation.validatePostResponse(EnvGlobals.patientId);
        validation.Observation.validateObservationId(EnvGlobals.observationId);
    }
    @Then("I receive valid Response for GET Observation service for specific Patient")
    public void i_receive_valid_Response_for_GET_Observation_service_patient() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Observation.validatePatient(EnvGlobals.patientId);
    }

    @Given("I Set GET Observation api endpoint for specific Patient")
    public void i_Set_GET_Observation_api_endpoint_for_specific_Patient() {
        endPoint = EndpointURLs.GET_OBSERVATION_BY_PATIENT_URL;
        endPoint= String.format(endPoint, EnvGlobals.patientId);
    }

    @Given("I Set PUT Observation api endpoint")
    public void i_Set_PUT_Observation_api_endpoint() {
        endPoint = EndpointURLs.GET_OBSERVATION_URL;
        endPoint= String.format(endPoint, EnvGlobals.observationId);
        RequestPayLoad = ObservationPayload.updateObservation(EnvGlobals.patientId,EnvGlobals.observationId);

    }

    @Then("I receive valid Response for PUT Observation service")
    public void i_receive_valid_Response_for_PUT_Observation_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Observation.validateCode();
    }
    @Given("I Set GET Observation api endpoint with invalid id")
    public void i_Set_GET_Observation_api_endpoint_with_invalid_id() {
        endPoint = EndpointURLs.GET_OBSERVATION_URL;
        endPoint= String.format(endPoint, "000");
    }

    @Then("I receive Invalid Response for GET Observation service")
    public void i_receive_Invalid_Response_for_GET_Observation_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_NOT_FOUND);
    }





}
