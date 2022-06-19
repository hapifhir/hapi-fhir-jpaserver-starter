package ca.uhn.fhir.jpa.starter.service;

import com.iprd.report.FhirClientProvider;

import ca.uhn.fhir.rest.client.impl.GenericClient;

public class FhirClientProviderImpl implements FhirClientProvider {
	
	GenericClient genericClient;
	
	public FhirClientProviderImpl(GenericClient client) {
		genericClient = client;
	}

	@Override
	public Object getFhirClient() {
		return genericClient;
	}

}
