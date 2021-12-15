package payloads;

public class LocationPayload {

    public static String createLocation(String name)
    {
        return "{\n" +
                "  \"resourceType\": \"Location\",\n" +
                "  \"status\": \"active\",\n" +
                "  \"name\": \""+name+"\",\n" +
                "  \"description\": \"HL7 Headquarters\",\n" +
                "  \"mode\": \"instance\",\n" +
                "  \"type\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/v3-RoleCode\",\n" +
                "          \"code\": \"SLEEP\",\n" +
                "          \"display\": \"Sleep disorders unit\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(+1) 734-677-7777\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"fax\",\n" +
                "      \"value\": \"(+1) 734-677-6622\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"email\",\n" +
                "      \"value\": \"hq@HL7.org\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"address\": {\n" +
                "    \"line\": [\n" +
                "      \"3300 Washtenaw Avenue, Suite 227\"\n" +
                "    ],\n" +
                "    \"city\": \"Ann Arbor\",\n" +
                "    \"state\": \"MI\",\n" +
                "    \"postalCode\": \"48104\",\n" +
                "    \"country\": \"USA\"\n" +
                "  },\n" +
                "  \"physicalType\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/location-physical-type\",\n" +
                "        \"code\": \"bu\",\n" +
                "        \"display\": \"Building\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"position\": {\n" +
                "    \"longitude\": 42.256500,\n" +
                "    \"latitude\": -83.694710\n" +
                "  }\n" +
                "}";
    }


    public static String updateLocation(String id,String name)
    {
        return "{\n" +
                "  \"resourceType\": \"Location\",\n" +
                "  \"id\": \""+id+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:40:19.425+00:00\",\n" +
                "    \"source\": \"#f41945d2549f7614\"\n" +
                "  },\n" +
                "  \"status\": \"active\",\n" +
                "  \"name\": \""+name+"\",\n" +
                "  \"description\": \"HL7 Headquarters\",\n" +
                "  \"mode\": \"instance\",\n" +
                "  \"type\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/v3-RoleCode\",\n" +
                "          \"code\": \"SLEEP\",\n" +
                "          \"display\": \"Sleep disorders unit\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(+1) 734-677-0000\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"fax\",\n" +
                "      \"value\": \"(+1) 734-677-6622\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"email\",\n" +
                "      \"value\": \"hq@HL7.org\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"address\": {\n" +
                "    \"line\": [\n" +
                "      \"3300 Washtenaw Avenue, Suite 227\"\n" +
                "    ],\n" +
                "    \"city\": \"Ann Arbor\",\n" +
                "    \"state\": \"MI\",\n" +
                "    \"postalCode\": \"48104\",\n" +
                "    \"country\": \"USA\"\n" +
                "  },\n" +
                "  \"physicalType\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/location-physical-type\",\n" +
                "        \"code\": \"bu\",\n" +
                "        \"display\": \"Buildings\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"position\": {\n" +
                "    \"longitude\": 42.256500,\n" +
                "    \"latitude\": -83.694710\n" +
                "  }\n" +
                "}";
    }
}
