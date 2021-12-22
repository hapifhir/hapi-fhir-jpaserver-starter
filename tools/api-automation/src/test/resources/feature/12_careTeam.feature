@CareTeam
Feature: Care Team

  @POST
  Scenario: Create Care Team
    Given I am Testing Case : "504"
    And I Set POST Care Team service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Care Team service

  @GET
  Scenario: Read Care Team
    Given I am Testing Case : "505"
    And I Set GET Care Team api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Care Team service


  @GET
  Scenario: Read Care Team for specific Patient
    Given I am Testing Case : "507"
    And I Set GET Care Team api endpoint for specific Patient
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Care Team service for specific Patient


  @PUT
  Scenario: Update Care Team
    Given I am Testing Case : "506"
    And I Set PUT Care Team api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Care Team service


  @GET @NegTest
  Scenario: Read Care Team for Invalid data
    Given I am Testing Case : "742"
    And I Set GET Care Team api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Care Team service
