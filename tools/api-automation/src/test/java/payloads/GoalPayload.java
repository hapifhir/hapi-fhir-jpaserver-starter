package payloads;

public class GoalPayload {

    public static String createGoal(String patientId,String observationId)
    {
        return "{\n" +
                "  \"resourceType\": \"Goal\",\n" +
                "  \"text\": {\n" +
                "    \"status\": \"additional\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n\\t\\t\\t<p> A simple care goal for a patient to lose weight due to obesity.</p>\\n\\t\\t</div>\"\n" +
                "  },\n" +
                "  \"lifecycleStatus\": \"on-hold\",\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/goal-category\",\n" +
                "          \"code\": \"dietary\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"priority\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/goal-priority\",\n" +
                "        \"code\": \"high-priority\",\n" +
                "        \"display\": \"High Priority\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"text\": \"high\"\n" +
                "  },\n" +
                "  \"description\": {\n" +
                "    \"text\": \"Target weight is 160 to 180 lbs.\"\n" +
                "  },\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\",\n" +
                "    \"display\": \"Peter James Chalmers\"\n" +
                "  },\n" +
                "  \"startDate\": \"2015-04-05\",\n" +
                "  \"target\": [\n" +
                "    {\n" +
                "      \"measure\": {\n" +
                "        \"coding\": [\n" +
                "          {\n" +
                "            \"system\": \"http://loinc.org\",\n" +
                "            \"code\": \"3141-9\",\n" +
                "            \"display\": \"Weight Measured\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"detailRange\": {\n" +
                "        \"low\": {\n" +
                "          \"value\": 160,\n" +
                "          \"unit\": \"lbs\",\n" +
                "          \"system\": \"http://unitsofmeasure.org\",\n" +
                "          \"code\": \"[lb_av]\"\n" +
                "        },\n" +
                "        \"high\": {\n" +
                "          \"value\": 180,\n" +
                "          \"unit\": \"lbs\",\n" +
                "          \"system\": \"http://unitsofmeasure.org\",\n" +
                "          \"code\": \"[lb_av]\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"dueDate\": \"2016-04-05\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"statusDate\": \"2016-02-14\",\n" +
                "  \"statusReason\": \"Patient wants to defer weight loss until after honeymoon.\",\n" +
                "  \"expressedBy\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\",\n" +
                "    \"display\": \"Peter James Chalmers\"\n" +
                "  },\n" +
                "  \"addresses\": [\n" +
                "    {\n" +
                "      \"display\": \"obesity condition\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"outcomeReference\": [\n" +
                "    {\n" +
                "      \"reference\": \"Observation/"+observationId+"\",\n" +
                "      \"display\": \"Body Weight Measured\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }


    public static String updateGoal(String patientId,String observationId,String GoalId)
    {
        return "{\n" +
                "  \"resourceType\": \"Goal\",\n" +
                "  \"id\": \""+GoalId+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T19:07:21.454+00:00\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"additional\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n\\t\\t\\t<p> A simple care goal for a patient to lose weight due to obesity.</p>\\n\\t\\t</div>\"\n" +
                "  },\n" +
                "  \"lifecycleStatus\": \"active\",\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/goal-category\",\n" +
                "          \"code\": \"dietary\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"priority\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/goal-priority\",\n" +
                "        \"code\": \"high-priority\",\n" +
                "        \"display\": \"High Priority\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"text\": \"high\"\n" +
                "  },\n" +
                "  \"description\": {\n" +
                "    \"text\": \"Target weight is 160 to 180 lbs.\"\n" +
                "  },\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\",\n" +
                "    \"display\": \"Peter James Chalmers\"\n" +
                "  },\n" +
                "  \"startDate\": \"2015-04-05\",\n" +
                "  \"target\": [\n" +
                "    {\n" +
                "      \"measure\": {\n" +
                "        \"coding\": [\n" +
                "          {\n" +
                "            \"system\": \"http://loinc.org\",\n" +
                "            \"code\": \"3141-9\",\n" +
                "            \"display\": \"Weight Measured\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"detailRange\": {\n" +
                "        \"low\": {\n" +
                "          \"value\": 160,\n" +
                "          \"unit\": \"lbs\",\n" +
                "          \"system\": \"http://unitsofmeasure.org\",\n" +
                "          \"code\": \"[lb_av]\"\n" +
                "        },\n" +
                "        \"high\": {\n" +
                "          \"value\": 180,\n" +
                "          \"unit\": \"lbs\",\n" +
                "          \"system\": \"http://unitsofmeasure.org\",\n" +
                "          \"code\": \"[lb_av]\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"dueDate\": \"2016-04-05\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"statusDate\": \"2016-02-14\",\n" +
                "  \"statusReason\": \"Patient wants to defer weight loss until after honeymoon.\",\n" +
                "  \"expressedBy\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\",\n" +
                "    \"display\": \"Peter James Chalmers\"\n" +
                "  },\n" +
                "  \"addresses\": [\n" +
                "    {\n" +
                "      \"display\": \"obesity condition\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"outcomeReference\": [\n" +
                "    {\n" +
                "      \"reference\": \"Observation/"+observationId+"\",\n" +
                "      \"display\": \"Body Weight Measured\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
