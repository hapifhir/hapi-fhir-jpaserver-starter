package ca.uhn.fhir.jpa.starter.service;

import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

import java.util.LinkedHashMap;

import org.hl7.fhir.instance.model.api.IIdType;

@Service
public class HelperService {
	FhirContext ctx = FhirContext.forR4();
	String serverBase = "http://localhost:8080/fhir";
    IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

	public ResponseEntity<LinkedHashMap<String, Object>> create(String id, String name) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		Patient pt = new Patient();
		pt.addName().setFamily(name);
		pt.setId("123");
		IIdType idFromCreate = fhirClient.create().resource(pt).execute().getId();
		map.put("id",idFromCreate);
        map.put("description", "Patient Created");
        map.put("status", "Success");
        map.put("state", "200");
        
		return new ResponseEntity<LinkedHashMap<String, Object>>(map, HttpStatus.OK);
	}
}
