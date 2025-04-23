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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
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

	@RequestMapping(method = RequestMethod.POST, value = "/dashboardUserBulkImport")
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadDashboardUsers(@RequestParam("file") MultipartFile file) throws Exception {
		return helperService.createDashboardUsers(file);
	}
	
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
		@RequestMapping(method = RequestMethod.GET, value = "/getOrganizations")
	public ResponseEntity<List<OrgItem>> getOrganizations(@RequestParam("organizationId") String organizationId) {
		List<OrgItem> orgItemsList = helperService.getOrganizationHierarchy(organizationId);
		return ResponseEntity.ok(orgItemsList);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/refreshMapToOrgId")
	public ResponseEntity<?> refreshMapToOrgId(
		@RequestHeader(name = "Authorization") String token,
		@RequestParam("orgId") String orgId
	) {
		helperService.refreshMapForOrgId(orgId);
		return ResponseEntity.ok("Refresh done");
	}

	@RequestMapping(method = RequestMethod.POST, value = "/addOrgIdToTable")
	public ResponseEntity<?> addOrgIdToTable(
	) {
		helperService.updatePatientIdentifierEntityTable();
		return ResponseEntity.ok("Updated patientIdentifierEntity table");
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
}
