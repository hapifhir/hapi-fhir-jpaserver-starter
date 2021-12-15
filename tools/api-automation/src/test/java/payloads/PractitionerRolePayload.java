package payloads;

public class PractitionerRolePayload {

    public static String createPractitionerRole(String organization,String location,String healthCareService, String practitioner)
    {
        return "{\n" +
                "  \"resourceType\": \"PractitionerRole\",\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n\\t\\t\\t<p>\\n\\t\\t\\t\\tDr Adam Careful is a Referring Practitioner for Acme Hospital from 1-Jan 2012 to 31-Mar\\n\\t\\t\\t\\t2012\\n\\t\\t\\t</p>\\n\\t\\t</div>\"\n" +
                "  },\n" +
                "  \"identifier\": [\n" +
                "    {\n" +
                "      \"system\": \"http://www.acme.org/practitioners\",\n" +
                "      \"value\": \"23\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"active\": true,\n" +
                "  \"period\": {\n" +
                "    \"start\": \"2012-01-01\",\n" +
                "    \"end\": \"2012-03-31\"\n" +
                "  },\n" +
                "  \"practitioner\": {\n" +
                "    \"reference\": \"Practitioner/"+practitioner+"\",\n" +
                "    \"display\": \"Dr Adam Careful\"\n" +
                "  },\n" +
                "  \"organization\": {\n" +
                "    \"reference\": \"Organization/"+organization+"\"\n" +
                "  },\n" +
                "  \"code\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0286\",\n" +
                "          \"code\": \"RP\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"specialty\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://snomed.info/sct\",\n" +
                "          \"code\": \"408443003\",\n" +
                "          \"display\": \"General medical practice\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"location\": [\n" +
                "    {\n" +
                "      \"reference\": \"Location/"+location+"\",\n" +
                "      \"display\": \"Good Health Clinic,South Wing, second floor\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"healthcareService\": [\n" +
                "    {\n" +
                "      \"reference\": \"HealthcareService/"+healthCareService+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(03) 5555 6473\",\n" +
                "      \"use\": \"work\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"email\",\n" +
                "      \"value\": \"adam.southern@example.org\",\n" +
                "      \"use\": \"work\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"availableTime\": [\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"mon\",\n" +
                "        \"tue\",\n" +
                "        \"wed\"\n" +
                "      ],\n" +
                "      \"availableStartTime\": \"09:00:00\",\n" +
                "      \"availableEndTime\": \"16:30:00\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"thu\",\n" +
                "        \"fri\"\n" +
                "      ],\n" +
                "      \"availableStartTime\": \"09:00:00\",\n" +
                "      \"availableEndTime\": \"12:00:00\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"notAvailable\": [\n" +
                "    {\n" +
                "      \"description\": \"Adam will be on extended leave during May 2017\",\n" +
                "      \"during\": {\n" +
                "        \"start\": \"2017-05-01\",\n" +
                "        \"end\": \"2017-05-20\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"availabilityExceptions\": \"Adam is generally unavailable on public holidays and during the Christmas/New Year break\"\n" +
                "}";
    }

    public static String updatePractitionerRole(String organization,String location,String healthCareService, String practitioner,String id)
    {
        return "{\n" +
                "  \"resourceType\": \"PractitionerRole\",\n" +
                "  \"id\": \""+id+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:51:41.967+00:00\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n\\t\\t\\t<p>\\n\\t\\t\\t\\tDr Adam Careful is a Referring Practitioner for Acme Hospital from 1-Jan 2012 to 31-Mar\\n\\t\\t\\t\\t2012\\n\\t\\t\\t</p>\\n\\t\\t</div>\"\n" +
                "  },\n" +
                "  \"identifier\": [\n" +
                "    {\n" +
                "      \"system\": \"http://www.acme.org/practitioners\",\n" +
                "      \"value\": \"23\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"active\": true,\n" +
                "  \"period\": {\n" +
                "    \"start\": \"2012-01-01\",\n" +
                "    \"end\": \"2012-03-31\"\n" +
                "  },\n" +
                "  \"practitioner\": {\n" +
                "    \"reference\": \"Practitioner/"+practitioner+"\",\n" +
                "    \"display\": \"Dr Adam Careful\"\n" +
                "  },\n" +
                "  \"organization\": {\n" +
                "    \"reference\": \"Organization/"+organization+"\"\n" +
                "  },\n" +
                "  \"code\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0286\",\n" +
                "          \"code\": \"RP\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"specialty\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://snomed.info/sct\",\n" +
                "          \"code\": \"408443003\",\n" +
                "          \"display\": \"General medical practice\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"location\": [\n" +
                "    {\n" +
                "      \"reference\": \"Location/"+location+"\",\n" +
                "      \"display\": \"Good Health Clinic,South Wing, second floor\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"healthcareService\": [\n" +
                "    {\n" +
                "      \"reference\": \"HealthcareService/"+healthCareService+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(03) 5555 6473\",\n" +
                "      \"use\": \"work\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"email\",\n" +
                "      \"value\": \"adam.southern@example.org\",\n" +
                "      \"use\": \"work\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"availableTime\": [\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"mon\",\n" +
                "        \"tue\",\n" +
                "        \"wed\"\n" +
                "      ],\n" +
                "      \"availableStartTime\": \"09:00:00\",\n" +
                "      \"availableEndTime\": \"16:30:00\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"thu\",\n" +
                "        \"fri\"\n" +
                "      ],\n" +
                "      \"availableStartTime\": \"09:00:00\",\n" +
                "      \"availableEndTime\": \"12:00:00\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"notAvailable\": [\n" +
                "    {\n" +
                "      \"description\": \"Adam will be on extended leave during May 2017\",\n" +
                "      \"during\": {\n" +
                "        \"start\": \"2017-05-01\",\n" +
                "        \"end\": \"2017-05-20\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"availabilityExceptions\": \"Adam is generally unavailable on public holidays and during the Christmas/New Year break Time\"\n" +
                "}";
    }


}
