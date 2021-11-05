package ca.uhn.fhir.jpa.starter.smart.util;

public class SmartResourceMapping {

	String resourceType;
	String launchCtxLabel;

	public SmartResourceMapping(String resourceType, String launchCtxLabel) {
		this.resourceType = resourceType;
		this.launchCtxLabel = launchCtxLabel;
	}

	public String getResourceType() {
		return resourceType;
	}

	public String getLaunchCtxLabel() {
		return launchCtxLabel;
	}
}
