package ca.uhn.fhir.jpa.starter.model;

import java.util.List;

public class Account {

	List<String> roles;

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public List<String> getRoles() {
		return roles;
	}

}