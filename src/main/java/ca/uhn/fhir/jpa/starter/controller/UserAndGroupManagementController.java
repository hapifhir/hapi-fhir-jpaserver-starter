package ca.uhn.fhir.jpa.starter.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.model.AnalyticItem;
import ca.uhn.fhir.jpa.starter.model.ApiAsyncTaskEntity;
import ca.uhn.fhir.jpa.starter.model.ReportType;
import ca.uhn.fhir.jpa.starter.service.*;
import ca.uhn.fhir.parser.IParser;
import com.iprd.fhir.utils.Validation;
import com.iprd.report.OrgItem;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDateTime;

@CrossOrigin(origins = {"http://localhost:3000/", "http://testhost.dashboard:3000/", "https://oclink.io/", "https://opencampaignlink.org/"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/iprd")

public class UserAndGroupManagementController {
	NotificationDataSource datasource = NotificationDataSource.getInstance();
	@Autowired
	HelperService helperService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	BigQueryService bigQueryService;

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

	@RequestMapping(method = RequestMethod.POST, value = "/dashboardUserBulkImport")
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadDashboardUsers(@RequestParam("file") MultipartFile file) throws Exception {
		return helperService.createDashboardUsers(file);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/{userId}/groups")
	public List<GroupRepresentation> getGroupsByUser(@PathVariable String userId) {
		return helperService.getGroupsByUser(userId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getAncMetaDataByOrganizationId")
	public ResponseEntity<List<Map<String, String>>> getAncMetaDataByOrganizationId(@RequestParam("organizationId") String organizationId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
		return helperService.getAncMetaDataByOrganizationId(organizationId, startDate, endDate);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getAncDailySummaryData")
	public ResponseEntity<?> getAncDailySummaryData(@RequestParam("organizationId") String organizationId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
		return helperService.getAncDailySummaryData(organizationId, startDate, endDate, new LinkedHashMap<>());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/details")
	public ResponseEntity<?> getDetails(
		@RequestParam Map<String, String> allFilters
	) throws SQLException, IOException {
		String organizationId = allFilters.get("lga");
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("lga");
		LinkedHashMap<String, String> filters = new LinkedHashMap<>(allFilters);

		LocalDateTime dateTimeNow = LocalDateTime.now();
		String[] extractedFromDateTimeNow = dateTimeNow.toString().split(":");
		String FormattedId = organizationId + startDate + endDate + extractedFromDateTimeNow[0];
		String hashOfFormattedId = String.valueOf(FormattedId.hashCode());

		ArrayList<ApiAsyncTaskEntity> fetchAsyncData = datasource.fetchStatus(hashOfFormattedId);

		if (fetchAsyncData == null || fetchAsyncData.isEmpty()) {
			try {
				ApiAsyncTaskEntity apiAsyncTaskEntity = new ApiAsyncTaskEntity(hashOfFormattedId, ApiAsyncTaskEntity.Status.PROCESSING.name(), null, null);
				datasource.insert(apiAsyncTaskEntity);
			} catch (Exception e) {
				e.printStackTrace();
			}

			helperService.saveQueryResult(organizationId, startDate, endDate, filters, hashOfFormattedId);
			return ResponseEntity.ok(hashOfFormattedId);
		}
		return ResponseEntity.ok(helperService.checkIfDataExistsInAsyncTable(hashOfFormattedId));

	}


	@RequestMapping(method = RequestMethod.GET, value = "/details/{uuid}")
	public ResponseEntity<?> getDataResult(@PathVariable String uuid) throws SQLException, IOException {
		return ResponseEntity.ok(helperService.checkIfDataExistsInAsyncTable(uuid));
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
		String practitionerRoleId = Validation.getPractitionerRoleIdByToken(token);
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		List<OrgItem> orgItemsList = helperService.getOrganizationsByPractitionerRoleId(practitionerRoleId);
		return ResponseEntity.ok(orgItemsList);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/indicator")
	public ResponseEntity<?> indicator() {
		return helperService.getIndicators();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/pieChartDefinition")
	public ResponseEntity<?> pieChartDefinition(){
		return helperService.getPieChartDefinition();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/linechartdefinition")
	public ResponseEntity<?> lineChartDefinition() {
		return helperService.getLineChartDefinitions();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/tabularIndicator")
	public ResponseEntity<?> tabularIndicators() {
		return helperService.getTabularIndicators();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/filters")
	public ResponseEntity<?> filter() {
		return helperService.getFilters();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/data")
	public ResponseEntity<?> data(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam Map<String, String> allFilters
	) {
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		ReportType type = ReportType.valueOf(allFilters.get("type"));
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("type");
		allFilters.remove("lga");
		String practitionerRoleId = Validation.getPractitionerRoleIdByToken(token);
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.putAll(allFilters);
		if (!filters.isEmpty()) {
			return helperService.getDataByPractitionerRoleIdWithFilters(practitionerRoleId, startDate, endDate, type, filters);
		}
		return helperService.getDataByPractitionerRoleId(practitionerRoleId, startDate, endDate, type);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/linechart")
	public ResponseEntity<?> lineChart(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam Map<String, String> allFilters
	) {
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		ReportType type = ReportType.valueOf(allFilters.get("type"));
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("type");
		allFilters.remove("lga");
		String practitionerRoleId = Validation.getPractitionerRoleIdByToken(token);
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.putAll(allFilters);
		if (!filters.isEmpty()) {
			return helperService.getLineChartByPractitionerRoleIdWithFilters(practitionerRoleId, startDate, endDate, type, filters);
		}
		return helperService.getLineChartByPractitionerRoleId(practitionerRoleId, startDate, endDate, type);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/pieChartData")
	public ResponseEntity<?> pieChartData(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam Map<String, String> allFilters
	){
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("type");
		allFilters.remove("lga");
		String practitionerRoleId = Validation.getPractitionerRoleIdByToken(token);
		LinkedHashMap<String, String> filters = new LinkedHashMap<>();
		filters.putAll(allFilters);
		return helperService.getPieChartData(practitionerRoleId, startDate, endDate, filters);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/tabularData")
	public ResponseEntity<?> getTabularData(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam Map<String, String> allFilters
	) {
		String startDate = allFilters.get("from");
		String endDate = allFilters.get("to");
		allFilters.remove("from");
		allFilters.remove("to");
		allFilters.remove("lga");
		String practitionerRoleId = Validation.getPractitionerRoleIdByToken(token);
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		LinkedHashMap<String, String> filters = new LinkedHashMap<>(allFilters);
		return helperService.getTabularDataByPractitionerRoleId(practitionerRoleId, startDate, endDate, filters);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/analytics")
	public ResponseEntity<?> analytics(@RequestHeader(name = "Authorization") String token) {
		String practitionerRoleId = Validation.getPractitionerRoleIdByToken(token);
		if (practitionerRoleId == null) {
			return ResponseEntity.ok("Error : Practitioner Role Id not found in token");
		}
		Organization organization = helperService.getOrganizationResourceByPractitionerRoleId(practitionerRoleId);
		if (organization == null) {
			return ResponseEntity.ok("Error : This user is not mapped to any organization");
		}
		List<AnalyticItem> analyticItems = new ArrayList<>();
		List<AnalyticItem> timeSpentAnalyticsItems = bigQueryService.timeSpentOnScreenAnalyticItems(organization);
		if (timeSpentAnalyticsItems == null) {
			return ResponseEntity.ok("Error: Unable to find file or fetch screen view information");
		}
		analyticItems.addAll(timeSpentAnalyticsItems);
		List<AnalyticItem> maternalAnalyticsItems = helperService.getMaternalAnalytics(organization.getId());
		if (maternalAnalyticsItems == null) {
			return ResponseEntity.ok("Error: Unable to find analytics file");
		}
		analyticItems.addAll(maternalAnalyticsItems);
		return ResponseEntity.ok(analyticItems);
	}
}
