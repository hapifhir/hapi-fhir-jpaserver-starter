package ch.ahdis.matchbox.util;

import java.io.File;
import java.io.IOException;
import java.util.*;

import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.EngineLoggingService;
import ch.ahdis.matchbox.config.MatchboxFhirContextProperties;
import ch.ahdis.matchbox.engine.exception.IgLoadException;
import ch.ahdis.matchbox.engine.exception.MatchboxEngineCreationException;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import ch.ahdis.matchbox.engine.exception.TerminologyServerException;

import ch.ahdis.matchbox.packages.IgLoaderFromJpaPackageCache;
import ch.ahdis.matchbox.util.http.HttpRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.conformance.R5ExtensionsLoader;
import org.hl7.fhir.r5.terminologies.CodeSystemUtilities;
import org.hl7.fhir.r5.terminologies.client.TerminologyClientContext;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.validation.service.StandAloneValidatorFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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


public class MatchboxEngineSupport {
	
	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchboxEngineSupport.class);

	private static MatchboxEngine mainEngine = null;
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

	private CliContext cliContext;

	private final MatchboxFhirContextProperties matchboxFhirContextProperties;

	private final FhirVersionEnum serverFhirVersion;

	public MatchboxEngineSupport(final MatchboxFhirContextProperties matchboxFhirContextProperties,
										  final CliContext cliContext,
										  @Value("${hapi.fhir.fhir_version}") final FhirVersionEnum serverFhirVersion) {
		this.sessionCache = new EngineSessionCache();
		this.matchboxFhirContextProperties = Objects.requireNonNull(matchboxFhirContextProperties);
		this.serverFhirVersion = serverFhirVersion;
		this.cliContext = cliContext;
		this.cliContext.setFhirVersion(this.serverFhirVersion.getFhirVersionString());
	}

	public CliContext getClientContext() {
		return this.cliContext;
	}

	public FhirVersionEnum getServerFhirVersion() {
		return this.serverFhirVersion;
	}

	public HttpRequestWrapper createWrapper(final HttpServletRequest request,
														 final HttpServletResponse response) throws IOException {
		return new HttpRequestWrapper(request, response, this.serverFhirVersion);
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
	 * @param cliRequestedContext cliContext parameters
	 * @param create     if true, create a new engine
	 * @param reload     if true, reload the engine
	 * @return a Matchbox engine.
	 * @throws MatchboxEngineCreationException if the engine cannot be created.
	 */
	public MatchboxEngine getMatchboxEngineNotSynchronized(final @Nullable String canonical,
																			 @Nullable CliContext cliRequestedContext,
																			 final boolean create,
																			 final boolean reload) throws MatchboxEngineCreationException {

		if (reload) {
			mainEngine = null;
			this.setInitialized(false);
		}
		if (mainEngine == null) {
			CliContext cliContextMain = new CliContext(this.cliContext);
			if (this.serverFhirVersion == FhirVersionEnum.R4) {
				log.debug("Preconfigure FHIR R4");
			    mainEngine = new MatchboxEngineBuilder().withXVersion(cliContextMain.getXVersion()).getEngineR4();
				try {
					mainEngine.setIgLoader(new IgLoaderFromJpaPackageCache(mainEngine.getPcm(),
																			mainEngine.getContext(),
																			mainEngine.getVersion(),
																			mainEngine.isDebug(),
																			this.myPackageCacheManager,
																			this.myNpmPackageVersionDao,
																			this.myDaoRegistry,
																			this.myBinaryStorageSvc,
																			this.myTxManager));
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
				this.configureValidationEngine(mainEngine, cliContextMain);
			} else if (this.serverFhirVersion == FhirVersionEnum.R4B) {
				log.debug("Preconfigure FHIR R4B");
			    mainEngine = new MatchboxEngineBuilder().withXVersion(cliContextMain.getXVersion()).getEngineR4B();
				mainEngine.setIgLoader(new IgLoaderFromJpaPackageCache(mainEngine.getPcm(),
				mainEngine.getContext(),
				mainEngine.getVersion(),
				mainEngine.isDebug(),
				this.myPackageCacheManager,
				this.myNpmPackageVersionDao,
				this.myDaoRegistry,
				this.myBinaryStorageSvc,
				this.myTxManager));
				this.configureValidationEngine(mainEngine, cliContextMain);
			} else if (this.serverFhirVersion == FhirVersionEnum.R5) {
				log.debug("Preconfigure FHIR R5");
			    mainEngine = new MatchboxEngineBuilder().withXVersion(cliContextMain.getXVersion()).getEngineR5();
				mainEngine.setIgLoader(new IgLoaderFromJpaPackageCache(mainEngine.getPcm(),
				mainEngine.getContext(),
				mainEngine.getVersion(),
				mainEngine.isDebug(),
				this.myPackageCacheManager,
				this.myNpmPackageVersionDao,
				this.myDaoRegistry,
				this.myBinaryStorageSvc,
				this.myTxManager));
				this.configureValidationEngine(mainEngine, cliContextMain);
			} else {
				throw new MatchboxUnsupportedFhirVersionException("getMatchboxEngineNotSynchronized", this.serverFhirVersion);
			}
			cliContextMain.setIg(this.getFhirCorePackage(cliContextMain));

			log.info("Cached default engine forever {} with parameters {}",
						(cliContextMain.getIg() != null ? "for " + cliContextMain.getIg() : ""),
						cliContextMain.hashCode());
			this.sessionCache.cacheSessionForEver("" + cliContextMain.hashCode(), mainEngine);
			this.cliContext.setIg(null); // otherwise we get for reloads the pacakge name instead a new one later  set ahdis/matchbox #144

			if (cliContextMain.getIgsPreloaded() != null) {
				for (final String ig : cliContextMain.getIgsPreloaded()) {
					if (cliContextMain.getOnlyOneEngine()) {
						try {
							mainEngine.getIgLoader().loadIg(mainEngine.getIgs(), mainEngine.getBinaries(), ig, true);
						} catch (final Exception e) {
							log.error("Error generating matchbox engine due to igLoader", e);
						}
					} else {
						CliContext cliContextCp = new CliContext(cliContextMain);
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

			if (cliContextMain.getOnlyOneEngine()) {
				log.warn(
					"Only one engine will be provided with the preloaded ig's mentioned in application.yaml, cannot handle multiple versions of ig's, DEVELOPMENT ONLY MODE");
			}
		}

		if (cliRequestedContext == null || cliRequestedContext == this.cliContext) {
			cliRequestedContext = new CliContext(this.cliContext);
		}

		if (cliRequestedContext.getIg() == null) {
			if ("default".equals(canonical) || canonical == null || mainEngine.getCanonicalResource(canonical, cliRequestedContext.getFhirVersion()) != null) {
				cliRequestedContext.setIg(this.getFhirCorePackage(cliRequestedContext));
			} else {
				NpmPackageVersionResourceEntity npm = loadPackageAssetByUrl(canonical,
																								FhirVersionEnum.forVersionString(cliRequestedContext.getFhirVersion()));
				if (npm == null) {
					npm = loadPackageAssetByUrl(canonical);
				}
				if (npm != null) {
					String ig = npm.getPackageVersion().getPackageId() + "#" + npm.getPackageVersion().getVersionId();
					cliRequestedContext.setFhirVersion(npm.getFhirVersion().getFhirVersionString());
					cliRequestedContext.setIg(ig); // set the ig in the cliContext that hashCode will be set
				}
			}
		}

		if (reload) {
			this.setInitialized(true);
		}

		if (cliRequestedContext.getOnlyOneEngine()) {
			if (create && cliRequestedContext.getIg() != null) {
				try {
					mainEngine.getIgLoader().loadIg(mainEngine.getIgs(), mainEngine.getBinaries(), cliRequestedContext.getIg(), true);
				} catch (final Exception e) {
					log.error("Error generating matchbox engine due to igLoader", e);
				}
			}
			return mainEngine;
		}
		
		// check if we have already a validator in cache for that
		final var matchboxEngine =
			(MatchboxEngine) this.sessionCache.fetchSessionValidatorEngine("" + cliRequestedContext.hashCode());
		if (matchboxEngine != null && !reload) {
			log.info("Using cached validate engine {} with parameters {}",
						(cliRequestedContext.getIg() != null ? "for " + cliRequestedContext.getIg() : ""),
						cliRequestedContext.hashCode());
			// Runtime runtime = Runtime.getRuntime();
			// runtime.gc();
			return matchboxEngine;
		}

		// create a new validator and cache it temporarily
		if (create && cliRequestedContext.getIg() != null) {
			log.info("Creating new cached validate engine {} with parameters {}",
						 (cliRequestedContext.getIg() != null ? "for " + cliRequestedContext.getIg() : ""),
						 cliRequestedContext.hashCode());
			MatchboxEngine baseEngine = mainEngine;
			if (!cliRequestedContext.getFhirVersion().equals(baseEngine.getVersion())) {
				log.debug("Creating base engine for {} with parameters and fhir Version {}",
						(cliRequestedContext.getIg() != null ? "for " + cliRequestedContext.getIg() : ""),
						cliRequestedContext.getFhirVersion());
				try {
					switch (cliRequestedContext.getFhirVersion()) {
						case "5.0.0":
							baseEngine = new MatchboxEngineBuilder().withXVersion(cliRequestedContext.getXVersion()).getEngineR5();
							break;
						case "4.3.0":
							baseEngine = new MatchboxEngineBuilder().withXVersion(cliRequestedContext.getXVersion()).getEngineR4B();
							break;
						case "4.0.1":
							baseEngine = new MatchboxEngineBuilder().withXVersion(cliRequestedContext.getXVersion()).getEngineR4();
							break;
						default:
							log.error("FHIR version not yet supported in mixed mode, needs to be added for version "
									+ cliRequestedContext.getFhirVersion());
							return null;
					}
				} catch (final Exception e) {
					log.error("Error generating matchbox engine", e);
					return null;
				}
			}
			final var created = this.createMatchboxEngine(baseEngine, cliRequestedContext.getIg(), cliRequestedContext);
			this.sessionCache.cacheSession("" + cliRequestedContext.hashCode(), created);
			// Runtime runtime = Runtime.getRuntime();
			// runtime.gc();
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

	private String getTxCachePath(String txServer) {
		String path = System.getenv("HOME");
		if (path == null || path.isEmpty()) {
			path = System.getProperty("user.dir");
		}
		String md5Hex = DigestUtils.md5Hex(txServer).toLowerCase();
		if (path==null) {
			path = "";
		}
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		return path +"txCache" +File.separator + md5Hex;
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
		if (cli.getTxServer() == null) {
			throw new MatchboxEngineCreationException("Terminology server is not set, you need to set it in the configuration file.");
		}
		if ("n/a".equals(cli.getTxServer())) {
			validator.getContext().setCanRunWithoutTerminology(true);
			validator.getContext().setNoTerminologyServer(true);
		} else {
			// we need really to do it explicitly
			validator.getContext().setCanRunWithoutTerminology(false);
			validator.getContext().setNoTerminologyServer(false);

			try {
				TerminologyClientContext.setAllowNonConformantServers(true);
				TerminologyClientContext.setCanAllowNonConformantServers(true);
				// Currently all terminology clients are to R4 for version greater than R4
				final String txver = validator.setTerminologyServer(cli.getTxServer(), cli.getTxLog(), FhirPublication.R4, cli.isTxUseEcosystem());
				log.debug("Version of the terminology server: {}", txver);
			} catch (final Exception e) {
				throw new TerminologyServerException("Error while setting the terminology server: " + e.getMessage(), e);
			}
			try {
				validator.initTxCache(cli.getTxServerCache() ? getTxCachePath(cli.getTxServer()) : null);
			} catch (final Exception e) {
				throw new TerminologyServerException("Error while setting the terminology server cache: " + getTxCachePath(cli.getTxServer()), e);
			}

			try {
				if (cli.isClearTxCache()) {
					validator.getContext().getTxCache().clear();
			  	}
			} catch (final Exception e) {
				throw new TerminologyServerException("Error while setting while trying to clear the terminology cache", e);
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
		validator.setShowMessageIds(cliContext.isShowMessageIds());
		validator.setForPublication(cli.isForPublication());
		validator.setShowTimes(true);
		validator.setAllowExampleUrls(cli.isAllowExampleUrls());
		validator.setCheckIPSCodes(cli.isCheckIpsCodes());
		if (cli.getBundle() != null) {
			validator.setQuestionnaireMode(cli.getQuestionnaireMode());
			String[] bundle = cli.getBundle().split(" ");
			if (bundle.length != 2) {
				log.error("Bundle parameter must have two values, the rule and the profile, ignoring the bundle parameter");
			} else {
				String rule = bundle[0];
				String profile = bundle[1];
				validator.getBundleValidationRules().add(new org.hl7.fhir.r5.utils.validation.BundleValidationRule().setRule(rule).setProfile(profile));
			}
		}
		if (!cli.isDisableDefaultResourceFetcher()) {
			StandAloneValidatorFetcher fetcher = new StandAloneValidatorFetcher(validator.getPcm(), validator.getContext(),
					validator);
			validator.setFetcher(fetcher);
			validator.getContext().setLocator(fetcher);
			validator.setPolicyAdvisor(fetcher);
			if (cli.isCheckReferences()) {
				fetcher.setReferencePolicy(ReferenceValidationPolicy.CHECK_VALID);
			} else {
				fetcher.setReferencePolicy(ReferenceValidationPolicy.IGNORE);
			}
			fetcher.setResolutionContext(cli.getResolutionContext());
		} else {
			validator.setPolicyAdvisor(new ValidationPolicyAdvisor(ReferenceValidationPolicy.CHECK_VALID));
			// https://github.com/ahdis/matchbox/issues/334
			// DisabledValidationPolicyAdvisor fetcher = new DisabledValidationPolicyAdvisor();
			// validator.setPolicyAdvisor(fetcher);
			// refpol = ReferenceValidationPolicy.CHECK_TYPE_IF_EXISTS;
		}
		validator.setJurisdiction(CodeSystemUtilities.readCoding(cli.getJurisdiction()));
		// TerminologyCache.setNoCaching(cliContext.isNoInternalCaching());

		// Configure which warnings will be suppressed in the validation results
		final Map<String, List<String>> suppressedWarnings =
			Objects.requireNonNullElseGet(this.matchboxFhirContextProperties.getSuppressWarnInfo(),
													HashMap::new);
		final Map<String, List<String>> suppressedError =
			Objects.requireNonNullElseGet(this.matchboxFhirContextProperties.getSuppressError(),
													HashMap::new);
		if (cli.getOnlyOneEngine()) {
			// If we only have one engine, then ignore all warnings that are defined in the configuration file
			suppressedWarnings.values().stream()
				.flatMap(List::stream)
				.forEach(pattern -> this.addSuppressedWarnInfoToEngine(pattern, validator));
			suppressedError.values().stream()
				.flatMap(List::stream)
				.forEach(pattern -> this.addSuppressedErrorToEngine(pattern, validator));		
		} else {
			// Otherwise, only ignore the warnings that are defined for the IGs that have been loaded in the engine
			// Note: we remove the hash in the package, because the hash is also removed when reading the YAML
			//       configuration file.
			validator.getContext().getLoadedPackages().stream()
				.map(pkg -> pkg.replace("#", ""))
				.forEach(ig -> {
					suppressedWarnings.getOrDefault(ig, Collections.emptyList())
						.forEach(pattern -> this.addSuppressedWarnInfoToEngine(pattern, validator));
					suppressedError.getOrDefault(ig, Collections.emptyList())
						.forEach(pattern -> this.addSuppressedErrorToEngine(pattern, validator));
				});

			validator.getContext().getLoadedPackages().stream().filter(pkg -> pkg.contains("#"))
				.map(pkg -> pkg.substring(0, pkg.indexOf("#")))
				.forEach(ig -> {
					suppressedWarnings.getOrDefault(ig, Collections.emptyList())
						.forEach(pattern -> this.addSuppressedWarnInfoToEngine(pattern, validator));
					suppressedError.getOrDefault(ig, Collections.emptyList())
						.forEach(pattern -> this.addSuppressedErrorToEngine(pattern, validator));
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

	private void addSuppressedErrorToEngine(final @NonNull String pattern,
															 final @NonNull MatchboxEngine engine) {
		String pathMessageid[] = pattern.split("!");
		if (pathMessageid.length == 2) {
			engine.addSuppressedError(pathMessageid[0], pathMessageid[1]);
			return;
		} else {
			log.error("Error in the configuration file, the pattern {} is not valid, it should be path!messageId", pattern);
		}
	}

	public INpmPackageVersionResourceDao getMyPackageVersionResourceDao() {
		return this.myPackageVersionResourceDao;
	}

	public PlatformTransactionManager getMyTxManager() {
		return this.myTxManager;
	}
}
