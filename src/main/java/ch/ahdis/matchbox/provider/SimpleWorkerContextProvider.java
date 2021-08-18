package ch.ahdis.matchbox.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.dstu2.model.ResourceType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.model.CanonicalResource;
import org.hl7.fhir.r5.model.Questionnaire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;

/**
 * This class is a simple implementation of the resource provider
 * interface that uses a HashMap to store all resources in memory.
 * <p>
 * This class currently supports the following FHIR operations:
 * </p>
 * <ul>
 * <li>Create</li>
 * <li>Update existing resource</li>
 * <li>Update non-existing resource (e.g. create with client-supplied ID)</li>
 * <li>Delete</li>
 * <li>Search by resource type with no parameters</li>
 * </ul>
 *
 * @param <T> The resource type to support
 */
public class SimpleWorkerContextProvider<T extends Resource> implements IResourceProvider {
	private static final Logger log = LoggerFactory.getLogger(SimpleWorkerContextProvider.class);
	private final Class<T> myResourceType;
	protected final SimpleWorkerContext fhirContext;
	private final String myResourceName;
	protected Map<String, TreeMap<Long, T>> myIdToVersionToResourceMap = Collections.synchronizedMap(new LinkedHashMap<>());
	protected Map<String, LinkedList<T>> myIdToHistory = Collections.synchronizedMap(new LinkedHashMap<>());
	protected LinkedList<T> myTypeHistory = new LinkedList<>();
	private AtomicLong myDeleteCount = new AtomicLong(0);
	private AtomicLong mySearchCount = new AtomicLong(0);
	private AtomicLong myUpdateCount = new AtomicLong(0);
	private AtomicLong myCreateCount = new AtomicLong(0);
	private AtomicLong myReadCount = new AtomicLong(0);

	/**
	 * Constructor
	 *
	 * @param theFhirContext  The FHIR context
	 * @param theResourceType The resource type to support
	 */
	public SimpleWorkerContextProvider(SimpleWorkerContext simpleWorkerContext, Class<T> theResourceType) {
	  fhirContext = simpleWorkerContext;
		myResourceType = theResourceType;
//		myResourceName = myFhirContext.getResourceDefinition(theResourceType).getName();
    myResourceName = theResourceType.getSimpleName();
	}

//	@Create
//	public MethodOutcome create(@ResourceParam T theResource) {
//		createInternal(theResource);
//
//		myCreateCount.incrementAndGet();
//
//		return new MethodOutcome()
//			.setCreated(true)
//			.setResource(theResource)
//			.setId(theResource.getIdElement());
//	}

	@Override
	public Class<T> getResourceType() {
		return myResourceType;
	}
	

	@Read
	public T read(@IdParam IIdType theId, RequestDetails theRequestDetails) {
	  
	    org.hl7.fhir.r5.model.Resource theResource = fhirContext.fetchResourceById(this.myResourceName,theId.getIdPart());
	    
	    @SuppressWarnings("unchecked")
      T retVal = (T) VersionConvertor_40_50.convertResource(theResource);

//		TreeMap<Long, T> versions = myIdToVersionToResourceMap.get(theId.getIdPart());
//		if (versions == null || versions.isEmpty()) {
//			throw new ResourceNotFoundException(theId);
//		}
//
//		T retVal;
//		if (theId.hasVersionIdPart()) {
//			Long versionId = theId.getVersionIdPartAsLong();
//			if (!versions.containsKey(versionId)) {
//				throw new ResourceNotFoundException(theId);
//			} else {
//				T resource = versions.get(versionId);
//				if (resource == null) {
//					throw new ResourceGoneException(theId);
//				}
//				retVal = resource;
//			}
//
//		} else {
//			retVal = versions.lastEntry().getValue();
//		}

		myReadCount.incrementAndGet();

//		retVal = fireInterceptorsAndFilterAsNeeded(retVal, theRequestDetails);
//		if (retVal == null) {
//			throw new ResourceNotFoundException(theId);
//		}
		return retVal;
	}
	
	public List<T> search() {
	  List<org.hl7.fhir.r5.model.Resource> resources = new ArrayList<org.hl7.fhir.r5.model.Resource>();
	  
    switch(this.myResourceName) {
  	  case "ImplementationGuide":
  	    resources.addAll(fhirContext.allImplementationGuides());
  	    break;
      case "StructureDefinition":
        resources.addAll(fhirContext.allStructures());
        break;
      case "StructureMap":
        resources.addAll(fhirContext.listTransforms());
        break;
      case "ConceptMap":
        resources.addAll(fhirContext.listMaps());
        break;
      case "Questionnaire":
        List<CanonicalResource> confResources = fhirContext.allConformanceResources();
        confResources.removeIf(filter -> !filter.getClass().equals(Questionnaire.class));
        resources.addAll(confResources);
        break;
      default:
        log.error(this.myResourceName + " not supported");
        return null;
	  }
	  List<T> result = new ArrayList<T>();
	  for (org.hl7.fhir.r5.model.Resource resource: resources) {
	    @SuppressWarnings("unchecked")
      T retVal = (T) VersionConvertor_40_50.convertResource(resource);
	    result.add(retVal);
	  }	  
	  return result;
	}


	@Search
	public List<T> searchAll(RequestDetails theRequestDetails) {
		List<T> retVal = search();
    
		mySearchCount.incrementAndGet();
		return retVal;

//		return fireInterceptorsAndFilterAsNeeded(retVal, theRequestDetails);
	}
}

//	@Search
//	public List<T> searchById(
//		@RequiredParam(name = "_id") TokenAndListParam theIds, RequestDetails theRequestDetails) {
//
//		List<T> retVal = new ArrayList<>();
//
//		for (TreeMap<Long, T> next : myIdToVersionToResourceMap.values()) {
//			if (next.isEmpty() == false) {
//				T nextResource = next.lastEntry().getValue();
//
//				boolean matches = true;
//				if (theIds != null && theIds.getValuesAsQueryTokens().size() > 0) {
//					for (TokenOrListParam nextIdAnd : theIds.getValuesAsQueryTokens()) {
//						matches = false;
//						for (TokenParam nextOr : nextIdAnd.getValuesAsQueryTokens()) {
//							if (nextOr.getValue().equals(nextResource.getIdElement().getIdPart())) {
//								matches = true;
//							}
//						}
//						if (!matches) {
//							break;
//						}
//					}
//				}
//
//				if (!matches) {
//					continue;
//				}
//
//				retVal.add(nextResource);
//			}
//		}
//
//		mySearchCount.incrementAndGet();
//
//		return fireInterceptorsAndFilterAsNeeded(retVal, theRequestDetails);
//	}

//	private IIdType store(@ResourceParam T theResource, String theIdPart, Long theVersionIdPart) {
//		IIdType id = myFhirContext.getVersion().newIdType();
//		String versionIdPart = Long.toString(theVersionIdPart);
//		id.setParts(null, myResourceName, theIdPart, versionIdPart);
//		if (theResource != null) {
//			theResource.setId(id);
//		}
//
//		/*
//		 * This is a bit of magic to make sure that the versionId attribute
//		 * in the resource being stored accurately represents the version
//		 * that was assigned by this provider
//		 */
//		if (theResource != null) {
//			if (myFhirContext.getVersion().getVersion() == FhirVersionEnum.DSTU2) {
//				ResourceMetadataKeyEnum.VERSION.put((IResource) theResource, versionIdPart);
//			} else {
//				BaseRuntimeChildDefinition metaChild = myFhirContext.getResourceDefinition(myResourceType).getChildByName("meta");
//				List<IBase> metaValues = metaChild.getAccessor().getValues(theResource);
//				if (metaValues.size() > 0) {
//					IBase meta = metaValues.get(0);
//					BaseRuntimeElementCompositeDefinition<?> metaDef = (BaseRuntimeElementCompositeDefinition<?>) myFhirContext.getElementDefinition(meta.getClass());
//					BaseRuntimeChildDefinition versionIdDef = metaDef.getChildByName("versionId");
//					List<IBase> versionIdValues = versionIdDef.getAccessor().getValues(meta);
//					if (versionIdValues.size() > 0) {
//						IPrimitiveType<?> versionId = (IPrimitiveType<?>) versionIdValues.get(0);
//						versionId.setValueAsString(versionIdPart);
//					}
//				}
//			}
//		}
//
//		ourLog.info("Storing resource with ID: {}", id.getValue());
//
//		// Store to ID->version->resource map
//		TreeMap<Long, T> versionToResource = getVersionToResource(theIdPart);
//		versionToResource.put(theVersionIdPart, theResource);
//
//		// Store to type history map
//		myTypeHistory.addFirst(theResource);
//
//		// Store to ID history map
//		myIdToHistory.computeIfAbsent(theIdPart, t -> new LinkedList<>());
//		myIdToHistory.get(theIdPart).addFirst(theResource);
//
//		// Return the newly assigned ID including the version ID
//		return id;
//	}
//
//	/**
//	 * @param theConditional This is provided only so that subclasses can implement if they want
//	 */
//	@Update
//	public MethodOutcome update(
//		@ResourceParam T theResource,
//		@ConditionalUrlParam String theConditional) {
//
//		ValidateUtil.isTrueOrThrowInvalidRequest(isBlank(theConditional), "This server doesn't support conditional update");
//
//		boolean created = updateInternal(theResource);
//		myUpdateCount.incrementAndGet();
//
//		return new MethodOutcome()
//			.setCreated(created)
//			.setResource(theResource)
//			.setId(theResource.getIdElement());
//	}
//
//	private boolean updateInternal(@ResourceParam T theResource) {
//		String idPartAsString = theResource.getIdElement().getIdPart();
//		TreeMap<Long, T> versionToResource = getVersionToResource(idPartAsString);
//
//		Long versionIdPart;
//		boolean created;
//		if (versionToResource.isEmpty()) {
//			versionIdPart = 1L;
//			created = true;
//		} else {
//			versionIdPart = versionToResource.lastKey() + 1L;
//			created = false;
//		}
//
//		IIdType id = store(theResource, idPartAsString, versionIdPart);
//		theResource.setId(id);
//		return created;
//	}
//
//	/**
//	 * This is a utility method that can be used to store a resource without
//	 * having to use the outside API. In this case, the storage happens without
//	 * any interaction with interceptors, etc.
//	 *
//	 * @param theResource The resource to store. If the resource has an ID, that ID is updated.
//	 * @return Return the ID assigned to the stored resource
//	 */
//	public IIdType store(T theResource) {
//		if (theResource.getIdElement().hasIdPart()) {
//			updateInternal(theResource);
//		} else {
//			createInternal(theResource);
//		}
//		return theResource.getIdElement();
//	}
//
//	/**
//	 * Returns an unmodifiable list containing the current version of all resources stored in this provider
//	 *
//	 * @since 4.1.0
//	 */
//	public List<T> getStoredResources() {
//		List<T> retVal = new ArrayList<>();
//		for (TreeMap<Long, T> next : myIdToVersionToResourceMap.values()) {
//			retVal.add(next.lastEntry().getValue());
//		}
//		return Collections.unmodifiableList(retVal);
//	}
//
//	private static <T extends IBaseResource> T fireInterceptorsAndFilterAsNeeded(T theResource, RequestDetails theRequestDetails) {
//		List<T> output = fireInterceptorsAndFilterAsNeeded(Lists.newArrayList(theResource), theRequestDetails);
//		if (output.size() == 1) {
//			return theResource;
//		} else {
//			return null;
//		}
//	}
//
//	private static <T extends IBaseResource> List<T> fireInterceptorsAndFilterAsNeeded(List<T> theResources, RequestDetails theRequestDetails) {
//		ArrayList<T> resourcesToReturn = new ArrayList<>(theResources);
//
//		if (theRequestDetails != null) {
//			IInterceptorBroadcaster interceptorBroadcaster = theRequestDetails.getInterceptorBroadcaster();
//
//			// Call the STORAGE_PREACCESS_RESOURCES pointcut (used for consent/auth interceptors)
//			SimplePreResourceAccessDetails preResourceAccessDetails = new SimplePreResourceAccessDetails(resourcesToReturn);
//			HookParams params = new HookParams()
//				.add(RequestDetails.class, theRequestDetails)
//				.addIfMatchesType(ServletRequestDetails.class, theRequestDetails)
//				.add(IPreResourceAccessDetails.class, preResourceAccessDetails);
//			interceptorBroadcaster.callHooks(Pointcut.STORAGE_PREACCESS_RESOURCES, params);
//			preResourceAccessDetails.applyFilterToList();
//
//			// Call the STORAGE_PREACCESS_RESOURCES pointcut (used for consent/auth interceptors)
//			SimplePreResourceShowDetails preResourceShowDetails = new SimplePreResourceShowDetails(resourcesToReturn);
//			HookParams preShowParams = new HookParams()
//				.add(RequestDetails.class, theRequestDetails)
//				.addIfMatchesType(ServletRequestDetails.class, theRequestDetails)
//				.add(IPreResourceShowDetails.class, preResourceShowDetails);
//			interceptorBroadcaster.callHooks(Pointcut.STORAGE_PRESHOW_RESOURCES, preShowParams);
//
//		}
//
//		return resourcesToReturn;
//	}
//}
