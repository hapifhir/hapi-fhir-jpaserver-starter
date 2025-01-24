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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.fhir.ucum.UcumEssenceService;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.context.SimpleWorkerContext.SimpleWorkerContextBuilder;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.fhirpath.FHIRPathEngine;
import org.hl7.fhir.r5.formats.IParser;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.renderers.RendererFactory;
import org.hl7.fhir.r5.renderers.utils.RenderingContext;
import org.hl7.fhir.r5.renderers.utils.ResourceWrapper;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.hl7.fhir.r5.utils.OperationOutcomeUtilities;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.utilities.ByteProvider;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.json.model.JsonObject;
import org.hl7.fhir.utilities.npm.CommonPackages;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.validation.IgLoader;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.ValidatorUtils;
import org.hl7.fhir.validation.cli.services.PassiveExpiringSessionCache;
import org.hl7.fhir.validation.instance.InstanceValidator;

import ch.ahdis.matchbox.engine.cli.VersionUtil;
import ch.ahdis.matchbox.engine.exception.IgLoadException;
import ch.ahdis.matchbox.engine.exception.MatchboxEngineCreationException;
import ch.ahdis.matchbox.engine.exception.TerminologyServerException;
import ch.ahdis.matchbox.mappinglanguage.MatchboxStructureMapUtilities;
import ch.ahdis.matchbox.mappinglanguage.TransformSupportServices;

/**
 * Base Engine providing functionality on top of the ValidationEngine
 * 
 * @author oliveregger
 *
 */
public class MatchboxEngine extends ValidationEngine {

	// Current packages that are provided with Matchbox Engine
	public static final String PACKAGE_R4_TERMINOLOGY = "hl7.terminology.r4#6.1.0";
	public static final String PACKAGE_R5_TERMINOLOGY = "hl7.terminology.r5#6.1.0";
	public static final String PACKAGE_R4_UV_EXTENSIONS = "hl7.fhir.uv.extensions.r4#1.0.0";
	public static final String PACKAGE_UV_EXTENSIONS = "hl7.fhir.uv.extensions#1.0.0";
	public static final String PACKAGE_UV_XVER = "hl7.fhir.uv.xver#0.1.0@mb";
	public static final String PACKAGE_CDA_UV_CORE = "hl7.cda.uv.core#2.0.0-sd-202406-matchbox-patch";

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxEngine.class);

	protected List<String> suppressedWarnInfoPatterns = new ArrayList<>();
	protected PassiveExpiringSessionCache sessionCache = new PassiveExpiringSessionCache();
	
	static protected ValidationEngine nullEngine;

	static protected SimpleWorkerContext fmlParseContext = null;

	
	static {
			try {
				nullEngine = new ValidationEngineBuilder().fromNothing();
		} catch (IOException e) {
				log.error("problem with inizializin", e);		
		}
	};

	private Parameters makeExpProfile() {
		Parameters ep = new Parameters();
		ep.addParameter("profile-url", "http://hl7.org/fhir/ExpansionProfile/dc8fd4bc-091a-424a-8a3b-6198ef146891"); // change this to blow the cache
		// all defaults....
		return ep;
	  }
	
	public MatchboxEngine(SimpleWorkerContext context) throws FHIRException, IOException  {
			super(nullEngine);
			setContext(context);
			this.setVersion(context.getVersion());
	    context.setCanNoTS(true);
	    
	    NpmPackage npmX = getPcm().loadPackage(CommonPackages.ID_XVER, CommonPackages.VER_XVER);
	    context.loadFromPackage(npmX, null);

	    this.setIgLoader(new IgLoader(this.getPcm(), this.getContext(), this.getVersion(), this.isDebug()));
	    try {
	        ClassLoader classLoader = ValidationEngine.class.getClassLoader();
	        InputStream ue = classLoader.getResourceAsStream("ucum-essence.xml");
	        context.setUcumService(new UcumEssenceService(ue));
	      } catch (Exception e) {
	        throw new FHIRException("Error loading UCUM from embedded ucum-essence.xml: "+e.getMessage(), e);
	      }
  	  context.setExpansionParameters(makeExpProfile());	
      FHIRPathEngine fhirPathEngine = new FHIRPathEngine(context);
      fhirPathEngine.setAllowDoubleQuotes(false);
      this.setFhirPathEngine(fhirPathEngine);
  		try {
  			this.setPcm(new FilesystemPackageCacheManager.Builder()
								.withCacheFolder(this.getPcm().getFolder())
								.build());
  		} catch (final IOException e) {
  			throw new MatchboxEngineCreationException(e);
  		}
	}

	public MatchboxEngine(final @NonNull ValidationEngine other) throws FHIRException, IOException {
		super(other);
		if (other instanceof MatchboxEngine) {
				MatchboxEngine otherMatchboxEngine = (MatchboxEngine) other;
				this.sessionCache = otherMatchboxEngine.sessionCache;
				this.suppressedWarnInfoPatterns = otherMatchboxEngine.suppressedWarnInfoPatterns;
		}
		// Create a new IgLoader, otherwise the context is desynchronized between the loader and the engine
		this.setIgLoader(new IgLoader(this.getPcm(), this.getContext(), this.getVersion(), this.isDebug()));
		try {
			this.setPcm(new FilesystemPackageCacheManager.Builder()
								.withCacheFolder(this.getPcm().getFolder())
								.build());
		} catch (final IOException e) {
			throw new MatchboxEngineCreationException(e);
		}
	}
	
	public void cacheXVersionEngine(MatchboxEngine engine) {
			sessionCache.cacheSession(engine.getVersion().substring(0,3), engine);
	}
		
	

	/**
	 * Builder class to instantiate a MappingEngine
	 * 
	 * @author oliveregger, ahdis ag
	 *
	 */
	public static class MatchboxEngineBuilder extends ValidationEngineBuilder {

		/**
		 * The terminology server to use. {@code null} means no server will be used.
		 */
		private String txServer = null;

		/**
		 * The filesystem package cache mode.
		 */
		private FilesystemPackageCacheMode packageCacheMode = FilesystemPackageCacheMode.USER;

		/**
		 * The filesystem package cache path if FilesystemPackageCacheMode.CUSTOM, or {@code null}.
		 */
		private String packageCachePath = null;

		/**
		 * The FHIR version to use.
		 */
		private FhirPublication fhirVersion = FhirPublication.R4;


		/**
		 * If FHIR XVersion should be enabled
		 */
		private boolean withXVersion = false;

		public MatchboxEngineBuilder withXVersion(boolean withXVersion) {
			this.withXVersion = withXVersion;
			return this;
		}

		/**
		 * Creates an empty builder instance
		 */
		public MatchboxEngineBuilder() {
		}

		/**
		 * Sets the terminology server. Use {@code null} to disable the use of a terminology server.
		 *
		 * @param txServer The URL of the terminology server or {@code null}.
		 */
		public void setTxServer(final String txServer) {
			this.txServer = txServer;
		}

		/**
		 * Sets the mode of the filesystem package cache manager. It controls where the package will be stored on the
		 * filesystem:
		 * <ul>
		 *    <li>{@code USER}: {USER HOME}/.fhir/packages</li>
		 *    <li>{@code SYSTEM}: /var/lib/.fhir/packages</li>
		 *    <li>{@code TESTING}: {TMP}/.fhir/packages</li>
		 * </ul>
		 *
		 *
		 * @see FilesystemPackageCacheManager.Builder for details.
		 * @param packageCacheMode The mode of the filesystem package cache manager.
		 */
		public void setPackageCacheMode(final FilesystemPackageCacheMode packageCacheMode) {
			this.packageCacheMode = packageCacheMode;
		}

		/**
		 * Sets the custom path of the package cache.
		 * @param packageCachePath The package cache path.
		 */
		public void setPackageCachePath(final String packageCachePath) {
			this.packageCacheMode = FilesystemPackageCacheMode.CUSTOM;
			this.packageCachePath = packageCachePath;
		}

		/**
		 * Returns a FHIR R4 engine configured with hl7 terminology
		 * 
		 * @return
		 * @throws MatchboxEngineCreationException
		 */
		public MatchboxEngine getEngineR4() throws MatchboxEngineCreationException {
			log.info("Initializing Matchbox Engine (FHIR R4 with terminology provided in classpath)");
			log.info(VersionUtil.getPoweredBy());
			final MatchboxEngine engine ;
			try { 
					engine = new MatchboxEngine(
									new SimpleWorkerContextBuilder().fromPackage(NpmPackage.fromPackage(getClass().getResourceAsStream("/hl7.fhir.r4.core.tgz")), ValidatorUtils.loaderForVersion("4.0.1"), false));
			}
			catch (final Exception e) { throw new MatchboxEngineCreationException(e); } 
			log.info("loaded hl7.fhir.r4.core#4.0.1");
			engine.setVersion(FhirPublication.R4.toCode());
			try {
				engine.loadPackage(this.getNpmPackageStream(PACKAGE_R4_TERMINOLOGY));
				engine.loadPackage(this.getNpmPackageStream(PACKAGE_R4_UV_EXTENSIONS));
				if (this.withXVersion) {
					this.removeStructureMaps(engine);
					engine.loadPackage(this.getNpmPackageStream(PACKAGE_UV_XVER));
				}
			} catch (final IOException e) {
				throw new IgLoadException(e);
			}
			if (this.txServer == null) {
				engine.getContext().setCanRunWithoutTerminology(true);
				engine.getContext().setNoTerminologyServer(true);
			} else {
				engine.getContext().setCanRunWithoutTerminology(false);
				engine.getContext().setNoTerminologyServer(false);
				try {
					engine.setTerminologyServer(this.txServer, null, FhirPublication.R4, true);
				} catch (final Exception e) {
					throw new TerminologyServerException(e);
				}
			}
			engine.getContext().setPackageTracker(engine);
			engine.setPcm(this.getFilesystemPackageCacheManager());
			engine.setPolicyAdvisor(new ValidationPolicyAdvisor(ReferenceValidationPolicy.CHECK_VALID));
			engine.setAllowExampleUrls(true);
			return engine;
		}

		/**
		 * Returns a FHIR R4B engine configured with hl7 terminology
		 * 
		 * @return
		 * @throws MatchboxEngineCreationException
		 */
		public MatchboxEngine getEngineR4B() throws MatchboxEngineCreationException {
			log.info("Initializing Matchbox Engine (FHIR R4B with terminology provided in classpath)");
			log.info(VersionUtil.getPoweredBy());
			final MatchboxEngine engine ;
			try { engine = new MatchboxEngine(new SimpleWorkerContextBuilder().fromPackage(NpmPackage.fromPackage(getClass().getResourceAsStream("/hl7.fhir.r4b.core.tgz")), ValidatorUtils.loaderForVersion("4.3.0"), false));
			}
			catch (final Exception e) { throw new MatchboxEngineCreationException(e); } 
			engine.setVersion(FhirPublication.R4B.toCode());
			try {
				engine.loadPackage(this.getNpmPackageStream(PACKAGE_R4_TERMINOLOGY));
				engine.loadPackage(this.getNpmPackageStream(PACKAGE_R4_UV_EXTENSIONS));
				if (this.withXVersion) {
					this.removeStructureMaps(engine);
					engine.loadPackage(this.getNpmPackageStream(PACKAGE_UV_XVER));
				}
			} catch (final IOException e) {
				throw new IgLoadException(e);
			}
			if (this.txServer == null) {
				engine.getContext().setCanRunWithoutTerminology(true);
				engine.getContext().setNoTerminologyServer(true);
			} else {
				engine.getContext().setCanRunWithoutTerminology(false);
				engine.getContext().setNoTerminologyServer(false);
				try {
					engine.setTerminologyServer(this.txServer, null, FhirPublication.R4, true);
				} catch (final Exception e) {
					throw new TerminologyServerException(e);
				}
			}
			engine.getContext().setPackageTracker(engine);
			engine.setPcm(this.getFilesystemPackageCacheManager());
			engine.setPolicyAdvisor(new ValidationPolicyAdvisor(ReferenceValidationPolicy.CHECK_VALID));
			engine.setAllowExampleUrls(true);
			return engine;
		}

		/**
		 * Returns a FHIR R5 engine configured with hl7 terminology
		 *
		 * @return
		 * @throws MatchboxEngineCreationException
		 */
		public MatchboxEngine getEngineR5() throws MatchboxEngineCreationException {
			log.info("Initializing Matchbox Engine (FHIR R5 with terminology provided in classpath)");
			log.info(VersionUtil.getPoweredBy());
			final MatchboxEngine engine;
			try { engine = new MatchboxEngine(createR5WorkerContext());
			}
			catch (final Exception e) { throw new MatchboxEngineCreationException(e); } 
			engine.setVersion(FhirPublication.R5.toCode());
			try {
				engine.loadPackage(this.getNpmPackageStream(PACKAGE_R5_TERMINOLOGY));
				engine.loadPackage(this.getNpmPackageStream(PACKAGE_UV_EXTENSIONS));
				if (this.withXVersion) {
					this.removeStructureMaps(engine);
					engine.loadPackage(this.getNpmPackageStream(PACKAGE_UV_XVER));
				}
			} catch (final IOException e) {
				throw new IgLoadException(e);
			}
			if (this.txServer == null) {
				engine.getContext().setCanRunWithoutTerminology(true);
				engine.getContext().setNoTerminologyServer(true);
			} else {
				engine.getContext().setCanRunWithoutTerminology(false);
				engine.getContext().setNoTerminologyServer(false);
				try {
					engine.setTerminologyServer(this.txServer, null, FhirPublication.R5, true);
				} catch (final Exception e) {
					throw new TerminologyServerException(e);
				}
			}

			engine.setPolicyAdvisor(new ValidationPolicyAdvisor(ReferenceValidationPolicy.CHECK_VALID));
			engine.setAllowExampleUrls(true);

			engine.getContext().setPackageTracker(engine);
			engine.setPcm(this.getFilesystemPackageCacheManager());
			return engine;
		}

		/**
		 * Returns empty engine
		 * 
		 * @return
		 * @throws MatchboxEngineCreationException
		 */
		public MatchboxEngine getEngine() throws MatchboxEngineCreationException {
			log.info("Initializing Matchbox Engine");
			log.info(VersionUtil.getPoweredBy());
			final MatchboxEngine engine;
			try { engine = new MatchboxEngine(this.fromNothing()); }
			catch (final IOException e) { throw new MatchboxEngineCreationException(e); }
			engine.setVersion(this.fhirVersion.toCode());
			engine.getContext().setAllowLoadingDuplicates(false);
			if (this.txServer == null) {
				engine.getContext().setCanRunWithoutTerminology(true);
				engine.getContext().setNoTerminologyServer(true);
			} else {
				engine.getContext().setCanRunWithoutTerminology(false);
				engine.getContext().setNoTerminologyServer(false);
				try {
					engine.setTerminologyServer(this.txServer, null, this.fhirVersion, true);
				} catch (final Exception e) {
					throw new TerminologyServerException(e);
				}
			}
			engine.getContext().setPackageTracker(engine);
			engine.setPcm(this.getFilesystemPackageCacheManager());
			engine.setPolicyAdvisor(new ValidationPolicyAdvisor(ReferenceValidationPolicy.CHECK_VALID));
			engine.setAllowExampleUrls(true);
			return engine;
		}

		@Override
		public ValidationEngine fromNothing() throws MatchboxEngineCreationException {
			try {
				return super.fromNothing();				
			} catch (final IOException e) {
				throw new MatchboxEngineCreationException(e);
			}
		}

		@Override
		public ValidationEngine fromSource(String src) throws URISyntaxException {
			try {
				return super.fromSource(src);				
			} catch (final IOException e) {
				throw new MatchboxEngineCreationException(e);
			}
	    }

		public MatchboxEngineBuilder withVersion(final String version) {
			super.withVersion(version);
			this.fhirVersion = FhirPublication.fromCode(version);
			return this;
		}

		private FilesystemPackageCacheManager getFilesystemPackageCacheManager() throws MatchboxEngineCreationException {
			try {
				return switch(this.packageCacheMode) {
					case USER -> new FilesystemPackageCacheManager.Builder().build();
					case SYSTEM -> new FilesystemPackageCacheManager.Builder().withSystemCacheFolder().build();
					case TESTING -> new FilesystemPackageCacheManager.Builder().withTestingCacheFolder().build();
					case CUSTOM ->
						new FilesystemPackageCacheManager.Builder().withCacheFolder(this.packageCachePath).build();
				};
			} catch (final IOException e) {
				throw new MatchboxEngineCreationException(e);
			}
		}

		@NonNull
		private InputStream getNpmPackageStream(final String packageName) {
			return Objects.requireNonNull(getClass().getResourceAsStream("/%s.tgz".formatted(packageName)));
		}

		/**
		 * Remove old StructureMaps from the context, especially from PACKAGE_R4_UV_EXTENSIONS which are replaced by
		 * newer versions from PACKAGE_UV_XVER
		 * @param engine
		 */
		private void removeStructureMaps(final MatchboxEngine engine) {
			for (final StructureMap map : engine.getContext().fetchResourcesByType(StructureMap.class)) {
				engine.getContext().dropResource(map);
			}
		}
	}

	public static SimpleWorkerContext createR5WorkerContext() throws IOException {
		return new SimpleWorkerContextBuilder()
			.fromPackage(
				NpmPackage.fromPackage(MatchboxEngine.class.getResourceAsStream("/hl7.fhir.r5.core.tgz")),
				ValidatorUtils.loaderForVersion("5.0.0"),
				false
			);
	}

	/**
	 * A safe getter for the PCM. It has been created in the constructor, so it should be safe to access.
	 */
	@Override
	public FilesystemPackageCacheManager getPcm() {
		try {
			return super.getPcm();
		} catch (final IOException e) {
			// This should not happen, we initialize the package cache manager in the constructor
			throw new RuntimeException(e);
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

		SimpleWorkerContext context = this.getContext();
		StructureMap map = context.fetchResource(StructureMap.class, mapUri);
		
		String fhirVersionTarget = getFhirVersion(getCanonicalFromStructureMap(map, StructureMap.StructureMapModelMode.TARGET));
		if (fhirVersionTarget !=null && (fhirVersionTarget.startsWith("4.0") || fhirVersionTarget.startsWith("4.3") || fhirVersionTarget.startsWith("5.0"))  && !fhirVersionTarget.equals(this.getVersion().substring(0, 3))) {
			log.info("Loading additional FHIR version for Target into context " + fhirVersionTarget);
			context = getContextForFhirVersion(fhirVersionTarget);
		}

		Element transformed = transform(ByteProvider.forBytes(input.getBytes("UTF-8")), (inputJson ? FhirFormat.JSON : FhirFormat.XML),
												  mapUri, context);
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		if (outputJson)
			new org.hl7.fhir.r5.elementmodel.JsonParser(context).compose(transformed, boas,
					IParser.OutputStyle.PRETTY,
					null);
		else
			new org.hl7.fhir.r5.elementmodel.XmlParser(context).compose(transformed, boas,
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
	public org.hl7.fhir.r5.elementmodel.Element transform(final ByteProvider source,
																			final FhirFormat cntType,
																			final String mapUri,
																			final SimpleWorkerContext targetContext)
			throws FHIRException, IOException {
		SimpleWorkerContext context = this.getContext();

		// usual case is that source and target are in the same FHIR version as in the context, however it could be that either source or target are in a different FHIR version
		// if this is the case we do lazy loading of the additional FHIR version into the context

		StructureMap map = context.fetchResource(StructureMap.class, mapUri);
		String canonicalSource = getCanonicalFromStructureMap(map, StructureMap.StructureMapModelMode.SOURCE);

		String fhirVersionSource = getFhirVersion(canonicalSource);
		if (fhirVersionSource !=null &&  (fhirVersionSource.startsWith("4.0") || fhirVersionSource.startsWith("4.3") || fhirVersionSource.startsWith("5.0")) && !fhirVersionSource.equals(this.getVersion().substring(0,3))) {
			log.info("Loading additional FHIR version for Source into context " + fhirVersionSource);
			context = getContextForFhirVersion(fhirVersionSource);
		}
		
		org.hl7.fhir.r5.elementmodel.ParserBase parser = Manager.makeParser(context, cntType);
		StructureDefinition sd = context.fetchResource(StructureDefinition.class, canonicalSource);
		if (sd.getKind() == StructureDefinitionKind.LOGICAL) {
			parser.setLogical(sd);
		}
		org.hl7.fhir.r5.elementmodel.Element src = parser.parseSingle(new ByteArrayInputStream(source.getBytes()), null);
		return transform(src, mapUri, targetContext);
	}

	/**
	 * Adapted transform operation from Validation Engine to use patched
	 * MatchboxStructureMapUtilities
	 */
	public SimpleWorkerContext getContextForFhirVersion(String fhirVersion)
			throws FHIRException, IOException {
		SimpleWorkerContext contextForFhirVersion = null;
		if (fhirVersion.startsWith("4.0")) {
			ValidationEngine engine = sessionCache.fetchSessionValidatorEngine(fhirVersion.substring(0,3));
			if (engine == null) {
				engine = new MatchboxEngineBuilder().getEngineR4();
				sessionCache.cacheSession(fhirVersion.substring(0,3), engine);
			} 
			contextForFhirVersion = engine.getContext();
			}
		if (fhirVersion.startsWith("4.3")) {
			ValidationEngine engine = sessionCache.fetchSessionValidatorEngine(fhirVersion.substring(0,3));
			if (engine == null) {
				engine = new MatchboxEngineBuilder().getEngineR4B();
				sessionCache.cacheSession(fhirVersion.substring(0,3), engine);
			} 
			contextForFhirVersion = engine.getContext();
		}
		if (fhirVersion.startsWith("5.0")) {
			ValidationEngine engine = sessionCache.fetchSessionValidatorEngine(fhirVersion.substring(0,3));
			if (engine == null) {
				engine = new MatchboxEngineBuilder().getEngineR5();
				sessionCache.cacheSession(fhirVersion.substring(0,3), engine);
			} 
			contextForFhirVersion = engine.getContext();
			}
		if (contextForFhirVersion != null) {
			// we need to copy now all StructureDefinitions from this Version to the new context
			// check first if they are not already defined
			if (this.getContext().fetchResource(StructureDefinition.class, "http://hl7.org/fhir/"+fhirVersion.substring(0,3)+"/StructureDefinition/StructureDefinition") == null) {
				int len = "http://hl7.org/fhir/".length();
				for (StructureDefinition sd : contextForFhirVersion.listStructures()) {
					if (sd.getUrl().startsWith("http://hl7.org/fhir/") && sd.getKind()!=null  && sd.getKind() != StructureDefinition.StructureDefinitionKind.LOGICAL && !"Extensions".equals(sd.getType())) {
						if (!Character.isDigit(sd.getUrl().charAt(len))) {
  						StructureDefinition sdn = sd.copy();
  						sdn.setUrl(sdn.getUrl().replace("http://hl7.org/fhir/", "http://hl7.org/fhir/"+fhirVersion.substring(0,3)+"/"));
  						sdn.addExtension().setUrl("http://hl7.org/fhir/StructureDefinition/elementdefinition-namespace")
  						  .setValue(new UriType("http://hl7.org/fhir"));
  						this.getContext().cacheResource(sdn);
						}
					}
				}	
			} 
		}
		return contextForFhirVersion;
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
	public org.hl7.fhir.r5.elementmodel.Element transform(org.hl7.fhir.r5.elementmodel.Element src,  String mapUri, SimpleWorkerContext targetContext)
			throws FHIRException, IOException {
		SimpleWorkerContext context = this.getContext();
		List<Base> outputs = new ArrayList<>();
		StructureMapUtilities scu = new MatchboxStructureMapUtilities(context,
				new TransformSupportServices(targetContext!=null ? targetContext : context, outputs), this);
		StructureMap map = context.fetchResource(StructureMap.class, mapUri);
		if (map == null) {
			log.error("Unable to find map " + mapUri + " (Known Maps = " + context.listMapUrls() + ")");
			throw new Error("Unable to find map " + mapUri + " (Known Maps = " + context.listMapUrls() + ")");
		}
		log.info("Using map " + map.getUrl() + (map.getVersion()!=null ? "|" + map.getVersion() + " " : "" )
				+ (map.getDateElement() != null && !map.getDateElement().isEmpty()  ? "(" + map.getDateElement().asStringValue() + ")" : ""));

		org.hl7.fhir.r5.elementmodel.Element resource = getTargetResourceFromStructureMap(map, targetContext);

		scu.transform(null, src, map, resource);
		resource.populatePaths(null);
		return resource;
	}

	/**
	 * returns the explication FHIR version of it the FHIR resource contains the version inside the url
	 * http://hl7.org/fhir/3.0/StructureDefinition/Account
	 * @param url
	 * @return
	 */
	private String getFhirVersion(String url) {
		return VersionUtilities.getMajMin(url);
	}

	/**
	 * gets the canonical for either source or target, we assume currently that the fhir source or target is the default canonical for the source and target, this might not be true
	 * however this approach is used in the getTargetResourceFromStructureMap below
	 * @param map
	 * @param mode
	 * @return
	 */
	private String getCanonicalFromStructureMap(StructureMap map, StructureMap.StructureMapModelMode mode) {
		String targetTypeUrl = null;
		for (StructureMap.StructureMapStructureComponent component : map.getStructure()) {
			if (component.getMode() == mode) {
				targetTypeUrl = component.getUrl();
				break;
			}
		}
		
		return targetTypeUrl;
	}

	private org.hl7.fhir.r5.elementmodel.Element getTargetResourceFromStructureMap(StructureMap map, SimpleWorkerContext targetContext) {
		String targetTypeUrl = null;
		SimpleWorkerContext context = (targetContext!=null ? targetContext : this.getContext());
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

		if (Utilities.isAbsoluteUrl(targetTypeUrl)) {
			int index = targetTypeUrl.indexOf("/"+context.getVersion().substring(0,3)+"/");
			if (index >= 0) {
				targetTypeUrl = targetTypeUrl.substring(0, index)+targetTypeUrl.substring(index+4);
			}
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

		return Manager.build(context, structureDefinition);
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
	 * replace a resource with the same canonical url for FHIR R4
	 * 
	 * @param resource canonical resource to add
	 * @throws FHIRException FHIR Exception
	 */
	public void addCanonicalResource(Resource resource) throws FHIRException {
		org.hl7.fhir.r5.model.Resource r5 = VersionConvertorFactory_40_50.convertResource(resource);
		getContext().cacheResource(r5);
	}

	/**
	 * adds a canonical resource to the loaded packages, please note that it will
	 * replace a resource with the same canonical url for FHIR R4B
	 * 
	 * @param resource canonical resource to add
	 * @throws FHIRException FHIR Exception
	 */
	public void addCanonicalResource(org.hl7.fhir.r4b.model.CanonicalResource resource) throws FHIRException {
		org.hl7.fhir.r5.model.Resource r5 = VersionConvertorFactory_43_50.convertResource(resource);
		getContext().cacheResource(r5);
	}

	/**
	 * adds a canonical resource to the loaded packages, please note that it will
	 * replace a resource with the same canonical url  for FHIR R5
	 * 
	 * @param resource canonical resource to add
	 * @throws FHIRException FHIR Exception
	 */
	public void addCanonicalResource(org.hl7.fhir.r5.model.CanonicalResource resource) throws FHIRException {
		getContext().cacheResource(resource);
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
	public OperationOutcome validate(final @NonNull InputStream stream,
												final @NonNull FhirFormat format,
												final @Nullable String profileUrl)
			throws FHIRException, IOException, EOperationOutcome {
		return this.messagesToOutcome(this.validate(format, stream, profileUrl), this.getContext());
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
	 */
	public OperationOutcome validate(final @NonNull Resource resource, final @Nullable String profileUrl)
			throws FHIRException, IOException, EOperationOutcome {
		final var result = new org.hl7.fhir.r4.formats.JsonParser().composeBytes(resource);
		return this.validate(new ByteArrayInputStream(result), FhirFormat.JSON, profileUrl);
	}

	// testing entry point
	public List<ValidationMessage> validate(final @NonNull FhirFormat format,
														 final @NonNull InputStream stream,
														 final @Nullable String profileUrl)
			throws FHIRException, IOException, EOperationOutcome {
		StructureDefinition sd = null;
		if (profileUrl != null) {
			sd = this.getStructureDefinitionR5(profileUrl);
			log.info("Using profile for validation " + sd.getUrl() + "|" + sd.getVersion() + " "
						+ (sd.getDateElement() != null ? "(" + sd.getDateElement().asStringValue() + ")" : ""));
		}
		final List<ValidationMessage> messages = new ArrayList<>();
		final InstanceValidator validator = getValidator(format);
		validator.validate(null, messages, stream, format, (sd != null) ? new ArrayList<>(List.of(sd)) :  new ArrayList<>());
		return this.filterValidationMessages(messages);
	}

	/**
	 * Get the corresponding StructureDefinition (R5)
	 *
	 * @param profile
	 * @return
	 */
	public StructureDefinition getStructureDefinitionR5(final String profile) {
		return this.getContext().fetchResource(StructureDefinition.class, profile);
	}

    /**
	 * Returns a canonical resource defined by its url
	 * 
	 * @param canonical
	 * @param fhirVersion
	 * @return
	 */
	public IBaseResource getCanonicalResource(String canonical, String fhirVersion) {
		org.hl7.fhir.r5.model.Resource fetched = this.getContext().fetchResource(null, canonical);
		// allResourcesById is not package aware (???) so we need to fetch it again
		if (fetched!=null) {
			org.hl7.fhir.r5.model.Resource fetched2  = this.getContext().fetchResource(fetched.getClass(), canonical);
			if (fetched2 != null) {
				switch(fhirVersion) {
					case "4.0.1":
						return VersionConvertorFactory_40_50.convertResource(fetched2);
					case "4.3.0":
						return VersionConvertorFactory_43_50.convertResource(fetched2);
					case "5.0.0":
						return fetched2;
				}
			}
		}
		return null;
	}

	// same as above but called from the validator on the meta data type
	// @Override
	// public CanonicalResource fetchCanonicalResource(IResourceValidator validator, String url) throws URISyntaxException {
	// 	return super.fetchCanonicalResource(validator, url);
		// we have an issue when just override the above, however we look not to get into here and d
		// aused by: java.lang.Error: The version 0.0 is not currently supported
        // at org.hl7.fhir.convertors.txClient.TerminologyClientFactory.makeClient(TerminologyClientFactory.java:57)
        // at org.hl7.fhir.validation.cli.services.StandAloneValidatorFetcher.fetchCanonicalResource(StandAloneValidatorFetcher.java:260)
        // at org.hl7.fhir.validation.ValidationEngine.fetchCanonicalResource(ValidationEngine.java:1051)
        // at ch.ahdis.matchbox.engine.MatchboxEngine.fetchCanonicalResource(MatchboxEngine.java:414)
        // at org.hl7.fhir.validation.instance.InstanceValidator.lookupProfileReference(InstanceValidator.java:4861)
        // at org.hl7.fhir.validation.instance.InstanceValidator.start(InstanceValidator.java:4782)
        // at org.hl7.fhir.validation.instance.InstanceValidator.validateResource(InstanceValidator.java:6184)
        // at org.hl7.fhir.validation.instance.InstanceValidator.validate(InstanceValidator.java:879)
        // at org.hl7.fhir.validation.instance.InstanceValidator.validate(InstanceValidator.java:713)
        // at ch.ahdis.matchbox.engine.MatchboxEngine.validate(MatchboxEngine.java:344)
	// }

	@Override
	public boolean fetchesCanonicalResource(IResourceValidator validator, String url) {
		// don't use the fetcher, should we do this better in directly in StandAloneValidatorFetcher implmentation
		// https://github.com/ahdis/matchbox/issues/67
		return getCanonicalResource(url,"5.0.0") != null;
	}

	/**
	 * Returns a canonical resource defined by its type and uri
	 * 
	 * @param type resource type
	 * @param id   resource id
	 * @return
	 */
	public IBaseResource getCanonicalResourceById(final String type, final @NonNull String id) {
		org.hl7.fhir.r5.model.Resource fetched = this.getContext().fetchResourceById(type, id);
		if (fetched != null) {
			if ("5.0.0".equals(this.getVersion())) {
				return fetched;
			}
			if ("4.3.0".equals(this.getVersion())) {
				return VersionConvertorFactory_43_50.convertResource(fetched);
			}
			return VersionConvertorFactory_40_50.convertResource(fetched);
		}
		return null;
	}

    /**
	 * Parses a FHIR Structure Map from the textual representation according to the
	 * FHIR Mapping Language grammar (see
	 * http://build.fhir.org/mapping-language.html#grammar) for FHIR release 5
	 * 
	 * @param content FHIR Mapping Language text
	 * @return parsed StructureMap resource
	 * @throws FHIRException FHIR Exception
	 */
	public org.hl7.fhir.r5.model.StructureMap parseMapR5(String content) throws IOException, FHIRException {
		if (MatchboxEngine.fmlParseContext == null) {
			if ("5.0.0".equals(this.getContext().getVersion())) {
				MatchboxEngine.fmlParseContext = this.getContext();
			} else {
				MatchboxEngine.fmlParseContext = MatchboxEngine.createR5WorkerContext();
			}
		}
		List<Base> outputs = new ArrayList<>();
		StructureMapUtilities scu = new MatchboxStructureMapUtilities(fmlParseContext,
				new TransformSupportServices(fmlParseContext, outputs), this);
		org.hl7.fhir.r5.model.StructureMap mapR5 = scu.parse(content, "map");
		mapR5.getText().setStatus(NarrativeStatus.GENERATED);
		mapR5.getText().setDiv(new XhtmlNode(NodeType.Element, "div"));
		String render = StructureMapUtilities.render(mapR5);
		mapR5.getText().getDiv().addTag("pre").addText(render);
		return mapR5;
	}


	/**
	 * Parses a FHIR Structure Map from the textual representation according to the
	 * FHIR Mapping Language grammar (see
	 * http://build.fhir.org/mapping-language.html#grammar)
	 * 
	 * @param content FHIR Mapping Language text
	 * @return parsed StructureMap resource
	 * @throws IOException FHIR Exception
	 * @throws FHIRException FHIR Exception
	 */
	public org.hl7.fhir.r4.model.StructureMap parseMap(String content) throws IOException, FHIRException {
		org.hl7.fhir.r5.model.StructureMap mapR5 = parseMapR5(content);
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
		StructureDefinition sdR5 = (StructureDefinition) VersionConvertorFactory_40_50.convertResource(sd);
		try {
			new ContextUtilities(this.getContext()).generateSnapshot(sdR5); 
		  } catch (Exception e) {
			// not sure what to do in this case?
			log.error("Unable to generate snapshot for "+sd.getUrl(), e);
			return null;
		  }
		return (org.hl7.fhir.r4.model.StructureDefinition) VersionConvertorFactory_40_50.convertResource(sdR5);
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
	public org.hl7.fhir.r5.model.StructureDefinition createSnapshot(org.hl7.fhir.r5.model.StructureDefinition sd)
			throws FHIRException, IOException {
		StructureDefinition sdR5 = sd;
		try {
			new ContextUtilities(this.getContext()).generateSnapshot(sdR5); 
		  } catch (Exception e) {
			// not sure what to do in this case?
			log.error("Unable to generate snapshot for "+sd.getUrl(), e);
			return null;
		  }
		return sd;
	}

	/**
	 * @param input      input to run fhirpath expression against
	 * @param inputJson  if input format is json or xml
	 * @param expression fhirPath expression
	 * @return result
	 * @throws FHIRException FHIR Exception
	 * @throws IOException   IO Exception
	 */
	public String evaluateFhirPath(final @NonNull String input,
											 final boolean inputJson,
											 final @NonNull String expression)
			throws FHIRException, IOException {
		FHIRPathEngine fpe = this.getValidator(null).getFHIRPathEngine();
		Element e = Manager.parseSingle(this.getContext(), new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
				(inputJson ? FhirFormat.JSON : FhirFormat.XML));
		//ExpressionNode exp = fpe.parse(expression);
		return fpe.evaluateToString(e, expression);
		//return fpe.evaluateToString(new ValidatorHostContext(this.getContext(), e), e, e, e, exp);
	}
	

	public String convert(final @NonNull String input, final boolean inputJson ) throws FHIRException, IOException {
	    Element e = Manager.parseSingle(this.getContext(), new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
	            (inputJson ? FhirFormat.JSON : FhirFormat.XML));
	    ByteArrayOutputStream bs = new ByteArrayOutputStream();
	    Manager.compose(this.getContext(), e, bs, (inputJson ? FhirFormat.XML : FhirFormat.JSON), OutputStyle.PRETTY, null);
	    String result = new String(bs.toByteArray());
	    bs.close();
	    return result;
    }

	/**
	 * Loads an IG in the engine from its NPM package as an {@link InputStream}, its ID and version. The
	 * {@link InputStream} will be closed by {@link org.hl7.fhir.utilities.npm.NpmPackage#readStream}.
	 * <p>
	 * The package dependencies shall be added manually, no recursive loading will be performed.
	 *
	 * @param inputStream The NPM package input stream. It will be closed.
	 */
	public void loadPackage(final @NonNull InputStream inputStream) throws IOException {
		final NpmPackage npmPackage = NpmPackage.fromPackage(Objects.requireNonNull(inputStream));

		// Remove the dependencies to disable recursive loading
		npmPackage.getNpm().set("dependencies", new JsonObject());
		this.getIgLoader().loadPackage(npmPackage, true);
	}

	/**
	 * Copies the given list, removes messages if they are warning and match a list of ignored messages.
	 *
	 * @param messages The original list of validation messages.
	 * @return A copy of the message list, without issues that have to be filtered.
	 */
	public List<ValidationMessage> filterValidationMessages(final @NonNull List<ValidationMessage> messages) {
		final List<Pattern> ignoredPatterns = this.compileSuppressedWarnInfoPatterns();
		return messages.stream()
			.filter(message -> {
				if (message.getLevel() != ValidationMessage.IssueSeverity.WARNING && message.getLevel() != ValidationMessage.IssueSeverity.INFORMATION) {
					// We keep everything that is not a warning or an information
					return true;
				}
				// We keep the warning only if it matches no
				return ignoredPatterns.parallelStream().noneMatch(pattern -> pattern.matcher(message.getMessage()).find());
			})
			.collect(Collectors.toList());
	}

	/**
	 * Filters the given list of slices messages by removing those that match the suppressed warning/information patterns.
	 *
	 * @param messages The list of messages to filter.
	 * @return The filtered list of messages.
	 */
	public List<String> filterSlicingMessages(final String[] messages) {
		final List<Pattern> ignoredPatterns = this.compileSuppressedWarnInfoPatterns();
		return Arrays.asList(messages).stream()
			.filter(message -> {
				return ignoredPatterns.parallelStream().noneMatch(pattern -> pattern.matcher(message).find());
			})
			.collect(Collectors.toList());
	}

	/**
	 * Adds a text to the list of suppressed validation warning/information-level issues.
	 * <p>
	 * Implementation note: The text is Regex-escaped before being added to the list.
	 *
	 * @param text The text to add.
	 */
	public void addSuppressedWarnInfo(final @NonNull String text) {
		this.suppressedWarnInfoPatterns.add(Pattern.quote(Objects.requireNonNull(text)));
	}

	/**
	 * Adds a Regex pattern to the list of suppressed validation warning/information-level issues.
	 *
	 * @param pattern The Regex pattern to add.
	 */
	public void addSuppressedWarnInfoPattern(final @NonNull String pattern) {
		this.suppressedWarnInfoPatterns.add(Objects.requireNonNull(pattern));
	}

	/**
	 * Returns the list of suppressed validation warning/information-level issues.
	 */
	public List<String> getSuppressedWarnInfoPatterns() {
		return this.suppressedWarnInfoPatterns;
	}

	/**
	 * Compiles the list of suppressed validation warning/information-level issues into a list of {@link Pattern}.
	 */
	protected List<Pattern> compileSuppressedWarnInfoPatterns() {
		return this.suppressedWarnInfoPatterns.stream().map(Pattern::compile).collect(Collectors.toList());
	}

	/**
	 * Maps a list of {@link ValidationMessage} to an R4 {@link OperationOutcome}.
	 */
	protected OperationOutcome messagesToOutcome(final @NonNull List<ValidationMessage> messages,
																final @NonNull SimpleWorkerContext context)
		throws IOException, FHIRException, EOperationOutcome {
		final var op = new org.hl7.fhir.r5.model.OperationOutcome();
		messages.stream().map(vm -> OperationOutcomeUtilities.convertToIssue(vm, op))
			.forEach(op.getIssue()::add);
		final var rc = new RenderingContext(context, null, null, "http://hl7.org/fhir", "", null,
											 RenderingContext.ResourceRendererMode.END_USER, RenderingContext.GenerationRules.VALID_RESOURCE);
 		RendererFactory.factory(op, rc).renderResource(ResourceWrapper.forResource(rc.getContextUtilities(), op));
		return (OperationOutcome) (VersionConvertorFactory_40_50.convertResource(op));
	}

	public enum FilesystemPackageCacheMode {
		USER, SYSTEM, TESTING, CUSTOM
	}


	/**
	 * Initializes the terminology cache 
	 * @param cacheDir
	 * @throws FileNotFoundException
	 * @throws FHIRException
	 * @throws IOException
	 */	
    public void initTxCache(String cacheDir) throws FileNotFoundException, FHIRException, IOException {
		if (cacheDir !=null) {
			getContext().initTxCache(cacheDir);
			getContext().setCachingAllowed(true);
		} else {
			getContext().setCachingAllowed(false);
		}
	}

}
