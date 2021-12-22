package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class CarePlan {

    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_CARE_TEAM= "careTeam.reference";
    public static final String RESPONSE_PATIENT = "subject.reference";
    public static final String RESPONSE_CONDITION = "addresses.reference";
    public static final String RESPONSE_GOAL = "goal.reference";
    public static final String RESPONSE_ENCOUNTER = "activity.outcomeReference.reference";
    public static final String RESPONSE_PRACTITIONER = "activity.detail[0].performer.reference";


    public static final String RESPONSE_ENTRY = "entry[%s].resource.";
    public static final String RESPONSE_DESCRIPTION = "activity.detail[1].description";

    public static void validatePostResponse(String patientID,String careTeam,String encounter,String practitioner,String condition,String goal) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "CarePlan");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PATIENT), "Patient/"+patientID);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_CARE_TEAM), "[CareTeam/"+careTeam+"]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_CONDITION), "[Condition/"+condition+"]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ENCOUNTER), "[[Encounter/"+encounter+"]]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_GOAL), "[Goal/"+goal+"]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PRACTITIONER), "[Practitioner/"+practitioner+"]");
    }

    public static void validateCarePlanId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }
    public static void validatePatient(String patient)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_PATIENT,0)), "Patient/"+patient);
    }

    public static void validateCareTeam(String careTeam)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_CARE_TEAM,0)), "[CareTeam/"+careTeam+"]");
    }

    public static void validateCondition(String condition)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_CONDITION,0)), "[Condition/"+condition+"]");
    }

    public static void validateEncounetr(String encounter)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_ENCOUNTER,0)), "[Encounter/"+encounter+"]");
    }
    public static void validateDescription() {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_DESCRIPTION), "Second contact to occurs in 5 months time");
    }
}
