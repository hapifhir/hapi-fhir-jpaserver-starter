package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class HealthCareService {


    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_ORGANIZATION= "providedBy.reference";
    public static final String RESPONSE_LOCATION = "location.reference";
    public static final String RESPONSE_AVAILABILITY= "availabilityExceptions";
    public static final String RESPONSE_ENTRY = "entry[%s].resource.";

    public static void validatePostResponse(String organization,String location) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "HealthcareService");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ORGANIZATION), "Organization/"+organization);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_LOCATION), "[Location/"+location+"]");
    }

    public static void validateHealthCareId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }
    public static void validateAvailabilityExceptions() {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_AVAILABILITY), "Reduced capacity is available during the Christmas period Time");
    }

    public static void validateOrganization(String organization)
    {
            Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_ORGANIZATION,0)), "Organization/"+organization);

    }

    public static void validateLocation(String location)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_LOCATION,0)), "[Location/"+location+"]");

    }



}
