package payloads;

public class CarePlanPayload {

    public static String createCarePlan(String patientId,String careTeam,String condition,String goalId,String encounterId,String practitionerId)
    {
        return "{\n" +
                "  \"resourceType\" : \"CarePlan\",\n" +
                "\t  \"text\": {\n" +
                "    \"status\": \"additional\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n      <p> Care plan to address obesity.  Goal is a target weight of 160 to 180 lbs.  Activities include diet and exercise.</p>\\n    </div>\"\n" +
                "  },\n" +
                "  \"status\" : \"active\",\n" +
                "  \"intent\" : \"plan\",\n" +
                "  \"subject\" : {\n" +
                "    \"reference\" : \"Patient/"+patientId+"\",\n" +
                "    \"display\" : \"Peter James Chalmers\"\n" +
                "  },\n" +
                "  \"period\" : {\n" +
                "    \"start\" : \"2019-05-24\",\n" +
                "    \"end\" : \"2020-02-24\"\n" +
                "  },\n" +
                "  \"careTeam\" : [\n" +
                "    {\n" +
                "      \"reference\" : \"CareTeam/"+careTeam+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"addresses\" : [\n" +
                "    {\n" +
                "      \"reference\" : \"Condition/"+condition+"\",\n" +
                "      \"display\" : \"Obesity\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"goal\" : [\n" +
                "    {\n" +
                "      \"reference\" : \"Goal/"+goalId+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"activity\" : [\n" +
                "    {\n" +
                "      \"outcomeReference\" : [\n" +
                "        {\n" +
                "          \"reference\" : \"Encounter/"+encounterId+"\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"detail\" : {\n" +
                "        \"kind\" : \"ServiceRequest\",\n" +
                "        \"instantiatesCanonical\" : [\n" +
                "          \"http://fhir.org/guides/who/anc-cds/PlanDefinition/anc-contact\"\n" +
                "        ],\n" +
                "        \"code\" : {\n" +
                "          \"coding\" : [\n" +
                "            {\n" +
                "              \"system\" : \"http://example.org/CodeSystem/encounter-type\",\n" +
                "              \"code\" : \"1st-contact\",\n" +
                "              \"display\" : \"1st care contact\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"text\" : \"1st care contact\"\n" +
                "        },\n" +
                "        \"status\" : \"in-progress\",\n" +
                "        \"performer\" : [\n" +
                "          {\n" +
                "            \"reference\" : \"Practitioner/"+practitionerId+"\",\n" +
                "            \"display\" : \"Dr. Adam Carefule\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"description\" : \"1st care contact\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"detail\" : {\n" +
                "        \"kind\" : \"ServiceRequest\",\n" +
                "        \"instantiatesCanonical\" : [\n" +
                "          \"http://fhir.org/guides/who/anc-cds/PlanDefinition/anc-contact\"\n" +
                "        ],\n" +
                "        \"code\" : {\n" +
                "          \"coding\" : [\n" +
                "            {\n" +
                "              \"system\" : \"http://example.org/CodeSystem/encounter-type\",\n" +
                "              \"code\" : \"anc-contact\",\n" +
                "              \"display\" : \"1st care contact\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"text\" : \"1st care contact\"\n" +
                "        },\n" +
                "        \"status\" : \"scheduled\",\n" +
                "        \"scheduledPeriod\" : {\n" +
                "          \"start\" : \"2019-07-26\"\n" +
                "        },\n" +
                "        \"performer\" : [\n" +
                "          {\n" +
                "             \"reference\" : \"Practitioner/"+practitionerId+"\",\n" +
                "            \"display\" : \"Dr. Adam Carefule\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"description\" : \"Second contact to occurs in 2 months time\"\n" +
                "      }\n" +
                "    }\n" +
                "\t\t\n" +
                "  ]\n" +
                "}";
    }

    public static String updateCarePlan(String patientId,String careTeam,String condition,String goalId,String encounterId,String practitionerId,String carePlanId)
    {
       return  "{\n" +
                "  \"resourceType\": \"CarePlan\",\n" +
                "  \"id\": \""+carePlanId+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T19:11:08.728+00:00\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"additional\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n      <p> Care plan to address obesity.  Goal is a target weight of 160 to 180 lbs.  Activities include diet and exercise.</p>\\n    </div>\"\n" +
                "  },\n" +
                "  \"status\": \"active\",\n" +
                "  \"intent\": \"plan\",\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\",\n" +
                "    \"display\": \"Peter James Chalmers\"\n" +
                "  },\n" +
                "  \"period\": {\n" +
                "    \"start\": \"2019-05-24\",\n" +
                "    \"end\": \"2020-02-24\"\n" +
                "  },\n" +
                "  \"careTeam\": [\n" +
                "    {\n" +
                "      \"reference\": \"CareTeam/"+careTeam+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"addresses\": [\n" +
                "    {\n" +
                "      \"reference\": \"Condition/"+condition+"\",\n" +
                "      \"display\": \"Obesity\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"goal\": [\n" +
                "    {\n" +
                "      \"reference\": \"Goal/"+goalId+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"activity\": [\n" +
                "    {\n" +
                "      \"outcomeReference\": [\n" +
                "        {\n" +
                "          \"reference\": \"Encounter/"+encounterId+"\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"detail\": {\n" +
                "        \"kind\": \"ServiceRequest\",\n" +
                "        \"instantiatesCanonical\": [\n" +
                "          \"http://fhir.org/guides/who/anc-cds/PlanDefinition/anc-contact\"\n" +
                "        ],\n" +
                "        \"code\": {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"http://example.org/CodeSystem/encounter-type\",\n" +
                "              \"code\": \"1st-contact\",\n" +
                "              \"display\": \"1st care contact\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"text\": \"1st care contact\"\n" +
                "        },\n" +
                "        \"status\": \"in-progress\",\n" +
                "        \"performer\": [\n" +
                "          {\n" +
                "            \"reference\": \"Practitioner/"+practitionerId+"\",\n" +
                "            \"display\": \"Dr. Adam Carefule\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"description\": \"1st care contact\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"detail\": {\n" +
                "        \"kind\": \"ServiceRequest\",\n" +
                "        \"instantiatesCanonical\": [\n" +
                "          \"http://fhir.org/guides/who/anc-cds/PlanDefinition/anc-contact\"\n" +
                "        ],\n" +
                "        \"code\": {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"http://example.org/CodeSystem/encounter-type\",\n" +
                "              \"code\": \"anc-contact\",\n" +
                "              \"display\": \"1st care contact\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"text\": \"1st care contact\"\n" +
                "        },\n" +
                "        \"status\": \"scheduled\",\n" +
                "        \"scheduledPeriod\": {\n" +
                "          \"start\": \"2019-07-26\"\n" +
                "        },\n" +
                "        \"performer\": [\n" +
                "          {\n" +
                "            \"reference\": \"Practitioner/"+practitionerId+"\",\n" +
                "            \"display\": \"Dr. Adam Carefule\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"description\": \"Second contact to occurs in 5 months time\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
