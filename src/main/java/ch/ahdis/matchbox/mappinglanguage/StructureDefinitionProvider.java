package ch.ahdis.matchbox.mappinglanguage;

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
import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.jpa.rp.r4.StructureDefinitionResourceProvider;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ch.ahdis.matchbox.interceptor.ImplementationGuidePackageInterceptor;

/**
 * StructureDefinitionProvider
 *
 * 
 */
public class StructureDefinitionProvider extends StructureDefinitionResourceProvider {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructureDefinitionProvider.class);

  public StructureDefinitionProvider() {
    super();
  }

  @Autowired
  private IValidationSupport validationSupport;
  
  @Autowired
  protected ConvertingWorkerContext baseWorkerContext;

  @Override
  public MethodOutcome create(HttpServletRequest theRequest, StructureDefinition theResource, String theConditional,
      RequestDetails theRequestDetails) {
    theResource = createSnaphshotIfEmpty(theResource);
    if (theResource.getUrl() != null) {
      org.hl7.fhir.r5.model.StructureDefinition existing = baseWorkerContext.fetchResource(org.hl7.fhir.r5.model.StructureDefinition.class, theResource.getUrl());
      if (existing != null) {
        theResource.setId(existing.getId());
        IdType theId = new IdType();
        theId.setId(existing.getId());
        return super.update(theRequest, theResource, theId, theConditional, theRequestDetails);
      }
    }
    return super.create(theRequest, theResource, theConditional, theRequestDetails);
  }

  @Override
  public MethodOutcome update(HttpServletRequest theRequest, StructureDefinition theResource, IIdType theId,
      String theConditional, RequestDetails theRequestDetails) {
    theResource = createSnaphshotIfEmpty(theResource);
    return super.update(theRequest, theResource, theId, theConditional, theRequestDetails);
  }

  private StructureDefinition createSnaphshotIfEmpty(StructureDefinition theResource) {
    if (theResource.getSnapshot() == null || theResource.getSnapshot().getElement() == null
        || theResource.getSnapshot().getElement().size() == 0) {
      // create Snapshot is currently not effective (<10s), seems to reload
      log.info("create Snapshot start");
      theResource = (StructureDefinition) validationSupport
          .generateSnapshot(baseWorkerContext.getMyValidationSupportContext(), theResource, null, null, null);
      log.info("create Snapshot end");
    }
    return theResource;
  }

}
