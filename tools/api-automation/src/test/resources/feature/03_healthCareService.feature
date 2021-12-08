@HealthCareService
Feature: HealthCareService

  @POST
  Scenario: Create Health Care Service
    Given I am Testing Case : "437"
    And I Set POST Health Care Service service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Health Care Service service

  @GET
  Scenario: Read Health Care Service
    Given I am Testing Case : "438"
    And I Set GET Health Care Service api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Health Care Service service

  @PUT
  Scenario: Update Practitioner
    Given I am Testing Case : "439"
    And I Set PUT Facility Health Care Service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Health Care Service service


  @GET
  Scenario: Read Health Care Service for specific location
    Given I am Testing Case : "441"
    And I Set GET Health Care Service api endpoint for specific location
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Health Care Service service for specific location

  @GET
  Scenario: Read Health Care Service for specific Organization
    Given I am Testing Case : "440"
    And I Set GET Health Care Service api endpoint for specific Organization
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Health Care Service service for specific Organization

  @GET @NegTest
  Scenario: Read Health Care Service for invalid Id
    Given I am Testing Case : "733"
    And I Set GET HealthCare service api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET HealthCare service
