package ch.ahdis.matchbox.mappinglanguage;

/*
 * #%L
 * Matchbox Engine
 * %%
 * Copyright (C) 2022 ahdis
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

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.utils.structuremap.ITransformerServices;
import org.hl7.fhir.utilities.Utilities;

public class TransformSupportServices implements ITransformerServices {

  private Parameters.ParametersParameterComponent traceToParameter;

  private List<Base> outputs;
  private IWorkerContext context;
  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TransformSupportServices.class);

  public TransformSupportServices(IWorkerContext worker, List<Base> outputs) {
    this.context = worker;
    this.outputs = outputs;
  }


  // matchbox patch https://github.com/ahdis/matchbox/issues/264
  @Override
  public Base createType(Object appInfo, String name, ProfileUtilities profileUtilities) throws FHIRException {
    StructureDefinition sd = context.fetchResource(StructureDefinition.class, name);
    if (sd == null) {
      if (Utilities.existsInList(name, "http://hl7.org/fhirpath/System.String")) {
        sd = context.fetchTypeDefinition("string"); 
      }
    }
    if (sd == null) {
      throw new FHIRException("Unable to create type "+name);
    } 
    return Manager.build(context, sd, profileUtilities);
  }

  public void setTraceToParameter(Parameters.ParametersParameterComponent traceToParameter) {
    this.traceToParameter = traceToParameter;
  }

  @Override
  public Base createResource(Object appInfo, Base res, boolean atRootofTransform) {
    if (atRootofTransform)
      outputs.add(res);
    return res;
  }

  @Override
  public Coding translate(Object appInfo, Coding source, String conceptMapUrl) throws FHIRException {
    ConceptMapEngine cme = new ConceptMapEngine(context);
    return cme.translate(source, conceptMapUrl);
  }

  @Override
  public Base resolveReference(Object appContext, String url) throws FHIRException {	
   	org.hl7.fhir.r5.model.Resource resource = context.fetchResource(org.hl7.fhir.r5.model.Resource.class, url);
   	return resource;
//    if (resource != null) {
//      String inStr = FhirContext.forR4Cached().newJsonParser().encodeResourceToString(resource);
//      try {
//        return Manager.parseSingle(context, new ByteArrayInputStream(inStr.getBytes()), FhirFormat.JSON);
//      } catch (IOException e) {
//        throw new FHIRException("Cannot convert resource to element model");
//      }
//    }
//    throw new FHIRException("resolveReference, url not found: " + url);
  }

  @Override
  public List<Base> performSearch(Object appContext, String url) throws FHIRException {
    throw new FHIRException("performSearch is not supported yet");
  }

  @Override
  public void log(String message) {
     if (traceToParameter != null) {
        Parameters.ParametersParameterComponent traceValue = traceToParameter.addPart();
        traceValue.setName("debug");
        traceValue.setValue(new StringType(message));
      } else {
        log.info(message);
      }
    }
    
}