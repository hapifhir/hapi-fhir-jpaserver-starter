package ch.ahdis.fhir.hapi.jpa.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import ca.uhn.fhir.jpa.rp.r4.ImplementationGuideResourceProvider;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.Enumerations.FHIRVersion;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionEntity;
import ca.uhn.fhir.jpa.packages.PackageDeleteOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RawParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SearchContainedModeEnum;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.CompositeAndListParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriAndListParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.MatchboxEngineSupport;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.cli.VersionUtil;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;

/**
 * $load and $load-all Operation for ImplementationGuide Resource (R4)
 *
 */
@DisallowConcurrentExecution
public class ImplementationGuideProviderR4 extends ImplementationGuideResourceProvider
		implements ApplicationContextAware, MatchboxImplementationGuideProvider {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImplementationGuideProviderR4.class);

	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;

	@Autowired
	private CliContext cliContext;

	@Override
	public MethodOutcome delete(HttpServletRequest theRequest, IIdType theResource, String theConditional,
			RequestDetails theRequestDetails) {
		ImplementationGuide guide = new ImplementationGuide();
		int pos = theResource.getIdPart().lastIndexOf("-");
		String version = theResource.getIdPart().substring(pos + 1);
		int space = version.indexOf(' ');
		if (space > 0) {
			version = version.substring(0, space);
		}
		guide.setVersion(version);
		String name = theResource.getIdPart().substring(0, pos);
		guide.setName(name);
		MethodOutcome outcome = new MethodOutcome();
		OperationOutcome oo = uninstall(guide);
		outcome.setOperationOutcome(oo);
		return outcome;
	}

	@Override
	public MethodOutcome update(HttpServletRequest theRequest, ImplementationGuide theResource, IIdType theId,
			String theConditional, RequestDetails theRequestDetails) {
		OperationOutcome oo = load(theResource);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setOperationOutcome(oo);
		return outcome;
	}

	@Override
	public MethodOutcome create(HttpServletRequest theRequest, ImplementationGuide theResource, String theConditional,
			RequestDetails theRequestDetails) {
		OperationOutcome oo = load(theResource);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setOperationOutcome(oo);
		return outcome;
	}

	@Autowired
	MatchboxPackageInstallerImpl packageInstallerSvc;

	@Autowired
	AppProperties appProperties;

	@Autowired
	private INpmPackageVersionDao myPackageVersionDao;

	@Autowired
	private PlatformTransactionManager myTxManager;

	public OperationOutcome getOperationOutcome(PackageInstallOutcomeJson pkgOutcome) {
		if (pkgOutcome == null) {
			return null;
		}
		OperationOutcome outcome = new OperationOutcome();
		for (String message : pkgOutcome.getMessage()) {
			outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING)
					.setDiagnostics(message);
		}
		for (String resource : pkgOutcome.getResourcesInstalled().keySet()) {
			outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING)
					.setDiagnostics(resource + ": " + pkgOutcome.getResourcesInstalled().get(resource));
		}
		return outcome;
	}

	public OperationOutcome getOperationOutcome(PackageDeleteOutcomeJson pkgOutcome) {
		if (pkgOutcome == null) {
			return null;
		}
		OperationOutcome outcome = new OperationOutcome();
		for (String message : pkgOutcome.getMessage()) {
			outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING)
					.setDiagnostics(message);
		}
		return outcome;
	}

	public OperationOutcome uninstall(ImplementationGuide theResource) {
		return getOperationOutcome(packageInstallerSvc.uninstall(this.getPackageInstallationSpec()
				.setPackageUrl(theResource.getUrl())
				.setName(theResource.getName())
				.setVersion(theResource.getVersion())));
	}

	public PackageInstallationSpec getPackageInstallationSpec() {
		return new PackageInstallationSpec()
				.addInstallResourceTypes(MatchboxPackageInstallerImpl.DEFAULT_INSTALL_TYPES.toArray(new String[0]))
				.setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_ONLY)
				.addDependencyExclude("hl7.fhir.r4.core")
				.addDependencyExclude("hl7.fhir.r5.core")
				.addDependencyExclude("hl7.terminology")
				.addDependencyExclude("hl7.terminology.r4")
				.addDependencyExclude("hl7.fhir.r4.examples");
	}

	public PackageInstallOutcomeJson load(ImplementationGuide theResource, PackageInstallOutcomeJson install) {
		PackageInstallOutcomeJson installOutcome = packageInstallerSvc
				.install(this.getPackageInstallationSpec().setName(theResource.getName())
						.setPackageUrl(theResource.getUrl()).setVersion(theResource.getVersion()));
		if (install != null) {
			install.getMessage().addAll(installOutcome.getMessage());
			return install;
		}
		return installOutcome;
	}

	public OperationOutcome load(ImplementationGuide theResource) {
		PackageInstallOutcomeJson installOutcome = packageInstallerSvc.install(this.getPackageInstallationSpec()
				.setPackageUrl(theResource.getUrl())
				.addInstallResourceTypes(MatchboxPackageInstallerImpl.DEFAULT_INSTALL_TYPES.toArray(new String[0]))
				.setName(theResource.getName())
				.setVersion(theResource.getVersion())
				.setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_ONLY));

		if (cliContext!=null && cliContext.getOnlyOneEngine()) {
			MatchboxEngine engine = matchboxEngineSupport.getMatchboxEngine(FHIRVersion._4_0_1.getDisplay(), this.cliContext, false, false);
			try {
				engine.loadPackage(theResource.getPackageId(), theResource.getVersion());
			} catch (Exception e) {
				log.error("Error loading package " + theResource.getPackageId() + " " + theResource.getVersion(), e);
			}
		} else {
			matchboxEngineSupport.getMatchboxEngine(FHIRVersion._4_0_1.getDisplay(), this.cliContext, false, true);
		}
		return getOperationOutcome(installOutcome);
	}

	public PackageInstallOutcomeJson loadAll(boolean replace) {
		matchboxEngineSupport.setInitialized(false);
		log.info("Initializing packages " + VersionUtil.getMemory());
		PackageInstallOutcomeJson installOutcome = null;
		if (appProperties.getImplementationGuides() != null) {
			Map<String, AppProperties.ImplementationGuide> guides = appProperties.getImplementationGuides();
			for (AppProperties.ImplementationGuide guide : guides.values()) {
				log.debug("Loading package " + guide.getName() + "#" + guide.getVersion() + " from property hapi.fhir" +
								 ".implementationGuides");
				boolean exists = new TransactionTemplate(myTxManager).execute(tx -> {
					Optional<NpmPackageVersionEntity> existing = myPackageVersionDao
							.findByPackageIdAndVersion(guide.getName(), guide.getVersion());
					return existing.isPresent();
				});
				if (!exists || replace) {
					ImplementationGuide ig = new ImplementationGuide();
					ig.setName(guide.getName());
					ig.setPackageId(guide.getName());
					ig.setUrl(guide.getUrl());
					ig.setVersion(guide.getVersion());
					installOutcome = load(ig, installOutcome);
				}
			}
		}
		log.info("Initializing packages finished " + VersionUtil.getMemory());

		if (this.appProperties.getOnly_install_packages() != null && this.appProperties.getOnly_install_packages()) {
			// In the 'only_install_packages' mode, we can stop after having installed the IGs in the database
			return installOutcome;
		}

		log.info("Creating cached engines during startup  " + VersionUtil.getMemory());
		// The matchboxEngineSupport will set the 'initialized' flag after having reloaded
		MatchboxEngine engine = matchboxEngineSupport.getMatchboxEngineNotSynchronized(null, this.cliContext, false,
																												 true);
		if (cliContext!=null && cliContext.getOnlyOneEngine()) {
			List<NpmPackageVersionEntity> packages = myPackageVersionDao
					.findAll(org.springframework.data.domain.Sort.by(Direction.ASC, "myPackageId", "myVersionId"));
			for (NpmPackageVersionEntity npmPackage : packages) {
				try {
					engine.loadPackage(npmPackage.getPackageId(), npmPackage.getVersionId());
				} catch (Exception e) {
					log.error("Error loading package " + npmPackage.getPackageId() + " " + npmPackage.getVersionId(), e);
				}
			}
		}
		log.info("Finished engines during startup  " + VersionUtil.getMemory());
		return installOutcome;
	}

	@Operation(name = "$load-all", type = ImplementationGuide.class, idempotent = false)
	public OperationOutcome loadAll() {
		return this.getOperationOutcome(loadAll(true));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	}

	@Override
	@Search(allowUnknownParams = true)
	public ca.uhn.fhir.rest.api.server.IBundleProvider search(
			jakarta.servlet.http.HttpServletRequest theServletRequest,
			jakarta.servlet.http.HttpServletResponse theServletResponse,

			ca.uhn.fhir.rest.api.server.RequestDetails theRequestDetails,

			@Description(shortDefinition = "Search the contents of the resource's data using a filter") @OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_FILTER) StringAndListParam theFtFilter,

			@Description(shortDefinition = "Search the contents of the resource's data using a fulltext search") @OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_CONTENT) StringAndListParam theFtContent,

			@Description(shortDefinition = "Search the contents of the resource's narrative using a fulltext search") @OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_TEXT) StringAndListParam theFtText,

			@Description(shortDefinition = "Search for resources which have the given tag") @OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_TAG) TokenAndListParam theSearchForTag,

			@Description(shortDefinition = "Search for resources which have the given security labels") @OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_SECURITY) TokenAndListParam theSearchForSecurity,

			@Description(shortDefinition = "Search for resources which have the given profile") @OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_PROFILE) UriAndListParam theSearchForProfile,

			@Description(shortDefinition = "Search for resources which have the given source value (Resource.meta.source)") @OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_SOURCE) UriAndListParam theSearchForSource,

			@Description(shortDefinition = "Return resources linked to by the given target") @OptionalParam(name = "_has") HasAndListParam theHas,

			@Description(shortDefinition = "The ID of the resource") @OptionalParam(name = "_id") TokenAndListParam the_id,

			@Description(shortDefinition = "A use context assigned to the implementation guide") @OptionalParam(name = "context") TokenAndListParam theContext,

			@Description(shortDefinition = "A quantity- or range-valued use context assigned to the implementation guide") @OptionalParam(name = "context-quantity") QuantityAndListParam theContext_quantity,

			@Description(shortDefinition = "A type of use context assigned to the implementation guide") @OptionalParam(name = "context-type") TokenAndListParam theContext_type,

			@Description(shortDefinition = "A use context type and quantity- or range-based value assigned to the implementation guide") @OptionalParam(name = "context-type-quantity", compositeTypes = {
					TokenParam.class,
					QuantityParam.class }) CompositeAndListParam<TokenParam, QuantityParam> theContext_type_quantity,

			@Description(shortDefinition = "A use context type and value assigned to the implementation guide") @OptionalParam(name = "context-type-value", compositeTypes = {
					TokenParam.class,
					TokenParam.class }) CompositeAndListParam<TokenParam, TokenParam> theContext_type_value,

			@Description(shortDefinition = "The implementation guide publication date") @OptionalParam(name = "date") DateRangeParam theDate,

			@Description(shortDefinition = "Identity of the IG that this depends on") @OptionalParam(name = "depends-on", targetTypes = {}) ReferenceAndListParam theDepends_on,

			@Description(shortDefinition = "The description of the implementation guide") @OptionalParam(name = "description") StringAndListParam theDescription,

			@Description(shortDefinition = "For testing purposes, not real usage") @OptionalParam(name = "experimental") TokenAndListParam theExperimental,

			@Description(shortDefinition = "Profile that all resources must conform to") @OptionalParam(name = "global", targetTypes = {}) ReferenceAndListParam theGlobal,

			@Description(shortDefinition = "Intended jurisdiction for the implementation guide") @OptionalParam(name = "jurisdiction") TokenAndListParam theJurisdiction,

			@Description(shortDefinition = "Computationally friendly name of the implementation guide") @OptionalParam(name = "name") StringAndListParam theName,

			@Description(shortDefinition = "Name of the publisher of the implementation guide") @OptionalParam(name = "publisher") StringAndListParam thePublisher,

			@Description(shortDefinition = "Location of the resource") @OptionalParam(name = "resource", targetTypes = {}) ReferenceAndListParam theResource,

			@Description(shortDefinition = "The current status of the implementation guide") @OptionalParam(name = "status") TokenAndListParam theStatus,

			@Description(shortDefinition = "The human-friendly name of the implementation guide") @OptionalParam(name = "title") StringAndListParam theTitle,

			@Description(shortDefinition = "The uri that identifies the implementation guide") @OptionalParam(name = "url") UriAndListParam theUrl,

			@Description(shortDefinition = "The business version of the implementation guide") @OptionalParam(name = "version") TokenAndListParam theVersion,

			@RawParam Map<String, List<String>> theAdditionalRawParams,

			@Description(shortDefinition = "Only return resources which were last updated as specified by the given range") @OptionalParam(name = "_lastUpdated") DateRangeParam theLastUpdated,

			@IncludeParam Set<Include> theIncludes,

			@IncludeParam(reverse = true) Set<Include> theRevIncludes,

			@Sort SortSpec theSort,

			@ca.uhn.fhir.rest.annotation.Count Integer theCount,

			@ca.uhn.fhir.rest.annotation.Offset Integer theOffset,

			SummaryEnum theSummaryMode,

			SearchTotalModeEnum theSearchTotalMode,

			SearchContainedModeEnum theSearchContainedMode

	) {
		startRequest(theServletRequest);
		try {
			List<NpmPackageVersionEntity> packages = myPackageVersionDao
					.findAll(org.springframework.data.domain.Sort.by(Direction.ASC, "myPackageId", "myVersionId"));
			List<ImplementationGuide> list = new ArrayList<ImplementationGuide>();

			for (NpmPackageVersionEntity npmPackage : packages) {
				ImplementationGuide ig = new ImplementationGuide();
				ig.setId(npmPackage.getPackageId() + "-" + npmPackage.getVersionId());
				ig.setTitle(npmPackage.getDescription());
				ig.setDate(npmPackage.getUpdatedTime());
				ig.setPackageId(npmPackage.getPackageId());
				if (npmPackage.isCurrentVersion()) {
					ig.setVersion(npmPackage.getVersionId() + " (last)");
				} else {
					ig.setVersion(npmPackage.getVersionId());
				}
				list.add(ig);
			}

			SimpleBundleProvider simpleBundleProivder = new SimpleBundleProvider(list);
			return simpleBundleProivder;

		} finally {
			endRequest(theServletRequest);
		}
	}

	@Override
	public ImplementationGuide read(HttpServletRequest theServletRequest, IIdType theId,
			RequestDetails theRequestDetails) {

		startRequest(theServletRequest);
		try {
			return new TransactionTemplate(myTxManager).execute(tx -> {
				String id = theId.getIdPart().substring(0, theId.getIdPart().lastIndexOf("-"));
				String version = theId.getIdPart().substring(theId.getIdPart().lastIndexOf("-") + 1);
				Optional<NpmPackageVersionEntity> packages = myPackageVersionDao.findByPackageIdAndVersion(id, version);
				if (packages.isPresent()) {
					NpmPackageVersionEntity npmPackage = packages.get();
					ImplementationGuide ig = new ImplementationGuide();
					ig.setId(npmPackage.getPackageId() + "-" + npmPackage.getVersionId());
					ig.setTitle(npmPackage.getDescription());
					ig.setDate(npmPackage.getUpdatedTime());
					ig.setPackageId(npmPackage.getPackageId());
					ig.setVersion(npmPackage.getVersionId());
					return ig;
				}
				return null;
			});
		} finally {
			endRequest(theServletRequest);
		}
	}

}
