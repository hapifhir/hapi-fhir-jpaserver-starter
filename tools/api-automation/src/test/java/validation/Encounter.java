package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class Encounter {

    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_PATIENT = "subject.reference";
    public static final String RESPONSE_ENTRY = "entry[%s].resource.";
    public static final String RESPONSE_STATUS = "status";


    public static void validatePostResponse(String patientID) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "Encounter");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PATIENT), "Patient/"+patientID);

    }
    public static void validateEncounterId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }
    public static void validateStatus()
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_STATUS), "finished");

    }

    public static void validatePatient(String patient)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_PATIENT,0)), "Patient/"+patient);

    }
}
