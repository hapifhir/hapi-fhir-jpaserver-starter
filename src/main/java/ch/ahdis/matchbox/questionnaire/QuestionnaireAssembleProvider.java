 /*
 * #%L
 * Matchbox
 * %%
 * Copyright (c) 2022- by RALY GmbH. All rights reserved.
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
package ch.ahdis.matchbox.questionnaire;

import org.hl7.fhir.r4.model.Questionnaire;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;

/**
 * $assemble operation for Questionnaire Resource
 * 
 * http://build.fhir.org/ig/HL7/sdc/OperationDefinition-Questionnaire-assemble.html
 * 
 * Algorithm The assembly process for a modular questionnaire proceeds as
 * follows:
 * 
 * 1) Resolve all subQuestionnaire extensions as described in the Modular Forms
 * page. If there is an issue resolving any of the subQuestionnaires or applying
 * the resolution process results in any errors, the operation SHOULD fail.
 * 
 * 2) Propagate all relevant information from any declared item.definition
 * elements on all resulting items, as described on the Modular Forms page.
 * 
 * 3) Remove the is-modular extension from the Questionnaire - because it is not
 * modular anymore!
 * 
 * 4) Add the extension, pointing to the canonical URL and version of the
 * Questionnaire that was assembled.
 * 
 * 5) Modify the Questionnaire.version to either be a UUID or append
 * "-assembled".
 * 
 * 6) Optionally, check the resulting Questionnaire to ensure that it is valid
 * according to the base Questionnaire and possibly any declared profiles. If
 * the resulting Questionnaire is not valid, return a warning.
 *
 * The result of the operation will be one of three things:
 * 
 * - If there are any errors, there will be a 4xx or 5xx error code and, ideally
 * an OperationOutcome as the body of the response.
 * 
 * - If there are no errors, warnings or information messages that result from
 * the assembly process, the body can just be the bare Questionnaire resource
 * that resulted from the operation.
 * 
 * - If there are any warnings or information messages, then the body will be a
 * Parameters instance with two parameters - 'response' containing the resulting
 * Questionnaire and 'outcome' containing an OperationOutcome with the warning
 * and/or information messages.
 */
public class QuestionnaireAssembleProvider {

	@Operation(name = "$assemble", type = Questionnaire.class, idempotent = true)
	public Questionnaire assemble(
			@OperationParam(name = "questionnaire", min = 1, max = 1) Questionnaire questionnaire) {

		return questionnaire;
	}

}
