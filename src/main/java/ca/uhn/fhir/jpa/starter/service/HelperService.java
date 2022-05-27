package ca.uhn.fhir.jpa.starter.service;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.github.andrewoma.dexx.collection.Pair;
import com.iprd.fhir.utils.FhirResourceTemplateHelper;
import com.iprd.fhir.utils.KeycloakGroupTemplateHelper;
import com.iprd.fhir.utils.Validation;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.iprd.fhir.utils.FhirResourceTemplateHelper;

import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;


import java.util.ArrayList;
import java.util.Arrays;

import ca.uhn.fhir.rest.gclient.ICriterion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.keycloak.representations.idm.UserRepresentation;

@Service
public class HelperService {

		FhirContext ctx = FhirContext.forR4();
		String serverBase = "http://localhost:8080/fhir";
		IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);
		Keycloak keycloak; 
		
		public void initializeKeycloak() {
			ResteasyClient client = (ResteasyClient)ClientBuilder.newClient();
		    keycloak = KeycloakBuilder
		    		.builder()
		    		.serverUrl("http://localhost:8081/auth")
		    		.grantType(OAuth2Constants.PASSWORD)
		    		.realm("master")
		    		.clientId("fhir-hapi-realm")
		    		.username ("managegroup")
		    		.password("12345")
		    		.resteasyClient(client)
		    		.build();
		}
		
		public ResponseEntity<LinkedHashMap<String, Object>> create(MultipartFile file) throws IOException {
			
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
				if (Validation.validateClinicAndStateCsvLine(csvData)) {
					
					Location state = FhirResourceTemplateHelper.state(csvData[0]);
					String stateId = createResource(state, Location.NAME.matches().value(state.getName()) );
//					createResource(state,Location.class,Location.NAME.matches().value(state.getName()));
					GroupRepresentation stateGroupRep = KeycloakGroupTemplateHelper.stateGroup(state.getName(), stateId);
					String stateGroupId = createGroup(stateGroupRep);
					updateResource(stateGroupId, stateId, Location.class);
					
					Location lga = FhirResourceTemplateHelper.lga(csvData[1], csvData[0]);
					String lgaId = createResource(lga, Location.NAME.matches().value(lga.getName()));
					GroupRepresentation lgaGroupRep = KeycloakGroupTemplateHelper.lgaGroup(lga.getName(), stateGroupId, lgaId);
					String lgaGroupId = createGroup(lgaGroupRep);
					updateResource(lgaGroupId, lgaId, Location.class);
//					
					Location ward = FhirResourceTemplateHelper.ward(csvData[0], csvData[1], csvData[2]);
					String wardId = createResource(ward, Location.NAME.matches().value(ward.getName()));
					GroupRepresentation wardGroupRep = KeycloakGroupTemplateHelper.lgaGroup(ward.getName(), lgaGroupId, wardId);
					String wardGroupId = createGroup(wardGroupRep);
					updateResource(wardGroupId, wardId, Location.class);
					
					Organization clinic = FhirResourceTemplateHelper.clinic(csvData[5],  csvData[3], csvData[4], csvData[0], csvData[1], csvData[2]);
					String facilityId = createResource(clinic, Organization.NAME.matches().value(clinic.getName()));
					GroupRepresentation facilityGroupRep = KeycloakGroupTemplateHelper.facilityGroup(
								clinic.getName(),
								wardGroupId,
								facilityId,
								csvData[6],
								csvData[7],
								csvData[4],
								csvData[5]
							);
					String facilityGroupId = createGroup(facilityGroupRep);
					updateResource(facilityGroupId, facilityId, Organization.class);
				}
			}
			map.put("uploadCSV", "Successful");
			return new ResponseEntity(map,HttpStatus.OK);
		}
		
		private String createGroup(GroupRepresentation groupRep) {
			RealmResource realmResource = keycloak.realm("fhir-hapi");
			for(GroupRepresentation group: realmResource.groups().groups()) {
				if(group.getName().equals(groupRep.getName()))
					return group.getId();
			}
			Response response = realmResource.groups().add(groupRep);
			return CreatedResponseUtil.getCreatedId(response);
		}
		
		private String createResource(Resource resource ,ICriterion<?> theCriterion) {
			 MethodOutcome outcome = fhirClient.create()
					   .resource(resource)
					   .conditional()
					   .where(theCriterion)
					   .execute();
			 return outcome.getId().getIdPart();
		}
		
//		private void createResource(Resource resource, Class<? extends IBaseResource> theClass,ICriterion<?> theCriterion ) {
//			Bundle bundle = fhirClient.search().forResource(theClass).where(theCriterion).returnBundle(Bundle.class).execute();
//			bundle.getEntry()
//			if(bundle.hasEntry()) {
////				TODO: Return ID
//			}else {
////				TODO: Create and return
//			}
//		}
		
		private <R extends IBaseResource> void updateResource(String groupId, String resourceId, Class<R> resourceClass) {
			 R resource = fhirClient.read().resource(resourceClass).withId(resourceId).execute();
			 try {
				 Method addIdentifier = resource.getClass().getMethod("addIdentifier");
				 Identifier obj = (Identifier) addIdentifier.invoke(resource);
				 obj.setValue(groupId);
				 MethodOutcome outcome = fhirClient.update().resource(resource).execute();
			 }
			 catch (SecurityException e) {
				 e.printStackTrace();
			 }
			  catch (NoSuchMethodException e) { 
				  e.printStackTrace();
			  } catch (IllegalAccessException e) {
				  e.printStackTrace();
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		private String createUser(String firstName, String lastName, String email,String userName,String password,String phoneNumber,String practitionerId, String practitionerRoleId) {
			UserRepresentation user = new UserRepresentation();
			CredentialRepresentation credential = new CredentialRepresentation();
			credential.setType(CredentialRepresentation.PASSWORD);
			credential.setValue(password);
			user.setCredentials(Arrays.asList(credential));
			user.setUsername(userName);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmail(email);
			user.singleAttribute("phoneNumber", phoneNumber);
			user.singleAttribute("type","HCW");
			user.singleAttribute("fhirPractitionerLogicalId ", practitionerId);
			user.singleAttribute("fhirPractitionerRoleLogicalId ", practitionerRoleId);
			user.setEnabled(true);
			RealmResource realmResource = keycloak.realm("fhir-hapi");
			Response response = realmResource.users().create(user);
			return CreatedResponseUtil.getCreatedId(response);
		}
		
		public ResponseEntity<LinkedHashMap<String, Object>> createUsers(@RequestParam("file") MultipartFile file) throws Exception{
			LinkedHashMap<String, Object> map = new LinkedHashMap<>();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
			String singleLine;
			int iteration = 0;
			while((singleLine = bufferedReader.readLine()) != null) {
				if(iteration == 0 && singleLine.contains("firstName")) {
					iteration++;
					continue;
				}
				String hcwData[] = singleLine.split(","); //firstName,lastName,email,phoneNumber,countryCode,gender,birthDate,keycloakUserName,initialPassword,state,lga,ward,facilityUID,role,qualification,stateIdentifier
				if(Validation.validationHcwCsvLine(hcwData))
				{
					Practitioner hcw = FhirResourceTemplateHelper.hcw(hcwData[0],hcwData[1],hcwData[3],hcwData[4],hcwData[5],hcwData[6],hcwData[9],hcwData[10],hcwData[11],hcwData[12],hcwData[13],hcwData[14]);
					PractitionerRole practitionerRole = FhirResourceTemplateHelper.practitionerRole(hcwData[13],hcwData[14]);
					String practitionerId = createResource(hcw, Practitioner.GIVEN.matches().value(hcw.getName().get(0).getGivenAsSingleString())); // Catch index out of bound
					String practitionerRoleId = createResource(practitionerRole, PractitionerRole.PRACTITIONER.hasId(hcw.getId()));
					String keycloakUserId = createUser(hcwData[0],hcwData[1],hcwData[2],hcwData[7], hcwData[8], hcwData[3], practitionerId, practitionerRoleId);
					updateResource(keycloakUserId, practitionerId, Practitioner.class);
					updateResource(keycloakUserId, practitionerRoleId, PractitionerRole.class);
				}
			}
			map.put("uploadCsv", "Successful");
			return new ResponseEntity(map,HttpStatus.OK);
		}
		
		

}
