package stepdefs;

import config.EndpointURLs;
import config.EnvGlobals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import general.ReusableFunctions;
import payloads.RelatedPersonPayload;

import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class RelatedPerson {


    @Given("I Set POST Related Person service api endpoint")
    public void i_Set_POST_Related_Person_service_api_endpoint() {
        endPoint = EndpointURLs.RELATED_PERSON_URL;
        RequestPayLoad = RelatedPersonPayload.createRelatedPerson(EnvGlobals.patientId);
    }

    @Then("I receive valid Response for POST Related Person service")
    public void i_receive_valid_Response_for_POST_Related_Person_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_CREATED);
        EnvGlobals.relatedPersonId = ReusableFunctions.getResponsePath("id");
        validation.RelatedPerson.validatePostResponse(EnvGlobals.patientId);
    }

    @Given("I Set GET Related Person api endpoint")
    public void i_Set_GET_Related_Person_api_endpoint() {
        endPoint = EndpointURLs.GET_RELATED_PERSON_URL;
        endPoint= String.format(endPoint, EnvGlobals.relatedPersonId);
    }

    @Then("I receive valid Response for GET Related Person service")
    public void i_receive_valid_Response_for_GET_Related_Person_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.RelatedPerson.validatePostResponse(EnvGlobals.patientId);
        validation.RelatedPerson.validateRelatedPersonId(EnvGlobals.relatedPersonId);
    }

    @Then("I receive valid Response for GET Related Person service for specific Patient")
    public void i_receive_valid_Response_for_GET_Related_Person_service_patient() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.RelatedPerson.validatePatient(EnvGlobals.patientId);
    }

    @Given("I Set GET Related Person api endpoint for specific Patient")
    public void i_Set_GET_Related_Person_api_endpoint_for_specific_Patient() {
        endPoint = EndpointURLs.GET_RELATED_PERSON_BY_PATIENT_URL;
        endPoint= String.format(endPoint, EnvGlobals.patientId);
    }

    @Given("I Set PUT Related Person api endpoint")
    public void i_Set_PUT_Facility_Related_Person_api_endpoint() {
        endPoint = EndpointURLs.GET_RELATED_PERSON_URL;
        endPoint= String.format(endPoint, EnvGlobals.relatedPersonId);
        RequestPayLoad = RelatedPersonPayload.updateRelatedPerson(EnvGlobals.patientId,EnvGlobals.relatedPersonId);

    }
    @Then("I receive valid Response for PUT Related Person service")
    public void i_receive_valid_Response_for_PUT_Related_Person_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_SUCCESS);
        validation.RelatedPerson.validateRelatedPersonId(EnvGlobals.relatedPersonId);
        validation.RelatedPerson.validateGender();
    }
    @Given("I Set GET Related Person api endpoint with invalid id")
    public void i_Set_GET_Related_Person_api_endpoint_with_invalid_id() {
        endPoint = EndpointURLs.GET_RELATED_PERSON_URL;
        endPoint= String.format(endPoint, "000");
    }

    @Then("I receive Invalid Response for GET Related Person service")
    public void i_receive_Invalid_Response_for_GET_Related_Person_service() {
        ReusableFunctions.thenFunction(Hooks.HTTP_RESPONSE_NOT_FOUND);
    }



}
