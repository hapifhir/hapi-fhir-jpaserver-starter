/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 * <p>
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package ca.uhn.fhir.jpa.starter.smart.model;

import ca.uhn.fhir.jpa.starter.smart.exception.InvalidClinicalScopeException;

public class SmartClinicalScope {

	private final String compartment;
	private final String resource;
	private final SmartOperationEnum operation;

	public SmartClinicalScope(String compartment, String resource, SmartOperationEnum operation) {
		this.compartment = compartment;
		this.resource = resource;
		this.operation = operation;
	}

	public SmartClinicalScope(String scope) {
		if(scope.matches("([A-z]*/([A-z]*|[*])[.]([*]|[A-z]*))")){
			String[] parts = scope.split("/");
			compartment = parts[0];
			String[] resourceAndOperation = parts[1].split("[.]");
			resource = resourceAndOperation[0];
			operation = SmartOperationEnum.findByValue(resourceAndOperation[1]);
		} else{
			throw new InvalidClinicalScopeException(scope+" is not a valid clinical scope");
		}

	}

	public String getCompartment(){
		return compartment;
	}

	public String getResource() {
		return resource;
	}

	public SmartOperationEnum getOperation() {
		//fix logic in base class, and not or
		return operation;
	}

}
