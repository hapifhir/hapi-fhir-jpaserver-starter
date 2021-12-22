
@ManagingOrganization
Feature: Facility Organization


  @POST @test
  Scenario: Create Managing Organization
    Given I am Testing Case : "115"
    And I Set POST Organization service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Organization service

  @GET
  Scenario: Read Managing Organization
    Given I am Testing Case : "116"
    And I Set GET Organization service api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Organization service


  @PUT
  Scenario: Update Managing Organization
    Given I am Testing Case : "117"
    And I Set PUT Organization service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Organization service

  @GET @test
  Scenario: Read Managing Organization with invalid id
    Given I am Testing Case : "118"
    And I Set GET Organization service api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Organization service

  @GET  @test
  Scenario: Read Managing Organization with specific Location
    Given I am Testing Case : "213"
    And I Set GET Organization service api endpoint for specific state
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Organization service with specific Location
