package payloads;

public class EncounterPayload {

    public static String createEncounter(String patientId)
    {
        return "{\n" +
                "  \"resourceType\": \"Encounter\",\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">Encounter with patient @example</div>\"\n" +
                "  },\n" +
                "  \"status\": \"in-progress\",\n" +
                "  \"class\": {\n" +
                "    \"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\",\n" +
                "    \"code\": \"IMP\",\n" +
                "    \"display\": \"inpatient encounter to check on Obsesity\"\n" +
                "  },\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\"\n" +
                "  }\n" +
                "}";

    }


    public static String updateEncounter(String patientId,String encounterId)
    {
        return "{\n" +
                "  \"resourceType\": \"Encounter\",\n" +
                "  \"id\": \""+encounterId+"\",\n" +
                "  \"meta\": {\n" +
                "    \"versionId\": \"1\",\n" +
                "    \"lastUpdated\": \"2021-10-07T18:27:24.447+00:00\",\n" +
                "    \"source\": \"#dc1174eb62182035\"\n" +
                "  },\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">Encounter with patient @example</div>\"\n" +
                "  },\n" +
                "  \"status\": \"finished\",\n" +
                "  \"class\": {\n" +
                "    \"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\",\n" +
                "    \"code\": \"IMP\",\n" +
                "    \"display\": \"inpatient encounter to check on Obsesity\"\n" +
                "  },\n" +
                "  \"subject\": {\n" +
                "    \"reference\": \"Patient/"+patientId+"\"\n" +
                "  }\n" +
                "}";

    }
}
