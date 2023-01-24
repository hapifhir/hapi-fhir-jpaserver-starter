package ch.ahdis.matchbox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_40;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBaseBinary;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.binary.svc.NullBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import ca.uhn.fhir.jpa.provider.BaseJpaResourceProvider;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RawParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SearchContainedModeEnum;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.storage.ResourcePersistentId;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.UriAndListParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.util.BinaryUtil;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ca.uhn.fhir.rest.api.MethodOutcome;

@DisallowConcurrentExecution
public class ConformanceResourceProvider<T extends MetadataResource> extends BaseJpaResourceProvider<T> {

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

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConformanceResourceProvider.class);

	@Autowired
	private FhirContext myCtx;

	private String resourceType;

	public ConformanceResourceProvider(String resourceType) {
		super();
		this.resourceType = resourceType;
	}

	@Search(allowUnknownParams = true)
	public ca.uhn.fhir.rest.api.server.IBundleProvider search(javax.servlet.http.HttpServletRequest theServletRequest,
			javax.servlet.http.HttpServletResponse theServletResponse,

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
		startRequest(theServletRequest);
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
			endRequest(theServletRequest);
		}
	}

	public List<CanonicalType> getCanonicals() {
		return new TransactionTemplate(myTxManager).execute(tx -> {
			Slice<NpmPackageVersionResourceEntity> outcome = myPackageVersionResourceDao
					.findByResourceType(PageRequest.of(0, 2147483646), resourceType);
			List<String> versioned = outcome.stream().filter(t -> !t.getPackageVersion().isCurrentVersion())
					.map(t -> (t.getCanonicalUrl() + "|" + t.getCanonicalVersion())).collect(Collectors.toList());
			Slice<NpmPackageVersionResourceEntity> current = myPackageVersionResourceDao
					.findCurrentByResourceType(PageRequest.of(0, 2147483646), resourceType);
			versioned.addAll(current.stream().map(t -> (t.getCanonicalUrl())).collect(Collectors.toList()));
			return versioned.stream().sorted().map(t -> new CanonicalType(t)).collect(Collectors.toList());
		});
	}

	/**
	 * Helper method which will attempt to use the IBinaryStorageSvc to resolve the
	 * binary blob if available. If the bean is unavailable, fallback to assuming we
	 * are using an embedded base64 in the data element.
	 * 
	 * @param theBinary the Binary who's `data` blob you want to retrieve
	 * @return a byte array containing the blob.
	 *
	 * @throws IOException
	 */
	private byte[] fetchBlobFromBinary(IBaseBinary theBinary) throws IOException {
		if (myBinaryStorageSvc != null && !(myBinaryStorageSvc instanceof NullBinaryStorageSvcImpl)) {
			return myBinaryStorageSvc.fetchDataBlobFromBinary(theBinary);
		} else {
			byte[] value = BinaryUtil.getOrCreateData(myCtx, theBinary).getValue();
			if (value == null) {
				throw new InternalErrorException(
						Msg.code(1296) + "Failed to fetch blob from Binary/" + theBinary.getIdElement());
			}
			return value;
		}
	}

	@SuppressWarnings("unchecked")
	private IFhirResourceDao<IBaseBinary> getBinaryDao() {
		return myDaoRegistry.getResourceDao("Binary");
	}

	private org.hl7.fhir.r4.model.Resource loadPackageEntityAdjustId(NpmPackageVersionResourceEntity contents) {
		org.hl7.fhir.r4.model.Resource resource = loadPackageEntity(contents);
		if (resource != null) {
			resource.setId(contents.getId());
		}
		return resource;
	}

	private org.hl7.fhir.r4.model.Resource loadPackageEntity(NpmPackageVersionResourceEntity contents) {
		try {
			ResourcePersistentId binaryPid = new ResourcePersistentId(contents.getResourceBinary().getId());
			IBaseBinary binary = getBinaryDao().readByPid(binaryPid);
			byte[] resourceContentsBytes = fetchBlobFromBinary(binary);
			String resourceContents = new String(resourceContentsBytes, StandardCharsets.UTF_8);
			switch (contents.getFhirVersion()) {
				case DSTU3:
					return VersionConvertorFactory_30_40
							.convertResource(new org.hl7.fhir.dstu3.formats.JsonParser().parse(resourceContents));
				case R4:
					return new org.hl7.fhir.r4.formats.JsonParser().parse(resourceContents);
				case R4B:
					return VersionConvertorFactory_40_50.convertResource(VersionConvertorFactory_43_50
							.convertResource(new org.hl7.fhir.r4b.formats.JsonParser().parse(resourceContents)));
				case R5:
					return VersionConvertorFactory_40_50
							.convertResource(new org.hl7.fhir.r5.formats.JsonParser().parse(resourceContents));
				default:
					log.error("FHIR version not support for loading form matchbox case ");
					throw new RuntimeException(Msg.code(1305) + "Failed to load package resource " + contents);
			}
		} catch (Exception e) {
			throw new RuntimeException(Msg.code(1305) + "Failed to load package resource " + contents, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T read(HttpServletRequest theServletRequest, IIdType theId, RequestDetails theRequestDetails) {

		startRequest(theServletRequest);
		try {
			return new TransactionTemplate(myTxManager).execute(tx -> {
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
					T resource = (T) loadPackageEntityAdjustId(outcome.toList().get(0));
					// if is current we check if already loaded in a own engine and might be updated
					// in the cache
					if (res.getPackageVersion().isCurrentVersion()) {
						MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(resource.getUrl(), null,
								false, false);
						if (matchboxEngine != null) {
							T update = (T) matchboxEngine.getCanonicalResource(resource.getUrl());
							if (update != null) {
								return update;
							}
						}
					}
					return (T) loadPackageEntityAdjustId(outcome.toList().get(0));
				} else {
					return (T) matchboxEngineSupport.getCachedResource(resourceType, pid);
				}
			}
			);
		} finally {
			endRequest(theServletRequest);
		}
	}

	@Override
	public MethodOutcome create(HttpServletRequest theRequest, T theResource, String theConditional,
			RequestDetails theRequestDetails) {

		MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(theResource.getUrl(), null, true,
				false);
		if (matchboxEngine == null) {
			matchboxEngine = matchboxEngineSupport.getMatchboxEngine("default", null, true, false);
		}
		Resource existing = matchboxEngine.getCanonicalResource(theResource.getUrl());
		if (existing != null) {
			theResource.setId(existing.getId());
			matchboxEngine.dropResource(resourceType, existing.getId());
		} else {
			if (theResource.getId() == null) {
				theResource.setId(theResource.getUrl().substring(theResource.getUrl().lastIndexOf("/") + 1));
			}
		}
		matchboxEngine.addCanonicalResource(theResource);
		MethodOutcome methodOutcome = new MethodOutcome();
		methodOutcome.setCreated(true);
		methodOutcome.setResource(theResource);
		return methodOutcome;
	}

	@Override
	public MethodOutcome update(HttpServletRequest theRequest, T theResource, IIdType theId,
			String theConditional, RequestDetails theRequestDetails) {
		MatchboxEngine matchboxEngine = matchboxEngineSupport.getMatchboxEngine(theResource.getUrl(), null, true,
				false);
		Resource existing = matchboxEngine.getCanonicalResource(theResource.getUrl());
		if (existing != null) {
			matchboxEngine.dropResource(resourceType, existing.getId());
		}
		matchboxEngine.addCanonicalResource(theResource);
		MethodOutcome methodOutcome = new MethodOutcome();
		methodOutcome.setCreated(false);
		methodOutcome.setResource(theResource);
		return methodOutcome;
	}

}
