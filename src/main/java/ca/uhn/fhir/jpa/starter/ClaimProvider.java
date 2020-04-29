package ca.uhn.fhir.jpa.starter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.ClaimResponse.Use;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.jpa.dao.DaoMethodOutcome;
import ca.uhn.fhir.jpa.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.rp.r4.ClaimResourceProvider;
import ca.uhn.fhir.jpa.rp.r4.PatientResourceProvider;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.springframework.context.ApplicationContext;


import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClaimProvider extends ClaimResourceProvider{
	
	IFhirResourceDao<Bundle> bundleDao;
	IFhirResourceDao<ClaimResponse> ClaimResponseDao;
	ClaimProvider(ApplicationContext appCtx){
		
		this.bundleDao = (IFhirResourceDao<Bundle>) appCtx.getBean("myBundleDaoR4", IFhirResourceDao.class);
		this.ClaimResponseDao = (IFhirResourceDao<ClaimResponse>) appCtx.getBean("myClaimResponseDaoR4", IFhirResourceDao.class);
		System.out.println("----\n---");
		System.out.println(this.ClaimResponseDao);
		System.out.println(this.bundleDao);
	}
	
	@Override
	public Class<Claim> getResourceType() {
	      return Claim.class;
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
	
	private String submitX12(String x12_generated,ClaimResponse claimResponse,String bundleJson) {
		String str_result = "";
		try {
			 
            // POST call for token
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("https://api-gateway.linkhealth.com/oauth/token");

            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            params.add(new BasicNameValuePair("client_id", "mettles"));
            params.add(new BasicNameValuePair("client_secret", "35a17248-bf55-44c4-8e51-057fd4a10501"));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            // Execute and get the response.
            HttpResponse http_response = httpclient.execute(httppost);
            HttpEntity entity = http_response.getEntity();
            
            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
					String jsonString =  EntityUtils.toString(entity);
					JSONParser parser = new JSONParser();
					org.json.simple.JSONObject tokenResponse = ( org.json.simple.JSONObject) parser.parse(jsonString);
					//tokenResponse = new JsonParser().parse(jsonString).getAsJsonObject();
					
                    String soap_xml = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"> <soap:Header> </soap:Header> <soap:Body> <ns2:COREEnvelopeRealTimeRequest xmlns:ns2=\"http://www.caqh.org/SOAP/WSDL/CORERule2.2.0.xsd\"> <PayloadType>X12_278_Request_005010X217E1_2</PayloadType> <ProcessingMode>RealTime</ProcessingMode> <PayloadID>"
                            // + claimResponse.getId()
                            + "42527" + "</PayloadID> <TimeStamp>2019-09-20T13:05:45.429</TimeStamp> <SenderID>"
                            + "1932102951" + "</SenderID> <ReceiverID>" + "06111" + "</ReceiverID><Bundle>"
                            + bundleJson + " <CORERuleVersion>v2.2.0</CORERuleVersion> <Payload>" + x12_generated
                            + "</Payload> </ns2:COREEnvelopeRealTimeRequest> </soap:Body> </soap:Envelope>\n";
                    URL soap_url = new URL("https://api-gateway.linkhealth.com/davinci/x12/priorauth");

                    byte[] soap_postDataBytes = soap_xml.getBytes("UTF-8");
                    HttpURLConnection soap_conn = (HttpURLConnection) soap_url.openConnection();
                    soap_conn.setRequestMethod("POST");
                    soap_conn.setRequestProperty("Content-Type", "application/xml");
                    soap_conn.setRequestProperty("Accept", "application/xml");
                    soap_conn.setRequestProperty("Authorization", "Bearer " + tokenResponse.get("access_token"));
                    soap_conn.setDoOutput(true);
                    soap_conn.getOutputStream().write(soap_postDataBytes);
                    BufferedReader soap_in = new BufferedReader(
                            new InputStreamReader(soap_conn.getInputStream(), "UTF-8"));
                    String str = null;
                    StringBuilder sb1 = new StringBuilder();
                    while ((str = soap_in.readLine()) != null) {
                        sb1.append(str);
                    }
                    str_result = sb1.toString();
//                    System.out.println("\n  >>> Str Res:"+str_result);
                    if (str_result.contains("HCR*A1")) {
                    	claimResponse.setStatus(ClaimResponse.ClaimResponseStatus.ACTIVE);
                    	claimResponse.setOutcome(ClaimResponse.RemittanceOutcome.COMPLETE);
               
                    } else if (str_result.contains("HCR*A2")) {
                    	claimResponse.setStatus(ClaimResponse.ClaimResponseStatus.ACTIVE);
                    	claimResponse.setOutcome(ClaimResponse.RemittanceOutcome.PARTIAL);
           
                    } else if (str_result.contains("HCR*A3")) {
                    	claimResponse.setStatus(ClaimResponse.ClaimResponseStatus.ENTEREDINERROR);
                    	claimResponse.setOutcome(ClaimResponse.RemittanceOutcome.ERROR);
              
                    } else if (str_result.contains("HCR*A4")) {
                    	claimResponse.setStatus(ClaimResponse.ClaimResponseStatus.ACTIVE);
                    	claimResponse.setOutcome(ClaimResponse.RemittanceOutcome.QUEUED);
              
                    } else if (str_result.contains("HCR*C")) {
                    	claimResponse.setStatus(ClaimResponse.ClaimResponseStatus.CANCELLED);
                    	claimResponse.setOutcome(ClaimResponse.RemittanceOutcome.COMPLETE);
                
                    }
                }
            }


            
		}
		catch(Exception ex) {
			
		}
		return  str_result;
	}
	
	private String generateX12(RequestDetails details,Bundle bundle, ClaimResponse claimResponse) {
		String x12_generated = "";
		try {

	            IParser jsonParser = details.getFhirContext().newJsonParser();
	            String jsonStr = jsonParser.encodeResourceToString(bundle);
//	            System.out.println("JSON:\n" + jsonStr);
	            StringBuilder sb = new StringBuilder();
	            String str_result = "";
	            URL url = new URL("http://cdex.mettles.com/x12/xmlx12");
	            byte[] postDataBytes = jsonStr.getBytes("UTF-8");
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("POST");
	            conn.setRequestProperty("Content-Type", "application/json");
	            conn.setRequestProperty("Accept", "application/json");
	            conn.setDoOutput(true);
	            conn.getOutputStream().write(postDataBytes);
	            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	            String line = null;
	            while ((line = in.readLine()) != null) {
	                sb.append(line);
	            }
	            String result = sb.toString();

	            JSONObject response = new JSONObject(result);
//	            System.out.print("JSON:" + response.toString());
	            if (response.has("x12_response")) {
	                x12_generated = response.getString("x12_response").replace("~", "~\n");
	                //submitX12( x12_generated, claimResponse, jsonStr);
	            }
	        }
		    catch (Exception ex) {
		        ex.printStackTrace();
		    }
		 return x12_generated;
	}
	
	private ClaimResponse  generateClaimResponse(Patient patient , Claim claim ) {
		ClaimResponse retVal = new ClaimResponse();
		try {
			if(patient.getIdentifier().size()>0) {
				String patientIdentifier = patient.getIdentifier().get(0).getValue();
				Reference patientRef = new Reference();
			     if (!patientIdentifier.isEmpty()) {
	                Identifier patientIdentifierObj = new Identifier();
	                patientIdentifierObj.setValue(patientIdentifier);
	                patientRef.setIdentifier(patientIdentifierObj);
	                retVal.setPatient(patientRef);
	             }
			}
			
			if(claim.getIdentifier().size() > 0) {
				 String claimIdentifier = claim.getIdentifier().get(0).getValue();
				 Reference reqRef = new Reference();
		         if (!claimIdentifier.isEmpty()) {
		             Identifier claimIdentifierObj = new Identifier();
		             claimIdentifierObj.setValue(claimIdentifier);
		             reqRef.setIdentifier(claimIdentifierObj);
		         }
		         retVal.setRequest(reqRef);
			 }
		     retVal.setStatus(ClaimResponse.ClaimResponseStatus.ACTIVE);
		     retVal.setOutcome(ClaimResponse.RemittanceOutcome.QUEUED);
		     /*
		     if(claim.getInsurer() != null) {
		    	 if(claim.getInsurer().getIdentifier() !=null) {
		    		 retVal.setInsurer(new Reference().setIdentifier(claim.getInsurer().getIdentifier()));
		    	 }
//		    	 retVal.setInsurer(claim.getInsurer());
		     }
		     if(claim.getProvider()!= null) {
		    	 retVal.setRequestor(new Reference().setIdentifier(claim.getProvider().getIdentifier()));
//		    	 retVal.setRequestor(claim.getProvider());
		     }
		     
		     */
		     
		     retVal.setUse(ClaimResponse.Use.PREAUTHORIZATION );
//		     retVal.setPatient(new Reference(patient.getId()));
		     retVal.setType(claim.getType());
		     if(claim.getSubType() != null) {
		    	 retVal.setSubType(claim.getSubType());
		     }
	         retVal.setCreated(new Date());
	         retVal.setUse(ClaimResponse.Use.PREAUTHORIZATION);

	         
	         retVal.setPreAuthRef(getSaltString());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
        return retVal;
	} 

	
	  // @Create
    @Operation(name = "$submit", idempotent = true)
    public Bundle claimSubmit(RequestDetails details,
            @OperationParam(name = "claim", min = 1, max = 1, type = Bundle.class) Bundle bundle)
            throws RuntimeException {
  
        Bundle collectionBundle = new Bundle().setType(Bundle.BundleType.COLLECTION);
        Bundle responseBundle = new Bundle();
        Bundle createdBundle = new Bundle();
        String claimURL = "";
        String patientId = "";
        String claim_response_status = "";
        String claim_response_outcome = "";
        String patientIdentifier = "";
        String claimIdentifier = "";
        Claim claim = new Claim();
        Patient patient = new Patient();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            collectionBundle.addEntry(entry);
//            System.out.println("ResType : " + entry.getResource().getResourceType());
            if (entry.getResource().getResourceType().toString().equals("Claim")) {
                try {
                    claim = (Claim) entry.getResource();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (entry.getResource().getResourceType().toString().equals("Patient")) {
                try {
                    patient = (Patient) entry.getResource();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {

	    	ClaimResponse retVal = generateClaimResponse(patient, claim);
	    	String x12_generated =  generateX12( details, bundle,retVal);
            System.out.println("----------X12 Generated--------- \n");
            System.out.println(x12_generated);
            System.out.println("\n------------------- \n");
            DaoMethodOutcome claimResponseOutcome = ClaimResponseDao.create(retVal);
            ClaimResponse claimResponse = (ClaimResponse) claimResponseOutcome.getResource();
            Bundle.BundleEntryComponent transactionEntry = new Bundle.BundleEntryComponent().setResource(claimResponse);
            responseBundle.addEntry(transactionEntry);
            DaoMethodOutcome bundleOutcome = this.bundleDao.create(collectionBundle);
            createdBundle = (Bundle) bundleOutcome.getResource();
            for (Bundle.BundleEntryComponent entry : createdBundle.getEntry()) {
                responseBundle.addEntry(entry);
            }
            responseBundle.setId(createdBundle.getId());
            responseBundle.setType(Bundle.BundleType.COLLECTION);
            return responseBundle;


        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getLocalizedMessage());
        }
    }
	
	

	@Operation(name = "$build", idempotent = true)
	public  Bundle build(RequestDetails theRequestDetails,
			@OperationParam(name = "bundle", min = 1, max = 1, type =  Bundle.class) Bundle bundle) {
//		System.out.println("\n\n op call ssssss"+this.getDao());
		
		return bundle;
		
	}

	
}