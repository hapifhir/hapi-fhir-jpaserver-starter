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
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import ch.ahdis.matchbox.providers.StructureMapResourceProvider;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r5.model.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * StructureMapTransformProvider
 */
public class StructureMapTransformProvider extends StructureMapResourceProvider {

	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;

	private final FhirContext fhirR5Context = FhirContext.forR5Cached();

	@Override
	public MethodOutcome create(final HttpServletRequest theRequest,
										 @ResourceParam final IBaseResource theResource,
										 @ConditionalUrlParam final String theConditional,
										 final RequestDetails theRequestDetails) {
		this.createNarrative(theResource);
		return super.create(theRequest, theResource, theConditional, theRequestDetails);
	}

	@Override
	public MethodOutcome update(final HttpServletRequest theRequest,
										 final IDomainResource theResource,
										 final IIdType theId,
										 final String theConditional,
										 final RequestDetails theRequestDetails) {
		this.createNarrative(theResource);
		return super.update(theRequest, theResource, theId, theConditional, theRequestDetails);
	}

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructureMapTransformProvider.class);


	@Operation(name = "$transform", type = StructureMap.class, manualResponse = true, manualRequest = true)
	public void manualInputAndOutput(final HttpServletRequest theServletRequest,
												final HttpServletResponse theServletResponse) throws IOException {
		// Parse the request body, it is either a Parameters resource, or any resource
		final String body = new String(theServletRequest.getInputStream().readAllBytes()).trim();
		final IBaseResource bodyResource = this.parseBaseResource(body);

		/*
		 * Parse the operation inputs:
		 * - 'source' (URL query parameter, optional): the StructureMap.url to use
		 * - 'resource' (input Parameters, optional): the resource to transform
		 * - 'model' (input Parameters, optional): some StructureDefinitions used by the StructureMap
		 * - 'map' (input Parameters, optional): the StructureMap (as resource or textual representation) to use
		 * - body (optional): the resource to transform
		 *
		 * Either 'source' or 'map' MUST be provided; if 'source' is provided, then it must exist in the server.
		 */
		final String resource;
		@Nullable String source = null;
		final List<StructureDefinition> models = new ArrayList<>(2);
		@Nullable StructureMap map = null;

		if (bodyResource instanceof final Parameters inputParameters) {
			if (!inputParameters.hasParameter("resource")) {
				throw new InvalidRequestException("When the body is a Parameters resource, the parameter 'resource' MUST " +
																 "be present");
			}
			resource = inputParameters.getParameter("resource").getValueStringType().getValueNotNull();

			if (inputParameters.hasParameter("model")) {
				final IBaseResource model = this.parseBaseResource(inputParameters.getParameter("model").getValueStringType().getValueNotNull());
				if (model instanceof final StructureDefinition structureDefinition) {
					models.add(structureDefinition);
				} else if (model instanceof final Bundle bundle) {
					for (final var entry : bundle.getEntry()) {
						if (entry.getResource() instanceof final StructureDefinition structureDefinition) {
							models.add(structureDefinition);
						}
					}
				} else {
					throw new InvalidRequestException(
						"The parameter 'model' must be a StructureDefinition or Bundle resource");
				}
			}

			if (inputParameters.hasParameter("map")) {
				try {
					map = this.parseResource(inputParameters.getParameter("map").getValueStringType().getValueNotNull(),
													 StructureMap.class);
				} catch (final Exception e) {
					final var tempEngine = this.matchboxEngineSupport.getMatchboxEngine(null, cliContext, true, false);
					map = tempEngine.parseMapR5(inputParameters.getParameter("map").getValueStringType().getValueNotNull());
				}
				if (map == null) {
					throw new InvalidRequestException("The parameter 'map' must be a StructureMap resource");
				}
			}
		} else {
			resource = body;
		}

		final Map<String, String[]> requestParams = theServletRequest.getParameterMap();
		if (requestParams.containsKey("source") && requestParams.get("source").length > 0) {
			if (requestParams.get("source").length > 1) {
				throw new InvalidRequestException("Only one 'source' parameter is allowed");
			}
			source = requestParams.get("source")[0];
		}

		if (source == null && map == null) {
			throw new InvalidRequestException("Either 'source' or 'map' parameter must be provided");
		}
		if (source != null && map != null) {
			throw new InvalidRequestException("Only one of 'source' or 'map' parameters can be provided");
		}

		// Initialize the Matchbox engine that will perform the transformation
		final CliContext cliContext = new CliContext(this.cliContext);
		final MatchboxEngine matchboxEngine;
		if (source != null) {
			matchboxEngine = this.matchboxEngineSupport.getMatchboxEngine(source, cliContext, true, false);
		} else {
			matchboxEngine = this.matchboxEngineSupport.getMatchboxEngine(null, cliContext, true, false);
		}
		if (matchboxEngine == null) {
			throw new UnprocessableEntityException("The Matchbox engine could not be initialized");
		}

		if (source != null) {
			map = matchboxEngine.getContext().fetchResource(StructureMap.class, source);
			if (map == null) {
				throw new UnprocessableEntityException("Map not available with canonical url " + source);
			}
		} else {
			// Store the map, as it is provided as a parameter
			matchboxEngine.getContext().cacheResource(map);
		}

		// Store the models, as they're provided as parameters
		for (final var model : models) {
			matchboxEngine.getContext().cacheResource(model);
		}

		try {
			final var responseContentType = this.parseRequestedResponseType(theServletRequest);
			theServletResponse.setContentType(responseContentType);

			final var transformed = matchboxEngine.transform(resource,
																			 !resource.startsWith("<"),
																			 map.getUrl(),
																			 responseContentType.contains("json"));
			theServletResponse.getOutputStream().print(transformed);
		} finally {
			// Let's clean up the engine of the models and map we've cached, if any
			// This allows re-use of the engine for other transformations, decreasing the startup time
			for (final var model : models) {
				matchboxEngine.getContext().dropResource(model);
			}
			if (source == null) {
				matchboxEngine.getContext().dropResource(map);
			}
		}
	}

	@Operation(name = "$convert", type = StructureMap.class, idempotent = true, returnParameters = {
		@OperationParam(name = "output", type = IBase.class, min = 1, max = 1)})
	public IBaseResource convert(@OperationParam(name = "input", min = 1, max = 1) final IBaseResource content,
										  @OperationParam(name = "ig", min = 0, max = 1) final String ig,
										  @OperationParam(name = "from", min = 0, max = 1) final String from,
										  @OperationParam(name = "to", min = 0, max = 1) final String to,
										  final HttpServletRequest theRequest) {
		// HAPI has already converted the textual map to a StructureMap resource if the Content-Type was right
		// (text/fhir-mapping)
		return content;
	}

	private IBaseResource parseBaseResource(String content) {
		content = content.trim();
		if (content.startsWith("<")) {
			return this.fhirR5Context.newXmlParser().parseResource(content);
		} else {
			return this.fhirR5Context.newJsonParser().parseResource(content);
		}
	}

	private <T extends IBaseResource> T parseResource(String content, final Class<T> theResourceType) {
		content = content.trim();
		if (content.startsWith("<")) {
			return this.fhirR5Context.newXmlParser().parseResource(theResourceType, content);
		} else {
			return this.fhirR5Context.newJsonParser().parseResource(theResourceType, content);
		}
	}

	private void createNarrative(final IBaseResource theResource) {
		final StructureMap map = (StructureMap) this.getCanonical(theResource);
		if (!map.hasText()) {
			final String render = StructureMapUtilities.render(map);
			switch (theResource) {
				case final org.hl7.fhir.r4.model.StructureMap r4 -> {
					r4.getText().setStatus(NarrativeStatus.GENERATED);
					r4.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
					r4.getText().getDiv().addTag("pre").addText(render);
				}
				case final org.hl7.fhir.r4b.model.StructureMap r4b -> {
					r4b.getText().setStatus(org.hl7.fhir.r4b.model.Narrative.NarrativeStatus.GENERATED);
					r4b.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
					r4b.getText().getDiv().addTag("pre").addText(render);
				}
				case final StructureMap r5 -> {
					r5.getText().setStatus(org.hl7.fhir.r5.model.Narrative.NarrativeStatus.GENERATED);
					r5.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
					r5.getText().getDiv().addTag("pre").addText(render);
				}
				case null -> throw new IllegalStateException("StructureMapTransformProvider: theResource is null");
				default -> throw new MatchboxUnsupportedFhirVersionException("StructureMapTransformProvider",
																								 theResource.getStructureFhirVersionEnum());
			}
		}
	}

	private String parseRequestedResponseType(final HttpServletRequest theServletRequest) {
		final Set<String> highestRankedAcceptValues =
			RestfulServerUtils.parseAcceptHeaderAndReturnHighestRankedOptions(theServletRequest);
		String responseContentType = ca.uhn.fhir.rest.api.Constants.CT_FHIR_XML_NEW;
		if (highestRankedAcceptValues.contains(ca.uhn.fhir.rest.api.Constants.CT_FHIR_JSON_NEW)) {
			responseContentType = ca.uhn.fhir.rest.api.Constants.CT_FHIR_JSON_NEW;
		}
		// patch for fhir-kit-client https://github.com/Vermonster/fhir-kit-client/pull/143
		if (highestRankedAcceptValues.contains(ca.uhn.fhir.rest.api.Constants.CT_FHIR_JSON)) {
			responseContentType = Constants.CT_FHIR_JSON_NEW;
		}
		return responseContentType;
	}
}
