package ch.ahdis.matchbox.mappinglanguage;

import java.beans.PropertyChangeSupport;

import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionKind;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ch.ahdis.matchbox.provider.SimpleWorkerContextProvider;

public class StructureDefinitionProvider extends SimpleWorkerContextProvider<StructureDefinition> {
  
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  
  public StructureDefinitionProvider(SimpleWorkerContext simpleWorkerContext) {
    super(simpleWorkerContext, StructureDefinition.class);
  }

  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructureDefinitionProvider.class);
  
  @Create
  public MethodOutcome createStructureDefinition(@ResourceParam StructureDefinition theResource) {
    log.debug("created structuredmap, caching");
    theResource.setSnapshot(null);
    // FIXME somehow the structuremaps are resolved by type and not by cannonical
    if (theResource.getKind()!=null && theResource.getKind() == StructureDefinitionKind.LOGICAL) {
      if (theResource.getType()!=null) {
          for (org.hl7.fhir.r5.model.StructureDefinition structure : fhirContext.allStructures()) {
              if (structure.getType()!=null && structure.getType().equals(theResource.getType())) {
                fhirContext.dropResource(structure);
              }
          }
      }
    }
    theResource.setId(theResource.getType());
    updateWorkerContext(theResource);
    MethodOutcome retVal = new MethodOutcome();
    retVal.setCreated(true);
    retVal.setResource(getByUrl(theResource.getUrl()));
    return retVal;
  }

  public void updateWorkerContext(StructureDefinition theResource) {
    org.hl7.fhir.r5.model.StructureDefinition cached = fhirContext.fetchResource(org.hl7.fhir.r5.model.StructureDefinition.class, theResource.getUrl());
    if (cached != null) {
      fhirContext.dropResource(cached);
    }    
    org.hl7.fhir.r5.model.StructureDefinition r5Structure = (org.hl7.fhir.r5.model.StructureDefinition) VersionConvertor_40_50.convertResource(theResource);
    fhirContext.generateSnapshot(r5Structure, true);
    fhirContext.cacheResource(r5Structure);
  }
  
  @Delete()
  public void deleteStructureDefinition(@IdParam IdType theId) {
      org.hl7.fhir.r5.model.StructureDefinition cached = fhirContext.fetchResource(org.hl7.fhir.r5.model.StructureDefinition.class, theId.getId());
      if (cached == null) {
          throw new ResourceNotFoundException("Unknown version");
      }
      fhirContext.dropResource(cached);
      return; //
  }
  
  @Update
  public MethodOutcome update(@IdParam IdType theId, @ResourceParam StructureDefinition theResource) {
     updateWorkerContext(theResource);
     return new MethodOutcome();
  }
  
  @Read()
  public org.hl7.fhir.r4.model.Resource getResourceById(@IdParam IdType theId) {
    return VersionConvertor_40_50.convertResource(getByUrl(theId.getId()));
  }
  
  public org.hl7.fhir.r5.model.StructureDefinition getByUrl(String url) {
    return fhirContext.fetchResource(org.hl7.fhir.r5.model.StructureDefinition.class, url);
  }
  
}
