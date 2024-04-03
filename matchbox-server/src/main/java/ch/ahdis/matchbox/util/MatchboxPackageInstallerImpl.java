package ch.ahdis.matchbox.util;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.jpa.model.dao.JpaPid;
import ca.uhn.fhir.util.BinaryUtil;
import ch.ahdis.matchbox.StructureDefinitionResourceProvider;
import jakarta.annotation.PostConstruct;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.*;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.utilities.json.model.JsonObject;
import org.hl7.fhir.utilities.npm.IPackageCacheManager;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.npm.NpmPackage.NpmPackageFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementCompositeDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.api.model.DaoMethodOutcome;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionDao;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionEntity;
import ca.uhn.fhir.jpa.model.util.JpaConstants;
import ca.uhn.fhir.jpa.packages.IHapiPackageCacheManager;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.packages.ImplementationGuideInstallationException;
import ca.uhn.fhir.jpa.packages.JpaPackageCache;
import ca.uhn.fhir.jpa.packages.PackageDeleteOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.SystemRequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.util.FhirTerser;
import ca.uhn.fhir.util.SearchParameterUtil;

/**
 * This is a copy of ca.uhn.fhir.jpa.packages.PackageInstallerSvcImpl with the
 * following modifications: - Resources with status "draft" are also loaded -
 * examples are also loaded Modifications are marked in source code comments
 * with "MODIFIED"
 *
 * @author alexander kreutz
 *
 */
public class MatchboxPackageInstallerImpl implements IPackageInstallerSvc {

	private static final Logger ourLog = LoggerFactory.getLogger(MatchboxPackageInstallerImpl.class);
	// MODIFIED
	public static List<String> DEFAULT_INSTALL_TYPES = Collections
			.unmodifiableList(Lists.newArrayList("NamingSystem", "CodeSystem", "ValueSet", "StructureDefinition",
					"ConceptMap", "SearchParameter", "StructureMap", "Questionnaire"));

	boolean enabled = true;
	@Autowired
	private FhirContext myFhirContext;
	@Autowired
	private DaoRegistry myDaoRegistry;

	@Autowired
	private IHapiPackageCacheManager myPackageCacheManager;
	@Autowired
	private PlatformTransactionManager myTxManager;
	@Autowired
	private INpmPackageVersionDao myPackageVersionDao;
	@Autowired
	private INpmPackageVersionResourceDao myPackageVersionResourceDao;
	@Autowired
	private PartitionSettings myPartitionSettings;
	@Autowired
	private IBinaryStorageSvc myBinaryStorageSvc;

	/**
	 * Constructor
	 */
	public MatchboxPackageInstallerImpl() {
		super();
	}

	@PostConstruct
	public void initialize() {
		switch (myFhirContext.getVersion().getVersion()) {
		case R5:
		case R4:
		case DSTU3:
			break;

		case DSTU2:
		case DSTU2_HL7ORG:
		case DSTU2_1:
		default: {
			ourLog.info("IG installation not supported for version: {}", myFhirContext.getVersion().getVersion());
			enabled = false;
		}
		}
	}

	// MODIFIED: added
	public PackageDeleteOutcomeJson uninstall(PackageInstallationSpec theInstallationSpec)
			throws ImplementationGuideInstallationException {
		PackageInstallOutcomeJson retVal = new PackageInstallOutcomeJson();
		boolean exists = new TransactionTemplate(myTxManager).execute(tx -> {
			Optional<NpmPackageVersionEntity> existing = myPackageVersionDao
					.findByPackageIdAndVersion(theInstallationSpec.getName(), theInstallationSpec.getVersion());
			return existing.isPresent();
		});
		if (exists) {
			ourLog.info("Remove Package {}#{} because it is a package based on an external url",
					theInstallationSpec.getName(), theInstallationSpec.getVersion());
			return myPackageCacheManager.uninstallPackage(theInstallationSpec.getName(), theInstallationSpec.getVersion());
		}
		return null;
	}

	/**
	 * Loads and installs an IG from a file on disk or the Simplifier repo using the
	 * {@link IPackageCacheManager}.
	 * <p>
	 * Installs the IG by persisting instances of the following types of resources:
	 * <p>
	 * - NamingSystem, CodeSystem, ValueSet, StructureDefinition (with snapshots),
	 * ConceptMap, SearchParameter, Subscription
	 * <p>
	 * Creates the resources if non-existent, updates them otherwise.
	 *
	 * @param theInstallationSpec The details about what should be installed
	 */
	@SuppressWarnings("ConstantConditions")
	public PackageInstallOutcomeJson install(PackageInstallationSpec theInstallationSpec)
			throws ImplementationGuideInstallationException {

		theInstallationSpec.addDependencyExclude("hl7.terminology.r4");
		theInstallationSpec.addDependencyExclude("hl7.terminology.r5");
		theInstallationSpec.addDependencyExclude("hl7.fhir.r4.core");  // terminology has a dependency to hl7.fhir.core.r4
		theInstallationSpec.addDependencyExclude("hl7.fhir.cda");  // used as dev
		theInstallationSpec.addDependencyExclude("hl7.fhir.uv.extensions.r4"); // terminology has a dependency to hl7.fhir.core.r4
		theInstallationSpec.addDependencyExclude("hl7.fhir.uv.extensions.r5"); 
		PackageInstallOutcomeJson retVal = new PackageInstallOutcomeJson();
		if (enabled) {
			try {

				boolean exists = new TransactionTemplate(myTxManager).execute(tx -> {
					Optional<NpmPackageVersionEntity> existing = myPackageVersionDao
							.findByPackageIdAndVersion(theInstallationSpec.getName(), theInstallationSpec.getVersion());
					return existing.isPresent();
				});
				if (exists) {
					ourLog.info("Package {}#{} is already installed", theInstallationSpec.getName(),
							theInstallationSpec.getVersion());
					// MODIFIED: This has been added to add remove packages based on url
					if (theInstallationSpec.getPackageUrl() != null && !theInstallationSpec.getPackageUrl().startsWith("classpath:") && !theInstallationSpec.getPackageUrl().startsWith("file:")) {
						ourLog.info("Remove Package {}#{} because it is a package based on an external url",
								theInstallationSpec.getName(), theInstallationSpec.getVersion());
						myPackageCacheManager.uninstallPackage(theInstallationSpec.getName(), theInstallationSpec.getVersion());
					} else {
						// Abort loading, the package is already installed
						return retVal;
					}
				}

				NpmPackage npmPackage = null;
				try {
					npmPackage = myPackageCacheManager.installPackage(theInstallationSpec);
				} catch (Exception e) {
					ourLog.error("Error installing package: " +theInstallationSpec.getName() + "#" + theInstallationSpec.getVersion(), e);
					throw e;
				}
				if (npmPackage == null) {
					throw new IOException("Package not found");
				}
				retVal.getMessage().addAll(JpaPackageCache.getProcessingMessages(npmPackage));

//				if (theInstallationSpec.isFetchDependencies()) {
					fetchAndInstallDependencies(npmPackage, theInstallationSpec, retVal);
//				}


			} catch (IOException e) {
				throw new ImplementationGuideInstallationException(
						"Could not load NPM package " + theInstallationSpec.getName() + "#" + theInstallationSpec.getVersion(), e);
			}

			// We have installed at least one new package, let's save the StructureDefinition titles in the database
			ourLog.debug("Updating StructureDefinition titles...");
			final var parserR4 = new org.hl7.fhir.r4.formats.JsonParser();
			final var parserR5 = new org.hl7.fhir.r5.formats.JsonParser();
			new TransactionTemplate(this.myTxManager).execute(tx -> {
				final var page = PageRequest.of(0, 2147483646);
				this.myPackageVersionResourceDao.findByResourceType(page, "StructureDefinition")
					.forEach(npmPackageVersionResourceEntity -> {
						try {
							if (npmPackageVersionResourceEntity.getFilename() != null && !npmPackageVersionResourceEntity.getFilename().endsWith(".json")) {
								// The filename has already been modified
								return;
							}
							final var sdBinary = MatchboxServerUtils.getBinaryFromId(npmPackageVersionResourceEntity.getResourceBinary().getId(), myDaoRegistry);
							final byte[] resourceContentsBytes;
							resourceContentsBytes = MatchboxServerUtils.fetchBlobFromBinary(sdBinary, myBinaryStorageSvc,
																												 myFhirContext);
							final String resourceContents = new String(resourceContentsBytes, StandardCharsets.UTF_8);
							final var title = switch (npmPackageVersionResourceEntity.getFhirVersion()) {
								case R4 -> {
									final var sd = (org.hl7.fhir.r4.model.StructureDefinition) parserR4.parse(resourceContents);
									if (sd.getTitle() != null) {
										yield sd.getTitle();
									}
									yield sd.getName();
								}
								case R5 -> {
									final var sd = (org.hl7.fhir.r5.model.StructureDefinition) parserR5.parse(resourceContents);
									if (sd.getTitle() != null) {
										yield sd.getTitle();
									}
									yield sd.getName();
								}
								default -> {
									ourLog.error("FHIR version not supported for parsing the StructureDefinition");
									throw new RuntimeException(Msg.code(1305) + "Failed to load package resource " + resourceContents);
								}
							};

							// Change the filename for the StructureDefinition title
							npmPackageVersionResourceEntity.setFilename(title);
							this.myPackageVersionResourceDao.save(npmPackageVersionResourceEntity);
						} catch (final IOException e) {
							ourLog.error("Unable to extract the StructureDefinition title", e);
						}
				});
				return null;
			});
			ourLog.debug("Updating StructureDefinition titles... Done");
		}

		return retVal;
	}

	private void fetchAndInstallDependencies(NpmPackage npmPackage, PackageInstallationSpec theInstallationSpec, PackageInstallOutcomeJson theOutcome) throws ImplementationGuideInstallationException {
		if (npmPackage.getNpm().has("dependencies")) {
			JsonObject dependencies = npmPackage.getNpm().get("dependencies").asJsonObject();
			List<String> igs = dependencies.getNames();
			for(String ig: igs) {
				String ver = dependencies.get(ig).asString();
				try {
					theOutcome.getMessage().add("Package " + npmPackage.id() + "#" + npmPackage.version() + " depends on package " + ig + "#" + ver);

					boolean skip = false;
					for (String next : theInstallationSpec.getDependencyExcludes()) {
						if (ig.matches(next)) {
							theOutcome.getMessage().add("Not installing dependency " + ig + " because it matches exclude criteria: " + next);
							skip = true;
							break;
						}
					}
					if (skip) {
						continue;
					}

					// resolve in local cache or on packages.fhir.org
					NpmPackage dependency = myPackageCacheManager.loadPackage(ig, ver);
					// recursive call to install dependencies of a package before
					// installing the package
					fetchAndInstallDependencies(dependency, theInstallationSpec, theOutcome);

				} catch (IOException e) {
					throw new ImplementationGuideInstallationException(Msg.code(1287) + String.format(
						"Cannot resolve dependency %s#%s", ig, ver), e);
				}
			}
		}
	}

	/**
	 * ============================= Utility methods ===============================
	 */

	// MODIFIED: This method has been reimplemented: also add example folder 
	private List<IBaseResource> parseResourcesOfType(String type, NpmPackage pkg) {
		if (!pkg.getFolders().containsKey("package")) {
			return Collections.emptyList();
		}

		ArrayList<IBaseResource> resources = new ArrayList<>();

		addFolder(type, pkg.getFolders().get("package"), resources);

		NpmPackageFolder exampleFolder = pkg.getFolders().get("example");
		if (exampleFolder != null) {
			try {
			  pkg.indexFolder("example", exampleFolder);
			  addFolder(type, exampleFolder, resources);
			} catch (IOException e) {
				throw new InternalErrorException("Cannot install resource of type " + type + ": Could not read example directory", e);
			}
		}
		return resources;
	}

	// MODIFIED: This utility method has been added. It is used by
	// parseResourcesOfType(type, pkg)
	private void addFolder(String type, NpmPackageFolder folder, List<IBaseResource> resources) {
		if (folder == null)
			return;
		List<String> filesForType;
		try {
			filesForType = folder.getTypes().get(type);
		} catch (final IOException exception) {
			throw new InternalErrorException(exception);
		}
		if (filesForType == null)
			return;
		for (String file : filesForType) {
			try {
				byte[] content = folder.fetchFile(file);
				// modified: add only resources in DEFAULT_INSTALL_TYPES
				IBaseResource resource = myFhirContext.newJsonParser().parseResource(new String(content));
				if (DEFAULT_INSTALL_TYPES.contains(resource.fhirType())) {
					resources.add(resource);
				}
			} catch (IOException e) {
				throw new InternalErrorException("Cannot install resource of type " + type + ": Could not fetch file " + file,
						e);
			}
		}
	}

	public void create(IBaseResource theResource, PackageInstallOutcomeJson theOutcome) {
		IFhirResourceDao dao = myDaoRegistry.getResourceDao(theResource.getClass());
		SearchParameterMap map = createSearchParameterMapFor(theResource);
		IBundleProvider searchResult = searchResource(dao, map);
		if (validForUpload(theResource)) {
			if (searchResult.isEmpty()) {

				ourLog.debug("Creating new resource matching {}", map.toNormalizedQueryString(myFhirContext));
				theOutcome.incrementResourcesInstalled(myFhirContext.getResourceType(theResource));

				IIdType id = theResource.getIdElement();

				if (id.isEmpty()) {
					createResource(dao, theResource);
					ourLog.debug("Created resource with new id");
				} else {
					if (id.isIdPartValidLong()) {
						String newIdPart = "npm-" + id.getIdPart();
						id.setParts(id.getBaseUrl(), id.getResourceType(), newIdPart, id.getVersionIdPart());
					}
					updateResource(dao, theResource);
					ourLog.info("Created resource with existing id " + id.toString());
				}
			} else {
				ourLog.debug("Updating existing resource matching {}", map.toNormalizedQueryString(myFhirContext));
				theResource.setId(searchResult.getResources(0, 1).get(0).getIdElement().toUnqualifiedVersionless());
				DaoMethodOutcome outcome = updateResource(dao, theResource);
				if (!outcome.isNop()) {
					theOutcome.incrementResourcesInstalled(myFhirContext.getResourceType(theResource));
				}
			}

		}
	}

	private IBundleProvider searchResource(IFhirResourceDao theDao, SearchParameterMap theMap) {
		if (myPartitionSettings.isPartitioningEnabled()) {
			SystemRequestDetails requestDetails = new SystemRequestDetails();
//			requestDetails.setTenantId(JpaConstants.DEFAULT_PARTITION_NAME);
			return theDao.search(theMap, requestDetails);
		} else {
			return theDao.search(theMap);
		}
	}

	private void createResource(IFhirResourceDao theDao, IBaseResource theResource) {
		if (myPartitionSettings.isPartitioningEnabled()) {
			SystemRequestDetails requestDetails = new SystemRequestDetails();
			requestDetails.setTenantId(JpaConstants.DEFAULT_PARTITION_NAME);
			theDao.create(theResource, requestDetails);
		} else {
			theDao.create(theResource);
		}
	}

	private DaoMethodOutcome updateResource(IFhirResourceDao theDao, IBaseResource theResource) {
		if (myPartitionSettings.isPartitioningEnabled()) {
			SystemRequestDetails requestDetails = new SystemRequestDetails();
			requestDetails.setTenantId(JpaConstants.DEFAULT_PARTITION_NAME);
			return theDao.update(theResource, requestDetails);
		} else {
			return theDao.update(theResource);
		}
	}

	// MODIFIED: This method has been modified: Also allow Resources with status
	// "draft"
	boolean validForUpload(IBaseResource theResource) {
		String resourceType = myFhirContext.getResourceType(theResource);
		if ("SearchParameter".equals(resourceType)) {

			String code = SearchParameterUtil.getCode(myFhirContext, theResource);
			if (defaultString(code).startsWith("_")) {
				return false;
			}

			String expression = SearchParameterUtil.getExpression(myFhirContext, theResource);
			if (isBlank(expression)) {
				return false;
			}

			if (SearchParameterUtil.getBaseAsStrings(myFhirContext, theResource).isEmpty()) {
				return false;
			}
		}

		List<IPrimitiveType> statusTypes = myFhirContext.newFhirPath().evaluate(theResource, "status",
				IPrimitiveType.class);
		if (statusTypes.size() > 0) {
			// Modified condition
			if (!statusTypes.get(0).getValueAsString().equals("active")
					&& !statusTypes.get(0).getValueAsString().equals("draft")) {
				return false;
			}
		}

		return true;
	}

	private boolean isStructureDefinitionWithoutSnapshot(IBaseResource r) {
		boolean retVal = false;
		FhirTerser terser = myFhirContext.newTerser();
		if (r.getClass().getSimpleName().equals("StructureDefinition")) {
			Optional<String> kind = terser.getSinglePrimitiveValue(r, "kind");
			if (kind.isPresent() && !(kind.get().equals("logical"))) {
				retVal = terser.getSingleValueOrNull(r, "snapshot") == null;
			}
		}
		return retVal;
	}

	private boolean isImplementationGuide(IBaseResource r) {
		if (r.getClass().getSimpleName().equals("ImplementationGuide")) {
			return true;
		}
		return false;
	}

	private SearchParameterMap createSearchParameterMapFor(IBaseResource resource) {
		if (resource.getClass().getSimpleName().equals("NamingSystem")) {
			String uniqueId = extractUniqeIdFromNamingSystem(resource);
			return SearchParameterMap.newSynchronous().add("value", new StringParam(uniqueId).setExact(true));
		} else if (resource.getClass().getSimpleName().equals("Subscription")) {
			String id = extractIdFromSubscription(resource);
			return SearchParameterMap.newSynchronous().add("_id", new TokenParam(id));
		} else if (resourceHasUrlElement(resource)) {
			String url = extractUniqueUrlFromMetadataResource(resource);
			String version = extractVersionFromMetadataResource(resource);
			return SearchParameterMap.newSynchronous().add("url", new UriParam(url)).add("version", new TokenParam(version));
		} else {
			TokenParam identifierToken = extractIdentifierFromOtherResourceTypes(resource);
			return SearchParameterMap.newSynchronous().add("identifier", identifierToken);
		}
	}

	private String extractUniqeIdFromNamingSystem(IBaseResource resource) {
		FhirTerser terser = myFhirContext.newTerser();
		IBase uniqueIdComponent = (IBase) terser.getSingleValueOrNull(resource, "uniqueId");
		if (uniqueIdComponent == null) {
			throw new ImplementationGuideInstallationException("NamingSystem does not have uniqueId component.");
		}
		IPrimitiveType<?> asPrimitiveType = (IPrimitiveType<?>) terser.getSingleValueOrNull(uniqueIdComponent, "value");
		return (String) asPrimitiveType.getValue();
	}

	private String extractIdFromSubscription(IBaseResource resource) {
		FhirTerser terser = myFhirContext.newTerser();
		IPrimitiveType<?> asPrimitiveType = (IPrimitiveType<?>) terser.getSingleValueOrNull(resource, "id");
		return (String) asPrimitiveType.getValue();
	}

	private String extractUniqueUrlFromMetadataResource(IBaseResource resource) {
		FhirTerser terser = myFhirContext.newTerser();
		IPrimitiveType<?> asPrimitiveType = (IPrimitiveType<?>) terser.getSingleValueOrNull(resource, "url");
		return (String) asPrimitiveType.getValue();
	}

	private String extractVersionFromMetadataResource(IBaseResource resource) {
		FhirTerser terser = myFhirContext.newTerser();
		IPrimitiveType<?> asPrimitiveType = (IPrimitiveType<?>) terser.getSingleValueOrNull(resource, "version");
		return (String) asPrimitiveType.getValue();
	}

	private TokenParam extractIdentifierFromOtherResourceTypes(IBaseResource resource) {
		FhirTerser terser = myFhirContext.newTerser();
		Identifier identifier = (Identifier) terser.getSingleValueOrNull(resource, "identifier");
		if (identifier != null) {
			return new TokenParam(identifier.getSystem(), identifier.getValue());
		} else {
			throw new UnsupportedOperationException(
					"Resources in a package must have a url or identifier to be loaded by the package installer.");
		}
	}

	private boolean resourceHasUrlElement(IBaseResource resource) {
		BaseRuntimeElementDefinition<?> def = myFhirContext.getElementDefinition(resource.getClass());
		if (!(def instanceof BaseRuntimeElementCompositeDefinition)) {
			throw new IllegalArgumentException("Resource is not a composite type: " + resource.getClass().getName());
		}
		BaseRuntimeElementCompositeDefinition<?> currentDef = (BaseRuntimeElementCompositeDefinition<?>) def;
		BaseRuntimeChildDefinition nextDef = currentDef.getChildByName("url");
		return nextDef != null;
	}

	@VisibleForTesting
	void setFhirContextForUnitTest(FhirContext theCtx) {
		myFhirContext = theCtx;
	}

}
