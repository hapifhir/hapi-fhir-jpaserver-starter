package ca.uhn.fhir.jpa.starter.service;

import android.util.Pair;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.AsyncConfiguration;
import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.anonymization.ISANONYMIZED;
import ca.uhn.fhir.jpa.starter.model.ApiAsyncTaskEntity;
import ca.uhn.fhir.jpa.starter.model.CategoryItem;
import ca.uhn.fhir.jpa.starter.model.PatientIdentifierEntity;
import ca.uhn.fhir.jpa.starter.model.ScoreCardIndicatorItem;
import ca.uhn.fhir.jpa.starter.model.OrgHierarchy;
import ca.uhn.fhir.jpa.starter.model.OrgIndicatorAverageResult;
import ca.uhn.fhir.jpa.starter.model.ScoreCardResponseItem;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iprd.fhir.utils.KeycloakTemplateHelper;
import com.iprd.fhir.utils.Utils;
import com.iprd.report.DataResult;
import com.iprd.report.model.FilterItem;
import com.iprd.report.model.definition.BarChartDefinition;
import com.iprd.report.model.definition.LineChart;
import com.iprd.report.model.definition.PieChartDefinition;
import com.iprd.report.model.definition.TabularItem;
import com.iprd.report.model.definition.PieChartCategoryDefinition;
import com.iprd.report.model.definition.LineChartItemDefinition;
import com.iprd.report.model.definition.BarChartItemDefinition;
import com.iprd.report.model.definition.FhirPathTransformation;
import com.iprd.report.model.definition.BarComponent;
import com.iprd.report.model.definition.ANCDailySummaryConfig;
import com.iprd.report.model.data.BarChartItemDataCollection;
import com.iprd.report.model.data.LineChartItemCollection;
import com.iprd.report.model.definition.IndicatorItem;
import com.iprd.report.model.data.PieChartItemDataCollection;
import com.iprd.report.model.data.ScoreCardItem;
import org.hibernate.SessionFactory;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.Date;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Arrays;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({NotificationDataSource.class, FhirContext.class, FhirClientAuthenticatorService.class, HelperService.class,Utils.class})
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
	private NotificationDataSource notificationDataSource;

	// Mock the SessionFactory
	@Mock
	private SessionFactory sessionFactory;

	@Mock
	private KeycloakBuilder keycloakBuilderMock;

	@Mock
	private ThreadPoolTaskExecutor asyncExecutorMock;

	@Mock
	private AsyncConfiguration asyncConfMock;

	@Mock
	CachingService cachingService;

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
	void getIndicators_Success() throws IOException, NoSuchFieldException, IllegalAccessException {
		// Mock data
		List<ScoreCardIndicatorItem> mockIndicators = readJsonFile("SCORECARD_DEFINITIONS.json", new TypeToken<List<ScoreCardIndicatorItem>>(){}.getType());
		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();
		// Assign values to its fields
		container.setScoreCardIndicatorItems(mockIndicators);
		setDashboardEnvToConfigMap(container);
		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getIndicators("V2");
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockIndicators, responseEntity.getBody());
	}

	@Test
	void getIndicators_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		setEmptyConfiguration();
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
		setDashboardEnvToConfigMap(container);
		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getCategories("V2");
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockIndicators, responseEntity.getBody());
	}

	@Test
	void getCategories_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		setEmptyConfiguration();
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
		setDashboardEnvToConfigMap(container);
		ResponseEntity<?> responseEntity = helperService.getEnvironmentOptions();
		List<?> environmentOptions = (List<?>) responseEntity.getBody();
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("V2", environmentOptions.get(0));
	}

	@Test
	void getEnvironmentOptions_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		setEmptyConfiguration();
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
		setDashboardEnvToConfigMap(container);
		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getBarChartDefinition("V2");
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockIndicators, responseEntity.getBody());
	}

	@Test
	void getBarChartDefinition_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		setEmptyConfiguration();
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
		setDashboardEnvToConfigMap(container);
		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getLineChartDefinitions("V2");
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockLineCharts, responseEntity.getBody());
	}

	@Test
	void getLineChartDefinitions_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		setEmptyConfiguration();
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
		setDashboardEnvToConfigMap(container);
		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getTabularIndicators("V2");
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockTabularIndicators, responseEntity.getBody());
	}

	@Test
	void getTabularIndicators_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		setEmptyConfiguration();
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
		setDashboardEnvToConfigMap(container);
		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getPieChartDefinition("V2");
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockPieChartIndicators, responseEntity.getBody());
	}

	@Test
	void getPieChartDefinition_FileNotFound() throws IOException, NoSuchFieldException, IllegalAccessException {
		setEmptyConfiguration();
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
		setDashboardEnvToConfigMap(container);
		// Call the method and assert the response
		ResponseEntity<?> responseEntity = helperService.getFilters("V2");
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(mockFilterItemIndicators, responseEntity.getBody());
	}

	@Test
	void getFiltersDefinition_FileNotFound() throws  NoSuchFieldException, IllegalAccessException {
		setEmptyConfiguration();
		// Call the method and assert the response for a file not found scenario
		ResponseEntity<?> responseEntity = helperService.getFilters("NonExistentEnv");
		// Assertions for a file not found scenario
		assertEquals("Error: Config File Not Found", responseEntity.getBody());
	}

	@Test
	public void testGetTableData() throws Exception {
		List<PatientIdentifierEntity> mockPatientList = getMockPatientList();
		// Stub the behavior of notificationDataSource
		Long lastUpdated = 123L;
		// Mock NotificationDataSource
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
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.put("filter1Id", "age");
		filters.put("filter1Value", "age-2");
		// Mock data
		List<FilterItem> mockFilterItemIndicators = readJsonFile("FILTER_DEFINITIONS.json", new TypeToken<List<FilterItem>>(){}.getType());
		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();
		// Assign values to its fields
		container.setFilterItems(mockFilterItemIndicators);
		setDashboardEnvToConfigMap(container);
		List<String> fhirSearchList = helperService.getFhirSearchListByFilters(filters, "V2");
		assertEquals("subject:Patient.birthdate=geSTART(19)&subject:Patient.birthdate=leEND(15)", fhirSearchList.get(0));
	}

	@Test
	void testGenerateBatchBundle() {
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
		assertEquals(result.first.size(), 1);
		assertEquals(result.second.size(), 2);
		assertEquals(result.first.get(0), "e15b899b-8d94-4279-8cb7-3eb90a14279b");
		assertEquals(result.second.get("ChildOrgId1").get(1), "e15b899b-8d94-4279-8cb7-3eb90a14279b");
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

	@Test
	public void testGetCategoriesFromAncDailySummaryConfig() throws IOException, NoSuchFieldException, IllegalAccessException {
		List<ANCDailySummaryConfig> mockIndicators = readJsonFile("DAILY_SUMMARY_DEFINITIONS.json", new TypeToken<List<ANCDailySummaryConfig>>(){}.getType());
		// Create an instance of DashboardConfigContainer
		DashboardConfigContainer container = new DashboardConfigContainer();
		// Assign values to its fields
		container.setAncDailySummaryConfig(mockIndicators);
		setDashboardEnvToConfigMap(container);
		List<String> output = helperService.getCategoriesFromAncDailySummaryConfig("V2");
		assertEquals(output.size(), 12);
		assertEquals(output.get(0), "patient-bio-data");
	}

	@Test
	void testSaveInAsyncTable() throws SQLException {
		List<Map<String, String>> dailyResult = new ArrayList<>();
		Map<String, String> dailyResultMap = new HashMap<>();
		dailyResultMap.put("Name", "KARTHIK233 OFFLINE");
		dailyResultMap.put("Card Number", "PCM/00000233");
		dailyResultMap.put("Date", "01-11-2023");
		dailyResultMap.put("Date of Birth", "23-03-1993");
		dailyResult.add(dailyResultMap);
		DataResult dataResult = new DataResult(
			"patient-bio-data",
			"SomeRandomByteArray".getBytes(),
			dailyResult
		);
		List<Map<String, String>> emptyList = new ArrayList<>();
		// Create an instance of ApiAsyncTaskEntity
		ApiAsyncTaskEntity apiAsyncTaskEntity = new ApiAsyncTaskEntity(
			"e15b899b-8d94-4279-8cb7-3eb90a14279b2023-11-012023-11-02patient-bio-dataV2",
			"PROCESSING",
			createClob(""),
			createClob(emptyList.toString()),
			Date.valueOf(LocalDate.now()),
			ISANONYMIZED.NO.name()
		);
		String dailyResultJsonString = new Gson().toJson(dailyResult);
		ArrayList<ApiAsyncTaskEntity> apiAsyncTaskEntityList = new ArrayList<>();
		apiAsyncTaskEntityList.add(apiAsyncTaskEntity);
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		// Mock the behavior of NotificationDataSource
		when(notificationDataSourceMock.fetchStatus(anyString()))
			.thenReturn(apiAsyncTaskEntityList);
		doNothing().when(notificationDataSourceMock).update(any());
		// Replace the actual instance with the mock in NotificationDataSource
		Whitebox.setInternalState(NotificationDataSource.class, notificationDataSourceMock);
		helperService.saveInAsyncTable(dataResult,"68455");
		verify(notificationDataSourceMock).update(any(ApiAsyncTaskEntity.class));
		// Verify the update method was called with the captured argument
		ArgumentCaptor<ApiAsyncTaskEntity> argumentCaptor = ArgumentCaptor.forClass(ApiAsyncTaskEntity.class);
		verify(notificationDataSourceMock).update(argumentCaptor.capture());
		// Get the captured value
		ApiAsyncTaskEntity capturedAsyncTaskEntity = argumentCaptor.getValue();
		// Now you can assert or inspect the capturedAsyncTaskEntity as needed
		assertEquals("e15b899b-8d94-4279-8cb7-3eb90a14279b2023-11-012023-11-02patient-bio-dataV2", capturedAsyncTaskEntity.getUuid());
		assertEquals("COMPLETED", capturedAsyncTaskEntity.getStatus());
		assertEquals(dailyResultJsonString, clobToString(capturedAsyncTaskEntity.getDailyResult()));
	}

	@Test
	public void testSaveQueryResult() throws Exception {
		// Set up test data
		String organizationId = "e15b899b-8d94-4279-8cb7-3eb90a14279b";
		String startDate = "2023-11-01";
		String endDate = "2023-11-01";
		LinkedHashMap<String, String> filters = new LinkedHashMap<>(); // Add your filters
		List<String> hashCodeList = Collections.singletonList("e15b899b-8d94-4279-8cb7-3eb90a14279b2023-11-012023-11-01antenatalV2");// Add your hashcodes
		String env = "V2";
		List<ANCDailySummaryConfig> ancDailySummaryConfig = new ArrayList<>(); // Add your config
		List<Map<String, String>> dailyResult = new ArrayList<>();
		Map<String, String> dailyResultMap = new HashMap<>();
		dailyResultMap.put("LMP", "08-07-2023");
		dailyResultMap.put("Gestational Age (Weeks)", "16");
		dailyResult.add(dailyResultMap);
		List<DataResult> dataResultList = new ArrayList<>();
		dataResultList.add(
			new DataResult(
				"antenatal",
				"SomeRandomByteArray".getBytes(),
				dailyResult
			)
		);
		HelperService helperServiceMock = mock(HelperService.class);
		when(helperServiceMock.getFhirSearchListByFilters(any(),anyString()
		)).thenReturn(Collections.emptyList());
		doReturn((List<?>) dataResultList)
			.when(helperServiceMock)
			.getReportGen(anyString(), anyString(), anyString(), anyString(), anyList(), anyList());
		doNothing().when(helperServiceMock).saveInAsyncTable(any(),anyString());
		// Mock the behavior of getOrganizationsPartOf to call the real method
		doCallRealMethod().when(helperServiceMock).saveQueryResult(organizationId, startDate, endDate, filters, hashCodeList, env, ancDailySummaryConfig);
		helperServiceMock.saveQueryResult(organizationId, startDate, endDate, filters, hashCodeList, env, ancDailySummaryConfig);
		verify(helperServiceMock).saveInAsyncTable(any(DataResult.class),anyString());
		// Verify the update method was called with the captured argument
		ArgumentCaptor<DataResult> argumentCaptor = ArgumentCaptor.forClass(DataResult.class);
		verify(helperServiceMock).saveInAsyncTable(argumentCaptor.capture(), anyString());
		// Get the captured value
		DataResult  capturedAsyncTaskEntity = argumentCaptor.getValue();
		assertEquals("antenatal", capturedAsyncTaskEntity.getCategoryId());
		assertEquals("08-07-2023", capturedAsyncTaskEntity.getDailyResult().get(0).get("LMP"));
	}

	@Test
	void testSaveQueryResultAndHandleException() throws FileNotFoundException, InterruptedException {
		// Prepare test data
		String organizationId = "yourOrgId";
		String startDate = "2024-01-04";
		String endDate = "2024-01-05";
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		List<String> hashcodes = Collections.singletonList("hashcode1");
		String env = "testEnv";
		List<ANCDailySummaryConfig> ancDailySummaryConfig = Collections.singletonList(
			new ANCDailySummaryConfig("categoryId", Collections.emptyList(), Collections.emptyList())
		);
		CountDownLatch latch = new CountDownLatch(1);
		HelperService helperServiceMoc = mock(HelperService.class);
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.initialize();
		when(helperServiceMoc.getAsyncExecutor(
		)).thenReturn(executor);
		doNothing().when(helperServiceMoc).saveQueryResult(anyString(),anyString(),anyString(),any(),anyList(),anyString(),anyList());
		doAnswer(invocation -> {
			latch.countDown();
			return null;
		}).when(helperServiceMoc).saveQueryResult(anyString(), anyString(), anyString(), any(), anyList(), anyString(), anyList());
		// Call the method
		doCallRealMethod().when(helperServiceMoc).saveQueryResultAndHandleException(organizationId, startDate, endDate, filters, hashcodes, env, ancDailySummaryConfig);
		helperServiceMoc.saveQueryResultAndHandleException(organizationId, startDate, endDate, filters, hashcodes, env, ancDailySummaryConfig);
		// Wait for the asynchronous operation to complete
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		// Verify that the saveQueryResult method was called
		verify(helperServiceMoc, times(1)).saveQueryResult(
			eq(organizationId), eq(startDate), eq(endDate), eq(filters), eq(hashcodes), eq(env), eq(ancDailySummaryConfig)
		);
		executor.shutdown();
	}

	@Test
	void testProcessCategories() throws FileNotFoundException {
		String organizationId = "e15b899b-8d94-4279-8cb7-3eb90a14279b";
		String startDate = "2023-11-01";
		String endDate = "2023-11-01";
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.put("filter1Id", "age");
		filters.put("filter1Value", "age-2");
		List<String> hashcodes = Collections.singletonList("hashcode1");
		String env = "V2";
		List<ANCDailySummaryConfig> ancDailySummaryConfig = Collections.singletonList(
			new ANCDailySummaryConfig("categoryId", Collections.emptyList(), Collections.emptyList())
		);
		HelperService helperServiceMock = mock(HelperService.class);
		when(helperServiceMock.getANCDailySummaryConfigFromFile(anyString()
		)).thenReturn(ancDailySummaryConfig);
		when(helperServiceMock.getCategoriesFromAncDailySummaryConfig(anyString()
		)).thenReturn(Collections.singletonList("antenatal"));
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		when(notificationDataSourceMock.fetchStatus(anyString()))
			.thenReturn(new ArrayList<ApiAsyncTaskEntity>());
		helperServiceMock.datasource = notificationDataSourceMock;
		doNothing().when(notificationDataSourceMock).insert(any());
		doNothing().when(helperServiceMock).saveQueryResultAndHandleException(anyString(),anyString(),anyString(),any(),anyList(),anyString(),anyList());
		doCallRealMethod().when(helperServiceMock).processCategories(organizationId,startDate,endDate,env,filters,true);
		Map<String,String> categoryWithHashCodes =  helperServiceMock.processCategories(organizationId,startDate,endDate,env,filters,true);
		ArgumentCaptor<List<ANCDailySummaryConfig>> argumentCaptor = ArgumentCaptor.forClass(List.class);
		verify(helperServiceMock).saveQueryResultAndHandleException(anyString(),anyString(),anyString(),any(),anyList(),anyString(),argumentCaptor.capture());
		List<ANCDailySummaryConfig> capturedList = argumentCaptor.getValue();
		assertEquals(ancDailySummaryConfig,capturedList);
		assertEquals(categoryWithHashCodes.get("antenatal"),"e15b899b-8d94-4279-8cb7-3eb90a14279b2023-11-012023-11-01antenatalV2age-2");
	}

//	@Test
//	void testProcessCategoriesForScheduledTasks() throws FileNotFoundException, SQLException {
//		String organizationId = "e15b899b-8d94-4279-8cb7-3eb90a14279b";
//		String startDate = "2023-11-01";
//		String endDate = "2024-01-04";
//		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
//		filters.put("filter1Id", "age");
//		filters.put("filter1Value", "age-2");
//		String env = "V2";
//		List<ANCDailySummaryConfig> ancDailySummaryConfig = Collections.singletonList(
//			new ANCDailySummaryConfig("categoryId", Collections.emptyList(), Collections.emptyList())
//		);
//		String sampleLongText = "This is a sample long text for the summary result. It can be a paragraph or more, containing multiple lines and details.";
//		// Create a Map for dailyResult
//		Map<String, String> dailyResultMap = new HashMap<>();
//		dailyResultMap.put("Name", "KARTHIK233 OFFLINE");
//		dailyResultMap.put("Card Number", "PCM/00000233");
//		dailyResultMap.put("Date", "01-11-2023");
//		dailyResultMap.put("Date of Birth", "23-03-1993");
//		// Create a List for dailyResult
//		List<Map<String, String>> dailyResultList = new ArrayList<>();
//		dailyResultList.add(dailyResultMap);
//		ArrayList<ApiAsyncTaskEntity> apiAsyncTaskEntityList = new ArrayList<>();
//		// Create an instance of ApiAsyncTaskEntity
//		ApiAsyncTaskEntity apiAsyncTaskEntity = new ApiAsyncTaskEntity(
//			"e15b899b-8d94-4279-8cb7-3eb90a14279b2023-11-012024-01-04patient-bio-dataV2age-2",
//			"COMPLETED",
//			createClob(sampleLongText),
//			createClob(dailyResultList.toString()),
//			Date.valueOf(LocalDate.of(2024, 4, 1))
//		);
//		apiAsyncTaskEntityList.add(apiAsyncTaskEntity);
//		HelperService helperServiceMock = mock(HelperService.class);
//		when(helperServiceMock.getANCDailySummaryConfigFromFile(anyString()
//		)).thenReturn(ancDailySummaryConfig);
//		when(helperServiceMock.getCategoriesFromAncDailySummaryConfig(anyString()
//		)).thenReturn(Collections.singletonList("patient-bio-data"));
//		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
//		when(notificationDataSourceMock.fetchStatus(anyString()))
//			.thenReturn(apiAsyncTaskEntityList);
//		when(notificationDataSourceMock.fetchApiAsyncTaskEntityList(anyList()))
//			.thenReturn(apiAsyncTaskEntityList);
//		helperServiceMock.datasource = notificationDataSourceMock;
//		doNothing().when(notificationDataSourceMock).updateObjects(any());
//		doNothing().when(helperServiceMock).saveQueryResultAndHandleException(anyString(),anyString(),anyString(),any(),anyList(),anyString(),anyList());
//		doCallRealMethod().when(helperServiceMock).processCategories(organizationId,startDate,endDate,env,filters,true);
//		Map<String,String> categoryWithHashCodes =  helperServiceMock.processCategories(organizationId,startDate,endDate,env,filters,true);
//		ArgumentCaptor<ArrayList<ApiAsyncTaskEntity>> argumentCaptorForAsyncTaskEntity = ArgumentCaptor.forClass(ArrayList.class);
//		verify(helperServiceMock.datasource).updateObjects(argumentCaptorForAsyncTaskEntity.capture());
//		ArrayList<ApiAsyncTaskEntity> capturedListForAsyncTaskEntity = argumentCaptorForAsyncTaskEntity.getValue();
//		assertEquals(capturedListForAsyncTaskEntity.get(0).getStatus(),"PROCESSING");
//		ArgumentCaptor<List<ANCDailySummaryConfig>> argumentCaptor = ArgumentCaptor.forClass(List.class);
//		verify(helperServiceMock).saveQueryResultAndHandleException(anyString(),anyString(),anyString(),any(),anyList(),anyString(),argumentCaptor.capture());
//		List<ANCDailySummaryConfig> capturedList = argumentCaptor.getValue();
//		assertEquals(ancDailySummaryConfig,capturedList);
//		assertEquals(categoryWithHashCodes.get("patient-bio-data"),"e15b899b-8d94-4279-8cb7-3eb90a14279b2023-11-012024-01-04patient-bio-dataV2age-2");
//	}
  
	@Test
	void testGetCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection() throws NoSuchMethodException {
		Date start = Date.valueOf(LocalDate.of(2024,01,05)); // replace with your desired start date
		Date end = Date.valueOf(LocalDate.of(2024,01,05));   // replace with your desired end date
		String indicator = "yourIndicator";
		List<String> orgIds = Arrays.asList("orgId1", "orgId2");
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(any(),any(),anyString(),anyList())).thenReturn(Double.valueOf("2.0"));
		Whitebox.setInternalState(NotificationDataSource.class, notificationDataSourceMock);
		Double output = helperService.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(null,start,end,indicator,orgIds);
		assertEquals(output,Double.valueOf(2.0));
	}

	@Test
	void testGetCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflectionMock() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// Set up your test data
		String transform = "getCacheValueSumByDateRangeIndicatorAndMultipleOrgId";
		Date start = Date.valueOf(LocalDate.of(2024,01,05)); // replace with your desired start date
		Date end = Date.valueOf(LocalDate.of(2024,01,05));   // replace with your desired end date
		String indicator = "yourIndicator";
		List<String> orgIds = new ArrayList<>(); // Replace with your orgIds
		// Set up the expected result when transform is not null
		Double expectedResult = 42.0;
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(any(),any(),anyString(),anyList())).thenReturn(Double.valueOf("42.0"));
		Whitebox.setInternalState(NotificationDataSource.class, notificationDataSourceMock);
		// Call the method to test
		Double result = helperService.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(transform, start, end, indicator, orgIds);
		// Verify that the private method was called
		verify(notificationDataSourceMock, times(1)).getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(start, end, indicator, orgIds);
		// Verify the result
		assertEquals(expectedResult, result);
	}

	@Test
	 void testGetBarChartData() {
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		LinkedHashMap<String, String> testParameters = initializeTestParameters();
		List<BarChartDefinition> barChartDefinitionList = getBarChartDefinitionList();
		HelperService helperServiceMock = mock(HelperService.class);
		when(helperServiceMock.getBarChartItemListFromFile(anyString()
		)).thenReturn(barChartDefinitionList);
		when(helperServiceMock.fetchIdsAndOrgIdToChildrenMapPair(anyString()
		)).thenReturn(createMockResult());
		when(helperServiceMock.getFhirSearchListByFilters(any(),anyString()
		)).thenReturn(Collections.emptyList());
		when(helperServiceMock.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(anyString(),any(),any(),anyString(),anyList()
		)).thenReturn(Double.valueOf("2.0"));
		doNothing().when(helperServiceMock).performCachingIfNotPresentForBarChart(anyList(),anyList(),any(),any(),anyList());
		doCallRealMethod().when(helperServiceMock).getBarChartData(anyString(),anyString(),any(),anyString(),anyString(),false);
		ResponseEntity<?> output = helperServiceMock.getBarChartData(testParameters.get("startDate"),testParameters.get("endDate"),filters,testParameters.get("env"),testParameters.get("lga"),false);
		List<BarChartItemDataCollection> barChartItems =  (List<BarChartItemDataCollection>) output.getBody();
		// Assertions for a successful scenario
		assertEquals(HttpStatus.OK, output.getStatusCode());
		assertEquals("2.0", barChartItems.get(0).getData().get(0).getBarComponentData().get(0).getValue());
	}
	@Test
	void testGetLineChartByPractitionerRoleId() {
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		LinkedHashMap<String, String> testParameters = initializeTestParameters();
		List<LineChart> lineCharts =getLineCharts();
		HelperService helperServiceMock = mock(HelperService.class);
		when(helperServiceMock.getLineChartDefinitionsItemListFromFile(anyString()
		)).thenReturn(lineCharts);
		when(helperServiceMock.fetchIdsAndOrgIdToChildrenMapPair(anyString()
		)).thenReturn(createMockResult());
		when(helperServiceMock.getFhirSearchListByFilters(any(),anyString()
		)).thenReturn(Collections.emptyList());
		when(helperServiceMock.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(anyString(),any(),any(),anyString(),anyList()
		)).thenReturn(Double.valueOf("3.0"));
		doNothing().when(helperServiceMock).performCachingForLineChartIfNotPresent(anyList(),anyList(),any(),any(),anyList());
		doCallRealMethod().when(helperServiceMock).getLineChartByPractitionerRoleId(anyString(),anyString(),any(),any(),anyString(),anyString(),false);
		ResponseEntity<?> output = helperServiceMock.getLineChartByPractitionerRoleId(testParameters.get("startDate"),testParameters.get("endDate"), ReportType.valueOf("daily"),filters,testParameters.get("env"),testParameters.get("lga"), false);
		List<LineChartItemCollection> lineChartItems =  (List<LineChartItemCollection>) output.getBody();
		assertEquals(HttpStatus.OK, output.getStatusCode());
		assert lineChartItems != null;
		assertEquals("3.0", lineChartItems.get(0).getValue().get(0).getValue());
		when(helperServiceMock.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(anyString(),any(),any(),anyString(),anyList()
		)).thenReturn(Double.valueOf("4.0"));
		ResponseEntity<?> weeklyOutput = helperServiceMock.getLineChartByPractitionerRoleId("2024-01-01","2024-01-07", ReportType.valueOf("weekly"),filters,testParameters.get("env"),testParameters.get("lga"),false);
		List<LineChartItemCollection> lineChartItemsWeekly =  (List<LineChartItemCollection>) weeklyOutput.getBody();
		assertEquals(HttpStatus.OK, weeklyOutput.getStatusCode());
		assert lineChartItemsWeekly != null;
		assertEquals("4.0", lineChartItemsWeekly.get(0).getValue().get(0).getValue());
		when(helperServiceMock.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(anyString(),any(),any(),anyString(),anyList()
		)).thenReturn(Double.valueOf("6.0"));
		ResponseEntity<?> monthlyOutput = helperServiceMock.getLineChartByPractitionerRoleId("2024-01-01","2024-01-07", ReportType.valueOf("monthly"),filters,testParameters.get("env"),testParameters.get("lga"),false);
		List<LineChartItemCollection> lineChartItemsMonthly =  (List<LineChartItemCollection>) monthlyOutput.getBody();
		assertEquals(HttpStatus.OK, monthlyOutput.getStatusCode());
		assert lineChartItemsMonthly != null;
		assertEquals("6.0", lineChartItemsMonthly.get(0).getValue().get(0).getValue());
		when(helperServiceMock.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(anyString(),any(),any(),anyString(),anyList()
		)).thenReturn(Double.valueOf("26.0"));
		ResponseEntity<?> quarterlyOutput = helperServiceMock.getLineChartByPractitionerRoleId("2024-01-01","2024-01-07", ReportType.valueOf("quarterly"),filters,testParameters.get("env"),testParameters.get("lga"),false);
		List<LineChartItemCollection> lineChartItemsQuarterly =  (List<LineChartItemCollection>) quarterlyOutput.getBody();
		assertEquals(HttpStatus.OK, quarterlyOutput.getStatusCode());
		assert lineChartItemsQuarterly != null;
		assertEquals("26.0", lineChartItemsQuarterly.get(0).getValue().get(0).getValue());
	}
	@Test
	void testGetPieChartDataByPractitionerRoleId() {
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		LinkedHashMap<String, String> testParameters = initializeTestParameters();
		PieChartDefinition pieChartDefinition = getPieChartDefinition();
		HelperService helperServiceMock = mock(HelperService.class);
		when(helperServiceMock.getPieChartItemDefinitionFromFile(anyString()
		)).thenReturn(Collections.singletonList(pieChartDefinition));
		when(helperServiceMock.fetchIdsAndOrgIdToChildrenMapPair(anyString()
		)).thenReturn(createMockResult());
		when(helperServiceMock.getFhirSearchListByFilters(any(),anyString()
		)).thenReturn(Collections.emptyList());
		when(helperServiceMock.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(anyString(),any(),any(),anyString(),anyList()
		)).thenReturn(Double.valueOf("7.2"));
		doNothing().when(helperServiceMock).performCachingForPieChartData(anyList(),anyList(),any(),any(),anyList());
		doCallRealMethod().when(helperServiceMock).getPieChartDataByPractitionerRoleId(anyString(),anyString(),any(),anyString(),anyString(),false);
		ResponseEntity<?> output = helperServiceMock.getPieChartDataByPractitionerRoleId("2024-01-01","2024-01-07", filters,testParameters.get("env"),testParameters.get("lga"),false);
		List<PieChartItemDataCollection> pieChartItemDataCollection  =  (List<PieChartItemDataCollection>) output.getBody();
		assertEquals(HttpStatus.OK, output.getStatusCode());
		assert pieChartItemDataCollection != null;
		assertEquals("7.2", pieChartItemDataCollection.get(0).getData().get(0).getValue());

	}

	@Test
	void testGetTabularDataByPractitionerRoleId() {
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		LinkedHashMap<String, String> testParameters = initializeTestParameters();
		FhirPathTransformation fhirPathTransformation = new FhirPathTransformation("Bundle.entry.resource.ofType(QuestionnaireResponse).where(questionnaire='Questionnaire/post-natal-mother').encounter.distinct().count()","",null);
		List<TabularItem> tabularItems = new ArrayList<TabularItem>();
		tabularItems.add(new TabularItem(0,"postnatal","Postnatal Mother Care","Time interval after birth mother returning for visit","1","5",fhirPathTransformation));
		HelperService helperServiceMock = mock(HelperService.class);
		when(helperServiceMock.getTabularItemListFromFile(anyString()
		)).thenReturn(tabularItems);
		when(helperServiceMock.fetchIdsAndOrgIdToChildrenMapPair(anyString()
		)).thenReturn(createMockResult());
		when(helperServiceMock.getFhirSearchListByFilters(any(),anyString()
		)).thenReturn(Collections.emptyList());
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getCacheValueSumByDateRangeIndicatorAndOrgId(any(),any(),anyString(),anyString())).thenReturn(Double.valueOf("42.0"));
		Whitebox.setInternalState(NotificationDataSource.class, notificationDataSourceMock);
		doNothing().when(helperServiceMock).performCachingForTabularData(anyList(),anyList(),any(),any(),anyList());
		doCallRealMethod().when(helperServiceMock).getTabularDataByPractitionerRoleId(anyString(),anyString(),any(),anyString(),anyString(),false);
		ResponseEntity<?> output = helperServiceMock.getTabularDataByPractitionerRoleId("2024-01-01","2024-01-07", filters,testParameters.get("env"),testParameters.get("lga"),false);
		List<ScoreCardItem> scoreCardItems  =  (List<ScoreCardItem>) output.getBody();
		assertEquals(HttpStatus.OK, output.getStatusCode());
		assert scoreCardItems != null;
		assertEquals("42.0", scoreCardItems.get(0).getValue());
		FhirPathTransformation fhirPathTransformationUsingReflection = new FhirPathTransformation("Bundle.entry.resource.ofType(QuestionnaireResponse).where(questionnaire='Questionnaire/post-natal-mother').encounter.distinct().count()","","getCacheValueSumByDateRangeIndicatorAndOrgId");
		List<TabularItem> tabularItemsWithReflection = new ArrayList<TabularItem>();
		tabularItemsWithReflection.add(new TabularItem(0,"postnatal","Postnatal Mother Care","Time interval after birth mother returning for visit","1","5",fhirPathTransformationUsingReflection));
		when(helperServiceMock.getTabularItemListFromFile(anyString()
		)).thenReturn(tabularItemsWithReflection);
		PowerMockito.when(notificationDataSourceMock.getCacheValueSumByDateRangeIndicatorAndOrgId(any(),any(),anyString(),anyString())).thenReturn(Double.valueOf("22.7"));
		ResponseEntity<?> result = helperServiceMock.getTabularDataByPractitionerRoleId("2024-01-01","2024-01-07", filters,testParameters.get("env"),testParameters.get("lga"),false);
		List<ScoreCardItem> scoreCardItemsWithReflection  =  (List<ScoreCardItem>) result.getBody();
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assert scoreCardItemsWithReflection != null;
		assertEquals("22.7", scoreCardItemsWithReflection.get(0).getValue());
	}

	@Test
	void testGetDataByPractitionerRoleId() throws Exception {
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		String practitionerRoleId = "c91ee7cd-f994-4771-8863-3713e6e52e98";
		ReportType type = ReportType.valueOf("summary");
		FhirPathTransformation fhirPathTransformation = new FhirPathTransformation("Bundle.entry.resource.ofType(QuestionnaireResponse).where( questionnaire = 'Questionnaire/delivery').item.where(linkId='11.0').item.where(linkId='11.1').answer.where(value.code ='trained-staff').count() / Bundle.entry.resource.ofType(Observation).where(code.coding.code= 'ANC.End.18').count()","","getCacheValueAverageWithZeroByDateRangeIndicatorAndMultipleOrgIdForScorecard");
		IndicatorItem indicatorItem = new IndicatorItem(3,"SBA/ Deliveries ","(Delivery by Skilled birth Attendant / Reported Facility Deliveries) *100","0.5","0.8","percent",fhirPathTransformation);
		List<IndicatorItem> indicators = new ArrayList<IndicatorItem>();
		indicators.add(indicatorItem);
		List<ScoreCardIndicatorItem> scoreCardIndicatorItemsList = new ArrayList<ScoreCardIndicatorItem>();
		scoreCardIndicatorItemsList.add(new ScoreCardIndicatorItem("labour-and-delivery",indicators));
		List<OrgHierarchy> orgHierarchyList = new ArrayList<OrgHierarchy>();
		orgHierarchyList.add(new OrgHierarchy("e15b899b-8d94-4279-8cb7-3eb90a14279b","facility","7af9888c-f064-4f0f-9e98-bd272b91778c","6c20db8d-0a28-4a43-9f55-dd0ac0c4d625","de37b153-213f-4453-a621-7e3c514f37a8","607cd751-49cb-48e9-971b-38fcd03d06ba"));
		List<OrgIndicatorAverageResult> orgIndicatorAverageResults = new ArrayList<OrgIndicatorAverageResult>();
		orgIndicatorAverageResults.add(new OrgIndicatorAverageResult("e15b899b-8d94-4279-8cb7-3eb90a14279b","1ad7ac8c50c04e98614b5b5898c16668",Double.valueOf("0.28")));
		HelperService helperServiceMock = mock(HelperService.class);
		when(helperServiceMock.getIndicatorItemListFromFile(anyString()
		)).thenReturn(scoreCardIndicatorItemsList);
		when(helperServiceMock.getOrganizationIdByPractitionerRoleId(anyString()
		)).thenReturn("e15b899b-8d94-4279-8cb7-3eb90a14279b");
		when(helperServiceMock.fetchIdsAndOrgIdToChildrenMapPair(anyString()
		)).thenReturn(createMockResult());
		when(helperServiceMock.getFhirSearchListByFilters(any(),anyString()
		)).thenReturn(Collections.emptyList());
		doNothing().when(helperServiceMock).performCachingIfNotPresent(anyList(),anyList(),any(),any(),anyList());
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getOrganizationalHierarchyList(anyString())).thenReturn(orgHierarchyList);
		PowerMockito.when(helperServiceMock.getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflectionForScorecard(anyString(),anyList(),anyList(),any(),any())).thenReturn(orgIndicatorAverageResults);
		Whitebox.setInternalState(NotificationDataSource.class, notificationDataSourceMock);
		AppProperties appPropertiesMock =  new AppProperties();
		appPropertiesMock.setFacility_batch_size(50);
		Whitebox.setInternalState(helperServiceMock, appPropertiesMock);
		doCallRealMethod().when(helperServiceMock).getDataByPractitionerRoleId(anyString(),anyString(),anyString(),any(),any(),anyString(), false);
		doCallRealMethod().when(helperServiceMock).getFacilitiesForOrganization(any(),anyList());
		doCallRealMethod().when(helperServiceMock).calculateAverage(anyList(),anyList(),anyString());
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.initialize();
		when(helperServiceMock.getAsyncExecutor(
		)).thenReturn(executor);
		ResponseEntity<?> output = helperServiceMock.getDataByPractitionerRoleId(practitionerRoleId,"2024-01-01","2024-01-07",type,filters,"V2", false);
		List<ScoreCardResponseItem> scoreCardResponseItems  =  (List<ScoreCardResponseItem>) output.getBody();
		assertEquals(HttpStatus.OK, output.getStatusCode());
		assert scoreCardResponseItems != null;
		assertEquals("0.28", scoreCardResponseItems.get(0).getScoreCardItemList().get(0).getValue());
	}

	@Test
	void testPerformCachingIfNotPresentForBarChart() {
		Date start = Date.valueOf("2024-01-04");
		Date end = Date.valueOf("2024-01-04");
		List<String> facilityIds = new ArrayList<String>();
		facilityIds.add("e15b899b-8d94-4279-8cb7-3eb90a14279b");
		List<BarChartDefinition> barChartDefinitionList = getBarChartDefinitionList();
		HelperService helperServiceMock = mock(HelperService.class);
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getIndicatorsPresent(any(),any())).thenReturn(Collections.emptyList());
		PowerMockito.when(notificationDataSourceMock.getDatesPresent(any(),any(),anyList(),anyList())).thenReturn(Collections.emptyList());
		Whitebox.setInternalState(helperServiceMock,"notificationDataSource", notificationDataSourceMock);
		Whitebox.setInternalState(helperServiceMock,"cachingService", cachingService);
		doNothing().when(cachingService).cacheDataForBarChart(anyString(),any(),anyList(),anyInt(),anyString());
		doCallRealMethod().when(helperServiceMock).performCachingIfNotPresentForBarChart(anyList(),anyList(),any(),any(),anyList());
		helperServiceMock.performCachingIfNotPresentForBarChart(barChartDefinitionList,facilityIds,start,end,Collections.emptyList());
		verify(cachingService).cacheDataForBarChart(anyString(),any(),anyList(),anyInt(),anyString());
	}

	@Test
	void testPerformCachingForLineChartIfNotPresent(){
		Date start = Date.valueOf("2024-01-04");
		Date end = Date.valueOf("2024-01-04");
		List<String> facilityIds = new ArrayList<String>();
		facilityIds.add("e15b899b-8d94-4279-8cb7-3eb90a14279b");
		List<LineChart> lineCharts =getLineCharts();
		HelperService helperServiceMock = mock(HelperService.class);
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getIndicatorsPresent(any(),any())).thenReturn(Collections.emptyList());
		PowerMockito.when(notificationDataSourceMock.getDatesPresent(any(),any(),anyList(),anyList())).thenReturn(Collections.emptyList());
		Whitebox.setInternalState(helperServiceMock,"notificationDataSource", notificationDataSourceMock);
		Whitebox.setInternalState(helperServiceMock,"cachingService", cachingService);
		doNothing().when(cachingService).cacheDataLineChart(anyString(),any(),anyList(),anyInt(),anyString());
		doCallRealMethod().when(helperServiceMock).performCachingForLineChartIfNotPresent(anyList(),anyList(),any(),any(),anyList());
		helperServiceMock.performCachingForLineChartIfNotPresent(lineCharts,facilityIds,start,end,Collections.emptyList());
		verify(cachingService).cacheDataLineChart(anyString(),any(),anyList(),anyInt(),anyString());
	}

	@Test
	void testPerformCachingForPieChartData(){
		Date start = Date.valueOf("2024-01-04");
		Date end = Date.valueOf("2024-01-04");
		List<String> facilityIds = new ArrayList<String>();
		facilityIds.add("e15b899b-8d94-4279-8cb7-3eb90a14279b");
		PieChartDefinition pieChartDefinition = getPieChartDefinition();
		HelperService helperServiceMock = mock(HelperService.class);
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getIndicatorsPresent(any(),any())).thenReturn(Collections.emptyList());
		PowerMockito.when(notificationDataSourceMock.getDatesPresent(any(),any(),anyList(),anyList())).thenReturn(Collections.emptyList());
		Whitebox.setInternalState(helperServiceMock,"notificationDataSource", notificationDataSourceMock);
		Whitebox.setInternalState(helperServiceMock,"cachingService", cachingService);
		doNothing().when(cachingService).cachePieChartData(anyString(),any(),anyList(),anyInt(),anyString());
		doCallRealMethod().when(helperServiceMock).performCachingForPieChartData(anyList(),anyList(),any(),any(),anyList());
		helperServiceMock.performCachingForPieChartData(Collections.singletonList(pieChartDefinition),facilityIds,start,end,Collections.emptyList());
		verify(cachingService).cachePieChartData(anyString(),any(),anyList(),anyInt(),anyString());
	}

	@Test
	void testPerformCachingForTabularData(){
		Date start = Date.valueOf("2024-01-04");
		Date end = Date.valueOf("2024-01-04");
		List<String> facilityIds = new ArrayList<String>();
		facilityIds.add("e15b899b-8d94-4279-8cb7-3eb90a14279b");
		FhirPathTransformation fhirPathTransformation = new FhirPathTransformation("Bundle.entry.resource.ofType(QuestionnaireResponse).where(questionnaire='Questionnaire/post-natal-mother').encounter.distinct().count()","",null);
		List<TabularItem> tabularItems = new ArrayList<TabularItem>();
		tabularItems.add(new TabularItem(0,"postnatal","Postnatal Mother Care","Time interval after birth mother returning for visit","1","5",fhirPathTransformation));
		HelperService helperServiceMock = mock(HelperService.class);
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getIndicatorsPresent(any(),any())).thenReturn(Collections.emptyList());
		PowerMockito.when(notificationDataSourceMock.getDatesPresent(any(),any(),anyList(),anyList())).thenReturn(Collections.emptyList());
		Whitebox.setInternalState(NotificationDataSource.class, notificationDataSourceMock);
		Whitebox.setInternalState(helperServiceMock,"cachingService", cachingService);
		doNothing().when(cachingService).cacheTabularData(anyString(),any(),anyList(),anyInt(),anyString());
		doCallRealMethod().when(helperServiceMock).performCachingForTabularData(anyList(),anyList(),any(),any(),anyList());
		helperServiceMock.performCachingForTabularData(tabularItems,facilityIds,start,end,Collections.emptyList());
		verify(cachingService).cacheTabularData(anyString(),any(),anyList(),anyInt(),anyString());
	}

	@Test
	void testPerformCachingIfNotPresent(){
		Date start = Date.valueOf("2024-01-04");
		Date end = Date.valueOf("2024-01-04");
		List<String> facilityIds = new ArrayList<String>();
		facilityIds.add("e15b899b-8d94-4279-8cb7-3eb90a14279b");
		FhirPathTransformation fhirPathTransformation = new FhirPathTransformation("Bundle.entry.resource.ofType(QuestionnaireResponse).where( questionnaire = 'Questionnaire/delivery').item.where(linkId='11.0').item.where(linkId='11.1').answer.where(value.code ='trained-staff').count() / Bundle.entry.resource.ofType(Observation).where(code.coding.code= 'ANC.End.18').count()","","getCacheValueAverageWithZeroByDateRangeIndicatorAndMultipleOrgId");
		IndicatorItem indicatorItem = new IndicatorItem(3,"SBA/ Deliveries ","(Delivery by Skilled birth Attendant / Reported Facility Deliveries) *100","0.5","0.8","percent",fhirPathTransformation);
		List<IndicatorItem> indicators = new ArrayList<IndicatorItem>();
		indicators.add(indicatorItem);
		HelperService helperServiceMock = mock(HelperService.class);
		NotificationDataSource notificationDataSourceMock = PowerMockito.mock(NotificationDataSource.class);
		PowerMockito.when(notificationDataSourceMock.getIndicatorsPresent(any(),any())).thenReturn(Collections.emptyList());
		PowerMockito.when(notificationDataSourceMock.getDatesPresent(any(),any(),anyList(),anyList())).thenReturn(Collections.emptyList());
		Whitebox.setInternalState(helperServiceMock,"notificationDataSource", notificationDataSourceMock);
		Whitebox.setInternalState(helperServiceMock,"cachingService", cachingService);
		doNothing().when(cachingService).cacheData(anyString(),any(),anyList(),anyString());
		doCallRealMethod().when(helperServiceMock).performCachingIfNotPresent(anyList(),anyList(),any(),any(),anyList());
		helperServiceMock.performCachingIfNotPresent(indicators,facilityIds,start,end,Collections.emptyList());
		verify(cachingService).cacheData(anyString(),any(),anyList(),anyString());
	}
	private static java.sql.Clob createClob(String data) throws SQLException {
		return new javax.sql.rowset.serial.SerialClob(data.toCharArray());
	}
	private Pair<List<String>, LinkedHashMap<String, List<String>>> createMockResult() {
		// Create a sample data for testing
		List<String> facilityOrgIdList = new ArrayList<>();
		facilityOrgIdList.add("e15b899b-8d94-4279-8cb7-3eb90a14279b");
		LinkedHashMap<String, List<String>> mapOfIdToChildren = new LinkedHashMap<>();
		mapOfIdToChildren.put("ParentOrgId", new ArrayList<>(Arrays.asList("ChildOrgId1", "ChildOrgId2")));
		mapOfIdToChildren.put("ChildOrgId1", new ArrayList<>(Arrays.asList("GrandchildOrgId1", "e15b899b-8d94-4279-8cb7-3eb90a14279b")));
		return new Pair<>(facilityOrgIdList, mapOfIdToChildren);
	}

	private Bundle generateOrganizationBundle() {
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

	private List<PatientIdentifierEntity> getMockPatientList(){
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
		return mockPatientList;
	}

	private static String clobToString(Clob clob) {
		if (clob == null) {
			return null;
		}
		try (Reader reader = clob.getCharacterStream();
			  BufferedReader bufferedReader = new BufferedReader(reader)) {

			StringWriter writer = new StringWriter();
			char[] buffer = new char[1024];
			int charsRead;
			while ((charsRead = bufferedReader.read(buffer)) != -1) {
				writer.write(buffer, 0, charsRead);
			}
			return writer.toString();
		} catch (Exception e) {
			// Handle exceptions accordingly
			e.printStackTrace();
			return null;
		}
	}
	private void setDashboardEnvToConfigMap(DashboardConfigContainer container) throws NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		mapToReturn.put("V2", container);
		field.set(helperService, mapToReturn);
	}
	private void setEmptyConfiguration() throws NoSuchFieldException, IllegalAccessException {
		// Set the private field in helperService to simulate an empty configuration
		Field field = HelperService.class.getDeclaredField("dashboardEnvToConfigMap");
		field.setAccessible(true);
		Map<String, DashboardConfigContainer> mapToReturn = new HashMap<>();
		field.set(helperService, mapToReturn);
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
	private static LinkedHashMap<String, String> initializeTestParameters() {
		LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
		parameters.put("startDate", "2024-01-04");
		parameters.put("endDate", "2024-01-04");
		parameters.put("env", "V2");
		parameters.put("lga", "e15b899b-8d94-4279-8cb7-3eb90a14279b");
		return parameters;
}

	private List<BarChartDefinition> getBarChartDefinitionList(){
		FhirPathTransformation fhirPathTransformation = new FhirPathTransformation("Bundle.entry.resource.ofType(Immunization).where(vaccineCode.coding.code='ANC.B10.DE245').count()","","");
		List<BarComponent> barComponents = new ArrayList<BarComponent>();
		barComponents.add(new BarComponent(1,1,"IPTp given","#488AC7",fhirPathTransformation));
		List<BarChartItemDefinition> barChartItemDefinitions = new ArrayList<BarChartItemDefinition>();
		barChartItemDefinitions.add(new BarChartItemDefinition(1,1,"IPTp 1",barComponents));
		BarChartDefinition barChartDefinition = new BarChartDefinition(1,"IPTp Administration","antenatal","IPTp Bar Chart",barChartItemDefinitions);
		List<BarChartDefinition> barChartDefinitionList = new ArrayList<BarChartDefinition>();
		barChartDefinitionList.add(barChartDefinition);
		return barChartDefinitionList;
	}

	private List<LineChart> getLineCharts(){
		FhirPathTransformation fhirPathTransformation = new FhirPathTransformation("Bundle.entry.resource.ofType(QuestionnaireResponse).item.where(linkId='vitals').item.where(linkId='no-of-anc-visits').answer.where(value>=1).count()","","");
		List<LineChartItemDefinition> lineChartItemDefinitions = new ArrayList<LineChartItemDefinition>();
		lineChartItemDefinitions.add(new LineChartItemDefinition(1,"ANC 1 visits",1,"#488AC7","ANC 1","ANC 1 Visits Occurred","none",fhirPathTransformation));
		List<LineChart> lineCharts = new ArrayList<LineChart>();
		lineCharts.add(new LineChart(1,"ANC Trend","ANC Trend Line Chart","antenatal",lineChartItemDefinitions));
		return lineCharts;
	}
	private PieChartDefinition getPieChartDefinition(){
		FhirPathTransformation fhirPathTransformation = new FhirPathTransformation("Bundle.entry.resource.ofType(Immunization).where(vaccineCode.coding.display.contains('IPTp-SP dose')).count()","","");
		PieChartCategoryDefinition pieChartCategoryDefinition = new PieChartCategoryDefinition(0,1,"Drug Administration","IpTp Doses","IpTp Doses occurred","#05f6d5",fhirPathTransformation);
		List<PieChartCategoryDefinition> pieChartCategoryDefinitions = new ArrayList<PieChartCategoryDefinition>();
		pieChartCategoryDefinitions.add(pieChartCategoryDefinition);
		PieChartDefinition pieChartDefinition = new PieChartDefinition("antenatal","Drug Administration Pie Chart",pieChartCategoryDefinitions);
		return pieChartDefinition;
	}

}