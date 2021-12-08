package payloads;

public class HealthCareService {

    public static String createHealthCareService(String organization,String location)
    {
        return "{\n" +
                "  \"resourceType\": \"HealthcareService\",\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n\\t\\t\\t25 Dec 2013 9:15am - 9:30am: <b>Busy</b> Physiotherapy\\n\\t\\t</div>\"\n" +
                "  },\n" +
                "  \"active\": true,\n" +
                "  \"providedBy\": {\n" +
                "    \"reference\": \"Organization/"+organization+"\",\n" +
                "    \"display\": \"Good Health Clinic\"\n" +
                "  },\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/service-category\",\n" +
                "          \"code\": \"8\",\n" +
                "          \"display\": \"Counselling\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"text\": \"Counselling\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"type\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://snomed.info/sct\",\n" +
                "          \"code\": \"394913002\",\n" +
                "          \"display\": \"Psychotherapy\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://snomed.info/sct\",\n" +
                "          \"code\": \"394587001\",\n" +
                "          \"display\": \"Psychiatry\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"specialty\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://snomed.info/sct\",\n" +
                "          \"code\": \"47505003\",\n" +
                "          \"display\": \"Posttraumatic stress disorder\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"location\": [\n" +
                "    {\n" +
                "      \"reference\": \"Location/"+location+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\": \"Consulting psychologists and/or psychology services\",\n" +
                "  \"comment\": \"Providing Specialist psychology services to the greater Den Burg area, many years of experience dealing with PTSD issues\",\n" +
                "  \"extraDetails\": \"Several assessments are required for these specialist services, and the waiting times can be greater than 3 months at times. Existing patients are prioritized when requesting appointments on the schedule.\",\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(555) silent\",\n" +
                "      \"use\": \"work\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"email\",\n" +
                "      \"value\": \"directaddress@example.com\",\n" +
                "      \"use\": \"work\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"coverageArea\": [\n" +
                "    {\n" +
                "      \"reference\": \"#DenBurg\",\n" +
                "      \"display\": \"Greater Denburg area\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"serviceProvisionCode\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/service-provision-conditions\",\n" +
                "          \"code\": \"cost\",\n" +
                "          \"display\": \"Fees apply\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"eligibility\": [\n" +
                "    {\n" +
                "      \"code\": {\n" +
                "        \"coding\": [\n" +
                "          {\n" +
                "            \"display\": \"DVA Required\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"comment\": \"Evidence of application for DVA status may be sufficient for commencing assessment\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"program\": [\n" +
                "    {\n" +
                "      \"text\": \"PTSD outreach\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"characteristic\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"display\": \"Wheelchair access\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"referralMethod\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"code\": \"phone\",\n" +
                "          \"display\": \"Phone\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"code\": \"fax\",\n" +
                "          \"display\": \"Fax\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"code\": \"elec\",\n" +
                "          \"display\": \"Secure Messaging\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"code\": \"semail\",\n" +
                "          \"display\": \"Secure Email\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"appointmentRequired\": false,\n" +
                "  \"availableTime\": [\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"wed\"\n" +
                "      ],\n" +
                "      \"allDay\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"mon\",\n" +
                "        \"tue\",\n" +
                "        \"thu\",\n" +
                "        \"fri\"\n" +
                "      ],\n" +
                "      \"availableStartTime\": \"08:30:00\",\n" +
                "      \"availableEndTime\": \"05:30:00\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"sat\",\n" +
                "        \"fri\"\n" +
                "      ],\n" +
                "      \"availableStartTime\": \"09:30:00\",\n" +
                "      \"availableEndTime\": \"04:30:00\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"notAvailable\": [\n" +
                "    {\n" +
                "      \"description\": \"Christmas/Boxing Day\",\n" +
                "      \"during\": {\n" +
                "        \"start\": \"2015-12-25\",\n" +
                "        \"end\": \"2015-12-26\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"description\": \"New Years Day\",\n" +
                "      \"during\": {\n" +
                "        \"start\": \"2016-01-01\",\n" +
                "        \"end\": \"2016-01-01\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"availabilityExceptions\": \"Reduced capacity is available during the Christmas period\"\n" +
                "}";
    }


    public static String updateHealthCareService(String organization,String location,String id)
    {
        return "{\n" +
                "  \"resourceType\": \"HealthcareService\",\n" +
                "  \"id\": \""+id+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:47:39.378+00:00\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n\\t\\t\\t25 Dec 2013 9:15am - 9:30am: <b>Busy</b> Physiotherapy\\n\\t\\t</div>\"\n" +
                "  },\n" +
                "  \"active\": true,\n" +
                "  \"providedBy\": {\n" +
                "    \"reference\": \"Organization/"+organization+"\",\n" +
                "    \"display\": \"Good Health Clinic\"\n" +
                "  },\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/service-category\",\n" +
                "          \"code\": \"8\",\n" +
                "          \"display\": \"Counselling\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"text\": \"Counselling\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"type\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://snomed.info/sct\",\n" +
                "          \"code\": \"394913002\",\n" +
                "          \"display\": \"Psychotherapy\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://snomed.info/sct\",\n" +
                "          \"code\": \"394587001\",\n" +
                "          \"display\": \"Psychiatry\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"specialty\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://snomed.info/sct\",\n" +
                "          \"code\": \"47505003\",\n" +
                "          \"display\": \"Posttraumatic stress disorder\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"location\": [\n" +
                "    {\n" +
                "      \"reference\": \"Location/"+location+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\": \"Consulting psychologists and/or psychology services\",\n" +
                "  \"comment\": \"Providing Specialist psychology services to the greater Den Burg area, many years of experience dealing with PTSD issues\",\n" +
                "  \"extraDetails\": \"Several assessments are required for these specialist services, and the waiting times can be greater than 3 months at times. Existing patients are prioritized when requesting appointments on the schedule.\",\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(555) silent\",\n" +
                "      \"use\": \"work\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"email\",\n" +
                "      \"value\": \"directaddress@example.com\",\n" +
                "      \"use\": \"work\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"coverageArea\": [\n" +
                "    {\n" +
                "      \"reference\": \"#DenBurg\",\n" +
                "      \"display\": \"Greater Denburg area\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"serviceProvisionCode\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/service-provision-conditions\",\n" +
                "          \"code\": \"cost\",\n" +
                "          \"display\": \"Fees apply\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"eligibility\": [\n" +
                "    {\n" +
                "      \"code\": {\n" +
                "        \"coding\": [\n" +
                "          {\n" +
                "            \"display\": \"DVA Required\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"comment\": \"Evidence of application for DVA status may be sufficient for commencing assessment\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"program\": [\n" +
                "    {\n" +
                "      \"text\": \"PTSD outreach\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"characteristic\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"display\": \"Wheelchair access\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"referralMethod\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"code\": \"phone\",\n" +
                "          \"display\": \"Phone\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"code\": \"fax\",\n" +
                "          \"display\": \"Fax\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"code\": \"elec\",\n" +
                "          \"display\": \"Secure Messaging\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"code\": \"semail\",\n" +
                "          \"display\": \"Secure Email\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"appointmentRequired\": false,\n" +
                "  \"availableTime\": [\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"wed\"\n" +
                "      ],\n" +
                "      \"allDay\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"mon\",\n" +
                "        \"tue\",\n" +
                "        \"thu\",\n" +
                "        \"fri\"\n" +
                "      ],\n" +
                "      \"availableStartTime\": \"08:30:00\",\n" +
                "      \"availableEndTime\": \"05:30:00\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"daysOfWeek\": [\n" +
                "        \"sat\",\n" +
                "        \"fri\"\n" +
                "      ],\n" +
                "      \"availableStartTime\": \"09:30:00\",\n" +
                "      \"availableEndTime\": \"04:30:00\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"notAvailable\": [\n" +
                "    {\n" +
                "      \"description\": \"Christmas/Boxing Day\",\n" +
                "      \"during\": {\n" +
                "        \"start\": \"2015-12-25\",\n" +
                "        \"end\": \"2015-12-26\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"description\": \"New Years Day\",\n" +
                "      \"during\": {\n" +
                "        \"start\": \"2016-01-01\",\n" +
                "        \"end\": \"2016-01-01\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"availabilityExceptions\": \"Reduced capacity is available during the Christmas period Time\"\n" +
                "}";
    }
}
