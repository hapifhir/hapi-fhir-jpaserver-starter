package validation;

import general.ReusableFunctions;
import org.junit.Assert;

public class CareTeam {


    public static final String RESPONSE_ID = "id";
    public static final String RESPONSE_RESOURCE_TYPE = "resourceType";
    public static final String RESPONSE_ORGANIZATION= "managingOrganization.reference";
    public static final String RESPONSE_PATIENT = "subject.reference";
    public static final String RESPONSE_PARTICIPANT = "participant.member[0].reference";
    public static final String RESPONSE_ENCOUNTER = "encounter.reference";
    public static final String RESPONSE_PRACTITIONER = "contained.id";

    public static final String RESPONSE_ENTRY = "entry[%s].resource.";
    public static final String RESPONSE_NAME = "name";

    public static void validatePostResponse(String patientID,String organization,String encounter,String practitioner) {
        Assert.assertNotNull(ReusableFunctions.getResponsePath(RESPONSE_ID));
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_RESOURCE_TYPE), "CareTeam");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PATIENT), "Patient/"+patientID);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PARTICIPANT), "Patient/"+patientID);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ORGANIZATION), "[Organization/"+organization+"]");
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ENCOUNTER), "Encounter/"+encounter);
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_PRACTITIONER), "["+practitioner+"]");
    }
    public static void validateCareTeamId(String id) {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_ID), id);
    }

    public static void validatePatient(String patient)
    {
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_PATIENT,0)), "Patient/"+patient);
        Assert.assertEquals(ReusableFunctions.getResponsePath(String.format(RESPONSE_ENTRY+RESPONSE_PARTICIPANT,0)), "Patient/"+patient);

    }
    public static void validateName() {
        Assert.assertEquals(ReusableFunctions.getResponsePath(RESPONSE_NAME), "Peter Charlmers Care team");
    }
}


