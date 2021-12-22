@Encounter
Feature: Encounter

  @POST
  Scenario: Create Encounter
    Given I am Testing Case : "488"
    And I Set POST Encounter service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Encounter service

  @GET
  Scenario: Read Encounter
    Given I am Testing Case : "489"
    And I Set GET Encounter api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Encounter service


  @GET
  Scenario: Read Encounter for specific Patient
    Given I am Testing Case : "491"
    And I Set GET Encounter api endpoint for specific Patient
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Encounter service for specific Patient


  @PUT
  Scenario: Update Encounter
    Given I am Testing Case : "490"
    And I Set PUT Encounter api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Encounter service

  @GET @NegTest
  Scenario: Read Encounter for Invalid data
    Given I am Testing Case : "738"
    And I Set GET Encounter api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Encounter service
