package ca.uhn.fhir.jpa.starter;

import java.util.Random;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;

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
import org.hl7.fhir.r4.model.Identifier;
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
	
	
	public String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 16) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

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
	
	@Operation(name = "$member-match")
	public  Parameters memberMatch(RequestDetails theRequestDetails,
			@OperationParam(name = "MemberPatient", min = 1, max = 1, type = Patient.class) Patient patientResource,
			@OperationParam(name = "OldCoverage", min = 1, max = 1, type = Coverage.class) Coverage oldCoverage,
			@OperationParam(name = "NewCoverage", min = 1, max = 1, type = Coverage.class) Coverage newCoverage,
			@OperationParam(name = "exact", min = 1, max = 1,type = BooleanType.class) BooleanType exact
			
			
			) {
		Parameters response = new Parameters();
		response.addParameter().setName("exact").setValue(exact);
//		System.out.println("MP: "+patientResource);
//		System.out.println("c1: "+oldCoverage);
//		System.out.println("c2: "+newCoverage);
//		System.out.println(exact);
		if(oldCoverage.getIdentifier().size() > 0) {
			boolean found = false;
			for(int j=0; j<oldCoverage.getIdentifier().size();j++) {
				SearchParameterMap map = new SearchParameterMap();
				map.add("identifier", new TokenParam(oldCoverage.getIdentifier().get(j).getValue()));
				IBundleProvider coveragesFound = this.coverageDao.search(map);
				if(coveragesFound.size() > 0) {
					found = true;
					for(IBaseResource coverageItem:coveragesFound.getResources(0, 1)) {
						Coverage coverage = (Coverage) coverageItem;
						String patientId = coverage.getBeneficiary().getReference();
//						System.out.println("SubjID: "+patientId);
						Patient patient = this.getDao().read(new IdType(patientId));
						
						Identifier identifier = new Identifier();
						CodeableConcept coding = new CodeableConcept();
						coding.addCoding().setSystem("http://hl7.davinci.org").setCode("UMB");
						String identifierValue = "";
						if(patient.getIdentifier().size() > 0) {
							for(Identifier identifierEntry : patient.getIdentifier()) {
//								if(identifierEntry.getSystem().equals("urn:oid:3.111.757.111.21")) {
//									identifierValue = identifierEntry.getValue();
//								}
								if(identifierEntry.getType().hasCoding()) {
									if(identifierEntry.getType().getCodingFirstRep().getCode().equals("UMB")) {
										patientResource.addIdentifier(identifierEntry);
									}
								}
							}
							
						}
						
						/*
						identifier.setValue(identifierValue);
						identifier.setType(coding);
						if(coverage.getIdentifier().size() > 0) {
//							coverage.getIdentifier().get(0).getSystem();
							identifier.setSystem(coverage.getIdentifier().get(0).getSystem());
							if(coverage.getPayor().size()>0) {
								identifier.setAssigner(new Reference().setReference(coverage.getPayor().get(0).getReference()));
							}
							
						}
						patient.addIdentifier(identifier);
						*/
						response.addParameter().setName("MemberPatient").setResource(patientResource);
						response.addParameter().setName("NewCoverage").setResource(newCoverage);
						return response;
					}
				
				}
			}
			if(!found) {
				throw new CustomException(404,"Coverage with given identifier was not found");

			}
		}
		else {
			throw new CustomException(404,"Identifier is mandatory in OldCoverage");
		}
		
		
		
		
		return response;
	
	
	}
	
	
	

	
}