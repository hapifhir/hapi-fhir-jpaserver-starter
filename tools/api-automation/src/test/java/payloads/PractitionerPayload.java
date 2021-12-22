package payloads;

public class PractitionerPayload {
    public static String createPractitioner()
    {
        return "{\n" +
                "  \"resourceType\": \"Practitioner\",\n" +
                "  \"active\": true,\n" +
                "  \"name\": [\n" +
                "    {\n" +
                "      \"family\": \"Careful\",\n" +
                "      \"given\": [\n" +
                "        \"Adam\"\n" +
                "      ],\n" +
                "      \"prefix\": [\n" +
                "        \"Dr\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"address\": [\n" +
                "    {\n" +
                "      \"use\": \"home\",\n" +
                "      \"line\": [\n" +
                "        \"534 Erewhon St\"\n" +
                "      ],\n" +
                "      \"city\": \"PleasantVille\",\n" +
                "      \"state\": \"Vic\",\n" +
                "      \"postalCode\": \"3999\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"qualification\": [\n" +
                "    {\n" +
                "      \"identifier\": [\n" +
                "        {\n" +
                "          \"system\": \"http://example.org/UniversityIdentifier\",\n" +
                "          \"value\": \"12345\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"code\": {\n" +
                "        \"coding\": [\n" +
                "          {\n" +
                "            \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0360/2.7\",\n" +
                "            \"code\": \"BS\",\n" +
                "            \"display\": \"Bachelor of Science\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"text\": \"Bachelor of Science\"\n" +
                "      },\n" +
                "      \"period\": {\n" +
                "        \"start\": \"1995\"\n" +
                "      },\n" +
                "      \"issuer\": {\n" +
                "        \"display\": \"Example University\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public static String updatePractitioner(String id)
    {
        return  "{\n" +
                "  \"resourceType\": \"Practitioner\",\n" +
                "  \"id\": \""+id+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:26:26.086+00:00\",\n" +
                "    \"source\": \"#d3eee3fe449b27a3\"\n" +
                "  },\n" +
                "  \"active\": true,\n" +
                "  \"name\": [\n" +
                "    {\n" +
                "      \"family\": \"Careful\",\n" +
                "      \"given\": [\n" +
                "        \"Adam\"\n" +
                "      ],\n" +
                "      \"prefix\": [\n" +
                "        \"Dr\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"address\": [\n" +
                "    {\n" +
                "      \"use\": \"home\",\n" +
                "      \"line\": [\n" +
                "        \"534 Erewhon St\"\n" +
                "      ],\n" +
                "      \"city\": \"PleasantVille\",\n" +
                "      \"state\": \"Vic\",\n" +
                "      \"postalCode\": \"3999\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"qualification\": [\n" +
                "    {\n" +
                "      \"identifier\": [\n" +
                "        {\n" +
                "          \"system\": \"http://example.org/UniversityIdentifier\",\n" +
                "          \"value\": \"12345\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"code\": {\n" +
                "        \"coding\": [\n" +
                "          {\n" +
                "            \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0360/2.7\",\n" +
                "            \"code\": \"BS\",\n" +
                "            \"display\": \"Bachelor of Computer Science\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"text\": \"Bachelor of Computer Science\"\n" +
                "      },\n" +
                "      \"period\": {\n" +
                "        \"start\": \"1995\"\n" +
                "      },\n" +
                "      \"issuer\": {\n" +
                "        \"display\": \"Example University\"\n" +
                "      }\n" +
                "    }\n" +
                "  ] \n" +
                "}";
    }
}
