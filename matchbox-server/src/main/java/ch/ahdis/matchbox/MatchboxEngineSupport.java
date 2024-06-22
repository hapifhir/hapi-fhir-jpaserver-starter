package ch.ahdis.matchbox;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.*;

import ch.ahdis.matchbox.config.MatchboxFhirContextProperties;
import ch.ahdis.matchbox.engine.exception.IgLoadException;
import ch.ahdis.matchbox.engine.exception.MatchboxEngineCreationException;
import ch.ahdis.matchbox.engine.exception.TerminologyServerException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.conformance.R5ExtensionsLoader;
import org.hl7.fhir.r5.terminologies.CodeSystemUtilities;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.validation.cli.services.IPackageInstaller;
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
import ch.ahdis.matchbox.engine.ValidationPolicyAdvisor;
import ch.ahdis.matchbox.util.EngineSessionCache;


public class MatchboxEngineSupport {
	
	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxEngineSupport.class);

	public static MatchboxEngine mainEngine = null;
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

	private final MatchboxFhirContextProperties matchboxFhirContextProperties;

	public MatchboxEngineSupport(final MatchboxFhirContextProperties matchboxFhirContextProperties) {
		this.sessionCache = new EngineSessionCache();
		this.matchboxFhirContextProperties = Objects.requireNonNull(matchboxFhirContextProperties);
	}

	public CliContext getClientContext() {
		return this.cliContext;
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
	public IBaseResource getCachedResource(final String resource, final @NonNull String id) {
		for (final String sessionId : this.sessionCache.getSessionIds()) {
			final var engine = (MatchboxEngine) this.sessionCache.fetchSessionValidatorEngine(sessionId);
			final IBaseResource res = engine.getCanonicalResourceById(resource, id);
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
		catch (final Exception e) { throw new MatchboxEngineCreationException(e); }
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
			} catch (final Exception e){
				throw new IgLoadException(e);
			}
		}
		log.debug("Package Summary: {}", validator.getContext().loadedPackageSummary());

		this.configureValidationEngine(validator, cliContext);
		log.debug("Finished creating new validate engine for {} with parameters {}", forIg, cliContext.hashCode());

		return validator;
	}

	/**
	 * Returns the FHIR core package for the specified FHIR version in the cliContext, or {@code null} if the FHIR
	 * version is not supported.
	 */
	public @Nullable String getFhirCorePackage(final @NonNull CliContext cliContext) {
		if (cliContext.getFhirVersion().startsWith("4.0")) {
			return "hl7.fhir.r4.core#4.0.1";
		}
		if (cliContext.getFhirVersion().startsWith("4.3")) {
			return "hl7.fhir.r4b.core#4.3.0";
		}
		if (cliContext.getFhirVersion().startsWith("5.0")) {
			return "hl7.fhir.r5.core#5.0.0";
		}
		return null;
	}
			
	/**
	 * Returns a Matchbox engine for the specified canonical with cliClontext parameters. It is synchronized and waits
	 * for the 'initialized' flag.
	 *
	 * @param canonical  URL to validate
	 * @param cliContext cliContext parameters
	 * @param create     if true, create a new engine
	 * @param reload     if true, reload the engine
	 * @return a Matchbox engine.
	 * @throws MatchboxEngineCreationException if the engine cannot be created.
	 */
	public synchronized MatchboxEngine getMatchboxEngine(final @Nullable String canonical,
																		  @Nullable CliContext cliContext,
																		  final boolean create,
																		  final boolean reload) throws MatchboxEngineCreationException {
		while (!this.isInitialized()) {
			log.info("ValidationEngine is not yet initialized, waiting for initialization of packages");
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {
				log.error("error waiting for initialization", e);
			}
		}

		return this.getMatchboxEngineNotSynchronized(canonical, cliContext, create, reload);
	}

	/**
	 * Returns a Matchbox engine for the specified canonical with cliClontext parameters. This method is not
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
			mainEngine = null;
			this.setInitialized(false);
		}
		if (mainEngine == null) {
			cliContext = new CliContext(this.cliContext);
			mainEngine = new MatchboxEngineBuilder().withVersion(this.cliContext.getFhirVersion()).getEngine();
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
					mainEngine.getIgLoader().loadIg(mainEngine.getIgs(), mainEngine.getBinaries(), "hl7.terminology#5.4.0", true);
					mainEngine.loadPackage("hl7.terminology", "5.4.0");
					mainEngine.loadPackage("hl7.fhir.r4.core", "4.0.1");
					log.debug("Load R5 Specials");
					final var r5e = new R5ExtensionsLoader(mainEngine.getPcm(), mainEngine.getContext());
					r5e.load();
					log.debug("Load R5 Specials done");
					r5e.loadR5SpecialTypes(List.of("ActorDefinition",
															 "Requirements",
															 "SubscriptionTopic",
															 "TestPlan"));
				} catch (final Exception e) {
					throw new IgLoadException("Failed to load R5 specials", e);
				}
				log.debug("Load R5 Specials types");
				this.configureValidationEngine(mainEngine, cliContext);
			} else if (cliContext.getFhirVersion().equals("5.0.0")) {
				log.debug("Preconfigure FHIR R5");
				try {
					mainEngine.setVersion("5.0.0");
					mainEngine.getIgLoader().loadIg(mainEngine.getIgs(), mainEngine.getBinaries(), "hl7.fhir.r5.core#5.0.0", true);
					mainEngine.loadPackage("hl7.terminology", "5.4.0");
					mainEngine.loadPackage("hl7.fhir.uv.extensions", "1.0.0");
				} catch (final Exception e) {
					throw new IgLoadException("Failed to load R5", e);
				}
				this.configureValidationEngine(mainEngine, cliContext);
			}
			cliContext.setIg(this.getFhirCorePackage(cliContext));

			log.info("Cached default engine forever {} with parameters {}",
						(cliContext.getIg() != null ? "for " + cliContext.getIg() : ""),
						cliContext.hashCode());
			this.sessionCache.cacheSessionForEver("" + cliContext.hashCode(), mainEngine);
			cliContext.setIg(null); // otherwise we get for reloads the pacakge name instead a new one later  set ahdis/matchbox #144

			if (cliContext.getIgsPreloaded() != null) {
				for (final String ig : cliContext.getIgsPreloaded()) {
					if (cliContext.getOnlyOneEngine()) {
						try {
							mainEngine.getIgLoader().loadIg(mainEngine.getIgs(), mainEngine.getBinaries(), ig, true);
						} catch (final Exception e) {
							log.error("Error generating matchbox engine due to igLoader", e);
						}
					} else {
						CliContext cliContextCp = new CliContext(this.cliContext);
						cliContextCp.setIg(ig); // set the ig in the cliContext that hashCode will be
						if (this.sessionCache.fetchSessionValidatorEngine("" + cliContextCp.hashCode()) == null) {
							MatchboxEngine created = this.createMatchboxEngine(mainEngine, ig, cliContextCp);
							this.sessionCache.cacheSessionForEver("" + cliContextCp.hashCode(), created);
							log.info("Cached validate engine forever {} with parameters {}",
										(ig != null ? "for " + ig : ""),
										cliContextCp.hashCode());
						}
					}
				}
			}

			if (cliContext.getOnlyOneEngine()) {
				log.warn(
					"Only one engine will be provided with the preloaded ig's mentioned in application.yaml, cannot handle multiple versions of ig's, DEVELOPMENT ONLY MODE");
			}
		}

		if (cliContext == null) {
			cliContext = new CliContext(this.cliContext);
		}

		if (cliContext.getIg() == null) {
			if ("default".equals(canonical) || canonical == null || mainEngine.getCanonicalResource(canonical, cliContext.getFhirVersion()) != null) {
				cliContext.setIg(this.getFhirCorePackage(cliContext));
			} else {
				NpmPackageVersionResourceEntity npm = loadPackageAssetByUrl(canonical,
																								FhirVersionEnum.forVersionString(cliContext.getFhirVersion()));
				if (npm == null) {
					npm = loadPackageAssetByUrl(canonical);
				}
				if (npm != null) {
					String ig = npm.getPackageVersion().getPackageId() + "#" + npm.getPackageVersion().getVersionId();
					cliContext.setFhirVersion(npm.getFhirVersion().getFhirVersionString());
					cliContext.setIg(ig); // set the ig in the cliContext that hashCode will be set
				}
			}
		}

		if (reload) {
			this.setInitialized(true);
		}

		if (cliContext.getOnlyOneEngine()) {
			if (create && cliContext.getIg() != null) {
				try {
					mainEngine.getIgLoader().loadIg(mainEngine.getIgs(), mainEngine.getBinaries(), cliContext.getIg(), true);
				} catch (final Exception e) {
					log.error("Error generating matchbox engine due to igLoader", e);
				}
			}
			return mainEngine;
		}

		// check if we have already a validator in cache for that
		final var matchboxEngine =
			(MatchboxEngine) this.sessionCache.fetchSessionValidatorEngine("" + cliContext.hashCode());
		if (matchboxEngine != null && !reload) {
			log.debug("Using cached validate engine {} with parameters {}",
						(cliContext.getIg() != null ? "for " + cliContext.getIg() : ""),
						cliContext.hashCode());
			return matchboxEngine;
		}

		// create a new validator and cache it temporarily
		if (create && cliContext.getIg() != null) {
			log.debug("Creating new cached validate engine {} with parameters {}",
						 (cliContext.getIg() != null ? "for " + cliContext.getIg() : ""),
						 cliContext.hashCode());
			MatchboxEngine baseEngine = mainEngine;
			if (!cliContext.getFhirVersion().equals(baseEngine.getVersion())) {
				log.debug("Creating base engine for {} with parameters and fhir Version {}",
						(cliContext.getIg() != null ? "for " + cliContext.getIg() : ""),
						cliContext.getFhirVersion());
				try {
					switch (cliContext.getFhirVersion()) {
						case "5.0.0":
							baseEngine = new MatchboxEngineBuilder().getEngineR5();
							break;
						case "4.0.1":
							baseEngine = new MatchboxEngineBuilder().getEngineR4();
							break;
						default:
							log.error("FHIR version not yet supported in mixed mode, needs to be added for version "
									+ cliContext.getFhirVersion());
							return null;
					}
				} catch (final Exception e) {
					log.error("Error generating matchbox engine", e);
					return null;
				}
			}
			final var created = this.createMatchboxEngine(baseEngine, cliContext.getIg(), cliContext);
			sessionCache.cacheSession("" + cliContext.hashCode(), created);
			return created;
		}
		return null;
	}

	public String getSessionId(final MatchboxEngine engine) {
		return this.sessionCache.getSessionId(engine);
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
		if ("n/a".equals(cli.getTxServer())) {
			validator.getContext().setCanRunWithoutTerminology(true);
			validator.getContext().setNoTerminologyServer(true);
		} else {
			// we need really to do it explicitly
			validator.getContext().setCanRunWithoutTerminology(false);
			validator.getContext().setNoTerminologyServer(false);

			try {
				final String txver = validator.setTerminologyServer(cli.getTxServer(), null, FhirPublication.R4, true);
				log.debug("Version of the terminology server: {}", txver);
			} catch (final Exception e) {
				throw new TerminologyServerException("Error while setting the terminology server: " + e.getMessage(), e);
			}
		}

		validator.setDebug(cli.isDoDebug());
		validator.getContext().setLogger(new EngineLoggingService());

		validator.setQuestionnaireMode(cli.getQuestionnaireMode());
		validator.setLevel(cli.getLevel());
		validator.setDoNative(cli.isDoNative());
		validator.setHintAboutNonMustSupport(cli.isHintAboutNonMustSupport());
		validator.setAnyExtensionsAllowed(false);
		for (String s : cli.getExtensions()) {
			if ("any".equals(s)) {
				validator.setAnyExtensionsAllowed(true);
			} else {	
				validator.getExtensionDomains().add(s);
			}
		}
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

		validator.setPolicyAdvisor(new ValidationPolicyAdvisor(ReferenceValidationPolicy.CHECK_VALID));
		// validator.getBundleValidationRules().addAll(cliContext.getBundleValidationRules());
		validator.setJurisdiction(CodeSystemUtilities.readCoding(cli.getJurisdiction()));
		// TerminologyCache.setNoCaching(cliContext.isNoInternalCaching());

		// Configure which warnings will be suppressed in the validation results
		final Map<String, List<String>> suppressedWarnings =
			Objects.requireNonNullElseGet(this.matchboxFhirContextProperties.getSuppressWarnInfo(),
													HashMap::new);
		if (cli.getOnlyOneEngine()) {
			// If we only have one engine, then ignore all warnings that are defined in the configuration file
			suppressedWarnings.values().stream()
				.flatMap(List::stream)
				.forEach(pattern -> this.addSuppressedWarnInfoToEngine(pattern, validator));
		} else {
			// Otherwise, only ignore the warnings that are defined for the IGs that have been loaded in the engine
			// Note: we remove the hash in the package, because the hash is also removed when reading the YAML
			//       configuration file.
			validator.getContext().getLoadedPackages().stream()
				.map(pkg -> pkg.replace("#", ""))
				.forEach(ig -> {
					suppressedWarnings.getOrDefault(ig, Collections.emptyList())
						.forEach(pattern -> this.addSuppressedWarnInfoToEngine(pattern, validator));
				});

			validator.getContext().getLoadedPackages().stream().filter(pkg -> pkg.contains("#"))
				.map(pkg -> pkg.substring(0, pkg.indexOf("#")))
				.forEach(ig -> {
					suppressedWarnings.getOrDefault(ig, Collections.emptyList())
						.forEach(pattern -> this.addSuppressedWarnInfoToEngine(pattern, validator));
				});
		}
		log.debug("Added {} suppressed warnings for these IGs", validator.getSuppressedWarnInfoPatterns().size());
	}

	private void addSuppressedWarnInfoToEngine(final @NonNull String pattern,
															 final @NonNull MatchboxEngine engine) {
		if (pattern.startsWith("regex:")) {
			// If it starts with "regex:", then remove the prefix and add it as a regex pattern
			engine.addSuppressedWarnInfoPattern(pattern.substring(6));
			return;
		}
		// Otherwise, add it as a simple string pattern
		engine.addSuppressedWarnInfo(pattern);
	}
}
