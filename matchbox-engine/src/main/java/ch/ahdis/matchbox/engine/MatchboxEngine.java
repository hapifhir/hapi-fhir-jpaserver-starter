package ch.ahdis.matchbox.engine;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.common.returnsreceiver.qual.This;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r5.conformance.ProfileUtilities;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.ExpressionNode;
import org.hl7.fhir.r5.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.validation.IgLoader;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.instance.InstanceValidator;
import org.hl7.fhir.validation.instance.utils.ValidatorHostContext;

import ch.ahdis.matchbox.engine.cli.VersionUtil;
import ch.ahdis.matchbox.mappinglanguage.MatchboxStructureMapUtilities;
import ch.ahdis.matchbox.mappinglanguage.TransformSupportServices;

/**
 * Base Engine providing functionality on top of the ValdiationEngine
 * 
 * @author oliveregger
 *
 */
public class MatchboxEngine extends ValidationEngine {

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxEngine.class);

	public MatchboxEngine(ValidationEngine other) throws FHIRException, IOException {
		super(other);

	}

	/**
	 * Builder class to instantiate a MappingEngine
	 * 
	 * @author oliveregger, ahdis ag
	 *
	 */
	public static class MatchboxEngineBuilder extends ValidationEngineBuilder {

		/**
		 * Creates an empty builder instance
		 */
		public MatchboxEngineBuilder() {
		}

		/**
		 * Returns a FHIR R4 engine configured with hl7 terminology
		 * 
		 * @return
		 * @throws FHIRException
		 * @throws IOException
		 * @throws URISyntaxException
		 */
		public MatchboxEngine getEngineR4() throws FHIRException, IOException, URISyntaxException {
			log.info("Initializing Matchbox Engine (FHIR R4 with terminology provided in classpath)");
			log.info(VersionUtil.getPoweredBy());
			MatchboxEngine engine = new MatchboxEngine(super.fromNothing());
			engine.setVersion("4.0.1");
			IgLoader igLoader = new IgLoaderFromClassPath(engine.getPcm(), engine.getContext(), engine.getVersion(),
					engine.isDebug());
			engine.setIgLoader(igLoader);
			igLoader.loadIg(engine.getIgs(), null, "/hl7.fhir.r4.core.tgz", false);
			igLoader.loadIg(engine.getIgs(), null, "/hl7.terminology#5.0.0.tgz", false);
			engine.getContext().setCanRunWithoutTerminology(true);
			engine.getContext().setNoTerminologyServer(true);
			engine.getContext().setPackageTracker(engine);
			return engine;
		}

		/**
		 * Returns empty engine
		 * 
		 * @return
		 * @throws FHIRException
		 * @throws IOException
		 * @throws URISyntaxException
		 */
		public MatchboxEngine getEngine() throws FHIRException, IOException, URISyntaxException {
			log.info("Initializing Matchbox Engine");
			log.info(VersionUtil.getPoweredBy());
			MatchboxEngine engine = new MatchboxEngine(super.fromNothing());
			engine.setVersion("4.0.1");
			engine.getContext().setCanRunWithoutTerminology(true);
			engine.getContext().setNoTerminologyServer(true);
			engine.getContext().setPackageTracker(engine);
			return engine;
		}

	}

	/**
	 * Transforms an input with the map identified by the uri to the output defined
	 * by the map
	 * 
	 * @param input     content to be transformed
	 * @param inputJson true if input is in json (if false xml is expected)
	 * @param mapUri    canonical url of StructureMap
	 * @return FHIR resource
	 * @throws FHIRException FHIR Exception
	 * @throws IOException   IO Exception
	 */
	public Resource transformToFhir(String input, boolean inputJson, String mapUri) throws FHIRException, IOException {
		String transformedXml = transform(input, inputJson, mapUri, false);
		return new org.hl7.fhir.r4.formats.XmlParser().parse(transformedXml);
	}

	/**
	 * Transforms an input with the map identified by the uri to the output defined
	 * by the map
	 * 
	 * @param input      source in UTF-8 format
	 * @param inputJson  if input is in json (or xml)
	 * @param mapUri     map to use for transformation
	 * @param outputJson if output is formatted as json (or xml)
	 * @return transformed input as string
	 * @throws FHIRException FHIR Exception
	 * @throws IOException   IO Exception
	 */
	public String transform(String input, boolean inputJson, String mapUri, boolean outputJson)
			throws FHIRException, IOException {
		log.info("Start transform: " + mapUri);
		Element transformed = transform(input.getBytes("UTF-8"), (inputJson ? FhirFormat.JSON : FhirFormat.XML),
				mapUri);
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		if (outputJson)
			new org.hl7.fhir.r5.elementmodel.JsonParser(getContext()).compose(transformed, boas,
					IParser.OutputStyle.PRETTY,
					null);
		else
			new org.hl7.fhir.r5.elementmodel.XmlParser(getContext()).compose(transformed, boas,
					IParser.OutputStyle.PRETTY,
					null);
		String result = new String(boas.toByteArray());
		boas.close();
		log.info("Transform finished: " + mapUri);
		return result;
	}

	/**
	 * Adapted transform operation from Validation Engine to use patched
	 * MatchboxStructureMapUtilities
	 */
	@Override
	public org.hl7.fhir.r5.elementmodel.Element transform(byte[] source, FhirFormat cntType, String mapUri)
			throws FHIRException, IOException {
		SimpleWorkerContext context = this.getContext();
		org.hl7.fhir.r5.elementmodel.Element src = Manager.parseSingle(context, new ByteArrayInputStream(source),
				cntType);
		return transform(src, mapUri);
	}

	/**
	 * Adapted transform operation from Validation Engine to use patched
	 * MatchboxStructureMapUtilities
	 * @param src
	 * @param mapUri
	 * @return
	 * @throws FHIRException
	 * @throws IOException
	 */
	public org.hl7.fhir.r5.elementmodel.Element transform(org.hl7.fhir.r5.elementmodel.Element src,  String mapUri)
			throws FHIRException, IOException {
		SimpleWorkerContext context = this.getContext();
		List<Base> outputs = new ArrayList<>();
		StructureMapUtilities scu = new MatchboxStructureMapUtilities(context,
				new TransformSupportServices(context, outputs), this);
		StructureMap map = context.fetchResource(StructureMap.class, mapUri);
		if (map == null) {
			log.error("Unable to find map " + mapUri + " (Known Maps = " + context.listMapUrls() + ")");
			throw new Error("Unable to find map " + mapUri + " (Known Maps = " + context.listMapUrls() + ")");
		}
		log.info("Using map " + map.getUrl() + (map.getVersion()!=null ? "|" + map.getVersion() + " " : "" )
				+ (map.getDateElement() != null && !map.getDateElement().isEmpty()  ? "(" + map.getDateElement().asStringValue() + ")" : ""));
		org.hl7.fhir.r5.elementmodel.Element resource = getTargetResourceFromStructureMap(map);
		scu.transform(null, src, map, resource);
		resource.populatePaths(null);
		return resource;
	}

	private org.hl7.fhir.r5.elementmodel.Element getTargetResourceFromStructureMap(StructureMap map) {
		String targetTypeUrl = null;
		SimpleWorkerContext context = this.getContext();
		for (StructureMap.StructureMapStructureComponent component : map.getStructure()) {
			if (component.getMode() == StructureMap.StructureMapModelMode.TARGET) {
				targetTypeUrl = component.getUrl();
				break;
			}
		}

		if (targetTypeUrl == null) {
			log.error("Unable to determine resource URL for target type");
			throw new FHIRException("Unable to determine resource URL for target type");
		}

		StructureDefinition structureDefinition = null;
		for (StructureDefinition sd : context.fetchResourcesByType(StructureDefinition.class)) {
			if (sd.getUrl().equalsIgnoreCase(targetTypeUrl)) {
				structureDefinition = sd;
				break;
			}
		}

		if (structureDefinition == null) {
			log.error("Unable to find StructureDefinition for target type ('" + targetTypeUrl + "')");
			throw new FHIRException("Unable to find StructureDefinition for target type ('" + targetTypeUrl + "')");
		}

		return Manager.build(getContext(), structureDefinition);
	}

	/**
	 * adds a canonical resource to the loaded packages, please note that it will
	 * replace a resource with the same canonical url
	 * 
	 * @param stream canonical resource to add
	 * @throws FHIRException FHIR Exception
	 */
	public void addCanonicalResource(InputStream stream) throws FHIRException {
		getContext().loadFromFile(stream, "", null);
	}

	/**
	 * adds a canonical resource to the loaded packages, please note that it will
	 * replace a resource with the same canonical url
	 * 
	 * @param resource canonical resource to add
	 * @throws FHIRException FHIR Exception
	 */
	public void addCanonicalResource(Resource resource) throws FHIRException {
		org.hl7.fhir.r5.model.Resource r5 = VersionConvertorFactory_40_50.convertResource(resource);
		getContext().cacheResource(r5);
	}

	/**
	 * validates a FHIR resources and provides OperationOutcome as output
	 * 
	 * @param stream     resource to validate as input stream
	 * @param format     format of resource
	 * @param profileUrl profile to validate against
	 * @return result as Operation Outcome
	 * @throws FHIRException     FHIR Exception
	 * @throws IOException       IO Exception
	 * @throws EOperationOutcome Exception OperationOutcome
	 */
	public OperationOutcome validate(InputStream stream, FhirFormat format, String profileUrl)
			throws FHIRException, IOException, EOperationOutcome {
		List<String> profiles = null;
		if (profileUrl != null) {
			profiles = new ArrayList<String>();
			profiles.add(profileUrl);
			List<StructureDefinition> sds = asSdList(profiles);
			for (StructureDefinition sd : sds) {
				log.info("Using profile for validation " + sd.getUrl() + "|" + sd.getVersion() + " "
						+ (sd.getDateElement() != null ? "(" + sd.getDateElement().asStringValue() + ")" : ""));
			}
		} else {
			log.info("validation on resource, no profile used");
		}

		org.hl7.fhir.r5.model.OperationOutcome outcome = super.validate(format, stream, profiles);
		return (OperationOutcome) (VersionConvertorFactory_40_50.convertResource(outcome));
	}

	/**
	 * validates a FHIR resources and provides OperationOutcome as output
	 * 
	 * @param resource   FHIR R4 resource
	 * @param profileUrl profile to validate against
	 * @return
	 * @return result as Operation Outcome
	 * @throws FHIRException     FHIR Exception
	 * @throws IOException       IO Exception
	 * @throws EOperationOutcome Exception OperationOutcome
	 */
	public OperationOutcome validate(Resource resource, String profileUrl)
			throws FHIRException, IOException, EOperationOutcome {
		String result = new org.hl7.fhir.r4.formats.JsonParser().composeString(resource);
		return validate(new ByteArrayInputStream(result.getBytes("UTF-8")), FhirFormat.JSON, profileUrl);
	}

	// testing entry point
	public List<ValidationMessage> validate(FhirFormat format, InputStream stream, String profileUrl)
			throws FHIRException, IOException, EOperationOutcome {
		List<String> profiles = null;
		if (profileUrl != null) {
			profiles = new ArrayList<String>();
			profiles.add(profileUrl);
			List<StructureDefinition> sds = asSdList(profiles);
			for (StructureDefinition sd : sds) {
				log.info("Using profile for validation " + sd.getUrl() + "|" + sd.getVersion() + " "
						+ (sd.getDateElement() != null ? "(" + sd.getDateElement().asStringValue() + ")" : ""));
			}
		}
		List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
		InstanceValidator validator = getValidator(format);
		validator.validate(null, messages, stream, format, asSdList(profiles));
		return messages;
	}

	/**
	 * internal use, checks if all profiles an be resolved
	 * 
	 * @param profiles canonical url of profiles (StructureDefinition)
	 * @return StructureDefintions
	 * @throws Error if profile cannot be resolved
	 */
	public List<StructureDefinition> asSdList(List<String> profiles) throws Error {
		List<StructureDefinition> list = new ArrayList<>();
		if (profiles != null) {
			for (String p : profiles) {
				StructureDefinition sd = this.getContext().fetchResource(StructureDefinition.class, p);
				if (sd == null) {
					log.error("Unable to resolve profile " + p);
					throw new Error("Unable to resolve profile " + p);
				}
				list.add(sd);
			}
		}
		return list;
	}

	/**
	 * Get the corresponding Struc
	 * 
	 * @param profile
	 * @return
	 */
	public org.hl7.fhir.r4.model.StructureDefinition getStructureDefinition(String profile) {
		ArrayList<String> profiles = new ArrayList<String>();
		profiles.add(profile);
		try {
			List<StructureDefinition> sds = asSdList(profiles);
			if (sds.size() > 0) {
				if (sds.size() > 1) {
					log.debug("Multiple StructureDefinitions for profile " + profile);
				}
				return (org.hl7.fhir.r4.model.StructureDefinition) VersionConvertorFactory_40_50
						.convertResource(sds.get(0));
			}
		} catch (Error e) {
		}
		return null;
	}

	/**
	 * Returns a canonical resource defined by its url
	 * 
	 * @param canonical
	 * @return
	 */
	public org.hl7.fhir.r4.model.Resource getCanonicalResource(String canonical) {
		org.hl7.fhir.r5.model.Resource fetched = this.getContext().fetchResource(null, canonical);
		// allResourcesById is not package aware (???) so we need to fetch it again
		if (fetched!=null) {
			org.hl7.fhir.r5.model.Resource fetched2  = this.getContext().fetchResource(fetched.getClass(), canonical);
			if (fetched2 != null) {
				return VersionConvertorFactory_40_50.convertResource(fetched2);
			}
		}
		return null;
	}

	/**
	 * Returns a canonical resource defined by its type and uri
	 * 
	 * @param type resource type
	 * @param uri  resource uri
	 * @return
	 */
	public org.hl7.fhir.r4.model.Resource getCanonicalResourceById(String type, String uri) {
		if (this.getContext().hasResource(type, uri)) {
			org.hl7.fhir.r5.model.Resource fetched = this.getContext().fetchResourceById(type, uri);
			if (fetched != null) {
				return VersionConvertorFactory_40_50.convertResource(fetched);
			}
		}
		return null;
	}

	/**
	 * Parses a FHIR Structure Map from the textual representation according to the
	 * FHIR Mapping Language grammar (see
	 * http://build.fhir.org/mapping-language.html#grammar)
	 * 
	 * @param content FHIR Mapping Language text
	 * @return parsed StructureMap resource
	 * @throws FHIRException FHIR Exception
	 */
	public org.hl7.fhir.r4.model.StructureMap parseMap(String content) throws FHIRException {
		SimpleWorkerContext context = this.getContext();
		List<Base> outputs = new ArrayList<>();
		StructureMapUtilities scu = new MatchboxStructureMapUtilities(context,
				new TransformSupportServices(context, outputs), this);
		org.hl7.fhir.r5.model.StructureMap mapR5 = scu.parse(content, "map");
		mapR5.getText().setStatus(NarrativeStatus.GENERATED);
		mapR5.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
		String render = StructureMapUtilities.render(mapR5);
		mapR5.getText().getDiv().addTag("pre").addText(render);
		return (org.hl7.fhir.r4.model.StructureMap) VersionConvertorFactory_40_50.convertResource(mapR5);
	}

	/**
	 * creates the snapshot for the provided StructureDefinition
	 * 
	 * @param sd StructureDefinition with differential
	 * @return StructureDefinition with snapshot (differential applied to base
	 *         definition)
	 * @throws FHIRException FHIR Exception
	 * @throws IOException   IO Exception
	 */
	public org.hl7.fhir.r4.model.StructureDefinition createSnapshot(org.hl7.fhir.r4.model.StructureDefinition sd)
			throws FHIRException, IOException {
		StructureDefinition base = this.getContext().fetchResource(StructureDefinition.class, sd.getBaseDefinition());
		StructureDefinition sdR5 = (StructureDefinition) VersionConvertorFactory_40_50.convertResource(sd);

		new ProfileUtilities(this.getContext(), null, null).setAutoFixSliceNames(true).generateSnapshot(base, sdR5,
				sdR5.getUrl(), null, sdR5.getName());

		return (org.hl7.fhir.r4.model.StructureDefinition) VersionConvertorFactory_40_50.convertResource(sdR5);
	}

	/**
	 * @param input      input to run fhirpath expression against
	 * @param inputJson  if input format is json or xml
	 * @param expression fhirPath expression
	 * @return result
	 * @throws FHIRException FHIR Exception
	 * @throws IOException   IO Exception
	 */
	public String evaluateFhirPath(String input, boolean inputJson, String expression)
			throws FHIRException, IOException {
		FHIRPathEngine fpe = this.getValidator(null).getFHIRPathEngine();
		Element e = Manager.parseSingle(this.getContext(), new ByteArrayInputStream(input.getBytes("UTF-8")),
				(inputJson ? FhirFormat.JSON : FhirFormat.XML));
		ExpressionNode exp = fpe.parse(expression);
		return fpe.evaluateToString(new ValidatorHostContext(this.getContext(), e), e, e, e, exp);
	}

	protected void txLog(String msg) {
		log.info("tx ", msg);
	}

}
