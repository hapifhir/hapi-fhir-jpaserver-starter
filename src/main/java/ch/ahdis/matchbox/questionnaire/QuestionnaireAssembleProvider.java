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

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.ExpressionNode;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.UriParam;
import ch.ahdis.matchbox.mappinglanguage.ConvertingWorkerContext;

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
  
  @Autowired
  private DaoRegistry myDaoRegistry;
  
  @Autowired
  protected ConvertingWorkerContext baseWorkerContext;

  
  public Questionnaire getQuestionnaire(String canonical) {
    SearchParameterMap params = new SearchParameterMap();
    params.setLoadSynchronousUpTo(1);
    params.add(Questionnaire.SP_URL, new UriParam(canonical));
    IBundleProvider search = myDaoRegistry.getResourceDao("Questionnaire").search(params);
    Integer size = search.size();
    if (size!=null && size.intValue()==1) {
      return (Questionnaire) search.getResources(0, 1).get(0);    
    }
    return null;
  }
  
  public List<QuestionnaireItemComponent> checkItemsList(Questionnaire questionnaire, List<QuestionnaireItemComponent> items, String linkdIdPrefix, boolean assembleContext) {
    String subLinkdIdPrefix = linkdIdPrefix;
    if (items != null) {
      List<QuestionnaireItemComponent> expanded = new ArrayList<QuestionnaireItemComponent>();
      for (QuestionnaireItemComponent item : items) {
        Extension extensionVariable = item.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/variable");
        if (extensionVariable != null &&  extensionVariable.getValue() instanceof Expression) {
          Expression expr = (Expression) extensionVariable.getValue();
          if ("linkIdPrefix".equals(expr.getName())) {
            FHIRPathEngine fp = new FHIRPathEngine(baseWorkerContext);
            ExpressionNode exp = fp.parse(expr.getExpression());
            // TODO: need to add linkIdPrefix as a variable to the FHIRPath expression, expression could also be    "expression" : "%linkIdPrefix + 'name.'", see https://build.fhir.org/ig/HL7/sdc/Parameters-sdc-modular-root-assembled.json.html
            List<Base> result = fp.evaluate(null, exp);
            subLinkdIdPrefix = result.get(0).primitiveValue();
          }
        }
        Extension extension = item.getExtensionByUrl("http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-subQuestionnaire");
        if (extension != null) {
          String canonical = extension.getValueAsPrimitive().getValueAsString();
          int version = canonical.lastIndexOf("|");
          if (version>0) {
            canonical = canonical.substring(0, version);
          }
          Questionnaire subQuestionnaire = this.getQuestionnaire(canonical);
          if (subQuestionnaire !=null) {
            Extension extensionAssembleContext = subQuestionnaire.getExtensionByUrl("http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-assembleContext");
            boolean propagateAssembleContext = (extensionAssembleContext != null && "linkIdPrefix".equals(extensionAssembleContext.getValueAsPrimitive().getValueAsString()));

            // An assembled Questionnaire SHALL refer to the original base
            // Questionnaire with a version-specific reference using the assembledFrom
            // extension. Questionnaires can also be searched by this extension using the If
            // a QuestionnaireResponse is based on an assembled Questionnaire, it SHOULD use
            // the URL specified in the assembledFrom extension rather than the URL and
            // version of the assembled Questionnaire itself.
            List<Extension> extensionsAssembledFrom = questionnaire.getExtensionsByUrl("http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-assembledFrom");
            boolean hasAssembledFromExtension = false;
            String canonicalReceived = subQuestionnaire.getUrl()+"|"+subQuestionnaire.getVersion();
            for(Extension extensionAssembledFrom: extensionsAssembledFrom) {
              if (canonicalReceived.equals(extensionAssembledFrom.getValueAsPrimitive().getValueAsString())) {
                hasAssembledFromExtension = true;
                continue;
              }
            }
            if (!hasAssembledFromExtension) {
              questionnaire.addExtension().setUrl("http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-assembledFrom").setValue(new CanonicalType(canonicalReceived));
            }
            expanded.addAll(checkItemsList(questionnaire, subQuestionnaire.getItem(), subLinkdIdPrefix, propagateAssembleContext));
          } else {
            expanded.add(item);
          }
        } else {
          QuestionnaireItemComponent copiedItem = item.copy();
          copiedItem.setLinkId((assembleContext ? linkdIdPrefix+item.getLinkId() : item.getLinkId()));
          if (item.getItem()!=null && item.getItem().size()>0) {
            copiedItem.getItem().clear();
            copiedItem.setItem(checkItemsList(questionnaire, item.getItem(), subLinkdIdPrefix, assembleContext));
          }
          expanded.add(copiedItem);
        }
      }
      return expanded;
    }
    return null;
  }

  @Operation(name = "$assemble", type = Questionnaire.class, idempotent = true)
  public Questionnaire assemble(@OperationParam(name = "questionnaire", min = 1, max = 1) Questionnaire questionnaire) {

    // Modular Questionnaires

    // TODO 5. In addition to propagating the items from the referenced
    // Questionnaire, additional items are also propagated as follows:
    // TODO 5.2 If language is declared on the imported Questionnaire, it must match
    // the language on the importing Questionnaire.
    // TODO 5.3 implicitRules and modifierExtension are prohibited and are
    // considered an error if present.
    
    // TODO 5.4 Any contained resources in the subQuestionnaire are added as
    // contained resources in the parent Questionnaire with the exception that,
    // barring linkIdPrefix requirements (see further below), if a subQuestionnaire
    // is imported more than once, contained resources will only be included once in
    // the assembled Questionnaire.

    // TODO 5.5.1 Extensions are propagated differently, depending on the nature of
    // the extension: propagate to the 'root' of the base Questionnaire: cqf-library
    // (check for uniqueness by url and only import once) launch-context (check for
    // uniqueness by code and only import once)

    // TODO 5.5.2 propagate to the item that contains the 'display' item being
    // substituted. (If the display item is at the root, then this will also be at
    // the root.): questionnaire-constraint
    // variable (check for uniqueness by name - duplication is an error)
    // itemPopulationContext (it is an error if there are multiple
    // itemPopulationContext resulting on the item this propagates to)
    // itemExtractionContext (it is an error if there are multiple
    // itemExtractionContext resulting on the item this propagates to)
    // TODO 5.5.2 all other extensions are ignored

    // TODO 7 It is possible that the same Questionnaire might be substituted more
    // than once as part of this process, however it is in an error if the module
    // references recurse. E.g. if Questionnaire A contains an item that references
    // subQuestionnaire B, which in turn has an element that references
    // subQuestionnaire A.
    // TOOD 8 It is also an error if the resulting fully-assembled Questionnaire has
    // any duplicate linkIds.
    // TODO 9 In order to avoid duplicate linkIds, a parent Questionnaire MAY
    // declare a special variable with the name linkIdPrefix. If there is a
    // linkIdPrefix in context at the time a subQuestionnaire is substituted, that
    // linkIdPrefix SHALL be pre-pended to the linkId and enableWhen.question
    // elements of all items in that Questionnaire. See the examples to see how this
    // works in practice. If linkIdPrefix is not used, care should be taken to
    // ensure that linkIds are appropriately coordinated to avoid overlap across all
    // referenced Questionnaires
    // TODO 10 LinkIdPrefixes are also prepended to the 'id' elements of any
    // contained resources and all references to contained resources (i.e.
    // references or canonicals that start with '#').
    // TODO 11 linkIdPrefixes will need to be referenced in any expressions that are
    // dependent on linkId. For example, if there is an expression that says
    // "%root.descendants().select(item.where(linkId='1.1'))" would need to change
    // to "%root.descendants().select(item.where(linkId=%linkIdPrefix + '1.1'))"
    // TODO 12 Imported Questionnaires may be defined to be dependent on contextual
    // information passed in from the referencing Questionnaire (including
    // 'linkIdPrefix'). To allow validating these subQuestionnaires independent of
    // their inclusion in a parent, all such dependencies must be declared using the
    // assembleContext extension on the root of the Questionnaire. As well, if a
    // Questionnaire is referenced by a subQuestionnaire extension, it is an error
    // if the listed variables are not available in the context of the referencing
    // element.
    // TODO 13 The presence of an assembleContext extension on a Questionnaire
    // indicates that it can ONLY be used as a part of a modular Questionnaire.
    
    
    Questionnaire questAssembled = questionnaire.copy();
    questAssembled.setText(null);
    
    // 5.7 The assemble-expectation on the resulting Questionnaire should be
    // adjusted to change the 'assemble' part of the code to 'independent', with the
    // exception that if the code was 'assemble-root', then the extension should be
    // removed entirely. E.g. assemble-root-or-child would change to
    // independent-root-or-child.
    
    Extension extensionAssembleExpectation = questAssembled.getExtensionByUrl("http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-assemble-expectation");
    if (extensionAssembleExpectation != null) {
      String code = extensionAssembleExpectation.getValueAsPrimitive().getValueAsString();
      if ("assemble-root".equals(code)) {
        questAssembled.getExtension().remove(extensionAssembleExpectation);
      } else {
        extensionAssembleExpectation.getValueAsPrimitive().setValueAsString(code.replace("assemble", "independent"));
      }
    }
    // TODO 14 If stored, an assembled Questionnaire SHALL have the same URL as the
    // base Questionnaire but must have a distinct version - typically either a UUID
    // or "[version]-assembled"
    questAssembled.setVersion(questAssembled.getVersion()+"-assembled");
    
      
    questAssembled.setItem(checkItemsList(questAssembled, questionnaire.getItem(), "", false));
    return questAssembled;
  }

}
