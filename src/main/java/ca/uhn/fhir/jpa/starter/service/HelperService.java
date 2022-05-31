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
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.iprd.fhir.utils.FhirResourceTemplateHelper;
import com.iprd.fhir.utils.KeycloakTemplateHelper;
import com.iprd.fhir.utils.Validation;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICreateWithQueryTyped;
import ca.uhn.fhir.rest.gclient.ICriterion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
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
		
		public ResponseEntity<LinkedHashMap<String, Object>> createGroups(MultipartFile file) throws IOException {
			
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
					String stateId = createResource(state, Location.NAME.matches().value(state.getName()));
					GroupRepresentation stateGroupRep = KeycloakTemplateHelper.stateGroup(state.getName(), stateId);
					String stateGroupId = createGroup(stateGroupRep);
					updateResource(stateGroupId, stateId, Location.class);
					
					Location lga = FhirResourceTemplateHelper.lga(csvData[1], csvData[0]);
					String lgaId = createResource(lga, Location.NAME.matches().value(lga.getName()));
					GroupRepresentation lgaGroupRep = KeycloakTemplateHelper.lgaGroup(lga.getName(), stateGroupId, lgaId);
					String lgaGroupId = createGroup(lgaGroupRep);
					updateResource(lgaGroupId, lgaId, Location.class);
					
					Location ward = FhirResourceTemplateHelper.ward(csvData[0], csvData[1], csvData[2]);
					String wardId = createResource(ward, Location.NAME.matches().value(ward.getName()));
					GroupRepresentation wardGroupRep = KeycloakTemplateHelper.lgaGroup(ward.getName(), lgaGroupId, wardId);
					String wardGroupId = createGroup(wardGroupRep);
					updateResource(wardGroupId, wardId, Location.class);
					
					Organization clinic = FhirResourceTemplateHelper.clinic(csvData[7],  csvData[3], csvData[4], csvData[0], csvData[1], csvData[2]);
					String facilityId = createResource(clinic, Organization.NAME.matches().value(clinic.getName()));
					GroupRepresentation facilityGroupRep = KeycloakTemplateHelper.facilityGroup(
								clinic.getName(),
								wardGroupId,
								facilityId,
								csvData[8],
								csvData[9],
								csvData[3],
								csvData[4]
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
		
		private String createResource(Resource resource ,ICriterion<?>...theCriterion) {
			/*
			 * ICreateQueryTYped q = fhirClient.create()
					   .resource(resource)
					   .conditional()
					   .where(firstCriterion)
					   
			 * For criterion in list:
			 * 	if not last criterion
			 * 		q = q.and(criterion)
			 * */
			 ICreateWithQueryTyped query = fhirClient.create()
					   .resource(resource)
					   .conditional()
					   .where(theCriterion[0]);
			 for(int i=1;i<theCriterion.length;i++) {
				 query = query.and(theCriterion[i]);
			 }
			 MethodOutcome outcome =  query.execute();
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
		
		private <R extends IBaseResource> void updateResource(String keycloakId, String resourceId, Class<R> resourceClass) {
			 R resource = fhirClient.read().resource(resourceClass).withId(resourceId).execute();
			 try {
				 Method addIdentifier = resource.getClass().getMethod("addIdentifier");
				 Identifier obj = (Identifier) addIdentifier.invoke(resource);
				 obj.setValue(keycloakId);
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
		
		private String createUser(UserRepresentation userRep) {
			RealmResource realmResource = keycloak.realm("fhir-hapi");
			for(UserRepresentation user: realmResource.users().list()) {
				if(user.getFirstName().equals(userRep.getFirstName()) && 
					user.getLastName().equals(userRep.getLastName()) && 
					user.getEmail().equals(userRep.getEmail())
				){
					return user.getId();
				}
			}
			Response response = realmResource.users().create(userRep);
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
					String practitionerId = createResource(hcw,
							Practitioner.GIVEN.matches().value(hcw.getName().get(0).getGivenAsSingleString()),
							Practitioner.FAMILY.matches().value(hcw.getName().get(0).getFamily()),
							Practitioner.TELECOM.exactly().systemAndValues(ContactPoint.ContactPointSystem.PHONE.toCode(),Arrays.asList(hcwData[4]+hcwData[3]))
						); // Catch index out of bound
					PractitionerRole practitionerRole = FhirResourceTemplateHelper.practitionerRole(hcwData[13],hcwData[14],practitionerId);
					String practitionerRoleId = createResource(practitionerRole, PractitionerRole.PRACTITIONER.hasId(practitionerId));
					UserRepresentation user = KeycloakTemplateHelper.user(hcwData[0],hcwData[1],hcwData[2],hcwData[7],hcwData[8],hcwData[3],practitionerId,practitionerRoleId,hcwData[9],hcwData[10],hcwData[11],hcwData[12]);
					String keycloakUserId = createUser(user);
					updateResource(keycloakUserId, practitionerId, Practitioner.class);
					updateResource(keycloakUserId, practitionerRoleId, PractitionerRole.class);
				}
			}
			map.put("uploadCsv", "Successful");
			return new ResponseEntity(map,HttpStatus.OK);
		}
		
		

}
