package stepdefs;

import config.ConfigProperties;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import general.ReusableFunctions;

import static stepdefs.Hooks.RequestPayLoad;
import static stepdefs.Hooks.endPoint;

public class genericSteps {

    @Given("I am Testing Case : {string}")
    public void i_am_Testing_Case(String caseId) {
        Hooks.caseID = caseId;
    }

    @When("I Set request HEADER and PAYLOAD")
    public void i_Set_request_HEADER_and_PAYLOAD() {
        ReusableFunctions.givenHeaderPayload(ReusableFunctions.headers(), RequestPayLoad);
    }

    @When("Send a POST HTTP request")
    public void send_a_POST_HTTP_request() {
        ReusableFunctions.whenFunction(Hooks.HTTP_METHOD_POST, ConfigProperties.baseUrl + endPoint);
    }
    @When("I Set request HEADER")
    public void i_Set_request_HEADER() {
        //ReusableFunctions.givenHeaders();
        ReusableFunctions.givenHeaders();
    }
    @When("Send a GET HTTP request")
    public void send_a_GET_HTTP_request() {
        ReusableFunctions.whenFunction(Hooks.HTTP_METHOD_GET, ConfigProperties.baseUrl + endPoint);
    }

    @When("Send a PUT HTTP request")
    public void send_a_PUT_HTTP_request() {
        ReusableFunctions.whenFunction(Hooks.HTTP_METHOD_PUT, ConfigProperties.baseUrl + endPoint);
    }


}
