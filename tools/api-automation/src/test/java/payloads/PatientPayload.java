package payloads;

public class PatientPayload {

    public static String createPatient(String organization)
    {
        return "{\n" +
                "  \"resourceType\": \"Patient\",\n" +
                "  \"active\": true,\n" +
                "  \"name\": [\n" +
                "    {\n" +
                "      \"use\": \"official\",\n" +
                "      \"family\": \"Chalmers\",\n" +
                "      \"given\": [\n" +
                "        \"Peter\",\n" +
                "        \"James\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"use\": \"usual\",\n" +
                "      \"given\": [\n" +
                "        \"Jim\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"use\": \"maiden\",\n" +
                "      \"family\": \"Windsor\",\n" +
                "      \"given\": [\n" +
                "        \"Peter\",\n" +
                "        \"James\"\n" +
                "      ],\n" +
                "      \"period\": {\n" +
                "        \"end\": \"2002\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"use\": \"home\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(03) 5555 6473\",\n" +
                "      \"use\": \"work\",\n" +
                "      \"rank\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(03) 3410 5613\",\n" +
                "      \"use\": \"mobile\",\n" +
                "      \"rank\": 2\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(03) 5555 8834\",\n" +
                "      \"use\": \"old\",\n" +
                "      \"period\": {\n" +
                "        \"end\": \"2014\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"gender\": \"male\",\n" +
                "  \"birthDate\": \"1974-12-25\",\n" +
                "  \"deceasedBoolean\": false,\n" +
                "  \"address\": [\n" +
                "    {\n" +
                "      \"use\": \"home\",\n" +
                "      \"type\": \"both\",\n" +
                "      \"text\": \"534 Erewhon St PeasantVille, Rainbow, Vic  3999\",\n" +
                "      \"line\": [\n" +
                "        \"534 Erewhon St\"\n" +
                "      ],\n" +
                "      \"city\": \"PleasantVille\",\n" +
                "      \"district\": \"Rainbow\",\n" +
                "      \"state\": \"Vic\",\n" +
                "      \"postalCode\": \"3999\",\n" +
                "      \"period\": {\n" +
                "        \"start\": \"1974-12-25\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"contact\": [\n" +
                "    {\n" +
                "      \"relationship\": [\n" +
                "        {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0131\",\n" +
                "              \"code\": \"N\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"name\": {\n" +
                "        \"family\": \"du Marché\",\n" +
                "        \"_family\": {\n" +
                "          \"extension\": [\n" +
                "            {\n" +
                "              \"url\": \"http://hl7.org/fhir/StructureDefinition/humanname-own-prefix\",\n" +
                "              \"valueString\": \"VV\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"given\": [\n" +
                "          \"Bénédicte\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"telecom\": [\n" +
                "        {\n" +
                "          \"system\": \"phone\",\n" +
                "          \"value\": \"+33 (237) 998327\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"address\": {\n" +
                "        \"use\": \"home\",\n" +
                "        \"type\": \"both\",\n" +
                "        \"line\": [\n" +
                "          \"534 Erewhon St\"\n" +
                "        ],\n" +
                "        \"city\": \"PleasantVille\",\n" +
                "        \"district\": \"Rainbow\",\n" +
                "        \"state\": \"Vic\",\n" +
                "        \"postalCode\": \"3999\",\n" +
                "        \"period\": {\n" +
                "          \"start\": \"1974-12-25\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"gender\": \"female\",\n" +
                "      \"period\": {\n" +
                "        \"start\": \"2012\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"managingOrganization\": {\n" +
                "    \"reference\": \"Organization/"+organization+"\"\n" +
                "  }\n" +
                "}";

    }

    public static String upadtePatient(String organization,String patientId)
    {
        return "{\n" +
                "  \"resourceType\": \"Patient\",\n" +
                "  \"id\": \""+patientId+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:25:42.052+00:00\",\n" +
                "    \"source\": \"#b21d90dc9f32723f\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Peter James <b>CHALMERS </b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Address</td><td><span>534 Erewhon St </span><br/><span>PleasantVille </span><span>Vic </span></td></tr><tr><td>Date of birth</td><td><span>25 December 1974</span></td></tr></tbody></table></div>\"\n" +
                "  },\n" +
                "  \"active\": true,\n" +
                "  \"name\": [\n" +
                "    {\n" +
                "      \"use\": \"official\",\n" +
                "      \"family\": \"Chalmers\",\n" +
                "      \"given\": [\n" +
                "        \"Peter\",\n" +
                "        \"James\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"use\": \"usual\",\n" +
                "      \"given\": [\n" +
                "        \"Jim\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"use\": \"maiden\",\n" +
                "      \"family\": \"Windsor\",\n" +
                "      \"given\": [\n" +
                "        \"Peter\",\n" +
                "        \"James\"\n" +
                "      ],\n" +
                "      \"period\": {\n" +
                "        \"end\": \"2002\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"use\": \"home\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(03) 5555 6473\",\n" +
                "      \"use\": \"work\",\n" +
                "      \"rank\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(03) 3410 5613\",\n" +
                "      \"use\": \"mobile\",\n" +
                "      \"rank\": 2\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"(03) 5555 8834\",\n" +
                "      \"use\": \"old\",\n" +
                "      \"period\": {\n" +
                "        \"end\": \"2014\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"gender\": \"female\",\n" +
                "  \"birthDate\": \"1974-12-25\",\n" +
                "  \"deceasedBoolean\": false,\n" +
                "  \"address\": [\n" +
                "    {\n" +
                "      \"use\": \"home\",\n" +
                "      \"type\": \"both\",\n" +
                "      \"text\": \"534 Erewhon St PeasantVille, Rainbow, Vic  3999\",\n" +
                "      \"line\": [\n" +
                "        \"534 Erewhon St\"\n" +
                "      ],\n" +
                "      \"city\": \"PleasantVille\",\n" +
                "      \"district\": \"Rainbow\",\n" +
                "      \"state\": \"Vic\",\n" +
                "      \"postalCode\": \"3999\",\n" +
                "      \"period\": {\n" +
                "        \"start\": \"1974-12-25\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"contact\": [\n" +
                "    {\n" +
                "      \"relationship\": [\n" +
                "        {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0131\",\n" +
                "              \"code\": \"N\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"name\": {\n" +
                "        \"family\": \"du Marché\",\n" +
                "        \"_family\": {\n" +
                "          \"extension\": [\n" +
                "            {\n" +
                "              \"url\": \"http://hl7.org/fhir/StructureDefinition/humanname-own-prefix\",\n" +
                "              \"valueString\": \"VV\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"given\": [\n" +
                "          \"Bénédicte\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"telecom\": [\n" +
                "        {\n" +
                "          \"system\": \"phone\",\n" +
                "          \"value\": \"+33 (237) 998327\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"address\": {\n" +
                "        \"use\": \"home\",\n" +
                "        \"type\": \"both\",\n" +
                "        \"line\": [\n" +
                "          \"534 Erewhon St\"\n" +
                "        ],\n" +
                "        \"city\": \"PleasantVille\",\n" +
                "        \"district\": \"Rainbow\",\n" +
                "        \"state\": \"Vic\",\n" +
                "        \"postalCode\": \"3999\",\n" +
                "        \"period\": {\n" +
                "          \"start\": \"1974-12-25\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"gender\": \"female\",\n" +
                "      \"period\": {\n" +
                "        \"start\": \"2012\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"managingOrganization\": {\n" +
                "    \"reference\": \"Organization/"+organization+"\"\n" +
                "  }\n" +
                "}";

    }
}
