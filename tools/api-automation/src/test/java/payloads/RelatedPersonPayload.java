package payloads;

public class RelatedPersonPayload {

    public static String createRelatedPerson(String patientId)
    {
        return "{\n" +
                "  \"resourceType\": \"RelatedPerson\",\n" +
                "  \"active\": true,\n" +
                "  \"patient\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\"\n" +
                "  },\n" +
                "  \"relationship\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0131\",\n" +
                "          \"code\": \"N\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/v3-RoleCode\",\n" +
                "          \"code\": \"WIFE\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\": [\n" +
                "    {\n" +
                "      \"family\": \"du Marché\",\n" +
                "      \"_family\": {\n" +
                "        \"extension\": [\n" +
                "          {\n" +
                "            \"url\": \"http://hl7.org/fhir/StructureDefinition/humanname-own-prefix\",\n" +
                "            \"valueString\": \"VV\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"given\": [\n" +
                "        \"Bénédicte\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"+33 (237) 998327\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"gender\": \"female\",\n" +
                "  \"address\": [\n" +
                "    {\n" +
                "      \"line\": [\n" +
                "        \"43, Place du Marché Sainte Catherine\"\n" +
                "      ],\n" +
                "      \"city\": \"Paris\",\n" +
                "      \"postalCode\": \"75004\",\n" +
                "      \"country\": \"FRA\"\n" +
                "    }\n" +
                "  ] \n" +
                "}";
    }


    public static String updateRelatedPerson(String patientId,String relatedPersonId)
    {
        return "{\n" +
                "  \"resourceType\": \"RelatedPerson\",\n" +
                "  \"id\": \""+relatedPersonId+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:26:17.389+00:00\",\n" +
                "    \"source\": \"#e9627fe50b2b05b7\"\n" +
                "  },\n" +
                "  \"active\": true,\n" +
                "  \"patient\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\"\n" +
                "  },\n" +
                "  \"relationship\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0131\",\n" +
                "          \"code\": \"N\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/v3-RoleCode\",\n" +
                "          \"code\": \"WIFE\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\": [\n" +
                "    {\n" +
                "      \"family\": \"du Marché\",\n" +
                "      \"_family\": {\n" +
                "        \"extension\": [\n" +
                "          {\n" +
                "            \"url\": \"http://hl7.org/fhir/StructureDefinition/humanname-own-prefix\",\n" +
                "            \"valueString\": \"VV\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"given\": [\n" +
                "        \"Bénédicte\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"+33 (237) 998327\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"gender\": \"male\",\n" +
                "  \"address\": [\n" +
                "    {\n" +
                "      \"line\": [\n" +
                "        \"43, Place du Marché Sainte Catherine\"\n" +
                "      ],\n" +
                "      \"city\": \"Paris\",\n" +
                "      \"postalCode\": \"75004\",\n" +
                "      \"country\": \"FRA\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
