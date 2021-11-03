package ch.ahdis.matchbox.interceptor;

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

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r4.formats.IParser;
import org.hl7.fhir.r4.formats.ParserFactory;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r5.utils.structuremap.ITransformerServices;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ch.ahdis.matchbox.spring.boot.autoconfigure.MutableHttpServletRequest;

/**
 * MappingLanguagerInterceptor converts a FHIR Mapping Language texture representation in a StructureMap resource
 * 
 * POST {{host}}/$convert HTTP/1.1
 * Accept: application/fhir+json
 * Content-Type: application/fhir+xml;fhirVersion=3.0
 *
 * TODO: No testcases written yet
 *
 * TODO: different versions for Accept-Header and Content-Type are not allowed according to the FHIR specification, 
 * need to raise a gforge for that: see https://www.hl7.org/fhir/http.html#version-parameter
 * 
 * inspired and credits to hapi-fhir @see ca.uhn.hapi.converters.server.VersionedApiConverterInterceptor
 *
 */
public class MappingLanguageInterceptor extends InterceptorAdapter implements ITransformerServices {
  
  


  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MappingLanguageInterceptor.class);
  
	private FhirVersionEnum serverVersion;
	private static Map<FhirVersionEnum, FhirContext> myFhirContextMap = Collections.synchronizedMap(new HashMap<FhirVersionEnum, FhirContext>());
	
	public MappingLanguageInterceptor() {
		super();
	}
	
	private FhirVersionEnum extractFhirVersion(String header) {
		if (header==null) {
			return null;
		}
		StringTokenizer tok = new StringTokenizer(header, ";");
		String wantVersionString = null;
		while (tok.hasMoreTokens()) {
			String next = tok.nextToken().trim();
			if (next.startsWith("fhirVersion=")) {
				wantVersionString = next.substring("fhirVersion=".length()).trim();
				break;
			}
		}
		if (isNotBlank(wantVersionString)) {
			return FhirVersionEnum.forVersionString(wantVersionString);
		}
		return null;
	}
	
	public static FhirContext getContextForVersion(FhirContext theContext, FhirVersionEnum theForVersion) {
		FhirContext context = theContext;
		if (context.getVersion().getVersion() != theForVersion) {
			context = myFhirContextMap.get(theForVersion);
			if (context == null) {
				context = theForVersion.newContext();
				myFhirContextMap.put(theForVersion, context);
			}
		}
		return context;
	}
	
	public FhirContext getContextForVersion(RequestDetails theRequestDetails, FhirVersionEnum theForVersion) {
		return getContextForVersion(theRequestDetails.getServer().getFhirContext(), theForVersion);
	}
	
  public StructureMap parseMap(String content) throws FHIRException {
    SimpleWorkerContext contextR5 = null;
    try {
      contextR5 = new SimpleWorkerContext();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    StructureMapUtilities smu5 = new StructureMapUtilities(contextR5, this);
    
    org.hl7.fhir.r5.model.StructureMap mapR5 = smu5.parse(content, "map");
    mapR5.getText().setStatus(NarrativeStatus.GENERATED);
    mapR5.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
    String render = StructureMapUtilities.render(mapR5);
    mapR5.getText().getDiv().addTag("pre").addText(render);
    
    StructureMap map =  (StructureMap) VersionConvertorFactory_40_50.convertResource(mapR5);
    return map;
  }

	@Override
	public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
			HttpServletResponse theResponse) throws AuthenticationException {

    String contentType = defaultString(theRequest.getHeader(Constants.HEADER_CONTENT_TYPE));		
		if (contentType.startsWith("text/fhir-mapping")) {
	    log.debug("processing text/fhir mapping - converting to json");	    
	    FhirVersionEnum version = extractFhirVersion(contentType);
      ((MutableHttpServletRequest) theRequest).putHeader(Constants.HEADER_CONTENT_TYPE, "application/fhir+json");

      StructureMap structureMap = parseMap(new String(theRequestDetails.loadRequestContents()));
      if ("PUT".equals(theRequest.getMethod())) {
        IIdType id = theRequestDetails.getId();
        structureMap.setId(id.getIdPart());
      }
      
      IParser parserConverted = ParserFactory.parser(FhirFormat.JSON);    
      try {
        log.debug(parserConverted.composeString(structureMap));
        theRequestDetails.setRequestContents(
            parserConverted.composeString(structureMap).getBytes());
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
		  return true;
    }		
		return true;
	}

  @Override
  public void log(String message) {
  }

  @Override
  public org.hl7.fhir.r5.model.Base createType(Object appInfo, String name) throws FHIRException {
	return null;
  }

  @Override
  public org.hl7.fhir.r5.model.Base createResource(Object appInfo, org.hl7.fhir.r5.model.Base res,
	  boolean atRootofTransform) {
	return null;
  }

  @Override
  public org.hl7.fhir.r5.model.Coding translate(Object appInfo, org.hl7.fhir.r5.model.Coding source,
	  String conceptMapUrl) throws FHIRException {
	return null;
  }

  @Override
  public org.hl7.fhir.r5.model.Base resolveReference(Object appContext, String url) throws FHIRException {
	return null;
  }

  @Override
  public List<org.hl7.fhir.r5.model.Base> performSearch(Object appContext, String url) throws FHIRException {
	return null;
  }



}
