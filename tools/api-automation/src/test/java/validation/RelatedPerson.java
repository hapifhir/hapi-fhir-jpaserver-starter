package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class RelatedPerson {
    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_PATIENT = "patient.reference";
    public static final String RESPONSE_ENTRY = "entry[%s].resource.";
    public static final String RESPONSE_GENDER = "gender";


    public static void validatePostResponse(String patientID) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "RelatedPerson");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PATIENT), "Patient/"+patientID);

    }

    public static void validateRelatedPersonId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }

    public static void validateGender()
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_GENDER), "male");

    }

    public static void validatePatient(String patient)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_PATIENT,0)), "Patient/"+patient);

    }



}
