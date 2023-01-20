package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.model.AnalyticComparison;
import ca.uhn.fhir.jpa.starter.model.AnalyticItem;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.jpa.starter.model.ApiAsyncTaskEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.iprd.fhir.utils.*;
import com.iprd.report.*;

import android.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.hibernate.engine.jdbc.ClobProxy;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static org.hibernate.search.util.common.impl.CollectionHelper.asList;
import static org.keycloak.util.JsonSerialization.mapper;


@Import(AppProperties.class)
@Service
public class HelperService {

	NotificationDataSource datasource = NotificationDataSource.getInstance();
	@Autowired
	AppProperties appProperties;
	@Autowired
	HttpServletRequest request;
	@Autowired
	CachingService cachingService;


	FhirContext ctx;
	Keycloak instance;
	TokenManager tokenManager;
	BearerTokenAuthInterceptor authInterceptor;

	private static final Logger logger = LoggerFactory.getLogger(HelperService.class);
	private static String IDENTIFIER_SYSTEM = "http://www.iprdgroup.com/Identifier/System";
	private static String SMS_EXTENTION_URL = "http://iprdgroup.com/Extentions/sms-sent";
	private static final long INITIAL_DELAY = 5 * 30000L;
	private static final long FIXED_DELAY = 5 * 60000L;

	private static final long AUTH_INITIAL_DELAY = 25 * 60000L;
	private static final long AUTH_FIXED_DELAY = 50 * 60000L;
	private static final long DELAY = 2 * 60000;

	NotificationDataSource notificationDataSource;

	public ResponseEntity<LinkedHashMap<String, Object>> createGroups(MultipartFile file) throws IOException {

		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		List<String> states = new ArrayList<>();
		List<String> lgas = new ArrayList<>();
		List<String> wards = new ArrayList<>();
		List<String> clinics = new ArrayList<>();
		List<String> invalidClinics = new ArrayList<>();

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
		String singleLine;
		int iteration = 0;
		String stateId = "", lgaId = "", wardId = "", facilityOrganizationId = "", facilityLocationId = "";
		String stateGroupId = "", lgaGroupId = "", wardGroupId = "", facilityGroupId = "";

		while ((singleLine = bufferedReader.readLine()) != null) {
			if (iteration == 0) { //skip header of CSV file
				iteration++;
				continue;
			}
			String[] csvData = singleLine.split(",");
			//State, LGA, Ward, FacilityUID, FacilityCode, CountryCode, PhoneNumber, FacilityName, FacilityLevel, Ownership, Argusoft Identifier
			if (!csvData[3].isEmpty()) {
				if (Validation.validateClinicAndStateCsvLine(csvData)) {
					if (!states.contains(csvData[0])) {
						Organization state = FhirResourceTemplateHelper.state(csvData[0]);
						stateId = createResource(state, Organization.class, Organization.NAME.matchesExactly().value(state.getName()));
						states.add(state.getName());
						GroupRepresentation stateGroupRep = KeycloakTemplateHelper.stateGroup(state.getName(), stateId);
						stateGroupId = createGroup(stateGroupRep);
						updateResource(stateGroupId, stateId, Organization.class);
					}

					if (!lgas.contains(csvData[1])) {
						Organization lga = FhirResourceTemplateHelper.lga(csvData[1], csvData[0], stateId);
						lgaId = createResource(lga, Organization.class, Organization.NAME.matchesExactly().value(lga.getName()));
						lgas.add(lga.getName());
						GroupRepresentation lgaGroupRep = KeycloakTemplateHelper.lgaGroup(lga.getName(), stateGroupId, lgaId);
						lgaGroupId = createGroup(lgaGroupRep);
						updateResource(lgaGroupId, lgaId, Organization.class);
					}

					if (!wards.contains(csvData[2])) {
						Organization ward = FhirResourceTemplateHelper.ward(csvData[0], csvData[1], csvData[2], lgaId);
						wardId = createResource(ward, Organization.class, Organization.NAME.matchesExactly().value(ward.getName()));
						wards.add(ward.getName());
						GroupRepresentation wardGroupRep = KeycloakTemplateHelper.wardGroup(ward.getName(), lgaGroupId, wardId);
						wardGroupId = createGroup(wardGroupRep);
						updateResource(wardGroupId, wardId, Organization.class);
					}

					if (!clinics.contains(csvData[7])) {
						Location clinicLocation = FhirResourceTemplateHelper.clinic(csvData[0], csvData[1], csvData[2], csvData[7]);
						Organization clinicOrganization = FhirResourceTemplateHelper.clinic(csvData[7], csvData[3], csvData[4], csvData[5], csvData[6], csvData[0], csvData[1], csvData[2], wardId, csvData[10]);
						facilityOrganizationId = createResource(clinicOrganization, Organization.class, Organization.NAME.matchesExactly().value(clinicOrganization.getName()));
						facilityLocationId = createResource(clinicLocation, Location.class, Location.NAME.matchesExactly().value(clinicLocation.getName()));
						clinics.add(clinicOrganization.getName());

						GroupRepresentation facilityGroupRep = KeycloakTemplateHelper.facilityGroup(
							clinicOrganization.getName(),
							wardGroupId,
							facilityOrganizationId,
							facilityLocationId,
							csvData[8],
							csvData[9],
							csvData[3],
							csvData[4],
							csvData[10]
						);
						facilityGroupId = createGroup(facilityGroupRep);
						updateResource(facilityGroupId, facilityOrganizationId, Organization.class);
						updateResource(facilityGroupId, facilityLocationId, Location.class);
					}
				} else {
					invalidClinics.add(csvData[7] + "," + csvData[0] + "," + csvData[1] + "," + csvData[2]);
				}
			}
		}
		map.put("Cannot create Clinics with state, lga, ward", invalidClinics);
		map.put("uploadCSV", "Successful");
		return new ResponseEntity<LinkedHashMap<String, Object>>(map, HttpStatus.OK);
	}

	public ResponseEntity<LinkedHashMap<String, Object>> createUsers(@RequestParam("file") MultipartFile file) throws Exception {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		List<String> practitioners = new ArrayList<>();
		List<String> invalidUsers = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
		String singleLine;
		int iteration = 0;
		String practitionerRoleId = "";
		String practitionerId = "";
		String organizationId = "";

		while ((singleLine = bufferedReader.readLine()) != null) {
			if (iteration == 0) { //Skip header of CSV
				iteration++;
				continue;
			}
			String hcwData[] = singleLine.split(",");
			organizationId = getOrganizationIdByFacilityUID(hcwData[12]);
			//firstName,lastName,email,countryCode,phoneNumber,gender,birthDate,keycloakUserName,initialPassword,state,lga,ward,facilityUID,role,qualification,stateIdentifier, Argusoft Identifier
			if (!hcwData[12].isEmpty()) {
				if (Validation.validationHcwCsvLine(hcwData)) {
					if (!(practitioners.contains(hcwData[0]) && practitioners.contains(hcwData[1]) && practitioners.contains(hcwData[4] + hcwData[3]))) {
						Practitioner hcw = FhirResourceTemplateHelper.hcw(hcwData[0], hcwData[1], hcwData[4], hcwData[3], hcwData[5], hcwData[6], hcwData[9], hcwData[10], hcwData[11], hcwData[12], hcwData[13], hcwData[14], hcwData[15], hcwData[16]);
						practitionerId = createResource(hcw,
							Practitioner.class,
							Practitioner.GIVEN.matches().value(hcw.getName().get(0).getGivenAsSingleString()),
							Practitioner.FAMILY.matches().value(hcw.getName().get(0).getFamily()),
							Practitioner.TELECOM.exactly().systemAndValues(ContactPoint.ContactPointSystem.PHONE.toCode(), Arrays.asList(hcwData[4] + hcwData[3]))
						); // Catch index out of bound
						practitioners.add(hcw.getName().get(0).getFamily());
						practitioners.add(hcw.getName().get(0).getGivenAsSingleString());
						practitioners.add(hcw.getTelecom().get(0).getValue());
						PractitionerRole practitionerRole = FhirResourceTemplateHelper.practitionerRole(hcwData[13], hcwData[14], practitionerId, organizationId);
						practitionerRoleId = createResource(practitionerRole, PractitionerRole.class, PractitionerRole.PRACTITIONER.hasId(practitionerId));
						UserRepresentation user = KeycloakTemplateHelper.user(hcwData[0], hcwData[1], hcwData[2], hcwData[7], hcwData[8], hcwData[4], hcwData[3], practitionerId, practitionerRoleId, hcwData[13], hcwData[9], hcwData[10], hcwData[11], hcwData[12], hcwData[16]);
						String keycloakUserId = createUser(user);
						RoleRepresentation role = KeycloakTemplateHelper.role(hcwData[13]);
						createRoleIfNotExists(role);
						if (keycloakUserId != null) {
							assignRole(keycloakUserId,role.getName());
							updateResource(keycloakUserId, practitionerId, Practitioner.class);
							updateResource(keycloakUserId, practitionerRoleId, PractitionerRole.class);
						}
					}
				}
			} else {
				invalidUsers.add(hcwData[0] + " " + hcwData[1] + "," + hcwData[9] + "," + hcwData[10] + "," + hcwData[11] + "," + hcwData[12]);
			}
		}
		map.put("Cannot create users with groups", invalidUsers);
		map.put("uploadCsv", "Successful");
		return new ResponseEntity<LinkedHashMap<String, Object>>(map, HttpStatus.OK);
	}

	public ResponseEntity<LinkedHashMap<String, Object>> createDashboardUsers(@RequestParam("file") MultipartFile file) throws Exception {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		List<String> practitioners = new ArrayList<>();
		List<String> invalidUsers = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
		String singleLine;
		int iteration = 0;
		String practitionerRoleId = "";
		String practitionerId = "";
		String organizationId = "";

		while ((singleLine = bufferedReader.readLine()) != null) {
			if (iteration == 0) {
				iteration++;
				continue;
			}
			String hcwData[] = singleLine.split(",");
			if (!hcwData[11].isEmpty()) {
				organizationId = getOrganizationIdByOrganizationName(hcwData[11]);
				//firstName,lastName,email,phoneNumber,countryCode,gender,birthDate,keycloakUserName,facilityUID,role,initialPassword,Organization,Type
				Organization state = FhirResourceTemplateHelper.state(hcwData[11]);
				if (Validation.validationHcwCsvLine(hcwData)) {
					if (!(practitioners.contains(hcwData[0]) && practitioners.contains(hcwData[1]) && practitioners.contains(hcwData[3] + hcwData[4]))) {
						Practitioner hcw = FhirResourceTemplateHelper.user(hcwData[0], hcwData[1], hcwData[3], hcwData[4], hcwData[5], hcwData[6], hcwData[11], hcwData[6], state.getMeta().getTag().get(0).getCode());
						practitionerId = createResource(hcw,
							Practitioner.class,
							Practitioner.GIVEN.matches().value(hcw.getName().get(0).getGivenAsSingleString()),
							Practitioner.FAMILY.matches().value(hcw.getName().get(0).getFamily()),
							Practitioner.TELECOM.exactly().systemAndValues(ContactPoint.ContactPointSystem.PHONE.toCode(), Arrays.asList(hcwData[4] + hcwData[3]))
						);
						practitioners.add(hcw.getName().get(0).getFamily());
						practitioners.add(hcw.getName().get(0).getGivenAsSingleString());
						practitioners.add(hcw.getTelecom().get(0).getValue());
						PractitionerRole practitionerRole = FhirResourceTemplateHelper.practitionerRole(hcwData[10], "NA", practitionerId, organizationId);
						practitionerRoleId = createResource(practitionerRole, PractitionerRole.class, PractitionerRole.PRACTITIONER.hasId(practitionerId));
						UserRepresentation user = KeycloakTemplateHelper.dashboardUser(hcwData[0], hcwData[1], hcwData[2], hcwData[7], hcwData[8], hcwData[3], hcwData[4], practitionerId, practitionerRoleId, hcwData[9], hcwData[10], hcwData[11], state.getMeta().getTag().get(0).getCode());
						String keycloakUserId = createUser(user);
						if (keycloakUserId != null) {
							updateResource(keycloakUserId, practitionerId, Practitioner.class);
							updateResource(keycloakUserId, practitionerRoleId, PractitionerRole.class);
						}
					}
				}
			} else {
				invalidUsers.add(hcwData[0] + " " + hcwData[1] + "," + hcwData[11]);
			}
		}
		map.put("Cannot create users with organization", invalidUsers);
		map.put("uploadCsv", "Successful");
		return new ResponseEntity<LinkedHashMap<String, Object>>(map, HttpStatus.OK);
	}

	public List<GroupRepresentation> getGroupsByUser(String userId) {
		RealmResource realmResource = FhirClientAuthenticatorService.getKeycloak().realm(appProperties.getKeycloak_Client_Realm());
		List<GroupRepresentation> groups = realmResource.users().get(userId).groups(0, appProperties.getKeycloak_max_group_count(), false);
		return groups;
	}

	public ResponseEntity<List<Map<String, String>>> getAncMetaDataByOrganizationId(String organizationId, String startDate, String endDate) {
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<Map<String, String>> ancMetaData = ReportGeneratorFactory.INSTANCE.reportGenerator().getAncMetaDataByOrganizationId(fhirClientProvider, new DateRange(startDate, endDate), organizationId);
		return ResponseEntity.ok(ancMetaData);
	}

	public ResponseEntity<?> getAncDailySummaryData(String organizationId, String startDate, String endDate, LinkedHashMap<String, String> filters) {
		try {
			FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
			List<String> fhirSearchList = getFhirSearchListByFilters(filters);
			ANCDailySummaryConfig ancDailySummaryConfig = getANCDailySummaryConfigFromFile();
			DataResult dataResult = ReportGeneratorFactory.INSTANCE.reportGenerator().getAncDailySummaryData(fhirClientProvider, new DateRange(startDate, endDate), organizationId, ancDailySummaryConfig, fhirSearchList);
			return ResponseEntity.ok(dataResult);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
	}

	public void saveInAsyncTable(DataResult dataResult, String id) {

		byte[] summaryResult = dataResult.getSummaryResult();
		List<Map<String, String>> dailyResult = dataResult.getDailyResult();
		String base64SummaryResult = Base64.getEncoder().encodeToString(summaryResult);
		String dailyResultJsonString = new Gson().toJson(dailyResult); // SPlit into two , one arraylist and one base64Encoded string.

		try {
			ArrayList asyncData = datasource.fetchStatus(id);
			ApiAsyncTaskEntity asyncRecord = (ApiAsyncTaskEntity) asyncData.get(0);
			asyncRecord.setStatus(ApiAsyncTaskEntity.Status.COMPLETED.name());
			asyncRecord.setDailyResult(ClobProxy.generateProxy(dailyResultJsonString));
			asyncRecord.setSummaryResult(ClobProxy.generateProxy(base64SummaryResult));
			datasource.update(asyncRecord);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public String convertClobToString (Clob input) throws  IOException, SQLException{
		Reader reader = input.getCharacterStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(reader, writer);
		return writer.toString();
	}


	public ResponseEntity<?> getAsyncData(String uuid) throws SQLException, IOException {

		ArrayList<ApiAsyncTaskEntity> asyncData = datasource.fetchStatus(uuid);
		if (asyncData == null) return null;
		ApiAsyncTaskEntity asyncRecord = asyncData.get(0);

		if (asyncRecord.getSummaryResult() == null) return null;

		String dailyResultInString = convertClobToString(asyncRecord.getDailyResult());
		String summaryResultInString = convertClobToString(asyncRecord.getSummaryResult());
		List<Map<String, String>> dailyResult= mapper.readValue(dailyResultInString, new TypeReference<List<Map<String, String>>>() {});
		DataResultJava dataResult = new DataResultJava(summaryResultInString, dailyResult);
		return  ResponseEntity.ok(dataResult);

	}

	public ResponseEntity<?> checkIfDataExistsInAsyncTable(String uuid) throws SQLException, IOException {
		if (getAsyncData(uuid) == null) {
			return ResponseEntity.ok("Searching in Progress");
		} else return ResponseEntity.ok(getAsyncData(uuid));

	}


	@Async
	public void saveQueryResult(String organizationId, String startDate, String endDate, LinkedHashMap<String, String> filters, String id) {

		try {
			FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
			List<String> fhirSearchList = getFhirSearchListByFilters(filters);
			ANCDailySummaryConfig ancDailySummaryConfig = getANCDailySummaryConfigFromFile();
			DataResult dataResult = ReportGeneratorFactory.INSTANCE.reportGenerator().getAncDailySummaryData(fhirClientProvider, new DateRange(startDate, endDate), organizationId, ancDailySummaryConfig, fhirSearchList);
			saveInAsyncTable(dataResult, id);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	@Scheduled(cron = "0 0 23 * * *")
	public void removeAsyncTableCache() {
		datasource.clearAsyncTable();
	}

	public Bundle getEncountersBelowLocation(String locationId) {
		List<String> locationIdsList = new ArrayList<>();
		locationIdsList.add(locationId);
		ListIterator<String> locationIdIterator = locationIdsList.listIterator();

		while (locationIdIterator.hasNext()) {
			List<String> tempList = new ArrayList<>();
			getOrganizationsPartOf(tempList, FhirClientAuthenticatorService.serverBase + "/Location?partof=Location/" + locationIdIterator.next() + "&_elements=id");
			tempList.forEach(item -> {
				locationIdIterator.add(item);
				locationIdIterator.previous();
			});
		}
		Bundle batchBundle = generateBatchBundle("/Encounter?location=" + String.join(",", locationIdsList));
		Bundle responseBundle = FhirClientAuthenticatorService.getFhirClient().transaction().withBundle(batchBundle).prettyPrint().encodedJson().execute();
		return responseBundle;
	}

	private Pair<List<String>, LinkedHashMap<String, List<String>>> getFacilityIdsAndOrgIdToChildrenMapPair(String orgId) {
		List<String> facilityOrgIdList = new ArrayList<>();
		List<String> orgIdList = new ArrayList<>();
		orgIdList.add(orgId);
		ListIterator<String> orgIdIterator = orgIdList.listIterator();

		LinkedHashMap<String, List<String>> mapOfIdToChildren = new LinkedHashMap<>();

		while (orgIdIterator.hasNext()) {
			String tempOrgId = orgIdIterator.next();
			List<String> childrenList = new ArrayList<>();
			getOrganizationsPartOf(childrenList, FhirClientAuthenticatorService.serverBase + "/Organization?partof=Organization/" + tempOrgId + "&_elements=id");
			childrenList.forEach(item -> {
				orgIdIterator.add(item);
				orgIdIterator.previous();
			});

			if (childrenList.isEmpty()) {
				facilityOrgIdList.add(tempOrgId);
			}

			mapOfIdToChildren.put(tempOrgId, childrenList);

			mapOfIdToChildren.forEach((id, children) -> {
				if (children.contains(tempOrgId)) {
					List<String> prevChild = mapOfIdToChildren.get(id);
					prevChild.addAll(childrenList);
					mapOfIdToChildren.put(id, prevChild);
				}
			});
		}

		return new Pair<>(facilityOrgIdList, mapOfIdToChildren);
	}

	private List<String> getFacilityOrgIds(String orgId) {
		List<String> facilityOrgIdList = new ArrayList<>();
		List<String> orgIdList = new ArrayList<>();
		orgIdList.add(orgId);
		ListIterator<String> orgIdIterator = orgIdList.listIterator();

		while (orgIdIterator.hasNext()) {
			String tempOrgId = orgIdIterator.next();
			List<String> childrenList = new ArrayList<>();
			getOrganizationsPartOf(childrenList, FhirClientAuthenticatorService.serverBase + "/Organization?partof=Organization/" + tempOrgId + "&_elements=id");
			childrenList.forEach(item -> {
				orgIdIterator.add(item);
				orgIdIterator.previous();
			});
			if (childrenList.isEmpty()) {
				facilityOrgIdList.add(tempOrgId);
			}
		}
		return facilityOrgIdList;
	}

	private LinkedHashMap<String, List<String>> getOrganizationIdToChildrenMap(String orgId) {
		List<String> orgIdList = new ArrayList<>();
		orgIdList.add(orgId);
		ListIterator<String> orgIdIterator = orgIdList.listIterator();

		LinkedHashMap<String, List<String>> mapOfIdToChildren = new LinkedHashMap<>();

		while (orgIdIterator.hasNext()) {
			String tempOrgId = orgIdIterator.next();
			List<String> childrenList = new ArrayList<>();
			getOrganizationsPartOf(childrenList, FhirClientAuthenticatorService.serverBase + "/Organization?partof=Organization/" + tempOrgId + "&_elements=id");
			childrenList.forEach(item -> {
				orgIdIterator.add(item);
				orgIdIterator.previous();
			});

			mapOfIdToChildren.put(tempOrgId, childrenList);

			mapOfIdToChildren.forEach((id, children) -> {
				if (children.contains(tempOrgId)) {
					List<String> prevChild = mapOfIdToChildren.get(id);
					prevChild.addAll(childrenList);
					mapOfIdToChildren.put(id, prevChild);
				}
			});

		}
		return mapOfIdToChildren;
	}

	public void getOrganizationsPartOf(List<String> idsList, String url) {
		Bundle searchBundle = FhirClientAuthenticatorService.getFhirClient().search()
			.byUrl(url)
			.returnBundle(Bundle.class)
			.execute();
		idsList.addAll(searchBundle.getEntry().stream().map(r -> r.getResource().getIdElement().getIdPart()).collect(Collectors.toList()));
		if (searchBundle.hasLink() && bundleContainsNext(searchBundle)) {
			getOrganizationsPartOf(idsList, getNextUrl(searchBundle.getLink()));
		}
	}

	public Bundle generateBatchBundle(String url) {
		Bundle bundle = new Bundle();
		bundle.setId("batch-bundle");
		bundle.setType(BundleType.BATCH);
		BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();

		BundleEntryRequestComponent bundleEntryRequestComponent = new BundleEntryRequestComponent();
		bundleEntryRequestComponent.setMethod(HTTPVerb.GET);
		bundleEntryRequestComponent.setUrl(url);

		bundleEntryComponent.setRequest(bundleEntryRequestComponent);
		bundle.addEntry(bundleEntryComponent);
		return bundle;
	}

	public ResponseEntity<?> getIndicators() {
		try {
			List<IndicatorItem> indicators = getIndicatorItemListFromFile();
			return ResponseEntity.ok(indicators);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
	}

	public ResponseEntity<?> getLineChartDefinitions() {
		try {
			List<LineChart> lineCharts = getLineChartDefinitionsItemListFromFile();
			return ResponseEntity.ok(lineCharts);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
	}
	public ResponseEntity<?> getTabularIndicators() {
		try {
			List<TabularItem> indicators = getTabularItemListFromFile();
			return ResponseEntity.ok(indicators);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
	}

	public ResponseEntity<?> getPieChartDefinition() {
		try {
			List<PieChartDefinition> pieChartIndicators = getPieChartItemDefinitionFromFile();
			return ResponseEntity.ok(pieChartIndicators);
		} catch (FileNotFoundException e){
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
	}
	public ResponseEntity<?> getFilters() {
		try {
			List<FilterItem> filters = getFilterItemListFromFile();
			return ResponseEntity.ok(filters);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error:Config File Not Found");
		}
	}

	public List<OrgItem> getOrganizationsByPractitionerRoleId(String practitionerRoleId) {
		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
		return getOrganizationHierarchy(organizationId);
	}

	public ResponseEntity<?> getPieChartData(String practitionerRoleId, String startDate, String endDate, LinkedHashMap<String, String> filters){
		List<PieChartItem> pieChartItems = new ArrayList<>();
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		try{
			List<PieChartDefinition> pieChartDefinitions = getPieChartItemDefinitionFromFile();
			String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
			List<String> fhirSearchList = getFhirSearchListByFilters(filters);
			pieChartItems = ReportGeneratorFactory.INSTANCE.reportGenerator().getPieChartData(fhirClientProvider, organizationId, new DateRange(startDate, endDate), pieChartDefinitions, fhirSearchList);
		} catch (FileNotFoundException e){
			e.printStackTrace();
			return null;
		}
		return ResponseEntity.ok(pieChartItems);
	}
	public ResponseEntity<?> getDataByPractitionerRoleIdWithFilters(String practitionerRoleId, String startDate, String endDate, ReportType type, LinkedHashMap<String, String> filters) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<ScoreCardItem> scoreCardItems = new ArrayList<>();
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());

		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
		try {
			List<IndicatorItem> indicators = getIndicatorItemListFromFile();
			List<String> fhirSearchList = getFhirSearchListByFilters(filters);
			switch (type) {
				case summary: {
					scoreCardItems = ReportGeneratorFactory.INSTANCE.reportGenerator().getData(fhirClientProvider, organizationId, new DateRange(startDate, endDate), indicators, fhirSearchList);
					break;
				}
				case quarterly: {
					List<Pair<Date, Date>> quarterDatePairList = DateUtilityHelper.getQuarterlyDates(Date.valueOf(startDate), Date.valueOf(endDate));
					for (Pair<Date, Date> pair : quarterDatePairList) {
						List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getFacilityData(fhirClientProvider, organizationId, new DateRange(pair.first.toString(), pair.second.toString()), indicators, fhirSearchList);
						for (IndicatorItem indicatorItem : indicators) {
							List<ScoreCardItem> filteredList = data.stream().filter(scoreCardItem -> indicatorItem.getId() == scoreCardItem.getIndicatorId()).collect(Collectors.toList());
							double sum = 0;
							for (ScoreCardItem item : filteredList) {
								sum += Double.parseDouble(item.getValue());
							}
							scoreCardItems.add(new ScoreCardItem(organizationId, indicatorItem.getId(), String.valueOf(sum), pair.first.toString(), pair.second.toString()));
						}
					}
					break;
				}
				case weekly: {
					List<Pair<Date, Date>> weeklyDatePairList = DateUtilityHelper.getWeeklyDates(Date.valueOf(startDate), Date.valueOf(endDate));
					for (Pair<Date, Date> pair : weeklyDatePairList) {
						List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getFacilityData(fhirClientProvider, organizationId, new DateRange(pair.first.toString(), pair.second.toString()), indicators, fhirSearchList);
						for (IndicatorItem indicatorItem : indicators) {
							List<ScoreCardItem> filteredList = data.stream().filter(scoreCardItem -> indicatorItem.getId() == scoreCardItem.getIndicatorId()).collect(Collectors.toList());
							double sum = 0;
							for (ScoreCardItem item : filteredList) {
								sum += Double.parseDouble(item.getValue());
							}
							scoreCardItems.add(new ScoreCardItem(organizationId, indicatorItem.getId(), String.valueOf(sum), pair.first.toString(), pair.second.toString()));
						}
					}
					break;
				}
				case monthly: {
					List<Pair<Date, Date>> monthlyDatePairList = DateUtilityHelper.getMonthlyDates(Date.valueOf(startDate), Date.valueOf(endDate));
					for (Pair<Date, Date> pair : monthlyDatePairList) {
						List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getFacilityData(fhirClientProvider, organizationId, new DateRange(pair.first.toString(), pair.second.toString()), indicators, fhirSearchList);
						for (IndicatorItem indicatorItem : indicators) {
							List<ScoreCardItem> filteredList = data.stream().filter(scoreCardItem -> indicatorItem.getId() == scoreCardItem.getIndicatorId()).collect(Collectors.toList());
							double sum = 0;
							for (ScoreCardItem item : filteredList) {
								sum += Double.parseDouble(item.getValue());
							}
							scoreCardItems.add(new ScoreCardItem(organizationId, indicatorItem.getId(), String.valueOf(sum), pair.first.toString(), pair.second.toString()));
						}
					}
					break;
				}
				case daily: {
					List<Pair<Date, Date>> dailyDatePairList = DateUtilityHelper.getDailyDates(Date.valueOf(startDate), Date.valueOf(endDate));
					for (Pair<Date, Date> pair : dailyDatePairList) {
						List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getFacilityData(fhirClientProvider, organizationId, new DateRange(pair.first.toString(), pair.second.toString()), indicators, fhirSearchList);
						for (IndicatorItem indicatorItem : indicators) {
							List<ScoreCardItem> filteredList = data.stream().filter(scoreCardItem -> indicatorItem.getId() == scoreCardItem.getIndicatorId()).collect(Collectors.toList());
							double sum = 0;
							for (ScoreCardItem item : filteredList) {
								sum += Double.parseDouble(item.getValue());
							}
							scoreCardItems.add(new ScoreCardItem(organizationId, indicatorItem.getId(), String.valueOf(sum), pair.first.toString(), pair.second.toString()));
						}
					}
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
		return ResponseEntity.ok(scoreCardItems);
	}

	public List<AnalyticItem> getMaternalAnalytics(String organizationId) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<AnalyticItem> analyticItems = new ArrayList<>();
		try {
			List<IndicatorItem> analyticsItemListFromFile = getAnalyticsItemListFromFile();
			Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = getFacilityIdsAndOrgIdToChildrenMapPair(organizationId);

			Pair<Date, Date> currentWeek = DateUtilityHelper.getCurrentWeekDates();
			Pair<Date, Date> prevWeek = DateUtilityHelper.getPrevWeekDates();

			performCachingIfNotPresent(analyticsItemListFromFile, idsAndOrgIdToChildrenMapPair.first, prevWeek.first, currentWeek.second);
			for (IndicatorItem indicator : analyticsItemListFromFile) {
				Double currentWeekCacheValueSum = notificationDataSource
					.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(currentWeek.first, currentWeek.second,
						Utils.getMd5StringFromFhirPath(indicator.getFhirPath()), idsAndOrgIdToChildrenMapPair.first);

				Double prevWeekCacheValueSum = notificationDataSource
					.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(prevWeek.first, prevWeek.second,
						Utils.getMd5StringFromFhirPath(indicator.getFhirPath()), idsAndOrgIdToChildrenMapPair.first);

				AnalyticComparison comparisonValue = (currentWeekCacheValueSum > prevWeekCacheValueSum) ? AnalyticComparison.POSITIVE_UP : AnalyticComparison.NEGATIVE_DOWN;

				analyticItems.add(new AnalyticItem(indicator.getDescription(), String.valueOf(currentWeekCacheValueSum.intValue()), comparisonValue));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return analyticItems;
	}

	public ResponseEntity<?> getTabularDataByPractitionerRoleId(String practitionerRoleId, String startDate, String endDate, LinkedHashMap<String, String> filters) {
		List<ScoreCardItem> scoreCardItems = new ArrayList<>();
		try {
			List<TabularItem> tabularItemList = getTabularItemListFromFile();
			List<String> fhirSearchList = getFhirSearchListByFilters(filters);

			String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
			FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());

			Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = getFacilityIdsAndOrgIdToChildrenMapPair(organizationId);

			if (fhirSearchList.isEmpty()) {
				Date start = Date.valueOf(startDate);
				Date end = Date.valueOf(endDate);
				notificationDataSource = NotificationDataSource.getInstance();
				performCachingForTabularData(tabularItemList, idsAndOrgIdToChildrenMapPair.first, start, end);
				for (String orgId : idsAndOrgIdToChildrenMapPair.first) {
					for (TabularItem indicator : tabularItemList) {
						Double cacheValueSum = notificationDataSource
							.getCacheValueSumByDateRangeIndicatorAndOrgId(start, end,
								Utils.getMd5StringFromFhirPath(indicator.getFhirPath()), orgId);
						scoreCardItems.add(new ScoreCardItem(orgId, indicator.getId(),
							cacheValueSum.toString(), startDate, endDate));
					}
				}
			} else {
				scoreCardItems = ReportGeneratorFactory.INSTANCE.reportGenerator().getTabularData(fhirClientProvider, organizationId, new DateRange(startDate, endDate), tabularItemList, fhirSearchList);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
		return ResponseEntity.ok(scoreCardItems);
	}

	private void performCachingForTabularData(List<TabularItem> indicators, List<String> facilityIds, Date startDate, Date endDate) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<String> currentIndicatorMD5List = indicators.stream().map(indicatorItem -> Utils.getMd5StringFromFhirPath(indicatorItem.getFhirPath())).collect(Collectors.toList());

		List<Date> dates = new ArrayList<>();
		List<String> presentIndicators = notificationDataSource.getIndicatorsPresent(startDate, endDate);

		List<String> existingIndicators = new ArrayList<>();
		List<String> nonExistingIndicators = new ArrayList<>();

		for (String indicator : currentIndicatorMD5List) {
			if (presentIndicators.contains(indicator)) {
				existingIndicators.add(indicator);
			} else {
				nonExistingIndicators.add(indicator);
			}
		}
		List<Date> presentDates = notificationDataSource.getDatesPresent(startDate, endDate, nonExistingIndicators.isEmpty() ? existingIndicators : nonExistingIndicators, facilityIds);

		Date start = startDate;
		Date end = Date.valueOf(endDate.toLocalDate().plusDays(1));
		while (!start.equals(end)) {
			if (!presentDates.contains(start)) {
				dates.add(start);
			}
			start = Date.valueOf(start.toLocalDate().plusDays(1));
		}

		facilityIds.forEach(facilityId -> {
			dates.forEach(date -> {
				cachingService.cacheTabularData(facilityId, date, indicators);
			});
		});

		Date currentDate = DateUtilityHelper.getCurrentSqlDate();
		//Always cache current date data if it lies between start and end date.
		if (currentDate.getTime() >= startDate.getTime() && currentDate.getTime() <= Date.valueOf(endDate.toLocalDate().plusDays(1)).getTime()) {
			facilityIds.forEach(facilityId -> {
				cachingService.cacheTabularData(facilityId, DateUtilityHelper.getCurrentSqlDate(), indicators);
			});
		}

	}

	public ResponseEntity<?> getDataByPractitionerRoleId(String practitionerRoleId, String startDate, String endDate, ReportType type) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<ScoreCardItem> scoreCardItems = new ArrayList<>();
		try {
			List<IndicatorItem> indicators = getIndicatorItemListFromFile();
			String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);

			Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = getFacilityIdsAndOrgIdToChildrenMapPair(organizationId);

			Date start = Date.valueOf(startDate);
			Date end = Date.valueOf(endDate);

			performCachingIfNotPresent(indicators, idsAndOrgIdToChildrenMapPair.first, start, end);
			switch (type) {
				case summary: {
					LinkedHashMap<String, List<String>> mapOfIdToChildren = idsAndOrgIdToChildrenMapPair.second;
					mapOfIdToChildren.forEach((id, children) -> {
						children.add(id);
						for (IndicatorItem indicator : indicators) {
							Double cacheValueSum = notificationDataSource
								.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(start, end,
									Utils.getMd5StringFromFhirPath(indicator.getFhirPath()), children);
							scoreCardItems.add(new ScoreCardItem(id, indicator.getId(), cacheValueSum.toString(),
								startDate, endDate));
						}
					});
					break;
				}
				case quarterly: {
					List<String> facilityIds = idsAndOrgIdToChildrenMapPair.first;
					List<Pair<Date, Date>> quarterDatePairList = DateUtilityHelper.getQuarterlyDates(start, end);
					for (Pair<Date, Date> pair : quarterDatePairList) {
						for (IndicatorItem indicator : indicators) {
							Double cacheValueSum = notificationDataSource
								.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(pair.first, pair.second,
									Utils.getMd5StringFromFhirPath(indicator.getFhirPath()), facilityIds);
							scoreCardItems.add(new ScoreCardItem(organizationId, indicator.getId(),
								cacheValueSum.toString(), pair.first.toString(), pair.second.toString()));
						}
					}
					break;
				}
				case weekly: {
					List<String> facilityIds = idsAndOrgIdToChildrenMapPair.first;
					List<Pair<Date, Date>> weeklyDatePairList = DateUtilityHelper.getWeeklyDates(start, end);
					for (Pair<Date, Date> pair : weeklyDatePairList) {
						for (IndicatorItem indicator : indicators) {
							Double cacheValueSum = notificationDataSource
								.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(pair.first, pair.second,
									Utils.getMd5StringFromFhirPath(indicator.getFhirPath()), facilityIds);
							scoreCardItems.add(new ScoreCardItem(organizationId, indicator.getId(),
								cacheValueSum.toString(), pair.first.toString(), pair.second.toString()));
						}
					}
					break;
				}
				case monthly: {
					List<String> facilityIds = idsAndOrgIdToChildrenMapPair.first;
					List<Pair<Date, Date>> monthlyDatePairList = DateUtilityHelper.getMonthlyDates(start, end);
					for (Pair<Date, Date> pair : monthlyDatePairList) {
						for (IndicatorItem indicator : indicators) {
							Double cacheValueSum = notificationDataSource
								.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(pair.first, pair.second,
									Utils.getMd5StringFromFhirPath(indicator.getFhirPath()), facilityIds);
							scoreCardItems.add(new ScoreCardItem(organizationId, indicator.getId(),
								cacheValueSum.toString(), pair.first.toString(), pair.second.toString()));
						}
					}
					break;
				}
				case daily: {
					List<String> facilityIds = idsAndOrgIdToChildrenMapPair.first;
					List<Pair<Date, Date>> dailyDatePairList = DateUtilityHelper.getDailyDates(start, end);
					for (Pair<Date, Date> pair : dailyDatePairList) {
						for (IndicatorItem indicator : indicators) {
							Double cacheValueSum = notificationDataSource
								.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(pair.first, pair.second,
									Utils.getMd5StringFromFhirPath(indicator.getFhirPath()), facilityIds);
							scoreCardItems.add(new ScoreCardItem(organizationId, indicator.getId(),
								cacheValueSum.toString(), pair.first.toString(), pair.second.toString()));
						}
					}
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
		return ResponseEntity.ok(scoreCardItems);
	}

	private void performCachingIfNotPresent(List<IndicatorItem> indicators, List<String> facilityIds, Date startDate, Date endDate) {
		List<String> currentIndicatorMD5List = indicators.stream().map(indicatorItem -> Utils.getMd5StringFromFhirPath(indicatorItem.getFhirPath())).collect(Collectors.toList());

		List<Date> dates = new ArrayList<>();
		List<String> presentIndicators = notificationDataSource.getIndicatorsPresent(startDate, endDate);

		List<String> existingIndicators = new ArrayList<>();
		List<String> nonExistingIndicators = new ArrayList<>();

		for (String indicator : currentIndicatorMD5List) {
			if (presentIndicators.contains(indicator)) {
				existingIndicators.add(indicator);
			} else {
				nonExistingIndicators.add(indicator);
			}
		}
		List<Date> presentDates = notificationDataSource.getDatesPresent(startDate, endDate, nonExistingIndicators.isEmpty() ? existingIndicators : nonExistingIndicators, facilityIds);

		Date start = startDate;
		Date end = Date.valueOf(endDate.toLocalDate().plusDays(1));
		while (!start.equals(end)) {
			if (!presentDates.contains(start)) {
				dates.add(start);
			}
			start = Date.valueOf(start.toLocalDate().plusDays(1));
		}

		facilityIds.forEach(facilityId -> {
			dates.forEach(date -> {
				cachingService.cacheData(facilityId, date, indicators);
			});
		});

		Date currentDate = DateUtilityHelper.getCurrentSqlDate();
		//Always cache current date data if it lies between start and end date.
		if (currentDate.getTime() >= startDate.getTime() && currentDate.getTime() <= Date.valueOf(endDate.toLocalDate().plusDays(1)).getTime()) {
			facilityIds.forEach(facilityId -> {
				cachingService.cacheData(facilityId, DateUtilityHelper.getCurrentSqlDate(), indicators);
			});
		}

	}

	public ResponseEntity<?> getLineChartByPractitionerRoleIdWithFilters(String practitionerRoleId, String startDate, String endDate, ReportType type, LinkedHashMap<String, String> filters) {
		notificationDataSource = NotificationDataSource.getInstance();
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<LineChartItemCollection> lineChartItemCollections = new ArrayList<>();
		HashMap<Integer,List<LineChartItem>> lineChartIdMap = new HashMap<Integer,List<LineChartItem>>();
		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
		try {
			List<LineChart> lineCharts = getLineChartDefinitionsItemListFromFile();
			List<String> fhirSearchList = getFhirSearchListByFilters(filters);
			List<Pair<Date, Date>> datePairList = DateUtilityHelper.getQuarterlyDates(Date.valueOf(startDate), Date.valueOf(endDate));
			switch (type) {
				case quarterly: {
					datePairList = DateUtilityHelper.getQuarterlyDates(Date.valueOf(startDate), Date.valueOf(endDate));
					break;
				}
				case weekly: {
					datePairList = DateUtilityHelper.getWeeklyDates(Date.valueOf(startDate), Date.valueOf(endDate));
					break;
				}
				case monthly: {
					datePairList = DateUtilityHelper.getMonthlyDates(Date.valueOf(startDate), Date.valueOf(endDate));
					break;
				}
				case daily: {
					datePairList = DateUtilityHelper.getDailyDates(Date.valueOf(startDate), Date.valueOf(endDate));
					break;
				}
			}
			for (Pair<Date, Date> pair : datePairList) {
				List<LineChartItemCollection> lineChartItemCollectionsForDate = ReportGeneratorFactory.INSTANCE.reportGenerator().getLineChartData(fhirClientProvider, organizationId, new DateRange(pair.first.toString(), pair.second.toString()), lineCharts, fhirSearchList);
				for(LineChartItemCollection lineChartItemCollection : lineChartItemCollectionsForDate ) {
					if(lineChartIdMap.get(lineChartItemCollection.getChartId())!=null) {
						lineChartIdMap.get(lineChartItemCollection.getChartId()).addAll(lineChartItemCollection.getValue());
					}else {
						List<LineChartItem> tempLineChartList = new ArrayList<LineChartItem>();
						tempLineChartList.addAll(lineChartItemCollection.getValue());
						lineChartIdMap.put(lineChartItemCollection.getChartId(),tempLineChartList);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
		lineChartIdMap.entrySet().parallelStream().forEach(entry ->
			lineChartItemCollections.add(new LineChartItemCollection(entry.getKey(), entry.getValue()))
		);
		return ResponseEntity.ok(lineChartItemCollections);
	}
	
	public ResponseEntity<?> getLineChartByPractitionerRoleId(String practitionerRoleId, String startDate, String endDate, ReportType type) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<LineChartItemCollection> lineChartItemCollections = new ArrayList<>();
		try {
			List<LineChart> lineCharts = getLineChartDefinitionsItemListFromFile();
			String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);

			Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = getFacilityIdsAndOrgIdToChildrenMapPair(organizationId);

			Date start = Date.valueOf(startDate);
			Date end = Date.valueOf(endDate);

			performCachingForLineChartIfNotPresent(lineCharts, idsAndOrgIdToChildrenMapPair.first, start, end);
			List<String> facilityIds = idsAndOrgIdToChildrenMapPair.first;
			List<Pair<Date, Date>> datePairList = DateUtilityHelper.getDailyDates(start, end);
			switch (type) {
				case quarterly: {
					datePairList = DateUtilityHelper.getQuarterlyDates(start, end);
					break;
				}
				case weekly: {
					datePairList = DateUtilityHelper.getWeeklyDates(start, end);		
					break;
				}
				case monthly: {
					datePairList = DateUtilityHelper.getMonthlyDates(start, end);
					break;
				}
				case daily: {
					datePairList = DateUtilityHelper.getDailyDates(start, end);
					break;
				}
			default:
				break;
			}
			
			for (LineChart lineChart : lineCharts) {
				ArrayList<LineChartItem> lineChartItems = new ArrayList<LineChartItem>();
				for (Pair<Date, Date> weekDayPair : datePairList) {
					for(LineChartItemDefinition lineChartDefinition : lineChart.getLineChartItemDefinitions()) {
						Double cacheValueSum = notificationDataSource
								.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(weekDayPair.first, weekDayPair.second,
									Utils.getMd5KeyForLineCacheMd5(lineChartDefinition.getFhirPath(),lineChartDefinition.getId(),lineChart.getId()), facilityIds);
						lineChartItems.add(new LineChartItem(lineChartDefinition.getId(),String.valueOf(cacheValueSum), weekDayPair.first.toString(), weekDayPair.second.toString()));
					}
				}
				lineChartItemCollections.add(new LineChartItemCollection(lineChart.getId(), lineChartItems));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error : Config File Not Found");
		}
		return ResponseEntity.ok(lineChartItemCollections);
	}
	
	private void performCachingForLineChartIfNotPresent(List<LineChart> lineCharts, List<String> facilityIds, Date startDate, Date endDate) {
		List<String> currentIndicatorMD5List = lineCharts.stream().flatMap(lineChart ->
				lineChart.getLineChartItemDefinitions().stream().map(lineDefinition->
						Utils.getMd5KeyForLineCacheMd5(lineDefinition.getFhirPath(), lineDefinition.getId(), lineChart.getId())
					)
				).collect(Collectors.toList());

		List<Date> dates = new ArrayList<>();
		List<String> presentIndicators = notificationDataSource.getIndicatorsPresent(startDate, endDate);

		List<String> existingIndicators = new ArrayList<>();
		List<String> nonExistingIndicators = new ArrayList<>();

		for (String indicator : currentIndicatorMD5List) {
			if (presentIndicators.contains(indicator)) {
				existingIndicators.add(indicator);
			} else {
				nonExistingIndicators.add(indicator);
			}
		}
		List<Date> presentDates = notificationDataSource.getDatesPresent(startDate, endDate, nonExistingIndicators.isEmpty() ? existingIndicators : nonExistingIndicators, facilityIds);

		Date start = startDate;
		Date end = Date.valueOf(endDate.toLocalDate().plusDays(1));
		while (!start.equals(end)) {
			if (!presentDates.contains(start)) {
				dates.add(start);
			}
			start = Date.valueOf(start.toLocalDate().plusDays(1));
		}

		facilityIds.forEach(facilityId -> {
			dates.forEach(date -> {
				cachingService.cacheDataLineChart(facilityId, date, lineCharts);
			});
		});

		Date currentDate = DateUtilityHelper.getCurrentSqlDate();
		//Always cache current date data if it lies between start and end date.
		if (currentDate.getTime() >= startDate.getTime() && currentDate.getTime() <= Date.valueOf(endDate.toLocalDate().plusDays(1)).getTime()) {
			facilityIds.forEach(facilityId -> {
				cachingService.cacheDataLineChart(facilityId, DateUtilityHelper.getCurrentSqlDate(), lineCharts);
			});
		}
	}
	
	List<String> getFhirSearchListByFilters(LinkedHashMap<String, String> filters) throws FileNotFoundException {
		List<String> fhirSearchList = new ArrayList<>();
		List<FilterItem> filterItemList = getFilterItemListFromFile();
		for (int i = 0; i <= filters.size() - 2; i += 2) {
			int keyIndex = i / 2;
			String id = filters.get("filter" + String.valueOf(keyIndex + 1) + "Id");
			String value = filters.get("filter" + String.valueOf(keyIndex + 1) + "Value");
			FilterItem filterItem = filterItemList.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
			if (filterItem != null) {
				FilterOptions filterOption = filterItem.getOptions().stream().filter(option -> option.getId().equals(value)).findFirst().orElse(null);
				if (filterOption != null) {
					fhirSearchList.add(filterOption.getFhirSearch());
				}
			}
		}
		return fhirSearchList;
	}

	List<FilterItem> getFilterItemListFromFile() throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(appProperties.getFilters_config_file()));
		return new Gson().fromJson(reader, new TypeToken<List<FilterItem>>() {
		}.getType());
	}

	List<IndicatorItem> getIndicatorItemListFromFile() throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(appProperties.getAnc_config_file()));
		return new Gson().fromJson(reader, new TypeToken<List<IndicatorItem>>() {
		}.getType());
	}

	List<PieChartDefinition> getPieChartItemDefinitionFromFile() throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(appProperties.getPie_config_file()));
		return new Gson().fromJson(reader, new TypeToken<List<PieChartDefinition>>(){
		}.getType());
	}

	List<LineChart> getLineChartDefinitionsItemListFromFile() throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(appProperties.getLine_chart_definitions_file()));
		return new Gson().fromJson(reader, new TypeToken<List<LineChart>>() {
		}.getType());
	}
	
	List<TabularItem> getTabularItemListFromFile() throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(appProperties.getTabular_config_file()));
		return new Gson().fromJson(reader, new TypeToken<List<TabularItem>>() {
		}.getType());
	}

	List<IndicatorItem> getAnalyticsItemListFromFile() throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(appProperties.getAnalytics_config_file()));
		return new Gson().fromJson(reader, new TypeToken<List<IndicatorItem>>() {
		}.getType());
	}

	ANCDailySummaryConfig getANCDailySummaryConfigFromFile() throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(appProperties.getDaily_and_summary_config_file()));
		return new Gson().fromJson(reader, ANCDailySummaryConfig.class);
	}

	public List<OrgItem> getOrganizationHierarchy(String organizationId) {
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		return ReportGeneratorFactory.INSTANCE.reportGenerator().getOrganizationHierarchy(fhirClientProvider, organizationId);
	}

	public String getOrganizationIdByPractitionerRoleId(String practitionerRoleId) {
		Bundle bundle = FhirClientAuthenticatorService.getFhirClient().search().forResource(PractitionerRole.class).where(PractitionerRole.RES_ID.exactly().identifier(practitionerRoleId)).returnBundle(Bundle.class).execute();
		if (!bundle.hasEntry()) {
			return null;
		}
		PractitionerRole practitionerRole = (PractitionerRole) bundle.getEntry().get(0).getResource();
		return practitionerRole.getOrganization().getReferenceElement().getIdPart();
	}

	public Organization getOrganizationResourceByPractitionerRoleId(String practitionerRoleId) {
		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
		if (organizationId == null) return null;
		Bundle bundle = FhirClientAuthenticatorService.getFhirClient().search().forResource(Organization.class).where(Organization.RES_ID.exactly().identifier(organizationId)).returnBundle(Bundle.class).execute();
		if (!bundle.hasEntry()) {
			return null;
		}
		return (Organization) bundle.getEntry().get(0).getResource();
	}

	public String getOrganizationIdByFacilityUID(String facilityUID) {
		Bundle organizationBundle = new Bundle();
		String queryPath = "/Organization?";
		queryPath += "identifier=" + facilityUID + "";
		FhirUtils.getBundleBySearchUrl(organizationBundle, queryPath);
		if (organizationBundle.hasEntry() && organizationBundle.getEntry().size() > 0) {
			Organization organization = (Organization) organizationBundle.getEntry().get(0).getResource();
			return organization.getIdElement().getIdPart();
		}
		return null;
	}

	public String getOrganizationIdByOrganizationName(String name) {
		Bundle organizationBundle = new Bundle();
		String queryPath = "/Organization?";
		queryPath += "name=" + name + "";
		String searchUrl = getValidURL(FhirClientAuthenticatorService.serverBase + queryPath);
		if (searchUrl == null) {
			return null;
		}
		FhirUtils.getBundleBySearchUrl(organizationBundle, searchUrl);
		if (organizationBundle.hasEntry() && organizationBundle.getEntry().size() > 0) {
			Organization organization = (Organization) organizationBundle.getEntry().get(0).getResource();
			return organization.getIdElement().getIdPart();
		}
		return null;
	}

	static String getValidURL(String invalidURLString) {
		try {
			// Convert the String and decode the URL into the URL class
			URL url = new URL(URLDecoder.decode(invalidURLString, StandardCharsets.UTF_8.toString()));

			// Use the methods of the URL class to achieve a generic solution
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			// return String or
			// uri.toURL() to return URL object
			return uri.toString();
		} catch (URISyntaxException | UnsupportedEncodingException | MalformedURLException ignored) {
			return null;
		}
	}

	private String createGroup(GroupRepresentation groupRep) {
		RealmResource realmResource = FhirClientAuthenticatorService.getKeycloak().realm(appProperties.getKeycloak_Client_Realm());
		List<GroupRepresentation> groups = realmResource.groups().groups(groupRep.getName(), 0, Integer.MAX_VALUE);
		if (!groups.isEmpty()) {
			return groups.get(0).getId();
		}
		Response response = realmResource.groups().add(groupRep);
		return CreatedResponseUtil.getCreatedId(response);
	}

	private String createResource(Resource resource, Class<? extends IBaseResource> theClass, ICriterion<?>... theCriterion) {
		IQuery<IBaseBundle> query = FhirClientAuthenticatorService.getFhirClient().search().forResource(theClass).where(theCriterion[0]);
		for (int i = 1; i < theCriterion.length; i++)
			query = query.and(theCriterion[i]);
		Bundle bundle = query.returnBundle(Bundle.class).execute();
		if (!bundle.hasEntry()) {
			MethodOutcome outcome = FhirClientAuthenticatorService.getFhirClient().update().resource(resource).execute();
			return outcome.getId().getIdPart();
		}
		return bundle.getEntry().get(0).getFullUrl().split("/")[5];
	}

	private <R extends IBaseResource> void updateResource(String keycloakId, String resourceId, Class<R> resourceClass) {
		R resource = FhirClientAuthenticatorService.getFhirClient().read().resource(resourceClass).withId(resourceId).execute();
		try {
			Method getIdentifier = resource.getClass().getMethod("getIdentifier");
			List<Identifier> identifierList = (List<Identifier>) getIdentifier.invoke(resource);
			for (Identifier identifier : identifierList) {
				if (identifier.getSystem().equals(IDENTIFIER_SYSTEM + "/KeycloakId")) {
					return;
				}
			}
			Method addIdentifier = resource.getClass().getMethod("addIdentifier");
			Identifier obj = (Identifier) addIdentifier.invoke(resource);
			obj.setSystem(IDENTIFIER_SYSTEM + "/KeycloakId");
			obj.setValue(keycloakId);
			MethodOutcome outcome = FhirClientAuthenticatorService.getFhirClient().update().resource(resource).execute();
		} catch (SecurityException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			e.printStackTrace();
		}
	}

	private String createUser(UserRepresentation userRep) {
		RealmResource realmResource = FhirClientAuthenticatorService.getKeycloak().realm(appProperties.getKeycloak_Client_Realm());
		List<UserRepresentation> users = realmResource.users().search(userRep.getUsername(), 0, Integer.MAX_VALUE);
		//if not empty, return id
		if (!users.isEmpty())
			users.get(0).getId();
		try {
			Response response = realmResource.users().create(userRep);
			return CreatedResponseUtil.getCreatedId(response);
		} catch (WebApplicationException e) {
			logger.error("Cannot create user " + userRep.getUsername() + " with groups " + userRep.getGroups() + "\n");
			e.printStackTrace();
			return null;
		}
	}

	private void createRoleIfNotExists(RoleRepresentation roleRepresentation) {
		RealmResource realmResource = FhirClientAuthenticatorService.getKeycloak().realm(appProperties.getKeycloak_Client_Realm());
		String clientId = realmResource.clients().findByClientId(appProperties.getFhir_hapi_client_id()).get(0).getId();
		if (roleWithNameExists(clientId, roleRepresentation.getName())) {
			return;
		}
		try {
			realmResource.clients().get(clientId).roles().create(roleRepresentation);
		} catch (WebApplicationException ex) {
			logger.error("cannot create role" + roleRepresentation.getName() + "\n" + ex.getStackTrace().toString());
		}
	}

	public Boolean roleWithNameExists(String clientId, String roleName) {
		RealmResource realmResource = FhirClientAuthenticatorService.getKeycloak().realm(appProperties.getKeycloak_Client_Realm());
		for (RoleRepresentation roleRepresentation : realmResource.clients().get(clientId).roles().list()) {
			if (roleRepresentation.getName().equals(roleName)) {
				return true;
			}
		}
		return false;

	}

	private void assignRole(String userId, String roleName) {
		try {
			RealmResource realmResource = FhirClientAuthenticatorService.getKeycloak().realm(appProperties.getKeycloak_Client_Realm());
			String clientId = realmResource.clients().findByClientId(appProperties.getFhir_hapi_client_id()).get(0).getId();
			RoleRepresentation saveRoleRepresentation = realmResource.clients().get(clientId).roles().get(roleName).toRepresentation();
			realmResource.users().get(userId).roles().clientLevel(clientId).add(asList(saveRoleRepresentation));
		} catch (WebApplicationException e) {
			logger.error("Cannot assign role " + roleName + " to user " + userId);
		}
	}

	private boolean bundleContainsNext(Bundle bundle) {
		for (BundleLinkComponent link : bundle.getLink()) {
			if (link.getRelation().equals("next"))
				return true;
		}
		return false;
	}

	private String getNextUrl(List<BundleLinkComponent> bundleLinks) {
		for (BundleLinkComponent link : bundleLinks) {
			if (link.getRelation().equals("next")) {
				return link.getUrl();
			}
		}
		return null;
	}
}
