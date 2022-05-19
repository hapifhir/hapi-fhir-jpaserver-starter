package ca.uhn.fhir.jpa.starter.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.apache.jena.sparql.function.library.print;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.iprd.fhir.utils.FhirResourceTemplateHelper;

import ca.uhn.fhir.jpa.starter.service.HelperService;

@RestController
@RequestMapping("/iprd")
public class SampleController {
	
	@Autowired
	HelperService helperService;
	
	@RequestMapping(method = RequestMethod.GET,value = {"/read"})
	public String read(String id) {
		FhirResourceTemplateHelper.state("oyo");
		FhirResourceTemplateHelper.lga("Ibadan South West", "oyo");
		FhirResourceTemplateHelper.ward("Agbokojo", "oyo");
		FhirResourceTemplateHelper.clinic("St Lucia Hospital", "oyo");
		return "test";
	}
	
	@RequestMapping(method = RequestMethod.POST,value = {"/create"})
	public ResponseEntity<LinkedHashMap<String, Object>> create( 
			@RequestParam(value = "id",required = false,defaultValue = "") String id, 
			 @RequestParam(value = "name",required = false,defaultValue = "") String name) {
		ResponseEntity resp = helperService.create(id, name);
		return resp;
	}
}
