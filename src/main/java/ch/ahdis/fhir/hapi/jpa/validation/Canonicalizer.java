package ch.ahdis.fhir.hapi.jpa.validation;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseHasExtensions;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.interceptor.model.RequestPartitionId;
import ca.uhn.fhir.jpa.model.entity.ResourceTable;
import ca.uhn.fhir.jpa.searchparam.extractor.ResourceIndexedSearchParams;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.storage.TransactionDetails;
import ca.uhn.fhir.util.FhirTerser;
import ca.uhn.fhir.util.ResourceReferenceInfo;

public class Canonicalizer {

  protected FhirContext fhirContext;
  protected FhirTerser fhirTerser;

  public Canonicalizer(FhirContext fhirContext) {
    this.fhirContext = fhirContext;
    this.fhirTerser = this.fhirContext.newTerser();
  }

  private static Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareTo);

  private static Comparator<IBaseExtension<?, ?>> extensionComparator = Comparator
      .comparing(IBaseExtension<?, ?>::getUrl, nullSafeStringComparator);
  
  public void sortExtensionRecursive(IBase ibase) {
    IBaseHasExtensions baseHasExtensions = validateExtensionSupport(ibase);
    baseHasExtensions.getExtension().sort(extensionComparator);
    for(IBaseExtension<?, ?> extension : baseHasExtensions.getExtension()) {
      sortExtensionRecursive(extension);
    }
  }
  

  public IBaseResource canonicalize(IBaseResource input) {
    // Resource
    clearField(this.fhirContext, input, "id");
    clearField(this.fhirContext, input, "meta");
    clearField(this.fhirContext, input, "implicitRules");

    // DomainResource TODO

    // TODO ... text 0..1 Narrative Text summary of the resource, for human
    // interpretation
    // TODO ... contained 0..* Resource Contained, inline Resources
//    this.fhirTerser.getAllResourceReferences(theResource)

//    or maybe 
//    
//    private void extractResourceLinksForContainedResources(RequestPartitionId theRequestPartitionId, ResourceIndexedSearchParams theParams, ResourceTable theEntity, IBaseResource theResource, TransactionDetails theTransactionDetails, boolean theFailOnInvalidReference, RequestDetails theRequest) {
//
//      FhirTerser terser = myContext.newTerser();
//
//      // 1. get all contained resources
//      Collection<IBaseResource> containedResources = terser.getAllEmbeddedResources(theResource, false);
//
//      extractResourceLinksForContainedResources(theRequestPartitionId, theParams, theEntity, theResource, theTransactionDetails, theFailOnInvalidReference, theRequest, containedResources, new HashSet<>());
//    }
//
//    private void extractResourceLinksForContainedResources(RequestPartitionId theRequestPartitionId, ResourceIndexedSearchParams theParams, ResourceTable theEntity, IBaseResource theResource, TransactionDetails theTransactionDetails, boolean theFailOnInvalidReference, RequestDetails theRequest, Collection<IBaseResource> theContainedResources, Collection<IBaseResource> theAlreadySeenResources) {

      // 2

    // sort contained resource depth first
    // check all references in the resource which start with an # (contained resource), for each reference that is not yet in the contained list
    List<ResourceReferenceInfo> resourceReferences = this.fhirTerser.getAllResourceReferences(input);
    Collection<IBaseResource> containedResources = this.fhirTerser.getAllEmbeddedResources(input, false);
    
    final Map<String, String> ids = new  HashMap<String, String>();
    int contained = 0;
    for (ResourceReferenceInfo resourceReference : resourceReferences) {
      String refContainted = resourceReference.getResourceReference().getReferenceElement().getValue();
      if (!resourceReference.getName().startsWith("contained") && refContainted.startsWith("#") && refContainted.length()>1) {
        String containedId = refContainted.substring(1);
        if (ids.get(containedId)==null) {
          String newContainedId = "c"+contained++;
          ids.put(containedId, newContainedId);
          //   add the contained resource, (replace id?)
          //     for the contained resource check all references in the resource which start with an #, add the contained resource ...
          // check contained resource itself
        }
      }
      
    }
    //   add the contained resource, (replace id?)
    //     for the contained resource check all references in the resource which start with an #, add the contained resource ...
    // check contained resource itself
    
    // ... extension 0..* Extension Additional content defined by implementations
    // sort in complex extensions according to the url
    sortExtensionRecursive(input);
    
    // TODO modifierExtension ?! 0..* Extension Extensions that cannot be ignored
    
    // TODO: if we have * cardinality of a specific extension should we order?
    

    return input;
  }

  private static BaseRuntimeChildDefinition getBaseRuntimeChildDefinition(FhirContext theFhirContext,
      String theFieldName, IBaseResource theFrom) {
    RuntimeResourceDefinition definition = theFhirContext.getResourceDefinition(theFrom);
    BaseRuntimeChildDefinition childDefinition = definition.getChildByName(theFieldName);
    Validate.notNull(childDefinition);
    return childDefinition;
  }

  public static void clearField(FhirContext theFhirContext, IBaseResource theResource, String theFieldName) {
    BaseRuntimeChildDefinition childDefinition = getBaseRuntimeChildDefinition(theFhirContext, theFieldName,
        theResource);
    childDefinition.getMutator().setValue(theResource, null);
  }

  private static IBaseHasExtensions validateExtensionSupport(IBase theBase) {
    if (!(theBase instanceof IBaseHasExtensions)) {
      throw new IllegalArgumentException(
          Msg.code(1747) + String.format("Expected instance that supports extensions, but got %s", theBase));
    }
    return (IBaseHasExtensions) theBase;
  }

}
