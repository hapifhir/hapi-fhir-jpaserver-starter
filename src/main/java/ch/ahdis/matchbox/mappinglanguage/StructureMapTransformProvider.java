package ch.ahdis.matchbox.mappinglanguage;

import java.io.IOException;
import java.util.ArrayList;
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

import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r5.model.Property;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.provider.SimpleWorkerContextProvider;

public class StructureMapTransformProvider extends SimpleWorkerContextProvider<StructureMap> implements IResourceProvider {
  
  private StructureMapUtilities utils = null;
  

  public StructureMapTransformProvider(SimpleWorkerContext fhirContext) {
    super(fhirContext, StructureMap.class);
    utils = new StructureMapUtilities(fhirContext, new TransformSupportServices(fhirContext, new ArrayList<Base>()));
  }
 
  @Create
  public MethodOutcome createStructureMap(@ResourceParam StructureMap theResource) {
    log.debug("created structuredmap, caching");

    // FIXME: don't know why a # is prefixed to the contained it
    for (org.hl7.fhir.r4.model.Resource r : theResource.getContained()) {
      if (r instanceof org.hl7.fhir.r4.model.ConceptMap && ((org.hl7.fhir.r4.model.ConceptMap) r).getId().startsWith("#")) {
        r.setId(((org.hl7.fhir.r4.model.ConceptMap) r).getId().substring(1));
      }
      // If a contained element is not referred it will not serialized in hapi
      if (r instanceof org.hl7.fhir.r4.model.ConceptMap) {
        Extension e = new Extension();
        Reference reference = new Reference();
        reference.setReference("#"+r.getId());
        e.setValue(reference);
        e.setUrl("http://fhir.ch/reference");
        theResource.addExtension(e);
      }
    }
    theResource.setId(theResource.getName());
    theResource = updateWorkerContext(theResource);
    MethodOutcome retVal = new MethodOutcome();
    retVal.setCreated(true);
    retVal.setResource(theResource);
    return retVal;
  }

  public StructureMap updateWorkerContext(StructureMap theResource) {
    org.hl7.fhir.r5.model.StructureMap cached = fhirContext.fetchResource(org.hl7.fhir.r5.model.StructureMap.class, theResource.getUrl());
    if (cached != null) {
      fhirContext.dropResource(cached);
    }    
    
    org.hl7.fhir.r5.model.StructureMap mapR5 = (org.hl7.fhir.r5.model.StructureMap) VersionConvertor_40_50.convertResource(theResource);
    mapR5.getText().setStatus(NarrativeStatus.GENERATED);
    mapR5.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
    String render = StructureMapUtilities.render(mapR5);
    mapR5.getText().getDiv().addTag("pre").addText(render);

    fhirContext.cacheResource(mapR5);
    
    return (StructureMap) VersionConvertor_40_50.convertResource(mapR5);
  }
  
  @Delete()
  public void deleteStructureMap(@IdParam IdType theId) {
      org.hl7.fhir.r5.model.StructureMap cached = fhirContext.fetchResource(org.hl7.fhir.r5.model.StructureMap.class, theId.getId());
      if (cached == null) {
          throw new ResourceNotFoundException("Unknown version");
      }
      fhirContext.dropResource(cached);
      return; //
  }
  
  @Update
  public MethodOutcome update(@IdParam IdType theId, @ResourceParam StructureMap theResource) {
     updateWorkerContext(theResource);
     return new MethodOutcome();
  }
  
  @Read()
  public org.hl7.fhir.r4.model.Resource getResourceById(@IdParam IdType theId) {
    return VersionConvertor_40_50.convertResource(getMapByUrl(theId.getId()));
  }



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
    org.hl7.fhir.r5.model.StructureMap map = (org.hl7.fhir.r5.model.StructureMap) fhirContext.fetchResourceById(theStructureMap.getResourceType(), theStructureMap.getIdPart());
    if (map == null) {
      throw new UnprocessableEntityException("Map not available with id "+theStructureMap.getIdPart());
    }
    transfrom(map, theServletRequest, theServletResponse);
  }
  
      
  @Operation(name = "$transform", manualResponse = true, manualRequest = true)
  public void manualInputAndOutput(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
      throws IOException {

    Map<String, String[]> requestParams = theServletRequest.getParameterMap();
    String[] source = requestParams.get("source");
    if (source != null && source.length > 0) {
      
      org.hl7.fhir.r5.model.StructureMap map  = fhirContext.getTransform(source[0]);
      if (map == null) {
          throw new UnprocessableEntityException("Map not available with canonical url "+source[0]);
      }
      transfrom(map, theServletRequest, theServletResponse);
    } else {
      throw new UnprocessableEntityException("No source parameter provided");
    }
  }

  
  
  public void transfrom(org.hl7.fhir.r5.model.StructureMap map, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {

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

    org.hl7.fhir.r5.elementmodel.Element r = getTargetResourceFromStructureMap(map);
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

  private org.hl7.fhir.r5.elementmodel.Element getTargetResourceFromStructureMap(org.hl7.fhir.r5.model.StructureMap map) {
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

  public org.hl7.fhir.r5.model.StructureMap getMapByUrl(String url) {
    return fhirContext.fetchResource(org.hl7.fhir.r5.model.StructureMap.class, url);
  }


  
}
