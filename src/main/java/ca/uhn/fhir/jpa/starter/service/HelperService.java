package ca.uhn.fhir.jpa.starter.service;

import android.util.Pair;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.AsyncConfiguration;
import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.DashboardEnvironmentConfig;
import ca.uhn.fhir.jpa.starter.model.AnalyticComparison;
import ca.uhn.fhir.jpa.starter.model.AnalyticItem;
import ca.uhn.fhir.jpa.starter.model.ApiAsyncTaskEntity;
import ca.uhn.fhir.jpa.starter.model.CategoryItem;
import ca.uhn.fhir.jpa.starter.model.LastSyncEntity;
import ca.uhn.fhir.jpa.starter.model.MapCacheEntity;
import ca.uhn.fhir.jpa.starter.model.OrgHierarchy;
import ca.uhn.fhir.jpa.starter.model.OrgIndicatorAverageResult;
import ca.uhn.fhir.jpa.starter.model.PatientIdentifierEntity;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import ca.uhn.fhir.jpa.starter.model.ScoreCardIndicatorItem;
import ca.uhn.fhir.jpa.starter.model.ScoreCardResponseItem;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.FhirResourceTemplateHelper;
import com.iprd.fhir.utils.FhirUtils;
import com.iprd.fhir.utils.KeycloakTemplateHelper;
import com.iprd.fhir.utils.Utils;
import com.iprd.fhir.utils.Validation;
import com.iprd.report.DashboardModel;
import com.iprd.report.DataResult;
import com.iprd.report.DateRange;
import com.iprd.report.FhirClientProvider;
import com.iprd.report.OrgItem;
import com.iprd.report.OrgType;
import com.iprd.report.ReportGeneratorFactory;
import com.iprd.report.model.FilterItem;
import com.iprd.report.model.FilterOptions;
import com.iprd.report.model.data.BarChartItemDataCollection;
import com.iprd.report.model.data.BarComponentCategory;
import com.iprd.report.model.data.BarComponentData;
import com.iprd.report.model.data.LineChartItem;
import com.iprd.report.model.data.LineChartItemCollection;
import com.iprd.report.model.data.PieChartItem;
import com.iprd.report.model.data.PieChartItemDataCollection;
import com.iprd.report.model.data.ScoreCardItem;
import com.iprd.report.model.definition.ANCDailySummaryConfig;
import com.iprd.report.model.definition.BarChartDefinition;
import com.iprd.report.model.definition.BarChartItemDefinition;
import com.iprd.report.model.definition.BarComponent;
import com.iprd.report.model.definition.IndicatorItem;
import com.iprd.report.model.definition.LineChart;
import com.iprd.report.model.definition.LineChartItemDefinition;
import com.iprd.report.model.definition.PieChartCategoryDefinition;
import com.iprd.report.model.definition.PieChartDefinition;
import com.iprd.report.model.definition.TabularItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.engine.jdbc.ClobProxy;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Location.LocationPositionComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	@Autowired
	DashboardEnvironmentConfig dashboardEnvironmentConfig;
	@Autowired
	AsyncConfiguration asyncConf;
	@Autowired
	FhirClientAuthenticatorService fhirClientAuthenticatorService;
	Map<String, DashboardConfigContainer> dashboardEnvToConfigMap = new HashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(HelperService.class);
	private static final long INITIAL_DELAY = 5 * 30000L;
	private static final long FIXED_DELAY = 5 * 60000L;

	private static final long AUTH_INITIAL_DELAY = 25 * 60000L;
	private static final long AUTH_FIXED_DELAY = 50 * 60000L;
	private static final long DELAY = 2 * 60000;
	// todo - change the URLs below once resources are updated as per Implementation
	// Guide
	private static String EXTENSION_PLUSCODE_URL = "http://iprdgroup.org/fhir/Extention/location-plus-code";
	private static String IDENTIFIER_SYSTEM = "http://www.iprdgroup.com/Identifier/System";
	private static String SMS_EXTENTION_URL = "http://iprdgroup.com/Extentions/sms-sent";

	NotificationDataSource notificationDataSource;
	LinkedHashMap<String, Pair<List<String>, LinkedHashMap<String, List<String>>>> mapOfIdsAndOrgIdToChildrenMapPair;
	LinkedHashMap<String, List<OrgItem>> mapOfOrgHierarchy;
	private String lga;

	@PostConstruct
	public void init() {
		dashboardEnvToConfigMap = dashboardEnvironmentConfig.getDashboardEnvToConfigMap();
		mapOfIdsAndOrgIdToChildrenMapPair = new LinkedHashMap<String, Pair<List<String>, LinkedHashMap<String, List<String>>>>();
		mapOfOrgHierarchy = new LinkedHashMap<String, List<OrgItem>>();
	}

	public Pair<List<String>, LinkedHashMap<String, List<String>>> fetchIdsAndOrgIdToChildrenMapPair(String orgId) {
		if (!mapOfIdsAndOrgIdToChildrenMapPair.containsKey(orgId)) {
			mapOfIdsAndOrgIdToChildrenMapPair.put(orgId, getFacilityIdsAndOrgIdToChildrenMapPair(orgId));
		}
		return mapOfIdsAndOrgIdToChildrenMapPair.get(orgId);
	}

	private List<OrgItem> fetchOrgHierarchy(String orgId) {
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl(
				(GenericClient) fhirClientAuthenticatorService.getFhirClient());

		if (!mapOfOrgHierarchy.containsKey(orgId)) {
			mapOfOrgHierarchy.put(orgId, ReportGeneratorFactory.INSTANCE.reportGenerator()
					.getOrganizationHierarchy(fhirClientProvider, orgId));
		}
		return mapOfOrgHierarchy.get(orgId);
	}

	public void refreshMapForOrgId(String orgId) {
		mapOfIdsAndOrgIdToChildrenMapPair.put(orgId, getFacilityIdsAndOrgIdToChildrenMapPair(orgId));
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl(
				(GenericClient) fhirClientAuthenticatorService.getFhirClient());
		mapOfOrgHierarchy.put(orgId,
				ReportGeneratorFactory.INSTANCE.reportGenerator().getOrganizationHierarchy(fhirClientProvider, orgId));
	}

	private GroupRepresentation getKeycloakGroup(String groupName) {
		RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
				.realm(appProperties.getKeycloak_Client_Realm());
		try {
			List<GroupRepresentation> groups = realmResource.groups().groups(groupName, true, 0, 1, false);
			if (groups.size() == 0) {
				return null;
			}
			return groups.get(0).getName().equals(groupName) ? groups.get(0) : null;
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	// Recursive function below. Therefore, keeping counter to identify the depth of
	// hierarchy and update the corresponding group type. 0->facility, 1->ward,
	// 2->lga, 3->state, 4->country
	private String updateKeycloakGroupAndResource(String[] updatedDetails, String groupId, int counter) {

		// State(0), LGA(1), Ward(2), FacilityUID(3), FacilityCode(4), CountryCode(5),
		// PhoneNumber(6), FacilityName(7), FacilityLevel(8), Ownership(9), Argusoft
		// Identifier(10), Longitude(11), Latitude(12), Pluscode(13)
		String stateName = updatedDetails[0];
		String lgaName = updatedDetails[1];
		String wardName = updatedDetails[2];
		String facilityUID = updatedDetails[3];
		String facilityCode = updatedDetails[4];
		String countryCode = updatedDetails[5];
		String phoneNumber = updatedDetails[6];
		String facilityName = updatedDetails[7];
		String level = updatedDetails[8];
		String ownership = updatedDetails[9];
		String argusoftIdentifier = updatedDetails[10];
		String longitude = updatedDetails[11];
		String latitude = updatedDetails[12];
		String pluscode = updatedDetails[13];
		String country = updatedDetails[14];

		RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
				.realm(appProperties.getKeycloak_Client_Realm());
		try {
			GroupResource groupResource = realmResource.groups().group(groupId);
			GroupRepresentation group = groupResource.toRepresentation();
			Map<String, List<String>> attributes = group.getAttributes();
			String type = attributes.get("type").get(0);
			String orgId = attributes.get("organization_id").get(0);
			String locId = attributes.containsKey("location_id") ? attributes.get("location_id").get(0) : null;
			String parentId = attributes.containsKey("parent") ? attributes.get("parent").get(0) : null;
			if (type.equals("facility")) {
				updateKeycloakGroupAndResource(updatedDetails, parentId, counter + 1);
				String oldName = attributes.get("facility_name").get(0);
				String oldOwnership = attributes.get("ownership").get(0);
				String oldLevel = attributes.get("facility_level").get(0);
				if (!oldName.equals(facilityName))
					attributes.put("facility_name", Arrays.asList(facilityName));
				if (!oldOwnership.equals(ownership))
					attributes.put("ownership", Arrays.asList(ownership));
				if (!oldLevel.equals(level))
					attributes.put("facility_level", Arrays.asList(level));
				groupResource.update(group);
				updateResource(orgId, Organization.class, updatedDetails, counter);
				updateResource(locId, Location.class, updatedDetails, counter);
			} else {
				if (type.equals("country"))
					return null;
				updateKeycloakGroupAndResource(updatedDetails, parentId, counter + 1);
				String oldName = group.getName();
				if (type.equals("state") && oldName.contentEquals(stateName))
					return null;
				if (type.equals("lga") && oldName.contentEquals(lgaName))
					return null;
				if (type.equals("ward") && oldName.contentEquals(wardName))
					return null;
				group.setName(counter == 3 ? stateName : counter == 1 ? wardName : lgaName);
				groupResource.update(group);
				updateResource(orgId, Organization.class, updatedDetails, counter);
			}
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public ResponseEntity<LinkedHashMap<String, Object>> createGroups(MultipartFile file) throws IOException {

		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		List<String> invalidClinics = new ArrayList<>();
		Set<OrgHierarchy> uniqueOrgHierarchySet = new HashSet<>();

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
		String singleLine;
		int iteration = 0;
		String countryId = "", stateId = "", lgaId = "", wardId = "", facilityOrganizationId = "",
				facilityLocationId = "";
		String countryGroupId = "", stateGroupId = "", lgaGroupId = "", wardGroupId = "", facilityGroupId = "";
		while ((singleLine = bufferedReader.readLine()) != null) {
			if (iteration == 0) { // skip header of CSV file
				iteration++;
				continue;
			}
			String[] csvData = singleLine.split(",");
			iteration++;

			if (!Validation.validateClinicAndStateCsvLine(csvData)) {
				logger.warn("CSV validation failed");
				invalidClinics.add("Row length validation failed: " + singleLine);
				continue;
			}

			// State(0), LGA(1), Ward(2), FacilityUID(3), FacilityCode(4), CountryCode(5),
			// PhoneNumber(6), FacilityName(7), FacilityLevel(8), Ownership(9), Argusoft
			// Identifier(10), Longitude(11), Latitude(12), Pluscode(13), Country(14)
			String stateName = csvData[0];
			String lgaName = csvData[1];
			String wardName = csvData[2];
			String facilityUID = csvData[3];
			String facilityCode = csvData[4];
			String countryCode = csvData[5];
			String phoneNumber = csvData[6];
			String facilityName = csvData[7];
			String type = csvData[8];
			String ownership = csvData[9];
			String argusoftIdentifier = csvData[10];
			String longitude = csvData[11];
			String latitude = csvData[12];
			String pluscode = csvData[13];
			String countryName = csvData[14];

			if (facilityUID.isEmpty()) {
				invalidClinics
						.add("Invalid facilityUID: " + facilityName + "," + stateName + "," + lgaName + "," + wardName);
				continue;
			}

			String group_id = null; // Initializing to "no-value"
			GroupRepresentation keycloakGroup = getKeycloakGroup(facilityUID);
			if (null != keycloakGroup) {
				group_id = keycloakGroup.getId();
			}

			if (null != group_id) {
				updateKeycloakGroupAndResource(csvData, group_id, 0);
			} else {
				Organization country = FhirResourceTemplateHelper.country(countryName);
				GroupRepresentation countryGroupRep = KeycloakTemplateHelper.countryGroup(country.getName(),
						country.getIdElement().getIdPart());
				countryGroupId = createKeycloakGroup(countryGroupRep);
				if (countryGroupId == null) {
					invalidClinics.add("Group creation failed for state: " + facilityName + "," + countryName + ","
							+ stateName + "," + lgaName + "," + wardName);
					continue;
				}
				countryId = createResource(countryGroupId, country, Organization.class);
				if (countryId == null) {
					invalidClinics.add("Resource creation failed for state: " + facilityName + "," + countryName + ","
							+ stateName + "," + lgaName + "," + wardName);
					continue;
				}
				uniqueOrgHierarchySet.add(new OrgHierarchy(countryId, "country", null, null, null, null));
				Organization state = FhirResourceTemplateHelper.state(stateName, countryName, countryId);
				GroupRepresentation stateGroupRep = KeycloakTemplateHelper.stateGroup(state.getName(), countryGroupId,
						state.getIdElement().getIdPart());
				stateGroupId = createKeycloakGroup(stateGroupRep);
				if (stateGroupId == null) {
					invalidClinics.add("Group creation failed for state: " + facilityName + "," + stateName + ","
							+ lgaName + "," + wardName);
					continue;
				}
				stateId = createResource(stateGroupId, state, Organization.class);
				if (stateId == null) {
					invalidClinics.add("Resource creation failed for state: " + facilityName + "," + stateName + ","
							+ lgaName + "," + wardName);
					continue;
				}
				uniqueOrgHierarchySet.add(new OrgHierarchy(stateId, "state", countryId, null, null, null));
				Organization lga = FhirResourceTemplateHelper.lga(lgaName, stateName, stateId);
				GroupRepresentation lgaGroupRep = KeycloakTemplateHelper.lgaGroup(lga.getName(), stateGroupId,
						lga.getIdElement().getIdPart());
				lgaGroupId = createKeycloakGroup(lgaGroupRep);
				if (lgaGroupId == null) {
					invalidClinics.add("Group creation failed for LGA: " + facilityName + "," + stateName + ","
							+ lgaName + "," + wardName);
					continue;
				}
				lgaId = createResource(lgaGroupId, lga, Organization.class);
				if (lgaId == null) {
					invalidClinics.add("Resource creation failed for LGA: " + facilityName + "," + stateName + ","
							+ lgaName + "," + wardName);
					continue;
				}
				if (!wardName.isEmpty()) {
					Organization ward = FhirResourceTemplateHelper.ward(stateName, lgaName, wardName, lgaId);
					GroupRepresentation wardGroupRep = KeycloakTemplateHelper.wardGroup(ward.getName(), lgaGroupId,
							ward.getIdElement().getIdPart());
					wardGroupId = createKeycloakGroup(wardGroupRep);
					if (wardGroupId == null) {
						invalidClinics.add("Group creation failed for Ward: " + facilityName + "," + stateName + ","
								+ lgaName + "," + wardName);
						continue;
					}
					wardId = createResource(wardGroupId, ward, Organization.class);
					if (wardId == null) {
						invalidClinics.add("Resource creation failed for Ward: " + facilityName + "," + stateName + ","
								+ lgaName + "," + wardName);
						continue;
					}

				} else {
					// Handle Clinics that are at a LGA level eg: mobile clinics
					wardGroupId = lgaGroupId;
					wardId = lgaId;
				}
				Organization clinicOrganization = FhirResourceTemplateHelper.clinic(facilityName, facilityUID,
						facilityCode, countryCode, phoneNumber, stateName, lgaName, wardName, type, wardId,
						csvData[10]);
				Location clinicLocation = FhirResourceTemplateHelper.clinic(stateName, lgaName, wardName, facilityName,
						longitude, latitude, pluscode, clinicOrganization.getIdElement().getIdPart());
				GroupRepresentation facilityGroupRep = KeycloakTemplateHelper.facilityGroup(
						clinicOrganization.getName(), wardGroupId, clinicOrganization.getIdElement().getIdPart(),
						clinicLocation.getIdElement().getIdPart(), type, ownership, facilityUID, facilityCode,
						argusoftIdentifier);
				facilityGroupId = createKeycloakGroup(facilityGroupRep);
				if (facilityGroupId == null) {
					invalidClinics.add("Group creation failed for facility: " + facilityName + "," + stateName + ","
							+ lgaName + "," + wardName);
					continue;
				}
				facilityOrganizationId = createResource(facilityGroupId, clinicOrganization, Organization.class);
				facilityLocationId = createResource(facilityGroupId, clinicLocation, Location.class);
				if (facilityOrganizationId == null || facilityLocationId == null) {
					invalidClinics.add("Resource creation failed for Facility: " + facilityName + "," + stateName + ","
							+ lgaName + "," + wardName);
				}
			}
		}
		List<OrgHierarchy> orgHierarchyList = new ArrayList<>(uniqueOrgHierarchySet);

		OrgHierarchy countryOrgHierarchy = orgHierarchyList.stream()
				.filter(orgHierarchy -> "country".equals(orgHierarchy.getLevel())).findFirst().orElse(null);

		List<OrgHierarchy> stateOrgHierarchies = orgHierarchyList.stream()
				.filter(orgHierarchy -> !"country".equals(orgHierarchy.getLevel())).collect(Collectors.toList());

		if (countryOrgHierarchy != null) {
			for (OrgHierarchy state : stateOrgHierarchies) {
				saveOrganizationStructure(state.getOrgId(), countryOrgHierarchy.getOrgId());
			}
		}

		map.put("count", iteration);
		if (invalidClinics.size() > 0) {
			map.put("issues", invalidClinics);
		}
		map.put("uploadTaskStatus", "Completed");
		return new ResponseEntity<LinkedHashMap<String, Object>>(map, HttpStatus.OK);
	}

	public ResponseEntity<LinkedHashMap<String, Object>> createUsers(@RequestParam("file") MultipartFile file)
			throws Exception {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		List<String> practitioners = new ArrayList<>();
		List<String> invalidUsers = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
		String singleLine;
		int iteration = 0;
		String practitionerRoleId = "";
		String practitionerId = "";
		String organizationId = "";

		outer: while ((singleLine = bufferedReader.readLine()) != null) {
			if (iteration == 0) { // Skip header of CSV
				iteration++;
				continue;
			}
			String hcwData[] = singleLine.split(",");
			// firstName(0),lastName(1),email(2),countryCode(3),phoneNumber(4),gender(5),birthDate(6),keycloakUserName(7),
			// initialPassword(8),state(9),lga(10),ward(11),facilityUID(12),role(13),qualification(14),stateIdentifier(15),
			// Argusoft Identifier(16), Country(17)
			String firstName = hcwData[0];
			String lastName = hcwData[1];
			String email = hcwData[2];
			String countryCode = hcwData[3];
			String phoneNumber = hcwData[4];
			String gender = hcwData[5];
			String birthDate = hcwData[6];
			String keycloakUserName = hcwData[7];
			String initialPassword = hcwData[8];
			String state = hcwData[9];
			String lga = hcwData[10];
			String ward = hcwData[11];
			String facilityUID = hcwData[12];
			String role = hcwData[13];
			String qualification = hcwData[14];
			String stateIdentifier = hcwData[15];
			String argusoftIdentifier = hcwData[16];
			String countryName = hcwData[17];
			organizationId = getOrganizationIdByFacilityUID(facilityUID);

			String s = firstName + "," + lastName + "," + state + "," + lga + "," + ward + "," + facilityUID;
			if (facilityUID.isEmpty()) {
				map.put("FacilityUid is empty", s);
				continue;
			}

			if (!Validation.validationHcwCsvLine(hcwData)) {
				map.put("CSV length validation failed", s);
				continue;
			}

			String givenState = state;
			String givenLga = lga;
			String givenWard = ward;
			GroupRepresentation keycloakGroup;

			Map<String, Integer> levelNameOccurrences = new HashMap<>();

			Map<String, String> levels = new LinkedHashMap<>(); // Use a LinkedHashMap
			levels.put("countryName", countryName);
			levels.put("givenState", givenState);
			levels.put("givenLga", givenLga);
			
			if (!givenWard.isEmpty()) {
				levels.put("givenWard", givenWard);
			}
			// Count variable occurrences
			for (String level : levels.values()) {
				levelNameOccurrences.put(level, levelNameOccurrences.getOrDefault(level, 0) + 1);
			}

			List<String> repeatedLevelNames = new ArrayList<>();

			for (Map.Entry<String, Integer> entry : levelNameOccurrences.entrySet()) {
				if (entry.getValue() > 1) {
					repeatedLevelNames.add(entry.getKey());
				}
			}
			// Finding levels with same name
			if (!repeatedLevelNames.isEmpty()) {
				List<String> levelsWithSameName = new ArrayList<>();

				for (String equalVariable : repeatedLevelNames) {
					List<String> matchingKeys = new ArrayList<>();

					for (Map.Entry<String, String> entry : levels.entrySet()) {
						if (entry.getValue().equals(equalVariable)) {
							matchingKeys.add(entry.getKey());
						}
					}
					// Remove the first value from the list
					if (!matchingKeys.isEmpty()) {
						matchingKeys.remove(0);
					}
					levelsWithSameName.addAll(matchingKeys);
				}

				List<String> keysList = new ArrayList<>(levels.keySet());

				// Append the parent level to the level which has same name as parent
				for (String level : levelsWithSameName) {
					switch (level) {
					case "givenState":
						givenState = countryName + "_" + givenState;
						keycloakGroup = getKeycloakGroup(givenState);
						if (null != keycloakGroup && keycloakGroup.getAttributes().get("type").get(0).equals("state")) {
							state = givenState;
						} else {
							map.put("State is not found", s);
							continue outer;
						}

					case "givenLga":
						givenLga = givenState + "_" + givenLga;
						keycloakGroup = getKeycloakGroup(givenLga);
						if (null != keycloakGroup && keycloakGroup.getAttributes().get("type").get(0).equals("lga")) {
							lga = givenLga;
						} else {
							map.put("Lga is not found", s);
							continue outer;
						}

					case "givenWard":
						givenWard = givenLga + "_" + givenWard;
						keycloakGroup = getKeycloakGroup(givenWard);
						if (null != keycloakGroup && keycloakGroup.getAttributes().get("type").get(0).equals("ward")) {
							ward = givenWard;
						} else {
							map.put("Ward is not found", s);
							continue outer;
						}
					}
				}
			}

			if (!(practitioners.contains(firstName) && practitioners.contains(lastName)
					&& practitioners.contains(phoneNumber + countryCode))) {
				Practitioner practitioner = FhirResourceTemplateHelper.hcw(firstName, lastName, phoneNumber,
						countryCode, gender, birthDate, state, lga, ward, facilityUID, role, qualification,
						stateIdentifier, argusoftIdentifier);
//				practitionerId = createResource(hcw,
//					Practitioner.class,
//					Practitioner.GIVEN.matches().value(hcw.getName().get(0).getGivenAsSingleString()),
//					Practitioner.FAMILY.matches().value(hcw.getName().get(0).getFamily()),
//					Practitioner.TELECOM.exactly().systemAndValues(ContactPoint.ContactPointSystem.PHONE.toCode(), Arrays.asList(phoneNumber + countryCode))
//				); // Catch index out of bound
				practitioners.add(practitioner.getName().get(0).getFamily());
				practitioners.add(practitioner.getName().get(0).getGivenAsSingleString());
				practitioners.add(practitioner.getTelecom().get(0).getValue());
				PractitionerRole practitionerRole = FhirResourceTemplateHelper.practitionerRole(role, qualification,
						practitioner.getIdElement().getIdPart(), organizationId);
//				practitionerRoleId = createResource(practitionerRole, PractitionerRole.class, PractitionerRole.PRACTITIONER.hasId(practitionerId));
				UserRepresentation user = KeycloakTemplateHelper.user(firstName, lastName, email, keycloakUserName,
						initialPassword, phoneNumber, countryCode, practitioner.getIdElement().getIdPart(),
						practitionerRole.getIdElement().getIdPart(), role, state, lga, ward, facilityUID,
						argusoftIdentifier, countryName);
				String keycloakUserId = createKeycloakUser(user);
				if (keycloakUserId == null) {
					map.put("User not created", s);
				} else {
					RoleRepresentation KeycloakRoleRepresentation = KeycloakTemplateHelper.role(role);
					createRoleIfNotExists(KeycloakRoleRepresentation);
					assignRole(keycloakUserId, KeycloakRoleRepresentation.getName());
					practitionerId = createResource(keycloakUserId, practitioner, Practitioner.class,
							Practitioner.GIVEN.matchesExactly()
									.value(practitioner.getName().get(0).getGivenAsSingleString()),
							Practitioner.FAMILY.matchesExactly().value(practitioner.getName().get(0).getFamily()),
							Practitioner.TELECOM.exactly().systemAndValues(
									ContactPoint.ContactPointSystem.PHONE.toCode(),
									Arrays.asList(countryCode + phoneNumber)));
					if (practitionerId == null) {
						invalidUsers.add("Resource creation failed for user: " + s);
						continue;
					}
					practitionerRoleId = createResource(keycloakUserId, practitionerRole, PractitionerRole.class,
							PractitionerRole.PRACTITIONER.hasId("Practitioner/" + practitionerId));
					if (practitionerRoleId == null) {
						invalidUsers.add("Resource creation failed for user: " + s);
					}
				}
			}
		}
		map.put("UploadTaskStatus", "Completed");
		return new ResponseEntity<LinkedHashMap<String, Object>>(map, HttpStatus.OK);
	}

	public ResponseEntity<LinkedHashMap<String, Object>> createDashboardUsers(@RequestParam("file") MultipartFile file)
			throws Exception {
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

			if (!Validation.validationDashboardUserCsvLine(hcwData)) {
				invalidUsers.add("CSV length validation failed: " + hcwData[0] + " " + hcwData[1]);
				continue;
			}

			String firstName = hcwData[0];
			String lastName = hcwData[1];
			String email = hcwData[2];
			String phoneNumber = hcwData[3];
			String countryCode = hcwData[4];
			String gender = hcwData[5];
			String birthDate = hcwData[6];
			String userName = hcwData[7];
			String initialPassword = hcwData[8];
			String facilityUID = hcwData[9];
			String role = hcwData[10];
			String organizationName = hcwData[11];
			String type = hcwData[12];

			if (organizationName.isEmpty()) {
				map.put("Can not create user", firstName + " " + lastName + "," + organizationName);
				continue;
			}

			organizationId = getOrganizationIdByOrganizationNameAndType(organizationName, type);
			if (organizationId == null) {
				invalidUsers.add("Organization not found: " + firstName + " " + lastName + "," + organizationName);
				continue;
			}

			if (practitioners.contains(firstName) && practitioners.contains(lastName)
					&& practitioners.contains(email)) {
				invalidUsers.add(
						"Practitioner already exists: " + firstName + "," + lastName + "," + userName + "," + email);
				continue;
			}
			Practitioner practitioner = FhirResourceTemplateHelper.user(firstName, lastName, phoneNumber, countryCode,
					gender, birthDate, organizationName, facilityUID, type.toLowerCase());
			practitioners.add(practitioner.getName().get(0).getFamily());
			practitioners.add(practitioner.getName().get(0).getGivenAsSingleString());
			practitioners.add(email);
			PractitionerRole practitionerRole = FhirResourceTemplateHelper.practitionerRole(role, "NA",
					practitioner.getIdElement().getIdPart(), organizationId);

			UserRepresentation user = KeycloakTemplateHelper.dashboardUser(firstName, lastName, email, userName,
					initialPassword, phoneNumber, countryCode, practitioner.getIdElement().getIdPart(),
					practitionerRole.getIdElement().getIdPart(), facilityUID, role, organizationName,
					type.toLowerCase());
			String keycloakUserId = createKeycloakUser(user);
			if (keycloakUserId == null) {
				invalidUsers.add("Failed to create user: " + firstName + " " + lastName + "," + userName + "," + email);
				continue;
			}
			RoleRepresentation KeycloakRoleRepresentation = KeycloakTemplateHelper.role(role);
			createRoleIfNotExists(KeycloakRoleRepresentation);
			assignRole(keycloakUserId, KeycloakRoleRepresentation.getName());
			practitionerId = createResource(keycloakUserId, practitioner, Practitioner.class,
					Practitioner.GIVEN.matchesExactly().value(practitioner.getName().get(0).getGivenAsSingleString()),
					Practitioner.FAMILY.matchesExactly().value(practitioner.getName().get(0).getFamily()),
					Practitioner.TELECOM.exactly().systemAndValues(ContactPoint.ContactPointSystem.PHONE.toCode(),
							Arrays.asList(countryCode + phoneNumber)));
			if (practitionerId == null) {
				invalidUsers.add("Failed to create resource for user: " + firstName + " " + lastName + "," + userName
						+ "," + email);
				continue;
			}
			if (!practitionerId.equals(practitioner.getIdElement().getIdPart())) {
				// If the practitioner already exists we need to change the reference.
				// Because in while creating PractitionerRole old previous practitioner id used
				// as reference.
				practitionerRole.setId(new IdType("Practitioner", practitionerId));
			}
			practitionerRoleId = createResource(keycloakUserId, practitionerRole, PractitionerRole.class,
					PractitionerRole.PRACTITIONER.hasId("Practitioner/" + practitionerId));
			if (practitionerRoleId == null) {
				invalidUsers.add("Failed to create resource for user: " + firstName + " " + lastName + "," + userName
						+ "," + email);
			}
		}
		if (invalidUsers.size() > 0) {
			map.put("issues", invalidUsers);
		}
		map.put("taskStatus", "Completed");
		return new ResponseEntity<LinkedHashMap<String, Object>>(map, HttpStatus.OK);
	}

	public ResponseEntity<?> getTableData(Long lastUpdated) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<PatientIdentifierEntity> patientInfoResourceEntities = notificationDataSource
				.getPatientInfoResourceEntityDataBeyondLastUpdated(lastUpdated);
		return new ResponseEntity<List<PatientIdentifierEntity>>(patientInfoResourceEntities, HttpStatus.OK);
	}

	public ResponseEntity<String> computeSyncTime(String practitionerRoleId, String env) {
		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
		List<Timestamp> facilityWiseTimestamps = new ArrayList<>();
		notificationDataSource = NotificationDataSource.getInstance();

		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
				organizationId);

		// Get the current date and time
		Timestamp fiveDaysAgoTimestamp = new Timestamp(DateUtilityHelper.calculateMillisecondsRelativeToCurrentTime(5));

		List<LastSyncEntity> lastSyncData = notificationDataSource.fetchLastSyncEntitiesByOrgs(
				idsAndOrgIdToChildrenMapPair.first, env, ApiAsyncTaskEntity.Status.COMPLETED.name(),
				fiveDaysAgoTimestamp);

		// Group the data by organization ID
		Map<String, List<LastSyncEntity>> groupedData = lastSyncData.stream()
				.collect(Collectors.groupingBy(LastSyncEntity::getOrgId));

		// Sort each group by startDateTime
		groupedData.values().forEach(list -> list.sort(Comparator.comparing(LastSyncEntity::getStartDateTime)));

		// Loop through the grouped data
		for (List<LastSyncEntity> entityList : groupedData.values()) {
			if (!entityList.isEmpty()) {
				// Get the last element in the list
				LastSyncEntity lastEntity = entityList.get(entityList.size() - 1);

				// Check if endDateTime is not null
				if (lastEntity.getEndDateTime() != null) {
					// Add it to facilityWiseTimestamps
					facilityWiseTimestamps.add(lastEntity.getEndDateTime());
				}
			}
		}

		// Find the oldest timestamp
		Timestamp oldestTimestamp = facilityWiseTimestamps.stream().min(Timestamp::compareTo).orElse(null);
		if (oldestTimestamp != null) {
			return ResponseEntity.ok(Utils.calculateAndFormatTimeDifference(oldestTimestamp));
		}
		return ResponseEntity.ok("Not found");
	}

	public ResponseEntity<String> computeFacilitySyncTime(String env, String selectedOrganizationId) {
		List<String> organizationIds = new ArrayList<>();
		organizationIds.add(selectedOrganizationId);

		// Get the current date and time
		Timestamp fiveDaysAgoTimestamp = new Timestamp(DateUtilityHelper.calculateMillisecondsRelativeToCurrentTime(5));

		List<LastSyncEntity> lastSyncData = notificationDataSource.fetchLastSyncEntitiesByOrgs(organizationIds, env,
				ApiAsyncTaskEntity.Status.COMPLETED.name(), fiveDaysAgoTimestamp);

		// Sort by startDateTime
		lastSyncData.sort(Comparator.comparing(LastSyncEntity::getStartDateTime));
		if (!lastSyncData.isEmpty()) {
			LastSyncEntity lastEntity = lastSyncData.get(lastSyncData.size() - 1);
			if (lastEntity != null && lastEntity.getEndDateTime() != null) {
				return ResponseEntity.ok(Utils.calculateAndFormatTimeDifference(lastEntity.getEndDateTime()));
			}
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
	}

	public List<GroupRepresentation> getGroupsByUser(String userId) {
		RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
				.realm(appProperties.getKeycloak_Client_Realm());
		List<GroupRepresentation> groups = realmResource.users().get(userId).groups(0,
				appProperties.getKeycloak_max_group_count(), false);
		return groups;
	}

//	public ResponseEntity<List<Map<String, String>>> getAncMetaDataByOrganizationId(String organizationId, String startDate, String endDate) {
//		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
//		List<Map<String, String>> ancMetaData = ReportGeneratorFactory.INSTANCE.reportGenerator().getAncMetaDataByOrganizationId(fhirClientProvider, new DateRange(startDate, endDate), organizationId);
//		return ResponseEntity.ok(ancMetaData);
//	}

//	public ResponseEntity<?> getAncDailySummaryData(String organizationId, String startDate, String endDate, LinkedHashMap<String, String> filters,String env) {
//		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
//		List<String> fhirSearchList = getFhirSearchListByFilters(filters,env);
//		ANCDailySummaryConfig ancDailySummaryConfig = getANCDailySummaryConfigFromFile(env);
//		DataResult dataResult = ReportGeneratorFactory.INSTANCE.reportGenerator().getAncDailySummaryData(fhirClientProvider, new DateRange(startDate, endDate), organizationId, ancDailySummaryConfig, fhirSearchList);
//		return ResponseEntity.ok(dataResult);
//	}

	public void saveInAsyncTable(DataResult dataResult, String id) {

		byte[] summaryResult = dataResult.getSummaryResult();
		List<Map<String, String>> dailyResult = dataResult.getDailyResult();
		String base64SummaryResult = Base64.getEncoder().encodeToString(summaryResult);
		String dailyResultJsonString = new Gson().toJson(dailyResult); // SPlit into two , one arraylist and one
																		// base64Encoded string.

		try {
			ArrayList asyncData = datasource.fetchStatus(id);
			ApiAsyncTaskEntity asyncRecord = (ApiAsyncTaskEntity) asyncData.get(0);
			asyncRecord.setStatus(ApiAsyncTaskEntity.Status.COMPLETED.name());
			asyncRecord.setDailyResult(ClobProxy.generateProxy(dailyResultJsonString));
			asyncRecord.setSummaryResult(ClobProxy.generateProxy(base64SummaryResult));
			asyncRecord.setLastUpdated(Date.valueOf(LocalDate.now()));
			datasource.update(asyncRecord);
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}

	}

	public String convertClobToString(Clob input) throws IOException, SQLException {
		Reader reader = input.getCharacterStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(reader, writer);
		return writer.toString();
	}

	private List<String> getCategoriesFromAncDailySummaryConfig(String env) {
		List<ANCDailySummaryConfig> ancDailySummaryConfig = getANCDailySummaryConfigFromFile(env);
		return ancDailySummaryConfig.stream().map(ANCDailySummaryConfig::getCategoryId).collect(Collectors.toList());
	}

	public Map<String, String> processCategories(String organizationId, String startDate, String endDate, String env,
			LinkedHashMap<String, String> filters, boolean isApiRequest) {
		Map<String, String> categoryWithHashCodes = new HashMap<>();
		List<String> categories = getCategoriesFromAncDailySummaryConfig(env);
		List<String> hashCodes = new ArrayList<>(Collections.emptyList());
		List<String> hashCodesToBeProcessed = new ArrayList<>(Collections.emptyList());
		String concatenatedFilterValues = filters.entrySet().stream()
			.filter(entry -> entry.getKey().endsWith("Value"))
			.map(Map.Entry::getValue)
			.collect(Collectors.joining());

		List<ANCDailySummaryConfig> ancDailySummaryConfig = getANCDailySummaryConfigFromFile(env);
		String hashOfFormattedId = "";

		for (String category : categories) {
			hashOfFormattedId = organizationId + startDate + endDate + category + env + concatenatedFilterValues;
			categoryWithHashCodes.put(category, hashOfFormattedId);
			ArrayList<ApiAsyncTaskEntity> fetchAsyncData = datasource.fetchStatus(hashOfFormattedId);
			hashCodes.add(hashOfFormattedId);

			if (fetchAsyncData == null || fetchAsyncData.isEmpty()) {
				try {
					ApiAsyncTaskEntity apiAsyncTaskEntity = new ApiAsyncTaskEntity(hashOfFormattedId, ApiAsyncTaskEntity.Status.PROCESSING.name(), null, null, Date.valueOf(LocalDate.now()));
					datasource.insert(apiAsyncTaskEntity);
					// Add the hash code upon successful insertion
					hashCodesToBeProcessed.add(hashOfFormattedId);
				} catch (Exception e) {
					logger.warn(ExceptionUtils.getStackTrace(e));
				}
			}

			if(categories.indexOf(category) == (categories.size()-1)) {
				if(!isApiRequest || !hashCodesToBeProcessed.isEmpty()){
					saveQueryResultAndHandleException(organizationId, startDate, endDate, filters, hashCodes, env, ancDailySummaryConfig);
				}
				else {
					if(hashCodesToBeProcessed.isEmpty() && fetchAsyncData != null && !fetchAsyncData.isEmpty()){
							Date lastUpdated = fetchAsyncData.get(0).getLastUpdated();
							Date currentDate = Date.valueOf(LocalDate.now());
							// Updates the record when start date is not part of current month but end date is part of current month
							if(Date.valueOf(endDate).toLocalDate().compareTo(LocalDate.now().withDayOfMonth(1)) >= 0 && Date.valueOf(startDate).toLocalDate().compareTo(LocalDate.now().withDayOfMonth(1)) < 0 && !lastUpdated.equals(currentDate)){
								ArrayList<ApiAsyncTaskEntity> entitiesToUpdate = datasource.fetchApiAsyncTaskEntityList(hashCodes);
								for (ApiAsyncTaskEntity asyncRecord : entitiesToUpdate) {
									asyncRecord.setStatus(ApiAsyncTaskEntity.Status.PROCESSING.name());
								}
								datasource.updateObjects(entitiesToUpdate);
								saveQueryResultAndHandleException(organizationId, startDate, endDate, filters, hashCodes, env, ancDailySummaryConfig);
							}
						}
					}
				}
			}
		return categoryWithHashCodes;
	}

	private void saveQueryResultAndHandleException(String organizationId, String startDate, String endDate,
			LinkedHashMap<String, String> filters, List<String> hashcodes, String env,
			List<ANCDailySummaryConfig> ancDailySummaryConfig) {
		ThreadPoolTaskExecutor executor =  asyncConf.asyncExecutor();
		executor.submit(() -> {
			try {
				saveQueryResult(organizationId, startDate, endDate, filters, hashcodes, env, ancDailySummaryConfig);
			} catch (FileNotFoundException e) {
				// Handle the exception, e.g., log it or take appropriate action.
				e.printStackTrace();
			}
		});
		}


	public ResponseEntity<?> getAsyncData(Map<String,String> categoryWithHashCodes) throws SQLException, IOException {
		List<DataResultJava> dataResult = new ArrayList<>();
		for(Map.Entry<String,String> item : categoryWithHashCodes.entrySet()) {
			ArrayList<ApiAsyncTaskEntity> asyncData = datasource.fetchStatus(item.getValue());
			if (asyncData == null) return ResponseEntity.ok("Searching in Progress");
			ApiAsyncTaskEntity asyncRecord = asyncData.get(0);
			if (asyncRecord.getSummaryResult() == null || asyncRecord.getStatus() == ApiAsyncTaskEntity.Status.PROCESSING.name()) return ResponseEntity.ok("Searching in Progress");
			String dailyResultInString = convertClobToString(asyncRecord.getDailyResult());
			String summaryResultInString = convertClobToString(asyncRecord.getSummaryResult());
			List<Map<String, String>> dailyResult= mapper.readValue(dailyResultInString, new TypeReference<List<Map<String, String>>>() {});
			dataResult.add(new DataResultJava(item.getKey(),summaryResultInString, dailyResult));
		}
		return  ResponseEntity.ok(dataResult);
	}

	public void saveQueryResult(String organizationId, String startDate, String endDate,
			LinkedHashMap<String, String> filters, List<String> hashcodes, String env,
			List<ANCDailySummaryConfig> ancDailySummaryConfig) throws FileNotFoundException {
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl(
				(GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<String> fhirSearchList = getFhirSearchListByFilters(filters, env);
		logger.warn("Calling details page function saveQueryResult. StartDate: {} EndDate: {}", startDate, endDate);
		Long start3 = System.nanoTime();
		List<DataResult> dataResult = ReportGeneratorFactory.INSTANCE.reportGenerator().getAncDailySummaryData(
				fhirClientProvider, new DateRange(startDate, endDate), organizationId, ancDailySummaryConfig,
				fhirSearchList);
		for (String hashcode : hashcodes) {
			saveInAsyncTable(dataResult.get(hashcodes.indexOf(hashcode)), hashcode);
		}
		Long end3 = System.nanoTime();
		Double diff3 = ((end3 - start3) / 1e9); // Convert nanoseconds to seconds
		logger.warn("details page function saveQueryResult ended StartDate: {} EndDate: {} timetaken: {}", startDate,
				endDate, diff3);
	}

	// @Scheduled(fixedDelay = 300000)
	@Scheduled(cron = "0 0 23 * * *")
	public void refreshSyncForCurrentMonth() {
		try {
			List<String> orgIdsForCaching = appProperties.getOrganization_ids_for_caching();
			List<String> envsForCaching = appProperties.getEnvs_for_caching();
			for (String orgId : orgIdsForCaching) {
				for (String envs : envsForCaching) {
					Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
							orgId);
					cacheDashboardData(idsAndOrgIdToChildrenMapPair.first,
							String.valueOf(LocalDate.now().minusDays(31)), String.valueOf(LocalDate.now()), envs);
				}
			}
		} catch (Exception e) {
			logger.warn("Caching task failed " + ExceptionUtils.getStackTrace(e));
		}
	}

	@Scheduled(cron = "0 30 23 * * *")
	public void refreshAsyncTaskStatusTable() {
		try {
			List<String> orgIdsForCaching = appProperties.getOrganization_ids_for_caching();
			List<String> envsForCaching = appProperties.getEnvs_for_caching();
			LinkedHashMap<String, String> emptyMap = new LinkedHashMap<>();
			for (String orgId : orgIdsForCaching) {
				for (String env : envsForCaching) {
					logger.warn("Caching for details page started ");
					Date start = Date.valueOf(Date.valueOf(LocalDate.now()).toLocalDate().minusYears(2));
					Date end = Date.valueOf(LocalDate.now());
					List<Pair<Date, Date>> quarterDatePairList = DateUtilityHelper.getQuarterlyDates(start, end);
					// Remove the last pair which is current quarter pair
					if (!quarterDatePairList.isEmpty()) {
						quarterDatePairList.remove(quarterDatePairList.size() - 1);
					}
					for (Pair<Date, Date> pair : quarterDatePairList) {
						processCategories(orgId, pair.first.toString(), pair.second.toString(), env, emptyMap, false);
					}
					logger.warn("refreshAsyncTaskStatusTable quarterDatePairList " + quarterDatePairList);
				}
			}
			logger.warn("Caching for details page completed ");
		} catch (JDBCException e) {
			logger.warn(" RefreshAsyncTaskStatusTable Caching task failed " + ExceptionUtils.getStackTrace(e));
		}
	}

	@Scheduled(cron = "0 0 23 1 * ?")
	public void cleanupLastSyncStatusTable() {
		// Get the current date and time
		notificationDataSource = NotificationDataSource.getInstance();
		notificationDataSource.clearLastSyncStatusTable(
				new Timestamp(DateUtilityHelper.calculateMillisecondsRelativeToCurrentTime(30)));
	}

	public Bundle getEncountersBelowLocation(String locationId) {
		List<String> locationIdsList = new ArrayList<>();
		locationIdsList.add(locationId);
		ListIterator<String> locationIdIterator = locationIdsList.listIterator();

		while (locationIdIterator.hasNext()) {
			List<String> tempList = new ArrayList<>();
			getOrganizationsPartOf(tempList, FhirClientAuthenticatorService.serverBase + "/Location?partof=Location/"
					+ locationIdIterator.next() + "&_elements=id");
			tempList.forEach(item -> {
				locationIdIterator.add(item);
				locationIdIterator.previous();
			});
		}
		Bundle batchBundle = generateBatchBundle("/Encounter?location=" + String.join(",", locationIdsList));
		Bundle responseBundle = fhirClientAuthenticatorService.getFhirClient().transaction().withBundle(batchBundle)
				.prettyPrint().encodedJson().execute();
		return responseBundle;
	}

	private Pair<List<String>, LinkedHashMap<String, List<String>>> getFacilityIdsAndOrgIdToChildrenMapPair(
			String orgId) {
		List<String> facilityOrgIdList = new ArrayList<>();
		List<String> orgIdList = new ArrayList<>();
		orgIdList.add(orgId);
		ListIterator<String> orgIdIterator = orgIdList.listIterator();

		LinkedHashMap<String, List<String>> mapOfIdToChildren = new LinkedHashMap<>();

		while (orgIdIterator.hasNext()) {
			String tempOrgId = orgIdIterator.next();
			List<String> childrenList = new ArrayList<>();
			getOrganizationsPartOf(childrenList, FhirClientAuthenticatorService.serverBase
					+ "/Organization?partof=Organization/" + tempOrgId + "&_elements=id");
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
			getOrganizationsPartOf(childrenList, FhirClientAuthenticatorService.serverBase
					+ "/Organization?partof=Organization/" + tempOrgId + "&_elements=id");
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
			getOrganizationsPartOf(childrenList, FhirClientAuthenticatorService.serverBase
					+ "/Organization?partof=Organization/" + tempOrgId + "&_elements=id");
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
		Bundle searchBundle = fhirClientAuthenticatorService.getFhirClient().search().byUrl(url)
				.returnBundle(Bundle.class).execute();
		idsList.addAll(searchBundle.getEntry().stream().map(r -> r.getResource().getIdElement().getIdPart())
				.collect(Collectors.toList()));
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

	public ResponseEntity<?> getIndicators(String env) {
		try {
			List<ScoreCardIndicatorItem> indicators = getIndicatorItemListFromFile(env);
			return ResponseEntity.ok(indicators);
		} catch (NullPointerException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return ResponseEntity.ok("Error : ScoreCard Config File Not Found");
		}
	}

	public ResponseEntity<?> getCategories(String env) {
		try {
			CategoryItem categoryItem = getCategoriesFromFile(env);
			return ResponseEntity.ok(categoryItem);
		} catch (NullPointerException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return ResponseEntity.ok("Error : Category Config File Not Found");
		}
	}

	public ResponseEntity<?> getEnvironmentOptions() {
		try {
			List<String> environmentOptions = new ArrayList<>(dashboardEnvToConfigMap.keySet());
			return ResponseEntity.ok(environmentOptions);
		} catch (NullPointerException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return ResponseEntity.ok("Error: Environment Config File Not Found");
		}
	}

	public ResponseEntity<?> getBarChartDefinition(String env) {
		try {
			List<BarChartDefinition> barChartDefinition = getBarChartItemListFromFile(env);
			return ResponseEntity.ok(barChartDefinition);
		} catch (NullPointerException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return ResponseEntity.ok("Error :Bar Config File Not Found");
		}
	}

	public ResponseEntity<?> getLineChartDefinitions(String env) {
		try {
			List<LineChart> lineCharts = getLineChartDefinitionsItemListFromFile(env);
			return ResponseEntity.ok(lineCharts);
		} catch (NullPointerException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return ResponseEntity.ok("Error :Line Config File Not Found");
		}
	}

	public ResponseEntity<?> getTabularIndicators(String env) {
		try {
			List<TabularItem> indicators = getTabularItemListFromFile(env);
			return ResponseEntity.ok(indicators);
		} catch (NullPointerException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return ResponseEntity.ok("Error : Tabular Config File Not Found");
		}
	}

	public ResponseEntity<?> getPieChartDefinition(String env) {
		try {
			List<PieChartDefinition> pieChartIndicators = getPieChartItemDefinitionFromFile(env);
			return ResponseEntity.ok(pieChartIndicators);
		} catch (NullPointerException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return ResponseEntity.ok("Error :Pie Chart Config File Not Found");
		}
	}

	public AnalyticItem getPatientCount(String practitionerRoleId) {
		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
		int patientCount = 0;

		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
				organizationId);
		for (String orgId : idsAndOrgIdToChildrenMapPair.first) {
			patientCount += fhirClientAuthenticatorService.getFhirClient().search()
					.byUrl("Patient?_has:Encounter:patient:service-provider=" + orgId + "&_count=0")
					.returnBundle(Bundle.class).execute().getTotal();
		}
		return new AnalyticItem("Total number of Patients", String.valueOf(patientCount), null);
	}

	public ResponseEntity<?> getFilters(String env) {
		try {
			List<FilterItem> filters = dashboardEnvToConfigMap.get(env).getFilterItems();
			return ResponseEntity.ok(filters);
		} catch (NullPointerException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return ResponseEntity.ok("Error: Config File Not Found");
		}
	}

	public List<OrgItem> getOrganizationsByPractitionerRoleId(String practitionerRoleId) {
		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
		return fetchOrgHierarchy(organizationId);
	}

	public ResponseEntity<?> getPieChartDataByPractitionerRoleId(String startDate, String endDate,
			LinkedHashMap<String, String> filters, String env, String lga) {
		notificationDataSource = NotificationDataSource.getInstance();

		List<PieChartDefinition> pieChartDefinitions = getPieChartItemDefinitionFromFile(env);
		String organizationId = lga;

		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
				organizationId);
		List<String> fhirSearchList = getFhirSearchListByFilters(filters, env);
		Date start = Date.valueOf(startDate);
		Date end = Date.valueOf(endDate);
		performCachingForPieChartData(pieChartDefinitions, idsAndOrgIdToChildrenMapPair.first, start, end,
				fhirSearchList);
		List<String> facilityIds = idsAndOrgIdToChildrenMapPair.first;
		logger.warn("leng: facility ids " + facilityIds.size());
		List<PieChartItemDataCollection> pieChartItemDataCollection = new ArrayList<>();
		for (PieChartDefinition pieChartDefinition : pieChartDefinitions) {
			List<PieChartItem> pieChartItems = new ArrayList<>();
			for (PieChartCategoryDefinition pieChartItem : pieChartDefinition.getItem()) {
				String key = pieChartItem.getFhirPath().getExpression() + String.join(",", fhirSearchList)
						+ pieChartDefinition.getCategoryId();
				Double cacheValueSum = getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(
						pieChartItem.getFhirPath().getTransformServer(), start, end,
						Utils.md5Bytes(key.getBytes(StandardCharsets.UTF_8)), facilityIds);
				pieChartItems.add(new PieChartItem(pieChartItem.getId(), organizationId, pieChartItem.getHeader(),
						pieChartItem.getName(), String.valueOf(cacheValueSum), pieChartItem.getChartId(),
						pieChartItem.getColorHex()));
			}
			pieChartItemDataCollection
					.add(new PieChartItemDataCollection(pieChartDefinition.getCategoryId(), pieChartItems));
		}
		return ResponseEntity.ok(pieChartItemDataCollection);
	}

	public List<AnalyticItem> getMaternalAnalytics(String organizationId, String env) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<AnalyticItem> analyticItems = new ArrayList<>();
		List<IndicatorItem> analyticsItemListFromFile = getAnalyticsItemListFromFile(env);
		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
				organizationId);

		Pair<Date, Date> currentWeek = DateUtilityHelper.getCurrentWeekDates();
		Pair<Date, Date> prevWeek = DateUtilityHelper.getPrevWeekDates();

		performCachingIfNotPresent(analyticsItemListFromFile, idsAndOrgIdToChildrenMapPair.first, prevWeek.first,
				currentWeek.second, new ArrayList<String>());
		for (IndicatorItem indicator : analyticsItemListFromFile) {
			Double currentWeekCacheValueSum = getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(
					indicator.getFhirPath().getTransformServer(), currentWeek.first, currentWeek.second,
					Utils.md5Bytes(indicator.getFhirPath().getExpression().getBytes(StandardCharsets.UTF_8)),
					idsAndOrgIdToChildrenMapPair.first);

			Double prevWeekCacheValueSum = getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(
					indicator.getFhirPath().getTransformServer(), prevWeek.first, prevWeek.second,
					Utils.md5Bytes(indicator.getFhirPath().getExpression().getBytes(StandardCharsets.UTF_8)),
					idsAndOrgIdToChildrenMapPair.first);

			AnalyticComparison comparisonValue = (currentWeekCacheValueSum > prevWeekCacheValueSum)
					? AnalyticComparison.POSITIVE_UP
					: AnalyticComparison.NEGATIVE_DOWN;

			analyticItems.add(new AnalyticItem(indicator.getDescription(),
					String.valueOf(currentWeekCacheValueSum.intValue()), comparisonValue));
		}
		return analyticItems;
	}

	private void performCachingForTabularData(List<TabularItem> indicators, List<String> facilityIds, Date startDate,
			Date endDate, List<String> fhirSearchList) {
		String filterString = String.join(",", fhirSearchList);
		notificationDataSource = NotificationDataSource.getInstance();
		List<String> currentIndicatorMD5List = indicators.stream()
				.map(indicatorItem -> Utils.md5Bytes(
						(indicatorItem.getFhirPath().getExpression() + filterString).getBytes(StandardCharsets.UTF_8)))
				.collect(Collectors.toList());

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
		List<Date> presentDates = notificationDataSource.getDatesPresent(startDate, endDate,
				nonExistingIndicators.isEmpty() ? existingIndicators : nonExistingIndicators, facilityIds);
		Date start = startDate;
		Date end = Date.valueOf(endDate.toLocalDate().plusDays(1));
		while (!start.equals(end)) {
			if (!presentDates.contains(start)) {
				dates.add(start);
			}
			start = Date.valueOf(start.toLocalDate().plusDays(1));
		}
		logger.warn("Tabular Cache present days: " + presentDates.toString() + "Cache existing indicators: "
				+ existingIndicators.toString() + "Cache missing days: " + dates.toString()
				+ "Cache missing indicators days: " + nonExistingIndicators.toString());

		Date currentDate = new Date(System.currentTimeMillis());
		boolean currentDateNotInDatesList = Utils.noneMatchDates(dates, currentDate);

		for (int count = 0; count < facilityIds.size(); count++) {
			String facilityId = facilityIds.get(count);
			final int finalcount = count;
			dates.forEach(date -> {
				cachingService.cacheTabularData(facilityId, date, indicators, finalcount, filterString);
			});

			// Always cache current date data if it lies between start and end date.
			if (currentDateNotInDatesList && currentDate.getTime() >= startDate.getTime()
					&& currentDate.getTime() <= Date.valueOf(endDate.toLocalDate().plusDays(1)).getTime()) {
				cachingService.cacheTabularData(facilityId, DateUtilityHelper.getCurrentSqlDate(), indicators, 0,
						filterString);
			}
		}
	}

	private void performCachingForPieChartData(List<PieChartDefinition> pieChartDefinitions, List<String> facilityIds,
			Date startDate, Date endDate, List<String> fhirSearchList) {
		String filterString = String.join(",", fhirSearchList);
		List<String> currentIndicatorMd5List = pieChartDefinitions.stream().flatMap(pieChartDefinitionCategory -> {
			if (pieChartDefinitionCategory != null) {
				return pieChartDefinitionCategory.getItem().stream().map(pieChartDefinitionItem -> Utils
						.md5Bytes((pieChartDefinitionItem.getFhirPath().getExpression() + filterString
								+ pieChartDefinitionCategory.getCategoryId()).getBytes(StandardCharsets.UTF_8)));
			} else {
				return Stream.empty();
			}
		}).collect(Collectors.toList());
		List<Date> dates = new ArrayList<>();
		List<String> presentIndicators = notificationDataSource.getIndicatorsPresent(startDate, endDate);

		List<String> existingIndicators = new ArrayList<>();
		List<String> nonExistingIndicators = new ArrayList<>();

		for (String indicator : currentIndicatorMd5List) {
			if (presentIndicators.contains(indicator)) {
				existingIndicators.add(indicator);
			} else {
				nonExistingIndicators.add(indicator);
			}
		}
		List<Date> presentDates = notificationDataSource.getDatesPresent(startDate, endDate,
				nonExistingIndicators.isEmpty() ? existingIndicators : nonExistingIndicators, facilityIds);

		Date start = startDate;
		Date end = Date.valueOf(endDate.toLocalDate().plusDays(1));
		while (!start.equals(end)) {
			if (!presentDates.contains(start)) {
				dates.add(start);
			}
			start = Date.valueOf(start.toLocalDate().plusDays(1));
		}
		logger.warn("Pie Cache present days: " + presentDates.toString() + "Cache existing indicators: "
				+ existingIndicators.toString() + "Cache missing days: " + dates.toString()
				+ "Cache missing indicators days: " + nonExistingIndicators.toString());

		Date currentDate = new Date(System.currentTimeMillis());
		boolean currentDateNotInDatesList = Utils.noneMatchDates(dates, currentDate);

		for (int count = 0; count < facilityIds.size(); count++) {
			String facilityId = facilityIds.get(count);
			final int finalcount = count;
			dates.forEach(date -> {
				cachingService.cachePieChartData(facilityId, date, pieChartDefinitions, finalcount, filterString);
			});

			// Always cache current date data if it lies between start and end date.
			if (currentDateNotInDatesList && currentDate.getTime() >= startDate.getTime()
					&& currentDate.getTime() <= Date.valueOf(endDate.toLocalDate().plusDays(1)).getTime()) {
				cachingService.cachePieChartData(facilityId, DateUtilityHelper.getCurrentSqlDate(), pieChartDefinitions,
						0, filterString);
			}
		}

	}

	public ResponseEntity<?> getTabularDataByPractitionerRoleId(String startDate, String endDate,
			LinkedHashMap<String, String> filters, String env, String lga) {
		List<ScoreCardItem> scoreCardItems = new ArrayList<>();
		List<TabularItem> tabularItemList = getTabularItemListFromFile(env);
		List<String> fhirSearchList = getFhirSearchListByFilters(filters, env);

		String organizationId = lga;

		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
				organizationId);
		Date start = Date.valueOf(startDate);
		Date end = Date.valueOf(endDate);
		notificationDataSource = NotificationDataSource.getInstance();
		performCachingForTabularData(tabularItemList, idsAndOrgIdToChildrenMapPair.first, start, end, fhirSearchList);
		for (String orgId : idsAndOrgIdToChildrenMapPair.first) {
			for (TabularItem indicator : tabularItemList) {
				String key = indicator.getFhirPath().getExpression() + String.join(",", fhirSearchList);
				Double cacheValue = 0.0;
				if (indicator.getFhirPath().getTransformServer() == null) {
					cacheValue = notificationDataSource.getCacheValueSumByDateRangeIndicatorAndOrgId(start, end,
							Utils.md5Bytes(key.getBytes(StandardCharsets.UTF_8)), orgId);
				} else {
					try {
						cacheValue = (Double) notificationDataSource.getClass()
								.getMethod(indicator.getFhirPath().getTransformServer(), Date.class, Date.class,
										String.class, String.class)
								.invoke(notificationDataSource, start, end,
										Utils.md5Bytes(key.getBytes(StandardCharsets.UTF_8)), orgId);
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						logger.warn(ExceptionUtils.getStackTrace(e));
					}
				}
				scoreCardItems
						.add(new ScoreCardItem(orgId, indicator.getId(), cacheValue.toString(), startDate, endDate));
			}
		}
		return ResponseEntity.ok(scoreCardItems);
	}

	public Double calculateAverage(List<String> targetOrgIds, List<OrgIndicatorAverageResult> fromTable,
			String hashedId, String transformServer) {
		List<OrgIndicatorAverageResult> filteredList = fromTable.stream().filter(cacheEntity -> targetOrgIds
				.contains(cacheEntity.getOrgId()) && hashedId.contains(cacheEntity.getIndicator())
				&& (!transformServer.equals("getCacheValueAverageWithoutZeroByDateRangeIndicatorAndMultipleOrgId")
						|| cacheEntity.getAverageValue() > 0.0))
				.collect(Collectors.toList());

		if (filteredList.isEmpty()) {
			return 0.0;
		}

		// Calculate the sum of values
		double sum = filteredList.stream().mapToDouble(OrgIndicatorAverageResult::getAverageValue).sum();

		if (sum == 0.0) {
			return 0.0;
		}

		// Calculate the average
		double average = sum / filteredList.size();

		return average;
	}

	public List<String> getFacilitiesForOrganization(OrgHierarchy targetItem, List<OrgHierarchy> orgHierarchyList) {

		List<String> facilityOrgIds = new ArrayList<>();

		String targetLevel = targetItem.getLevel();

		if ("facility".equals(targetLevel)) {
			facilityOrgIds.add(targetItem.getOrgId());
		} else {
			// Create a mapping of targetLevel to parent level field
			Map<String, Function<OrgHierarchy, String>> parentLevelFieldMap = new HashMap<>();
			parentLevelFieldMap.put("ward", OrgHierarchy::getWardParent);
			parentLevelFieldMap.put("lga", OrgHierarchy::getLgaParent);
			parentLevelFieldMap.put("state", OrgHierarchy::getStateParent);
			parentLevelFieldMap.put("country", OrgHierarchy::getCountryParent);

			Function<OrgHierarchy, String> targetFunction = parentLevelFieldMap.get(targetLevel);

			if (targetFunction == null) {
				return Collections.emptyList(); // Handle the case where targetFunction is null
			}

			String targetValue = targetItem.getOrgId();

			facilityOrgIds
					.addAll(orgHierarchyList.stream().filter(orgHierarchy -> "facility".equals(orgHierarchy.getLevel()))
							.filter(orgHierarchy -> Objects.equals(targetValue, targetFunction.apply(orgHierarchy)))
							.map(OrgHierarchy::getOrgId).collect(Collectors.toList()));
		}

		return facilityOrgIds;
	}

	@Async("asyncTaskExecutor")
	public void saveOrganizationStructure(String organizationId, String parentId) {
		List<OrgItem> orgItems = fetchOrgHierarchy(organizationId);
		ArrayList<OrgHierarchy> orgHierarchyList = new ArrayList<>();
		List<List<OrgHierarchy>> failedChunks = new ArrayList<>();
		AtomicBoolean exceptionOccurred = new AtomicBoolean(false); // Flag to track exceptions
		logger.warn("Data insertion into 'organization Hierarchy' table has started for parentId " + parentId
				+ " organizationId " + organizationId);
		if (orgItems.isEmpty()) {
			logger.warn("Organization Hierarchy is not found in FHIR server for this OrganizationId " + organizationId);
			return;
		}

		// Start the recursive traversal from the top level
		for (OrgItem orgItem : orgItems) {
			OrgHierarchy parentOrgItem = datasource.getOrganizationalHierarchyItem(parentId);
			if (parentOrgItem != null) {
				String countryParent = parentOrgItem.getCountryParent();
				String stateParent = parentOrgItem.getStateParent();
				String lgaParent = parentOrgItem.getLgaParent();
				String wardParent = parentOrgItem.getWardParent();

				switch (parentOrgItem.getLevel()) {
				case "country":
					countryParent = parentOrgItem.getOrgId();
					break;
				case "state":
					stateParent = parentOrgItem.getOrgId();
					break;
				case "lga":
					lgaParent = parentOrgItem.getOrgId();
					break;
				case "ward":
					wardParent = parentOrgItem.getOrgId();
					break;
				// Additional cases...
				default:
					logger.warn("Provided parentId " + parentId + "is not valid");
					return;
				}
				Pair<Boolean, ArrayList<OrgHierarchy>> isValid = createOrgHierarchyRecursive(orgItem, countryParent,
						stateParent, lgaParent, wardParent, orgHierarchyList);
				if (isValid.first) {
					List<OrgHierarchy> dataToInsert = isValid.second;
					int chunkSize = 100;
					CountDownLatch latch = new CountDownLatch(
							(int) Math.ceil((double) dataToInsert.size() / chunkSize));

					ThreadPoolTaskExecutor executorDb = asyncConf.asyncExecutor();
					for (int i = 0; i < dataToInsert.size(); i += chunkSize) {
						List<OrgHierarchy> chunk = dataToInsert.subList(i,
								Math.min(i + chunkSize, dataToInsert.size()));

						executorDb.submit(() -> {
							try {
								datasource.insertObjectsWithListNative(chunk);
							} catch (Exception e) {
								// Handle exceptions in a separate method
								handleException(e, chunk, failedChunks, exceptionOccurred);
							} finally {
								latch.countDown(); // Signal task completion
							}
						});
					}

					// Wait for all tasks to complete
					try {
						latch.await();
					} catch (InterruptedException e) {
						// Handle the interruption
						e.printStackTrace();
					}
					if (exceptionOccurred.get()) {
						for (List<OrgHierarchy> chunk : failedChunks) {
							logger.warn("Failed to insert data into organization_structure table, chunk: "
									+ chunk.toString()); // Convert the chunk to a string and log it
						}
						return;
					}

				} else {
					logger.warn("Organization type is not valid for this Organization" + organizationId);
					return;
				}
			} else {
				logger.warn("Please provide valid Parent Organization Id" + parentId);
				return;
			}
		}
		logger.warn("Successfully saved Organizations in organization_structure table for parentId " + parentId
				+ "organizationId " + organizationId);
	}

	private void handleException(Exception e, List<OrgHierarchy> chunk, List<List<OrgHierarchy>> failedChunks,
			AtomicBoolean exceptionOccurred) {
		if (e instanceof JDBCException || e instanceof DataAccessException || e instanceof PersistenceException) {
			// Handle exceptions here, e.g., log the error
			e.printStackTrace();
			failedChunks.add(chunk);
			exceptionOccurred.set(true);
		}
	}

	private Pair<Boolean, ArrayList<OrgHierarchy>> createOrgHierarchyRecursive(OrgItem orgItem, String countryParent,
			String stateParent, String lgaParent, String wardParent, ArrayList<OrgHierarchy> orgHierarchyList) {
		String orgId = orgItem.getId().toString();
		OrgType orgType = orgItem.getType();

		// Check orgType and return false if it's not valid
		if (!isValidOrgType(orgType)) {
			return Pair.create(false, orgHierarchyList);
		}

		// Create a separate list for the current level of hierarchy
		ArrayList<OrgHierarchy> currentLevelList = new ArrayList<>();

		// Process children based on orgType
		for (OrgItem child : orgItem.getChildren()) {
			if (OrgType.LGA.equals(child.getType())) {
				Pair<Boolean, ArrayList<OrgHierarchy>> result = createOrgHierarchyRecursive(child, countryParent, orgId,
						lgaParent, wardParent, currentLevelList);
				if (!result.first) {
					return Pair.create(false, orgHierarchyList); // Return false if any child is not valid
				}
			} else if (OrgType.WARD.equals(child.getType())) {
				Pair<Boolean, ArrayList<OrgHierarchy>> result = createOrgHierarchyRecursive(child, countryParent,
						stateParent, orgId, wardParent, currentLevelList);
				if (!result.first) {
					return Pair.create(false, orgHierarchyList); // Return false if any child is not valid
				}
			} else if (OrgType.FACILITY.equals(child.getType())) {
				Pair<Boolean, ArrayList<OrgHierarchy>> result = createOrgHierarchyRecursive(child, countryParent,
						stateParent, lgaParent, orgId, currentLevelList);
				if (!result.first) {
					return Pair.create(false, orgHierarchyList); // Return false if any child is not valid
				}
			}
		}

		// Create an OrgHierarchy object and add it to the list
		orgHierarchyList.add(new OrgHierarchy(orgId, orgType.toString().toLowerCase(), countryParent, stateParent,
				lgaParent, wardParent));
		orgHierarchyList.addAll(currentLevelList);

		// If we reach this point, it means the organization type is valid and all
		// children are valid
		return Pair.create(true, orgHierarchyList);
	}

	private boolean isValidOrgType(OrgType orgType) {
		return OrgType.STATE.name().equals(orgType.name()) || OrgType.LGA.name().equals(orgType.name())
				|| OrgType.WARD.name().equals(orgType.name()) || OrgType.FACILITY.name().equals(orgType.name());
	}

	public ResponseEntity<?> getDataByPractitionerRoleId(String practitionerRoleId, String startDate, String endDate,
			ReportType type, LinkedHashMap<String, String> filters, String env) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<ScoreCardResponseItem> scoreCardResponseItems = new ArrayList<>();
		List<String> facilities = new ArrayList<>();
		List<ScoreCardIndicatorItem> scoreCardIndicatorItemsList = getIndicatorItemListFromFile(env);
		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);

		Long start3 = System.nanoTime();
		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
				organizationId);
		Long end3 = System.nanoTime();
		Double diff3 = ((end3 - start3) / 1e9); // Convert nanoseconds to seconds
		logger.warn("getDataByPractitionerRoleId_idsAndOrgIdToChildrenMapPair " + diff3);

		Date start = Date.valueOf(startDate);
		Date end = Date.valueOf(endDate);
		List<String> fhirSearchList = getFhirSearchListByFilters(filters, env);
		List<IndicatorItem> indicators = new ArrayList<>();
		scoreCardIndicatorItemsList
				.forEach(scoreCardIndicatorItem -> indicators.addAll(scoreCardIndicatorItem.getIndicators()));

		Long start1 = System.nanoTime();
		performCachingIfNotPresent(indicators, idsAndOrgIdToChildrenMapPair.first, start, end, fhirSearchList);
		Long end1 = System.nanoTime();
		Double diff1 = ((end1 - start1) / 1e9); // Convert nanoseconds to seconds
		logger.warn("getDataByPractitionerRoleId_performCachingIfNotPresent " + diff1);

		List<OrgHierarchy> orgHierarchyList = notificationDataSource.getOrganizationalHierarchyList(organizationId);
		if (!orgHierarchyList.isEmpty()) {
			Optional<OrgHierarchy> matchingOrg = orgHierarchyList.stream()
					.filter(org -> org.getOrgId().equals(organizationId)).findFirst();

			if (matchingOrg.isPresent()) {
				OrgHierarchy matchedOrgHierarchyItem = matchingOrg.get();
				facilities.addAll(getFacilitiesForOrganization(matchedOrgHierarchyItem, orgHierarchyList));
			}
		}

		if (!facilities.isEmpty()) {
			logger.warn("scorecard_numberOfFacilities " + facilities.size());
			List<String> indicatorIds = new ArrayList<>();

			indicators.forEach(indicatorItem -> {
				String keyBuilder = new StringBuilder().append(indicatorItem.getFhirPath().getExpression())
						.append(String.join(",", fhirSearchList)).toString();
				indicatorIds.add(Utils.md5Bytes(keyBuilder.getBytes(StandardCharsets.UTF_8)));
			});

			List<OrgIndicatorAverageResult> fromTable = new ArrayList<>();
			ThreadPoolTaskExecutor executor = asyncConf.asyncExecutor();

			List<Future<List<OrgIndicatorAverageResult>>> futures = new ArrayList<>();

			// Create a CountDownLatch with the number of tasks
			CountDownLatch latch = new CountDownLatch(
					(int) Math.ceil((double) facilities.size() / appProperties.getFacility_batch_size()));

			Long start47 = System.nanoTime();
			for (int i = 0; i < facilities.size(); i += appProperties.getFacility_batch_size()) {
				List<String> chunk = facilities.subList(i,
						Math.min(i + appProperties.getFacility_batch_size(), facilities.size()));

				// Submit each chunk for parallel processing using the existing thread pool
				Future<List<OrgIndicatorAverageResult>> future = executor.submit(() -> {
					try {
						List<OrgIndicatorAverageResult> result = notificationDataSource
								.getOrgIndicatorAverageResult(chunk, indicatorIds, start, end);
						return result;
					} catch (JDBCException | DataAccessException e) {
						// Handle exceptions here, e.g., log the error
						e.printStackTrace();
						return new ArrayList<>(); // Return an empty list in case of exceptions
					} catch (PersistenceException e) {
						// Handle exceptions here, e.g., log the error
						e.printStackTrace();
						return new ArrayList<>(); // Return an empty list in case of exceptions
					} finally {
						latch.countDown(); // Signal that this task is done
					}
				});
				futures.add(future);
			}

			try {
				// Wait for all tasks to complete
				latch.await();
			} catch (InterruptedException e) {
				// Handle the interruption, if needed
				e.printStackTrace();
			}

			// Collect the results from the futures
			for (Future<List<OrgIndicatorAverageResult>> future : futures) {
				try {
					fromTable.addAll(future.get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Long end47 = System.nanoTime();
			Double diff47 = ((end47 - start47) / 1e9); // Convert nanoseconds to seconds
			logger.warn("getDataByPractitionerRoleId got results from db call " + diff47);

			scoreCardIndicatorItemsList.forEach(scoreCardIndicatorItem -> {
				ScoreCardResponseItem scoreCardResponseItem = new ScoreCardResponseItem();
				scoreCardResponseItem.setCategoryId(scoreCardIndicatorItem.getCategoryId());
				List<ScoreCardItem> scoreCardItems = new ArrayList<>();
				switch (type) {
				case summary: {
					// other categories
					orgHierarchyList.forEach(orgHierarchyItem -> {
						// Call the other function with orgHierarchy as an argument
						List<String> facility = getFacilitiesForOrganization(orgHierarchyItem, orgHierarchyList);
						for (IndicatorItem indicator : scoreCardIndicatorItem.getIndicators()) {
							String keyBuilder = new StringBuilder().append(indicator.getFhirPath().getExpression())
									.append(String.join(",", fhirSearchList)).toString();
							String hashedId = Utils.md5Bytes(keyBuilder.getBytes(StandardCharsets.UTF_8));
							Double value = calculateAverage(facility, fromTable, hashedId,
									indicator.getFhirPath().getTransformServer());
							scoreCardItems.add(new ScoreCardItem(orgHierarchyItem.getOrgId(), indicator.getId(),
									value.toString(), startDate, endDate));
						}
					});
				}
				}
				scoreCardResponseItem.setScoreCardItemList(scoreCardItems);
				scoreCardResponseItems.add(scoreCardResponseItem);
			});
		}

		return ResponseEntity.ok(scoreCardResponseItems);
	}

	public ResponseEntity<?> getBarChartData(String startDate, String endDate, LinkedHashMap<String, String> filters,
			String env, String lga) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<BarChartItemDataCollection> barChartItems = new ArrayList<>();
		List<BarChartDefinition> barCharts = getBarChartItemListFromFile(env);
		String organizationId = lga;

		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
				organizationId);
		List<String> facilityIds = idsAndOrgIdToChildrenMapPair.first;

		Date start = Date.valueOf(startDate);
		Date end = Date.valueOf(endDate);
		List<String> fhirSearchList = getFhirSearchListByFilters(filters, env);
		performCachingIfNotPresentForBarChart(barCharts, idsAndOrgIdToChildrenMapPair.first, start, end,
				fhirSearchList);
		for (BarChartDefinition barChart : barCharts) {
			List<BarComponentCategory> barComponentCategory = new ArrayList<>();
			for (BarChartItemDefinition barChartItem : barChart.getBarChartItemDefinitions()) {
				ArrayList<BarComponentData> barComponents = new ArrayList<BarComponentData>();
				for (BarComponent barComponent : barChartItem.getBarComponentList()) {
					String key = barComponent.getFhirPath().getExpression() + String.join(",", fhirSearchList);
					String md5 = Utils.getMd5KeyForLineCacheMd5WithCategory(key, barComponent.getBarChartItemId(),
							barChartItem.getChartId(), barChart.getCategoryId());
					Double cacheValueSum = getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(
							barComponent.getFhirPath().getTransformServer(), start, end, md5, facilityIds);
					barComponents.add(new BarComponentData(barComponent.getId(), barComponent.getBarChartItemId(),
							cacheValueSum.toString()));
				}
				barComponentCategory.add(new BarComponentCategory(barChartItem.getId(), barComponents));
			}
			barChartItems.add(
					new BarChartItemDataCollection(barChart.getId(), barChart.getCategoryId(), barComponentCategory));
		}
		return ResponseEntity.ok(barChartItems);
	}

	private void performCachingIfNotPresent(List<IndicatorItem> indicators, List<String> facilityIds, Date startDate,
			Date endDate, List<String> fhirSearchList) {
		String filterString = String.join(",", fhirSearchList);
		logger.warn("**** " + filterString);
		List<String> currentIndicatorMD5List = indicators.stream()
				.map(indicatorItem -> Utils.md5Bytes(
						(indicatorItem.getFhirPath().getExpression() + filterString).getBytes(StandardCharsets.UTF_8)))
				.collect(Collectors.toList());

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
		List<Date> presentDates = notificationDataSource.getDatesPresent(startDate, endDate,
				nonExistingIndicators.isEmpty() ? existingIndicators : nonExistingIndicators, facilityIds);

		Date start = startDate;
		Date end = Date.valueOf(endDate.toLocalDate().plusDays(1));
		while (!start.equals(end)) {
			if (!presentDates.contains(start)) {
				dates.add(start);
			}
			start = Date.valueOf(start.toLocalDate().plusDays(1));
		}
		logger.warn("Data Score card Cache present days: " + presentDates.toString() + "Cache existing indicators: "
				+ existingIndicators.toString() + "Cache missing days: " + dates.toString()
				+ "Cache missing indicators days: " + nonExistingIndicators.toString());

		Date currentDate = new Date(System.currentTimeMillis());
		boolean currentDateNotInDatesList = Utils.noneMatchDates(dates, currentDate);

		for (int count = 0; count < facilityIds.size(); count++) {
			String facilityId = facilityIds.get(count);
			dates.forEach(date -> {
				cachingService.cacheData(facilityId, date, indicators, filterString);
			});
			// Always cache current date data if it lies between start and end date.
			if (currentDateNotInDatesList && currentDate.getTime() >= startDate.getTime()
					&& currentDate.getTime() <= Date.valueOf(endDate.toLocalDate().plusDays(1)).getTime()) {
				cachingService.cacheData(facilityId, DateUtilityHelper.getCurrentSqlDate(), indicators, filterString);
			}
		}
	}

	private void performCachingIfNotPresentForBarChart(List<BarChartDefinition> barCharts, List<String> facilityIds,
			Date startDate, Date endDate, List<String> fhirSearchList) {
		String filterString = String.join(",", fhirSearchList);
		List<String> currentIndicatorMD5List = barCharts.stream()
				.flatMap(barChart -> barChart.getBarChartItemDefinitions().stream()
						.flatMap(barItemDefinition -> barItemDefinition.getBarComponentList().stream()
								.map(barComponent -> Utils.getMd5KeyForLineCacheMd5WithCategory(
										barComponent.getFhirPath().getExpression() + filterString,
										barComponent.getBarChartItemId(), barItemDefinition.getChartId(),
										barChart.getCategoryId()))))
				.collect(Collectors.toList());

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
		List<Date> presentDates = notificationDataSource.getDatesPresent(startDate, endDate,
				nonExistingIndicators.isEmpty() ? existingIndicators : nonExistingIndicators, facilityIds);

		Date start = startDate;
		Date end = Date.valueOf(endDate.toLocalDate().plusDays(1));
		while (!start.equals(end)) {
			if (!presentDates.contains(start)) {
				dates.add(start);
			}
			start = Date.valueOf(start.toLocalDate().plusDays(1));
		}
		logger.warn("Bar Chart Cache Cache present days: " + presentDates.toString() + "Cache existing indicators: "
				+ existingIndicators.toString() + "Cache missing days: " + dates.toString()
				+ "Cache missing indicators days: " + nonExistingIndicators.toString());

		Date currentDate = new Date(System.currentTimeMillis());
		boolean currentDateNotInDatesList = Utils.noneMatchDates(dates, currentDate);

		for (int count = 0; count < facilityIds.size(); count++) {
			String facilityId = facilityIds.get(count);
			final int finalcount = count;
			dates.forEach(date -> {
				cachingService.cacheDataForBarChart(facilityId, date, barCharts, finalcount, filterString);
			});
			// Always cache current date data if it lies between start and end date.
			if (currentDateNotInDatesList && currentDate.getTime() >= startDate.getTime()
					&& currentDate.getTime() <= Date.valueOf(endDate.toLocalDate().plusDays(1)).getTime()) {
				cachingService.cacheDataForBarChart(facilityId, DateUtilityHelper.getCurrentSqlDate(), barCharts, 0,
						filterString);
			}
		}
	}

	public ResponseEntity<?> getLineChartByPractitionerRoleId(String startDate, String endDate, ReportType type,
			LinkedHashMap<String, String> filters, String env, String lga) {
		notificationDataSource = NotificationDataSource.getInstance();
		List<LineChartItemCollection> lineChartItemCollections = new ArrayList<>();
		List<LineChart> lineCharts = getLineChartDefinitionsItemListFromFile(env);
		String organizationId = lga;

		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = fetchIdsAndOrgIdToChildrenMapPair(
				organizationId);

		Date start = Date.valueOf(startDate);
		Date end = Date.valueOf(endDate);
		List<String> fhirSearchList = getFhirSearchListByFilters(filters, env);

		performCachingForLineChartIfNotPresent(lineCharts, idsAndOrgIdToChildrenMapPair.first, start, end,
				fhirSearchList);
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
				for (LineChartItemDefinition lineChartDefinition : lineChart.getLineChartItemDefinitions()) {
					String key = lineChartDefinition.getFhirPath().getExpression() + String.join(",", fhirSearchList);
					Double cacheValue = getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(
							lineChartDefinition.getFhirPath().getTransformServer(), weekDayPair.first,
							weekDayPair.second, Utils.getMd5KeyForLineCacheMd5WithCategory(key,
									lineChartDefinition.getId(), lineChart.getId(), lineChart.getCategoryId()),
							facilityIds);
					lineChartItems.add(new LineChartItem(lineChartDefinition.getId(), String.valueOf(cacheValue),
							weekDayPair.first.toString(), weekDayPair.second.toString()));
				}
			}
			lineChartItemCollections
					.add(new LineChartItemCollection(lineChart.getId(), lineChart.getCategoryId(), lineChartItems));
		}
		return ResponseEntity.ok(lineChartItemCollections);
	}

	private Double getCacheValueForDateRangeIndicatorAndMultipleOrgIdByReflection(String transform, Date start,
			Date end, String indicator, List<String> orgIds) {
		notificationDataSource = NotificationDataSource.getInstance();
		if (transform == null) {
			return notificationDataSource.getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(start, end, indicator,
					orgIds);
		} else {
			try {
				return (Double) notificationDataSource.getClass()
						.getMethod(transform, Date.class, Date.class, String.class, List.class)
						.invoke(notificationDataSource, start, end, indicator, orgIds);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				logger.warn(ExceptionUtils.getStackTrace(e));
				return 0.0;
			}
		}
	}

	private void performCachingForLineChartIfNotPresent(List<LineChart> lineCharts, List<String> facilityIds,
			Date startDate, Date endDate, List<String> fhirSearchList) {
		String filterString = String.join(",", fhirSearchList);
		List<String> currentIndicatorMD5List = lineCharts.stream()
				.flatMap(lineChart -> lineChart.getLineChartItemDefinitions().stream()
						.map(lineDefinition -> Utils.getMd5KeyForLineCacheMd5WithCategory(
								lineDefinition.getFhirPath().getExpression() + filterString, lineDefinition.getId(),
								lineChart.getId(), lineChart.getCategoryId())))
				.collect(Collectors.toList());

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
		List<Date> presentDates = notificationDataSource.getDatesPresent(startDate, endDate,
				nonExistingIndicators.isEmpty() ? existingIndicators : nonExistingIndicators, facilityIds);
		Date start = startDate;
		Date end = Date.valueOf(endDate.toLocalDate().plusDays(1));
		while (!start.equals(end)) {
			if (!presentDates.contains(start)) {
				dates.add(start);
			}
			start = Date.valueOf(start.toLocalDate().plusDays(1));
		}
		logger.warn("Line Chart Cache present days: " + presentDates.toString() + " " + "Cache existing indicators: "
				+ existingIndicators.toString() + " " + "Cache missing days: " + dates.toString() + " "
				+ "Cache missing indicators days: " + nonExistingIndicators.toString());

		Date currentDate = new Date(System.currentTimeMillis());
		boolean currentDateNotInDatesList = Utils.noneMatchDates(dates, currentDate);

		for (int count = 0; count < facilityIds.size(); count++) {
			String facilityId = facilityIds.get(count);
			final int finalcount = count;
			dates.forEach(date -> {
				cachingService.cacheDataLineChart(facilityId, date, lineCharts, finalcount, filterString);
			});
			// Always cache current date data if it lies between start and end date.
			if (currentDateNotInDatesList && currentDate.getTime() >= startDate.getTime()
					&& currentDate.getTime() <= Date.valueOf(endDate.toLocalDate().plusDays(1)).getTime()) {
				cachingService.cacheDataLineChart(facilityId, DateUtilityHelper.getCurrentSqlDate(), lineCharts, 0,
						filterString);
			}
		}
	}

	public void cacheDashboardData(List<String> facilities, String start, String end, String env) {
		List<ScoreCardIndicatorItem> scoreCardIndicatorItemsList = getIndicatorItemListFromFile(env);
		List<IndicatorItem> analyticsItemListFromFile = getAnalyticsItemListFromFile(env);
		List<IndicatorItem> indicators = new ArrayList<>();
		scoreCardIndicatorItemsList
				.forEach(scoreCardIndicatorItem -> indicators.addAll(scoreCardIndicatorItem.getIndicators()));
		indicators.addAll(analyticsItemListFromFile);
		List<PieChartDefinition> pieChartDefinitions = getPieChartItemDefinitionFromFile(env);
		List<BarChartDefinition> barCharts = getBarChartItemListFromFile(env);
		List<LineChart> lineCharts = getLineChartDefinitionsItemListFromFile(env);
		List<TabularItem> tabularItemList = getTabularItemListFromFile(env);
		ThreadPoolTaskExecutor executor =  asyncConf.asyncExecutor();
		HashMap <String,Pair<Long,Long>> orgToTiming = new HashMap();
		ArrayList<MapCacheEntity> resultToAddInMapCache = new ArrayList<>();
		ArrayList<MapCacheEntity> resultToUpdateinMapCache = new ArrayList<>();
		cachingService.processMapDataForCache(facilities, start, end, resultToAddInMapCache, resultToUpdateinMapCache);
		List<List<String>> facilityBatches = Utils.partitionFacilities(facilities, appProperties.getExecutor_max_pool_size());
		int count = 0;
		long startTime = System.nanoTime();
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (List<String> facilityBatch : facilityBatches) {
			count += 1;
			final int countFinal = count;
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					for (String facilityId : facilityBatch) {
						Date endDate = Date.valueOf(Date.valueOf(end).toLocalDate().plusDays(1));
						Date startDate = Date.valueOf(start);
						LastSyncEntity lastSyncEntity = new LastSyncEntity(facilityId,
								ApiAsyncTaskEntity.Status.PROCESSING.name(), env,
								new Timestamp(System.currentTimeMillis()), null);
						datasource.insert(lastSyncEntity);
						cacheDashboardData(facilityId, startDate, endDate, indicators, barCharts, tabularItemList,
								lineCharts, pieChartDefinitions, countFinal, orgToTiming, env);
					}
				}
			};
			executor.submit(worker);
		}
	}

	List<String> getFhirSearchListByFilters(LinkedHashMap<String, String> filters, String env) {
		List<String> fhirSearchList = new ArrayList<>();
		List<FilterItem> filterItemList = getFilterItemListFromFile(env);
		for (int i = 0; i <= filters.size() - 2; i += 2) {
			int keyIndex = i / 2;
			String id = filters.get("filter" + String.valueOf(keyIndex + 1) + "Id");
			String value = filters.get("filter" + String.valueOf(keyIndex + 1) + "Value");
			FilterItem filterItem = filterItemList.stream().filter(item -> item.getId().equals(id)).findFirst()
					.orElse(null);
			if (filterItem != null) {
				FilterOptions filterOption = filterItem.getOptions().stream()
						.filter(option -> option.getId().equals(value)).findFirst().orElse(null);
				if (filterOption != null) {
					fhirSearchList.add(filterOption.getFhirSearch());
				}
			}
		}
		return fhirSearchList;
	}

	public void cacheDashboardData(String orgId, Date startDate, Date endDate, List<IndicatorItem> indicators,
			List<BarChartDefinition> barCharts, List<TabularItem> tabularItems, List<LineChart> lineCharts,
			List<PieChartDefinition> pieChartDefinitions, int count, HashMap<String, Pair<Long, Long>> orgToTiming,
			String env) {
		notificationDataSource = NotificationDataSource.getInstance();
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl(
				(GenericClient) fhirClientAuthenticatorService.getFhirClient());
		DashboardModel dashboard = ReportGeneratorFactory.INSTANCE.reportGenerator().getOverallDataToCache(
				fhirClientProvider, orgId, new DateRange(startDate.toString(), endDate.toString()), indicators,
				lineCharts, barCharts, tabularItems, pieChartDefinitions, Collections.emptyList());
//		ThreadPoolTaskExecutor cacheExecutor =  asyncConf.cacheExecutor();

//		Runnable worker = new Runnable() {
//			@Override
//			public void run() {
		Date currentDate = startDate;
		Double diff = 0.0;
		while (!currentDate.equals(endDate)) {
			Long start = System.nanoTime();
			cachingService.cacheData(orgId, currentDate, indicators, count, dashboard.getScoreCardItemList(), "");
			cachingService.cacheDataForBarChart(orgId, currentDate, barCharts, count,
					dashboard.getBarChartItemCollectionList(), "");
			cachingService.cacheDataLineChart(orgId, currentDate, lineCharts, count,
					dashboard.getLineChartItemCollections(), "");
			cachingService.cachePieChartData(orgId, currentDate, pieChartDefinitions, count,
					dashboard.getPieChartItemList(), "");
			cachingService.cacheTabularData(orgId, currentDate, tabularItems, count, dashboard.getTabularItemList(),
					"");
			currentDate = Date.valueOf(currentDate.toLocalDate().plusDays(1));
			Long end = System.nanoTime();
			diff += (end - start) / 1000000000.0;
		}
		List<LastSyncEntity> lastSyncData = datasource.getEntitiesByOrgEnvStatus(orgId, env,
				ApiAsyncTaskEntity.Status.PROCESSING.name());

		try {
			if (!lastSyncData.isEmpty()) {
				LastSyncEntity lastSyncRecord = (LastSyncEntity) lastSyncData.get(0);
				lastSyncRecord.setEndDateTime(new Timestamp(System.currentTimeMillis()));
				lastSyncRecord.setStatus(ApiAsyncTaskEntity.Status.COMPLETED.name());
				datasource.update(lastSyncRecord);
			}
		} catch (HibernateException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		logger.warn("ALL Dates for org ****** " + orgId + " " + String.valueOf(diff));
//			}
//		};
//		cacheExecutor.submit(worker);
	}

//	@Scheduled(fixedDelay = 24 * DELAY, initialDelay = DELAY)
//	protected void cacheDailyData() {
//		Map<String, DashboardConfigContainer> dashboardEnvToConfigMap = dashboardEnvironmentConfig.getDashboardEnvToConfigMap();
//		dashboardEnvironmentConfig.getEnvToFilePathMapping().forEach((env, definitionTypeToFilePathMap) -> {
//			cachingService.cacheData(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getAnalyticsIndicatorItems(),0);
//			cachingService.cacheDataForBarChart(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getBarChartDefinitions(),0);
//			cachingService.cacheTabularData(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getTabularItems(),0);
//			cachingService.cachePieChartData(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getPieChartDefinitions(),0);
//			cachingService.cacheDataLineChart(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getLineCharts(),0);
//		});
//	}
//
	List<FilterItem> getFilterItemListFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getFilterItems();
	}

	List<ScoreCardIndicatorItem> getIndicatorItemListFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getScoreCardIndicatorItems();
	}

	CategoryItem getCategoriesFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getCategoryItem();
	}

	
	List<BarChartDefinition> getBarChartItemListFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getBarChartDefinitions();
	}

	List<PieChartDefinition> getPieChartItemDefinitionFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getPieChartDefinitions();
	}

	List<LineChart> getLineChartDefinitionsItemListFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getLineCharts();
	}

	List<TabularItem> getTabularItemListFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getTabularItems();
	}

	List<IndicatorItem> getAnalyticsItemListFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getAnalyticsIndicatorItems();
	}

	public List<ANCDailySummaryConfig> getANCDailySummaryConfigFromFile(String env) throws NullPointerException {
		return dashboardEnvToConfigMap.get(env).getAncDailySummaryConfig();
	}

	public List<OrgItem> getOrganizationHierarchy(String organizationId) {
		return fetchOrgHierarchy(organizationId);
	}

	public String getOrganizationIdByPractitionerRoleId(String practitionerRoleId) {
		Bundle bundle = fhirClientAuthenticatorService.getFhirClient().search().forResource(PractitionerRole.class)
				.where(PractitionerRole.RES_ID.exactly().identifier(practitionerRoleId)).returnBundle(Bundle.class)
				.execute();
		if (!bundle.hasEntry()) {
			return null;
		}
		PractitionerRole practitionerRole = (PractitionerRole) bundle.getEntry().get(0).getResource();
		return practitionerRole.getOrganization().getReferenceElement().getIdPart();
	}

	public Organization getOrganizationResourceByPractitionerRoleId(String practitionerRoleId) {
		String organizationId = getOrganizationIdByPractitionerRoleId(practitionerRoleId);
		if (organizationId == null)
			return null;
		Bundle bundle = fhirClientAuthenticatorService.getFhirClient().search().forResource(Organization.class)
				.where(Organization.RES_ID.exactly().identifier(organizationId)).returnBundle(Bundle.class).execute();
		if (!bundle.hasEntry()) {
			return null;
		}
		return (Organization) bundle.getEntry().get(0).getResource();
	}

	public String getOrganizationIdByFacilityUID(String facilityUID) {
		FhirUtils fhirUtils = new FhirUtils();
		Bundle organizationBundle = new Bundle();
		String queryPath = "/Organization?";
		queryPath += "identifier=" + facilityUID + "";
		fhirUtils.getBundleBySearchUrl(organizationBundle, queryPath, fhirClientAuthenticatorService.getFhirClient());
		if (organizationBundle.hasEntry() && organizationBundle.getEntry().size() > 0) {
			Organization organization = (Organization) organizationBundle.getEntry().get(0).getResource();
			return organization.getIdElement().getIdPart();
		}
		return null;
	}

	public String getOrganizationIdByOrganizationNameAndType(String name, String type) {

		Bundle organizationBundle = fhirClientAuthenticatorService.getFhirClient().search()
				.forResource(Organization.class).where(Organization.NAME.matchesExactly().value(name))
				.and(new TokenClientParam("_tag").exactly()
						.systemAndCode("https://www.iprdgroup.com/ValueSet/OrganizationType/tags", type))
				.returnBundle(Bundle.class).execute();

		if (organizationBundle.hasEntry() && organizationBundle.getEntry().size() > 0) {
			return organizationBundle.getEntry().get(0).getResource().getIdElement().getIdPart();
		}
		return null;
	}

	static String getValidURL(String invalidURLString) {
		try {
			// Convert the String and decode the URL into the URL class
			URL url = new URL(URLDecoder.decode(invalidURLString, StandardCharsets.UTF_8.toString()));

			// Use the methods of the URL class to achieve a generic solution
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			// return String or
			// uri.toURL() to return URL object
			return uri.toString();
		} catch (URISyntaxException | UnsupportedEncodingException | MalformedURLException ignored) {
			return null;
		}
	}

	private String createKeycloakGroup(GroupRepresentation groupRep) {
		RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
				.realm(appProperties.getKeycloak_Client_Realm());
		List<GroupRepresentation> groups = realmResource.groups().groups(groupRep.getName(), 0, Integer.MAX_VALUE,
				false);
//
		for (GroupRepresentation group : groups) {
			if (group.getName().equals(groupRep.getName()) && (groupRep.getAttributes().get("parent") == null
					|| group.getAttributes().containsValue(groupRep.getAttributes().get("parent")))) {
				return group.getId();
			}
		}
		try {
			Response response = realmResource.groups().add(groupRep);
			return CreatedResponseUtil.getCreatedId(response);
		} catch (WebApplicationException ex) {
			logger.warn("Group with identical name found. Appending the parent group name to existing name.");
			GroupResource parentGroupResource = realmResource.groups()
					.group(groupRep.getAttributes().get("parent").get(0));
			GroupRepresentation parentGroup = parentGroupResource.toRepresentation();
			String parentName = parentGroup.getName();
			groupRep.setName(parentName + "_" + groupRep.getName());
			return createKeycloakGroup(groupRep);
		}
	}

//	private IBaseResource createResource(Resource resource, Class<? extends IBaseResource> theClass, ICriterion<?>... theCriterion) {
//		IQuery<IBaseBundle> query = FhirClientAuthenticatorService.getFhirClient().search().forResource(theClass).where(theCriterion[0]);
//		for (int i = 1; i < theCriterion.length; i++)
//			query = query.and(theCriterion[i]);
//		Bundle bundle = query.returnBundle(Bundle.class).execute();
//		if (!bundle.hasEntry()) {
//			MethodOutcome outcome = FhirClientAuthenticatorService.getFhirClient().update().resource(resource).execute();
//			logger.warn(resource.getId());
//			return outcome.getId().getIdPart();
//		}
//		return bundle.getEntry().get(0).getFullUrl().split("/")[5];
//	}

	private <R extends IBaseResource> String createResource(String keycloakId, IBaseResource resource,
			Class<R> resourceClass, ICriterion<?>... theCriterion) {
		IQuery<IBaseBundle> query = fhirClientAuthenticatorService.getFhirClient().search().forResource(resourceClass)
				.where(theCriterion[0]);
		for (int i = 1; i < theCriterion.length; i++)
			query = query.and(theCriterion[i]);
		try {
			Bundle bundle = query.returnBundle(Bundle.class).execute();
			if (bundle.hasEntry() && bundle.getEntry().size() > 0) {
				Resource existingResource = bundle.getEntry().get(0).getResource();
				Method getIdentifier = resource.getClass().getMethod("getIdentifier");
				List<Identifier> identifierList = (List<Identifier>) getIdentifier.invoke(existingResource);
				for (Identifier identifier : identifierList) {
					if (identifier.getSystem().equals(IDENTIFIER_SYSTEM + "/KeycloakId")
							&& identifier.getValue().equals(keycloakId)) {
						return existingResource.getIdElement().getIdPart();
					}
				}
			}
			Method addIdentifier = resource.getClass().getMethod("addIdentifier");
			Identifier obj = (Identifier) addIdentifier.invoke(resource);
			obj.setSystem(IDENTIFIER_SYSTEM + "/KeycloakId");
			obj.setValue(keycloakId);
			MethodOutcome outcome = fhirClientAuthenticatorService.getFhirClient().update().resource(resource)
					.execute();
			return outcome.getId().getIdPart();
		} catch (SecurityException | NoSuchMethodException | InvocationTargetException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		} catch (IllegalAccessException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	private <R extends IBaseResource> String createResource(String keycloakId, IBaseResource resource,
			Class<R> resourceClass) {

		RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
				.realm(appProperties.getKeycloak_Client_Realm());
		R existingResource = null;
		GroupResource groupResource = null;
		GroupRepresentation group = null;
		try {
			if (resourceClass.equals(Organization.class)) {
				groupResource = realmResource.groups().group(keycloakId);
				group = groupResource.toRepresentation();
				String orgId = group.getAttributes().get("organization_id").get(0);
				existingResource = fhirClientAuthenticatorService.getFhirClient().read().resource(resourceClass)
						.withId(orgId).execute();
			} else if (resourceClass.equals(Location.class)) {
				groupResource = realmResource.groups().group(keycloakId);
				group = groupResource.toRepresentation();
				String locId = group.getAttributes().get("location_id").get(0);
				existingResource = fhirClientAuthenticatorService.getFhirClient().read().resource(resourceClass)
						.withId(locId).execute();
			}
		} catch (ResourceNotFoundException e) {
			logger.warn("RESOURCE NOT FOUND");
		}
		try {
			if (existingResource == null) {
				Method addIdentifier = resource.getClass().getMethod("addIdentifier");
				Identifier obj = (Identifier) addIdentifier.invoke(resource);
				obj.setSystem(IDENTIFIER_SYSTEM + "/KeycloakId");
				obj.setValue(keycloakId);
				MethodOutcome outcome = fhirClientAuthenticatorService.getFhirClient().update().resource(resource)
						.execute();
				if (outcome.getCreated() || outcome.getOperationOutcome() == null) {
					return outcome.getId().getIdPart();
				} else {
					return null;
				}
			}
			return existingResource.getIdElement().getIdPart();
		} catch (SecurityException | NoSuchMethodException | InvocationTargetException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		} catch (IllegalAccessException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	private <R extends IBaseResource> void updateResource(String resourceId, Class<R> resourceClass,
			String[] updatedDetails, int counter) {
		// State(0), LGA(1), Ward(2), FacilityUID(3), FacilityCode(4), CountryCode(5),
		// PhoneNumber(6), FacilityName(7), FacilityLevel(8), Ownership(9), Argusoft
		// Identifier(10), Longitude(11), Latitude(12), Pluscode(13)
		String stateName = updatedDetails[0];
		String lgaName = updatedDetails[1];
		String wardName = updatedDetails[2];
		String countryCode = updatedDetails[5];
		String phoneNumber = updatedDetails[6];
		String facilityName = updatedDetails[7];
		String longitude = updatedDetails[11];
		String latitude = updatedDetails[12];
		String pluscode = updatedDetails[13];
		String countryName = updatedDetails[14];
		Organization organizationResource = resourceClass.equals(Organization.class)
				? fhirClientAuthenticatorService.getFhirClient().read().resource(Organization.class).withId(resourceId)
						.execute()
				: null;
		Location locationResource = resourceClass.equals(Location.class)
				? fhirClientAuthenticatorService.getFhirClient().read().resource(Location.class).withId(resourceId)
						.execute()
				: null;
		try {
			switch (counter) {
			case 0:
				if (resourceClass.equals(Organization.class)) {
					if (!organizationResource.getName().equals(facilityName)) {
						organizationResource.addAlias(organizationResource.getName());
						organizationResource.setName(facilityName);
					}
					List<Address> addresses = new ArrayList<>();
					Address address = new Address();
					address.setState(stateName);
					address.setDistrict(lgaName);
					address.setCity(wardName);
					addresses.add(address);
					organizationResource.setAddress(addresses);
					ContactPoint contactPoint = new ContactPoint();
					contactPoint.setValue(countryCode + "-" + phoneNumber);
					List<ContactPoint> listOfContacts = organizationResource.getTelecom();
					boolean contactExists = false;
					for (ContactPoint existinContact : listOfContacts) {
						if (existinContact.getValue().equals(contactPoint.getValue())) {
							contactExists = true;
							break;
						}
					}
					if (!contactExists) {
						List<ContactPoint> distinctListOfContacts = listOfContacts.stream()
								.collect(Collectors.toMap(ContactPoint::getValue, Function.identity(),
										(existing, replacement) -> existing))
								.values().stream().collect(Collectors.toList());
						distinctListOfContacts.add(contactPoint);
						organizationResource.setTelecom(distinctListOfContacts);
					}
				} else {
					locationResource.setName(facilityName);
					Address address = new Address();
					address.setState(stateName);
					address.setDistrict(lgaName);
					address.setCity(wardName);
					locationResource.setAddress(address);
					boolean positionPresent = locationResource.hasPosition();
					try {
						if (positionPresent) {
							LocationPositionComponent oldPosition = locationResource.getPosition();
							LocationPositionComponent newPosition = new LocationPositionComponent();
							newPosition.setLongitude(Double.parseDouble(longitude));
							newPosition.setLatitude(Double.parseDouble(latitude));
							if (!(oldPosition.getLatitudeElement().equals(newPosition.getLatitudeElement())
									&& !oldPosition.getLongitudeElement().equals(newPosition.getLongitudeElement()))) {
								locationResource.setPosition(newPosition);
								Extension pluscodeExtension = new Extension();
								pluscodeExtension.setUrl(EXTENSION_PLUSCODE_URL);
								StringType pluscodeValue = new StringType(pluscode);
								pluscodeExtension.setValue(pluscodeValue);
								List<Extension> listOfExtension = locationResource.getExtension();
								boolean extensionExists = false;
								for (Extension existingExtension : listOfExtension) {
									if (existingExtension.getUrl().equals(pluscodeExtension.getUrl())) {
										existingExtension.setValue(pluscodeValue);
										extensionExists = true;
										break;
									}
								}
								if (!extensionExists) {
									locationResource.addExtension(pluscodeExtension);
								}
							}
						} else {
							LocationPositionComponent position = new LocationPositionComponent();
							position.setLongitude(Double.parseDouble(longitude));
							position.setLatitude(Double.parseDouble(latitude));
							locationResource.setPosition(position);
							Extension pluscodeExtension = new Extension();
							pluscodeExtension.setUrl(EXTENSION_PLUSCODE_URL);
							StringType pluscodeValue = new StringType(pluscode);
							pluscodeExtension.setValue(pluscodeValue);
							locationResource.addExtension(pluscodeExtension);
						}
					} catch (NumberFormatException e) {
						logger.warn("The provided updated latitude or longitude value is non-numeric");
					}
				}
				break;
			case 1:
				if (resourceClass.equals(Organization.class)) {
					organizationResource.addAlias(organizationResource.getName());
					organizationResource.setName(wardName);
					List<Address> addresses = new ArrayList<>();
					Address address = new Address();
					address.setState(stateName);
					address.setDistrict(lgaName);
					address.setCity(wardName);
					addresses.add(address);
					organizationResource.setAddress(addresses);
				}
				break;
			case 2:
				if (resourceClass.equals(Organization.class)) {
					organizationResource.addAlias(organizationResource.getName());
					organizationResource.setName(lgaName);
					List<Address> addresses = new ArrayList<>();
					Address address = new Address();
					address.setState(stateName);
					address.setDistrict(lgaName);
					addresses.add(address);
					organizationResource.setAddress(addresses);
				}
				break;
			case 3:
				if (resourceClass.equals(Organization.class)) {
					organizationResource.addAlias(organizationResource.getName());
					organizationResource.setName(stateName);
					List<Address> addresses = new ArrayList<>();
					Address address = new Address();
					address.setState(stateName);
					addresses.add(address);
					organizationResource.setAddress(addresses);
				}
			}
			if (null == organizationResource) {
				fhirClientAuthenticatorService.getFhirClient().update().resource(locationResource).execute();
			} else {
				fhirClientAuthenticatorService.getFhirClient().update().resource(organizationResource).execute();
			}
		} catch (SecurityException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
	}

	private String createKeycloakUser(UserRepresentation userRep) {
		RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
				.realm(appProperties.getKeycloak_Client_Realm());
		List<UserRepresentation> users = realmResource.users().search(userRep.getUsername(), 0, Integer.MAX_VALUE);
		// if not empty, return id

		for (UserRepresentation user : users) {
			if (Objects.equals(user.getUsername(), userRep.getUsername())) {
				return user.getId();
			}
		}
		try {
			Response response = realmResource.users().create(userRep);
			return CreatedResponseUtil.getCreatedId(response);
		} catch (WebApplicationException e) {
			String errorMessage = "An error occurred while creating a Keycloak user.";
			errorMessage += "\nUser: " + userRep.getUsername();
			errorMessage += "\nError message: " + e.getMessage();
			logger.warn(errorMessage, ExceptionUtils.getStackTrace(e));
			return null;
		}
	}

	private void createRoleIfNotExists(RoleRepresentation roleRepresentation) {
		RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
				.realm(appProperties.getKeycloak_Client_Realm());
		String clientId = realmResource.clients().findByClientId(appProperties.getFhir_hapi_client_id()).get(0).getId();
		if (roleWithNameExists(clientId, roleRepresentation.getName())) {
			return;
		}
		try {
			realmResource.clients().get(clientId).roles().create(roleRepresentation);
		} catch (WebApplicationException ex) {
			logger.error("Cannot create role" + roleRepresentation.getName() + "\n" + ex.getStackTrace().toString());
		}
	}

	public Boolean roleWithNameExists(String clientId, String roleName) {
		RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
				.realm(appProperties.getKeycloak_Client_Realm());
		for (RoleRepresentation roleRepresentation : realmResource.clients().get(clientId).roles().list()) {
			if (roleRepresentation.getName().equals(roleName)) {
				return true;
			}
		}
		return false;

	}

	private void assignRole(String userId, String roleName) {
		try {
			RealmResource realmResource = fhirClientAuthenticatorService.getKeycloak()
					.realm(appProperties.getKeycloak_Client_Realm());
			String clientId = realmResource.clients().findByClientId(appProperties.getFhir_hapi_client_id()).get(0)
					.getId();
			RoleRepresentation saveRoleRepresentation = realmResource.clients().get(clientId).roles().get(roleName)
					.toRepresentation();
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

	public List<MapResponse> getEncounterForMap(String orgId, String from, String to) {
		Gson gson = new Gson();
		notificationDataSource = NotificationDataSource.getInstance();
		List<String> allClinics = fetchIdsAndOrgIdToChildrenMapPair(orgId).first;
		try {
//			cachingService.performCachingForMapDataIfRequired(allClinics, from, to);
			List<MapCacheEntity> responseFromCache = notificationDataSource.getMapDataByOrgIdAndDateRange(allClinics,
					Date.valueOf(LocalDate.parse(from, DateTimeFormatter.ISO_DATE)),
					Date.valueOf(LocalDate.parse(to, DateTimeFormatter.ISO_DATE)));
			LinkedHashMap<String, ArrayList<LocationData>> categoryWiseResponse = new LinkedHashMap<>();
			for (MapCacheEntity entry : responseFromCache) {
				String categoryId = entry.getCategoryId();
				if (categoryWiseResponse.containsKey(categoryId)) {
					ArrayList<LocationData> locationDataList = categoryWiseResponse.get(categoryId);
					LocationData lastAddLocationData = locationDataList.get(locationDataList.size() - 1);
					if (lastAddLocationData.getLat().equals(entry.getLat())
							&& lastAddLocationData.getLng().equals(entry.getLng())) {
						lastAddLocationData.setWeight(lastAddLocationData.getWeight() + entry.getWeight());
						locationDataList.set(locationDataList.size() - 1, lastAddLocationData);
					} else {
						LocationData newLocationData = new LocationData();
						newLocationData.setLat(entry.getLat());
						newLocationData.setLng(entry.getLng());
						newLocationData.setWeight(entry.getWeight());
						locationDataList.add(newLocationData);
					}
					categoryWiseResponse.put(categoryId, locationDataList);
				} else {
					LocationData newLocationData = new LocationData();
					newLocationData.setLat(entry.getLat());
					newLocationData.setLng(entry.getLng());
					newLocationData.setWeight(entry.getWeight());
					ArrayList<LocationData> locationDataList = new ArrayList<>();
					locationDataList.add(newLocationData);
					categoryWiseResponse.put(categoryId, locationDataList);
				}
			}
			List<MapResponse> mapResponse = new ArrayList<>();
			for (Map.Entry<String, ArrayList<LocationData>> categoryWiseEntry : categoryWiseResponse.entrySet()) {
				MapResponse response = new MapResponse();
				response.setCategoryId(categoryWiseEntry.getKey());
				response.setCategoryResult(categoryWiseEntry.getValue());
				mapResponse.add(response);
			}
			return mapResponse;
		} catch (Exception e) {
			logger.warn(e.toString());
		}
		return null;
	}

	@Getter
	@Setter
	public class LocationData {
		private Double lat;
		private Double lng;
		private int weight;
	}

	@Getter
	@Setter
	public class MapResponse {
		private String categoryId;
		private ArrayList<LocationData> categoryResult;
	}
}
