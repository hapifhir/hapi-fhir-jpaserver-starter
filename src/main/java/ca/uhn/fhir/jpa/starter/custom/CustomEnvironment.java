package ca.uhn.fhir.jpa.starter.custom;

import java.util.ArrayList;
import java.util.Arrays;

public class CustomEnvironment {
    public static String auth_url = System.getenv("AUTH_URL");
    public static String auth_cert_endpoint = System.getenv("AUTH_CERT_ENDPOINT");
    public static String JWT_CERT = null;
    public static ArrayList<String> resourceTypes_al = new ArrayList<>(Arrays.asList("Bundle", "Patient", "Observation", "Condition", "Group", "Location"));
    public static String stackType_ICMR = "ICMR";
    public static String stackType_FHIR = "FHIR";
}
