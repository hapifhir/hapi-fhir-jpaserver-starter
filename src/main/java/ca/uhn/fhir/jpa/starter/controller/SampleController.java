package ca.uhn.fhir.jpa.starter.controller;

import java.io.*;
import java.util.LinkedHashMap;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
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
	
	@RequestMapping(method = RequestMethod.GET,value = {"/read"})
	public String read(String id) {
		FhirResourceTemplateHelper.state("oyo");
//		FhirResourceTemplateHelper.lga("Ibadan South West", "oyo");
//		FhirResourceTemplateHelper.ward("Agbokojo", "oyo");
//		FhirResourceTemplateHelper.clinic("St Lucia Hospital", "oyo", "19145158", "30/08/1/1/1/0019", "+234-6789045655");
		return "test";
	}
	
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
			String[] csvData = singleLine.split(","); //state,lga,ward,facilityUID,facilityCode,facilityName,facilityLevel,countryCode,phoneNumber
			if (Validation.validateClinicAndStateCsvLine(csvData))
			{
			Location state = FhirResourceTemplateHelper.state(csvData[0]);
			helperService.create(state);
			Location lga = FhirResourceTemplateHelper.lga(csvData[1], csvData[0]);
			helperService.create(lga);
			Location ward = FhirResourceTemplateHelper.ward(csvData[0], csvData[1], csvData[2]);
			helperService.create(ward);
			Organization clinic = FhirResourceTemplateHelper.clinic(csvData[5],  csvData[3], csvData[4], csvData[7], csvData[8], csvData[0], csvData[1], csvData[2]);
			helperService.create(clinic);
			}
		}
		map.put("uploadCSV", "Successful");
		return new ResponseEntity(map,HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = {"/keycloak"})
	public void keycloakInitAndCreateGroup() {
		helperService.initializeKeycloak();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = {"/group"})
	public void keycloakCreateGroups(String group) {
		helperService.createGroup(group);
	}
}
