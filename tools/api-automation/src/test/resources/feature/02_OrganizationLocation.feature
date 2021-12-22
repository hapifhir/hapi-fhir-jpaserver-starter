@LocationOrganization
Feature: Location Organization

  @POST
  Scenario: Create Location
    Given I am Testing Case : "112"
    And I Set POST Location service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Location service

  @GET
  Scenario: Read Location
    Given I am Testing Case : "111"
    And I Set GET Location service api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Location service

  @PUT
  Scenario: Update Location
    Given I am Testing Case : "114"
    And I Set PUT Location service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Location service

  @GET
  Scenario: Read Location with invalid id
    Given I am Testing Case : "218"
    And I Set GET Location service api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Location service

  @GET
  Scenario: Read Location with specific state
    Given I am Testing Case : "214"
    And I Set Location service api endpoint for specific state
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Location service with specific state
