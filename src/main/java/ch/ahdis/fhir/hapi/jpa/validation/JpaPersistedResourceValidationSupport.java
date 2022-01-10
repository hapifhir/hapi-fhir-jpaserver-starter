package ch.ahdis.fhir.hapi.jpa.validation;
/*-
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2021 Smile CDR, Inc.
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

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.server.IBundleProvider;

public class JpaPersistedResourceValidationSupport extends ca.uhn.fhir.jpa.dao.JpaPersistedResourceValidationSupport {
  
  @Autowired
  private DaoRegistry myDaoRegistry;

  /**
   * Constructor
   */
  public JpaPersistedResourceValidationSupport(FhirContext theFhirContext) {
    super(theFhirContext);
    Validate.notNull(theFhirContext);
  }

  
  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T extends IBaseResource> List<T> fetchAllStructureDefinitions() {
    IBundleProvider search = myDaoRegistry.getResourceDao("StructureDefinition").search(new SearchParameterMap().setLoadSynchronousUpTo(5000));
    return (List<T>) search.getResources(0, 5000);
  }


}
