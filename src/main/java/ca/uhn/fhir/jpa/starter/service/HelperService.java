package ca.uhn.fhir.jpa.starter.service;

import org.hl7.fhir.r4.model.Resource;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.hl7.fhir.instance.model.api.IIdType;

@Service
public class HelperService {

		FhirContext ctx = FhirContext.forR4();
		String serverBase = "http://localhost:8080/fhir";
		IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);
		Keycloak keycloak;  
		
    public void initializeKeycloak() {
    	
//    	ResteasyProviderFactory resteasyProviderFactory = ResteasyProviderFactoryImpl.getInstance();
//    	HttpServletResponse contextData = resteasyProviderFactory.getContextData(HttpServletResponse.class);
    	ResteasyClient client = (ResteasyClient)ClientBuilder.newClient();
                
    	 keycloak = KeycloakBuilder.builder()
 	    		.serverUrl("https://malaria1.opencampaignlink.org:8443/auth")
 	    		.grantType(OAuth2Constants.PASSWORD)
 	      		.realm("fhir-hapi")
 	    		.clientId("fhir-hapi-server")
 	    		.username("admin")
 	    		.password("p33wd!@")
 	    		.resteasyClient(client)
 	    		.build();
    	 
 	    RealmResource realmResource = keycloak.realm("fhir-hapi");
 	    System.out.println(realmResource);
    }

    public String createGroup(String groupName) {
   	 
    RealmResource realmResource = keycloak.realm("fhir-hapi");
    GroupRepresentation groupRep = new GroupRepresentation();
    groupRep.setName(groupName);
    Response response = realmResource.groups().add(groupRep);
    return CreatedResponseUtil.getCreatedId(response);
    }
    
	public void create(Resource name) {
		RealmResource realmResource = keycloak.realm("fhir-hapi");
		IIdType idFromCreate = fhirClient.create().resource(name).execute().getId();
	}

}
