package ch.ahdis.fhir.hapi.jpa.validation;

/*
 * #%L
 * Matchbox Server
 * %%
 * Copyright (C) 2018 - 2019 ahdis
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.validation.ValidationMessage;

import ca.uhn.fhir.validation.*;

/**
 * Base class for a bridge between the RI validation tools and HAPI
 */
abstract class BaseValidatorBridge implements IValidatorModule {

	public BaseValidatorBridge() {
		super();
	}

	private void doValidate(IValidationContext<?> theCtx) {
		List<ValidationMessage> messages = validate(theCtx);

		for (ValidationMessage riMessage : messages) {
			SingleValidationMessage hapiMessage = new SingleValidationMessage();
			if (riMessage.getCol() != -1) {
				hapiMessage.setLocationCol(riMessage.getCol());
			}
			if (riMessage.getLine() != -1) {
				hapiMessage.setLocationLine(riMessage.getLine());
			}
			hapiMessage.setLocationString(riMessage.getLocation());
			hapiMessage.setMessage(riMessage.getMessage());
			if (riMessage.getLevel() != null) {
				hapiMessage.setSeverity(ResultSeverityEnum.fromCode(riMessage.getLevel().toCode()));
			}
			theCtx.addValidationMessage(hapiMessage);
		}
	}

	protected abstract List<ValidationMessage> validate(IValidationContext<?> theCtx);

	@Override
	public void validateResource(IValidationContext<IBaseResource> theCtx) {
		doValidate(theCtx);
	}

}