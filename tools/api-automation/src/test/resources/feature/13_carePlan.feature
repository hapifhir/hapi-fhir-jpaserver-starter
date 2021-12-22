@CarePlan
Feature: Care Plan

  @POST
  Scenario: Create Care Plan
    Given I am Testing Case : "508"
    And I Set POST Care Plan service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Care Plan service

  @GET
  Scenario: Read Care Plan
    Given I am Testing Case : "509"
    And I Set GET Care Plan api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Care Plan service

  @GET
  Scenario: Read Care Plan for specific Patient
    Given I am Testing Case : "511"
    And I Set GET Care Plan api endpoint for specific Patient
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Care Plan service for specific Patient

  @GET
  Scenario: Read Care Plan for specific Care Team
    Given I am Testing Case : "512"
    And I Set GET Care Plan api endpoint for specific care Team
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Care Plan service for specific Care Team
  @GET
  Scenario: Read Care Plan for specific Condition
    Given I am Testing Case : "513"
    And I Set GET Care Plan api endpoint for specific condition
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Care Plan service for specific Condition
#  @GET
#  Scenario: Read Care Plan for specific Encounter
#    Given I am Testing Case : "514"
#    And I Set GET Care Plan api endpoint for specific encounter
#    When I Set request HEADER
#    And Send a GET HTTP request
#    Then I receive valid Response for GET Care Plan service for specific Encounter
  @PUT
  Scenario: Update Care Plan
    Given I am Testing Case : "510"
    And I Set PUT Care Plan api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Care Plan service
  @GET @NegTest
  Scenario: Read Care Plan for Invalid data
    Given I am Testing Case : "743"
    And I Set GET Care Plan api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Care Plan service
