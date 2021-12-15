@RelatedPerson
Feature: RelatedPerson

  @POST
  Scenario: Create Related Person
    Given I am Testing Case : "484"
    And I Set POST Related Person service api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a POST HTTP request
    Then I receive valid Response for POST Related Person service

  @GET
  Scenario: Read Related Person
    Given I am Testing Case : "486"
    And I Set GET Related Person api endpoint
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Related Person service


  @GET
  Scenario: Read Related Person for specific Patient
    Given I am Testing Case : "487"
    And I Set GET Related Person api endpoint for specific Patient
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive valid Response for GET Related Person service for specific Patient


  @PUT
  Scenario: Update Related Person
    Given I am Testing Case : "485"
    And I Set PUT Related Person api endpoint
    When I Set request HEADER and PAYLOAD
    And Send a PUT HTTP request
    Then I receive valid Response for PUT Related Person service

  @GET @NegTest
  Scenario: Read Related Person for Invalid data
    Given I am Testing Case : "737"
    And I Set GET Related Person api endpoint with invalid id
    When I Set request HEADER
    And Send a GET HTTP request
    Then I receive Invalid Response for GET Related Person service
