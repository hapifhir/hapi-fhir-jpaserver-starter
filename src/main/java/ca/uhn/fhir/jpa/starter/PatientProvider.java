package ca.uhn.fhir.jpa.starter;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.jpa.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.rp.r4.PatientResourceProvider;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;


public class PatientProvider extends PatientResourceProvider{
	private IFhirSystemDao<org.hl7.fhir.r4.model.Bundle, org.hl7.fhir.r4.model.Meta> systemDao;
	private IFhirResourceDao<Coverage> coverageDao;
	PatientProvider(IFhirSystemDao<org.hl7.fhir.r4.model.Bundle, org.hl7.fhir.r4.model.Meta> systemDao,IFhirResourceDao<Coverage> coverageDao){
		this.systemDao = systemDao;
		this.coverageDao = coverageDao;
	}
	@Override
	public Class<Patient> getResourceType() {
	      return Patient.class;
	}
	
	
	private class CustomException extends BaseServerResponseException{

		public CustomException(int theStatusCode, String theMessage) {
			super(theStatusCode, theMessage);
			// TODO Auto-generated constructor stub
		}
		
	}

	@Operation(name = "$build", idempotent = true)
	public  Bundle build(RequestDetails theRequestDetails,
			@OperationParam(name = "bundle", min = 1, max = 1, type =  Bundle.class) Bundle bundle) {
//		System.out.println("\n\n op call ssssss"+this.getDao());
		
		return bundle;
		
	}
	
	@Operation(name = "$payer-match")
	public  Bundle payerMatch(RequestDetails theRequestDetails,
			@OperationParam(name = "bundle", min = 1, max = 1, type =  Bundle.class) Bundle bundle) {
		System.out.println("\n\n op call ssssss"+bundle+"---"+bundle.getEntry());
		Bundle responseBundle = new Bundle();
		int i =0;
		for(BundleEntryComponent entry : bundle.getEntry()) {
			
			System.out.println(entry+"--"+entry.getResource());
			if(entry.getResource().getResourceType().toString().equals("Coverage")) {
				Coverage oldPlan =  (Coverage)entry.getResource();
				System.out.println(oldPlan.getIdentifier()+"--"+oldPlan.getIdentifier().size());
				if(oldPlan.getIdentifier().size() > 0) {
					boolean found = false;
					for(int j=0; j<oldPlan.getIdentifier().size();j++) {
						SearchParameterMap map = new SearchParameterMap();
						map.add("identifier", new TokenParam(oldPlan.getIdentifier().get(j).getValue()));
						IBundleProvider coveragesFound = this.coverageDao.search(map);
						if(coveragesFound.size() > 0) {
							found = true;
							for(IBaseResource coverageItem:coveragesFound.getResources(0, 1)) {
								Coverage coverage = (Coverage) coverageItem;
								String patientId = coverage.getBeneficiary().getReference();
								System.out.println("SubjID: "+patientId);
								Patient patient = this.getDao().read(new IdType(patientId));
								System.out.println("Subj: "+patient);
					            BundleEntryComponent patientEntry = new BundleEntryComponent().setResource(patient);
					            BundleEntryComponent coverageEntry = new BundleEntryComponent().setResource(coverage);
					            responseBundle.addEntry(patientEntry);
					            responseBundle.addEntry(coverageEntry);
					            responseBundle.setType(Bundle.BundleType.TRANSACTION);
					            return responseBundle;
					            
							}
						}
					}
					if(!found) {
						throw new CustomException(404,"Coverage with given identifier was not found");

					}
					
					
				}
				else {
					throw new CustomException(404,"Identifier is mandatory in coverage");
				}
			}
			else if(i == 1) {
				throw new CustomException(404,"Resource Type Coverage is expected");
			}
			i++;
		}
		return responseBundle;
		
	}
	
	

	
}