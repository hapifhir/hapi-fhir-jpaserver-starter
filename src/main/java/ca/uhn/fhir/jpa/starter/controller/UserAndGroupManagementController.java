package ca.uhn.fhir.jpa.starter.controller;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
	
	@RequestMapping(method = RequestMethod.POST,value = {"/organizationBulkImport"})
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadClinicsAndStates(@RequestParam("file") MultipartFile file) throws IOException {
		return helperService.createGroups(file);
	}
	
	
	@RequestMapping(method = RequestMethod.POST,value = {"/hcwBulkImport"})
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadHcw(@RequestParam("file") MultipartFile file) throws Exception{
		return helperService.createUsers(file);
	}
}
