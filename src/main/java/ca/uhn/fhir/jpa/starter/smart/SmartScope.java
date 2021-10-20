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
package ca.uhn.fhir.jpa.starter.smart;

// Originally from https://github.com/igia/igia-fhir-autoconfigure
public class SmartScope {
	private String scope;

	public SmartScope(String scope) {
		this.scope = scope;
	}

	public boolean isResourceScope(String resourceName){
		return resourceName.equalsIgnoreCase(firstPartOrNull());
	}

	public String getResource() {
		if (!isResourceScope("patient") && !isResourceScope("user"))
			return null;

		int forwardSlashIndex = this.scope.indexOf("/");
		int periodIndex = this.scope.indexOf(".");

		return this.scope.substring(forwardSlashIndex + 1, periodIndex);
	}

	public String getOperation() {
		//fix logic in base class, and not or
		if (!isResourceScope("patient") && !isResourceScope("user"))
			return null;

		int periodIndex = this.scope.indexOf(".");

		return this.scope.substring(periodIndex + 1);
	}


	private String firstPartOrNull() {
		if (scope == null) {
			return null;
		}

		int forwardSlashIndex = this.scope.indexOf("/");

		if (forwardSlashIndex == -1) {
			return null;
		}

		return this.scope.substring(0, forwardSlashIndex);
	}
}
