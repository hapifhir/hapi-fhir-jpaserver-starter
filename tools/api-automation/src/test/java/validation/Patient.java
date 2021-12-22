package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class Patient {

    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_ORGANIZATION = "managingOrganization.reference";
    public static final String RESPONSE_ENTRY = "entry[%s].resource.";
    public static final String RESPONSE_GENDER = "gender";

    public static void validatePostResponse(String organization) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "Patient");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ORGANIZATION), "Organization/"+organization);
    }

    public static void validatePatientId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }

    public static void validateOrganization(String organization)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_ORGANIZATION,0)), "Organization/"+organization);

    }

    public static void validateGender()
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_GENDER), "female");

    }

}
