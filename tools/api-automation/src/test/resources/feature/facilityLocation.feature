@FacilityLocation
Feature: Managing Organization

  @POST
Scenario: Create Facility Location
Given I am Testing Case : "215"
And I Set POST Facility Location service api endpoint
When I Set request HEADER and PAYLOAD
And Send a POST HTTP request
Then I receive valid Response for POST facility Location service

@GET
Scenario: Read Facility Organization
Given I am Testing Case : "216"
And I Set GET Location service api endpoint
When I Set request HEADER
And Send a GET HTTP request
Then I receive valid Response for GET facility Location service


@PUT
Scenario: Update Facility Location
Given I am Testing Case : "217"
And I Set PUT Facility Location service api endpoint
When I Set request HEADER and PAYLOAD
And Send a PUT HTTP request
Then I receive valid Response for PUT facility Location service

