 @FacilityOrganization
  Feature: Managing Organization

  @POST
  Scenario: Create Facility Organization
    Given I am Testing Case : "208"
    And I Set POST Facility Organization service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST facility Organization service

  @GET
  Scenario: Read Facility Organization
    Given I am Testing Case : "209"
    And I Set GET Organization service api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET facility Organization service


  @PUT
  Scenario: Update Facility Organization
    Given I am Testing Case : "210"
    And I Set PUT Facility Organization service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT facility Organization service

  @GET @test
  Scenario: Read Facility Organization with invalid id
    Given I am Testing Case : "211"
    And I Set GET Organization service api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Organization service

