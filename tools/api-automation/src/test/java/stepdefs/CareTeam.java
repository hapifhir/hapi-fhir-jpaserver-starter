package stepdefs;

import config.EndpointURLs;
import config.EnvGlobals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import general.ReusableFunctions;
import payloads.CareTeamPayload;

import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class CareTeam {
    @Given("I Set POST Care Team service api endpoint")
    public void i_Set_POST_Care_Team_service_api_endpoint() {
        endPoint = EndpointURLs.CARE_TEAM_URL;
        RequestPayLoad = CareTeamPayload.createCareTeam(EnvGlobals.PractitionerId,EnvGlobals.patientId,EnvGlobals.encounterId,EnvGlobals.managingOrgId);
    }

    @Then("I receive valid Response for POST Care Team service")
    public void i_receive_valid_Response_for_POST_Care_Team_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.careTeamId = ReusableFunctions.getResponsePath("id");
        validation.CareTeam.validatePostResponse(EnvGlobals.patientId,EnvGlobals.managingOrgId,EnvGlobals.encounterId,EnvGlobals.PractitionerId);
    }

    @Given("I Set GET Care Team api endpoint")
    public void i_Set_GET_Care_Team_api_endpoint() {
        endPoint = EndpointURLs.GET_CARE_TEAM_URL;
        endPoint = String.format(endPoint, EnvGlobals.careTeamId);
    }

    @Then("I receive valid Response for GET Care Team service")
    public void i_receive_valid_Response_for_GET_Care_Team_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.CareTeam.validatePostResponse(EnvGlobals.patientId,EnvGlobals.managingOrgId,EnvGlobals.encounterId,EnvGlobals.PractitionerId);
        validation.CareTeam.validateCareTeamId(EnvGlobals.careTeamId);
    }

    @Then("I receive valid Response for GET Care Team service for specific Patient")
    public void i_receive_valid_Response_for_GET_Care_Team_service_patient() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.CareTeam.validatePatient(EnvGlobals.patientId);

    }

    @Given("I Set GET Care Team api endpoint for specific Patient")
    public void i_Set_GET_Care_Team_api_endpoint_for_specific_Patient() {
        endPoint = EndpointURLs.GET_CARE_TEAM_BY_PATIENT_URL;
        endPoint= String.format(endPoint, EnvGlobals.patientId);
    }

    @Given("I Set PUT Care Team api endpoint")
    public void i_Set_PUT_Care_Team_api_endpoint() {
        endPoint = EndpointURLs.GET_CARE_TEAM_URL;
        endPoint= String.format(endPoint, EnvGlobals.careTeamId);
        RequestPayLoad = CareTeamPayload.updateCareTeam(EnvGlobals.PractitionerId,EnvGlobals.patientId,EnvGlobals.encounterId,EnvGlobals.managingOrgId,EnvGlobals.careTeamId);
    }

    @Then("I receive valid Response for PUT Care Team service")
    public void i_receive_valid_Response_for_PUT_Care_Team_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.CareTeam.validateName();
    }

    @Given("I Set GET Care Team api endpoint with invalid id")
    public void i_Set_GET_Care_Team_api_endpoint_with_invalid_id() {
        endPoint = EndpointURLs.GET_CARE_TEAM_URL;
        endPoint = String.format(endPoint, "000");
    }

    @Then("I receive Invalid Response for GET Care Team service")
    public void i_receive_Invalid_Response_for_GET_Care_Team_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_NOT_FOUND);
    }


}
