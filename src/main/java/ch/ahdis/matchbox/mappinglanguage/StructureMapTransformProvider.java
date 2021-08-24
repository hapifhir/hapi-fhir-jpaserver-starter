package ch.ahdis.matchbox.mappinglanguage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
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
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Property;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;

/**
 * $transform Operation for StructureMaps
 *
 */
public class StructureMapTransformProvider {
  
  private StructureMapUtilities utils = null;
  
  @Autowired
  protected FhirContext myFhirCtx;
	
  @Autowired
  protected ConvertingWorkerContext baseWorkerContext;
  

  /*public StructureMapTransformProvider(SimpleWorkerContext fhirContext) {
    super(fhirContext, StructureMap.class);
    utils = new StructureMapUtilities(fhirContext, new TransformSupportServices(fhirContext, new ArrayList<Base>()));
  }*/
 
  

  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructureMapTransformProvider.class);

  private void removeBundleEntryIds(org.hl7.fhir.r5.elementmodel.Element bundle) {
    List<Element> ids = bundle.getChildrenByName("id");
    for(Element id: ids) {
      bundle.getChildren().remove(id);
    }
    List<Element> entries = bundle.getChildrenByName("entry");
    for(Element entry : entries) {
      Property fullUrl = entry.getChildByName("fullUrl");
      if (fullUrl.getValues()!=null && fullUrl.getValues().get(0).primitiveValue().startsWith("urn:uuid:")) {
        Property resource = entry.getChildByName("resource");
        if (resource!=null && resource.getValues()!=null) {
          Element entryResource = (Element) resource.getValues().get(0);
          ids = entryResource.getChildrenByName("id");
          for(Element id: ids) {
            entryResource.getChildren().remove(id);
          }
        }
      }
    }
  }

  
  @Operation(name = "$transform", manualResponse = true, manualRequest = true)
  public void manualInputAndOutput(@IdParam IdType theStructureMap, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
	  ConvertingWorkerContext fhirContext = new ConvertingWorkerContext(baseWorkerContext);
    org.hl7.fhir.r5.model.StructureMap map = (org.hl7.fhir.r5.model.StructureMap) fhirContext.fetchResourceById(theStructureMap.getResourceType(), theStructureMap.getIdPart());
    if (map == null) {
      throw new UnprocessableEntityException("Map not available with id "+theStructureMap.getIdPart());
    }
    transfrom(map, theServletRequest, theServletResponse, fhirContext);
  }
  
      
  @Operation(name = "$transform", manualResponse = true, manualRequest = true)
  public void manualInputAndOutput(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
      throws IOException {
	  ConvertingWorkerContext fhirContext = new ConvertingWorkerContext(baseWorkerContext);
    Map<String, String[]> requestParams = theServletRequest.getParameterMap();
    String[] source = requestParams.get("source");
    if (source != null && source.length > 0) {
      
      org.hl7.fhir.r5.model.StructureMap map  = fhirContext.getTransform(source[0]);
      if (map == null) {
          throw new UnprocessableEntityException("Map not available with canonical url "+source[0]);
      }
      transfrom(map, theServletRequest, theServletResponse, fhirContext);
    } else {
      throw new UnprocessableEntityException("No source parameter provided");
    }
  }

  
  
  public void transfrom(org.hl7.fhir.r5.model.StructureMap map, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse, ConvertingWorkerContext fhirContext) throws IOException {

    String contentType = theServletRequest.getContentType();
    org.hl7.fhir.r5.elementmodel.Element src = Manager.parse(fhirContext, theServletRequest.getInputStream(),
        contentType.contains("xml") ? FhirFormat.XML : FhirFormat.JSON);
    Set<String> highestRankedAcceptValues = RestfulServerUtils
        .parseAcceptHeaderAndReturnHighestRankedOptions(theServletRequest);

    String responseContentType = Constants.CT_FHIR_XML_NEW;
    if (highestRankedAcceptValues.contains(Constants.CT_FHIR_JSON_NEW)) {
      responseContentType = Constants.CT_FHIR_JSON_NEW;
    }
    // patch for fhir-kit-client https://github.com/Vermonster/fhir-kit-client/pull/143
    if (highestRankedAcceptValues.contains(Constants.CT_FHIR_JSON)) {
      responseContentType = Constants.CT_FHIR_JSON_NEW;
    }

    org.hl7.fhir.r5.elementmodel.Element r = getTargetResourceFromStructureMap(map, fhirContext);
    if (r == null) {
      throw new UnprocessableEntityException("Target Structure can not be resolved from map, is the corresponding implmentation guide provided?");
    }
    
    utils.transform(null, src, map, r);
    if (r.isResource() && "Bundle".contentEquals(r.getType())) {
      Property bundleType = r.getChildByName("type");
      if (bundleType!=null && bundleType.getValues()!=null && "document".equals(bundleType.getValues().get(0).primitiveValue())) {
        removeBundleEntryIds(r);
      }
    }
    theServletResponse.setContentType(responseContentType);
    theServletResponse.setCharacterEncoding("UTF-8");
    ServletOutputStream output = theServletResponse.getOutputStream();

    if (output != null) {
      if (output != null && responseContentType.equals(Constants.CT_FHIR_JSON_NEW))
        new org.hl7.fhir.r5.elementmodel.JsonParser(fhirContext).compose(r, output, OutputStyle.PRETTY, null);
      else
        new org.hl7.fhir.r5.elementmodel.XmlParser(fhirContext).compose(r, output, OutputStyle.PRETTY, null);
    }
    theServletResponse.getOutputStream().close();

  }

  private org.hl7.fhir.r5.elementmodel.Element getTargetResourceFromStructureMap(org.hl7.fhir.r5.model.StructureMap map, IWorkerContext fhirContext) {
    String targetTypeUrl = null;
    for (StructureMapStructureComponent component : map.getStructure()) {
      if (component.getMode() == org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.TARGET) {
        targetTypeUrl = component.getUrl();
        break;
      }
    }

    if (targetTypeUrl == null)
      throw new FHIRException("Unable to determine resource URL for target type");

    StructureDefinition structureDefinition = null;
    for (StructureDefinition sd : fhirContext.getStructures()) {
      if (sd.getUrl().equalsIgnoreCase(targetTypeUrl)) {
        structureDefinition = sd;
        break;
      }
    }
    if (structureDefinition == null)
      throw new FHIRException("Unable to determine StructureDefinition for target type");

    return Manager.build(fhirContext, structureDefinition);
  }
  

  
}
