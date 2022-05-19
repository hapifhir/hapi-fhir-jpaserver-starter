package ch.ahdis.matchbox.mappinglanguage;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.hl7.fhir.common.hapi.validation.validator.VersionSpecificWorkerContextWrapper;
import org.hl7.fhir.common.hapi.validation.validator.VersionTypeConverterR4;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.utils.ToolingExtensions;
import org.hl7.fhir.r5.context.SimpleWorkerContext.IValidatorFactory;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.utils.XVerExtensionManager;
import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.utilities.OIDUtils;
import org.hl7.fhir.validation.instance.InstanceValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;

/**
 * Adaptions for StructrueMaps transformation in 5 using
 * VersionSpecificWorkerContextWrapper
 *
 */
public class ConvertingWorkerContext extends VersionSpecificWorkerContextWrapper {

  private List<org.hl7.fhir.r5.model.StructureDefinition> myAllStructures = null;

  
  @Override
  // see Issue https://github.com/ahdis/matchbox/issues/48
  // ca.uhn.fhir.rest.server.exceptions.InternalErrorException: HAPI-0389: Failed to call access method: org.hl7.fhir.exceptions.FHIRException: Exception executing transform observation.category = cc('http://terminology.hl7.org/CodeSystem/observation-category', 'social-history', 'Social History') on Rule "rule-11": HAPI-0667: Can not find resource of type ValueSet with uri http://terminology.hl7.org/CodeSystem/observation-category
  // workaround, we don't through an exception when we don't find the uri
  public <T extends Resource> T fetchResourceWithException(Class<T> theClass_, String theUri) throws FHIRException {
    T retVal = fetchResource(theClass_, theUri);
    return retVal;
  }

  // see Issue https://github.com/ahdis/matchbox/issues/31  
  // this function gets now only the base StructureDefinition for R4 which the FHIRPathEngine is using to initialize itself
  @Override
  public List<org.hl7.fhir.r5.model.StructureDefinition> allStructures() {

    List<org.hl7.fhir.r5.model.StructureDefinition> retVal = myAllStructures;
    if (retVal == null) {
      DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport(FhirContext.forR4Cached());
      retVal = new ArrayList<>();
      for (IBaseResource next : defaultProfileValidationSupport.fetchAllStructureDefinitions()) {
        try {
          Resource converted = myModelConverter.toCanonical(next);
          retVal.add((org.hl7.fhir.r5.model.StructureDefinition) converted);
        } catch (FHIRException e) {
          throw new InternalErrorException(e);
        }
      }
      myAllStructures = retVal;
    }

    return retVal;
  }


  // Logical Models can be defined by the type, we ch
  @Override
  public org.hl7.fhir.r5.model.StructureDefinition fetchTypeDefinition(String typeName) {
    if (typeName == null) {
      return null;
    }
    String ns = null;
    String type = null;
    if (typeName.contains("|")) {
      ns = typeName.substring(0, typeName.indexOf("|"));
      type = typeName.substring(typeName.indexOf("|")+1);
    } else {
      type = typeName;
    }
    for (org.hl7.fhir.r5.model.StructureDefinition sd : this.allStructures()) {
      if (sd.getDerivation() == org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule.SPECIALIZATION && !sd.getUrl().startsWith("http://hl7.org/fhir/StructureDefinition/de-")) {
        if(type.equals(sd.getType()) && (ns == null || ns.equals(FormatUtilities.FHIR_NS)) && !org.hl7.fhir.r5.utils.ToolingExtensions.hasExtension(sd, "http://hl7.org/fhir/StructureDefinition/elementdefinition-namespace"))
          return sd;
        String sns = org.hl7.fhir.r5.utils.ToolingExtensions.readStringExtension(sd, "http://hl7.org/fhir/StructureDefinition/elementdefinition-namespace");
        if ((type.equals(sd.getType()) || type.equals(sd.getName())) && ns != null && ns.equals(sns))
          return sd;
      }
    } 
    if (myDaoRegistry != null) {
      IBundleProvider search = null;
      SearchParameterMap params = new SearchParameterMap();
      params.setLoadSynchronousUpTo(100);
      params.add(org.hl7.fhir.r4.model.StructureDefinition.SP_TYPE, new UriParam(type));
      search = myDaoRegistry.getResourceDao("StructureDefinition").search(params);
      Integer size = search.size();
      if (size == null || size == 0) {
        return null;
      }
      for (IBaseResource resource : search.getAllResources()) {
        org.hl7.fhir.r4.model.StructureDefinition sd = (org.hl7.fhir.r4.model.StructureDefinition) resource;
        if (sd.getDerivation() == TypeDerivationRule.SPECIALIZATION && !sd.getUrl().startsWith("http://hl7.org/fhir/StructureDefinition/de-")) {
          if(type.equals(sd.getType()) && (ns == null || ns.equals(FormatUtilities.FHIR_NS)) && !ToolingExtensions.hasExtension(sd, "http://hl7.org/fhir/StructureDefinition/elementdefinition-namespace"))
            return (org.hl7.fhir.r5.model.StructureDefinition) myModelConverter.toCanonical(sd);
          String sns = ToolingExtensions.readStringExtension(sd, "http://hl7.org/fhir/StructureDefinition/elementdefinition-namespace");
          if ((type.equals(sd.getType()) || type.equals(sd.getName())) && ns != null && ns.equals(sns))
            return (org.hl7.fhir.r5.model.StructureDefinition) myModelConverter.toCanonical(sd);
        }
      } 
    }
    return null;
  }
  
  @Override
  public List<String> getTypeNames() {
    List<String> result = new ArrayList<String>();
    for (org.hl7.fhir.r5.model.StructureDefinition sd : getStructures()) {
      if (sd.getKind() != StructureDefinitionKind.LOGICAL && sd.getDerivation() == org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule.SPECIALIZATION)
        result.add(sd.getName());
    }
    Collections.sort(result);
    return result;
  }


  private static final Logger ourLog = LoggerFactory.getLogger(ConvertingWorkerContext.class);
  private final ValidationSupportContext myValidationSupportContext;
  private final IVersionTypeConverter myModelConverter;
  
  static private IValidatorFactory validatorFactory = null;
  private XVerExtensionManager xverManager;


  @Autowired
  private DaoRegistry myDaoRegistry;

  @Autowired
  protected FhirContext myFhirCtx;

  public ConvertingWorkerContext(IValidationSupport myValidationSupport)
      throws FileNotFoundException, IOException, FHIRException {
    super(new ValidationSupportContext(myValidationSupport), new VersionTypeConverterR4());
    this.myValidationSupportContext = new ValidationSupportContext(myValidationSupport);
    this.myModelConverter = new VersionTypeConverterR4();
    if (ConvertingWorkerContext.validatorFactory==null) {
      ConvertingWorkerContext.validatorFactory = new InstanceValidatorFactory();
    }
  }

  public <T extends org.hl7.fhir.r4.model.Resource> T fetchResourceAsR4(Class<T> class_, String uri) {
    return (T) doFetchResource(class_, uri);
  }
  
  public StructureMap fixMap(@ResourceParam StructureMap theResource) {
    if (theResource!=null) {
      // don't know why a # is prefixed to the contained it
      for (org.hl7.fhir.r5.model.Resource r : theResource.getContained()) {
        if (r instanceof org.hl7.fhir.r5.model.ConceptMap && ((org.hl7.fhir.r5.model.ConceptMap) r).getId().startsWith("#")) {
          r.setId(((org.hl7.fhir.r5.model.ConceptMap) r).getId().substring(1));
        }
      }
    }
    return theResource;
  }
  
  @Override
  public org.hl7.fhir.r5.model.StructureMap getTransform(String url) {
    return fixMap((org.hl7.fhir.r5.model.StructureMap) myModelConverter
        .toCanonical(doFetchResource(org.hl7.fhir.r4.model.StructureMap.class, url)));
  }
  
  @Override
  public <T extends Resource> T fetchResource(Class<T> class_, String uri) {
    if (isBlank(uri)) {
      return null;
    }
    if (class_!=null && "ConceptMap".equals(class_.getSimpleName())) {
      return (T) myModelConverter
          .toCanonical(doFetchResource(org.hl7.fhir.r4.model.ConceptMap.class, uri));
    }
    return super.fetchResource(class_, uri);
  }

  private <T extends IBaseResource> IBaseResource doFetchResource(@Nullable Class<T> theClass, String theUri) {
    IBaseResource myNoMatch = null;
    if (theClass == null || "Resource".equals(theClass.getSimpleName())) {
      Supplier<IBaseResource>[] fetchers = new Supplier[] { () -> doFetchResource(ValueSet.class, theUri),
          () -> doFetchResource(CodeSystem.class, theUri), () -> doFetchResource(StructureDefinition.class, theUri),
          () -> doFetchResource(Questionnaire.class, theUri),() -> doFetchResource(ConceptMap.class, theUri) };
      return Arrays.stream(fetchers).map(t -> t.get()).filter(t -> t != myNoMatch).findFirst().orElse(myNoMatch);
    }

    IdType id = new IdType(theUri);
    boolean localReference = false;
    if (id.hasBaseUrl() == false && id.hasIdPart() == true) {
      localReference = true;
    }

    String resourceName = myValidationSupportContext.getRootValidationSupport().getFhirContext()
        .getResourceType(theClass);
    IBundleProvider search;
    switch (resourceName) {
    case "ValueSet":
      if (localReference) {
        SearchParameterMap params = new SearchParameterMap();
        params.setLoadSynchronousUpTo(1);
        params.add(IAnyResource.SP_RES_ID, new StringParam(theUri));
        search = myDaoRegistry.getResourceDao(resourceName).search(params);
        if (search.size() == 0) {
          params = new SearchParameterMap();
          params.setLoadSynchronousUpTo(1);
          params.add(ValueSet.SP_URL, new UriParam(theUri));
          search = myDaoRegistry.getResourceDao(resourceName).search(params);
        }
      } else {
        int versionSeparator = theUri.lastIndexOf('|');
        SearchParameterMap params = new SearchParameterMap();
        params.setLoadSynchronousUpTo(1);
        if (versionSeparator != -1) {
          params.add(ValueSet.SP_VERSION, new TokenParam(theUri.substring(versionSeparator + 1)));
          params.add(ValueSet.SP_URL, new UriParam(theUri.substring(0, versionSeparator)));
        } else {
          params.add(ValueSet.SP_URL, new UriParam(theUri));
        }
        params.setSort(new SortSpec("_lastUpdated").setOrder(SortOrderEnum.DESC));
        search = myDaoRegistry.getResourceDao(resourceName).search(params);
      }
      break;
    case "StructureDefinition": {
      // Don't allow the core FHIR definitions to be overwritten
      if (theUri.startsWith("http://hl7.org/fhir/StructureDefinition/")) {
        String typeName = theUri.substring("http://hl7.org/fhir/StructureDefinition/".length());
        if (myValidationSupportContext.getRootValidationSupport().getFhirContext()
            .getElementDefinition(typeName) != null) {
          return myNoMatch;
        }
      }
      SearchParameterMap params = new SearchParameterMap();
      params.setLoadSynchronousUpTo(1);
      params.add(StructureDefinition.SP_URL, new UriParam(theUri));
      search = myDaoRegistry.getResourceDao("StructureDefinition").search(params);
      break;
    }
    case "Questionnaire": {
      SearchParameterMap params = new SearchParameterMap();
      params.setLoadSynchronousUpTo(1);
      if (localReference) {
        params.add(IAnyResource.SP_RES_ID, new StringParam(id.getIdPart()));
      } else {
        params.add(Questionnaire.SP_URL, new UriParam(id.getValue()));
      }
      search = myDaoRegistry.getResourceDao("Questionnaire").search(params);
      break;
    }
    case "CodeSystem": {
      int versionSeparator = theUri.lastIndexOf('|');
      SearchParameterMap params = new SearchParameterMap();
      params.setLoadSynchronousUpTo(1);
      if (versionSeparator != -1) {
        params.add(CodeSystem.SP_VERSION, new TokenParam(theUri.substring(versionSeparator + 1)));
        params.add(CodeSystem.SP_URL, new UriParam(theUri.substring(0, versionSeparator)));
      } else {
        params.add(CodeSystem.SP_URL, new UriParam(theUri));
      }
      params.setSort(new SortSpec("_lastUpdated").setOrder(SortOrderEnum.DESC));
      search = myDaoRegistry.getResourceDao(resourceName).search(params);
      break;
    }
    case "ImplementationGuide":
    case "SearchParameter": {
      SearchParameterMap params = new SearchParameterMap();
      params.setLoadSynchronousUpTo(1);
      params.add(ImplementationGuide.SP_URL, new UriParam(theUri));
      search = myDaoRegistry.getResourceDao(resourceName).search(params);
      break;
    }
    case "StructureMap":
      if (localReference) {
        SearchParameterMap params = new SearchParameterMap();
        params.setLoadSynchronousUpTo(1);
        params.add(IAnyResource.SP_RES_ID, new StringParam(theUri));
        search = myDaoRegistry.getResourceDao(resourceName).search(params);
        if (search.size() == 0) {
          params = new SearchParameterMap();
          params.setLoadSynchronousUpTo(1);
          params.add(ValueSet.SP_URL, new UriParam(theUri));
          search = myDaoRegistry.getResourceDao(resourceName).search(params);
        }
      } else {
        int versionSeparator = theUri.lastIndexOf('|');
        SearchParameterMap params = new SearchParameterMap();
        params.setLoadSynchronousUpTo(1);
        if (versionSeparator != -1) {
          params.add(ValueSet.SP_VERSION, new TokenParam(theUri.substring(versionSeparator + 1)));
          params.add(ValueSet.SP_URL, new UriParam(theUri.substring(0, versionSeparator)));
        } else {
          params.add(ValueSet.SP_URL, new UriParam(theUri));
        }
        params.setSort(new SortSpec("_lastUpdated").setOrder(SortOrderEnum.DESC));
        search = myDaoRegistry.getResourceDao(resourceName).search(params);
      }
      break;
    case "ConceptMap":
      if (localReference) {
        SearchParameterMap params = new SearchParameterMap();
        params.setLoadSynchronousUpTo(1);
        params.add(IAnyResource.SP_RES_ID, new StringParam(theUri));
        search = myDaoRegistry.getResourceDao(resourceName).search(params);
        if (search.size() == 0) {
          params = new SearchParameterMap();
          params.setLoadSynchronousUpTo(1);
          params.add(ValueSet.SP_URL, new UriParam(theUri));
          search = myDaoRegistry.getResourceDao(resourceName).search(params);
        }
      } else {
        int versionSeparator = theUri.lastIndexOf('|');
        SearchParameterMap params = new SearchParameterMap();
        params.setLoadSynchronousUpTo(1);
        if (versionSeparator != -1) {
          params.add(ValueSet.SP_VERSION, new TokenParam(theUri.substring(versionSeparator + 1)));
          params.add(ValueSet.SP_URL, new UriParam(theUri.substring(0, versionSeparator)));
        } else {
          params.add(ValueSet.SP_URL, new UriParam(theUri));
        }
        params.setSort(new SortSpec("_lastUpdated").setOrder(SortOrderEnum.DESC));
        search = myDaoRegistry.getResourceDao(resourceName).search(params);
      }
      break;
    default:
      throw new IllegalArgumentException("Can't fetch resource type: " + resourceName);
    }

    Integer size = search.size();
    if (size == null || size == 0) {
      return myNoMatch;
    }

    if (size > 1) {
      ourLog.warn("Found multiple {} instances with URL search value of: {}", resourceName, theUri);
    }

    return search.getResources(0, 1).get(0);
  }

  public String oid2Uri(String oid) {
    return OIDUtils.getUriForOid(oid);
  }
  
  @Override
  public IResourceValidator newValidator() throws FHIRException {
    return validatorFactory.makeValidator(this, xverManager);
  }
}
