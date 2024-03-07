package ch.ahdis.matchbox;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import ch.ahdis.matchbox.util.MatchboxServerUtils;
import jakarta.servlet.http.HttpServletRequest;

import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RawParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.SearchContainedModeEnum;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.UriAndListParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;

import static ch.ahdis.matchbox.util.MatchboxServerUtils.addExtension;

@DisallowConcurrentExecution
public class ConformancePackageResourceProvider<R4 extends MetadataResource, R4B extends org.hl7.fhir.r4b.model.CanonicalResource, R5 extends org.hl7.fhir.r5.model.CanonicalResource> implements IResourceProvider {

	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;

	@Autowired
	AppProperties appProperties;

	@Autowired
	private INpmPackageVersionResourceDao myPackageVersionResourceDao;

	@Autowired
	private PlatformTransactionManager myTxManager;

	@Autowired
	private IBinaryStorageSvc myBinaryStorageSvc;

	@Autowired
	private DaoRegistry myDaoRegistry;

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConformancePackageResourceProvider.class);

	@Autowired
	private FhirContext myCtx;

	@Autowired
	protected CliContext cliContext;

	private String resourceType;
	protected final Class<R4> classR4;
	protected final Class<R4B> classR4B;
	protected final Class<R5> classR5;

	public ConformancePackageResourceProvider(Class<R4> r4Type, Class<R4B> r4BType, Class<R5> r5Type) {
		super();
		this.classR4 = r4Type;
		this.resourceType = r4Type.getSimpleName();
		this.classR4B = r4BType;
		this.classR5 = r5Type;
	}

	@Search(allowUnknownParams = true)
	public ca.uhn.fhir.rest.api.server.IBundleProvider search(jakarta.servlet.http.HttpServletRequest theServletRequest,
			jakarta.servlet.http.HttpServletResponse theServletResponse,

			ca.uhn.fhir.rest.api.server.RequestDetails theRequestDetails,

			@Description(shortDefinition = "The ID of the resource") @OptionalParam(name = "_id") TokenAndListParam the_id,

			@Description(shortDefinition = "The uri that identifies the conformance resource") @OptionalParam(name = "url") UriAndListParam theUrl,

			@Description(shortDefinition = "The business version of the conformance resource") @OptionalParam(name = "version") TokenAndListParam theCanonicalVersion,

			@RawParam Map<String, List<String>> theAdditionalRawParams,

			@IncludeParam Set<Include> theIncludes,

			@IncludeParam(reverse = true) Set<Include> theRevIncludes,

			@Sort SortSpec theSort,

			@ca.uhn.fhir.rest.annotation.Count Integer theCount,

			@ca.uhn.fhir.rest.annotation.Offset Integer theOffset,

			SummaryEnum theSummaryMode,

			SearchTotalModeEnum theSearchTotalMode,

			SearchContainedModeEnum theSearchContainedMode

	) {
		if ("ImplementationGuide".equals(resourceType)) {
			try {
				return new TransactionTemplate(myTxManager).execute(tx -> {
					final int offset = (theOffset == null ? 0 : theOffset.intValue());
					final int count = (theCount == null ? 10000 : theCount.intValue());
					Slice<NpmPackageVersionResourceEntity> outcome = null;

					if (the_id != null) {
						String pid = the_id.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue();
						outcome = myPackageVersionResourceDao.findByResourceTypeById(PageRequest.of(offset, count),
								resourceType,
								Long.parseLong(pid));
					} else {
						if (theUrl != null) {
							String url = theUrl.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue();
							if (theCanonicalVersion != null) {
								String canonicalVersion = theCanonicalVersion.getValuesAsQueryTokens().get(0)
										.getValuesAsQueryTokens().get(0).getValue();
								outcome = myPackageVersionResourceDao.findByResourceTypeByCanonicalByCanonicalVersion(
										PageRequest.of(offset, count), resourceType, url, canonicalVersion);
							} else {
								outcome = myPackageVersionResourceDao
										.findByResourceTypeByCanoncial(PageRequest.of(offset, count), resourceType, url);
							}
						} else {
							outcome = myPackageVersionResourceDao.findByResourceType(PageRequest.of(offset, count),
									resourceType);
						}
					}

					SimpleBundleProvider bundleProvider = new SimpleBundleProvider(
							outcome.stream().map(t -> loadPackageEntityAdjustId(t)).collect(Collectors.toList()));
					bundleProvider.setCurrentPageOffset(offset);
					bundleProvider.setCurrentPageSize(count);
					return bundleProvider;
				});
			} finally {
				}
		} 
		
		if (cliContext.getOnlyOneEngine()){
			List<org.hl7.fhir.r5.model.Resource> resources = new ArrayList<org.hl7.fhir.r5.model.Resource>();
			MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(null, cliContext,
					false, false);
			if (matchboxEngine != null) {

				if (theUrl != null) {
					String url = theUrl.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue();
					R5 r = matchboxEngine.getContext().fetchResource(classR5,url);
					resources.add(r);
				} else {
					resources.addAll(matchboxEngine.getContext().fetchResourcesByType(classR5));
				}
				return new SimpleBundleProvider(
					resources.stream().map(VersionConvertorFactory_40_50::convertResource).collect(Collectors.toList()));
			}
		}
		return null;
	}

	/**
	 * Returns the list of installed StructureDefinitions, as a list of R5 CanonicalTypes.
	 */
	public List<org.hl7.fhir.r5.model.CanonicalType> getCanonicalsR5() {
		return new TransactionTemplate(myTxManager).execute(tx -> {
			final var page = PageRequest.of(0, 2147483646);

			// Find the IDs of the current StructureDefinitions.
			final var currentEntityIds =
				this.myPackageVersionResourceDao.findCurrentByResourceType(page, this.resourceType)
					.stream()
					.map(NpmPackageVersionResourceEntity::getId)
					.collect(Collectors.toUnmodifiableSet());

			return this.myPackageVersionResourceDao.findByResourceType(page, this.resourceType)
				.stream()
				.peek(entity -> {
					// NB: getCanonicalVersion() may be null is rare cases, but getPackageVersion().getVersionId() should not
					if (entity.getCanonicalVersion() == null) {
						entity.setCanonicalVersion(entity.getPackageVersion().getVersionId());
					}
				})
				// Sort the StructureDefinitions by canonical URL first, and then by version
				.sorted(Comparator
							  .comparing(NpmPackageVersionResourceEntity::getCanonicalUrl)
							  .thenComparing(NpmPackageVersionResourceEntity::getCanonicalVersion))
				.map(entity -> {
					final var canonical = new org.hl7.fhir.r5.model.CanonicalType(entity.getCanonicalUrl());
					// Add custom extensions to the CanonicalType to store additional information
					addExtension(canonical, "ig-id",
									 new org.hl7.fhir.r5.model.StringType(entity.getPackageVersion().getPackageId()));
					addExtension(canonical, "ig-version",
									 new org.hl7.fhir.r5.model.StringType(entity.getCanonicalVersion()));
					addExtension(canonical, "ig-current",
									 new org.hl7.fhir.r5.model.BooleanType(currentEntityIds.contains(entity.getId())));
					addExtension(canonical, "sd-canonical", new org.hl7.fhir.r5.model.StringType(entity.getCanonicalUrl()));
					if (entity.getFilename() != null && !entity.getFilename().isBlank()) {
						addExtension(canonical, "sd-title", new org.hl7.fhir.r5.model.StringType(entity.getFilename()));
					} else {
						addExtension(canonical, "sd-title", new org.hl7.fhir.r5.model.StringType(entity.getCanonicalUrl()));
					}
					return canonical;
				})
				.toList();
		});
	}

	public List<NpmPackageVersionResourceEntity> getPackageResources() {
		return new TransactionTemplate(this.myTxManager).execute(tx -> {
			return myPackageVersionResourceDao
				.findByResourceType(PageRequest.of(0, 2147483646), resourceType).stream().toList();
		});
	}

	public List<NpmPackageVersionResourceEntity> getCurrentPackageResources() {
		return new TransactionTemplate(this.myTxManager).execute(tx -> {
			return myPackageVersionResourceDao
				.findCurrentByResourceType(PageRequest.of(0, 2147483646), resourceType).stream().toList();
		});
	}

	private IBaseResource loadPackageEntityAdjustId(NpmPackageVersionResourceEntity contents) {
		IBaseResource resource = loadPackageEntity(contents);
		if (resource != null) {
			resource.setId(contents.getId());
		}
		return resource;
	}

	public org.hl7.fhir.r5.model.CanonicalResource getCanonical(IBaseResource theResource) {
		if (classR4.isInstance(theResource)) {
			R4 r4 = classR4.cast(theResource);
			return (org.hl7.fhir.r5.model.CanonicalResource) VersionConvertorFactory_40_50.convertResource(r4);
		}
		if (classR4B.isInstance(theResource)) {
			R4B r4b = classR4B.cast(theResource);
			return (org.hl7.fhir.r5.model.CanonicalResource) VersionConvertorFactory_43_50.convertResource(r4b);
		}
		if (classR5.isInstance(theResource)) {
			R5 r5 = classR5.cast(theResource);
			return r5;
		}
		log.error("FHIR version not supported for resource "+theResource.fhirType()+": "+theResource.getIdElement().getIdPart()+ " : "+ theResource.getStructureFhirVersionEnum());
		return null;
	}

	private IBaseResource loadPackageEntity(NpmPackageVersionResourceEntity contents) {
		try {
			final var binary = MatchboxServerUtils.getBinaryFromId(contents.getResourceBinary().getId(), myDaoRegistry);
			final byte[] resourceContentsBytes = MatchboxServerUtils.fetchBlobFromBinary(binary, myBinaryStorageSvc,
																												  myCtx);
			final String resourceContents = new String(resourceContentsBytes, StandardCharsets.UTF_8);
            return switch (contents.getFhirVersion()) {
                case R4 -> new org.hl7.fhir.r4.formats.JsonParser().parse(resourceContents);
                case R4B -> new org.hl7.fhir.r4b.formats.JsonParser().parse(resourceContents);
                case R5 -> new org.hl7.fhir.r5.formats.JsonParser().parse(resourceContents);
                default -> {
                    log.error("FHIR version not support for loading form matchbox case ");
                    throw new RuntimeException(Msg.code(1305) + "Failed to load package resource " + contents);
                }
            };
		} catch (Exception e) {
			throw new RuntimeException(Msg.code(1305) + "Failed to load package resource " + contents, e);
		}
	}

	@Read(version = false)
	public IBaseResource read(HttpServletRequest theServletRequest, @IdParam IIdType theId, RequestDetails theRequestDetails) {
		try {
			return new TransactionTemplate(myTxManager).execute(tx -> {

				CliContext cliContext = new CliContext(this.cliContext);
		
				final int offset = 0;
				final int count = 1;
				Slice<NpmPackageVersionResourceEntity> outcome = null;
				String pid = theId.getIdPart();
				// check if pid is a long number
				if (pid.matches("\\d+")) {
					outcome = myPackageVersionResourceDao.findByResourceTypeById(PageRequest.of(offset, count),
							resourceType,
							Long.parseLong(pid));
				}
				if (outcome !=null && outcome.getSize() == 1) {
					NpmPackageVersionResourceEntity res = outcome.toList().get(0);
					IBaseResource resource = (IBaseResource) loadPackageEntityAdjustId(outcome.toList().get(0));
					cliContext.setFhirVersion(getFhirVersion(resource));
	
					String url = "null";
					if (classR4.isInstance(resource)) {
						R4 r = classR4.cast(resource);
						url = r.getUrl();
					}
					if (classR4B.isInstance(resource)) {
						R4B r = classR4B.cast(resource);
						url = r.getUrl();
					}
					if (classR5.isInstance(resource)) {
						R5 r = classR5.cast(resource);
						url = r.getUrl();
					}
			
					// if is current we check if already loaded in a own engine and might be updated
					// in the cache
					if (res.getPackageVersion().isCurrentVersion() || cliContext.getOnlyOneEngine()) {
						MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(url, cliContext,
								false, false);
						if (matchboxEngine != null) {
							IBaseResource update = matchboxEngine.getCanonicalResource(url,getFhirVersion(resource));
							if (update != null) {
								return update;
							}
						}
					}
					return loadPackageEntityAdjustId(outcome.toList().get(0));
				} else {
					return matchboxEngineSupport.getCachedResource(resourceType, pid);
				}
			}
			);
		} finally {
		}
	}

	public String getFhirVersion(IBaseResource theResource) {
		return getFhirVersion(theResource.getStructureFhirVersionEnum().getFhirVersionString());
	}

	public String getFhirVersion(String fhirVersionDetailed) {
		if (fhirVersionDetailed.startsWith("4.0")) {
			return "4.0.1";
		}
		if (fhirVersionDetailed.startsWith("4.3")) {
			return "4.3.0";
		}
		if (fhirVersionDetailed.startsWith("5")) {
			return "5.0.0";
		}
		return fhirVersionDetailed;
	}

	@Create()
	public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam IBaseResource theResource, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

		String url = null;

		CliContext cliContext = new CliContext(this.cliContext);
		cliContext.setFhirVersion(getFhirVersion(theResource));
		if (cliContext.getOnlyOneEngine()) {

			if (classR4.isInstance(theResource)) {
				R4 r = classR4.cast(theResource);
				url = r.getUrl();
			}
			if (classR4B.isInstance(theResource)) {
				R4B r = classR4B.cast(theResource);
				url = r.getUrl();
			}
			if (classR5.isInstance(theResource)) {
				R5 r = classR5.cast(theResource);
				url = r.getUrl();
			}
		
			if (url != null) {
				MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(url, cliContext, true, false);
				if (matchboxEngine == null) {
					matchboxEngine = matchboxEngineSupport.getMatchboxEngine("default", cliContext, true, false);
				}
				if (matchboxEngine != null) {
					Resource existing = matchboxEngine.getCanonicalResourceR4(url);
					if (existing != null) {
						theResource.setId(existing.getId());
						matchboxEngine.dropResource(resourceType, existing.getId());
					} else {
						if (theResource.getIdElement().isEmpty()) {
							theResource.setId(url.substring(url.lastIndexOf("/") + 1));
						}  else {
							theResource.setId(theResource.getIdElement().getIdPart());
						}
					}
					if (classR4.isInstance(theResource)) {
						R4 r4 = classR4.cast(theResource);
						r4.getMeta().setLastUpdated(new Date());
						matchboxEngine.addCanonicalResource(r4);
					}
					if (classR4B.isInstance(theResource)) {
						R4B r4b = classR4B.cast(theResource);
						r4b.getMeta().setLastUpdated(new Date());
						matchboxEngine.addCanonicalResource(r4b);
					}
					if (classR5.isInstance(theResource)) {
						R5 r5 = classR5.cast(theResource);
						r5.getMeta().setLastUpdated(new Date());
						matchboxEngine.addCanonicalResource(r5);
					}
					MethodOutcome methodOutcome = new MethodOutcome();
					methodOutcome.setCreated(true);
					methodOutcome.setResource(theResource);
					return methodOutcome;
				}
			}
			throw new ResourceNotFoundException("matchbox engine not found for url " + url + " and fhir version " + cliContext.getFhirVersion());
		} else {
			throw new MethodNotAllowedException("Creating conformance resources is only allowed in development mode, set matchbox.fhir.context.onlyOneEngine=true in application.yaml");
		}
	}

	@Update
	public MethodOutcome update(HttpServletRequest theRequest,  @ResourceParam IDomainResource theResource,  @IdParam IIdType theId,
		@ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

		String url = null;
		CliContext cliContext = new CliContext(this.cliContext);
		cliContext.setFhirVersion(getFhirVersion(theResource));
		if (cliContext.getOnlyOneEngine()) {
			if (classR4.isInstance(theResource)) {
				R4 r = classR4.cast(theResource);
				r.getMeta().setLastUpdated(new Date());
				url = r.getUrl();
			}
			if (classR4B.isInstance(theResource)) {
				R4B r = classR4B.cast(theResource);
				r.getMeta().setLastUpdated(new Date());
				url = r.getUrl();
			}
			if (classR5.isInstance(theResource)) {
				R5 r = classR5.cast(theResource);
				r.getMeta().setLastUpdated(new Date());
				url = r.getUrl();
			}

			if (url!=null) {
				MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(url, cliContext, true, false);
				if (matchboxEngine == null) {
					matchboxEngine = matchboxEngineSupport.getMatchboxEngine("default", cliContext, true, false);
				}
				if (matchboxEngine != null) {
					Resource existing = matchboxEngine.getCanonicalResourceR4(url);
					if (existing != null) {
						theResource.setId(existing.getId());
						matchboxEngine.dropResource(resourceType, existing.getId());
					} else {
						if (theResource.getIdElement().isEmpty()) {
							theResource.setId(url.substring(url.lastIndexOf("/") + 1));
						}  else {
							theResource.setId(theResource.getIdElement().getIdPart());
						}
					}
					if (classR4.isInstance(theResource)) {
						R4 r4 = classR4.cast(theResource);
						matchboxEngine.addCanonicalResource(r4);
					}
					if (classR4B.isInstance(theResource)) {
						R4B r4b = classR4B.cast(theResource);
						matchboxEngine.addCanonicalResource(r4b);
					}
					if (classR5.isInstance(theResource)) {
						R5 r5 = classR5.cast(theResource);
						matchboxEngine.addCanonicalResource(r5);
					}
					MethodOutcome methodOutcome = new MethodOutcome();
					methodOutcome.setCreated(false);
					methodOutcome.setResource(theResource);
					return methodOutcome;
				}
			}
			throw new ResourceNotFoundException("matchbox engine not found for url " + url + " and fhir version " + cliContext.getFhirVersion());
		} else {
			throw new MethodNotAllowedException("Updating conformance resources is only allowed in development mode, set matchbox.fhir.context.onlyOneEngine=true in application.yaml");
		}
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return switch (this.myCtx.getVersion().getVersion()) {
			case R4 -> this.classR4;
			case R4B -> this.classR4B;
			case R5 -> this.classR5;
			default -> throw new MatchboxUnsupportedFhirVersionException("ConformancePackageResourceProvider",
																							 this.myCtx.getVersion().getVersion());
		};
	}

}
