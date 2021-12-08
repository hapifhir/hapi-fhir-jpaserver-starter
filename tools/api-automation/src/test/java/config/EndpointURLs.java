
package config;


public class EndpointURLs {


    public static  String MANAGING_ORGANIZATION_URL = "/Organization";
    public static  String GET_MANAGING_ORGANIZATION_URL = "/Organization/%s";
    public static  String GET_ORGANIZATION_BY_LOCATION = "/Organization?address-state=MI";

    public static  String LOCATION_ORGANIZATION_URL = "/Location";
    public static  String GET_LOCATION_ORGANIZATION_URL = "/Location/%s";
    public static  String GET_LOCATION_BY_STATE = "/Location?address-state=MI";

    public static  String PRACTITIONER_URL = "/Practitioner";
    public static  String GET_PRACTITIONER_URL = "/Practitioner/%s";

    public static  String HEALTHCARESERVICES_URL = "/HealthcareService";
    public static  String GET_HEALTHCARESERVICES_URL = "/HealthcareService/%s";
    public static  String GET_HEALTHCARESERVICES_BY_ORGANIZATION_URL = "/HealthcareService?organization=%s";
    public static  String GET_HEALTHCARESERVICES_BY_LOCATION_URL = "/HealthcareService?location=%s";


    public static String PRACTITIONER_ROLE_URL = "/PractitionerRole";
    public static String GET_PRACTITIONER_ROLE_URL = "/PractitionerRole/%s";
    public static String GET_PRACTITIONER_ROLE_BY_ORGANIZATION_URL = "/PractitionerRole?organization=%s";
    public static String GET_PRACTITIONER_ROLE_BY_LOCATION_URL = "/PractitionerRole?location=%s";
    public static String GET_PRACTITIONER_ROLE_BY_HEALTHCARESERVICE_URL = "/PractitionerRole?healthcareService=%s";
    public static String GET_PRACTITIONER_ROLE_BY_PRACTITIONER_URL = "/PractitionerRole?practitioner=%s";

    public static String PATIENT_ROLE_URL = "/Patient";
    public static String GET_PATIENT_ROLE_URL = "/Patient/%s";
    public static String GET_PATIENT_BY_ORGANIZATION_URL = "/Patient?organization=%s";


    public static String RELATED_PERSON_URL = "/RelatedPerson";
    public static String GET_RELATED_PERSON_URL = "/RelatedPerson/%s";
    public static String GET_RELATED_PERSON_BY_PATIENT_URL = "/RelatedPerson?patient=%s";



    public static String ENCOUNTER_URL = "/Encounter";
    public static String GET_ENCOUNTER_URL = "/Encounter/%s";
    public static String GET_ENCOUNTER_BY_PATIENT_URL = "/Encounter?patient=%s";


    public static String CONDITION_URL = "/Condition";
    public static String GET_CONDITION_URL = "/Condition/%s";
    public static String GET_CONDITION_BY_PATIENT_URL = "/Condition?patient=%s";


    public static String OBSERVATION_URL = "/Observation";
    public static String GET_OBSERVATION_URL = "/Observation/%s";
    public static String GET_OBSERVATION_BY_PATIENT_URL = "/Observation?patient=%s";


    public static String GOAL_URL = "/Goal";
    public static String GET_GOAL_URL = "/Goal/%s";
    public static String GET_GOAL_BY_PATIENT_URL = "/Goal?patient=%s";

    public static String CARE_TEAM_URL = "/CareTeam";
    public static String GET_CARE_TEAM_URL = "/CareTeam/%s";
    public static String GET_CARE_TEAM_BY_PATIENT_URL = "/CareTeam?patient=%s";

    public static String CARE_PLAN_URL = "/CarePlan";
    public static String GET_CARE_PLAN_URL = "/CarePlan/%s";
    public static String GET_CARE_PLAN_BY_PATIENT_URL = "/CarePlan?patient=%s";
    public static String GET_CARE_PLAN_BY_CARE_TEAM_URL = "/CarePlan?care-team=%s";
    public static String GET_CARE_PLAN_BY_CONDITION_URL = "/CarePlan?condition=%s";
    public static String GET_CARE_PLAN_BY_ENCOUNTER_URL = "/CarePlan?encounter=%s";


}
