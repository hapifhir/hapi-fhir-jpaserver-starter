package payloads;

public class CareTeamPayload {

    public static String createCareTeam(String practitionerId,String patientId,String encounterId,String organizationId)
    {
        return "{\n" +
                "  \"resourceType\": \"CareTeam\",\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">Care Team</div>\"\n" +
                "  },\n" +
                "  \"contained\": [\n" +
                "    {\n" +
                "      \"resourceType\": \"Practitioner\",\n" +
                "      \"id\": \""+practitionerId+"\",\n" +
                "     \"name\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"family\": \"Careful\",\n" +
                "\t\t\t\t\t\"given\": [\n" +
                "\t\t\t\t\t\t\"Adam\"\n" +
                "\t\t\t\t\t],\n" +
                "\t\t\t\t\t\"prefix\": [\n" +
                "\t\t\t\t\t\t\"Dr\"\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"active\",\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://loinc.org\",\n" +
                "          \"code\": \"LA27976-2\",\n" +
                "          \"display\": \"Encounter-focused care team\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\": \"Peter James Charlmers Care team\",\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\",\n" +
                "    \"display\": \"Peter James Chalmers\"\n" +
                "  },\n" +
                "  \"encounter\": {\n" +
                "    \"reference\": \"Encounter/"+encounterId+"\"\n" +
                "  },\n" +
                "  \"period\": {\n" +
                "    \"end\": \"2013-01-01\"\n" +
                "  },\n" +
                "  \"participant\": [\n" +
                "    {\n" +
                "      \"role\": [\n" +
                "        {\n" +
                "          \"text\": \"responsiblePerson\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"member\": {\n" +
                "        \"reference\": \"Patient/"+patientId+"\",\n" +
                "        \"display\": \"Peter James Chalmers\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": [\n" +
                "        {\n" +
                "          \"text\": \"adviser\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"member\": {\n" +
                "        \"reference\": \"#pr1\",\n" +
                "        \"display\": \"Dorothy Dietition\"\n" +
                "      },\n" +
                "      \"onBehalfOf\": {\n" +
                "        \"reference\": \"Organization/f001\"\n" +
                "      },\n" +
                "      \"period\": {\n" +
                "        \"end\": \"2013-01-01\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"managingOrganization\": [\n" +
                "    {\n" +
                "      \"reference\": \"Organization/"+organizationId+"\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }


    public static String updateCareTeam(String practitionerId,String patientId,String encounterId,String organizationId,String careTeamId)
    {
        return "{\n" +
                "  \"resourceType\": \"CareTeam\",\n" +
                "  \"id\": \""+careTeamId+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:57:11.183+00:00\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">Care Team</div>\"\n" +
                "  },\n" +
                "  \"contained\": [\n" +
                "    {\n" +
                "      \"resourceType\": \"Practitioner\",\n" +
                "      \"id\": \""+practitionerId+"\",\n" +
                "      \"name\": [\n" +
                "        {\n" +
                "          \"family\": \"Careful\",\n" +
                "          \"given\": [\n" +
                "            \"Adam\"\n" +
                "          ],\n" +
                "          \"prefix\": [\n" +
                "            \"Dr\"\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"active\",\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://loinc.org\",\n" +
                "          \"code\": \"LA27976-2\",\n" +
                "          \"display\": \"Encounter-focused care team\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\": \"Peter Charlmers Care team\",\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\",\n" +
                "    \"display\": \"Peter James Chalmers\"\n" +
                "  },\n" +
                "  \"encounter\": {\n" +
                "    \"reference\": \"Encounter/"+encounterId+"\"\n" +
                "  },\n" +
                "  \"period\": {\n" +
                "    \"end\": \"2013-01-01\"\n" +
                "  },\n" +
                "  \"participant\": [\n" +
                "    {\n" +
                "      \"role\": [\n" +
                "        {\n" +
                "          \"text\": \"responsiblePerson\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"member\": {\n" +
                "        \"reference\": \"Patient/"+patientId+"\",\n" +
                "        \"display\": \"Peter James Chalmers\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": [\n" +
                "        {\n" +
                "          \"text\": \"responsiblePerson\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"member\": {\n" +
                "        \"reference\": \"#pr1\",\n" +
                "        \"display\": \"Dorothy Dietition\"\n" +
                "      },\n" +
                "      \"onBehalfOf\": {\n" +
                "        \"reference\": \"Organization/f001\"\n" +
                "      },\n" +
                "      \"period\": {\n" +
                "        \"end\": \"2013-01-01\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"managingOrganization\": [\n" +
                "    {\n" +
                "      \"reference\": \"Organization/"+organizationId+"\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
