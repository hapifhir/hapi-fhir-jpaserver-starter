package ch.ahdis.matchbox.mappinglanguage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletOutputStream;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.ByteProvider;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.MatchboxEngineSupport;
import ch.ahdis.matchbox.StructureMapResourceProvider;
import ch.ahdis.matchbox.engine.MatchboxEngine;

/**
 * StructureMapTransformProvider

 *
 */
public class StructureMapTransformProviderR4 extends StructureMapResourceProvider {
	
	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;
  
  public void createNarrative(IBaseResource theResource) {
    org.hl7.fhir.r5.model.StructureMap map = (org.hl7.fhir.r5.model.StructureMap) this.getCanonical(theResource);
    if (!map.hasText()) {
      String render = StructureMapUtilities.render(map);
      if (classR4.isInstance(theResource)) {
        org.hl7.fhir.r4.model.StructureMap  r4 = classR4.cast(theResource);
        r4.getText().setStatus(NarrativeStatus.GENERATED);
        r4.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
        r4.getText().getDiv().addTag("pre").addText(render);
        return ; 
        }
      if (classR4B.isInstance(theResource)) {
        org.hl7.fhir.r4b.model.StructureMap  r4b = classR4B.cast(theResource);
        r4b.getText().setStatus(org.hl7.fhir.r4b.model.Narrative.NarrativeStatus.GENERATED);
        r4b.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
        r4b.getText().getDiv().addTag("pre").addText(render);
      }
      if (classR5.isInstance(theResource)) {
        org.hl7.fhir.r5.model.StructureMap  r5 = classR5.cast(theResource);
        r5.getText().setStatus(org.hl7.fhir.r5.model.Narrative.NarrativeStatus.GENERATED);
        r5.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
        r5.getText().getDiv().addTag("pre").addText(render);
      }
    }
  }
  
  @Override
	public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam IBaseResource theResource, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
    createNarrative(theResource);
    return super.create(theRequest, theResource, theConditional, theRequestDetails);
  }

  @Override
  public MethodOutcome update(HttpServletRequest theRequest, IDomainResource theResource, IIdType theId,
    String theConditional, RequestDetails theRequestDetails) {
    createNarrative(theResource);
    return super.update(theRequest, theResource, theId, theConditional, theRequestDetails);
  }

  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructureMapTransformProviderR4.class);

  @Operation(name = "$transform", type = StructureMap.class, manualResponse = true, manualRequest = true)
  public void manualInputAndOutput(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
      throws IOException {
    Map<String, String[]> requestParams = theServletRequest.getParameterMap();
    String[] source = requestParams.get("source");
    CliContext cliContext = new CliContext(this.cliContext);
    if (source != null && source.length > 0) {
      MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(source[0], cliContext, true, false);
      if (matchboxEngine == null) {
        throw new UnprocessableEntityException("matchbox engine could not be initialized with canonical url "+source[0]);
      }
      org.hl7.fhir.r5.model.StructureMap map  = matchboxEngine.getContext().fetchResource(org.hl7.fhir.r5.model.StructureMap.class, source[0]);
      if (map == null) {
          throw new UnprocessableEntityException("Map not available with canonical url "+source[0]);
      }
      for (StructureMapStructureComponent component  : map.getStructure()) {
        if (component.getUrl() != null && matchboxEngine.getStructureDefinitionR5(component.getUrl()) == null) {
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

    org.hl7.fhir.r5.elementmodel.Element r = matchboxEngine.transform(ByteProvider.forBytes(theServletRequest.getInputStream().readAllBytes()), contentType.contains("xml") ? FhirFormat.XML : FhirFormat.JSON, map.getUrl());
    
    theServletResponse.setContentType(responseContentType);
    theServletResponse.setCharacterEncoding("UTF-8");
    
    ServletOutputStream output = theServletResponse.getOutputStream();
    try {
      if (output != null) {
        if (responseContentType.equals(Constants.CT_FHIR_JSON_NEW))
          new org.hl7.fhir.r5.elementmodel.JsonParser(matchboxEngine.getContext()).compose(r, output, OutputStyle.PRETTY, null);
        else
          new org.hl7.fhir.r5.elementmodel.XmlParser(matchboxEngine.getContext()).compose(r, output, OutputStyle.PRETTY, null);
      }
    } catch(org.hl7.fhir.exceptions.FHIRException e) {
      log.error("Transform exception", e);
      output.write("Exception during Transform".getBytes());
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
