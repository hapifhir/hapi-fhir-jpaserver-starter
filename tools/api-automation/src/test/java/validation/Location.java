package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class Location {
    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_NAME = "name";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_TELECOM = "telecom.value[0]";
    public static final String RESPONSE_STATE = "address.state";
    public static final String RESPONSE_ENTRY = "entry[%s].resource.";

    public static void validatePostResponse(String name) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_NAME), name);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "Location");
    }

    public static void validateLocationId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }
    public static void validateLocationTelecom() {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_TELECOM), "(+1) 734-677-0000");
    }

    public static void validateLocation()
    {
        for(int i = 0 ; i< 10;i++) {
            Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_STATE,i)), "MI");

        }
    }
}
