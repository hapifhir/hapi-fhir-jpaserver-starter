package ch.ahdis.matchbox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import ch.ahdis.matchbox.engine.exception.IgLoadException;
import ch.ahdis.matchbox.engine.exception.MatchboxEngineCreationException;
import ch.ahdis.matchbox.engine.exception.TerminologyServerUnreachableException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.conformance.R5ExtensionsLoader;
import org.hl7.fhir.r5.terminologies.CodeSystemUtilities;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.validation.IgLoader;
import org.hl7.fhir.validation.cli.services.IPackageInstaller;
import org.hl7.fhir.validation.cli.services.StandAloneValidatorFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionDao;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.MatchboxEngine.MatchboxEngineBuilder;
import ch.ahdis.matchbox.util.EngineSessionCache;


public class MatchboxEngineSupport {
	
	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxEngineSupport.class);

	private static MatchboxEngine engine = null;
	private EngineSessionCache sessionCache;
	
	private boolean initialized = false;

	@Autowired
	private DaoRegistry myDaoRegistry;
  
	
	@Autowired
	private INpmPackageVersionResourceDao myPackageVersionResourceDao;
	
	@Autowired
	private PlatformTransactionManager myTxManager;
	
	@Autowired
	private IHapiPackageCacheManager myPackageCacheManager;

	@Autowired
	private INpmPackageVersionDao myNpmPackageVersionDao;
	
	@Autowired(required = false)
	private IBinaryStorageSvc myBinaryStorageSvc;

	@Autowired
	private CliContext cliContext;

	public MatchboxEngineSupport() {
		this.sessionCache = new EngineSessionCache();
	}

	public NpmPackageVersionResourceEntity loadPackageAssetByUrl(String theCanonicalUrl) {
		NpmPackageVersionResourceEntity resourceEntity  = new TransactionTemplate(myTxManager).execute(tx -> {
			String canonicalUrl = theCanonicalUrl;
			int versionSeparator = canonicalUrl.lastIndexOf('|');
			Slice<NpmPackageVersionResourceEntity> slice;
			if (versionSeparator != -1) {
				String canonicalVersion = canonicalUrl.substring(versionSeparator + 1);
				canonicalUrl = canonicalUrl.substring(0, versionSeparator);
				slice = myPackageVersionResourceDao.findByCanonicalUrlByCanonicalVersion(PageRequest.of(0, 2), canonicalUrl, canonicalVersion);
			} else {
				slice = myPackageVersionResourceDao.findCurrentVersionByCanonicalUrl(PageRequest.of(0, 2), canonicalUrl);
			}
			if (slice.isEmpty()) {
				return null;
			} 
			if (slice.getContent().size()>1) {
				log.error("multiple entries with same canonical (version) for "+theCanonicalUrl);
			}
			return slice.getContent().get(0);
		});
		return resourceEntity;
	}

	public NpmPackageVersionResourceEntity loadPackageAssetByUrl(String theCanonicalUrl, FhirVersionEnum theFhirVersion) {
		NpmPackageVersionResourceEntity resourceEntity  = new TransactionTemplate(myTxManager).execute(tx -> {
			String canonicalUrl = theCanonicalUrl;
			int versionSeparator = canonicalUrl.lastIndexOf('|');
			Slice<NpmPackageVersionResourceEntity> slice;
			if (versionSeparator != -1) {
				String canonicalVersion = canonicalUrl.substring(versionSeparator + 1);
				canonicalUrl = canonicalUrl.substring(0, versionSeparator);
				slice = myPackageVersionResourceDao.findByCanonicalUrlByCanonicalVersion(PageRequest.of(0, 2), theFhirVersion, canonicalUrl, canonicalVersion);
			} else {
				slice = myPackageVersionResourceDao.findCurrentVersionByCanonicalUrl(PageRequest.of(0, 2), theFhirVersion, canonicalUrl);
			}
			if (slice.isEmpty()) {
				return null;
			} 
			if (slice.getContent().size()>1) {
				log.error("multiple entries with same canonical (version) for "+theCanonicalUrl);
			}
			return slice.getContent().get(0);
		});
		return resourceEntity;
	}

	public NpmPackageVersionResourceEntity loadPackageAssetByLikeCanonicalCurrent(String canonical, FhirVersionEnum theFhirVersion) {
		if (!canonical.contains("|")) {
			if (canonical.contains("/")) {
				// remove resource id
				canonical = canonical.substring(0, canonical.lastIndexOf("/"));
				if (canonical.contains("/")) {
						// remove resource name
					String canonicalUrlLike = canonical.substring(0, canonical.lastIndexOf("/")) +"*";
					NpmPackageVersionResourceEntity resourceEntity  = new TransactionTemplate(myTxManager).execute(tx -> {
						Slice<NpmPackageVersionResourceEntity> slice = myPackageVersionResourceDao.findCurrentVersionByLikeCanonicalUrl(PageRequest.of(0, 1), theFhirVersion, canonicalUrlLike);
						if (slice.isEmpty()) {
							return null;
						} 
						return slice.getContent().get(0);
					});
					return resourceEntity;
				}
			}
		}
		return null;
	}

	/**
	 * returns a cached resource stored in the session cache
	 * @param resource
	 * @param id
	 * @return
	 */
	public IBaseResource getCachedResource(String resource, String id) {
		for (String sessionId : sessionCache.getSessionIds()) {
			MatchboxEngine engine = (MatchboxEngine) sessionCache.fetchSessionValidatorEngine(sessionId);
			IBaseResource res = engine.getCanonicalResourceById(resource, id);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	private MatchboxEngine createMatchboxEngine(final @NonNull MatchboxEngine engine,
															  final @Nullable String ig,
															  final @NonNull CliContext cliContext) throws MatchboxEngineCreationException {
		final String forIg = (ig != null) ? "for " + ig : "";
		log.info("Creating new validate engine {} with parameters {}", forIg, cliContext.hashCode());

		final MatchboxEngine validator;
		try { validator = new MatchboxEngine(engine); }
		catch (final IOException e) { throw new MatchboxEngineCreationException(e); }
		validator.setIgLoader(new IgLoaderFromJpaPackageCache(validator.getPcm(),
																				validator.getContext(),
																				validator.getVersion(),
																				validator.isDebug(),
																				this.myPackageCacheManager,
																				this.myNpmPackageVersionDao,
																				this.myDaoRegistry,
																				this.myBinaryStorageSvc,
																				this.myTxManager));
		if (ig != null) {
			try {
				validator.getIgLoader().loadIg(validator.getIgs(), validator.getBinaries(), ig, true);
			} catch (final IOException e){
				throw new IgLoadException(e);
			}
			log.info("Package Summary: "+validator.getContext().loadedPackageSummary());
		}

			this.configureValidationEngine(validator, cliContext);
			log.info("finished creating new validate engine for "+(ig!=null ? "for "+ig : "" ) +" with parameters "+cliContext.hashCode());

		return validator;
	}

	public String getIgPackage(CliContext cliContext) {
		String ig = null;
		if (cliContext.getFhirVersion().startsWith("4.0")) {
			ig = "hl7.fhir.r4.core#4.0.1";
		}
		if (cliContext.getFhirVersion().startsWith("4.3")) {
			ig = "hl7.fhir.r4b.core#4.3.0";
		}
		if (cliContext.getFhirVersion().startsWith("5.0")) {
			ig = "hl7.fhir.core#5.0.0";
		}
		return ig;
	}
			
	/**
	 * returns a matchbox-engine for the specified canonical with cliClontext parameters
	 * @param canonical URL to validate
	 * @param cliContext cliContext parameters
	 * @param create if true, create a new engine
	 * @param reload if true, reload the engine
	 * @return matchbox-engine
	 */
	public synchronized MatchboxEngine getMatchboxEngine(final @Nullable String canonical,
																		  @Nullable CliContext cliContext,
																		  final boolean create,
																		  final boolean reload) throws MatchboxEngineCreationException {
		while (!this.isInitialized()) {
			log.info("ValidationEngine is not yet initialized, waiting for initialization of packages");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error("error waiting for initialization", e);
			}
		}

		return this.getMatchboxEngineNotSynchronized(canonical, cliContext, create, reload);
	}

	/**
	 * returns a matchbox-engine for the specified canonical with cliClontext parameters. This method is not
	 * synchronized and does not wait for the 'initialized' flag. It should be used only for internal calls from the
	 * IG Provider load-all method.
	 *
	 * @param canonical  URL to validate
	 * @param cliContext cliContext parameters
	 * @param create     if true, create a new engine
	 * @param reload     if true, reload the engine
	 * @return a Matchbox engine.
	 * @throws MatchboxEngineCreationException if the engine cannot be created.
	 */
	public MatchboxEngine getMatchboxEngineNotSynchronized(final @Nullable String canonical,
																			 @Nullable CliContext cliContext,
																			 final boolean create,
																			 final boolean reload) throws MatchboxEngineCreationException {

		if (reload) {
			engine = null;
			log.info("setInitialized(false) from reload");
			this.setInitialized(false);
		}
		if (engine == null) {
			cliContext = new CliContext(this.cliContext);
			mainEngine = new MatchboxEngineBuilder().getEngine();
			mainEngine.setIgLoader(new IgLoaderFromJpaPackageCache(mainEngine.getPcm(),
																		 mainEngine.getContext(),
																		 mainEngine.getVersion(),
																		 mainEngine.isDebug(),
																		 this.myPackageCacheManager,
																		 this.myNpmPackageVersionDao,
																		 this.myDaoRegistry,
																		 this.myBinaryStorageSvc,
																		 this.myTxManager));
			if (cliContext.getFhirVersion().equals("4.0.1")) {
				log.debug("Preconfigure FHIR R4");
				try {
					mainEngine.getIgLoader().loadIg(mainEngine.getIgs(), mainEngine.getBinaries(), "hl7.terminology#5.3.0", true);
					mainEngine.loadPackage("hl7.terminology", "5.3.0");
					mainEngine.loadPackage("hl7.fhir.r4.core", "4.0.1");
					log.debug("Load R5 Specials");
					final var r5e = new R5ExtensionsLoader(mainEngine.getPcm(), mainEngine.getContext());
					r5e.load();
					log.debug("Load R5 Specials done");
					r5e.loadR5SpecialTypes(List.of("ActorDefinition",
															 "Requirements",
															 "SubscriptionTopic",
															 "TestPlan"));
				} catch (final IOException e) {
					throw new IgLoadException("Failed to load R5 specials", e);
				}
				log.debug("Load R5 Specials types");
				if (mainEngine.getCanonicalResource(
					"http://hl7.org/fhir/5.0/StructureDefinition/extension-DiagnosticReport.composition") == null) {
					log.error("Could not load R5 Specials");
				}

				this.configureValidationEngine(mainEngine, cliContext);
			}
			cliContext.setIg(this.getFhirCorePackage(cliContext));

			log.info("Cached default engine forever {} with parameters {}",
						(cliContext.getIg() != null ? "for " + cliContext.getIg() : ""),
						cliContext.hashCode());
			sessionCache.cacheSessionForEver("" + cliContext.hashCode(), mainEngine);
			cliContext.setIg(null); // otherwise we get for reloads the pacakge name instead a new one later  set ahdis/matchbox #144

			if (cliContext.getIgsPreloaded() != null && cliContext.getIgsPreloaded().length > 0) {
				for (String ig : cliContext.getIgsPreloaded()) {
					if (cliContext.getOnlyOneEngine()) {
						try {
							igLoader.loadIg(engine.getIgs(), engine.getBinaries(), ig, true);
						} catch (FHIRException | IOException e) {
							log.error("error generating matchbox engine due to igLoader", e);
						}
					} else {
						CliContext cliContextCp = new CliContext(this.cliContext);
						cliContextCp.setIg(ig); // set the ig in the cliContext that hashCode will be
						if (this.sessionCache.fetchSessionValidatorEngine("" + cliContextCp.hashCode()) == null) {
							MatchboxEngine created = this.createMatchboxEngine(engine, ig, cliContextCp);
							sessionCache.cacheSessionForEver("" + cliContextCp.hashCode(), created);
							log.info("cached validate engine forever" + (ig != null ? "for " + ig : "") + " with parameters " + cliContextCp.hashCode());
						}
					}
				}
			}

			if (cliContext.getOnlyOneEngine()) {
				log.info(
					"Only one engine will be provided with the preloaded ig's mentioned in application.yaml, cannot handle multiple versions of ig's, DEVELOPMENT ONLY MODE");
			}
		}

		if (cliContext == null) {
			cliContext = new CliContext(this.cliContext);
		}

		if (cliContext.getIg() == null) {
			if ("default".equals(canonical) || canonical == null || engine.getCanonicalResource(canonical) != null) {
				cliContext.setIg(this.getIgPackage(cliContext));
			} else {
				NpmPackageVersionResourceEntity npm = loadPackageAssetByUrl(canonical,
																								FhirVersionEnum.forVersionString(cliContext.getFhirVersion()));
				if (npm == null) {
					npm = loadPackageAssetByLikeCanonicalCurrent(canonical,
																				FhirVersionEnum.forVersionString(cliContext.getFhirVersion()));
				}
				if (npm != null) {
					String ig = npm.getPackageVersion().getPackageId() + "#" + npm.getPackageVersion().getVersionId();
					log.info("using ig " + ig + " for canonical url " + canonical);
					cliContext.setIg(ig); // set the ig in the cliContext that hashCode will be set
				} else {
					// lets try to find a package that contains the canonical with a different FHIR version (e.g. for mapping between versions)
					npm = loadPackageAssetByUrl(canonical);
					if (npm != null) {
						String ig = npm.getPackageVersion().getPackageId() + "#" + npm.getPackageVersion().getVersionId();
						cliContext.setFhirVersion(npm.getFhirVersion().getFhirVersionString());
						cliContext.setIg(ig); // set the ig in the cliContext that hashCode will be set
					}
				}
			}
		}

		if (reload) {
			log.info("setInitialized(true) from reload");
			this.setInitialized(true);
		}

		if (cliContext.getOnlyOneEngine()) {
			if (create && cliContext.getIg() != null) {
				try {
					engine.getIgLoader().loadIg(engine.getIgs(), engine.getBinaries(), cliContext.getIg(), true);
				} catch (FHIRException | IOException e) {
					log.error("error generating matchbox engine due to igLoader", e);
				}
			}
			return engine;
		}

		// check if we have already a validator in cache for that
		MatchboxEngine matchboxEngine = (MatchboxEngine) this.sessionCache.fetchSessionValidatorEngine("" + cliContext.hashCode());
		if ((matchboxEngine != null && reload == false)) {
			log.info("using cached validate engine" + (cliContext.getIg() != null ? "for " + cliContext.getIg() : "") + " with parameters " + cliContext.hashCode());
			return matchboxEngine;
		}

		// create a new validator and cache it temporarly
		if (create && cliContext.getIg() != null) {
			log.info("creating new cached validate engine " + (cliContext.getIg() != null ? "for " + cliContext.getIg() : "") + " with parameters " + cliContext.hashCode());
			MatchboxEngine baseEngine = engine;
			if (!cliContext.getFhirVersion().equals(baseEngine.getVersion())) {
				log.info("creating base engine for" + (cliContext.getIg() != null ? "for " + cliContext.getIg() : "") + " with parameters and fhir Version " + cliContext.getFhirVersion());
				try {
					baseEngine = new MatchboxEngineBuilder().getEngine();
					baseEngine.setVersion(cliContext.getFhirVersion());
				} catch (final Exception e) {
					log.error("error generating matchbox engine", e);
					return null;
				}
			}
			MatchboxEngine created = this.createMatchboxEngine(baseEngine, cliContext.getIg(), cliContext);
			sessionCache.cacheSession("" + cliContext.hashCode(), created);
			return created;
		}
		return null;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}


	/**
	 * Configures the validation engine with the cliContext parameters.
	 *
	 * @param validator Thr validation engine.
	 * @param cli       The cliContext parameters.
	 * @throws MatchboxEngineCreationException if the engine cannot be configured.
	 */
	private void configureValidationEngine(final MatchboxEngine validator,
														final CliContext cli) throws MatchboxEngineCreationException {
		log.info("Terminology server {}", cli.getTxServer());
		String txServer = cli.getTxServer();
		if ("n/a".equals(cli.getTxServer())) {
			txServer = null;
			validator.getContext().setCanRunWithoutTerminology(true);
			validator.getContext().setNoTerminologyServer(true);
		} else {
			// we need really to do it explicitly
			validator.getContext().setCanRunWithoutTerminology(false);
			validator.getContext().setNoTerminologyServer(false);
		}
		try {
			final String txver = validator.setTerminologyServer(txServer, null, FhirPublication.R4);
			log.info("Version of the terminology server: {}", txver);
		} catch (final IOException | URISyntaxException e) {
			throw new TerminologyServerUnreachableException(e);
		}

		validator.setDebug(cli.isDoDebug());
		validator.getContext().setLogger(new EngineLoggingService());

		validator.setQuestionnaireMode(cli.getQuestionnaireMode());
		validator.setLevel(cli.getLevel());
		validator.setDoNative(cli.isDoNative());
		validator.setHintAboutNonMustSupport(cli.isHintAboutNonMustSupport());
//			for (String s : cliContext.getExtensions()) {
//			if ("any".equals(s)) {
		validator.setAnyExtensionsAllowed(true);
//			} else {
//				validator.getExtensionDomains().add(s);
//			}
//			}
		validator.setLanguage(cli.getLang());
		validator.setLocale(Locale.forLanguageTag(cli.getLocale()));
		if (cli.getSnomedCT() != null) {
			validator.setSnomedExtension(cli.getSnomedCT());
		}
		validator.setDisplayWarnings(cli.isDisplayIssuesAreWarnings());
		validator.setAssumeValidRestReferences(cli.isAssumeValidRestReferences());
		validator.setShowMessagesFromReferences(cli.isShowMessagesFromReferences());
		validator.setDoImplicitFHIRPathStringConversion(cli.isDoImplicitFHIRPathStringConversion());
		validator.setHtmlInMarkdownCheck(cli.getHtmlInMarkdownCheck());
		validator.setNoExtensibleBindingMessages(cli.isNoExtensibleBindingMessages());
		validator.setNoUnicodeBiDiControlChars(cli.isNoUnicodeBiDiControlChars());
		validator.setNoInvariantChecks(cli.isNoInvariants());
		validator.setWantInvariantInMessage(cli.isWantInvariantsInMessages());
		validator.setSecurityChecks(cli.isSecurityChecks());
		validator.setCrumbTrails(cli.isCrumbTrails());
		validator.setForPublication(cli.isForPublication());
		validator.setShowTimes(true);
		validator.setAllowExampleUrls(cli.isAllowExampleUrls());

		final var fetcher = new StandAloneValidatorFetcher(
			validator.getPcm(),
			validator.getContext(),
			new IPackageInstaller() {
				// https://github.com/ahdis/matchbox/issues/67
				@Override
				public boolean packageExists(String id, String ver) throws IOException, FHIRException {
					return false;
				}

				@Override
				public void loadPackage(String id, String ver) throws IOException, FHIRException {
				}
			});
		validator.setFetcher(fetcher);
		validator.getContext().setLocator(fetcher);

		// validator.getBundleValidationRules().addAll(cliContext.getBundleValidationRules());
		validator.setJurisdiction(CodeSystemUtilities.readCoding(cli.getJurisdiction()));
		// TerminologyCache.setNoCaching(cliContext.isNoInternalCaching());
	}
}
