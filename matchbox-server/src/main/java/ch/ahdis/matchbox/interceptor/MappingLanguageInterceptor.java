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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r4.formats.IParser;
import org.hl7.fhir.r4.formats.ParserFactory;
import org.hl7.fhir.r4.hapi.ctx.FhirR4;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.context.SimpleWorkerContext.SimpleWorkerContextBuilder;
import org.hl7.fhir.r5.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r5.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r5.utils.structuremap.ITransformerServices;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ch.ahdis.matchbox.MatchboxEngineSupport;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.spring.boot.autoconfigure.MutableHttpServletRequest;

/**
 * MappingLanguagerInterceptor converts a FHIR Mapping Language texture
 * representation in a StructureMap resource
 * 
 * POST {{host}}/$convert HTTP/1.1 Accept: application/fhir+json Content-Type:
 * application/fhir+xml;fhirVersion=3.0
 *
 * 
 * inspired and credits to hapi-fhir @see
 * ca.uhn.hapi.converters.server.VersionedApiConverterInterceptor
 *
 */
public class MappingLanguageInterceptor extends InterceptorAdapter {

	protected MatchboxEngineSupport matchboxEngineSupport;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MappingLanguageInterceptor.class);

	public MappingLanguageInterceptor(MatchboxEngineSupport matchboxEngineSupport) {
		super();
		this.matchboxEngineSupport = matchboxEngineSupport;
	}

	private FHIRVersion extractFhirVersion(String header) {
		if (header == null) {
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
			return FHIRVersion.fromCode(wantVersionString);
		}
		return FHIRVersion.fromCode(matchboxEngineSupport.getClientContext().getFhirVersion());
	}

	public StructureMap parseMap(String content) throws FHIRException {
		MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine("default", null, true, false);
		return matchboxEngine.parseMapR5(content);
	}

	@Override
	public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
			HttpServletResponse theResponse) throws AuthenticationException {

		String contentType = defaultString(theRequest.getHeader(Constants.HEADER_CONTENT_TYPE));
		if (contentType.startsWith("text/fhir-mapping")) {
			log.debug("processing text/fhir mapping - converting to json");
			FHIRVersion fhirVersion = this.extractFhirVersion(contentType);
			((MutableHttpServletRequest) theRequest).putHeader(Constants.HEADER_CONTENT_TYPE, "application/fhir+json;fhirVersion="+fhirVersion.getDisplay());

			
			StructureMap structureMap = parseMap(new String(theRequestDetails.loadRequestContents()));
			if ("PUT".equals(theRequest.getMethod())) {
				IIdType id = theRequestDetails.getId();
				if (id != null) {
					structureMap.setId(id.getIdPart());
				}
			}

			String json = null;
			try {
				switch(fhirVersion) {
					case _4_0_1:
						org.hl7.fhir.r4.model.StructureMap sm4 =  (org.hl7.fhir.r4.model.StructureMap) VersionConvertorFactory_40_50.convertResource(structureMap);
						json = new org.hl7.fhir.r4.formats.JsonParser().composeString(sm4);				
						break;
					case _5_0_0:
						json = new org.hl7.fhir.r5.formats.JsonParser().composeString(structureMap);				
						break;
					default:
						log.error("FHIR Version not supported "+fhirVersion.getDisplay());
				}
				theRequestDetails.setRequestContents(json.getBytes());
			} catch (IOException e) {
				log.error("Exception while conversion to json", e);
			}
		}
		return true;
	}

}
