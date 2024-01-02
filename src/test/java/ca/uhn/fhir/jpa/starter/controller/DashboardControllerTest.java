package ca.uhn.fhir.jpa.starter.controller;

import ca.uhn.fhir.jpa.starter.model.ScoreCardIndicatorItem;
import ca.uhn.fhir.jpa.starter.service.HelperService;
import com.google.common.collect.testing.Helpers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class DashboardControllerTest {
	@InjectMocks
	private DashboardController dashboardController;

	@Mock
	private HelperService helperService;

	@Test
	public void testYourControllerMethod() throws IOException {
		// Mock data
		List<ScoreCardIndicatorItem> mockIndicators = readJsonFile("SCORECARD_DEFINITIONS.json", ScoreCardIndicatorItem.class);

		assertEquals(ResponseEntity.ok("mockIndicators"), "mockIndicators");
	}

	private <T> List<T> readJsonFile(String fileName, Class<T> valueType) throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

		if (inputStream == null) {
			throw new FileNotFoundException("File not found: " + fileName);
		}

		try (Reader reader = new InputStreamReader(inputStream)) {
			Gson gson = new Gson();
			return gson.fromJson(reader, TypeToken.getParameterized(List.class, valueType).getType());
		}
	}


}