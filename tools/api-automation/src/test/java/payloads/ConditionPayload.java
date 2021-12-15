package payloads;

public class ConditionPayload {

    public static String createCondition(String patientId)
    {
        return "{\n" +
                "  \"resourceType\": \"Condition\",\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">Obestity</div>\"\n" +
                "  },\n" +
                "  \"clinicalStatus\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/condition-clinical\",\n" +
                "        \"code\": \"active\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"verificationStatus\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/condition-ver-status\",\n" +
                "        \"code\": \"confirmed\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/condition-category\",\n" +
                "          \"code\": \"problem-list-item\",\n" +
                "          \"display\": \"Problem List Item\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"severity\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://snomed.info/sct\",\n" +
                "        \"code\": \"255604002\",\n" +
                "        \"display\": \"Mild\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"code\": {\n" +
                "    \"text\": \"Obeseity\"\n" +
                "  },\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\"\n" +
                "  },\n" +
                "  \"onsetString\": \"approximately November 2012\"\n" +
                "}";
    }



    public static String updateCondition(String patientId,String conditionId)
    {
        return "{\n" +
                "  \"resourceType\": \"Condition\",\n" +
                "  \"id\": \""+conditionId+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:34:35.164+00:00\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">Obestity</div>\"\n" +
                "  },\n" +
                "  \"clinicalStatus\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/condition-clinical\",\n" +
                "        \"code\": \"active\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"verificationStatus\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/condition-ver-status\",\n" +
                "        \"code\": \"confirmed\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/condition-category\",\n" +
                "          \"code\": \"problem-list-item\",\n" +
                "          \"display\": \"Problem List Item\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"severity\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://snomed.info/sct\",\n" +
                "        \"code\": \"255604002\",\n" +
                "        \"display\": \"Mild\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "     \"code\": {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"http://example.org/tbd\",\n" +
                "              \"code\": \"TBD\",\n" +
                "              \"display\": \"Pregnancy\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"text\": \"Pregnancy\"\n" +
                "        },\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\"\n" +
                "  },\n" +
                "  \"onsetString\": \"approximately November 2012\"\n" +
                "}";
    }
}
