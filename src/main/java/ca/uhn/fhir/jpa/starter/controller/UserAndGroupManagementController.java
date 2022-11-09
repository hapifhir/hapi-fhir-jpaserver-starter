package ca.uhn.fhir.jpa.starter.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.iprd.report.DataResult;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.service.HelperService;
import ca.uhn.fhir.jpa.starter.service.NotificationService;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;

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
	
	@RequestMapping(method = RequestMethod.GET, value = "/getOrganizationHierarchy")
	public ResponseEntity<?> getOrganizationHierarchy(@RequestParam("organizationId") String organizationId) {
		helperService.getOrganizationHierarchy(organizationId);
		return ResponseEntity.ok("");
	}
}
