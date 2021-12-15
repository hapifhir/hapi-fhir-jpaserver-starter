@Practitioner
Feature: Practitioner

  @POST
  Scenario: Create Practitioner
    Given I am Testing Case : "434"
    And I Set POST Practitioner service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Practitioner service

  @GET
  Scenario: Read Practitioner
    Given I am Testing Case : "435"
    And I Set GET Practitioner api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Practitioner service


  @PUT
  Scenario: Update Practitioner
    Given I am Testing Case : "436"
    And I Set PUT Facility Practitioner api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Practitioner service


  @GET @NegTest
  Scenario: Read Practitioner for Invalid data
    Given I am Testing Case : "734"
    And I Set GET Practitioner api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Practitioner service
