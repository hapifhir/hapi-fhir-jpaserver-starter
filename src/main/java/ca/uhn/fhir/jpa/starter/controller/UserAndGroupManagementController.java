package ca.uhn.fhir.jpa.starter.controller;

import android.util.Pair;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.ConfigDefinitionTypes;
import ca.uhn.fhir.jpa.starter.DashboardEnvironmentConfig;
import ca.uhn.fhir.jpa.starter.model.ApiAsyncTaskEntity;
import ca.uhn.fhir.jpa.starter.model.AnalyticItem;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import ca.uhn.fhir.jpa.starter.model.OCLQrResponse;
import ca.uhn.fhir.jpa.starter.model.OCLQrRequest;
import ca.uhn.fhir.jpa.starter.service.NotificationDataSource;
import ca.uhn.fhir.jpa.starter.service.HelperService;
import ca.uhn.fhir.jpa.starter.service.BigQueryService;
import ca.uhn.fhir.jpa.starter.service.QrService;
import ca.uhn.fhir.parser.IParser;
import com.iprd.fhir.utils.Validation;
import com.iprd.report.OrgItem;
import com.iprd.report.model.definition.ANCDailySummaryConfig;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@CrossOrigin(origins = {"http://localhost:3000/", "http://testhost.dashboard:3000/", "https://oclink.io/", "https://opencampaignlink.org/"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/iprd")
public class UserAndGroupManagementController {
	NotificationDataSource datasource = NotificationDataSource.getInstance();
	@Autowired
	HelperService helperService;
	@Autowired
	BigQueryService bigQueryService;
	@Autowired
	QrService qrService;
	@Autowired
	DashboardEnvironmentConfig dashboardEnvironmentConfig;

	private Map<String, Map<ConfigDefinitionTypes, String>> envToFileMap;

	private static final Logger logger = LoggerFactory.getLogger(UserAndGroupManagementController.class);

	@PostConstruct
	public void init() {
		envToFileMap = dashboardEnvironmentConfig.getEnvToFilePathMapping();
	}

	IParser iParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser();

	@RequestMapping(method = RequestMethod.POST, value = "/organizationBulkImport")
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadClinicsAndStates(@RequestParam("file") MultipartFile file) throws IOException {
		return helperService.createGroups(file);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/hcwBulkImport")
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadHcw(@RequestParam("file") MultipartFile file)
		throws Exception {
		return helperService.createUsers(file);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getTableData/{lastUpdated}")
	public ResponseEntity<?> getTableData(@PathVariable Long lastUpdated){
		return helperService.getTableData(lastUpdated);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/dashboardUserBulkImport")
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadDashboardUsers(@RequestParam("file") MultipartFile file) throws Exception {
		return helperService.createDashboardUsers(file);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/{userId}/groups")
	public List<GroupRepresentation> getGroupsByUser(@PathVariable String userId) {
		return helperService.getGroupsByUser(userId);
	}

//	@RequestMapping(method = RequestMethod.GET, value = "/getAncMetaDataByOrganizationId")
//	public ResponseEntity<List<Map<String, String>>> getAncMetaDataByOrganizationId(@RequestParam("organizationId") String organizationId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
//		return helperService.getAncMetaDataByOrganizationId(organizationId, startDate, endDate);
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/getAncDailySummaryData")
//	public ResponseEntity<?> getAncDailySummaryData(@RequestParam("env") String env,@RequestParam("organizationId") String organizationId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
//		return helperService.getAncDailySummaryData(organizationId, startDate, endDate, new LinkedHashMap<>(),env);
//	}

	@RequestMapping(method = RequestMethod.GET, value = "/details")
	public ResponseEntity<?> getDetails(
			@RequestParam("env") String env,
		@RequestParam Map<String, String> allFilters
	) throws SQLException, IOException {
		String organizationId = allFilters.get("lga");
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("lga");
		allFilters.remove("env");
		allFilters.remove("type");
		LinkedHashMap<String, String> filters = new LinkedHashMap<>(allFilters);

		LocalDateTime dateTimeNow = LocalDateTime.now();
		String[] extractedFromDateTimeNow = dateTimeNow.toString().split(":");
		List<String> categories = new ArrayList<>(Collections.emptyList());
		List<String> hashcodes = new ArrayList<>(Collections.emptyList());
		Map<String,String> categoryWithHashCodes = new HashMap<>();
		List<ANCDailySummaryConfig> ancDailySummaryConfig = helperService.getANCDailySummaryConfigFromFile(env);

		for (ANCDailySummaryConfig singleCategory : ancDailySummaryConfig) {
			categories.add(singleCategory.getCategoryId());
		}
		String hashOfFormattedId = "";
		for (String category : categories) {
			 hashOfFormattedId = organizationId + startDate + endDate + category + extractedFromDateTimeNow[0];
			categoryWithHashCodes.put(category,hashOfFormattedId);
			ArrayList<ApiAsyncTaskEntity> fetchAsyncData = datasource.fetchStatus(hashOfFormattedId);
			if (fetchAsyncData == null || fetchAsyncData.isEmpty()) {
			try {
				ApiAsyncTaskEntity apiAsyncTaskEntity = new ApiAsyncTaskEntity(hashOfFormattedId, ApiAsyncTaskEntity.Status.PROCESSING.name(), null, null);
				datasource.insert(apiAsyncTaskEntity);
			} catch (Exception e) {
				logger.warn(ExceptionUtils.getStackTrace(e));
			}
				hashcodes.add(hashOfFormattedId);
			if(categories.indexOf(category) == (categories.size()-1)) {
				helperService.saveQueryResult(organizationId, startDate, endDate, filters, hashcodes,env,ancDailySummaryConfig);
				return ResponseEntity.status(202).build();
						}
			}
		}
		if (helperService.getAsyncData(categoryWithHashCodes).getBody() == "Searching in Progress") return ResponseEntity.status(202).build();
		return ResponseEntity.ok(helperService.getAsyncData(categoryWithHashCodes));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getEncountersBelowLocation")
	public ResponseEntity<String> getEncountersBelowLocation(@RequestParam("locationId") String locationId) {
		Bundle bundle = helperService.getEncountersBelowLocation(locationId);
		return ResponseEntity.ok(iParser.encodeResourceToString(bundle));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getOrganizations")
	public ResponseEntity<List<OrgItem>> getOrganizations(@RequestParam("organizationId") String organizationId) {
		List<OrgItem> orgItemsList = helperService.getOrganizationHierarchy(organizationId);
		return ResponseEntity.ok(orgItemsList);
	}


	@RequestMapping(method = RequestMethod.GET, value = "/organizations")
	public ResponseEntity<?> organizations(@RequestHeader(name = "Authorization") String token) {
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		List<OrgItem> orgItemsList = helperService.getOrganizationsByPractitionerRoleId(practitionerRoleId);
		return ResponseEntity.ok(orgItemsList);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/barchartDefinition")
	public ResponseEntity<?> barchartDefinition(@RequestParam("env") String env) {
		return helperService.getBarChartDefinition(env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/indicator")
	public ResponseEntity<?> indicator(@RequestParam("env") String env) {
		return helperService.getIndicators(env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/categories")
	public ResponseEntity<?> categories(@RequestParam("env") String env) {
		return helperService.getCategories(env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/pieChartDefinition")
	public ResponseEntity<?> pieChartDefinition(@RequestParam("env") String env){
		return helperService.getPieChartDefinition(env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/linechartdefinition")
	public ResponseEntity<?> lineChartDefinition(@RequestParam("env") String env) {
		return helperService.getLineChartDefinitions(env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/tabularIndicator")
	public ResponseEntity<?> tabularIndicators(@RequestParam("env") String env) {
		return helperService.getTabularIndicators(env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/filters")
	public ResponseEntity<?> filter(@RequestParam("env") String env) {
		return helperService.getFilters(env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/data")
	public ResponseEntity<?> data(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env,
		@RequestParam Map<String, String> allFilters
	) {
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		ReportType type = ReportType.valueOf(allFilters.get("type"));
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("type");
		allFilters.remove("lga");
		allFilters.remove("env");
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.putAll(allFilters);
		return helperService.getDataByPractitionerRoleId(practitionerRoleId, startDate, endDate, type,filters,env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/linechart")
	public ResponseEntity<?> lineChart(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env,
		@RequestParam Map<String, String> allFilters
	) {
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		ReportType type = ReportType.valueOf(allFilters.get("type"));
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("type");
		allFilters.remove("lga");
		allFilters.remove("env");
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.putAll(allFilters);
		return helperService.getLineChartByPractitionerRoleId(practitionerRoleId, startDate, endDate, type,filters,env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/pieChartData")
	public ResponseEntity<?> pieChartData(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env,
		@RequestParam Map<String, String> allFilters
	){
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("type");
		allFilters.remove("lga");
		allFilters.remove("env");
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.putAll(allFilters);

		return helperService.getPieChartDataByPractitionerRoleId(practitionerRoleId, startDate, endDate,filters,env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/tabularData")
	public ResponseEntity<?> getTabularData(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env,
		@RequestParam Map<String, String> allFilters
	) {
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("lga");
		allFilters.remove("env");
		allFilters.remove("type");
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		LinkedHashMap<String, String> filters = new LinkedHashMap<>(allFilters);
		return helperService.getTabularDataByPractitionerRoleId(practitionerRoleId, startDate, endDate, filters,env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/refreshMapToOrgId")
	public ResponseEntity<?> refreshMapToOrgId(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("orgId") String orgId
	) {
		helperService.refreshMapForOrgId(orgId);
		return ResponseEntity.ok("Refresh done");
	}

	@RequestMapping(method = RequestMethod.GET, value = "/barChartData")
	public ResponseEntity<?> getBarChartData(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env,
		@RequestParam Map<String, String> allFilters
	) {
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("type");
		allFilters.remove("lga");
		allFilters.remove("env");
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.putAll(allFilters);
		return helperService.getBarChartData(practitionerRoleId, startDate, endDate,filters,env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/analytics")
	public ResponseEntity<?> analytics(@RequestHeader(name = "Authorization") String token,@RequestParam("env") String env) {
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		Organization organization = helperService.getOrganizationResourceByPractitionerRoleId(practitionerRoleId);
		if (organization == null) {
			return ResponseEntity.ok("Error : This user is not mapped to any organization");
		}
		List<AnalyticItem> analyticItems = new ArrayList<>();
		try {
			List<AnalyticItem> timeSpentAnalyticsItems = bigQueryService.timeSpentOnScreenAnalyticItems(organization);
			if (timeSpentAnalyticsItems == null) {
				return ResponseEntity.ok("Error: Unable to find file or fetch screen view information");
			}
			analyticItems.addAll(timeSpentAnalyticsItems);
		}catch(Exception e) {

		}

		List<AnalyticItem> maternalAnalyticsItems = helperService.getMaternalAnalytics(organization.getIdElement().getIdPart(),env);
		if (maternalAnalyticsItems == null) {
			return ResponseEntity.ok("Error: Unable to find analytics file");
		}
		analyticItems.addAll(maternalAnalyticsItems);
		analyticItems.add(helperService.getPatientCount(practitionerRoleId));
		return ResponseEntity.ok(analyticItems);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/oclQr")
	public ResponseEntity<OCLQrResponse> oclQr(@RequestParam(name = "baseUrl", defaultValue = "") String baseUrl,
															 @RequestParam(name = "campaignGuid", defaultValue = "") String campaignGuid,
															 @RequestParam(name = "campaignName", defaultValue = "") String campaignName,
															 @RequestParam(name = "campaignUrl", defaultValue = "") String campaignUrl,
															 @RequestParam(name = "location", defaultValue = "") String location,
															 @RequestParam(name = "locationPre", defaultValue = "") String locationPre,
															 @RequestParam(name = "timePre", defaultValue = "") String timePre,
															 @RequestParam(name = "verticalCode", defaultValue = "") String verticalCode,
															 @RequestParam(name = "verticalDescription", defaultValue = "") String verticalDescription,
															 @RequestParam(name = "userDefinedData", defaultValue = "") String userDefinedData,
															 @RequestParam(name = "humanReadableFlag", defaultValue = "true") boolean humanReadableFlag,
															 @RequestParam(name = "errorCorrectionLevelBits", defaultValue = "2") int errorCorrectionLevelBits
	) {
		OCLQrRequest oclQrRequest = new OCLQrRequest();
		oclQrRequest.setBaseUrl(baseUrl);
		oclQrRequest.setCampGuid(campaignGuid);
		oclQrRequest.setCampName(campaignName);
		oclQrRequest.setCampUrl(campaignUrl);
		oclQrRequest.setLocation(location);
		oclQrRequest.setLocationPre(locationPre);
		oclQrRequest.setTimePre(timePre);
		oclQrRequest.setVerticalCode(verticalCode);
		oclQrRequest.setVerticalDescription(verticalDescription);
		oclQrRequest.setUserDefinedData(userDefinedData);
		oclQrRequest.setHumanReadableFlag(humanReadableFlag);
		oclQrRequest.setErrorCorrectionLevelBits(errorCorrectionLevelBits);
		return ResponseEntity.ok(qrService.getOclQr(oclQrRequest));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/cacheDashboardDataSequential")
	public ResponseEntity<?> cacheDashboardDataSequential(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("from") String from,
		@RequestParam("to") String to,
		@RequestParam(value = "organizationId", required = false) String organizationId,
		@RequestParam("env") String env) {
		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		if (organizationId == null) {
			organizationId = helperService.getOrganizationIdByPractitionerRoleId(practitionerRoleId);
			if (organizationId == null) {
				return ResponseEntity.ok("Error : This user is not mapped to any organization");
			}
		}
		Pair<List<String>, LinkedHashMap<String, List<String>>> idsAndOrgIdToChildrenMapPair = helperService.fetchIdsAndOrgIdToChildrenMapPair(organizationId);
		helperService.cacheDashboardData(idsAndOrgIdToChildrenMapPair.first, from, to, env);
		return ResponseEntity.ok("Caching in Progress");
	}
}
