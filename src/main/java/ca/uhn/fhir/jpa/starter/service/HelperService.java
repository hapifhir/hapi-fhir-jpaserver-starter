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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.iprd.fhir.utils.FhirResourceTemplateHelper;
import com.iprd.fhir.utils.Validation;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
		    		.username ("manageuser")
		    		.password("12345")
		    		.resteasyClient(client)
		    		.build();
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
					String practitionerId = createResource(hcw);
					String practitionerRoleId = createResource(practitionerRole);
					String keycloakUserId = createUser(hcwData[0],hcwData[1],hcwData[2],hcwData[7], hcwData[8], hcwData[3], practitionerId, practitionerRoleId);
					updateResource(keycloakUserId, practitionerId, Practitioner.class);
					updateResource(keycloakUserId, practitionerRoleId, PractitionerRole.class);
				}
			}
			map.put("uploadCsv", "Successful");
			return new ResponseEntity(map,HttpStatus.OK);
		}
		
		public String createResource(Resource name) {
//			RealmResource realmResource = keycloak.realm("fhir-hapi");
			String idFromCreate = fhirClient.create().resource(name).execute().getId().getIdPart();
			return idFromCreate;
		}
		
		public <R extends IBaseResource> String updateResource(String keycloakId, String resourceId, Class<R> resourceClass) {
			 R resource = fhirClient.read().resource(resourceClass).withId(resourceId).execute();
			
			 if(resource.fhirType().equals(Practitioner.class.getSimpleName()))
				 ((Practitioner) resource).addIdentifier().setValue(keycloakId);
			 if(resource.fhirType().equals(PractitionerRole.class.getSimpleName()))
				 ((PractitionerRole) resource).addIdentifier().setValue(keycloakId);
			MethodOutcome outcome = fhirClient.update().resource(resource).execute();
			return outcome.getId().getIdPart();
			
		} 

}
