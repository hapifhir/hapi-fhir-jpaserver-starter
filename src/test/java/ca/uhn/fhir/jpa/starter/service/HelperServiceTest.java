package ca.uhn.fhir.jpa.starter.service;

import android.util.Pair;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.DashboardEnvironmentConfig;
import ca.uhn.fhir.jpa.starter.model.CategoryItem;
import ca.uhn.fhir.jpa.starter.model.PatientIdentifierEntity;
import ca.uhn.fhir.jpa.starter.model.ScoreCardIndicatorItem;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iprd.fhir.utils.KeycloakTemplateHelper;
import com.iprd.report.model.FilterItem;
import com.iprd.report.model.definition.BarChartDefinition;
import com.iprd.report.model.definition.LineChart;
import com.iprd.report.model.definition.PieChartDefinition;
import com.iprd.report.model.definition.TabularItem;
import org.hibernate.SessionFactory;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleType; // Import the correct BundleType
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({NotificationDataSource.class, FhirContext.class, FhirClientAuthenticatorService.class, HelperService.class})
@ContextConfiguration(classes = {AppProperties.class})
class HelperServiceTest {

	@InjectMocks
	private HelperService helperService;

	@Mock
	private KeycloakTemplateHelper keycloakTemplateHelper;

	@Mock
	private TokenManager tokenManager;

	@Mock
	private FhirClientAuthenticatorService fhirClientAuthenticatorService;

	@Mock
	DashboardEnvironmentConfig dashboardEnvironmentConfig;
	@Mock
	private DashboardConfigContainer dashboardConfigContainer;

	@Mock
	private NotificationDataSource notificationDataSource;

	// Mock the SessionFactory
	@Mock
	private SessionFactory sessionFactory;

	@Mock
	private KeycloakBuilder keycloakBuilderMock;

//	@InjectMocks
//	private Keycloak keycloakMock;
//	@InjectMocks
//	private AppProperties appPropertiesMock;

	@Mock
	private AppProperties appProperties;

	@InjectMocks
	FhirClientAuthenticatorService fhirClientAuthenticatorServiceMock;

	@Mock
	private IGenericClient fhirClient;

	@Mock
	private FhirContext fhirContextMock;

	@Mock
	private IQuery<IBaseBundle> queryByUrl;

	@Mock
	private IQuery<IBaseBundle> queryForResource;

	@Mock
	private Bundle bundle;

	@BeforeEach
	void setUp() throws IOException {
		// Initialize the mocks before each test
		MockitoAnnotations.initMocks(this);

	}



	@Test
	void test (){
		Assertions.assertEquals(1, 1);
	}

	@Test
	void getIndicators_Success() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Mock data
		List<ScoreCardIndicatorItem> mockIndicators = readJsonFile("SCORECARD_DEFINITIONS.json", new TypeToken<List<ScoreCardIndicatorItem>>(){}.getType());

		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setScoreCardIndicatorItems(mockIndicators);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);

		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getIndicators("V2");

		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockIndicators, responseEntity.getBody());
	}

	@Test
	void getIndicators_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);

		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getIndicators("NonExistentEnv");

		// Assertions for a file not found scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("Error : ScoreCard Config File Not Found", responseEntity.getBody());
	}

	@Test
	void getCategories_Success() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Mock data
		CategoryItem  mockIndicators = readJsonFile("CATEGORY_DEFINITIONS.json", CategoryItem.class);

		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setCategoryItem(mockIndicators);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);

		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getCategories("V2");

		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockIndicators, responseEntity.getBody());
	}

	@Test
	void getCategories_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);

		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getCategories("NonExistentEnv");

		// Assertions for a file not found scenario
		assertEquals("Error : Category Config File Not Found", responseEntity.getBody());
	}



	@Test
	void getEnvironmentOptions_Success() throws IOException, NoSuchFieldException, IllegalAccessException {
		CategoryItem  mockIndicators = readJsonFile("CATEGORY_DEFINITIONS.json", CategoryItem.class);

		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setCategoryItem(mockIndicators);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);
		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getEnvironmentOptions();
		List<?> environmentOptions = (List<?>) responseEntity.getBody();
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("V2", environmentOptions.get(0));
	}

	@Test
	void getEnvironmentOptions_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);

		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getEnvironmentOptions();

		// Assertions for a file not found scenario
		assertEquals(Collections.emptyList(), responseEntity.getBody());

	}


	@Test
	void getBarChartDefinition_Success() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Mock data
		List<BarChartDefinition> mockIndicators = readJsonFile("BARCHART_DEFINITIONS.json", new TypeToken<List<BarChartDefinition>>(){}.getType());

		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setBarChartDefinitions(mockIndicators);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);

		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getBarChartDefinition("V2");

		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockIndicators, responseEntity.getBody());
	}

	@Test
	void getBarChartDefinition_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);

		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getBarChartDefinition("NonExistentEnv");

		// Assertions for a file not found scenario
		assertEquals("Error :Bar Config File Not Found", responseEntity.getBody());

	}

	@Test
	void getLineChartDefinitions_Success() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Mock data
		List<LineChart> mockLineCharts = readJsonFile("LINECHART_DEFINITIONS.json", new TypeToken<List<LineChart>>(){}.getType());

		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setLineCharts(mockLineCharts);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);

		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getLineChartDefinitions("V2");

		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockLineCharts, responseEntity.getBody());
	}

	@Test
	void getLineChartDefinitions_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);

		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getLineChartDefinitions("NonExistentEnv");

		// Assertions for a file not found scenario
		assertEquals("Error :Line Config File Not Found", responseEntity.getBody());

	}

	@Test
	void getTabularIndicators_Success() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Mock data
		List<TabularItem> mockTabularIndicators = readJsonFile("TABULARCHART_DEFINITIONS.json", new TypeToken<List<TabularItem>>(){}.getType());

		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setTabularItems(mockTabularIndicators);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);

		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getTabularIndicators("V2");

		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockTabularIndicators, responseEntity.getBody());
	}

	@Test
	void getTabularIndicators_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);

		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getTabularIndicators("NonExistentEnv");

		// Assertions for a file not found scenario
		assertEquals("Error : Tabular Config File Not Found", responseEntity.getBody());

	}

	@Test
	void getPieChartDefinition_Success() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Mock data
		List<PieChartDefinition> mockPieChartIndicators = readJsonFile("PIECHART_DEFINITIONS.json", new TypeToken<List<PieChartDefinition>>(){}.getType());

		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setPieChartDefinitions(mockPieChartIndicators);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);

		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getPieChartDefinition("V2");

		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockPieChartIndicators, responseEntity.getBody());
	}

	@Test
	void getPieChartDefinition_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);

		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getPieChartDefinition("NonExistentEnv");

		// Assertions for a file not found scenario
		assertEquals("Error :Pie Chart Config File Not Found", responseEntity.getBody());
	}

	@Test
	void getFilters_Success() throws NoSuchFieldException, IllegalAccessException, IOException {
		// Mock data
		List<FilterItem> mockFilterItemIndicators = readJsonFile("FILTER_DEFINITIONS.json", new TypeToken<List<FilterItem>>(){}.getType());
		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setFilterItems(mockFilterItemIndicators);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);

		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getFilters("V2");

		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockFilterItemIndicators, responseEntity.getBody());
	}

	@Test
	void getFiltersDefinition_FileNotFound() throws  NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);

		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getFilters("NonExistentEnv");

		// Assertions for a file not found scenario
		assertEquals("Error: Config File Not Found", responseEntity.getBody());
	}

	@Test
	public void testGetTableData() throws Exception {

		List<PatientIdentifierEntity> mockPatientList = new ArrayList<>();
		PatientIdentifierEntity mockPatient = PowerMockito.mock(PatientIdentifierEntity.class);
		when(mockPatient.getId()).thenReturn(3L);
		when(mockPatient.getCratedTime()).thenReturn(1682968824863L);
		when(mockPatient.getIdentifierType()).thenReturn("OCL_ID");
		when(mockPatient.getOclGuid()).thenReturn("ac0c2b46-c9a3-48b6-8036-65a1004d7eb0");
		when(mockPatient.getOclVersionId()).thenReturn(String.valueOf(3));
		when(mockPatient.getPatientId()).thenReturn("44ef7b62-0a97-4af1-a2ab-9400fb228d65");
		when(mockPatient.getPatientIdentifier()).thenReturn("K47ZPDFQBZJS");
		when(mockPatient.getStatus()).thenReturn("DELETE");
		when(mockPatient.getUpdatedTime()).thenReturn(1696000069381L);
		when(mockPatient.getOrgId()).thenReturn("e15b899b-8d94-4279-8cb7-3eb90a14279b");

		mockPatientList.add(mockPatient);
		// Stub the behavior of notificationDataSource
		Long lastUpdated = 123L;
		//		// Mock NotificationDataSource
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);

		// Mock the behavior of NotificationDataSource
		when(notificationDataSourceMock.getPatientInfoResourceEntityDataBeyondLastUpdated(lastUpdated))
			.thenReturn(mockPatientList);

		// Replace the actual instance with the mock in NotificationDataSource
		Whitebox.setInternalState(NotificationDataSource.class, notificationDataSourceMock);

		ResponseEntity<?> result = helperService.getTableData(lastUpdated);

		// Assertions
		assertEquals(HttpStatus.OK, result.getStatusCode());

		List<PatientIdentifierEntity> resultList = (List<PatientIdentifierEntity>) result.getBody();
		assertEquals(1, resultList.size());

		// Additional assertions based on the expected values
		PatientIdentifierEntity resultPatient = resultList.get(0);
		assertEquals(3L, resultPatient.getId());
		assertEquals(1682968824863L, resultPatient.getCratedTime());
		assertEquals("OCL_ID", resultPatient.getIdentifierType());
	}
	@Test
	void testConvertClobToString() throws IOException, SQLException {
		// Mocking the Clob
		Clob mockClob = mock(Clob.class);
		Mockito.when(mockClob.getCharacterStream()).thenReturn(new StringReader("Test CLOB Data"));

		// Call the method with the mock Clob
		String result = helperService.convertClobToString(mockClob);

		// Verify the result
		assertEquals("Test CLOB Data", result);
	}

	@Test
	void testGetFhirSearchListByFilters() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Arrange
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.put("filter1Id", "age");
		filters.put("filter1Value", "age-2");

		// Mock data
		List<FilterItem> mockFilterItemIndicators = readJsonFile("FILTER_DEFINITIONS.json", new TypeToken<List<FilterItem>>(){}.getType());
		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();

		// Assign values to its fields
		container.setFilterItems(mockFilterItemIndicators);

		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);


		// Act
		List<String> fhirSearchList = helperService.getFhirSearchListByFilters(filters, "V2");
		assertEquals("subject:Patient.birthdate=geSTART(19)&subject:Patient.birthdate=leEND(15)", fhirSearchList.get(0));

	}

	@Test
	void testGenerateBatchBundle() {
		// Act
		Bundle resultBundle = helperService.generateBatchBundle("https://example.com/api/resource");

		// Assert
		assertNotNull(resultBundle, "Generated bundle should not be null");
		assertEquals("batch-bundle",resultBundle.getId());
		assertEquals("Bundle type should be BATCH", BundleType.BATCH, resultBundle.getType());

		// Check the entry in the bundle
		assertFalse(resultBundle.getEntry().isEmpty(), "Bundle should contain at least one entry");
		Bundle.BundleEntryComponent entryComponent = resultBundle.getEntryFirstRep();

		// Check the entry request component
		assertNotNull(entryComponent.getRequest(), "Entry request component should not be null");
		Bundle.BundleEntryRequestComponent requestComponent = entryComponent.getRequest();
		assertEquals("Request method should be GET", Bundle.HTTPVerb.GET, requestComponent.getMethod());
		assertEquals("https://example.com/api/resource", requestComponent.getUrl());
	}


private Bundle createMockBundle() {
	Bundle bundle = new Bundle();
	Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
	entry.setResource(new Organization().setId("6c20db8d-0a28-4a43-9f55-dd0ac0c4d625"));
	bundle.addEntry(entry);
	return bundle;
}

	private Bundle createPractitionerRoleMockBundle() {
		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();

		// Create a PractitionerRole resource with an "organization" reference
		PractitionerRole practitionerRole = new PractitionerRole();
		practitionerRole.setId("f06e1458-c9e4-48fd-99d6-a254c7d5c4d3");

		Reference organizationReference = new Reference();
		organizationReference.setReference("Organization/6c20db8d-0a28-4a43-9f55-dd0ac0c4d625");
		practitionerRole.setOrganization(organizationReference);

		entry.setResource(practitionerRole);
		bundle.addEntry(entry);

		return bundle;
	}

	@Test
	public void testGetOrganizationsPartOf() {
		List<String> idsList = new ArrayList<>();
		String url = "http://localhost:8085/fhir/Organization/12345";

		// Create a mock for your class
		HelperService helperServiceMock = mock(HelperService.class);

		// Mock the behavior of fhirClientCall to return the desired value
		when(helperServiceMock.performFhirSearch(any(),any(),anyString())).thenReturn(createMockBundle());

		// Mock the behavior of getOrganizationsPartOf to call the real method
		doCallRealMethod().when(helperServiceMock).getOrganizationsPartOf(anyList(), anyString());

		// Now, when you call getOrganizationsPartOf, it will call the actual method,
		// and the actual method will call fhirClientCall, which is mocked.
		helperServiceMock.getOrganizationsPartOf(idsList, url);

		// Assert or verify based on your requirements
		// For example, verify that the idsList is populated correctly
		assertEquals(1, idsList.size());
		assertEquals("6c20db8d-0a28-4a43-9f55-dd0ac0c4d625", idsList.get(0));
	}


	@Test
	public void testGetFacilityIdsAndOrgIdToChildrenMapPair() {

		HelperService helperService = mock(HelperService.class);
		PowerMockito.doCallRealMethod().when(helperService).getFacilityIdsAndOrgIdToChildrenMapPair(anyString());
		Mockito.when(helperService.performFhirSearch(any(),any(),anyString())).thenReturn(generateOrganizationBundle());

		Pair<List<String>, LinkedHashMap<String, List<String>>> result  = helperService.getFacilityIdsAndOrgIdToChildrenMapPair("00168333-2937-4321-9f6a-10e86ede55c5");
		assertEquals(result.first.size(),1);
		assertEquals(result.second.size(),1);
		assertEquals(result.first.get(0),"00168333-2937-4321-9f6a-10e86ede55c5");
	}

	@Test
	public void testFetchIdsAndOrgIdToChildrenMapPair() {

		HelperService helperServiceMock = mock(HelperService.class);

		helperServiceMock.mapOfIdsAndOrgIdToChildrenMapPair = new LinkedHashMap<String, Pair<List<String>, LinkedHashMap<String, List<String>>>>();

		// Use doCallRealMethod to invoke the real method when getOrganizationsPartOf is called
		PowerMockito.doCallRealMethod().when(helperServiceMock).fetchIdsAndOrgIdToChildrenMapPair(anyString());

		// Stub the behavior of getFacilityIdsAndOrgIdToChildrenMapPair
		Mockito.when(helperServiceMock.getFacilityIdsAndOrgIdToChildrenMapPair(anyString())).thenReturn(createMockResult());

		// Log or debug statements to trace the flow
		Pair<List<String>, LinkedHashMap<String, List<String>>> result = helperServiceMock.fetchIdsAndOrgIdToChildrenMapPair("00168333-2937-4321-9f6a-10e86ede55c5");
		assertEquals(result.first.size(), 2);
		assertEquals(result.second.size(), 2);
		assertEquals(result.first.get(0), "FacilityOrgId1");
		assertEquals(result.second.get("ChildOrgId1").get(1), "GrandchildOrgId2");
	}

	@Test
	public void testGetOrganizationIdByPractitionerRoleId(){
		// Create a mock for your class
		HelperService helperServiceMock = mock(HelperService.class);

		when(helperServiceMock.performFhirSearch(
			any(),
			any(), // or provide a real map with data
			any()
		)).thenReturn(createPractitionerRoleMockBundle());

		// Mock the behavior of getOrganizationsPartOf to call the real method
		doCallRealMethod().when(helperServiceMock).getOrganizationIdByPractitionerRoleId(anyString());

		// Now, when you call getOrganizationsPartOf, it will call the actual method,
		// and the actual method will call fhirClientCall, which is mocked.
		String result = helperServiceMock.getOrganizationIdByPractitionerRoleId("f06e1458-c9e4-48fd-99d6-a254c7d5c4d3");

		// Assert or verify based on your requirements
		// For example, verify that the idsList is populated correctly
		assertEquals(result, "6c20db8d-0a28-4a43-9f55-dd0ac0c4d625");
	}

	@Test
	public void testGetOrganizationResourceByPractitionerRoleId(){
		// Create a mock for your class
		HelperService helperServiceMock = mock(HelperService.class);

		when(helperServiceMock.getOrganizationIdByPractitionerRoleId(any()
		)).thenReturn("6c20db8d-0a28-4a43-9f55-dd0ac0c4d625");

		when(helperServiceMock.performFhirSearch(
			any(),
			any(), // or provide a real map with data
			any()
		)).thenReturn(createMockBundle());


		// Mock the behavior of getOrganizationsPartOf to call the real method
		doCallRealMethod().when(helperServiceMock).getOrganizationResourceByPractitionerRoleId(anyString());

		// Now, when you call getOrganizationsPartOf, it will call the actual method,
		// and the actual method will call fhirClientCall, which is mocked.
		Organization result = helperServiceMock.getOrganizationResourceByPractitionerRoleId("f06e1458-c9e4-48fd-99d6-a254c7d5c4d3");

		// Assert or verify based on your requirements
		// For example, verify that the idsList is populated correctly
		assertEquals(result.getId(), "6c20db8d-0a28-4a43-9f55-dd0ac0c4d625");
	}


	private Pair<List<String>, LinkedHashMap<String, List<String>>> createMockResult() {
		// Create a sample data for testing
		List<String> facilityOrgIdList = new ArrayList<>();
		facilityOrgIdList.add("FacilityOrgId1");
		facilityOrgIdList.add("FacilityOrgId2");

		LinkedHashMap<String, List<String>> mapOfIdToChildren = new LinkedHashMap<>();
		mapOfIdToChildren.put("ParentOrgId", new ArrayList<>(Arrays.asList("ChildOrgId1", "ChildOrgId2")));
		mapOfIdToChildren.put("ChildOrgId1", new ArrayList<>(Arrays.asList("GrandchildOrgId1", "GrandchildOrgId2")));

		return new Pair<>(facilityOrgIdList, mapOfIdToChildren);
	}


	public Bundle generateOrganizationBundle() {
		// Create an instance of the organization resource
		Organization organization = new Organization();
		organization.setId("00168333-2937-4321-9f6a-10e86ede55c5");

		// Set meta information
		organization.getMeta().addTag()
			.setSystem("https://www.iprdgroup.com/ValueSet/OrganizationType/tags")
			.setCode("facility")
			.setDisplay("FACILITY");

		// Create a bundle and add the organization resource to it
		Bundle bundle = new Bundle();
		bundle.addEntry().setResource(organization).setFullUrl(organization.getId());

		return bundle;
	}

	private <T> T readJsonFile(String fileName, Type type) throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

		if (inputStream == null) {
			throw new FileNotFoundException("File not found: " + fileName);
		}

		try (Reader reader = new InputStreamReader(inputStream)) {
			Gson gson = new Gson();
			return gson.fromJson(reader, type);
		}
	}

}