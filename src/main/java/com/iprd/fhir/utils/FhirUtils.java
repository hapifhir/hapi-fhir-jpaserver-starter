package com.iprd.fhir.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.jpa.starter.service.ServerInterceptor;

import org.apache.jena.ext.xerces.util.URI.MalformedURIException;
import org.hl7.fhir.r4.model.Identifier;

public class FhirUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerInterceptor.class);

	public static String getOclIdentifier(List<Identifier> identifiers) {
		String oclId = null;
		for (Identifier identifier : identifiers) {
			if (identifier.hasSystem() && identifier.getSystem().equals("http://iprdgroup.com/identifiers/ocl")) {
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

	public static String getOclLink(List<Identifier> identifiers) {
		String oclId = null;
		for (Identifier identifier : identifiers) {
			if (identifier.hasSystem() && identifier.getSystem().equals("http://iprdgroup.com/identifiers/ocl")) {
				oclId = identifier.getValue();
			}
		}
		return oclId;
	}
	
	public static String getOclIdFromString(String query) throws MalformedURIException {
		try {
			URL url = new URL(query);
			String queryUrl = url.getQuery();
			if (queryUrl == null || queryUrl.isEmpty())
				return null;
			Map<String, String> queryMap = getQueryMap(queryUrl);
			if (queryMap.isEmpty() || !queryMap.containsKey("s")) return null;
			return queryMap.get("s");

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
}
