package ca.uhn.fhir.jpa.starter.controller;

import java.io.*;
import java.util.LinkedHashMap;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iprd.fhir.utils.FhirResourceTemplateHelper;
import com.iprd.fhir.utils.Validation;
import com.mysql.cj.result.Row;

import ca.uhn.fhir.jpa.starter.service.HelperService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/iprd")
public class SampleController {
	
	@Autowired
	HelperService helperService;
	
	@RequestMapping(method = RequestMethod.POST,value = {"/uploadCsvFile"})
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadClinicsAndStates(@RequestParam("file") MultipartFile file) throws IOException {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
		String singleLine;
		int iteration = 0;
		while((singleLine = bufferedReader.readLine())!=null){
			if(iteration == 0 && singleLine.contains("state")) { //skip header of CSV file
				iteration++;
				continue;
			}
			String[] csvData = singleLine.split(","); //state,lga,ward,facilityUID,facilityCode,countryCode,phoneNumber,facilityName,facilityLevel
			if (Validation.validateClinicAndStateCsvLine(csvData))
			{
			Location state = FhirResourceTemplateHelper.state(csvData[0]);
			helperService.createResource(state);
			Location lga = FhirResourceTemplateHelper.lga(csvData[1],csvData[0]);
			helperService.createResource(lga);
			Location ward = FhirResourceTemplateHelper.ward(csvData[0],csvData[1],csvData[2]);
			helperService.createResource(ward);
			Organization clinic = FhirResourceTemplateHelper.clinic(csvData[5],csvData[3],csvData[4],csvData[5],csvData[6],csvData[0],csvData[1],csvData[2]);
			helperService.createResource(clinic);
			}
		}
		map.put("uploadCSV", "Successful");
		return new ResponseEntity(map,HttpStatus.OK);
	}
	
	
	@RequestMapping(method = RequestMethod.POST,value = {"/hcwBulkImport"})
	public ResponseEntity<LinkedHashMap<String, Object>> bulkUploadHcw(@RequestParam("file") MultipartFile file) throws Exception{
		return helperService.createUsers(file);
	}
	
}
