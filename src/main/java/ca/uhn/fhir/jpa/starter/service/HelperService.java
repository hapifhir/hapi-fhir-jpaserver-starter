package ca.uhn.fhir.jpa.starter.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.jena.ext.xerces.util.URI.MalformedURIException;
import org.codehaus.jettison.json.JSONObject;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.iprd.fhir.utils.FhirResourceTemplateHelper;
import com.iprd.fhir.utils.KeycloakTemplateHelper;
import com.iprd.fhir.utils.Validation;
import com.iprd.report.FhirClientProvider;
import com.iprd.report.ReportGeneratorFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.dao.BaseHapiFhirDao;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
//import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


@Import(AppProperties.class)
@Service
public class HelperService {
	
		@Autowired
		AppProperties appProperties;
		@Autowired
		HttpServletRequest request;
		
		Keycloak keycloak;

		FhirContext ctx;
		String serverBase;
		IGenericClient fhirClient;
		Keycloak instance;
		TokenManager tokenManager;
		BearerTokenAuthInterceptor authInterceptor;
		
		private static final Logger logger = LoggerFactory.getLogger(HelperService.class);
		private static String IDENTIFIER_SYSTEM = "http://www.iprdgroup.com/Identifier/System";
		private static String SMS_EXTENTION_URL = "http://iprdgroup.com/Extentions/sms-sent";
		private static final long INITIAL_DELAY = 5*30000L;
		private static final long FIXED_DELAY = 5*60000L;

		private static final long AUTH_INITIAL_DELAY = 25 * 60000L;
		private static final long AUTH_FIXED_DELAY = 50 * 60000L;
		private static final long DELAY = 2 * 60000;

		public void initializeKeycloak() {
			ctx = FhirContext.forR4();
			serverBase = appProperties.getHapi_Server_address();
			fhirClient = ctx.newRestfulGenericClient(serverBase);		
			ResteasyClient client = (ResteasyClient)ClientBuilder.newClient();
		    keycloak = KeycloakBuilder
		    		.builder()
		    		.serverUrl(appProperties.getKeycloak_Server_address())
		    		.grantType(OAuth2Constants.PASSWORD)
		    		.realm(appProperties.getKeycloak_Realm())
		    		.clientId(appProperties.getKeycloak_Client_Id())
		    		.username (appProperties.getKeycloak_Username())
		    		.password(appProperties.getKeycloak_Password())
		    		.resteasyClient(client)
		    		.build();
			instance = Keycloak.
					getInstance(
						appProperties.getKeycloak_Server_address(),
						appProperties.getKeycloak_Client_Realm(),
						appProperties.getFhir_user(),
						appProperties.getFhir_password(),
						appProperties.getFhir_hapi_client_id(),
						appProperties.getFhir_hapi_client_secret()
					);
			tokenManager = instance.tokenManager();
			registerClientAuthInterceptor();
		}

		@Scheduled(fixedDelay = AUTH_FIXED_DELAY, initialDelay = AUTH_INITIAL_DELAY)
		private void registerClientAuthInterceptor() {
			String accessToken = tokenManager.getAccessTokenString();
			try {
				fhirClient.unregisterInterceptor(authInterceptor);
			}catch(Exception e) {
				e.printStackTrace();
			}
			authInterceptor = new BearerTokenAuthInterceptor(accessToken); // the reason this is below is to unregister interceptors to avoid memory leak. Null pointer is caught in try catch.
			fhirClient = ctx.newRestfulGenericClient(serverBase);
			fhirClient.registerInterceptor(authInterceptor);
		}

		@Scheduled(fixedDelay = DELAY, initialDelay = DELAY)
		private void searchPatients() throws IOException {
			String dateTime = DateTimeFormatter.ISO_INSTANT
					.withZone(ZoneId.of("UTC"))
					.format(Instant.now());
			String date= DateTimeFormatter.ISO_INSTANT.format(Instant.now()).split("T")[0];
			Extension smsSent = new Extension();
			 smsSent.addExtension(
					 SMS_EXTENTION_URL, 
					 new DateTimeType(dateTime));
			 String encounterClass = "IMP";
			 String queryPath = "Patient?";
			 queryPath+="_has:Encounter:patient:class="+encounterClass+"&";
			 queryPath+="_has:Encounter:patient:date=eq"+date+"";
			 Bundle patientBundle = new Bundle();
			 getBundleBySearchUrl(patientBundle, queryPath);
			 
			 String encounterQueryPath = "Encounter?class="+encounterClass+"&";
			 encounterQueryPath += "_date=eq"+date+"&subject=Patient/";
			 
			 for(BundleEntryComponent entry: patientBundle.getEntry()) {
				 Patient patient = (Patient)entry.getResource();
				 String patientId = patient.getIdElement().getIdPart();
				 Bundle encBundle = getCheckInEncounter(encounterQueryPath + patientId);
				 if(encBundle.getEntry().isEmpty()) {
					 continue;
				 }	
				 Encounter encounter = (Encounter) encBundle.getEntry().get(0).getResource();
				 String patientOclId = getOclIdentifier(patient.getIdentifier()).replaceAll(".(?!$)", "$0 ").replaceAll(".{8}", "$0\n");
				 String oclLink = getOclLink(patient.getIdentifier());
				 String patientName = patient.getName().get(0).getNameAsSingleString();
				 String mobile = patient.getTelecom().get(0).getValue();
				 Extension encounterExtension = encounter.getExtensionByUrl(SMS_EXTENTION_URL);
				 if(encounterExtension == null)
				 {
					 if(mobile.startsWith("+234-")&&mobile.length()>6){				 
						 mobile = mobile.substring(5);
						 if(mobile.startsWith("0"))
						 {
							mobile = mobile.substring(1); 
						 }	
						 OkHttpClient client = new OkHttpClient().newBuilder().build();
						 okhttp3.MediaType mediaType = okhttp3.MediaType.parse("text/plain");
						 String patientDetailsMessage = "Thanks for visiting!\nHere are the details of your visit: \nName: "+patientName+" \nDate: "+date+" \nYour OCL Id is:\n"+patientOclId+"";
						 String oclLinkMessage = "The QR image for OCL code:\n"+patientOclId+"\nis here:\n"+oclLink+"";
						 String messageVisitDetails ="https://portal.nigeriabulksms.com/api/?username=impacthealth@hacey.org&password=IPRDHACEY123&message="+patientDetailsMessage+"&sender=HACEY-IPRD&mobiles="+mobile;
						 String messageQrImage = "https://portal.nigeriabulksms.com/api/?username=impacthealth@hacey.org&password=IPRDHACEY123&message="+oclLinkMessage+"&sender=HACEY-IPRD&mobiles="+mobile;	 
						 Request requestVisitDetails = new Request.Builder().url(messageVisitDetails).build();
						 Request requestQrImage = new Request.Builder().url(messageQrImage).build();
						 okhttp3.Response responseVisitDetails = client.newCall(requestVisitDetails).execute();
						 okhttp3.Response responseQrImage = client.newCall(requestQrImage).execute();	
						 try {
							 if(responseVisitDetails.isSuccessful() && responseQrImage.isSuccessful())
							 {
								 encounter.addExtension(smsSent.getExtensionByUrl(SMS_EXTENTION_URL));
								 fhirClient.update()
								   .resource(encounter)
								   .execute();
							 } 
						 }catch(Exception e) {
							 e.printStackTrace();
						 }finally {
							 responseVisitDetails.body().close();
							 responseQrImage.body().close();
						 }
					 }
				 }
			}		 
		}
		
		private String getOclIdentifier(List<Identifier> identifiers) {
			String oclId = null;
			for(Identifier identifier : identifiers ) {
				if(identifier.hasSystem() && identifier.getSystem().equals("http://iprdgroup.com/identifiers/ocl")) {
					oclId = identifier.getValue();
					break;
				}
			}
			try {
				oclId = getOclIdFromString(oclId);
			} catch (MalformedURIException e) {
				logger.debug(e.getMessage());
			}
			return oclId;
		}
		
		private String getOclLink(List<Identifier> identifiers)	{
			String oclId = null;
			for(Identifier identifier: identifiers) {
				if(identifier.hasSystem() && identifier.getSystem().equals("http://iprdgroup.com/identifiers/ocl")) {
					oclId = identifier.getValue();
				}
			}
			return oclId;
		}
		private Bundle getCheckInEncounter(String url) {
			Bundle encounter = fhirClient.search()
					   .byUrl(url)
					   .returnBundle(Bundle.class)
					   .execute();
			return encounter;
		}

		private ReferenceClientParam Reference(String string) {
			// TODO Auto-generated method stub
			return null;
		}

		private Map<String, String> getQueryMap(String query){
			String[] params = query.split("&");
	        Map<String, String> map = new HashMap<String, String>();

	        for (String param : params) {
	            String name = param.split("=")[0];
	            String value = param.split("=")[1];
	            map.put(name, value);
	        }
	        return map;
		}

		private String getOclIdFromString(String query) throws MalformedURIException {
			try {
				URL url=new URL(query);
				String queryUrl = url.getQuery();
				if(queryUrl == null || queryUrl.isEmpty())
					return null;
				Map<String, String> queryMap = getQueryMap(queryUrl);
				if(queryMap.isEmpty() || !queryMap.containsKey("s")) return null;
				return queryMap.get("s");

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}
		
		public ResponseEntity<LinkedHashMap<String, Object>> createGroups(MultipartFile file) throws IOException {

			LinkedHashMap<String, Object> map = new LinkedHashMap<>();
			List<String> states = new ArrayList<>();
			List<String> lgas = new ArrayList<>();
			List<String> wards = new ArrayList<>();
			List<String> clinics  = new ArrayList<>();
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
			String singleLine;
			int iteration = 0;
			String stateId="",lgaId="",wardId="",facilityId="";
			String stateGroupId="", lgaGroupId="", wardGroupId="", facilityGroupId="";
			
			while((singleLine = bufferedReader.readLine())!=null){
				if(iteration == 0) { //skip header of CSV file
					iteration++;
					continue;
				}
				String[] csvData = singleLine.split(",");
				//State, LGA, Ward, FacilityUID, FacilityCode, CountryCode, PhoneNumber, FacilityName, FacilityLevel, Ownership
				if (Validation.validateClinicAndStateCsvLine(csvData)) {
					if(!states.contains(csvData[0])) {
						Location state = FhirResourceTemplateHelper.state(csvData[0]);
						stateId = createResource(state,Location.class,Location.NAME.matches().value(state.getName()));
						states.add(state.getName());
						GroupRepresentation stateGroupRep = KeycloakTemplateHelper.stateGroup(state.getName(), stateId);
						stateGroupId = createGroup(stateGroupRep);
						updateResource(stateGroupId, stateId, Location.class);
					}

					if(!lgas.contains(csvData[1])) {
						Location lga = FhirResourceTemplateHelper.lga(csvData[1], csvData[0]);
						lgaId = createResource(lga, Location.class, Location.NAME.matches().value(lga.getName()));
						lgas.add(lga.getName());
						GroupRepresentation lgaGroupRep = KeycloakTemplateHelper.lgaGroup(lga.getName(), stateGroupId, lgaId);
						lgaGroupId = createGroup(lgaGroupRep);
						updateResource(lgaGroupId, lgaId, Location.class);
					}

					if(!wards.contains(csvData[2])) {
						Location ward = FhirResourceTemplateHelper.ward(csvData[0], csvData[1], csvData[2]);
						wardId = createResource(ward, Location.class, Location.NAME.matches().value(ward.getName()));
						wards.add(ward.getName());
						GroupRepresentation wardGroupRep = KeycloakTemplateHelper.wardGroup(ward.getName(), lgaGroupId, wardId);
						wardGroupId = createGroup(wardGroupRep);
						updateResource(wardGroupId, wardId, Location.class);
					}

					if(!clinics.contains(csvData[7])) {
						Organization clinic = FhirResourceTemplateHelper.clinic(csvData[7],  csvData[3], csvData[4], csvData[5], csvData[6], csvData[0], csvData[1], csvData[2]);
						facilityId = createResource(clinic, Organization.class, Organization.NAME.matches().value(clinic.getName()));
						clinics.add(clinic.getName());
						GroupRepresentation facilityGroupRep = KeycloakTemplateHelper.facilityGroup(
									clinic.getName(),
									wardGroupId,
									facilityId,
									csvData[8],
									csvData[9],
									csvData[3],
									csvData[4]
								);
						facilityGroupId = createGroup(facilityGroupRep);
						updateResource(facilityGroupId, facilityId, Organization.class);
					}
				}
			}
			map.put("uploadCSV", "Successful");
			return new ResponseEntity<LinkedHashMap<String, Object>>(map,HttpStatus.OK);
		}

		public ResponseEntity<LinkedHashMap<String, Object>> createUsers(@RequestParam("file") MultipartFile file) throws Exception{
			LinkedHashMap<String, Object> map = new LinkedHashMap<>();
			List<String> practitioners = new ArrayList<>();

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
			String singleLine;
			int iteration = 0;
			String practitionerRoleId = "";
			String practitionerId = "";

			while((singleLine = bufferedReader.readLine()) != null) {
				if(iteration == 0) { //Skip header of CSV
					iteration++;
					continue;
				}
				String hcwData[] = singleLine.split(",");
				//firstName,lastName,email,countryCode,phoneNumber,gender,birthDate,keycloakUserName,initialPassword,state,lga,ward,facilityUID,role,qualification,stateIdentifier
				if(Validation.validationHcwCsvLine(hcwData))
				{
					if(!(practitioners.contains(hcwData[0]) && practitioners.contains(hcwData[1]) && practitioners.contains(hcwData[4]+hcwData[3]))) {
						Practitioner hcw = FhirResourceTemplateHelper.hcw(hcwData[0],hcwData[1],hcwData[4],hcwData[3],hcwData[5],hcwData[6],hcwData[9],hcwData[10],hcwData[11],hcwData[12],hcwData[13],hcwData[14],hcwData[15]);
						practitionerId = createResource(hcw,
								Practitioner.class,
								Practitioner.GIVEN.matches().value(hcw.getName().get(0).getGivenAsSingleString()),
								Practitioner.FAMILY.matches().value(hcw.getName().get(0).getFamily()),
								Practitioner.TELECOM.exactly().systemAndValues(ContactPoint.ContactPointSystem.PHONE.toCode(),Arrays.asList(hcwData[4]+hcwData[3]))
							); // Catch index out of bound
						practitioners.add(hcw.getName().get(0).getFamily());
						practitioners.add(hcw.getName().get(0).getGivenAsSingleString());
						practitioners.add(hcw.getTelecom().get(0).getValue());
						PractitionerRole practitionerRole = FhirResourceTemplateHelper.practitionerRole(hcwData[13],hcwData[14],practitionerId);
						practitionerRoleId = createResource(practitionerRole, PractitionerRole.class, PractitionerRole.PRACTITIONER.hasId(practitionerId));
						UserRepresentation user = KeycloakTemplateHelper.user(hcwData[0],hcwData[1],hcwData[2],hcwData[7],hcwData[8],hcwData[4],hcwData[3],practitionerId,practitionerRoleId,hcwData[13],hcwData[9],hcwData[10],hcwData[11],hcwData[12]);
						String keycloakUserId = createUser(user);
						if(keycloakUserId != null) {
							updateResource(keycloakUserId, practitionerId, Practitioner.class);
							updateResource(keycloakUserId, practitionerRoleId, PractitionerRole.class);
						}
					}
				}
			}
			map.put("uploadCsv", "Successful");
			return new ResponseEntity<LinkedHashMap<String, Object>>(map,HttpStatus.OK);
		}

		public List<GroupRepresentation> getGroupsByUser(String userId) {
			RealmResource realmResource = keycloak.realm("fhir-hapi");
			List<GroupRepresentation> groups =  realmResource.users().get(userId).groups(0,appProperties.getKeycloak_max_group_count(),false);
			return groups;
		}

		public ResponseEntity<InputStreamResource> generateDailyReport(String date, String organizationId, List<List<String>> fhirExpressions) {
			FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClient);
			byte[] pdfContent = ReportGeneratorFactory.INSTANCE.reportGenerator().generateDailyReport(fhirClientProvider, date, organizationId, fhirExpressions);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pdfContent);
			HttpHeaders headers=  new HttpHeaders();
			headers.add("Content-Disposition", "inline; filename=daily_report.pdf");
			return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(byteArrayInputStream));
		}

		private String createGroup(GroupRepresentation groupRep) {
			RealmResource realmResource = keycloak.realm("fhir-hapi");
			List<GroupRepresentation> groups = realmResource.groups().groups(groupRep.getName(), 0, Integer.MAX_VALUE);
			if(!groups.isEmpty()) {
				return groups.get(0).getId();
			}
			Response response = realmResource.groups().add(groupRep);
			return CreatedResponseUtil.getCreatedId(response);
		}

		private String createResource(Resource resource, Class<? extends IBaseResource> theClass,ICriterion<?>...theCriterion ) {
			IQuery<IBaseBundle> query = fhirClient.search().forResource(theClass).where(theCriterion[0]);
			for(int i=1;i<theCriterion.length;i++)
				query = query.and(theCriterion[i]);
			Bundle bundle = query.returnBundle(Bundle.class).execute();
			if(!bundle.hasEntry()) {
				MethodOutcome outcome = fhirClient.update().resource(resource).execute();
				 return outcome.getId().getIdPart();
			}
			return bundle.getEntry().get(0).getFullUrl().split("/")[5];
		}

		private <R extends IBaseResource> void updateResource(String keycloakId, String resourceId, Class<R> resourceClass) {
			 R resource = fhirClient.read().resource(resourceClass).withId(resourceId).execute();
			 try {
				 Method getIdentifier = resource.getClass().getMethod("getIdentifier");
				 List<Identifier> identifierList = (List<Identifier>) getIdentifier.invoke(resource);
				 for(Identifier identifier: identifierList) {
					 if(identifier.getSystem().equals(IDENTIFIER_SYSTEM+"/KeycloakId")) {return;}
				 }
				 Method addIdentifier = resource.getClass().getMethod("addIdentifier");
				 Identifier obj = (Identifier) addIdentifier.invoke(resource);
				 obj.setSystem(IDENTIFIER_SYSTEM+"/KeycloakId");
				 obj.setValue(keycloakId);
				 MethodOutcome outcome = fhirClient.update().resource(resource).execute();
			 }
			 catch (SecurityException | NoSuchMethodException | InvocationTargetException e) {
				 e.printStackTrace();
			 } catch (IllegalAccessException e) {
				  e.printStackTrace();
				e.printStackTrace();
			}
		}

		private String createUser(UserRepresentation userRep) {
			RealmResource realmResource = keycloak.realm(appProperties.getKeycloak_Client_Realm());
			List<UserRepresentation> users = realmResource.users().search(userRep.getUsername(), 0, Integer.MAX_VALUE);
			//if not empty, return id
			if(!users.isEmpty())
				users.get(0).getId();
			try {
				Response response = realmResource.users().create(userRep);
				return CreatedResponseUtil.getCreatedId(response);
			}catch(WebApplicationException e) {
				logger.error("Cannot create user "+userRep.getUsername()+" with groups "+userRep.getGroups()+"\n"+e.getStackTrace().toString());
				return null;
			}
		}

		@Scheduled(fixedDelay = FIXED_DELAY, initialDelay = INITIAL_DELAY )
		public void mapResourcesToPatient() {
			//Searching for patient created with OCL-ID
			Bundle tempPatientBundle = new Bundle();
			getBundleBySearchUrl(tempPatientBundle, serverBase+"/Patient?identifier=patient_with_ocl");

			for(BundleEntryComponent entry: tempPatientBundle.getEntry()) {
				Patient tempPatient = (Patient)entry.getResource();
				String tempPatientId = tempPatient.getIdElement().getIdPart();
				String oclId = tempPatient.getIdentifier().get(0).getValue();

				//Searching for actual patient with OCL-ID
				String actualPatientId = getActualPatientId(oclId);
				if(actualPatientId == null){
					continue;
				}
				Bundle resourceBundle = new Bundle();
				getBundleBySearchUrl(resourceBundle, serverBase+"/Patient/"+tempPatientId+"/$everything");

				for(BundleEntryComponent resourceEntry: resourceBundle.getEntry()) {
					Resource resource = resourceEntry.getResource();
					if(resource.fhirType().equals("Patient")) {
						continue;
					}
					else if(resource.fhirType().equals("Immunization")) {
						Immunization immunization = (Immunization) resource;
						immunization.getPatient().setReference("Patient/"+actualPatientId);
						fhirClient.update()
						   .resource(immunization)
						   .execute();
						continue;
					}
					try {
						Method getSubject = resource.getClass().getMethod("getSubject");
						Reference subject = (Reference)getSubject.invoke(resource);
						subject.setReference("Patient/"+actualPatientId);
						fhirClient.update()
								   .resource(resource)
								   .execute();
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						logger.error(e.getMessage());
					}
				}
			}
		}

		private String getActualPatientId(String oclId) {
			Bundle patientBundle = new Bundle();
			getBundleBySearchUrl(patientBundle, serverBase+"/Patient?identifierPartial:contains="+oclId);
			for(BundleEntryComponent entry: patientBundle.getEntry()) {
				Patient patient = (Patient)entry.getResource();
				if(isActualPatient(patient, oclId))
					return patient.getIdElement().getIdPart();
			}
			return null;
		}

		private boolean isActualPatient(Patient patient, String oclId) {
			for(Identifier identifier:patient.getIdentifier()) {
				if (identifier.getValue().equals("patient_with_ocl")) {
					return false;
				}
			}
			return true;
		}

		private void getBundleBySearchUrl(Bundle bundle, String url) {
			Bundle searchBundle = fhirClient.search()
					   .byUrl(url)
					   .returnBundle(Bundle.class)
					   .execute();
			bundle.getEntry().addAll(searchBundle.getEntry());
			if(searchBundle.hasLink() && bundleContainsNext(searchBundle)) {
				getBundleBySearchUrl(bundle, getNextUrl(searchBundle.getLink()));
			}
		}

		private boolean bundleContainsNext(Bundle bundle) {
			for(BundleLinkComponent link: bundle.getLink()) {
				if(link.getRelation().equals("next"))
					return true;
			}
			return false;
		}
		private String getNextUrl(List<BundleLinkComponent> bundleLinks) {
			for(BundleLinkComponent link: bundleLinks) {
				if(link.getRelation().equals("next")) {
					return link.getUrl();
				}
			}
			return null;
		}
}
