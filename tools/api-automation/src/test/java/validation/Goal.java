package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class Goal {
    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_PATIENT = "subject.reference";
    public static final String RESPONSE_EXPRESSED_BY = "expressedBy.reference";
    public static final String RESPONSE_OBSERVATION = "outcomeReference.reference";
    public static final String RESPONSE_LIFECYCLE = "lifecycleStatus";
    public static final String RESPONSE_ENTRY = "entry[%s].resource.";
    public static final String RESPONSE_Code = "code.coding.display";


    public static void validatePostResponse(String patientID,String observationId) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "Goal");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PATIENT), "Patient/"+patientID);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_EXPRESSED_BY), "Patient/"+patientID);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_OBSERVATION), "[Observation/"+observationId+"]");
    }
    public static void validateGoalId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }
    public static void validatePatient(String patient)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_PATIENT,0)), "Patient/"+patient);
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_EXPRESSED_BY,0)), "Patient/"+patient);

    }
    public static void validateLifeCycle() {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_LIFECYCLE), "active");
    }
}
