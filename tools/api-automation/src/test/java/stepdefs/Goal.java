package stepdefs;

import config.EndpointURLs;
import config.EnvGlobals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import general.ReusableFunctions;
import payloads.GoalPayload;

import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class Goal {

    @Given("I Set POST Goal service api endpoint")
    public void i_Set_POST_Goal_service_api_endpoint() {
        endPoint = EndpointURLs.GOAL_URL;
        RequestPayLoad = GoalPayload.createGoal(EnvGlobals.patientId, EnvGlobals.observationId);
    }

    @Then("I receive valid Response for POST Goal service")
    public void i_receive_valid_Response_for_POST_Goal_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.goalId= ReusableFunctions.getResponsePath("id");
        validation.Goal.validatePostResponse(EnvGlobals.patientId,EnvGlobals.observationId);

    }
    @Given("I Set GET Goal api endpoint")
    public void i_Set_GET_Goal_api_endpoint() {
        endPoint = EndpointURLs.GET_GOAL_URL;
        endPoint = String.format(endPoint, EnvGlobals.goalId);
    }

    @Then("I receive valid Response for GET Goal service")
    public void i_receive_valid_Response_for_GET_Goal_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Goal.validatePostResponse(EnvGlobals.patientId,EnvGlobals.observationId);
        validation.Goal.validateGoalId(EnvGlobals.goalId);
    }

    @Then("I receive valid Response for GET Goal service for specific Patient")
    public void i_receive_valid_Response_for_GET_Goal_service_patient() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Goal.validatePatient(EnvGlobals.patientId);
    }

    @Given("I Set GET Goal api endpoint for specific Patient")
    public void i_Set_GET_Goal_api_endpoint_for_specific_Patient() {
        endPoint = EndpointURLs.GET_GOAL_BY_PATIENT_URL;
        endPoint= String.format(endPoint, EnvGlobals.patientId);
    }

    @Given("I Set PUT Goal api endpoint")
    public void i_Set_PUT_Goal_api_endpoint() {
        endPoint = EndpointURLs.GET_GOAL_URL;
        endPoint= String.format(endPoint, EnvGlobals.goalId);
        RequestPayLoad = GoalPayload.updateGoal(EnvGlobals.patientId,EnvGlobals.observationId,EnvGlobals.goalId);
    }
    @Then("I receive valid Response for PUT Goal service")
    public void i_receive_valid_Response_for_PUT_Goal_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.Goal.validateLifeCycle();
    }

    @Given("I Set GET Goal api endpoint with invalid id")
    public void i_Set_GET_Goal_api_endpoint_with_invalid_id() {
        endPoint = EndpointURLs.GET_GOAL_URL;
        endPoint = String.format(endPoint, "000");
    }

    @Then("I receive Invalid Response for GET Goal service")
    public void i_receive_Invalid_Response_for_GET_Goal_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_NOT_FOUND);
    }


}
