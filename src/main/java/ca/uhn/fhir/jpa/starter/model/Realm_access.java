package ca.uhn.fhir.jpa.starter.model;

import java.util.List;


public class Realm_access {

	List<String> roles;


	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public List<String> getRoles() {
		return roles;
	}

}