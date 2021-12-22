@Observation
Feature: Observation

  @POST
  Scenario: Create Observation
    Given I am Testing Case : "496"
    And I Set POST Observation service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Observation service

  @GET
  Scenario: Read Observation
    Given I am Testing Case : "497"
    And I Set GET Observation api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Observation service


  @GET
  Scenario: Read Observation for specific Patient
    Given I am Testing Case : "499"
    And I Set GET Observation api endpoint for specific Patient
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Observation service for specific Patient


  @PUT
  Scenario: Update Observation
    Given I am Testing Case : "498"
    And I Set PUT Observation api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Observation service

  @GET @NegTest
  Scenario: Read Observation for Invalid data
    Given I am Testing Case : "740"
    And I Set GET Observation api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Observation service
