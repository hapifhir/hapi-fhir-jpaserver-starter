package validation;

import com.jayway.jsonpath.JsonPath;
import config.EnvGlobals;
import general.ReusableFunctions;
import org.junit.Assert;

public class ManagingOrg {

    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_NAME = "name";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_LINE = "address.line";
    public static final String RESPONSE_CITY = "address.city";
    public static final String RESPONSE_STATE = "address.state";
    public static final String RESPONSE_POSTAL= "address.postalCode";
    public static final String RESPONSE_COUNTRY = "address.country";
    public static final String RESPONSE_ENTRY = "entry[%s].resource.";



    public static void validatePostResponse(String name) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_NAME), name);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "Organization");
    }

    public static void validateOrganizationId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }
    public static void validateOrganizationAddress() {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_LINE), "[[3300 Washtenaw Avenue, Suite 227]]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_CITY), "[Ann Arbor]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_STATE), "[MI]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_POSTAL), "[48104]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_COUNTRY), "[USA]");
    }

    public static void validateLocation()
    {

        for(int i = 0 ; i< 10;i++) {

            Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_STATE,i)), "[MI]");

        }
    }



}
