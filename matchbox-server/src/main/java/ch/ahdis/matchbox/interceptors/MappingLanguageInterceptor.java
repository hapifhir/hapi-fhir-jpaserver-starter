package ch.ahdis.matchbox.interceptors;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.StringTokenizer;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.UnclassifiedServerFailureException;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import ch.ahdis.matchbox.mappinglanguage.MatchboxStructureMapUtilities;
import ch.ahdis.matchbox.mappinglanguage.TransformSupportServices;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.model.Narrative;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import ch.ahdis.matchbox.spring.boot.autoconfigure.MutableHttpServletRequest;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import javax.annotation.Nullable;

/**
 * MappingLanguageInterceptor converts a FHIR Mapping Language texture
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

	protected final MatchboxEngineSupport matchboxEngineSupport;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MappingLanguageInterceptor.class);

	public MappingLanguageInterceptor(final MatchboxEngineSupport matchboxEngineSupport) {
		super();
		this.matchboxEngineSupport = matchboxEngineSupport;
	}

	@Override
	public boolean incomingRequestPostProcessed(final RequestDetails theRequestDetails,
															  final HttpServletRequest theRequest,
															  final HttpServletResponse theResponse) throws AuthenticationException {

		final String contentType = defaultString(theRequest.getHeader(Constants.HEADER_CONTENT_TYPE));
		if (contentType.startsWith("text/fhir-mapping")) {
			log.debug("processing text/fhir mapping - converting to json");

			FhirVersionEnum requestedFhirVersion = this.extractFhirVersion(contentType);
			final FhirVersionEnum mainEngineFhirVersion =
				FhirVersionEnum.forVersionString(this.matchboxEngineSupport.getClientContext().getFhirVersion());

			if (requestedFhirVersion != null && this.matchboxEngineSupport.getClientContext().getOnlyOneEngine()) {
				// If the onlyOneEngine mode is enabled, check that it's the same FHIR version
				if (mainEngineFhirVersion != requestedFhirVersion) {
					throw new UnclassifiedServerFailureException(415, "The requested FHIR version (" + requestedFhirVersion +
						") does not match the engine's FHIR version (" + mainEngineFhirVersion + ")");
				}
			} else if (requestedFhirVersion == null) {
				// If the requested FHIR version is not specified, use the engine's FHIR version
				requestedFhirVersion = mainEngineFhirVersion;
			}

			// Update the MIME type in the request
			((MutableHttpServletRequest) theRequest).putHeader(Constants.HEADER_CONTENT_TYPE, "application/fhir+json;" +
				"fhirVersion="+requestedFhirVersion.getFhirVersionString());

			final var mapText = new String(theRequestDetails.loadRequestContents(), StandardCharsets.UTF_8);

			// Force the StructureMap ID to a specific value for PUT requests
			String structureMapId = null;
			if ("PUT".equals(theRequest.getMethod())) {
				final IIdType id = theRequestDetails.getId();
				if (id != null) {
					structureMapId = id.getIdPart();
				}
			}

			final String jsonStructureMap;
			try {
				jsonStructureMap = this.compileAndSerializeMap(mapText, requestedFhirVersion, structureMapId);
			} catch (final IOException e) {
				// Nothing we can do here, propagate the exception (error 500)
				throw new InternalErrorException("Failed to convert StructureMap to JSON", e);
			}
			theRequestDetails.setRequestContents(jsonStructureMap.getBytes(StandardCharsets.UTF_8));
		}
		return true;
	}

	@Nullable
	private FhirVersionEnum extractFhirVersion(final String header) {
		if (header != null) {
			final var tokenizer = new StringTokenizer(header, ";");
			String wantVersionString = null;
			while (tokenizer.hasMoreTokens()) {
				String next = tokenizer.nextToken().trim();
				if (next.startsWith("fhirVersion=")) {
					wantVersionString = next.substring("fhirVersion=".length()).trim();
					break;
				}
			}
			if (isNotBlank(wantVersionString)) {
				return FhirVersionEnum.forVersionString(wantVersionString);
			}
		}
		// Returns null if no FHIR version can be extracted from the header
		return null;
	}

	/**
	 * Compiles the given FHIR Mapping Language text and serializes the resulting StructureMap resource. The
	 * compilation always happens in the FHIR R5 version, then the resulting resource is converted to the requested FHIR
	 * version.
	 *
	 * @param mapText The FHIR map as text.
	 * @param fhirVersion The FHIR version to use for the serialization.
	 * @param structureMapId The id to set on the StructureMap resource, or null if no id should be set.
	 * @return
	 * @throws IOException
	 */
	public String compileAndSerializeMap(final String mapText,
													 final FhirVersionEnum fhirVersion,
													 final @Nullable String structureMapId) throws IOException {
		StructureMap mapR5 = matchboxEngineSupport.getMatchboxEngine("default", matchboxEngineSupport.getClientContext(), true, false).parseMapR5(mapText);
		if (structureMapId != null) {
			mapR5.setId(structureMapId);
		}
		return switch (fhirVersion) {
			case R4:
				org.hl7.fhir.r4.model.StructureMap sm4 = (org.hl7.fhir.r4.model.StructureMap) VersionConvertorFactory_40_50.convertResource(
					mapR5);
				yield new org.hl7.fhir.r4.formats.JsonParser().composeString(sm4);
			case R4B:
				org.hl7.fhir.r4b.model.StructureMap sm4b = (org.hl7.fhir.r4b.model.StructureMap) VersionConvertorFactory_43_50.convertResource(
					mapR5);
				yield new org.hl7.fhir.r4b.formats.JsonParser().composeString(sm4b);
			case R5:
				yield new org.hl7.fhir.r5.formats.JsonParser().composeString(mapR5);
			default:
				throw new MatchboxUnsupportedFhirVersionException("Unsupported FHIR version: " + fhirVersion,
																				  fhirVersion);
		};
	}
}
