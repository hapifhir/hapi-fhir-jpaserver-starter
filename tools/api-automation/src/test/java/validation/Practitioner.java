package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class Practitioner {

    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_RESOURCE_TEXT = "qualification.code.text";


    public static void validatePostResponse() {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "Practitioner");
    }


    public static void validatePractitionerId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }

    public static void validateTextField() {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TEXT), "[Bachelor of Computer Science]");
    }
}
