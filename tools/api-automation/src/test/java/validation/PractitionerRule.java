package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class PractitionerRule {


    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_ORGANIZATION= "organization.reference";
    public static final String RESPONSE_LOCATION = "location.reference";
    public static final String RESPONSE_PRACTITIONER = "practitioner.reference";
    public static final String RESPONSE_HEALTH_CARE= "healthcareService.reference";
    public static final String RESPONSE_ENTRY = "entry[%s].resource.";
    public static final String RESPONSE_AVAILABILITY= "availabilityExceptions";

    public static void validatePostResponse(String organization,String location,String practitioner,String healthCareId) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "PractitionerRole");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PRACTITIONER), "Practitioner/"+practitioner);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ORGANIZATION), "Organization/"+organization);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_LOCATION), "[Location/"+location+"]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_HEALTH_CARE), "[HealthcareService/"+healthCareId+"]");
    }

    public static void validatePractitionerRuleId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }

    public static void validateAvailabilityExceptions() {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_AVAILABILITY), "Adam is generally unavailable on public holidays and during the Christmas/New Year break Time");
    }

    public static void validateOrganization(String organization)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_ORGANIZATION,0)), "Organization/"+organization);

    }

    public static void validateLocation(String location)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_LOCATION,0)), "[Location/"+location+"]");

    }

    public static void validatePractitioner(String practitionerId)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_PRACTITIONER,0)), "Practitioner/"+practitionerId);

    }

}
