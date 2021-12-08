package payloads;

public class ObservationPayload {


    public static String createObservation(String patientId)
    {
        return "{\n" +
                "  \"resourceType\": \"Observation\",\n" +
                "  \"meta\": {\n" +
                "    \"profile\": [\n" +
                "      \"http://hl7.org/fhir/StructureDefinition/vitalsigns\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: bmi</p><p><b>meta</b>: </p><p><b>status</b>: final</p><p><b>category</b>: Vital Signs <span>(Details : {http://terminology.hl7.org/CodeSystem/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b>: BMI <span>(Details : {LOINC code '39156-5' = 'Body mass index (BMI) [Ratio]', given as 'Body mass index (BMI) [Ratio]'})</span></p><p><b>subject</b>: <a>Patient/example</a></p><p><b>effective</b>: 02/07/1999</p><p><b>value</b>: 16.2 kg/m2<span> (Details: UCUM code kg/m2 = 'kg/m2')</span></p></div>\"\n" +
                "  },\n" +
                "  \"status\": \"final\",\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/observation-category\",\n" +
                "          \"code\": \"vital-signs\",\n" +
                "          \"display\": \"Vital Signs\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"text\": \"Vital Signs\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"code\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://loinc.org\",\n" +
                "        \"code\": \"39156-5\",\n" +
                "        \"display\": \"Body mass index (BMI) [Ratio]\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"text\": \"BMI\"\n" +
                "  },\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\"\n" +
                "  },\n" +
                "  \"effectiveDateTime\": \"1999-07-02\",\n" +
                "  \"valueQuantity\": {\n" +
                "    \"value\": 16.2,\n" +
                "    \"unit\": \"kg/m2\",\n" +
                "    \"system\": \"http://unitsofmeasure.org\",\n" +
                "    \"code\": \"kg/m2\"\n" +
                "  }\n" +
                "}";
    }


    public static String updateObservation(String patientId,String observationId)
    {
        return "{\n" +
                "  \"resourceType\": \"Observation\",\n" +
                "  \"id\": \""+observationId+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:28:30.709+00:00\",\n" +
                "    \"profile\": [\n" +
                "      \"http://hl7.org/fhir/StructureDefinition/vitalsigns\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: bmi</p><p><b>meta</b>: </p><p><b>status</b>: final</p><p><b>category</b>: Vital Signs <span>(Details : {http://terminology.hl7.org/CodeSystem/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b>: BMI <span>(Details : {LOINC code '39156-5' = 'Body mass index (BMI) [Ratio]', given as 'Body mass index (BMI) [Ratio]'})</span></p><p><b>subject</b>: <a>Patient/example</a></p><p><b>effective</b>: 02/07/1999</p><p><b>value</b>: 16.2 kg/m2<span> (Details: UCUM code kg/m2 = 'kg/m2')</span></p></div>\"\n" +
                "  },\n" +
                "  \"status\": \"corrected\",\n" +
                "  \"category\": [\n" +
                "    {\n" +
                "      \"coding\": [\n" +
                "        {\n" +
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/observation-category\",\n" +
                "          \"code\": \"vital-signs\",\n" +
                "          \"display\": \"Vital Signs\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"text\": \"Vital Signs\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"code\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://loinc.org\",\n" +
                "        \"code\": \"39156-5\",\n" +
                "        \"display\": \"Body mass index (BMI) [Ratio]\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"text\": \"BMI\"\n" +
                "  },\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\"\n" +
                "  },\n" +
                "  \"effectiveDateTime\": \"1999-07-02\",\n" +
                "  \"valueQuantity\": {\n" +
                "    \"value\": 18.2,\n" +
                "    \"unit\": \"kg/m2\",\n" +
                "    \"system\": \"http://unitsofmeasure.org\",\n" +
                "    \"code\": \"kg/m2\"\n" +
                "  }\n" +
                "}";
    }
}
