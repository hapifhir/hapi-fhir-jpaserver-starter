@Goal
Feature: Goal

  @POST
  Scenario: Create Goal
    Given I am Testing Case : "500"
    And I Set POST Goal service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Goal service

  @GET
  Scenario: Read Goal
    Given I am Testing Case : "501"
    And I Set GET Goal api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Goal service


  @GET
  Scenario: Read Goal for specific Patient
    Given I am Testing Case : "503"
    And I Set GET Goal api endpoint for specific Patient
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Goal service for specific Patient


  @PUT
  Scenario: Update Goal
    Given I am Testing Case : "502"
    And I Set PUT Goal api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Goal service

  @GET @NegTest
  Scenario: Read Goal for Invalid data
    Given I am Testing Case : "741"
    And I Set GET Goal api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Goal service

