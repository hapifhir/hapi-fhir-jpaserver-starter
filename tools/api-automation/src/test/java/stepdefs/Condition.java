package stepdefs;

import config.EndpointURLs;
import config.EnvGlobals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import general.ReusableFunctions;
import payloads.ConditionPayload;
import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class Condition {

    @Given("I Set POST Condition service api endpoint")
    public void i_Set_POST_Condition_service_api_endpoint() {
        endPoint = EndpointURLs.CONDITION_URL;
        RequestPayLoad = ConditionPayload.createCondition(EnvGlobals.patientId);
    }

    @Then("I receive valid Response for POST Condition service")
    public void i_receive_valid_Response_for_POST_Condition_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.conditionId = ReusableFunctions.getResponsePath("id");
        validation.Condition.validatePostResponse(EnvGlobals.patientId);
    }

    @Given("I Set GET Condition api endpoint")
    public void i_Set_GET_Condition_api_endpoint() {
        endPoint = EndpointURLs.GET_CONDITION_URL;
        endPoint = String.format(endPoint, EnvGlobals.conditionId);
    }

    @Then("I receive valid Response for GET Condition service")
    public void i_receive_valid_Response_for_GET_Condition_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Condition.validatePostResponse(EnvGlobals.patientId);
        validation.Condition.validateConditionId(EnvGlobals.conditionId);
    }

    @Then("I receive valid Response for GET Condition service for specific Patient")
    public void i_receive_valid_Response_for_GET_Condition_service_patient() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Condition.validatePatient(EnvGlobals.patientId);
    }

    @Given("I Set GET Condition api endpoint for specific Patient")
    public void i_Set_GET_Condition_api_endpoint_for_specific_Patient() {
        endPoint = EndpointURLs.GET_CONDITION_BY_PATIENT_URL;
        endPoint= String.format(endPoint, EnvGlobals.patientId);
    }

    @Given("I Set PUT Condition api endpoint")
    public void i_Set_PUT_Facility_Condition_api_endpoint() {
        endPoint = EndpointURLs.GET_CONDITION_URL;
        endPoint= String.format(endPoint, EnvGlobals.conditionId);
        RequestPayLoad = ConditionPayload.updateCondition(EnvGlobals.patientId,EnvGlobals.conditionId);
    }

    @Then("I receive valid Response for PUT Condition service")
    public void i_receive_valid_Response_for_PUT_Condition_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Condition.validateCode();
    }

    @Given("I Set GET Condition api endpoint with invalid id")
    public void i_Set_GET_Condition_api_endpoint_with_invalid_id() {
        endPoint = EndpointURLs.GET_CONDITION_URL;
        endPoint = String.format(endPoint, "000");
    }

    @Then("I receive Invalid Response for GET Condition service")
    public void i_receive_Invalid_Response_for_GET_Condition_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_NOT_FOUND);
    }


}

