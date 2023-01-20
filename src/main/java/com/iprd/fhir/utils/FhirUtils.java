package com.iprd.fhir.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.jpa.starter.service.FhirClientAuthenticatorService;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iprd.report.FhirPath;

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

}
