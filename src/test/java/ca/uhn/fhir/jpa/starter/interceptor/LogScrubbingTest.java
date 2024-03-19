package ca.uhn.fhir.jpa.starter.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
public class LogScrubbingTest {

 private static final Logger logger = LoggerFactory.getLogger(LogScrubbingTest.class);
	ObjectMapper mapper = new ObjectMapper();
	private static final String MESSAGE = "Hello, Testing Logs here..!!";
	private static final String MASKMESSAGE_PATIENT = "patient=12345 PATIENT:12345 Patient-12345 Patient/12345 ";
	private static final String MASKMESSAGE_TASK = "task=12345 TASK:12345 Task-12345 Task/12345 ";
	private static final String MASKMESSAGE_ACCESS_TOKEN = "access_token=\"eyJ0eXAiOiJKV1QiLCJhbGciOiJ\" access_token:\"eyJ0eXAiOiJKV1QiLCJhbGciOiJ\" access_token-\"eyJ0eXAiOiJKV1QiLCJhbGciOiJ\" ";
	private static final String MASKMESSAGE_PATIENT_SUBSTRING = "this is a patient=12345 and patient-234 qwerasdf ...) ";
	private static final String MASKMESSAGE_ENCOUNTER = "encounter=12345 ENCOUNTER:12345 Encounter-12345 Encounter/12345 " ;
	private static final String MASKMESSAGE_MEDICATIONREQUEST = "medicationrequest=12345 MEDICATIONREQUEST:12345 medicationRequest-12345 Medicationrequest/12345 ";
	private static final String MASKMESSAGE_OBSERVATION = "observation=12345 OBSERVATION:12345 Observation-12345 Observation/12345 ";
	private static final String MASKMESSAGE_ID_TOKEN = "id_token=12345 and id_token:12345 and id_token-12345 ";
	private static final String MASKMESSAGE_REFRESH_TOKEN = "refresh_token=12345 and refresh_token:12345 and refresh_token-12345 ";
	private static final String MASKMESSAGE_AUTHORIZATION = "authorization=123 authorization:1234 authorization-1234 ";
	private static final String MASKMESSAGE_ALL_STRING = "patient=12345 task=12345 encounter=12345 medicationrequest=12345 observation=12345 id_token=eyJ0eXBkJo refresh_token=eyJ0eXAjdk authorization=eyJ0eXAiOi ";
	private List<String> loggerAttributes = new ArrayList<>(List.of("level", "message", "thread_name", "logger_name"));
	private boolean isValidJson = false;

  @Test
	void testLogMessage(CapturedOutput output) throws JsonMappingException, JsonProcessingException, ParseException {
		logMessage(MESSAGE);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		verifyBasics(jsonNode);
	}

	@Test
	void logsShouldinJsonFormat(CapturedOutput output) throws Exception {
		logMessage(MESSAGE);
		JsonNode jsonNode = null;
		try {
			jsonNode = mapper.readTree(output.getAll());
			isValidJson = true;
		} catch (Exception e) {
			isValidJson = false;
		}
		Assertions.assertTrue(isValidJson);
		verifyBasics(jsonNode);
	}

	@Test
	void logsShouldHaveAttributes(CapturedOutput output) throws JsonMappingException, JsonProcessingException {
		logMessage(MESSAGE);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		jsonNode.fieldNames().forEachRemaining(n -> {
			Assertions.assertTrue(loggerAttributes.contains(n));
		});
	}

	@Test
	void checkIfLoggingIsMaskedForPatient(CapturedOutput output) throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_PATIENT);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("patient=**** PATIENT:**** Patient-**** Patient/**** ", message);
	}

	@Test
	void checkIfLoggingIsMaskedForTask(CapturedOutput output) throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_TASK);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("task=**** TASK:**** Task-**** Task/**** ", message);
	}
	
	@Test
	void checkIfLoggingIsMaskedForAccessToken(CapturedOutput output)
			throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_ACCESS_TOKEN);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("access_token=**** access_token:**** access_token-**** ", message);
	}
	
	@Test
	void checkIfLoggingIsMaskedForPatientInString(CapturedOutput output)
			throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_PATIENT_SUBSTRING);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("this is a patient=**** and patient-**** qwerasdf ...) ", message);
	}

	@Test
	void checkIfLoggingIsMaskedForEncounter(CapturedOutput output)
			throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_ENCOUNTER);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("encounter=**** ENCOUNTER:**** Encounter-**** Encounter/**** ", message);
	}

	@Test
	void checkIfLoggingIsMaskedForMedicationRequest(CapturedOutput output) throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_MEDICATIONREQUEST);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("medicationrequest=**** MEDICATIONREQUEST:**** medicationRequest-**** Medicationrequest/**** ", message);
	}
	
	@Test
	void checkIfLoggingIsMaskedForObservation(CapturedOutput output) throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_OBSERVATION);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("observation=**** OBSERVATION:**** Observation-**** Observation/**** ", message);
	}
	@Test
	void checkIfLoggingIsMaskedForIdToken(CapturedOutput output) throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_ID_TOKEN);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("id_token=**** and id_token:**** and id_token-**** ", message);
	}

	@Test
	void checkIfLoggingIsMaskedForRefreshToken(CapturedOutput output)
			throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_REFRESH_TOKEN);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("refresh_token=**** and refresh_token:**** and refresh_token-**** ", message);
	}

	@Test
	void checkIfLoggingIsMaskedForAuthorization(CapturedOutput output)
			throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_AUTHORIZATION);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals("authorization=**** authorization:**** authorization-**** ", message);
	}

	@Test
	void checkIfLoggingIsMaskedForAllInString(CapturedOutput output)
			throws JsonMappingException, JsonProcessingException {
		logMessage(MASKMESSAGE_ALL_STRING);
		JsonNode jsonNode = mapper.readTree(output.getAll());
		String message = jsonNode.get("message").asText();
		Assertions.assertEquals(
				"patient=**** task=**** encounter=**** medicationrequest=**** observation=**** id_token=**** refresh_token=**** authorization=**** ",
				message);
	}

	void logMessage(String message) {
		logger.info(message);
	}

	void verifyBasics(JsonNode node) throws ParseException {
		Assertions.assertEquals("ca.uhn.fhir.jpa.starter.interceptor.LogScrubbingTest", node.get("logger_name").textValue());
		Assertions.assertEquals("main", node.get("thread_name").textValue());
		Assertions.assertEquals(MESSAGE, node.get("message").textValue());
		Assertions.assertEquals("INFO", node.get("level").textValue());
	}
}

