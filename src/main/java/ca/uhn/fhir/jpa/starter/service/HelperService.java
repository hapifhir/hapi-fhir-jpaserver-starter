package ca.uhn.fhir.jpa.starter.service;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
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

	public void create(Resource name) {
		IIdType idFromCreate = fhirClient.create().resource(name).execute().getId();
	}
}
