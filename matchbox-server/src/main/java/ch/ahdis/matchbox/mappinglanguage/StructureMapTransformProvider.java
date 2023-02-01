package ch.ahdis.matchbox.mappinglanguage;

import java.io.IOException;
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

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.MatchboxEngineSupport;
import ch.ahdis.matchbox.StructureMapResourceProvider;
import ch.ahdis.matchbox.engine.MatchboxEngine;

/**
 * StructureMapTransformProvider

 *
 */
public class StructureMapTransformProvider extends StructureMapResourceProvider {
	
	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;
  
  public void createNarrative(StructureMap map) {
    if (!map.hasText()) {
      map.getText().setStatus(NarrativeStatus.GENERATED);
      map.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
      String render = StructureMapUtilities.render((org.hl7.fhir.r5.model.StructureMap) VersionConvertorFactory_40_50.convertResource(map));
      map.getText().getDiv().addTag("pre").addText(render);
    }
  }
  
  @Override
  public MethodOutcome create(HttpServletRequest theRequest, StructureMap theResource, String theConditional,
      RequestDetails theRequestDetails) {
    createNarrative(theResource);
    return super.create(theRequest, theResource, theConditional, theRequestDetails);
  }

  @Override
  public MethodOutcome update(HttpServletRequest theRequest, StructureMap theResource, IIdType theId,
      String theConditional, RequestDetails theRequestDetails) {
    createNarrative(theResource);
    return super.update(theRequest, theResource, theId, theConditional, theRequestDetails);
  }

  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructureMapTransformProvider.class);
  
  public StructureMap fixMap(@ResourceParam StructureMap theResource) {
    if (theResource!=null) {
      // don't know why a # is prefixed to the contained it
      for (org.hl7.fhir.r4.model.Resource r : theResource.getContained()) {
        if (r instanceof org.hl7.fhir.r4.model.ConceptMap && ((org.hl7.fhir.r4.model.ConceptMap) r).getId().startsWith("#")) {
          r.setId(((org.hl7.fhir.r4.model.ConceptMap) r).getId().substring(1));
        }
      }
    }
    return theResource;
  }

//  private void removeBundleEntryIds(org.hl7.fhir.r5.elementmodel.Element bundle) {
//    List<Element> ids = bundle.getChildrenByName("id");
//    for(Element id: ids) {
//      bundle.getChildren().remove(id);
//    }
//    List<Element> entries = bundle.getChildrenByName("entry");
//    for(Element entry : entries) {
//      Property fullUrl = entry.getChildByName("fullUrl");
//      if (fullUrl.getValues()!=null && fullUrl.getValues().get(0).primitiveValue().startsWith("urn:uuid:")) {
//        Property resource = entry.getChildByName("resource");
//        if (resource!=null && resource.getValues()!=null) {
//          Element entryResource = (Element) resource.getValues().get(0);
//          ids = entryResource.getChildrenByName("id");
//          for(Element id: ids) {
//            entryResource.getChildren().remove(id);
//          }
//        }
//      }
//    }
//  }

  /*
  @Operation(name = "$transform", manualResponse = true, manualRequest = true)
  public void manualInputAndOutput(@IdParam IdType theStructureMap, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
    org.hl7.fhir.r5.model.StructureMap map = (org.hl7.fhir.r5.model.StructureMap) baseWorkerContext.fetchResourceById(theStructureMap.getResourceType(), theStructureMap.getIdPart());
    if (map == null) {
      throw new UnprocessableEntityException("Map not available with id "+theStructureMap.getIdPart());
    }
    transfrom(map, theServletRequest, theServletResponse, baseWorkerContext);
  }
  */
 
  @Operation(name = "$transform", type = StructureMap.class, manualResponse = true, manualRequest = true)
  public void manualInputAndOutput(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
      throws IOException {
    Map<String, String[]> requestParams = theServletRequest.getParameterMap();
    String[] source = requestParams.get("source");
    if (source != null && source.length > 0) {
      MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(source[0], null, true, false);
      if (matchboxEngine == null) {
        throw new UnprocessableEntityException("matchbox engine could not be initialized with canonical url "+source[0]);
      }
      org.hl7.fhir.r5.model.StructureMap map  = matchboxEngine.getContext().fetchResource(org.hl7.fhir.r5.model.StructureMap.class, source[0]);
      if (map == null) {
          throw new UnprocessableEntityException("Map not available with canonical url "+source[0]);
      }
      for (StructureMapStructureComponent component  : map.getStructure()) {
        if (component.getUrl() != null && matchboxEngine.getStructureDefinition(component.getUrl()) == null) {
          throw new UnprocessableEntityException("matchbox engine could not be initialized with canonical url required for transform for "+source[0]+ " component "+component.getUrl());
        }
      }
      transform(map, theServletRequest, theServletResponse, matchboxEngine);
    } else {
      throw new UnprocessableEntityException("No source parameter provided");
    }
  }
    
  public void transform(org.hl7.fhir.r5.model.StructureMap map, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse, MatchboxEngine matchboxEngine) throws IOException {

    String contentType = theServletRequest.getContentType();

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

    org.hl7.fhir.r5.elementmodel.Element r = matchboxEngine.transform(theServletRequest.getInputStream().readAllBytes(), contentType.contains("xml") ? FhirFormat.XML : FhirFormat.JSON, map.getUrl());
    
//    if (r.isResource() && "Bundle".contentEquals(r.getType())) {
//      Property bundleType = r.getChildByName("type");
//      if (bundleType!=null && bundleType.getValues()!=null && "document".equals(bundleType.getValues().get(0).primitiveValue())) {
//        removeBundleEntryIds(r);
//      }
//    }
    theServletResponse.setContentType(responseContentType);
    theServletResponse.setCharacterEncoding("UTF-8");
    
    ServletOutputStream output = theServletResponse.getOutputStream();
    try {
      if (output != null) {
        if (output != null && responseContentType.equals(Constants.CT_FHIR_JSON_NEW))
          new org.hl7.fhir.r5.elementmodel.JsonParser(matchboxEngine.getContext()).compose(r, output, OutputStyle.PRETTY, null);
        else
          new org.hl7.fhir.r5.elementmodel.XmlParser(matchboxEngine.getContext()).compose(r, output, OutputStyle.PRETTY, null);
      }
    } catch(org.hl7.fhir.exceptions.FHIRException e) {
      log.error("Transform exception", e);
      output.write("Exception during Transform: ".getBytes());
      output.write(e.getMessage().getBytes());
    }
    theServletResponse.getOutputStream().close();
  }
  
  @Operation(name = "$convert", type = StructureMap.class, idempotent = true, returnParameters = {
      @OperationParam(name = "output", type = IBase.class, min = 1, max = 1) })
  public IBaseResource convert(@OperationParam(name = "input", min = 1, max = 1) final IBaseResource content,
      @OperationParam(name = "ig", min = 0, max = 1) final String ig,
      @OperationParam(name = "from", min = 0, max = 1) final String from,
      @OperationParam(name = "to", min = 0, max = 1) final String to,
      HttpServletRequest theRequest) {
    
    log.debug("$convert");
    return content;
  }

}

