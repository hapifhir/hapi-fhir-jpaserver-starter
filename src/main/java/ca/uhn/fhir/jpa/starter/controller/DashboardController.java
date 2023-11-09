package ca.uhn.fhir.jpa.starter.controller;

import android.util.Pair;
import ca.uhn.fhir.jpa.starter.ConfigDefinitionTypes;
import ca.uhn.fhir.jpa.starter.DashboardEnvironmentConfig;
import ca.uhn.fhir.jpa.starter.model.AnalyticItem;
import ca.uhn.fhir.jpa.starter.model.ApiAsyncTaskEntity;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import ca.uhn.fhir.jpa.starter.service.BigQueryService;
import ca.uhn.fhir.jpa.starter.service.HelperService;
import ca.uhn.fhir.jpa.starter.service.NotificationDataSource;
import com.iprd.fhir.utils.Validation;
import com.iprd.report.OrgItem;
import com.iprd.report.model.definition.ANCDailySummaryConfig;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = {"http://localhost:3000/", "http://testhost.dashboard:3000/", "https://oclink.io/", "https://opencampaignlink.org/"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/iprd/web")
public class DashboardController {
	NotificationDataSource datasource = NotificationDataSource.getInstance();
	@Autowired
	HelperService helperService;
	@Autowired
	BigQueryService bigQueryService;
	@Autowired
	DashboardEnvironmentConfig dashboardEnvironmentConfig;

	private Map<String, Map<ConfigDefinitionTypes, String>> envToFileMap;

	private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@PostConstruct
	public void init() {
		envToFileMap = dashboardEnvironmentConfig.getEnvToFilePathMapping();
	}


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
		allFilters.remove("counter");
		LinkedHashMap<String, String> filters = new LinkedHashMap<>(allFilters);

		Map<String,String> categoryWithHashCodes = helperService.processCategories(organizationId, startDate, endDate, env, filters,true);

		if (helperService.getAsyncData(categoryWithHashCodes).getBody() == "Searching in Progress") return ResponseEntity.status(202).build();
		return ResponseEntity.ok(helperService.getAsyncData(categoryWithHashCodes));
	}


	@RequestMapping(method = RequestMethod.GET, value = "/lastSyncTime")
	public ResponseEntity<?> getLastSyncTime(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env){

		String practitionerRoleId = Validation.getJWTToken(token).getPractitionerRoleId();
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}

		return helperService.computeSyncTime(practitionerRoleId,env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getTableData/{lastUpdated}")
	public ResponseEntity<?> getTableData(@PathVariable Long lastUpdated){
		return helperService.getTableData(lastUpdated);
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
	@RequestMapping(method = RequestMethod.GET, value = "/environments")
	public ResponseEntity<?> environments() {
		return helperService.getEnvironmentOptions();
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

	@RequestMapping(method = RequestMethod.GET, value = "/organizationStructure")
	public ResponseEntity<?> organizationStructure(
		@RequestParam("orgId") String orgId,
		@RequestParam("parentId") String parentId) {
		helperService.saveOrganizationStructure(orgId, parentId);
		return ResponseEntity.ok("Data insertion into 'organization structure' table has started");
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
		return helperService.getDataByPractitionerRoleId(practitionerRoleId, startDate, endDate, type, filters, env);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/linechart")
	public ResponseEntity<?> lineChart(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env,
		@RequestParam("lga") String lga,
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
		return helperService.getLineChartByPractitionerRoleId(startDate,endDate,type,filters,env,lga);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/pieChartData")
	public ResponseEntity<?> pieChartData(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env,
		@RequestParam("lga") String lga,
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

		return helperService.getPieChartDataByPractitionerRoleId(startDate, endDate,filters,env,lga);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/tabularData")
	public ResponseEntity<?> getTabularData(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("env") String env,
		@RequestParam("lga") String lga,
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
		return helperService.getTabularDataByPractitionerRoleId(startDate, endDate, filters,env,lga);
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
		@RequestParam("lga") String lga,
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
		return helperService.getBarChartData(startDate, endDate,filters,env,lga);
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

	@RequestMapping(method = RequestMethod.GET, value = "/maps")
	public List<HelperService.MapResponse> getEncountersForMap(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam ("lga") String orgId,
		@RequestParam("from") String from,
		@RequestParam("to") String to
	) {
		return helperService.getEncounterForMap(orgId, from, to);
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
