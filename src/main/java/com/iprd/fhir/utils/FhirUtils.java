package com.iprd.fhir.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import ca.uhn.fhir.jpa.starter.service.FhirClientAuthenticatorService;
import kotlin.Triple;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kotlin.Pair;

import org.apache.jena.ext.xerces.util.URI.MalformedURIException;
import org.hl7.fhir.r4.model.Identifier;

public class FhirUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(FhirUtils.class);

	public static Boolean isOclPatient(List<Identifier> identifiers) {
		for (Identifier identifier : identifiers) {
			if (identifier.hasSystem() && identifier.getSystem().equals("http://iprdgroup.com/identifiers/patientWithOcl")) {
				return true;
			}
		}
		return false;
	}
	
	public static Triple<String, String, String> getOclIdFromIdentifier(List<Identifier> identifiers) {
		Triple<String, String, String> oclId = null;
		String oclIdentifierValue = null;
		for (Identifier identifier : identifiers) {
			if (identifier.hasSystem() && identifier.getSystem().equals("http://iprdgroup.com/identifiers/ocl")) {
				oclIdentifierValue = identifier.getValue();
				break;
			}
		}
		try {
			oclId = getOclIdFromString(oclIdentifierValue);
		} catch (MalformedURIException e) {
			logger.debug(e.getMessage());
		}
		return oclId;
	}

	public static String getPatientCardNumber(List<Identifier> identifiers) {
		for (Identifier identifier : identifiers) {
			if (identifier.hasSystem() && identifier.getSystem().equals("http://iprdgroup.com/identifiers/patient-card")) {
				return identifier.getValue();
			}
		}
		return null;
	}

	public static String getOclLink(List<Identifier> identifiers) {
		for (Identifier identifier : identifiers) {
			if (identifier.hasSystem() && identifier.getSystem().equals("http://iprdgroup.com/identifiers/ocl")) {
				return identifier.getValue();
			}
		}
		return null;
	}
	
	public static Triple<String, String, String> getOclIdFromString(String query) throws MalformedURIException {
		try {
			URL url = new URL(query);
			String queryUrl = url.getQuery();
			if (queryUrl == null || queryUrl.isEmpty())
				return null;
			Map<String, String> queryMap = getQueryMap(queryUrl);
			if (queryMap.isEmpty() || !queryMap.containsKey("s")) return null;
			return new Triple<>(queryMap.get("s"), queryMap.get("v"), queryMap.get("g"));

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	public static Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();

		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}

	public static void getBundleBySearchUrl(Bundle bundle, String url) {
		Bundle searchBundle = FhirClientAuthenticatorService.getFhirClient().search()
			.byUrl(url)
			.returnBundle(Bundle.class)
			.execute();
		bundle.getEntry().addAll(searchBundle.getEntry());
		// Recursively adding all the resources to the bundle if it contains next URL.
		if (searchBundle.hasLink() && bundleContainsNext(searchBundle)) {
			getBundleBySearchUrl(bundle, getNextUrl(searchBundle.getLink()));
		}
	}

	public static boolean bundleContainsNext(Bundle bundle) {
		for (Bundle.BundleLinkComponent link : bundle.getLink()) {
			if (link.getRelation().equals("next"))
				return true;
		}
		return false;
	}

	public static String getNextUrl(List<Bundle.BundleLinkComponent> bundleLinks) {
		for (Bundle.BundleLinkComponent link : bundleLinks) {
			if (link.getRelation().equals("next")) {
				return link.getUrl();
			}
		}
		return null;
	}

	public static String getPractitionerRoleFromId(String practitionerRoleId){
		Bundle bundle = FhirClientAuthenticatorService.getFhirClient().search().forResource(PractitionerRole.class).where(PractitionerRole.RES_ID.exactly().identifier(practitionerRoleId)).returnBundle(Bundle.class).execute();
		if (!bundle.hasEntry()) {
			return null;
		}
		PractitionerRole practitionerRole = (PractitionerRole) bundle.getEntry().get(0).getResource();
		String role = practitionerRole.getCodeFirstRep().getCodingFirstRep().getCode();
		return role;
	}

	public static Pair<List<String>,List<Identifier>> getMissingIdentifierAndNewIdentifier(List<Identifier> identifierOldList, List<Identifier> identifierNewList) {
	    List<String> missingFromNew = new ArrayList<String>();
		 List<Identifier> missingFromOldIdentifiers = new ArrayList<Identifier>();
	    Set<String> newIdentifiers = new HashSet<String>();
		 HashMap<String,Integer> valueToIndex = new HashMap<>();
	    int index =0;
	    // Add all identifiers in the new list to the set
	    for (Identifier identifier : identifierNewList) {
	    	if(identifier.getSystem().equals("http://iprdgroup.com/identifiers/ocl") || identifier.getSystem().equals("http://iprdgroup.com/identifiers/patient-card")) {
		        newIdentifiers.add(identifier.getValue());
				  valueToIndex.put(identifier.getValue(),index);
	    	}
			 index+=1;
	    }
	    // Check if each identifier in the old list is present in the new list
	    for (Identifier identifier : identifierOldList) {
	        if (!newIdentifiers.contains(identifier.getValue())) {
		    	if(identifier.getSystem().equals("http://iprdgroup.com/identifiers/ocl")|| identifier.getSystem().equals("http://iprdgroup.com/identifiers/patient-card")) {
		    		missingFromNew.add(identifier.getValue());
		    	}
	        }else {
				  missingFromOldIdentifiers.add(identifierNewList.get(valueToIndex.get(identifier.getValue())));
	        }
	    }
	    
	    return new Pair(missingFromNew,missingFromOldIdentifiers);
	}

	public enum KeyId{
		APPCLIENT,
		DASHBOARD
	}
}
