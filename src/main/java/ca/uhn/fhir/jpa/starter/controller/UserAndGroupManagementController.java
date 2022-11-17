package ca.uhn.fhir.jpa.starter.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.service.HelperService;
import ca.uhn.fhir.jpa.starter.service.NotificationService;
import ca.uhn.fhir.parser.IParser;
import com.iprd.fhir.utils.Validation;
import com.iprd.report.DataResult;
import com.iprd.report.IndicatorItem;
import com.iprd.report.OrgItem;
import com.iprd.report.ScoreCardItem;
import org.hl7.fhir.r4.model.Bundle;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000","https://oclink.io","https://opencampaignlink.org"}, maxAge = 3600,  allowCredentials = "true")
@RestController
@RequestMapping("/iprd")
public class UserAndGroupManagementController {

	@Autowired
	HelperService helperService;
	@Autowired
	NotificationService notificationService;

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
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadDashboardUsers(@RequestParam("file") MultipartFile file) throws Exception{
		return helperService.createDashboardUsers(file);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/user/{userId}/groups")
	public List<GroupRepresentation> getGroupsByUser(@PathVariable String userId) {
		return helperService.getGroupsByUser(userId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getAncMetaDataByOrganizationId")
	public ResponseEntity<List<Map<String, String>>> getAncMetaDataByOrganizationId(@RequestParam("organizationId") String organizationId,@RequestParam("startDate") String startDate,@RequestParam("endDate") String endDate) {
		return helperService.getAncMetaDataByOrganizationId(organizationId, startDate, endDate);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getAncDailySummaryData")
	public ResponseEntity<DataResult> getAncDailySummaryData(@RequestParam("organizationId") String organizationId,@RequestParam("startDate") String startDate,@RequestParam("endDate") String endDate) {
		return helperService.getAncDailySummaryData(organizationId, startDate, endDate);
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
	public ResponseEntity<List<OrgItem>> organizations(@RequestHeader(name="Authorization") String token) {
		String practitionerRoleId = Validation.getPractitionerRoleIdByToken(token);
		if(practitionerRoleId == null) {
			return ResponseEntity.badRequest().build();
		}
		List<OrgItem> orgItemsList = helperService.getOrganizationsByPractitionerRoleId(practitionerRoleId);
		return ResponseEntity.ok(orgItemsList);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/indicator")
	public ResponseEntity<List<IndicatorItem>> indicator() throws FileNotFoundException {
		List<IndicatorItem> items = helperService.getIndicators();
		return ResponseEntity.ok(items);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/data")
	public ResponseEntity<List<ScoreCardItem>> data(@RequestHeader(name="Authorization") String token, @RequestParam("from") String startDate, @RequestParam("to") String endDate) throws FileNotFoundException {
		String practitionerRoleId = Validation.getPractitionerRoleIdByToken(token);
		if(practitionerRoleId == null) {
			return ResponseEntity.badRequest().build();
		}
		return helperService.getDataByPractitionerRoleId(practitionerRoleId, startDate, endDate);
	}
}
