package ca.uhn.fhir.jpa.starter.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ca.uhn.fhir.jpa.starter.service.HelperService;

@RestController
@RequestMapping("/iprd")
public class UserAndGroupManagementController {

	@Autowired
	HelperService helperService;

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

	@RequestMapping(method = RequestMethod.GET, value = "/generateDailyReport")
	public ResponseEntity<InputStreamResource> generateDailyReport(@RequestParam("date") String date,
			@RequestParam("organizationId") String organizationId, @RequestBody List<List<String>> fhirExpressions) {
		return helperService.generateDailyReport(date, organizationId, fhirExpressions);
	}
}
