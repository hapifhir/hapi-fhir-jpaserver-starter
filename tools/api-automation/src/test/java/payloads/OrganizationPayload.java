package payloads;

public class OrganizationPayload {

    public static String createOrganization(String name)
    {
        return "{\n" +
                "  \"resourceType\": \"Organization\",\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n      \\n      <p>"+name+"</p>\\n    \\n    </div>\"\n" +
                "  },\n" +
                "  \"name\": \""+name+"\"\n" +
                "}";
    }


    public static String updateOrganization(String id,String name)
    {
        return "{\n" +
                "  \"resourceType\": \"Organization\",\n" +
                "  \"id\": \""+id+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:40:06.941+00:00\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n      \\n      <p>"+name+"</p>\\n    \\n    </div>\"\n" +
                "  },\n" +
                "  \"name\": \""+name+"\",\n" +
                "  \"address\": [\n" +
                "    {\n" +
                "      \"line\": [\n" +
                "        \"3300 Washtenaw Avenue, Suite 227\"\n" +
                "      ],\n" +
                "      \"city\": \"Ann Arbor\",\n" +
                "      \"state\": \"MI\",\n" +
                "      \"postalCode\": \"48104\",\n" +
                "      \"country\": \"USA\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

    }



}
