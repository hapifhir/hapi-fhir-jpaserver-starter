package org.hl7.fhir.r5.context;

import java.io.File;

/*
  Copyright (c) 2011+, HL7, Inc.
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without modification, 
  are permitted provided that the following conditions are met:
    
   * Redistributions of source code must retain the above copyright notice, this 
     list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright notice, 
     this list of conditions and the following disclaimer in the documentation 
     and/or other materials provided with the distribution.
   * Neither the name of HL7 nor the names of its contributors may be used to 
     endorse or promote products derived from this software without specific 
     prior written permission.
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.
  
 */


import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.TerminologyServiceException;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities;
import org.hl7.fhir.r5.context.CanonicalResourceManager.CanonicalResourceProxy;
import org.hl7.fhir.r5.context.ILoggingService.LogCategory;
import org.hl7.fhir.r5.extensions.ExtensionDefinitions;
import org.hl7.fhir.r5.extensions.ExtensionUtilities;
import org.hl7.fhir.r5.model.ActorDefinition;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.Bundle.BundleType;
import org.hl7.fhir.r5.model.Bundle.HTTPVerb;
import org.hl7.fhir.r5.model.CanonicalResource;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.Constants;
import org.hl7.fhir.r5.model.DomainResource;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionBindingComponent;
import org.hl7.fhir.r5.model.Enumerations.CodeSystemContentMode;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.r5.model.Measure;
import org.hl7.fhir.r5.model.NamingSystem;
import org.hl7.fhir.r5.model.NamingSystem.NamingSystemIdentifierType;
import org.hl7.fhir.r5.model.NamingSystem.NamingSystemType;
import org.hl7.fhir.r5.model.NamingSystem.NamingSystemUniqueIdComponent;
import org.hl7.fhir.r5.model.OperationDefinition;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r5.model.PackageInformation;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.hl7.fhir.r5.model.PrimitiveType;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.Requirements;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.SearchParameter;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.UrlType;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetComposeComponent;
import org.hl7.fhir.r5.profilemodel.PEBuilder;
import org.hl7.fhir.r5.profilemodel.PEBuilder.PEElementPropertiesPolicy;
import org.hl7.fhir.r5.renderers.OperationOutcomeRenderer;
import org.hl7.fhir.r5.terminologies.CodeSystemUtilities;
import org.hl7.fhir.r5.terminologies.ImplicitValueSets;
import org.hl7.fhir.r5.terminologies.ValueSetUtilities;
import org.hl7.fhir.r5.terminologies.client.TerminologyClientContext;
import org.hl7.fhir.r5.terminologies.client.TerminologyClientManager;
import org.hl7.fhir.r5.terminologies.client.TerminologyClientR5;
import org.hl7.fhir.r5.terminologies.expansion.ValueSetExpander;
import org.hl7.fhir.r5.terminologies.expansion.ValueSetExpansionOutcome;
import org.hl7.fhir.r5.terminologies.utilities.CodingValidationRequest;
import org.hl7.fhir.r5.terminologies.utilities.TerminologyCache;
import org.hl7.fhir.r5.terminologies.utilities.TerminologyCache.CacheToken;
import org.hl7.fhir.r5.terminologies.utilities.TerminologyCache.SourcedCodeSystem;
import org.hl7.fhir.r5.terminologies.utilities.TerminologyCache.SourcedValueSet;
import org.hl7.fhir.r5.terminologies.utilities.TerminologyOperationContext;
import org.hl7.fhir.r5.terminologies.utilities.TerminologyOperationContext.TerminologyServiceProtectionException;
import org.hl7.fhir.r5.terminologies.utilities.TerminologyServiceErrorClass;
import org.hl7.fhir.r5.terminologies.utilities.ValidationResult;
import org.hl7.fhir.r5.terminologies.validation.VSCheckerException;
import org.hl7.fhir.r5.terminologies.validation.ValueSetValidator;
import org.hl7.fhir.r5.utils.PackageHackerR5;
import org.hl7.fhir.r5.utils.ResourceUtilities;

import org.hl7.fhir.r5.utils.UserDataNames;
import org.hl7.fhir.r5.utils.client.EFhirClientException;
import org.hl7.fhir.r5.utils.validation.ValidationContextCarrier;
import org.hl7.fhir.utilities.FhirPublication;
import org.hl7.fhir.utilities.FileUtilities;
import org.hl7.fhir.utilities.MarkedToMoveToAdjunctPackage;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.ToolingClientLogger;
import org.hl7.fhir.utilities.UUIDUtilities;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.filesystem.ManagedFileAccess;
import org.hl7.fhir.utilities.i18n.I18nBase;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.i18n.subtag.LanguageSubtagRegistry;
import org.hl7.fhir.utilities.i18n.subtag.LanguageSubtagRegistryLoader;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.validation.ValidationOptions;

import com.google.gson.JsonObject;

import lombok.Getter;

@Slf4j
@MarkedToMoveToAdjunctPackage
public abstract class BaseWorkerContext extends I18nBase implements IWorkerContext {
  private static boolean allowedToIterateTerminologyResources;

  public interface IByteProvider {
    byte[] bytes() throws IOException;
  }

  public class BytesProvider implements IByteProvider {

    private byte[] bytes;

    protected BytesProvider(byte[] bytes) {
      super();
      this.bytes = bytes;
    }

    @Override
    public byte[] bytes() throws IOException {
      return bytes;
    }

  }

  public class BytesFromPackageProvider implements IByteProvider {

    private NpmPackage pi;
    private String name;

    public BytesFromPackageProvider(NpmPackage pi, String name) {
      this.pi = pi;
      this.name = name;
    }

    @Override
    public byte[] bytes() throws IOException {
      return FileUtilities.streamToBytes(pi.load("other", name));
    }

  }
  
  public class BytesFromFileProvider implements IByteProvider {

    private String name;

    public BytesFromFileProvider(String name) {
      this.name = name;
    }

    @Override
    public byte[] bytes() throws IOException {
      return FileUtilities.streamToBytes(ManagedFileAccess.inStream(name));
    }

  }
  
  class OIDSource {
    private String folder;
    private Connection db;
    private String pid;
    protected OIDSource(String folder, String pid) {
      super();
      this.folder = folder;
      this.pid = pid;
    }
    
  }

  private static final boolean QA_CHECK_REFERENCE_SOURCE = false; // see comments below

  public static class ResourceProxy {
    private Resource resource;
    private CanonicalResourceProxy proxy;

    public ResourceProxy(Resource resource) {
      super();
      this.resource = resource;
    }
    public ResourceProxy(CanonicalResourceProxy proxy) {
      super();
      this.proxy = proxy;
    }
    
    public Resource getResource() {
      return resource != null ? resource : proxy.getResource();
    }
      
    public CanonicalResourceProxy getProxy() {
      return proxy;
    }
    
    public String getUrl() {
      if (resource == null) {
        return proxy.getUrl();
      } else if (resource instanceof CanonicalResource) {
        return ((CanonicalResource) resource).getUrl(); 
      } else {
        return null;
      }
    }
    
  }

  public class MetadataResourceVersionComparator<T extends CanonicalResource> implements Comparator<T> {

    final private List<T> list;

    public MetadataResourceVersionComparator(List<T> list) {
      this.list = list;
    }

    @Override
    public int compare(T arg1, T arg2) {
      String v1 = arg1.getVersion();
      String v2 = arg2.getVersion();
      if (v1 == null && v2 == null) {
        return Integer.compare(list.indexOf(arg1), list.indexOf(arg2)); // retain original order
      } else if (v1 == null) {
        return -1;
      } else if (v2 == null) {
        return 1;
      } else {
        String mm1 = VersionUtilities.getMajMin(v1);
        String mm2 = VersionUtilities.getMajMin(v2);
        if (mm1 == null || mm2 == null) {
          return v1.compareTo(v2);
        } else {
          return mm1.compareTo(mm2);
        }
      }
    }
  }

  private Object lock = new Object(); // used as a lock for the data that follows
  protected String version; // although the internal resources are all R5, the version of FHIR they describe may not be 

  private boolean minimalMemory = false;

  private Map<String, Map<String, ResourceProxy>> allResourcesById = new HashMap<String, Map<String, ResourceProxy>>();
  private Map<String, List<ResourceProxy>> allResourcesByUrl = new HashMap<String, List<ResourceProxy>>();
  
  // all maps are to the full URI
  private CanonicalResourceManager<CodeSystem> codeSystems = new CanonicalResourceManager<CodeSystem>(false, minimalMemory);
  private final HashMap<String, SystemSupportInformation> supportedCodeSystems = new HashMap<>();
  private final Set<String> unsupportedCodeSystems = new HashSet<String>(); // know that the terminology server doesn't support them
  private CanonicalResourceManager<ValueSet> valueSets = new CanonicalResourceManager<ValueSet>(false, minimalMemory);
  private CanonicalResourceManager<ConceptMap> maps = new CanonicalResourceManager<ConceptMap>(false, minimalMemory);
  protected CanonicalResourceManager<StructureMap> transforms = new CanonicalResourceManager<StructureMap>(false, minimalMemory);
  private CanonicalResourceManager<StructureDefinition> structures = new CanonicalResourceManager<StructureDefinition>(false, minimalMemory);
  private TypeManager typeManager = new TypeManager(structures);
  private final CanonicalResourceManager<Measure> measures = new CanonicalResourceManager<Measure>(false, minimalMemory);
  private final CanonicalResourceManager<Library> libraries = new CanonicalResourceManager<Library>(false, minimalMemory);
  private CanonicalResourceManager<ImplementationGuide> guides = new CanonicalResourceManager<ImplementationGuide>(false, minimalMemory);
  private final CanonicalResourceManager<CapabilityStatement> capstmts = new CanonicalResourceManager<CapabilityStatement>(false, minimalMemory);
  private final CanonicalResourceManager<SearchParameter> searchParameters = new CanonicalResourceManager<SearchParameter>(false, minimalMemory);
  private final CanonicalResourceManager<Questionnaire> questionnaires = new CanonicalResourceManager<Questionnaire>(false, minimalMemory);
  private final CanonicalResourceManager<OperationDefinition> operations = new CanonicalResourceManager<OperationDefinition>(false, minimalMemory);
  private final CanonicalResourceManager<PlanDefinition> plans = new CanonicalResourceManager<PlanDefinition>(false, minimalMemory);
  private final CanonicalResourceManager<ActorDefinition> actors = new CanonicalResourceManager<ActorDefinition>(false, minimalMemory);
  private final CanonicalResourceManager<Requirements> requirements = new CanonicalResourceManager<Requirements>(false, minimalMemory);
  private final CanonicalResourceManager<NamingSystem> systems = new CanonicalResourceManager<NamingSystem>(false, minimalMemory);
  private Map<String, NamingSystem> systemUrlMap;

  private LanguageSubtagRegistry registry;
  
  private UcumService ucumService;
  protected Map<String, IByteProvider> binaries = new HashMap<String, IByteProvider>();
  protected Map<String, Set<OIDDefinition>> oidCacheManual = new HashMap<>();
  protected List<OIDSource> oidSources = new ArrayList<>();

  protected Map<String, Map<String, ValidationResult>> validationCache = new HashMap<String, Map<String,ValidationResult>>();
  protected String name;
  private boolean allowLoadingDuplicates;

  private final Set<String> codeSystemsUsed = new HashSet<>();
  protected ToolingClientLogger txLog;
  protected boolean canRunWithoutTerminology;
  protected boolean noTerminologyServer;
  private int expandCodesLimit = 1000;
  protected org.hl7.fhir.r5.context.ILoggingService logger = new Slf4JLoggingService(log);
  protected final TerminologyClientManager terminologyClientManager = new TerminologyClientManager(new TerminologyClientR5.TerminologyClientR5Factory(), UUID.randomUUID().toString(), logger);
  protected Parameters expParameters;
  private Map<String, PackageInformation> packages = new HashMap<>();

  @Getter
  protected TerminologyCache txCache = new TerminologyCache(this, null);
  protected TimeTracker clock;
  private boolean tlogging = true;
  private IWorkerContextManager.ICanonicalResourceLocator locator;
  protected String userAgent;
  protected ContextUtilities cutils;
  private List<String> suppressedMappings;

  protected BaseWorkerContext() throws FileNotFoundException, IOException, FHIRException {
    setValidationMessageLanguage(getLocale());
    clock = new TimeTracker();
    initLang();
    cutils = new ContextUtilities(this, suppressedMappings);
  }

  protected BaseWorkerContext(Locale locale) throws FileNotFoundException, IOException, FHIRException {
    this.setLocale(locale);
    clock = new TimeTracker();
    initLang();
    cutils = new ContextUtilities(this, suppressedMappings);
  }

  protected BaseWorkerContext(CanonicalResourceManager<CodeSystem> codeSystems, CanonicalResourceManager<ValueSet> valueSets, CanonicalResourceManager<ConceptMap> maps, CanonicalResourceManager<StructureDefinition> profiles,
      CanonicalResourceManager<ImplementationGuide> guides) throws FileNotFoundException, IOException, FHIRException {
    this();
    this.codeSystems = codeSystems;
    this.valueSets = valueSets;
    this.maps = maps;
    this.structures = profiles;
    this.typeManager = new TypeManager(structures);
    this.guides = guides;
    clock = new TimeTracker();
    initLang();
    cutils = new ContextUtilities(this, suppressedMappings);
  }

  private void initLang() throws IOException {
    registry = new LanguageSubtagRegistry();
    LanguageSubtagRegistryLoader loader = new LanguageSubtagRegistryLoader(registry);
    loader.loadFromDefaultResource();
  }

  protected void copy(BaseWorkerContext other) {
    synchronized (other.lock) { // tricky, because you need to lock this as well, but it's really not in use yet 
      allResourcesById.putAll(other.allResourcesById);
      codeSystems.copy(other.codeSystems);
      valueSets.copy(other.valueSets);
      maps.copy(other.maps);
      transforms.copy(other.transforms);
      structures.copy(other.structures);
      typeManager = new TypeManager(structures);
      // Snapshot generation is not thread safe, so before this copy of can be used by another thread, we create all the
      // necessary snapshots. This prevent asynchronous snapshot generation for the shared structure definitions.
      for (String typeName : typeManager.getTypeNames()) {
        if (typeName != null) {
          StructureDefinition structureDefinition = typeManager.fetchTypeDefinition(typeName);
          generateSnapshot(structureDefinition, "6");
        }
      }
      searchParameters.copy(other.searchParameters);
      plans.copy(other.plans);
      questionnaires.copy(other.questionnaires);
      operations.copy(other.operations);
      systems.copy(other.systems);
      systemUrlMap = null;
      guides.copy(other.guides);
      capstmts.copy(other.capstmts);
      measures.copy(other.measures);
      libraries.copy(other.libraries);

      allowLoadingDuplicates = other.allowLoadingDuplicates;
      name = other.name;
      txLog = other.txLog;
      canRunWithoutTerminology = other.canRunWithoutTerminology;
      noTerminologyServer = other.noTerminologyServer;
      if (other.txCache != null)
        txCache = other.txCache; // no copy. for now?
      expandCodesLimit = other.expandCodesLimit;
      logger = other.logger;
      expParameters = other.expParameters != null ? other.expParameters.copy() : null;
      version = other.version;
      supportedCodeSystems.putAll(other.supportedCodeSystems);
      unsupportedCodeSystems.addAll(other.unsupportedCodeSystems);
      codeSystemsUsed.addAll(other.codeSystemsUsed);
      ucumService = other.ucumService;
      binaries.putAll(other.binaries);
      oidSources.addAll(other.oidSources);
      oidCacheManual.putAll(other.oidCacheManual);
      validationCache.putAll(other.validationCache);
      tlogging = other.tlogging;
      locator = other.locator;
      userAgent = other.userAgent;
      terminologyClientManager.copy(other.terminologyClientManager);
      cachingAllowed = other.cachingAllowed;
      suppressedMappings = other.suppressedMappings;
      cutils.setSuppressedMappings(other.suppressedMappings);
    }
  }
  
  
  public void cacheResource(Resource r) throws FHIRException {
    cacheResourceFromPackage(r, null);  
  }
  
  public void registerResourceFromPackage(CanonicalResourceProxy r, PackageInformation packageInfo) throws FHIRException {
    PackageHackerR5.fixLoadedResource(r, packageInfo);

    synchronized (lock) {
      if (packageInfo != null) {
        packages.put(packageInfo.getVID(), packageInfo);
      }
      if (r.getId() != null) {
        Map<String, ResourceProxy> map = allResourcesById.get(r.getType());
        if (map == null) {
          map = new HashMap<String, ResourceProxy>();
          allResourcesById.put(r.getType(), map);
        }
        if ((packageInfo == null || !packageInfo.isExamplesPackage()) || !map.containsKey(r.getId())) {
          map.put(r.getId(), new ResourceProxy(r));
        }
      }
      if (r.getUrl() != null) {
        List<ResourceProxy> list = allResourcesByUrl.get(r.getUrl());
        if (list == null) {
          list = new ArrayList<>();
          allResourcesByUrl.put(r.getUrl(), list);
        }
        list.add(new ResourceProxy(r));
      }

      String url = r.getUrl();
      if (!allowLoadingDuplicates && hasResourceVersion(r.getType(), url, r.getVersion()) && !packageInfo.isTHO()) {
        // special workaround for known problems with existing packages
        if (Utilities.existsInList(url, "http://hl7.org/fhir/SearchParameter/example")) {
          return;
        }
        CanonicalResource ex = fetchResourceWithException(r.getType(), url);
        throw new DefinitionException(formatMessage(I18nConstants.DUPLICATE_RESOURCE_, url, r.getVersion(), ex.getVersion(),
            ex.fhirType()));
      }
      switch(r.getType()) {
      case "StructureDefinition":
        if ("1.4.0".equals(version)) {
          StructureDefinition sd = (StructureDefinition) r.getResource();
          fixOldSD(sd);
        }
        structures.register(r, packageInfo);
        typeManager.see(r);
        break;
      case "ValueSet":
        valueSets.register(r, packageInfo);
        break;
      case "CodeSystem":        
        codeSystems.register(r, packageInfo);
        break;
      case "ImplementationGuide":
        guides.register(r, packageInfo);
        break;
      case "CapabilityStatement":
        capstmts.register(r, packageInfo);
        break;
      case "Measure":
        measures.register(r, packageInfo);
        break;
      case "Library":
        libraries.register(r, packageInfo);
        break;
      case "SearchParameter":
        searchParameters.register(r, packageInfo);
        break;
      case "PlanDefinition":
        plans.register(r, packageInfo);
        break;
      case "OperationDefinition":
        operations.register(r, packageInfo);
        break;
      case "Questionnaire":
        questionnaires.register(r, packageInfo);
        break;
      case "ConceptMap":
        maps.register(r, packageInfo);
        break;
      case "StructureMap":
        transforms.register(r, packageInfo);
        break;
      case "NamingSystem":
        systems.register(r, packageInfo);
        break;
      case "Requirements":
        requirements.register(r, packageInfo);
        break;
      case "ActorDefinition":
        actors.register(r, packageInfo);
        break;
      }
    }
  }

  public void cacheResourceFromPackage(Resource r, PackageInformation packageInfo) throws FHIRException {
 
    synchronized (lock) {   
      if (packageInfo != null) {
        packages.put(packageInfo.getVID(), packageInfo);
      }

      if (r.getId() != null) {
        Map<String, ResourceProxy> map = allResourcesById.get(r.fhirType());
        if (map == null) {
          map = new HashMap<String, ResourceProxy>();
          allResourcesById.put(r.fhirType(), map);
        }
        if ((packageInfo == null || !packageInfo.isExamplesPackage()) || !map.containsKey(r.getId())) {
          map.put(r.getId(), new ResourceProxy(r));
        } else {
          logger.logDebugMessage(LogCategory.PROGRESS,"Ignore "+r.fhirType()+"/"+r.getId()+" from package "+packageInfo.toString());
        }
      }
      if (r instanceof CanonicalResource) {
        CanonicalResource cr = (CanonicalResource) r;
        if (cr.getUrl() != null) {
          List<ResourceProxy> list = allResourcesByUrl.get(cr.getUrl());
          if (list == null) {
            list = new ArrayList<>();
            allResourcesByUrl.put(cr.getUrl(), list);
          }
          list.add(new ResourceProxy(r));
        }
      }

      if (r instanceof CodeSystem || r instanceof NamingSystem) {
        String url = null;
        Set<String> oids = new HashSet<String>();
        if (r instanceof CodeSystem) {
          CodeSystem cs = (CodeSystem) r;
          url = cs.getUrl();
          for (Identifier id : cs.getIdentifier()) {
            if (id.hasValue() && id.getValue().startsWith("urn:oid:")) {
              oids.add(id.getValue().substring(8));           
            }
          }
        }
        if (r instanceof NamingSystem) {
          NamingSystem ns = ((NamingSystem) r);
          if (ns.getKind() == NamingSystemType.CODESYSTEM) {
            for (NamingSystemUniqueIdComponent id : ns.getUniqueId()) {
              if (id.getType() == NamingSystemIdentifierType.URI) {
                url = id.getValue();
              }
              if (id.getType() == NamingSystemIdentifierType.OID) {
                oids.add(id.getValue());
              }
            }
          }
        }
        if (url != null) {
          for (String s : oids) {
            if (!oidCacheManual.containsKey(s)) {
              oidCacheManual.put(s, new HashSet<>());
            }
            oidCacheManual.get(s).add(new OIDDefinition(r.fhirType(), s, url, ((CanonicalResource) r).getVersion(), null, null));
          }
        }
      }

      if (r instanceof CanonicalResource) {
        CanonicalResource m = (CanonicalResource) r;
        String url = m.getUrl();
        if (!allowLoadingDuplicates && hasResource(r.getClass(), url)) {
          // special workaround for known problems with existing packages
          if (Utilities.existsInList(url, "http://hl7.org/fhir/SearchParameter/example")) {
            return;
          }
			// matchbox patch for duplicate resources, see https://github.com/ahdis/matchbox/issues/227
			// org.hl7.fhir.r5.conformance.R5ExtensionsLoader.loadR5SpecialTypes(R5ExtensionsLoader.java:141)
			CanonicalResource ex = fetchResourceWithException(m.fhirType(), url);
			if (ex.getVersion() != null && m.getVersion() != null && laterVersion(m.getVersion(), ex.getVersion())) {
				logger.logDebugMessage(LogCategory.INIT,
									   "Note replacing old version: "
										   + formatMessage(I18nConstants.DUPLICATE_RESOURCE_, url,
														   m.getVersion(), ex.getVersion(), ex.fhirType()));
				dropResource(ex);
			} else {
				logger.logDebugMessage(LogCategory.INIT,
									   "Note keeping newer version: "
										   + formatMessage(I18nConstants.DUPLICATE_RESOURCE_,
														   url,
														   m.getVersion(),
														   ex.getVersion(),
														   ex.fhirType()));
				return;
			}
        }
        if (r instanceof StructureDefinition) {
          StructureDefinition sd = (StructureDefinition) m;
          if ("1.4.0".equals(version)) {
            fixOldSD(sd);
          }
          structures.see(sd, packageInfo);
          typeManager.see(sd);
        } else if (r instanceof ValueSet) {
          valueSets.see((ValueSet) m, packageInfo);
        } else if (r instanceof CodeSystem) {
          CodeSystemUtilities.crossLinkCodeSystem((CodeSystem) r);
          codeSystems.see((CodeSystem) m, packageInfo);
        } else if (r instanceof ImplementationGuide) {
          guides.see((ImplementationGuide) m, packageInfo);
        } else if (r instanceof CapabilityStatement) {
          capstmts.see((CapabilityStatement) m, packageInfo);
        } else if (r instanceof Measure) {
          measures.see((Measure) m, packageInfo);
        } else if (r instanceof Library) {
          libraries.see((Library) m, packageInfo);        
        } else if (r instanceof SearchParameter) {
          searchParameters.see((SearchParameter) m, packageInfo);
        } else if (r instanceof PlanDefinition) {
          plans.see((PlanDefinition) m, packageInfo);
        } else if (r instanceof OperationDefinition) {
          operations.see((OperationDefinition) m, packageInfo);
        } else if (r instanceof Questionnaire) {
          questionnaires.see((Questionnaire) m, packageInfo);
        } else if (r instanceof ConceptMap) {
          maps.see((ConceptMap) m, packageInfo);
        } else if (r instanceof StructureMap) {
          transforms.see((StructureMap) m, packageInfo);
        } else if (r instanceof NamingSystem) {
          systems.see((NamingSystem) m, packageInfo);
          systemUrlMap = null;
        } else if (r instanceof Requirements) {
          requirements.see((Requirements) m, packageInfo);
        } else if (r instanceof ActorDefinition) {
          actors.see((ActorDefinition) m, packageInfo);
        }
      }
    }
  }

  public Map<String, NamingSystem> getNSUrlMap() {
    if (systemUrlMap == null) {
      systemUrlMap = new HashMap<>();
      try {
      List<NamingSystem> nsl = systems.getList();
      for (NamingSystem ns : nsl) {
        for (NamingSystemUniqueIdComponent uid : ns.getUniqueId()) {
          if (uid.getType() == NamingSystemIdentifierType.URI && uid.hasValue()) {
            systemUrlMap.put(uid.getValue(), ns) ;
          }
        }        
      }
      } catch (Exception e) {
        if (!nsFailHasFailed) {
          e.printStackTrace();
          nsFailHasFailed  = true;
        }
      }
    }
    return systemUrlMap;
  }

  
  public void fixOldSD(StructureDefinition sd) {
    if (sd.getDerivation() == TypeDerivationRule.CONSTRAINT && sd.getType().equals("Extension") && sd.getUrl().startsWith("http://hl7.org/fhir/StructureDefinition/")) {
      sd.setSnapshot(null);
    }
    for (ElementDefinition ed : sd.getDifferential().getElement()) {
      if (ed.getPath().equals("Extension.url") || ed.getPath().endsWith(".extension.url") ) {
        ed.setMin(1);
        if (ed.hasBase()) {
          ed.getBase().setMin(1);
        }
      }
      if ("extension".equals(ed.getSliceName())) {
        ed.setSliceName(null);
      }
    }
  }

  /*
   *  Compare business versions, returning "true" if the candidate newer version is in fact newer than the oldVersion
   *  Comparison will work for strictly numeric versions as well as multi-level versions separated by ., -, _, : or space
   *  Failing that, it will do unicode-based character ordering.
   *  E.g. 1.5.3 < 1.14.3
   *       2017-3-10 < 2017-12-7
   *       A3 < T2
   */
  private boolean laterVersion(String newVersion, String oldVersion) {
    // Compare business versions, retur
    newVersion = newVersion.trim();
    oldVersion = oldVersion.trim();
    if (StringUtils.isNumeric(newVersion) && StringUtils.isNumeric(oldVersion)) {
      return Double.parseDouble(newVersion) > Double.parseDouble(oldVersion);
    } else if (hasDelimiter(newVersion, oldVersion, ".")) {
      return laterDelimitedVersion(newVersion, oldVersion, "\\.");
    } else if (hasDelimiter(newVersion, oldVersion, "-")) {
      return laterDelimitedVersion(newVersion, oldVersion, "\\-");
    } else if (hasDelimiter(newVersion, oldVersion, "_")) {
      return laterDelimitedVersion(newVersion, oldVersion, "\\_");
    } else if (hasDelimiter(newVersion, oldVersion, ":")) {
      return laterDelimitedVersion(newVersion, oldVersion, "\\:");
    } else if (hasDelimiter(newVersion, oldVersion, " ")) {
      return laterDelimitedVersion(newVersion, oldVersion, "\\ ");
    } else {
      return newVersion.compareTo(oldVersion) > 0;
    }
  }
  
  /*
   * Returns true if both strings include the delimiter and have the same number of occurrences of it
   */
  private boolean hasDelimiter(String s1, String s2, String delimiter) {
    return s1.contains(delimiter) && s2.contains(delimiter) && s1.split(delimiter).length == s2.split(delimiter).length;
  }

  private boolean laterDelimitedVersion(String newVersion, String oldVersion, String delimiter) {
    String[] newParts = newVersion.split(delimiter);
    String[] oldParts = oldVersion.split(delimiter);
    for (int i = 0; i < newParts.length; i++) {
      if (!newParts[i].equals(oldParts[i])) {
        return laterVersion(newParts[i], oldParts[i]);
      }
    }
    // MATCHBOX PATCH for allowing loading HL7 Terminology (THO)
    return true;
  }
  
  protected <T extends CanonicalResource> void seeMetadataResource(T r, Map<String, T> map, List<T> list, boolean addId) throws FHIRException {
//    if (addId)
    //      map.put(r.getId(), r); // todo: why?
    list.add(r);
    if (r.hasUrl()) {
      // first, this is the correct reosurce for this version (if it has a version)
      if (r.hasVersion()) {
        map.put(r.getUrl()+"|"+r.getVersion(), r);
      }
      // if we haven't get anything for this url, it's the correct version
      if (!map.containsKey(r.getUrl())) {
        map.put(r.getUrl(), r);
      } else {
        List<T> rl = new ArrayList<T>();
        for (T t : list) {
          if (t.getUrl().equals(r.getUrl()) && !rl.contains(t)) {
            rl.add(t);
          }
        }
        Collections.sort(rl, new MetadataResourceVersionComparator<T>(list));
        map.put(r.getUrl(), rl.get(rl.size()-1));
        T latest = null;
        for (T t : rl) {
          if (VersionUtilities.versionsCompatible(t.getVersion(), r.getVersion())) {
            latest = t;
          }
        }
        if (latest != null) { // might be null if it's not using semver
          map.put(r.getUrl()+"|"+VersionUtilities.getMajMin(latest.getVersion()), rl.get(rl.size()-1));
        }
      }
    }
  }  

  @Override
  public CodeSystem fetchCodeSystem(String system, FhirPublication fhirVersion) {
    return fetchCodeSystem(system);
  }
  
  @Override
  public CodeSystem fetchCodeSystem(String system) {
    if (system == null) {
      return null;
    }
    if (system.contains("|")) {
      String s = system.substring(0, system.indexOf("|"));
      String v = system.substring(system.indexOf("|")+1);
      return fetchCodeSystem(s, v);
    }
    CodeSystem cs;
    synchronized (lock) {

      cs = codeSystems.get(system);
    }
    if (cs == null && locator != null) {
      locator.findResource(this, system);
      synchronized (lock) {
        cs = codeSystems.get(system);
      }
    }
    return cs;
  } 


  public CodeSystem fetchCodeSystem(String system, String version, FhirPublication fhirVersion) {
    return fetchCodeSystem(system, version);
  }
  
  public CodeSystem fetchCodeSystem(String system, String version) {
    if (version == null) {
      return fetchCodeSystem(system);
    }
    CodeSystem cs;
    synchronized (lock) {
      cs = codeSystems.get(system, version);
    }
    if (cs == null && locator != null) {
      locator.findResource(this, system);
      synchronized (lock) {
        cs = codeSystems.get(system);
      }
    }
    return cs;
  } 
  

  public CodeSystem fetchSupplementedCodeSystem(String system, FhirPublication fhirVersion) {
    return fetchSupplementedCodeSystem(system);  
  }
  
  public CodeSystem fetchSupplementedCodeSystem(String system, String version, FhirPublication fhirVersion) {
    return fetchSupplementedCodeSystem(system, version);
  }
  
  @Override
  public CodeSystem fetchSupplementedCodeSystem(String system) {
    CodeSystem cs = fetchCodeSystem(system);
    if (cs != null) {
      List<CodeSystem> supplements = codeSystems.getSupplements(cs);
      if (supplements.size() > 0) {
        cs = CodeSystemUtilities.mergeSupplements(cs, supplements);
      }
    }
    return cs;
  }

  @Override
  public CodeSystem fetchSupplementedCodeSystem(String system, String version) {
    CodeSystem cs = fetchCodeSystem(system, version);
    if (cs != null) {
      List<CodeSystem> supplements = codeSystems.getSupplements(cs);
      if (supplements.size() > 0) {
        cs = CodeSystemUtilities.mergeSupplements(cs, supplements);
      }
    }
    return cs;
  }

  @Override
  public SystemSupportInformation getTxSupportInfo(String system) throws TerminologyServiceException {
    return getTxSupportInfo(system, null);
  }
  @Override
  public SystemSupportInformation getTxSupportInfo(String system, String version) throws TerminologyServiceException {
    synchronized (lock) {
      String vurl = CanonicalType.urlWithVersion(system, version);
      if (codeSystems.has(vurl) && codeSystems.get(vurl).getContent() != CodeSystemContentMode.NOTPRESENT) {
        return new SystemSupportInformation(true, "internal", TerminologyClientContext.LATEST_VERSION);
      } else if (supportedCodeSystems.containsKey(vurl)) {
        return supportedCodeSystems.get(vurl);
      } else if (system.startsWith("http://example.org") || system.startsWith("http://acme.com") || system.startsWith("http://hl7.org/fhir/valueset-") || system.startsWith("urn:oid:")) {
        return new SystemSupportInformation(false);
      } else {
        if (noTerminologyServer) {
          return new SystemSupportInformation(false);
        }
        if (terminologyClientManager != null) {
          try {
            TerminologyClientContext client = terminologyClientManager.chooseServer(null, Set.of(vurl), false);
            supportedCodeSystems.put(vurl, new SystemSupportInformation(client.supportsSystem(vurl), client.getAddress(), client.getTxTestVersion()));
          } catch (Exception e) {
            if (canRunWithoutTerminology) {
              noTerminologyServer = true;
              logger.logMessage("==============!! Running without terminology server !! ==============");
              if (terminologyClientManager.getMasterClient() != null) {
                logger.logMessage("txServer = "+ terminologyClientManager.getMasterClient().getId());
                logger.logMessage("Error = "+e.getMessage()+"");
              }
              logger.logMessage("=====================================================================");
              return new SystemSupportInformation(false);
            } else {
              e.printStackTrace();
              throw new TerminologyServiceException(e);
            }
          }
          if (supportedCodeSystems.containsKey(vurl)) {
            return supportedCodeSystems.get(vurl);
          }
        }
      }
      return new SystemSupportInformation(false);
    }
  }

  @Override
  public boolean supportsSystem(String system) throws TerminologyServiceException {
    SystemSupportInformation si = getTxSupportInfo(system);
    return si.isSupported();
  }

  @Override
  public boolean supportsSystem(String system, FhirPublication fhirVersion) throws TerminologyServiceException {
    SystemSupportInformation si = getTxSupportInfo(system);
    return si.isSupported();
  }

  public boolean isServerSideSystem(String url) {
    boolean check = supportsSystem(url);
    return check && supportedCodeSystems.containsKey(url);
  }


  protected void txLog(String msg) {
    if (tlogging ) {
        logger.logDebugMessage(LogCategory.TX, msg);
    }
  }

  // --- expansion support ------------------------------------------------------------------------------------------------------------

  public int getExpandCodesLimit() {
    return expandCodesLimit;
  }

  public void setExpandCodesLimit(int expandCodesLimit) {
    this.expandCodesLimit = expandCodesLimit;
  }

  @Override
  public ValueSetExpansionOutcome expandVS(Resource src, ElementDefinitionBindingComponent binding, boolean cacheOk, boolean heirarchical) throws FHIRException {
    ValueSet vs = null;
    vs = fetchResource(ValueSet.class, binding.getValueSet(), src);
    if (vs == null) {
      throw new FHIRException(formatMessage(I18nConstants.UNABLE_TO_RESOLVE_VALUE_SET_, binding.getValueSet()));
    }
    return expandVS(vs, cacheOk, heirarchical);
  }


  @Override
  public ValueSetExpansionOutcome expandVS(ITerminologyOperationDetails opCtxt, ConceptSetComponent inc, boolean hierarchical, boolean noInactive) throws TerminologyServiceException {
    ValueSet vs = new ValueSet();
    vs.setStatus(PublicationStatus.ACTIVE);
    vs.setCompose(new ValueSetComposeComponent());
    vs.getCompose().setInactive(!noInactive);
    vs.getCompose().getInclude().add(inc);
    CacheToken cacheToken = txCache.generateExpandToken(vs, hierarchical);
    ValueSetExpansionOutcome res;
    res = txCache.getExpansion(cacheToken);
    if (res != null) {
      return res;
    }
    Set<String> systems = findRelevantSystems(vs);
    TerminologyClientContext tc = terminologyClientManager.chooseServer(vs, systems, true);
    if (tc == null) {
      return new ValueSetExpansionOutcome("No server available", TerminologyServiceErrorClass.INTERNAL_ERROR, true);      
    }
    Parameters p = constructParameters(opCtxt, tc, vs, hierarchical);
    for (ConceptSetComponent incl : vs.getCompose().getInclude()) {
      codeSystemsUsed.add(incl.getSystem());
    }
    for (ConceptSetComponent incl : vs.getCompose().getExclude()) {
      codeSystemsUsed.add(incl.getSystem());
    }
    
    if (noTerminologyServer) {
      return new ValueSetExpansionOutcome(formatMessage(I18nConstants.ERROR_EXPANDING_VALUESET_RUNNING_WITHOUT_TERMINOLOGY_SERVICES), TerminologyServiceErrorClass.NOSERVICE, false);
    }
    p.addParameter("count", expandCodesLimit);
    p.addParameter("offset", 0);
    txLog("$expand on "+txCache.summary(vs)+" on "+tc.getAddress());
    if (addDependentResources(opCtxt, tc, p, vs)) {
      p.addParameter().setName("cache-id").setValue(new IdType(terminologyClientManager.getCacheId()));
    }

    try {
      ValueSet result = tc.getClient().expandValueset(vs, p);
      res = new ValueSetExpansionOutcome(result).setTxLink(txLog == null ? null : txLog.getLastId());
      if (res != null && res.getValueset() != null) { 
        res.getValueset().setUserData(UserDataNames.VS_EXPANSION_SOURCE, tc.getHost());
      }
    } catch (Exception e) {
      res = new ValueSetExpansionOutcome(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), TerminologyServiceErrorClass.UNKNOWN, true);
      if (txLog != null) {
        res.setTxLink(txLog == null ? null : txLog.getLastId());
      }
    }
    txCache.cacheExpansion(cacheToken, res, TerminologyCache.PERMANENT);
    return res;
  }

  @Override
  public ValueSetExpansionOutcome expandVS(ValueSet vs, boolean cacheOk, boolean heirarchical) {
    if (expParameters == null)
      throw new Error(formatMessage(I18nConstants.NO_EXPANSION_PARAMETERS_PROVIDED));
    Parameters p = expParameters.copy(); 
    return expandVS(vs, cacheOk, heirarchical, false, p);
  }

  @Override
  public ValueSetExpansionOutcome expandVS(ValueSet vs, boolean cacheOk, boolean heirarchical, int count) {
    if (expParameters == null)
      throw new Error(formatMessage(I18nConstants.NO_EXPANSION_PARAMETERS_PROVIDED));
    Parameters p = expParameters.copy(); 
    p.addParameter("count", count);
    return expandVS(vs, cacheOk, heirarchical, false, p);
  }

  @Override
  public ValueSetExpansionOutcome expandVS(String url, boolean cacheOk, boolean hierarchical, int count) {
    if (expParameters == null)
      throw new Error(formatMessage(I18nConstants.NO_EXPANSION_PARAMETERS_PROVIDED));
    if (noTerminologyServer) {
      return new ValueSetExpansionOutcome(formatMessage(I18nConstants.ERROR_EXPANDING_VALUESET_RUNNING_WITHOUT_TERMINOLOGY_SERVICES), TerminologyServiceErrorClass.NOSERVICE, null, false);
    }

    Parameters p = expParameters.copy(); 
    p.addParameter("count", count);
    p.addParameter("url", new UriType(url));
    p.setParameter("_limit",new IntegerType("10000"));
    p.setParameter("_incomplete", new BooleanType("true"));

    CacheToken cacheToken = txCache.generateExpandToken(url, hierarchical);
    ValueSetExpansionOutcome res;
    if (cacheOk) {
      res = txCache.getExpansion(cacheToken);
      if (res != null) {
        return res;
      }
    }
    p.setParameter("excludeNested", !hierarchical);
    List<String> allErrors = new ArrayList<>();

    p.addParameter().setName("cache-id").setValue(new IdType(terminologyClientManager.getCacheId()));
    TerminologyClientContext tc = terminologyClientManager.chooseServer(url, true);
    try {
      if (tc == null) {
        throw new FHIRException("Unable to find a server to expand '"+url+"'");
      }
      txLog("$expand "+url+" on "+tc.getAddress());
    
      ValueSet result = tc.getClient().expandValueset(null, p);
      if (result != null) {
        if (!result.hasUrl()) {
          result.setUrl(url);
        }
        if (!result.hasUrl()) {
          throw new Error(formatMessage(I18nConstants.NO_URL_IN_EXPAND_VALUE_SET_2));
        }
      }
      res = new ValueSetExpansionOutcome(result).setTxLink(txLog == null ? null : txLog.getLastId()); 
      if (res != null && res.getValueset() != null) { 
        res.getValueset().setUserData(UserDataNames.VS_EXPANSION_SOURCE, tc.getHost());
      } 
    } catch (Exception e) {
      res = new ValueSetExpansionOutcome((e.getMessage() == null ? e.getClass().getName() : e.getMessage()), TerminologyServiceErrorClass.UNKNOWN, allErrors, true).setTxLink(txLog == null ? null : txLog.getLastId());
    }
    txCache.cacheExpansion(cacheToken, res, TerminologyCache.PERMANENT);
    return res;
  }

  @Override
  public ValueSetExpansionOutcome expandVS(ValueSet vs, boolean cacheOk, boolean heirarchical, boolean incompleteOk) {
    if (expParameters == null)
      throw new Error(formatMessage(I18nConstants.NO_EXPANSION_PARAMETERS_PROVIDED));
    Parameters p = expParameters.copy(); 
    return expandVS(vs, cacheOk, heirarchical, incompleteOk, p);
  }

  public ValueSetExpansionOutcome expandVS(ValueSet vs, boolean cacheOk, boolean hierarchical, boolean incompleteOk, Parameters pIn)  {
    return expandVS(vs, cacheOk, hierarchical, incompleteOk, pIn, false);
  }
  
  public ValueSetExpansionOutcome expandVS(ValueSet vs, boolean cacheOk, boolean hierarchical, boolean incompleteOk, Parameters pIn, boolean noLimits)  {
    if (pIn == null) {
      throw new Error(formatMessage(I18nConstants.NO_PARAMETERS_PROVIDED_TO_EXPANDVS));
    }
    if (vs.hasUrl() && (vs.getUrl().equals("http://hl7.org/fhir/ValueSet/all-time-units") || vs.getUrl().equals("http://hl7.org/fhir/ValueSet/all-distance-units"))) {
      return new ValueSetExpansionOutcome("This value set is not expanded correctly at this time (will be fixed in a future version)", TerminologyServiceErrorClass.VALUESET_UNSUPPORTED, false);
    }
    
    Parameters p = pIn.copy();
    p.setParameter("_limit",new IntegerType("10000"));
    p.setParameter("_incomplete", new BooleanType("true"));
    if (vs.hasExpansion()) {
      return new ValueSetExpansionOutcome(vs.copy());
    }
    if (!vs.hasUrl()) {
      throw new Error(formatMessage(I18nConstants.NO_VALUE_SET_IN_URL));
    }
    for (ConceptSetComponent inc : vs.getCompose().getInclude()) {
      if (inc.hasSystem()) {
        codeSystemsUsed.add(inc.getSystem());
      }
    }
    for (ConceptSetComponent inc : vs.getCompose().getExclude()) {
      if (inc.hasSystem()) {
        codeSystemsUsed.add(inc.getSystem());
      }
    }

    CacheToken cacheToken = txCache.generateExpandToken(vs, hierarchical);
    ValueSetExpansionOutcome res;
    if (cacheOk) {
      res = txCache.getExpansion(cacheToken);
      if (res != null) {
        return res;
      }
    }

    if (!noLimits && !p.hasParameter("count")) {
      p.addParameter("count", expandCodesLimit);
      p.addParameter("offset", 0);
    }
    p.setParameter("excludeNested", !hierarchical);
    if (incompleteOk) {
      p.setParameter("incomplete-ok", true);      
    }

    List<String> allErrors = new ArrayList<>();
    
    // ok, first we try to expand locally
    ValueSetExpander vse = constructValueSetExpanderSimple(new ValidationOptions(vs.getFHIRPublicationVersion()));
    res = null;
    try {
      res = vse.expand(vs, p);
      if (res != null && res.getValueset() != null) { 
        res.getValueset().setUserData(UserDataNames.VS_EXPANSION_SOURCE, vse.getSource());
      }
    } catch (Exception e) {
      allErrors.addAll(vse.getAllErrors());
      e.printStackTrace();
      res = new ValueSetExpansionOutcome(e.getMessage(), TerminologyServiceErrorClass.UNKNOWN, e instanceof EFhirClientException);
    }
    allErrors.addAll(vse.getAllErrors());
    if (res.getValueset() != null) {
      if (!res.getValueset().hasUrl()) {
        throw new Error(formatMessage(I18nConstants.NO_URL_IN_EXPAND_VALUE_SET));
      }
      txCache.cacheExpansion(cacheToken, res, TerminologyCache.TRANSIENT);
      return res;
    }
    if (res.getErrorClass() == TerminologyServiceErrorClass.INTERNAL_ERROR || isNoTerminologyServer() || res.getErrorClass() == TerminologyServiceErrorClass.VALUESET_UNKNOWN) { // this class is created specifically to say: don't consult the server
      return res;
    }

    // if that failed, we try to expand on the server
    if (noTerminologyServer) {
      return new ValueSetExpansionOutcome(formatMessage(I18nConstants.ERROR_EXPANDING_VALUESET_RUNNING_WITHOUT_TERMINOLOGY_SERVICES), TerminologyServiceErrorClass.NOSERVICE, allErrors, false);
    }

    p.addParameter().setName("cache-id").setValue(new IdType(terminologyClientManager.getCacheId()));
    Set<String> systems = findRelevantSystems(vs);
    TerminologyClientContext tc = terminologyClientManager.chooseServer(vs, systems, true);
    addDependentResources(null, tc, p, vs);

    
    txLog("$expand on "+txCache.summary(vs)+" on "+tc.getAddress());
    
    try {
      ValueSet result = tc.getClient().expandValueset(vs, p);
      if (result != null) {
        if (!result.hasUrl()) {
          result.setUrl(vs.getUrl());
        }
        if (!result.hasUrl()) {
          throw new Error(formatMessage(I18nConstants.NO_URL_IN_EXPAND_VALUE_SET_2));
        }
      }
      res = new ValueSetExpansionOutcome(result).setTxLink(txLog == null ? null : txLog.getLastId());  
    } catch (Exception e) {
      if (res != null && !res.isFromServer()) {
        res = new ValueSetExpansionOutcome(res.getError()+" (and "+e.getMessage()+")", res.getErrorClass(), false);
      } else {
        res = new ValueSetExpansionOutcome((e.getMessage() == null ? e.getClass().getName() : e.getMessage()), TerminologyServiceErrorClass.UNKNOWN, allErrors, true).setTxLink(txLog == null ? null : txLog.getLastId());
      }
    }
    if (res != null && res.getValueset() != null) {
      res.getValueset().setUserData(UserDataNames.VS_EXPANSION_SOURCE, tc.getHost());
    }
    txCache.cacheExpansion(cacheToken, res, TerminologyCache.PERMANENT);
    return res;
  }

//  private boolean hasTooCostlyExpansion(ValueSet valueset) {
//    return valueset != null && valueset.hasExpansion() && ExtensionUtilities.hasExtension(valueset.getExpansion(), ExtensionDefinitions.EXT_EXP_TOOCOSTLY);
//  }
  
  // --- validate code -------------------------------------------------------------------------------
  
  @Override
  public ValidationResult validateCode(ValidationOptions options, String system, String version, String code, String display) {
    assert options != null;
    Coding c = new Coding(system, version, code, display);
    return validateCode(options, c, null);
  }

  @Override
  public ValidationResult validateCode(ValidationOptions options, String system, String version, String code, String display, ValueSet vs) {
    assert options != null;
    Coding c = new Coding(system, version, code, display);
    ValidationResult ret = validateCode(options, "$", c, vs);
    ret.trimPath("$");
    return ret;
  }

  @Override
  public ValidationResult validateCode(ValidationOptions options, String code, ValueSet vs) {
    assert options != null;
    Coding c = new Coding(null, code, null);
    return validateCode(options.withGuessSystem(), c, vs);
  }


  @Override
  public void validateCodeBatch(ValidationOptions options, List<? extends CodingValidationRequest> codes, ValueSet vs, boolean passVS) {
    if (options == null) {
      options = ValidationOptions.defaults();
    }
    // 1st pass: what is in the cache? 
    // 2nd pass: What can we do internally 
    // 3rd pass: hit the server
    for (CodingValidationRequest t : codes) {
      t.setCacheToken(txCache != null ? txCache.generateValidationToken(options, t.getCoding(), vs, expParameters) : null);
      if (t.getCoding().hasSystem()) {
        codeSystemsUsed.add(t.getCoding().getSystem());
      }
      if (txCache != null) { 
        t.setResult(txCache.getValidation(t.getCacheToken()));
      }
    }
    if (options.isUseClient()) {
      for (CodingValidationRequest t : codes) {
        if (!t.hasResult()) {
          try {
            ValueSetValidator vsc = constructValueSetCheckerSimple(options, vs);
            vsc.setThrowToServer(options.isUseServer() && terminologyClientManager.hasClient());
            ValidationResult res = vsc.validateCode("Coding", t.getCoding());
            if (txCache != null) {
              txCache.cacheValidation(t.getCacheToken(), res, TerminologyCache.TRANSIENT);
            }
            t.setResult(res);
          } catch (Exception e) {
          }
        }
      }      
    }  

    for (CodingValidationRequest t : codes) {
      if (!t.hasResult()) {
        String codeKey = t.getCoding().hasVersion() ? t.getCoding().getSystem()+"|"+t.getCoding().getVersion() : t.getCoding().getSystem();
        if (!options.isUseServer()) {
         t.setResult(new ValidationResult(IssueSeverity.WARNING,formatMessage(I18nConstants.UNABLE_TO_VALIDATE_CODE_WITHOUT_USING_SERVER), TerminologyServiceErrorClass.BLOCKED_BY_OPTIONS, null));
        } else if (unsupportedCodeSystems.contains(codeKey)) {
          t.setResult(new ValidationResult(IssueSeverity.ERROR,formatMessage(I18nConstants.UNKNOWN_CODESYSTEM, t.getCoding().getSystem()), TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED, null));      
        } else if (noTerminologyServer) {
          t.setResult(new ValidationResult(IssueSeverity.ERROR,formatMessage(I18nConstants.ERROR_VALIDATING_CODE_RUNNING_WITHOUT_TERMINOLOGY_SERVICES, t.getCoding().getCode(), t.getCoding().getSystem()), TerminologyServiceErrorClass.NOSERVICE, null));
        }
      }
    }
    
    if (expParameters == null)
      throw new Error(formatMessage(I18nConstants.NO_EXPANSIONPROFILE_PROVIDED));
    // for those that that failed, we try to validate on the server
    Parameters batch = new Parameters();
    Set<String> systems = findRelevantSystems(vs);
    ValueSet lastvs = null;
    if (vs != null) {
      if (passVS) {
        batch.addParameter().setName("tx-resource").setResource(vs);
      }
      batch.addParameter("url", vs.getUrl());
    }
    List<CodingValidationRequest> items = new ArrayList<>();
    for (CodingValidationRequest codingValidationRequest : codes) {
      if (!codingValidationRequest.hasResult()) {
        items.add(codingValidationRequest);
        Parameters pIn = constructParameters(options, codingValidationRequest);
        setTerminologyOptions(options, pIn);
        batch.addParameter().setName("validation").setResource(pIn);
        systems.add(codingValidationRequest.getCoding().getSystem());
        findRelevantSystems(systems, codingValidationRequest.getCoding());
      }
    }
    
    if (items.size() > 0) {
      TerminologyClientContext tc = terminologyClientManager.chooseServer(vs, systems, false);
      Parameters resp = processBatch(tc, batch, systems, items.size());
      List<ParametersParameterComponent> validations = resp.getParameters("validation");
      for (int i = 0; i < items.size(); i++) {
        CodingValidationRequest t = items.get(i);
        ParametersParameterComponent r = validations.get(i);

        if (r.getResource() instanceof Parameters) {
          t.setResult(processValidationResult((Parameters) r.getResource(), null, tc.getAddress()));
          if (txCache != null) {
            txCache.cacheValidation(t.getCacheToken(), t.getResult(), TerminologyCache.PERMANENT);
          }
        } else {
          t.setResult(new ValidationResult(IssueSeverity.ERROR, getResponseText(r.getResource()), null).setTxLink(txLog == null ? null : txLog.getLastId()));          
        }
      }
    }    
  }

  private Parameters processBatch(TerminologyClientContext tc, Parameters batch, Set<String> systems, int size) {
    txLog("$batch validate for "+size+" codes on systems "+systems.toString());
    if (terminologyClientManager == null) {
      throw new FHIRException(formatMessage(I18nConstants.ATTEMPT_TO_USE_TERMINOLOGY_SERVER_WHEN_NO_TERMINOLOGY_SERVER_IS_AVAILABLE));
    }
    if (txLog != null) {
      txLog.clearLastId();
    }
    Parameters resp = tc.getClient().batchValidateVS(batch);
    if (resp == null) {
      throw new FHIRException(formatMessage(I18nConstants.TX_SERVER_NO_BATCH_RESPONSE));          
    }
    return resp;
  }

//  @Override
//  public void validateCodeBatchByRef(ValidationOptions options, List<? extends CodingValidationRequest> codes, String vsUrl) {
//    if (options == null) {
//      options = ValidationOptions.defaults();
//    }
//    // 1st pass: what is in the cache?
//    // 2nd pass: What can we do internally
//    // 3rd pass: hit the server
//    for (CodingValidationRequest t : codes) {
//      t.setCacheToken(txCache != null ? txCache.generateValidationToken(options, t.getCoding(), vsUrl, expParameters) : null);
//      if (t.getCoding().hasSystem()) {
//        codeSystemsUsed.add(t.getCoding().getSystem());
//      }
//      if (txCache != null) {
//        t.setResult(txCache.getValidation(t.getCacheToken()));
//      }
//    }
//    ValueSet vs = fetchResource(ValueSet.class, vsUrl);
//    if (options.isUseClient()) {
//      if (vs != null) {
//        for (CodingValidationRequest t : codes) {
//          if (!t.hasResult()) {
//            try {
//              ValueSetValidator vsc = constructValueSetCheckerSimple(options, vs);
//              vsc.setThrowToServer(options.isUseServer() && terminologyClientManager.hasClient());
//              ValidationResult res = vsc.validateCode("Coding", t.getCoding());
//              if (txCache != null) {
//                txCache.cacheValidation(t.getCacheToken(), res, TerminologyCache.TRANSIENT);
//              }
//              t.setResult(res);
//            } catch (Exception e) {
//            }
//          }
//        }
//      }
//    }
//
//    for (CodingValidationRequest t : codes) {
//      if (!t.hasResult()) {
//        String codeKey = t.getCoding().hasVersion() ? t.getCoding().getSystem()+"|"+t.getCoding().getVersion() : t.getCoding().getSystem();
//        if (!options.isUseServer()) {
//         t.setResult(new ValidationResult(IssueSeverity.WARNING,formatMessage(I18nConstants.UNABLE_TO_VALIDATE_CODE_WITHOUT_USING_SERVER), TerminologyServiceErrorClass.BLOCKED_BY_OPTIONS, null));
//        } else if (unsupportedCodeSystems.contains(codeKey)) {
//          t.setResult(new ValidationResult(IssueSeverity.ERROR,formatMessage(I18nConstants.UNKNOWN_CODESYSTEM, t.getCoding().getSystem()), TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED, null));
//        } else if (noTerminologyServer) {
//          t.setResult(new ValidationResult(IssueSeverity.ERROR,formatMessage(I18nConstants.ERROR_VALIDATING_CODE_RUNNING_WITHOUT_TERMINOLOGY_SERVICES, t.getCoding().getCode(), t.getCoding().getSystem()), TerminologyServiceErrorClass.NOSERVICE, null));
//        }
//      }
//    }
//
//    if (expParameters == null)
//      throw new Error(formatMessage(I18nConstants.NO_EXPANSIONPROFILE_PROVIDED));
//    // for those that that failed, we try to validate on the server
//    Bundle batch = new Bundle();
//    batch.setType(BundleType.BATCH);
//    Set<String> systems = vs != null ? findRelevantSystems(vs) : new HashSet<>();
//    for (CodingValidationRequest codingValidationRequest : codes) {
//      if (!codingValidationRequest.hasResult()) {
//        Parameters pIn = constructParameters(options, codingValidationRequest, vsUrl);
//        setTerminologyOptions(options, pIn);
//        BundleEntryComponent be = batch.addEntry();
//        be.setResource(pIn);
//        be.getRequest().setMethod(HTTPVerb.POST);
//        if (vsUrl != null) {
//          be.getRequest().setUrl("ValueSet/$validate-code");
//        } else {
//          be.getRequest().setUrl("CodeSystem/$validate-code");
//        }
//        be.setUserData(UserDataNames.TX_REQUEST, codingValidationRequest);
//        systems.add(codingValidationRequest.getCoding().getSystem());
//      }
//    }
//    TerminologyClientContext tc = terminologyClientManager.chooseServer(vs, systems, false);
//
//    if (batch.getEntry().size() > 0) {
//      Bundle resp = processBatch(tc, batch, systems);
//      for (int i = 0; i < batch.getEntry().size(); i++) {
//        CodingValidationRequest t = (CodingValidationRequest) batch.getEntry().get(i).getUserData(UserDataNames.TX_REQUEST);
//        BundleEntryComponent r = resp.getEntry().get(i);
//
//        if (r.getResource() instanceof Parameters) {
//          t.setResult(processValidationResult((Parameters) r.getResource(), vsUrl, tc.getAddress()));
//          if (txCache != null) {
//            txCache.cacheValidation(t.getCacheToken(), t.getResult(), TerminologyCache.PERMANENT);
//          }
//        } else {
//          t.setResult(new ValidationResult(IssueSeverity.ERROR, getResponseText(r.getResource()), null).setTxLink(txLog == null ? null : txLog.getLastId()));
//        }
//      }
//    }
//  }
//
  private String getResponseText(Resource resource) {
    if (resource instanceof OperationOutcome) {
      return OperationOutcomeRenderer.toString((OperationOutcome) resource);
    }
    return "Todo";
  }

  @Override
  public ValidationResult validateCode(ValidationOptions options, Coding code, ValueSet vs) {
    ValidationContextCarrier ctxt = new ValidationContextCarrier();
    return validateCode(options, "Coding", code, vs, ctxt);
  }
  
  public ValidationResult validateCode(ValidationOptions options, String path, Coding code, ValueSet vs) {
    ValidationContextCarrier ctxt = new ValidationContextCarrier();
    return validateCode(options, path, code, vs, ctxt);
  }

  private final String getCodeKey(Coding code) {
    return code.hasVersion() ? code.getSystem()+"|"+code.getVersion() : code.getSystem();
  }

  @Override
  public ValidationResult validateCode(final ValidationOptions optionsArg, final Coding code, final ValueSet vs, final ValidationContextCarrier ctxt) {
    return validateCode(optionsArg, "Coding", code, vs, ctxt); 
  }
  
  public ValidationResult validateCode(final ValidationOptions optionsArg, String path, final Coding code, final ValueSet vs, final ValidationContextCarrier ctxt) {
  
    ValidationOptions options = optionsArg != null ? optionsArg : ValidationOptions.defaults();
    
    if (code.hasSystem()) {
      codeSystemsUsed.add(code.getSystem());
    }

    final CacheToken cacheToken = cachingAllowed && txCache != null ? txCache.generateValidationToken(options, code, vs, expParameters) : null;
    ValidationResult res = null;
    if (cachingAllowed && txCache != null) {
      res = txCache.getValidation(cacheToken);
    }
    if (res != null) {
      updateUnsupportedCodeSystems(res, code, getCodeKey(code));
      return res;
    }

    List<OperationOutcomeIssueComponent> issues = new ArrayList<>();
    Set<String> unknownSystems = new HashSet<>();
    
    String localError = null;
    String localWarning = null;
    TerminologyServiceErrorClass type = TerminologyServiceErrorClass.UNKNOWN;
    if (options.isUseClient()) {
      // ok, first we try to validate locally
      try {
        ValueSetValidator vsc = constructValueSetCheckerSimple(options, vs, ctxt);
        if (vsc.getOpContext() != null) {
          vsc.getOpContext().note("Validate "+code.toString()+" @ "+path+" against "+(vs == null ? "null" : vs.getVersionedUrl()));
        }
        vsc.setUnknownSystems(unknownSystems);
        vsc.setThrowToServer(options.isUseServer() && terminologyClientManager.hasClient());
        if (!ValueSetUtilities.isServerSide(code.getSystem())) {
          res = vsc.validateCode(path, code.copy());
          if (txCache != null && cachingAllowed) {
            txCache.cacheValidation(cacheToken, res, TerminologyCache.TRANSIENT);
          }
          return res;
        }
      } catch (VSCheckerException e) {
        if (e.isWarning() || e.getType() == TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED) {
          localWarning = e.getMessage();
        } else {  
          localError = e.getMessage();
        }
        if (e.getIssues() != null) {
          issues.addAll(e.getIssues());
        }
        type = e.getType();
      } catch (TerminologyServiceProtectionException e) {
        OperationOutcomeIssueComponent iss = new OperationOutcomeIssueComponent(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.ERROR, e.getType());
        iss.getDetails().setText(e.getMessage());
        iss.setDiagnostics(e.getDiagnostics());
        issues.add(iss);
        return new ValidationResult(IssueSeverity.FATAL, e.getMessage(), e.getError(), issues);
      } catch (Exception e) {
//        e.printStackTrace();!
        localError = e.getMessage();
      }
    }
    
    if (localError != null && !terminologyClientManager.hasClient()) {
      if (unknownSystems.size() > 0) {
        return new ValidationResult(IssueSeverity.ERROR, localError, TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED, issues).setUnknownSystems(unknownSystems);
      } else if (type == TerminologyServiceErrorClass.INTERNAL_ERROR) {
        return new ValidationResult(IssueSeverity.FATAL, localError, TerminologyServiceErrorClass.INTERNAL_ERROR, issues);
      } else {
        return new ValidationResult(IssueSeverity.ERROR, localError, TerminologyServiceErrorClass.UNKNOWN, issues);
      }
    }
    if (localWarning != null && !terminologyClientManager.hasClient()) {
      return new ValidationResult(IssueSeverity.WARNING,formatMessage(I18nConstants.UNABLE_TO_VALIDATE_CODE_WITHOUT_USING_SERVER, localWarning), TerminologyServiceErrorClass.BLOCKED_BY_OPTIONS, issues);       
    }
    if (!options.isUseServer()) {
      if (localWarning != null) {
        return new ValidationResult(IssueSeverity.WARNING,formatMessage(I18nConstants.UNABLE_TO_VALIDATE_CODE_WITHOUT_USING_SERVER, localWarning), TerminologyServiceErrorClass.BLOCKED_BY_OPTIONS, issues);       
      } else {
        return new ValidationResult(IssueSeverity.WARNING,formatMessage(I18nConstants.UNABLE_TO_VALIDATE_CODE_WITHOUT_USING_SERVER, localError), TerminologyServiceErrorClass.BLOCKED_BY_OPTIONS, issues);
      }
    }
    String codeKey = getCodeKey(code);
    if (unsupportedCodeSystems.contains(codeKey)) {
      return new ValidationResult(IssueSeverity.ERROR,formatMessage(I18nConstants.UNKNOWN_CODESYSTEM, code.getSystem()), TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED, issues);      
    }
    
    // if that failed, we try to validate on the server
    if (noTerminologyServer) {
      return new ValidationResult(IssueSeverity.ERROR,formatMessage(I18nConstants.ERROR_VALIDATING_CODE_RUNNING_WITHOUT_TERMINOLOGY_SERVICES, code.getCode(), code.getSystem()), TerminologyServiceErrorClass.NOSERVICE, issues);
    }

    Set<String> systems = findRelevantSystems(code, vs);
    TerminologyClientContext tc = terminologyClientManager.chooseServer(vs, systems, false);
    
    String csumm = cachingAllowed && txCache != null ? txCache.summary(code) : null;
    if (cachingAllowed && txCache != null) {
      txLog("$validate "+csumm+(vs == null ? "" : " for "+ txCache.summary(vs))+" on "+tc.getAddress());
    } else {
      txLog("$validate "+csumm+" before cache exists on "+tc.getAddress());
    }
    try {
      Parameters pIn = constructParameters(options, code);
      res = validateOnServer2(tc, vs, pIn, options, systems);
    } catch (Exception e) {
      res = new ValidationResult(IssueSeverity.ERROR, e.getMessage() == null ? e.getClass().getName() : e.getMessage(), null).setTxLink(txLog == null ? null : txLog.getLastId()).setErrorClass(TerminologyServiceErrorClass.SERVER_ERROR);
    }
    if (!res.isOk() && res.getErrorClass() == TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED && (localError != null && !localError.equals(ValueSetValidator.NO_TRY_THE_SERVER))) {
      res = new ValidationResult(IssueSeverity.ERROR, localError, null).setTxLink(txLog == null ? null : txLog.getLastId()).setErrorClass(type);
    } 
    if (!res.isOk() && localError != null) {
      res.setDiagnostics("Local Error: "+localError.trim()+". Server Error: "+res.getMessage());
    } else if (!res.isOk() && res.getErrorClass() == TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED && res.getUnknownSystems() != null && res.getUnknownSystems().contains(codeKey) && localWarning != null) {
      // we had some problem evaluating locally, but the server doesn't know the code system, so we'll just go with the local error
      res = new ValidationResult(IssueSeverity.WARNING, localWarning, null);
      res.setDiagnostics("Local Warning: "+localWarning.trim()+". Server Error: "+res.getMessage());
      return res;
    }
    updateUnsupportedCodeSystems(res, code, codeKey);
    if (cachingAllowed && txCache != null) { // we never cache unsupported code systems - we always keep trying (but only once per run)
      txCache.cacheValidation(cacheToken, res, TerminologyCache.PERMANENT);
    }
    return res;
  }


  /**
   * ask the terminology system whether parent subsumes child. 
   * 
   * @return true if it does, false if it doesn't, and null if it's not know whether it does
   */
  public Boolean subsumes(ValidationOptions optionsArg, Coding parent, Coding child) {
    ValidationOptions options = optionsArg != null ? optionsArg : ValidationOptions.defaults();

    if (parent.hasSystem()) {
      codeSystemsUsed.add(parent.getSystem());
    } else {
      return null;
    }
    if (child.hasSystem()) {
      codeSystemsUsed.add(child.getSystem());
    } else {
      return null;
    }

    final CacheToken cacheToken = cachingAllowed && txCache != null ? txCache.generateSubsumesToken(options, parent, child, expParameters) : null;
    if (cachingAllowed && txCache != null) {
      Boolean res = txCache.getSubsumes(cacheToken);
      if (res != null) {
        return res;
      }
    }
    
    if (options.isUseClient() && parent.getSystem().equals(child.getSystem())) {
      CodeSystem cs = fetchCodeSystem(parent.getSystem());
      if (cs != null) {
        Boolean b = CodeSystemUtilities.subsumes(cs, parent.getCode(), child.getCode());
        if (txCache != null && cachingAllowed) {
          txCache.cacheSubsumes(cacheToken, b, true);
        }
        return b;
      }
    }

    if (!terminologyClientManager.hasClient() || !options.isUseServer() || unsupportedCodeSystems.contains(parent.getSystem()) || unsupportedCodeSystems.contains(child.getSystem()) || noTerminologyServer) {
      return null;      
    }

    Set<String> systems = new HashSet<>();
    systems.add(parent.getSystem());
    systems.add(child.getSystem());
    TerminologyClientContext tc = terminologyClientManager.chooseServer(null, systems, false);
    
    txLog("$subsumes "+parent.toString()+" > "+child.toString()+" on "+tc.getAddress());

    try {
      Parameters pIn =  new Parameters();
      pIn.addParameter().setName("codingA").setValue(parent);
      pIn.addParameter().setName("codingB").setValue(child);
      if (txLog != null) {
        txLog.clearLastId();
      }
      Parameters pOut = tc.getClient().subsumes(pIn);
      return processSubsumesResult(pOut, tc.getClient().getAddress());
    } catch (Exception e) {
      // e.printStackTrace();
    }
    return null;
  }


  public Boolean processSubsumesResult(Parameters pOut, String server) {
    for (ParametersParameterComponent p : pOut.getParameter()) {
      if (p.hasValue()) {
        if (p.getName().equals("outcome")) {
          return Utilities.existsInList(p.getValue().primitiveValue(), "equivalent", "subsumes");
        }
      }
    }
    return null;
  }

  protected ValueSetExpander constructValueSetExpanderSimple(ValidationOptions options) {
    return new ValueSetExpander(this, new TerminologyOperationContext(this, options, "expansion"));
  }

  protected ValueSetValidator constructValueSetCheckerSimple(ValidationOptions options,  ValueSet vs,  ValidationContextCarrier ctxt) {
    return new ValueSetValidator(this, new TerminologyOperationContext(this, options, "validation"), options, vs, ctxt, expParameters, terminologyClientManager, registry);
  }

  protected ValueSetValidator constructValueSetCheckerSimple( ValidationOptions options,  ValueSet vs) {
    return new ValueSetValidator(this, new TerminologyOperationContext(this, options, "validation"), options, vs, expParameters, terminologyClientManager, registry);
  }

  protected Parameters constructParameters(ITerminologyOperationDetails opCtxt, TerminologyClientContext tcd, ValueSet vs, boolean hierarchical) {
    Parameters p = expParameters.copy();
    p.setParameter("includeDefinition", false);
    p.setParameter("excludeNested", !hierarchical);

    addDependentResources(opCtxt, tcd, p, vs);
    p.addParameter().setName("cache-id").setValue(new IdType(terminologyClientManager.getCacheId()));
    return p;
  }

  protected Parameters constructParameters(ValidationOptions options, Coding coding) {
    Parameters pIn = new Parameters();
    if (options.isGuessSystem()) {
      pIn.addParameter().setName("inferSystem").setValue(new BooleanType(true));
      pIn.addParameter().setName("code").setValue(coding.getCodeElement());
    } else {
      pIn.addParameter().setName("coding").setValue(coding);
    }
    setTerminologyOptions(options, pIn);
    return pIn;
  }

  protected Parameters constructParameters(ValidationOptions options, CodeableConcept codeableConcept) {
    Parameters pIn = new Parameters();
    pIn.addParameter().setName("codeableConcept").setValue(codeableConcept);
    setTerminologyOptions(options, pIn);
    return pIn;
  }

  protected Parameters constructParameters(ValidationOptions options, CodingValidationRequest codingValidationRequest) {
    Parameters pIn = new Parameters();
    if (options.isGuessSystem()) {
      pIn.addParameter().setName("inferSystem").setValue(new BooleanType(true));
      pIn.addParameter().setName("code").setValue(codingValidationRequest.getCoding().getCodeElement());
    } else {      
      pIn.addParameter().setName("coding").setValue(codingValidationRequest.getCoding());
    }
    pIn.addParameters(expParameters);
    return pIn;
  }

  private void updateUnsupportedCodeSystems(ValidationResult res, Coding code, String codeKey) {
    if (res.getErrorClass() == TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED && !code.hasVersion() && fetchCodeSystem(codeKey) == null) {
      unsupportedCodeSystems.add(codeKey);
    }
  }

  private void setTerminologyOptions(ValidationOptions options, Parameters pIn) {
    if (options.hasLanguages()) {
      pIn.addParameter("displayLanguage", options.getLanguages().toString());
    }
    if (options.isMembershipOnly()) {
      pIn.addParameter("valueset-membership-only", true);
    }
    if (options.isDisplayWarningMode()) {
      pIn.addParameter("lenient-display-validation", true);
    }
    if (options.isVersionFlexible()) {
      pIn.addParameter("default-to-latest-version", true);     
    }
  }

  @Override
  public ValidationResult validateCode(ValidationOptions options, CodeableConcept code, ValueSet vs) {
    CacheToken cacheToken = txCache.generateValidationToken(options, code, vs, expParameters);
    ValidationResult res = null;
    if (cachingAllowed) {
      res = txCache.getValidation(cacheToken);
      if (res != null) {
        return res;
      }
    }
    for (Coding c : code.getCoding()) {
      if (c.hasSystem()) {
        codeSystemsUsed.add(c.getSystem());
      }
    }
    Set<String> unknownSystems = new HashSet<>();

    List<OperationOutcomeIssueComponent> issues = new ArrayList<>();
    
    String localError = null;
    String localWarning = null;
    
    if (options.isUseClient()) {
      // ok, first we try to validate locally
      try {
        ValueSetValidator vsc = constructValueSetCheckerSimple(options, vs);
        vsc.setUnknownSystems(unknownSystems);
        vsc.setThrowToServer(options.isUseServer() && terminologyClientManager.hasClient());
        res = vsc.validateCode("CodeableConcept", code);
        if (cachingAllowed) {
          txCache.cacheValidation(cacheToken, res, TerminologyCache.TRANSIENT);
        }
        return res;
      } catch (VSCheckerException e) {
        if (e.isWarning()) {
          localWarning = e.getMessage();
        } else {  
          localError = e.getMessage();
        }
        if (e.getIssues() != null) {
          issues.addAll(e.getIssues());
        }
      } catch (TerminologyServiceProtectionException e) {
        OperationOutcomeIssueComponent iss = new OperationOutcomeIssueComponent(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.ERROR, e.getType());
        iss.getDetails().setText(e.getMessage());
        iss.setDiagnostics(e.getDiagnostics());
        issues.add(iss);
        return new ValidationResult(IssueSeverity.FATAL, e.getMessage(), e.getError(), issues);
      } catch (Exception e) {
//        e.printStackTrace();
        localError = e.getMessage();
      }
    }

    if (localError != null && !terminologyClientManager.hasClient()) {
      if (unknownSystems.size() > 0) {
        return new ValidationResult(IssueSeverity.ERROR, localError, TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED, issues).setUnknownSystems(unknownSystems);
      } else {
        return new ValidationResult(IssueSeverity.ERROR, localError, TerminologyServiceErrorClass.UNKNOWN, issues);
      }
    }
    if (localWarning != null && !terminologyClientManager.hasClient()) {
      return new ValidationResult(IssueSeverity.WARNING,formatMessage(I18nConstants.UNABLE_TO_VALIDATE_CODE_WITHOUT_USING_SERVER, localWarning), TerminologyServiceErrorClass.BLOCKED_BY_OPTIONS, issues);       
    }
    
    if (!options.isUseServer()) {
      return new ValidationResult(IssueSeverity.WARNING, "Unable to validate code without using server", TerminologyServiceErrorClass.BLOCKED_BY_OPTIONS, null);      
    }
    
    // if that failed, we try to validate on the server
    if (noTerminologyServer) {
      return new ValidationResult(IssueSeverity.ERROR, "Error validating code: running without terminology services", TerminologyServiceErrorClass.NOSERVICE, null);
    }
    Set<String> systems = findRelevantSystems(code, vs);
    TerminologyClientContext tc = terminologyClientManager.chooseServer(vs, systems, false);

    txLog("$validate "+txCache.summary(code)+" for "+ txCache.summary(vs)+" on "+tc.getAddress());
    try {
      Parameters pIn = constructParameters(options, code);
      res = validateOnServer2(tc, vs, pIn, options, systems);
    } catch (Exception e) {
      issues.clear();
      OperationOutcomeIssueComponent iss = new OperationOutcomeIssueComponent(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.ERROR, org.hl7.fhir.r5.model.OperationOutcome.IssueType.EXCEPTION);
      iss.getDetails().setText(e.getMessage());
      issues.add(iss);
      res = new ValidationResult(IssueSeverity.ERROR, e.getMessage() == null ? e.getClass().getName() : e.getMessage(), issues).setTxLink(txLog == null ? null : txLog.getLastId()).setErrorClass(TerminologyServiceErrorClass.SERVER_ERROR);
    }
    if (cachingAllowed) {
      txCache.cacheValidation(cacheToken, res, TerminologyCache.PERMANENT);
    }
    return res;
  }

  private Set<String> findRelevantSystems(ValueSet vs) {
    Set<String> set = new HashSet<>();
    if (vs != null) {
      findRelevantSystems(set, vs);
    }
    return set;
  }

  private Set<String> findRelevantSystems(CodeableConcept code, ValueSet vs) {
    Set<String> set = new HashSet<>();
    if (vs != null) {
      findRelevantSystems(set, vs);
    }
    for (Coding c : code.getCoding()) {      
      findRelevantSystems(set, c);
    }
    return set;
  }

  private Set<String> findRelevantSystems(Coding code, ValueSet vs) {
    Set<String> set = new HashSet<>();
    if (vs != null) {
      findRelevantSystems(set, vs);
    }
    if (code != null) {      
      findRelevantSystems(set, code);
    }
    return set;
  }

  private void findRelevantSystems(Set<String> set, ValueSet vs) {
    for (ConceptSetComponent inc : vs.getCompose().getInclude()) {
      findRelevantSystems(set, inc);
    }
    for (ConceptSetComponent inc : vs.getCompose().getExclude()) {
      findRelevantSystems(set, inc);
    }    
  }

  private void findRelevantSystems(Set<String> set, ConceptSetComponent inc) {
    if (inc.hasSystem()) {
      if (inc.hasVersion()) {
        set.add(inc.getSystem()+"|"+inc.getVersion());
      } else {
        set.add(inc.getSystem());
      }
    }
    for (CanonicalType u : inc.getValueSet()) {
      ValueSet vs = fetchResource(ValueSet.class, u.getValue());
      if (vs != null) {
        findRelevantSystems(set, vs);
      } else {
        set.add(TerminologyClientManager.UNRESOLVED_VALUESET);
      }
    }
  }

  private void findRelevantSystems(Set<String> set, Coding c) {
    if (c.hasSystem()) {
      if (c.hasVersion()) {
        set.add(c.getSystem()+"|"+c.getVersion());
      } else {
        set.add(c.getSystem());
      }
    }    
  }

  protected ValidationResult validateOnServer(TerminologyClientContext tc, ValueSet vs, Parameters pin, ValidationOptions options) throws FHIRException {
    return  validateOnServer2(tc, vs, pin, options, null);
  }
  
  protected ValidationResult validateOnServer2(TerminologyClientContext tc, ValueSet vs, Parameters pin, ValidationOptions options, Set<String> systems) throws FHIRException {

    if (vs != null) {
      for (ConceptSetComponent inc : vs.getCompose().getInclude()) {
        codeSystemsUsed.add(inc.getSystem());
      }
      for (ConceptSetComponent inc : vs.getCompose().getExclude()) {
        codeSystemsUsed.add(inc.getSystem());
      }
    }

    addServerValidationParameters(null, tc, vs, pin, options, systems);
    
    if (txLog != null) {
      txLog.clearLastId();
    }
    if (tc == null) {
      throw new FHIRException(formatMessage(I18nConstants.ATTEMPT_TO_USE_TERMINOLOGY_SERVER_WHEN_NO_TERMINOLOGY_SERVER_IS_AVAILABLE));
    }
    Parameters pOut;
    if (vs == null) {
      pOut = tc.getClient().validateCS(pin);
    } else {
      pOut = tc.getClient().validateVS(pin);
    }
    return processValidationResult(pOut, vs == null ? null : vs.getUrl(), tc.getClient().getAddress());
  }

  protected void addServerValidationParameters(ITerminologyOperationDetails opCtxt, TerminologyClientContext terminologyClientContext, ValueSet vs, Parameters pin, ValidationOptions options) {
    addServerValidationParameters(opCtxt, terminologyClientContext, vs, pin, options, null);
  }
  
  protected void addServerValidationParameters(ITerminologyOperationDetails opCtxt, TerminologyClientContext terminologyClientContext, ValueSet vs, Parameters pin, ValidationOptions options, Set<String> systems) {
    boolean cache = false;
    if (vs != null) {
      if (terminologyClientContext != null && terminologyClientContext.isTxCaching() && terminologyClientContext.getCacheId() != null && vs.getUrl() != null && terminologyClientContext.getCached().contains(vs.getUrl()+"|"+ vs.getVersion())) {
        pin.addParameter().setName("url").setValue(new UriType(vs.getUrl()));
        if (vs.hasVersion()) {
          pin.addParameter().setName("valueSetVersion").setValue(new StringType(vs.getVersion()));            
        }
      } else if (options.getVsAsUrl()){
        pin.addParameter().setName("url").setValue(new UriType(vs.getUrl()));
      } else {
        if (vs.hasCompose() && vs.hasExpansion()) {
          vs = vs.copy();
          vs.setExpansion(null);
        }
        pin.addParameter().setName("valueSet").setResource(vs);
        if (vs.getUrl() != null) {
          terminologyClientContext.getCached().add(vs.getUrl()+"|"+ vs.getVersion());
        }
      }
      cache = true;
      addDependentResources(opCtxt, terminologyClientContext, pin, vs);
    }
    if (systems != null) {
      for (String s : systems) {
        cache = addDependentCodeSystem(opCtxt, terminologyClientContext, pin, s, null) || cache;
      }
    }
    pin.addParameter().setName("cache-id").setValue(new IdType(terminologyClientManager.getCacheId()));
    for (ParametersParameterComponent pp : pin.getParameter()) {
      if (pp.getName().equals("profile")) {
        throw new Error(formatMessage(I18nConstants.CAN_ONLY_SPECIFY_PROFILE_IN_THE_CONTEXT));
      }
    }
    if (expParameters == null) {
      throw new Error(formatMessage(I18nConstants.NO_EXPANSIONPROFILE_PROVIDED));
    }
    String defLang = null;
    for (ParametersParameterComponent pp : expParameters.getParameter()) {
      if ("defaultDisplayLanguage".equals(pp.getName())) {
        defLang = pp.getValue().primitiveValue();
      } else if (!pin.hasParameter(pp.getName())) {
        pin.addParameter(pp);
      } else if ("displayLanguage".equals(pp.getName())) {
        pin.setParameter(pp);
      }
    }
    if (defLang != null && !pin.hasParameter("displayLanguage")) {
      pin.addParameter().setName("displayLanguage").setValue(new CodeType(defLang));
    }

    if (options.isDisplayWarningMode()) {
      pin.addParameter("mode","lenient-display-validation");
    }
    pin.addParameter("diagnostics", true);
  }

  private boolean addDependentResources(ITerminologyOperationDetails opCtxt, TerminologyClientContext tc, Parameters pin, ValueSet vs) {
    boolean cache = false;
    for (ConceptSetComponent inc : vs.getCompose().getInclude()) {
      cache = addDependentResources(opCtxt, tc, pin, inc, vs) || cache;
    }
    for (ConceptSetComponent inc : vs.getCompose().getExclude()) {
      cache = addDependentResources(opCtxt, tc, pin, inc, vs) || cache;
    }
    return cache;
  }

  private boolean addDependentResources(ITerminologyOperationDetails opCtxt, TerminologyClientContext tc, Parameters pin, ConceptSetComponent inc, Resource src) {
    boolean cache = false;
    for (CanonicalType c : inc.getValueSet()) {
      ValueSet vs = fetchResource(ValueSet.class, c.getValue(), src);
      if (vs != null && !hasCanonicalResource(pin, "tx-resource", vs.getVUrl())) {
        cache = checkAddToParams(tc, pin, vs) || cache;
        addDependentResources(opCtxt, tc, pin, vs);
        for (Extension ext : vs.getExtensionsByUrl(ExtensionDefinitions.EXT_VS_CS_SUPPL_NEEDED)) {
          if (ext.hasValueCanonicalType()) {
            String url = ext.getValueCanonicalType().asStringValue();
            CodeSystem supp = fetchResource(CodeSystem.class, url);
            if (supp != null) {
              if (opCtxt != null) {
                opCtxt.seeSupplement(supp);
              }
              cache = checkAddToParams(tc, pin, supp) || cache;            
            }
          }
        }
      }
    }
    String sys = inc.getSystem();
    cache = addDependentCodeSystem(opCtxt, tc, pin, sys, src) || cache;
    return cache;
  }

  public boolean addDependentCodeSystem(ITerminologyOperationDetails opCtxt, TerminologyClientContext tc, Parameters pin, String sys, Resource src) {
    boolean cache = false;
    CodeSystem cs = fetchResource(CodeSystem.class, sys, src);
    if (cs != null && !hasCanonicalResource(pin, "tx-resource", cs.getVUrl()) && (cs.getContent() == CodeSystemContentMode.COMPLETE || cs.getContent() == CodeSystemContentMode.FRAGMENT)) {
      cache = checkAddToParams(tc, pin, cs) || cache;
    }
    for (CodeSystem supp : codeSystems.getSupplements(cs)) {
      if (opCtxt != null) {
        opCtxt.seeSupplement(supp);
      }
      if (!hasCanonicalResource(pin, "tx-resource", supp.getVUrl()) ) {
        cache = checkAddToParams(tc, pin, supp) || cache;
      }
    }
    if (sys != null) {
      // we also have to look at this by version because the resource might not be versioned or we might not have a copy
      for (CodeSystem supp : codeSystems.getSupplements(sys)) {

        if (opCtxt != null) {
          opCtxt.seeSupplement(supp);
        }
        if (!hasCanonicalResource(pin, "tx-resource", supp.getVUrl()) ) {
          cache = checkAddToParams(tc, pin, supp) || cache;
        }
      }
      if (!sys.contains("!")) {
        sys = getFixedVersion(sys, pin);
        if (sys != null) {
          for (CodeSystem supp : codeSystems.getSupplements(sys)) {
            if (opCtxt != null) {
              opCtxt.seeSupplement(supp);
            }
            if (!hasCanonicalResource(pin, "tx-resource", supp.getVUrl()) ) {
              cache = checkAddToParams(tc, pin, supp) || cache;
            }
          }
        }
      }
    }
    return cache;
  }

  private String getFixedVersion(String sys, Parameters pin) {
    for (ParametersParameterComponent p : pin.getParameter()) {
      if (Utilities.existsInList(p.getName(), "system-version", "force-system-version", "default-system-version")) {
        if (p.hasValuePrimitive() && p.getValue().primitiveValue() != null && p.getValue().primitiveValue().startsWith(sys)) {
          return p.getValue().primitiveValue();
        }
      }
    }
    return null;
  }

  private boolean checkAddToParams(TerminologyClientContext tc, Parameters pin, CanonicalResource cr) {
    boolean cache = false;
    boolean addToParams = false;
    if (tc.usingCache()) {
      if (!tc.alreadyCached(cr)) {
        tc.addToCache(cr);

        logger.logDebugMessage(LogCategory.CONTEXT, "add to cache: "+cr.getVUrl());

        addToParams = true;
        cache = true;
      } else {
        logger.logDebugMessage(LogCategory.CONTEXT,"already cached: "+cr.getVUrl());
      }
    } else {
      addToParams = true;
    }
    if (addToParams) {
      pin.addParameter().setName("tx-resource").setResource(cr);
    }
    return cache;
  }

  private boolean hasCanonicalResource(Parameters pin, String name, String vUrl) {
    for (ParametersParameterComponent p : pin.getParameter()) {
      if (name.equals(p.getName()) && p.hasResource() &&
          p.getResource() instanceof CanonicalResource && vUrl.equals(((CanonicalResource) p.getResource()).getVUrl())) {
        return true;
      }
    }
    return false;
  }

  public ValidationResult processValidationResult(Parameters pOut, String vs, String server) {
    boolean ok = false;
    String message = "No Message returned";
    String display = null;
    String system = null;
    String code = null;
    String version = null;
    boolean inactive = false;
    String status = null;
    List<OperationOutcomeIssueComponent> issues = new ArrayList<>();
    Set<String> unknownSystems = new HashSet<>();

    TerminologyServiceErrorClass err = TerminologyServiceErrorClass.UNKNOWN;
    for (ParametersParameterComponent p : pOut.getParameter()) {
      if (p.hasValue()) {
        if (p.getName().equals("result")) {
          ok = ((BooleanType) p.getValue()).getValue().booleanValue();
        } else if (p.getName().equals("message")) {
          message = p.getValue().primitiveValue();
        } else if (p.getName().equals("display")) {
          display = p.getValue().primitiveValue();
        } else if (p.getName().equals("system")) {
          system = ((PrimitiveType<?>) p.getValue()).asStringValue();
        } else if (p.getName().equals("version")) {
          version = ((PrimitiveType<?>) p.getValue()).asStringValue();
        } else if (p.getName().equals("code")) {
          code = ((PrimitiveType<?>) p.getValue()).asStringValue();
        } else if (p.getName().equals("inactive")) {
          inactive = "true".equals(((PrimitiveType<?>) p.getValue()).asStringValue());
        } else if (p.getName().equals("status")) {
          status = ((PrimitiveType<?>) p.getValue()).asStringValue();
        } else if (p.getName().equals("x-caused-by-unknown-system")) {
          String unkSystem = ((PrimitiveType<?>) p.getValue()).asStringValue();
          if (unkSystem != null && unkSystem.contains("|")) {
            err = TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED_VERSION; 
            system = unkSystem.substring(0, unkSystem.indexOf("|"));
            version = unkSystem.substring(unkSystem.indexOf("|")+1);
          } else {
            err = TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED;            
            unknownSystems.add(unkSystem);      
          }
        } else if (p.getName().equals("x-unknown-system")) {
          unknownSystems.add(((PrimitiveType<?>) p.getValue()).asStringValue());      
        } else if (p.getName().equals("warning-withdrawn")) {
          String msg = ((PrimitiveType<?>) p.getValue()).asStringValue();
          OperationOutcomeIssueComponent iss = new OperationOutcomeIssueComponent(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.INFORMATION, org.hl7.fhir.r5.model.OperationOutcome.IssueType.BUSINESSRULE);
          iss.getDetails().setText(formatMessage(vs == null ? I18nConstants.MSG_WITHDRAWN : I18nConstants.MSG_WITHDRAWN_SRC, msg, vs, impliedType(msg)));              
          issues.add(iss);
        } else if (p.getName().equals("warning-deprecated")) {
          String msg = ((PrimitiveType<?>) p.getValue()).asStringValue();
          OperationOutcomeIssueComponent iss = new OperationOutcomeIssueComponent(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.INFORMATION, org.hl7.fhir.r5.model.OperationOutcome.IssueType.BUSINESSRULE);
          iss.getDetails().setText(formatMessage(vs == null ? I18nConstants.MSG_DEPRECATED : I18nConstants.MSG_DEPRECATED_SRC, msg, vs, impliedType(msg)));              
          issues.add(iss);
        } else if (p.getName().equals("warning-retired")) {
          String msg = ((PrimitiveType<?>) p.getValue()).asStringValue();
          OperationOutcomeIssueComponent iss = new OperationOutcomeIssueComponent(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.INFORMATION, org.hl7.fhir.r5.model.OperationOutcome.IssueType.BUSINESSRULE);
          iss.getDetails().setText(formatMessage(vs == null ? I18nConstants.MSG_RETIRED : I18nConstants.MSG_RETIRED_SRC, msg, vs, impliedType(msg)));              
          issues.add(iss);
        } else if (p.getName().equals("warning-experimental")) {
          String msg = ((PrimitiveType<?>) p.getValue()).asStringValue();
          OperationOutcomeIssueComponent iss = new OperationOutcomeIssueComponent(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.INFORMATION, org.hl7.fhir.r5.model.OperationOutcome.IssueType.BUSINESSRULE);
          iss.getDetails().setText(formatMessage(vs == null ? I18nConstants.MSG_EXPERIMENTAL : I18nConstants.MSG_EXPERIMENTAL_SRC, msg, vs, impliedType(msg)));              
          issues.add(iss);
        } else if (p.getName().equals("warning-draft")) {
          String msg = ((PrimitiveType<?>) p.getValue()).asStringValue();
          OperationOutcomeIssueComponent iss = new OperationOutcomeIssueComponent(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.INFORMATION, org.hl7.fhir.r5.model.OperationOutcome.IssueType.BUSINESSRULE);
          iss.getDetails().setText(formatMessage(vs == null ? I18nConstants.MSG_DRAFT : I18nConstants.MSG_DRAFT_SRC, msg, vs, impliedType(msg)));              
          issues.add(iss);
        } else if (p.getName().equals("cause")) {
          try {
            IssueType it = IssueType.fromCode(((StringType) p.getValue()).getValue());
            if (it == IssueType.UNKNOWN) {
              err = TerminologyServiceErrorClass.UNKNOWN;
            } else if (it == IssueType.NOTFOUND) {
              err = TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED;
            } else if (it == IssueType.NOTSUPPORTED) {
              err = TerminologyServiceErrorClass.VALUESET_UNSUPPORTED;
            } else {
              err = null;
            }
          } catch (FHIRException e) {
          }
        }
      } else if (p.hasResource()) {
        if (p.getName().equals("issues")) {
          OperationOutcome oo = (OperationOutcome) p.getResource();
          for (OperationOutcomeIssueComponent iss : oo.getIssue()) {
            iss.addExtension(ExtensionDefinitions.EXT_ISSUE_SERVER, new UrlType(server));
            issues.add(iss);
          }
        } else {
          // nothing?
        }
      }
    }
    ValidationResult res = null;
    if (!ok) {
      res = new ValidationResult(IssueSeverity.ERROR, message, err, null).setTxLink(txLog == null ? null : txLog.getLastId());
      if (code != null) {
        res.setDefinition(new ConceptDefinitionComponent().setDisplay(display).setCode(code));
        res.setDisplay(display);
      }
      if (system != null) {
        res.setSystem(system);
      }
      if (version != null) {
        res.setVersion(version);
      }
    } else if (message != null && !message.equals("No Message returned")) { 
      res = new ValidationResult(IssueSeverity.WARNING, message, system, version, new ConceptDefinitionComponent().setDisplay(display).setCode(code), display, null).setTxLink(txLog == null ? null : txLog.getLastId());
    } else if (display != null) {
      res = new ValidationResult(system, version, new ConceptDefinitionComponent().setDisplay(display).setCode(code), display).setTxLink(txLog == null ? null : txLog.getLastId());
    } else {
      res = new ValidationResult(system, version, new ConceptDefinitionComponent().setCode(code), null).setTxLink(txLog == null ? null : txLog.getLastId());
    }
    res.setIssues(issues);
    res.setStatus(inactive, status);
    res.setUnknownSystems(unknownSystems);
    res.setServer(server);
    res.setParameters(pOut);
    return res;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------
  
  private Object impliedType(String msg) {
    if (msg.contains("/CodeSystem")) {
      return "CodeSystem";
    }
    if (msg.contains("/ValueSet")) {
      return "ValueSet";
    }
    return "item";
  }

  public void initTxCache(String cachePath) throws FileNotFoundException, FHIRException, IOException {
    if (cachePath != null) {
      txCache = new TerminologyCache(lock, cachePath);
      initTxCache(txCache);
    }
  }
  
  public void initTxCache(TerminologyCache cache) {
    txCache = cache;
    terminologyClientManager.setCache(txCache);
  }

  public void clearTSCache(String url) throws Exception {
    txCache.removeCS(url);
  }

  public boolean isCanRunWithoutTerminology() {
    return canRunWithoutTerminology;
  }

  public void setCanRunWithoutTerminology(boolean canRunWithoutTerminology) {
    this.canRunWithoutTerminology = canRunWithoutTerminology;
  }

  public void setLogger(@Nonnull org.hl7.fhir.r5.context.ILoggingService logger) {
    this.logger = logger;
    getTxClientManager().setLogger(logger);
  }

  public Parameters getExpansionParameters() {
    return expParameters;
  }

  public void setExpansionParameters(Parameters expParameters) {
    this.expParameters = expParameters;
    this.terminologyClientManager.setExpansionParameters(expParameters);
  }

  @Override
  public boolean isNoTerminologyServer() {
    return noTerminologyServer || !terminologyClientManager.hasClient();
  }

  public void setNoTerminologyServer(boolean noTerminologyServer) {
    this.noTerminologyServer = noTerminologyServer;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public List<String> getResourceNames(FhirPublication fhirVersion) {
    return getResourceNames();    
  }
  
  public Set<String> getResourceNamesAsSet(FhirPublication fhirVersion) {
    return getResourceNamesAsSet();
  }
  
  @Override
  public Set<String> getResourceNamesAsSet() {
    Set<String> res = new HashSet<String>();
    res.addAll(getResourceNames());
    return res;
  }

  public boolean isAllowLoadingDuplicates() {
    return allowLoadingDuplicates;
  }

  public void setAllowLoadingDuplicates(boolean allowLoadingDuplicates) {
    this.allowLoadingDuplicates = allowLoadingDuplicates;
  }

  @Override
  public <T extends Resource> T fetchResourceWithException(Class<T> class_, String uri) throws FHIRException {
    return fetchResourceWithException(class_, uri, null);
  }
  
  public <T extends Resource> T fetchResourceWithException(String cls, String uri) throws FHIRException {
    return fetchResourceWithExceptionByVersion(cls, uri, null, null);
  }
  
  public <T extends Resource> T fetchResourceWithException(Class<T> class_, String uri, Resource sourceForReference) throws FHIRException {
    return fetchResourceWithExceptionByVersion(class_, uri, null, sourceForReference);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Resource> T fetchResourceWithExceptionByVersion(Class<T> class_, String uri, String version, Resource sourceForReference) throws FHIRException {
    if (uri == null) {
      return null;
    }
    if (uri.startsWith("#")) {
      if (sourceForReference != null && sourceForReference instanceof DomainResource) {
        for (Resource r : ((DomainResource) sourceForReference).getContained()) {
          if (r.getClass() == class_ &&( "#"+r.getIdBase()).equals(uri)) {
            if (r instanceof CanonicalResource) {
              CanonicalResource cr = (CanonicalResource) r;
              if (!cr.hasUrl()) {
                cr.setUrl(UUIDUtilities.makeUuidUrn());
              }              
            }
            return (T) r;
          }
        }
      }
      return null;
    }
    
    if (QA_CHECK_REFERENCE_SOURCE) {
      // it can be tricky to trace the source of a reference correctly. The code isn't water tight,
      // particularly around snapshot generation. Enable this code to check that the references are 
      // correct (but it's slow)
      if (sourceForReference != null && uri.contains("ValueSet")) {
        if (!ResourceUtilities.hasURL(uri, sourceForReference)) {
          log.warn("Claimed source doesn't have url in it: "+sourceForReference.fhirType()+"/"+sourceForReference.getIdPart()+" -> "+uri);
        }
      }
    }
   
    List<String> pvlist = new ArrayList<>();
    if (sourceForReference != null && sourceForReference.getSourcePackage() != null) {
      populatePVList(pvlist, sourceForReference.getSourcePackage());
    }
    
    if (class_ == StructureDefinition.class) {
      uri = ProfileUtilities.sdNs(uri, null);
    }
    // matchbox patch for #265, fhirVersioned URL's eg (return urls with fhirVersion)
    if (Utilities.isAbsoluteUrl(uri)) {
      int index = uri.indexOf("/"+this.version.substring(0,3)+"/");
      if (index >= 0) {
        uri = uri.substring(0, index)+uri.substring(index+4);
      }
    }
    synchronized (lock) {

      if (version == null) {
        if (uri.contains("|")) {
          version = uri.substring(uri.lastIndexOf("|")+1);
          uri = uri.substring(0, uri.lastIndexOf("|"));
        }
      } else {
        assert !uri.contains("|");
      }
      if (uri.contains("#")) {
        uri = uri.substring(0, uri.indexOf("#"));
      } 
      if (class_ == Resource.class || class_ == null) {
        if (structures.has(uri)) {
          return (T) structures.get(uri, version, pvlist);
        }        
        if (guides.has(uri)) {
          return (T) guides.get(uri, version, pvlist);
        } 
        if (capstmts.has(uri)) {
          return (T) capstmts.get(uri, version, pvlist);
        } 
        if (measures.has(uri)) {
          return (T) measures.get(uri, version, pvlist);
        } 
        if (libraries.has(uri)) {
          return (T) libraries.get(uri, version, pvlist);
        } 
        if (valueSets.has(uri)) {
          return (T) valueSets.get(uri, version, pvlist);
        } 
        if (codeSystems.has(uri)) {
          return (T) codeSystems.get(uri, version, pvlist);
        } 
        if (systems.has(uri)) {
          return (T) systems.get(uri, version, pvlist);
        } 
        if (operations.has(uri)) {
          return (T) operations.get(uri, version, pvlist);
        } 
        if (searchParameters.has(uri)) {
          return (T) searchParameters.get(uri, version, pvlist);
        } 
        if (plans.has(uri)) {
          return (T) plans.get(uri, version, pvlist);
        } 
        if (maps.has(uri)) {
          return (T) maps.get(uri, version, pvlist);
        } 
        if (transforms.has(uri)) {
          return (T) transforms.get(uri, version, pvlist);
        } 
        if (actors.has(uri)) {
          return (T) actors.get(uri, version, pvlist);
        } 
        if (requirements.has(uri)) {
          return (T) requirements.get(uri, version, pvlist);
        } 
        if (questionnaires.has(uri)) {
          return (T) questionnaires.get(uri, version, pvlist);
        } 

        for (Map<String, ResourceProxy> rt : allResourcesById.values()) {
          for (ResourceProxy r : rt.values()) {
            if (uri.equals(r.getUrl())) {
              if (version == null || version == r.getResource().getMeta().getVersionId()) {
                return (T) r.getResource();
              }
            }
          }            
        }
        if (uri.matches(Constants.URI_REGEX) && !uri.contains("ValueSet")) {
          return null;
        }

        // it might be a special URL.
//        if (Utilities.isAbsoluteUrl(uri) || uri.startsWith("ValueSet/")) {
//          Resource res = null; // findTxValueSet(uri);
//          if (res != null) {
//            return (T) res;
//          }
//        }
        return null;      
      } else if (class_ == ImplementationGuide.class) {
        return (T) guides.get(uri, version, pvlist);
      } else if (class_ == CapabilityStatement.class) {
        return (T) capstmts.get(uri, version, pvlist);
      } else if (class_ == Measure.class) {
        return (T) measures.get(uri, version, pvlist);
      } else if (class_ == Library.class) {
        return (T) libraries.get(uri, version, pvlist);
      } else if (class_ == StructureDefinition.class) {
        return (T) structures.get(uri, version, pvlist);
      } else if (class_ == StructureMap.class) {
        return (T) transforms.get(uri, version, pvlist);
      } else if (class_ == NamingSystem.class) {
        return (T) systems.get(uri, version, pvlist);
      } else if (class_ == ValueSet.class) {
        return (T) valueSets.get(uri, version, pvlist);
      } else if (class_ == CodeSystem.class) {
        return (T) codeSystems.get(uri, version, pvlist);
      } else if (class_ == ConceptMap.class) {
        return (T) maps.get(uri, version, pvlist);
      } else if (class_ == ActorDefinition.class) {
        return (T) actors.get(uri, version, pvlist);
      } else if (class_ == Requirements.class) {
        return (T) requirements.get(uri, version, pvlist);
      } else if (class_ == PlanDefinition.class) {
        return (T) plans.get(uri, version, pvlist);
      } else if (class_ == OperationDefinition.class) {
        OperationDefinition od = operations.get(uri, version);
        return (T) od;
      } else if (class_ == Questionnaire.class) {
        return (T) questionnaires.get(uri, version, pvlist);
      } else if (class_ == SearchParameter.class) {
        SearchParameter res = searchParameters.get(uri, version, pvlist);
        return (T) res;
      }
      if (class_ == CodeSystem.class && codeSystems.has(uri)) { 
        return (T) codeSystems.get(uri, version, pvlist);
      }
      if (class_ == ValueSet.class && valueSets.has(uri)) {
        return (T) valueSets.get(uri, version, pvlist);
      } 
      
      if (class_ == Questionnaire.class) {
        return (T) questionnaires.get(uri, version, pvlist);
      } 
      if (supportedCodeSystems.containsKey(uri)) {
        return null;
      } 
      throw new FHIRException(formatMessage(I18nConstants.NOT_DONE_YET_CANT_FETCH_, uri));
    }
  }

  private void populatePVList(List<String> pvlist, PackageInformation sourcePackage) {
    pvlist.add(sourcePackage.getVID());
    List<String> toadd = new ArrayList<>();
    do {
      toadd.clear();
      for (String s : pvlist) {
        PackageInformation pi = packages.get(s);
        if (pi != null) {
          for (String v : pi.getDependencies()) {
            if (!pvlist.contains(v) && !toadd.contains(v)) {
              toadd.add(v);
            }
          }
        }        
      }
      pvlist.addAll(toadd);
    } while (toadd.size() > 0);
  }

  public PackageInformation getPackageForUrl(String uri) {
    if (uri == null) {
      return null;
    }
    uri = ProfileUtilities.sdNs(uri, null);

    synchronized (lock) {

      String version = null;
      if (uri.contains("|")) {
        version = uri.substring(uri.lastIndexOf("|")+1);
        uri = uri.substring(0, uri.lastIndexOf("|"));
      }
      if (uri.contains("#")) {
        uri = uri.substring(0, uri.indexOf("#"));
      } 
      if (structures.has(uri)) {
        return structures.getPackageInfo(uri, version);
      }        
      if (guides.has(uri)) {
        return guides.getPackageInfo(uri, version);
      } 
      if (capstmts.has(uri)) {
        return capstmts.getPackageInfo(uri, version);
      } 
      if (measures.has(uri)) {
        return measures.getPackageInfo(uri, version);
      } 
      if (libraries.has(uri)) {
        return libraries.getPackageInfo(uri, version);
      } 
      if (valueSets.has(uri)) {
        return valueSets.getPackageInfo(uri, version);
      } 
      if (codeSystems.has(uri)) {
        return codeSystems.getPackageInfo(uri, version);
      } 
      if (operations.has(uri)) {
        return operations.getPackageInfo(uri, version);
      } 
      if (searchParameters.has(uri)) {
        return searchParameters.getPackageInfo(uri, version);
      } 
      if (plans.has(uri)) {
        return plans.getPackageInfo(uri, version);
      } 
      if (maps.has(uri)) {
        return maps.getPackageInfo(uri, version);
      } 
      if (transforms.has(uri)) {
        return transforms.getPackageInfo(uri, version);
      } 
      if (actors.has(uri)) {
        return actors.getPackageInfo(uri, version);
      } 
      if (requirements.has(uri)) {
        return requirements.getPackageInfo(uri, version);
      } 
      if (questionnaires.has(uri)) {
        return questionnaires.getPackageInfo(uri, version);
      }         
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Resource> T fetchResourceWithExceptionByVersion(String cls, String uri, String version, CanonicalResource source) throws FHIRException {
    if (uri == null) {
      return null;
    }
   
    if ("StructureDefinition".equals(cls)) {
      uri = ProfileUtilities.sdNs(uri, null);
    }
    synchronized (lock) {

      if (version == null) {
        if (uri.contains("|")) {
          version = uri.substring(uri.lastIndexOf("|")+1);
          uri = uri.substring(0, uri.lastIndexOf("|"));
        }
      } else {
        boolean b = !uri.contains("|");
        assert b;
      }
      if (uri.contains("#")) {
        uri = uri.substring(0, uri.indexOf("#"));
      } 
      if (cls == null || "Resource".equals(cls)) {
        if (structures.has(uri)) {
          return (T) structures.get(uri, version);
        } 
        if (guides.has(uri)) {
          return (T) guides.get(uri, version);
        } 
        if (capstmts.has(uri)) {
          return (T) capstmts.get(uri, version);
        } 
        if (measures.has(uri)) {
          return (T) measures.get(uri, version);
        } 
        if (libraries.has(uri)) {
          return (T) libraries.get(uri, version);
        } 
        if (valueSets.has(uri)) {
          return (T) valueSets.get(uri, version);
        } 
        if (codeSystems.has(uri)) {
          return (T) codeSystems.get(uri, version);
        } 
        if (operations.has(uri)) {
          return (T) operations.get(uri, version);
        } 
        if (searchParameters.has(uri)) {
          return (T) searchParameters.get(uri, version);
        } 
        if (plans.has(uri)) {
          return (T) plans.get(uri, version);
        } 
        if (maps.has(uri)) {
          return (T) maps.get(uri, version);
        } 
        if (transforms.has(uri)) {
          return (T) transforms.get(uri, version);
        } 
        if (actors.has(uri)) {
          return (T) actors.get(uri, version);
        } 
        if (requirements.has(uri)) {
          return (T) requirements.get(uri, version);
        } 
        if (questionnaires.has(uri)) {
          return (T) questionnaires.get(uri, version);
        } 
        for (Map<String, ResourceProxy> rt : allResourcesById.values()) {
          for (ResourceProxy r : rt.values()) {
            if (uri.equals(r.getUrl())) {
              return (T) r.getResource();
            }
          }            
        }
      } else if ("ImplementationGuide".equals(cls)) {
        return (T) guides.get(uri, version);
      } else if ("CapabilityStatement".equals(cls)) {
        return (T) capstmts.get(uri, version);
      } else if ("Measure".equals(cls)) {
        return (T) measures.get(uri, version);
      } else if ("Library".equals(cls)) {
        return (T) libraries.get(uri, version);
      } else if ("StructureDefinition".equals(cls)) {
        return (T) structures.get(uri, version);
      } else if ("StructureMap".equals(cls)) {
        return (T) transforms.get(uri, version);
      } else if ("Requirements".equals(cls)) {
        return (T) requirements.get(uri, version);
      } else if ("ActorDefinition".equals(cls)) {
        return (T) actors.get(uri, version);
      } else if ("ValueSet".equals(cls)) {
        return (T) valueSets.get(uri, version);
      } else if ("CodeSystem".equals(cls)) {
        return (T) codeSystems.get(uri, version);
      } else if ("NamingSystem".equals(cls)) {
        return (T) systems.get(uri, version);
      } else if ("ConceptMap".equals(cls)) {
        return (T) maps.get(uri, version);
      } else if ("PlanDefinition".equals(cls)) {
        return (T) plans.get(uri, version);
      } else if ("OperationDefinition".equals(cls)) {
        OperationDefinition od = operations.get(uri, version);
        return (T) od;
      } else if ("Questionnaire".equals(cls)) {
        return (T) questionnaires.get(uri, version);
      } else if ("SearchParameter".equals(cls)) {
        SearchParameter res = searchParameters.get(uri, version);
        return (T) res;
      }
      if ("CodeSystem".equals(cls) && codeSystems.has(uri)) {
        return (T) codeSystems.get(uri, version);
      } 
      if ("ValueSet".equals(cls) && valueSets.has(uri)) {
        return (T) valueSets.get(uri, version);
      } 
      
      if ("Questionnaire".equals(cls)) {
        return (T) questionnaires.get(uri, version);
      } 
      if (cls == null) {
        if (uri.matches(Constants.URI_REGEX) && !uri.contains("ValueSet")) {
          return null;
        } 

        // it might be a special URL.
        if (Utilities.isAbsoluteUrl(uri) || uri.startsWith("ValueSet/")) {
          Resource res = null; // findTxValueSet(uri);
          if (res != null) {
            return (T) res;
          } 
        }
        return null;      
      }    
      if (supportedCodeSystems.containsKey(uri)) {
        return null;
      } 
      throw new FHIRException(formatMessage(I18nConstants.NOT_DONE_YET_CANT_FETCH_, uri));
    }
  }

  public <T extends Resource> List<T> fetchResourcesByType(Class<T> class_, FhirPublication fhirVersion) {
    return fetchResourcesByType(class_);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Resource> List<T> fetchResourcesByType(Class<T> class_) {

    List<T> res = new ArrayList<>();

    if (class_ == Resource.class || class_ == ValueSet.class)  {
      if (!isAllowedToIterateTerminologyResources()) {
        // what's going on here?
        // it's not unusual to have >50k ValueSets in context. Iterating all of
        // them will cause every one of them to loaded through the lazy loading infrastructure. This
        // can consume upwards of 10GB of RAM.
        //
        // By default, the context won't let you do that. If you do want to do it, and take the performance hit, then
        // setAllowedToIterateTerminologyResources(true);
        throw new Error("This context is configured to not allow Iterating ValueSet resources due to performance concerns");
      }
    }
    synchronized (lock) {

      if (class_ == Resource.class || class_ == DomainResource.class || class_ == CanonicalResource.class || class_ == null) {
        res.addAll((List<T>) structures.getList());
        res.addAll((List<T>) guides.getList());
        res.addAll((List<T>) capstmts.getList());
        res.addAll((List<T>) measures.getList());
        res.addAll((List<T>) libraries.getList());
        res.addAll((List<T>) valueSets.getList());
        res.addAll((List<T>) codeSystems.getList());
        res.addAll((List<T>) operations.getList());
        res.addAll((List<T>) searchParameters.getList());
        res.addAll((List<T>) plans.getList());
        res.addAll((List<T>) maps.getList());
        res.addAll((List<T>) transforms.getList());
        res.addAll((List<T>) questionnaires.getList());
        res.addAll((List<T>) systems.getList());
        res.addAll((List<T>) actors.getList());
        res.addAll((List<T>) requirements.getList());
      } else if (class_ == ImplementationGuide.class) {
        res.addAll((List<T>) guides.getList());
      } else if (class_ == CapabilityStatement.class) {
        res.addAll((List<T>) capstmts.getList());
      } else if (class_ == Measure.class) {
        res.addAll((List<T>) measures.getList());
      } else if (class_ == Library.class) {
        res.addAll((List<T>) libraries.getList());
      } else if (class_ == StructureDefinition.class) {
        res.addAll((List<T>) structures.getList());
      } else if (class_ == StructureMap.class) {
        res.addAll((List<T>) transforms.getList());
      } else if (class_ == ValueSet.class) {
        res.addAll((List<T>) valueSets.getList());
      } else if (class_ == CodeSystem.class) {
        res.addAll((List<T>) codeSystems.getList());
      } else if (class_ == NamingSystem.class) {
        res.addAll((List<T>) systems.getList());
      } else if (class_ == ActorDefinition.class) {
        res.addAll((List<T>) actors.getList());
      } else if (class_ == Requirements.class) {
        res.addAll((List<T>) requirements.getList());
      } else if (class_ == ConceptMap.class) {
        res.addAll((List<T>) maps.getList());
      } else if (class_ == PlanDefinition.class) {
        res.addAll((List<T>) plans.getList());
      } else if (class_ == OperationDefinition.class) {
        res.addAll((List<T>) operations.getList());
      } else if (class_ == Questionnaire.class) {
        res.addAll((List<T>) questionnaires.getList());
      } else if (class_ == SearchParameter.class) {
        res.addAll((List<T>) searchParameters.getList());
      }
    }
    return res;
  }

  @SuppressWarnings("unchecked")
  public <T extends Resource> List<T> fetchResourceVersionsByTypeAndUrl(Class<T> class_, String url) {

    List<T> res = new ArrayList<>();

    synchronized (lock) {

      if (class_ == Resource.class || class_ == DomainResource.class || class_ == CanonicalResource.class || class_ == null) {
        res.addAll((List<T>) structures.getVersionList(url));
        res.addAll((List<T>) guides.getVersionList(url));
        res.addAll((List<T>) capstmts.getVersionList(url));
        res.addAll((List<T>) measures.getVersionList(url));
        res.addAll((List<T>) libraries.getVersionList(url));
        res.addAll((List<T>) valueSets.getVersionList(url));
        res.addAll((List<T>) codeSystems.getVersionList(url));
        res.addAll((List<T>) operations.getVersionList(url));
        res.addAll((List<T>) searchParameters.getVersionList(url));
        res.addAll((List<T>) plans.getVersionList(url));
        res.addAll((List<T>) maps.getVersionList(url));
        res.addAll((List<T>) transforms.getVersionList(url));
        res.addAll((List<T>) questionnaires.getVersionList(url));
        res.addAll((List<T>) systems.getVersionList(url));
        res.addAll((List<T>) actors.getVersionList(url));
        res.addAll((List<T>) requirements.getVersionList(url));
      } else if (class_ == ImplementationGuide.class) {
        res.addAll((List<T>) guides.getVersionList(url));
      } else if (class_ == CapabilityStatement.class) {
        res.addAll((List<T>) capstmts.getVersionList(url));
      } else if (class_ == Measure.class) {
        res.addAll((List<T>) measures.getVersionList(url));
      } else if (class_ == Library.class) {
        res.addAll((List<T>) libraries.getVersionList(url));
      } else if (class_ == StructureDefinition.class) {
        res.addAll((List<T>) structures.getVersionList(url));
      } else if (class_ == StructureMap.class) {
        res.addAll((List<T>) transforms.getVersionList(url));
      } else if (class_ == ValueSet.class) {
        res.addAll((List<T>) valueSets.getVersionList(url));
      } else if (class_ == CodeSystem.class) {
        res.addAll((List<T>) codeSystems.getVersionList(url));
      } else if (class_ == NamingSystem.class) {
        res.addAll((List<T>) systems.getVersionList(url));
      } else if (class_ == ActorDefinition.class) {
        res.addAll((List<T>) actors.getVersionList(url));
      } else if (class_ == Requirements.class) {
        res.addAll((List<T>) requirements.getVersionList(url));
      } else if (class_ == ConceptMap.class) {
        res.addAll((List<T>) maps.getVersionList(url));
      } else if (class_ == PlanDefinition.class) {
        res.addAll((List<T>) plans.getVersionList(url));
      } else if (class_ == OperationDefinition.class) {
        res.addAll((List<T>) operations.getVersionList(url));
      } else if (class_ == Questionnaire.class) {
        res.addAll((List<T>) questionnaires.getVersionList(url));
      } else if (class_ == SearchParameter.class) {
        res.addAll((List<T>) searchParameters.getVersionList(url));
      }
    }
    return res;
  }

  private Set<String> notCanonical = new HashSet<String>();

  protected IWorkerContextManager.IPackageLoadingTracker packageTracker;
  private boolean forPublication;
  private boolean cachingAllowed = true;
  private static boolean nsFailHasFailed;

  public Resource fetchResourceById(String type, String uri, FhirPublication fhirVersion) {
    return fetchResourceById(type, uri);
  }
  
  @Override
  public Resource fetchResourceById(String type, String uri) {
    synchronized (lock) {
      String[] parts = uri.split("\\/");
      if (!Utilities.noString(type) && parts.length == 1) {
        if (allResourcesById.containsKey(type)) {
          ResourceProxy res = allResourcesById.get(type).get(parts[0]);
          return res == null ? null : res.getResource();
        } else {
          return null;
        }
      }
      if (parts.length >= 2) {
        if (!Utilities.noString(type)) {
          if (!type.equals(parts[parts.length-2])) { 
            throw new Error(formatMessage(I18nConstants.RESOURCE_TYPE_MISMATCH_FOR___, type, uri));
          }
        }
        return allResourcesById.get(parts[parts.length-2]).get(parts[parts.length-1]).getResource();
      } else {
        throw new Error(formatMessage(I18nConstants.UNABLE_TO_PROCESS_REQUEST_FOR_RESOURCE_FOR___, type, uri));
      }
    }
  }

  public <T extends Resource> T fetchResource(Class<T> class_, String uri, Resource sourceForReference) {
    try {
      return fetchResourceWithException(class_, uri, sourceForReference);
    } catch (FHIRException e) {
      throw new Error(e);
    }    
  }
  
  public <T extends Resource> T fetchResource(Class<T> class_, String uri, FhirPublication fhirVersion) {
    return fetchResource(class_, uri);
  }
  
  public <T extends Resource> T fetchResource(Class<T> class_, String uri) {
    try {
      return fetchResourceWithException(class_, uri, null);
    } catch (FHIRException e) {
      throw new Error(e);
    }
  }

  public <T extends Resource> T fetchResource(Class<T> class_, String uri, String version, FhirPublication fhirVersion) {
    return fetchResource(class_, uri, version);
  }
  public <T extends Resource> T fetchResource(Class<T> class_, String uri, String version) {
    try {
      return fetchResourceWithExceptionByVersion(class_, uri, version, null);
    } catch (FHIRException e) {
      throw new Error(e);
    }
  }
  
  @Override
  public <T extends Resource> boolean hasResource(Class<T> class_, String uri) {
    try {
      return fetchResourceWithException(class_, uri) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public <T extends Resource> boolean hasResource(String cls, String uri) {
    try {
      return fetchResourceWithException(cls, uri) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public <T extends Resource> boolean hasResourceVersion(Class<T> class_, String uri, String version) {
    try {
      return fetchResourceWithExceptionByVersion(class_, uri, version, null) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public <T extends Resource> boolean hasResourceVersion(String cls, String uri, String version) {
    try {
      return fetchResourceWithExceptionByVersion(cls, uri, version, null) != null;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public <T extends Resource> boolean hasResource(Class<T> class_, String uri, FhirPublication fhirVersion) {
    try {
      return fetchResourceWithException(class_, uri) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public <T extends Resource> boolean hasResource(String cls, String uri, FhirPublication fhirVersion) {
    try {
      return fetchResourceWithException(cls, uri) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public <T extends Resource> boolean hasResourceVersion(Class<T> class_, String uri, String version, FhirPublication fhirVersion) {
    try {
      return fetchResourceWithExceptionByVersion(class_, uri, version, null) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public <T extends Resource> boolean hasResourceVersion(String cls, String uri, String version, FhirPublication fhirVersion) {
    try {
      return fetchResourceWithExceptionByVersion(cls, uri, version, null) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public <T extends Resource> boolean hasResource(Class<T> class_, String uri, Resource sourceOfReference) {
    try {
      return fetchResourceWithExceptionByVersion(class_, uri, version, null) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public void reportStatus(JsonObject json) {
    synchronized (lock) {
      json.addProperty("codeystem-count", codeSystems.size());
      json.addProperty("valueset-count", valueSets.size());
      json.addProperty("conceptmap-count", maps.size());
      json.addProperty("transforms-count", transforms.size());
      json.addProperty("structures-count", structures.size());
      json.addProperty("guides-count", guides.size());
      json.addProperty("statements-count", capstmts.size());
      json.addProperty("measures-count", measures.size());
      json.addProperty("libraries-count", libraries.size());
    }
  }


  public void dropResource(Resource r) throws FHIRException {
    dropResource(r.fhirType(), r.getId());   
  }

  public void dropResource(String fhirType, String id) {
    synchronized (lock) {

      Map<String, ResourceProxy> map = allResourcesById.get(fhirType);
      if (map == null) {
        map = new HashMap<String, ResourceProxy>();
        allResourcesById.put(fhirType, map);
      }
      if (map.containsKey(id)) {
        map.remove(id); // this is a challenge because we might have more than one resource with this id (different versions)
      }

      if (fhirType.equals("StructureDefinition")) {
        structures.drop(id);
        typeManager.reload();
      } else if (fhirType.equals("ImplementationGuide")) {
        guides.drop(id);
      } else if (fhirType.equals("CapabilityStatement")) {
        capstmts.drop(id);
      } else if (fhirType.equals("Measure")) {
        measures.drop(id);
      } else if (fhirType.equals("Library")) {
        libraries.drop(id);
      } else if (fhirType.equals("ValueSet")) {
        valueSets.drop(id);
      } else if (fhirType.equals("CodeSystem")) {
        codeSystems.drop(id);
      } else if (fhirType.equals("OperationDefinition")) {
        operations.drop(id);
      } else if (fhirType.equals("Questionnaire")) {
        questionnaires.drop(id);
      } else if (fhirType.equals("ConceptMap")) {
        maps.drop(id);
      } else if (fhirType.equals("StructureMap")) {
        transforms.drop(id);
      } else if (fhirType.equals("NamingSystem")) {
        systems.drop(id);
        systemUrlMap = null;
      } else if (fhirType.equals("ActorDefinition")) {
        actors.drop(id);
      } else if (fhirType.equals("Requirements")) {
        requirements.drop(id);
      }
    }
  }

  private <T extends CanonicalResource> void dropMetadataResource(Map<String, T> map, String id) {
    T res = map.get(id);
    if (res != null) {
      map.remove(id);
      if (map.containsKey(res.getUrl())) {
        map.remove(res.getUrl());
      }
      if (res.getVersion() != null) {
        if (map.containsKey(res.getUrl()+"|"+res.getVersion())) {
          map.remove(res.getUrl()+"|"+res.getVersion());
        }
      }
    }
  }

  
  public String listSupportedSystems() {
    synchronized (lock) {
      String sl = null;
      for (String s : supportedCodeSystems.keySet()) {
        SystemSupportInformation ss = supportedCodeSystems.get(s);
        if (ss.isSupported()) {
          sl = sl == null ? s : sl + "\r\n" + s;
        }
      }
      return sl;
    }
  }


  public int totalCount() {
    synchronized (lock) {
      return valueSets.size() +  maps.size() + structures.size() + transforms.size();
    }
  }
  
  public List<ConceptMap> listMaps() {
    List<ConceptMap> m = new ArrayList<ConceptMap>();
    synchronized (lock) {
      maps.listAll(m);
    }
    return m;
  }
  
  public List<StructureDefinition> listStructures() {
    List<StructureDefinition> m = new ArrayList<StructureDefinition>();
    synchronized (lock) {
      structures.listAll(m);    
    }
    return m;
  }

  public StructureDefinition getStructure(String code) {
    synchronized (lock) {
      return structures.get(code);
    }
  }

  private String getUri(NamingSystem ns) {
    for (NamingSystemUniqueIdComponent id : ns.getUniqueId()) {
      if (id.getType() == NamingSystemIdentifierType.URI) {
        return id.getValue();
      }
    }
    return null;
  }

  private boolean hasOid(NamingSystem ns, String oid) {
    for (NamingSystemUniqueIdComponent id : ns.getUniqueId()) {
      if (id.getType() == NamingSystemIdentifierType.OID && id.getValue().equals(oid)) {
        return true;
      }
    }
    return false;
  }

  public void cacheVS(JsonObject json, Map<String, ValidationResult> t) {
    synchronized (lock) {
      validationCache.put(json.get("url").getAsString(), t);
    }
  }

  public SearchParameter getSearchParameter(String code) {
    synchronized (lock) {
      return searchParameters.get(code);
    }
  }

  @Override
  public org.hl7.fhir.r5.context.ILoggingService getLogger() {
    return logger;
  }


  public StructureDefinition fetchTypeDefinition(String typeName, FhirPublication fhirVersion) {
    return fetchTypeDefinition(typeName);
  }

  @Override
  public StructureDefinition fetchTypeDefinition(String typeName) {
    if (Utilities.isAbsoluteUrl(typeName)) {
      StructureDefinition res = fetchResource(StructureDefinition.class, typeName);
      if (res != null) {
        return res;
      }
    } 
    StructureDefinition structureDefinition = typeManager.fetchTypeDefinition(typeName);
    generateSnapshot(structureDefinition, "5");
    return structureDefinition;
  }

  void generateSnapshot(StructureDefinition structureDefinition, String breadcrumb) {
    if (structureDefinition != null && !structureDefinition.isGeneratedSnapshot()) {
      if (structureDefinition.isGeneratingSnapshot()) {
        throw new FHIRException("Attempt to fetch the profile "+ structureDefinition.getVersionedUrl()+" while generating the snapshot for it");
      }
      try {

        logger.logDebugMessage(LogCategory.GENERATE,"Generating snapshot for "+ structureDefinition.getVersionedUrl());

       // structureDefinition.setGeneratingSnapshot(true);
        try {
          cutils.generateSnapshot(structureDefinition);
        } finally {
          //structureDefinition.setGeneratingSnapshot(false);
        }
      } catch (Exception e) {
        // not sure what to do in this case?
        log.error("Unable to generate snapshot in @" + breadcrumb + " for " + structureDefinition.getVersionedUrl()+": "+e.getMessage());
        logger.logDebugMessage(ILoggingService.LogCategory.GENERATE, ExceptionUtils.getStackTrace(e));
      }
    }
  }

  @Override
  public List<StructureDefinition> fetchTypeDefinitions(String typeName) {
    return typeManager.getDefinitions(typeName);
  }

  @Override
  public List<StructureDefinition> fetchTypeDefinitions(String typeName, FhirPublication fhirVersion) {
    return typeManager.getDefinitions(typeName);
  }


  public boolean isPrimitiveType(String type) {
    return typeManager.isPrimitive(type);
  }

  public boolean isDataType(String type) {
    return typeManager.isDataType(type);
  }
  
  public boolean isTlogging() {
    return tlogging;
  }

  public void setTlogging(boolean tlogging) {
    this.tlogging = tlogging;
  }

  public UcumService getUcumService() {
    return ucumService;
  }

  public void setUcumService(UcumService ucumService) {
    this.ucumService = ucumService;
  }

  public String getLinkForUrl(String corePath, String url) {
    if (url == null) {
      return null;
    }
    
    if (codeSystems.has(url)) {
      return codeSystems.get(url).getWebPath();
    }

    if (valueSets.has(url)) {
      return valueSets.get(url).getWebPath();
    }

    if (maps.has(url)) {
      return maps.get(url).getWebPath();
    }
    
    if (transforms.has(url)) {
      return transforms.get(url).getWebPath();
    }
    
    if (actors.has(url)) {
      return actors.get(url).getWebPath();
    }
    
    if (requirements.has(url)) {
      return requirements.get(url).getWebPath();
    }
    
    if (structures.has(url)) {
      return structures.get(url).getWebPath();
    }
    
    if (guides.has(url)) {
      return guides.get(url).getWebPath();
    }
    
    if (capstmts.has(url)) {
      return capstmts.get(url).getWebPath();
    }
    
    if (measures.has(url)) {
      return measures.get(url).getWebPath();
    }

    if (libraries.has(url)) {
      return libraries.get(url).getWebPath();
    }

    if (searchParameters.has(url)) {
      return searchParameters.get(url).getWebPath();
    }
        
    if (questionnaires.has(url)) {
      return questionnaires.get(url).getWebPath();
    }

    if (operations.has(url)) {
      return operations.get(url).getWebPath();
    }
    
    if (plans.has(url)) {
      return plans.get(url).getWebPath();
    }

    if (url.equals("http://loinc.org")) {
      return corePath+"loinc.html";
    }
    if (url.equals("http://unitsofmeasure.org")) {
      return corePath+"ucum.html";
    } 
    if (url.equals("http://snomed.info/sct")) {
      return corePath+"snomed.html";
    } 
    return null;
  }

  public List<ImplementationGuide> allImplementationGuides() {
    List<ImplementationGuide> res = new ArrayList<>();
    guides.listAll(res);
    return res;
  }

  @Override
  public Set<String> getBinaryKeysAsSet() { return binaries.keySet(); }

  @Override
  public boolean hasBinaryKey(String binaryKey) {
    return binaries.containsKey(binaryKey);
  }

  @Override
  public byte[] getBinaryForKey(String binaryKey) {
    IByteProvider bp = binaries.get(binaryKey);
    try {
      return bp == null ? null : bp.bytes();
    } catch (Exception e) {
      throw new FHIRException(e);
    }
  }

  public void finishLoading(boolean genSnapshots) {
    if (!hasResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/Base")) {
      cacheResource(ProfileUtilities.makeBaseDefinition(version));
    }
    if(genSnapshots) {
      for (StructureDefinition sd : listStructures()) {
        try {
          if (sd.getSnapshot().isEmpty()) { 
            new ContextUtilities(this).generateSnapshot(sd);
            //          new XmlParser().setOutputStyle(OutputStyle.PRETTY).compose(ManagedFileAccess.outStream(Utilities.path("[tmp]", "snapshot", tail(sd.getUrl())+".xml")), sd);
          }
        } catch (Exception e) {
          log.error("Unable to generate snapshot @1 for "+tail(sd.getUrl()) +" from "+tail(sd.getBaseDefinition())+" because "+e.getMessage());
          logger.logDebugMessage(LogCategory.GENERATE, ExceptionUtils.getStackTrace(e));
        }
      }  
    }
    
    codeSystems.setVersion(version);
    valueSets.setVersion(version);
    maps.setVersion(version);
    transforms.setVersion(version);
    structures.setVersion(version);
    typeManager.reload();
    measures.setVersion(version);
    libraries.setVersion(version);
    guides.setVersion(version);
    capstmts.setVersion(version);
    searchParameters.setVersion(version);
    questionnaires.setVersion(version);
    operations.setVersion(version);
    plans.setVersion(version);
    systems.setVersion(version);
    actors.setVersion(version);
    requirements.setVersion(version);
  }

  protected String tail(String url) {
    if (Utilities.noString(url)) {
      return "noname";
    }
    if (url.contains("/")) {
      return url.substring(url.lastIndexOf("/")+1);
    }
    return url;
  }
  
  public int getClientRetryCount() {
    return terminologyClientManager.getRetryCount();
  }
  
  public IWorkerContext setClientRetryCount(int value) {
    terminologyClientManager.setRetryCount(value);
    return this;
  }

  public TerminologyClientManager getTxClientManager() {
    return terminologyClientManager;
  }

  public String getCacheId() {
    return terminologyClientManager.getCacheId();
  }

  public TimeTracker clock() {
    return clock;
  }
 
  public int countAllCaches() {
    return codeSystems.size() + valueSets.size() + maps.size() + transforms.size() + structures.size() + measures.size() + libraries.size() + 
        guides.size() + capstmts.size() + searchParameters.size() + questionnaires.size() + operations.size() + plans.size() + 
        systems.size()+ actors.size()+ requirements.size();
  }

  public Set<String> getCodeSystemsUsed() {
    return codeSystemsUsed ;
  }
 
  public IWorkerContextManager.ICanonicalResourceLocator getLocator() {
    return locator;
  }

  public void setLocator(IWorkerContextManager.ICanonicalResourceLocator locator) {
    this.locator = locator;
  }

  public String getUserAgent() {
    return userAgent;
  }

  protected void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
    terminologyClientManager.setUserAgent(userAgent);
  }


  public IWorkerContextManager.IPackageLoadingTracker getPackageTracker() {
    return packageTracker;
  }
  
  public IWorkerContext setPackageTracker(IWorkerContextManager.IPackageLoadingTracker packageTracker) {
    this.packageTracker = packageTracker;
    return this;
  }
  

  @Override
  public PEBuilder getProfiledElementBuilder(PEElementPropertiesPolicy elementProps, boolean fixedProps) {
    // TODO Auto-generated method stub
    return new PEBuilder(this, elementProps, fixedProps);
  }
  
  public boolean isForPublication() {
    return forPublication;
  }
  
  public void setForPublication(boolean value) {
    forPublication = value;
  }

  public boolean isCachingAllowed() {
    return cachingAllowed;
  }

  public void setCachingAllowed(boolean cachingAllowed) {
    this.cachingAllowed = cachingAllowed;
  }

  @Override
  public OIDSummary urlsForOid(String oid, String resourceType) {
    OIDSummary set = urlsForOid(oid, resourceType, true);
    if (set.getDefinitions().size() > 1) {
      set = urlsForOid(oid, resourceType, false);
    }
    return set;
  }
  
  public OIDSummary urlsForOid(String oid, String resourceType, boolean retired) {
    OIDSummary summary = new OIDSummary();
    if (oid != null) {
      if (oidCacheManual.containsKey(oid)) {
        summary.addOIDs(oidCacheManual.get(oid));
      }
      for (OIDSource os : oidSources) {
        if (os.db == null) {
          os.db = connectToOidSource(os.folder);
        }
        if (os.db != null) {
          try {
            PreparedStatement psql = resourceType == null ?
                os.db.prepareStatement("Select TYPE, URL, VERSION, Status from OIDMap where OID = ?") :
                os.db.prepareStatement("Select TYPE, URL, VERSION, Status from OIDMap where TYPE = '"+resourceType+"' and OID = ?");
            psql.setString(1, oid);
            ResultSet rs = psql.executeQuery();
            while (rs.next()) {
              if (retired || !"retired".equals(rs.getString(4))) {
                String rt = rs.getString(1);
                String url = rs.getString(2);
                String version = rs.getString(3);
                String status = rs.getString(4);
                summary.addOID(new OIDDefinition(rt, oid, url, version, os.pid, status));
              }
            }
          } catch (Exception e) {
            // nothing, there would alreagy have been an error
  //          e.printStackTrace();
          }
        }
      }      
  
      switch (oid) {
      case "2.16.840.1.113883.6.1" :
        summary.addOID(new OIDDefinition("CodeSystem", "2.16.840.1.113883.6.1", "http://loinc.org", null, null, null));
        break;
      case "2.16.840.1.113883.6.8" :
        summary.addOID(new OIDDefinition("CodeSystem", "2.16.840.1.113883.6.8", "http://unitsofmeasure.org", null, null, null));
        break;
      case "2.16.840.1.113883.6.96" :
        summary.addOID(new OIDDefinition("CodeSystem", "2.16.840.1.113883.6.96", "http://snomed.info/sct", null, null, null));
        break;
      default:
      }
    }
    summary.sort();
    return summary;
  }

  private Connection connectToOidSource(String folder) {
    try {
      File ff = ManagedFileAccess.file(folder);
      File of = ManagedFileAccess.file(Utilities.path(ff.getAbsolutePath(), ".oid-map-2.db"));
      if (!of.exists()) {
        OidIndexBuilder oidBuilder = new OidIndexBuilder(ff, of);
        oidBuilder.build();
      }
      return DriverManager.getConnection("jdbc:sqlite:"+of.getAbsolutePath());
    } catch (Exception e) {
      return null;
    }
  }


  public void unload() {

    codeSystems.unload();
    valueSets.unload();
    maps.unload();
    transforms.unload();
    structures.unload();
    typeManager.unload();
    measures.unload();
    libraries.unload();
    guides.unload();
    capstmts.unload();
    searchParameters.unload();
    questionnaires.unload();
    operations.unload();
    plans.unload();
    actors.unload();
    requirements.unload();
    systems.unload();

    binaries.clear();
    validationCache.clear();
    txCache.unload();
}
  
  private <T extends Resource> T doFindTxResource(Class<T> class_, String canonical) {
    // well, we haven't found it locally. We're going look it up
    if (class_ == ValueSet.class) {
      ValueSet ivs = new ImplicitValueSets(getExpansionParameters()).generateImplicitValueSet(canonical);
      if (ivs != null) {
        return (T) ivs;
      }
      SourcedValueSet svs = null;
      if (txCache.hasValueSet(canonical)) {
        svs = txCache.getValueSet(canonical);
      } else {
        svs = terminologyClientManager.findValueSetOnServer(canonical);
        txCache.cacheValueSet(canonical, svs);
      }
      if (svs != null) {
        String web = ExtensionUtilities.readStringExtension(svs.getVs(), ExtensionDefinitions.EXT_WEB_SOURCE_OLD, ExtensionDefinitions.EXT_WEB_SOURCE_NEW);
        if (web == null) {
          web = Utilities.pathURL(svs.getServer(), "ValueSet", svs.getVs().getIdBase());
        }
        svs.getVs().setWebPath(web);
        svs.getVs().setUserData(UserDataNames.render_external_link, svs.getServer()); // so we can render it differently
      }      
      if (svs == null) {
        return null;
      } else {
        cacheResource(svs.getVs());
        return (T) svs.getVs();
      }
    } else if (class_ == CodeSystem.class) {
      SourcedCodeSystem scs = null;
      if (txCache.hasCodeSystem(canonical)) {
        scs = txCache.getCodeSystem(canonical);
      } else {
        scs = terminologyClientManager.findCodeSystemOnServer(canonical);
        txCache.cacheCodeSystem(canonical, scs);
      }
      if (scs != null) {
        String web = ExtensionUtilities.readStringExtension(scs.getCs(), ExtensionDefinitions.EXT_WEB_SOURCE_OLD, ExtensionDefinitions.EXT_WEB_SOURCE_NEW);
        if (web == null) {
          web = Utilities.pathURL(scs.getServer(), "ValueSet", scs.getCs().getIdBase());
        }
        scs.getCs().setWebPath(web);
        scs.getCs().setUserData(UserDataNames.render_external_link, scs.getServer()); // so we can render it differently
      }      
      if (scs == null) {
        return null;
      } else {
        cacheResource(scs.getCs());
        return (T) scs.getCs();
      }
    } else {
      throw new Error("Not supported: doFindTxResource with type of "+class_.getName());
    }
  }

  public <T extends Resource> T findTxResource(Class<T> class_, String canonical, Resource sourceOfReference) {
    if (canonical == null) {
      return null;
    }
   T result = fetchResource(class_, canonical, sourceOfReference);
   if (result == null) {
     result = doFindTxResource(class_, canonical);
   }
   return result;
  }

  public <T extends Resource> T findTxResource(Class<T> class_, String canonical) {
    if (canonical == null) {
      return null;
    }
    T result = fetchResource(class_, canonical);
    if (result == null) {
      result = doFindTxResource(class_, canonical);
    }
    return result;
  }
  
  public <T extends Resource> T findTxResource(Class<T> class_, String canonical, String version) {
    if (canonical == null) {
      return null;
    }
    T result = fetchResource(class_, canonical, version);
    if (result == null) {
      result = doFindTxResource(class_, canonical+"|"+version);
    }
    return result;
  }

  @Override
  public <T extends Resource> List<T> fetchResourcesByUrl(Class<T> class_, String uri) {
    List<T> res = new ArrayList<>();
    if (uri != null && !uri.startsWith("#")) {
      if (class_ == StructureDefinition.class) {
        uri = ProfileUtilities.sdNs(uri, null);
      }
      if (uri.contains("|")) {
        throw new Error("at fetchResourcesByUrl, but a version is found in the uri - should not happen ('"+uri+"')");
      }
      if (uri.contains("#")) {
        uri = uri.substring(0, uri.indexOf("#"));
      } 
      synchronized (lock) {
        if (class_ == Resource.class || class_ == null) {
          List<ResourceProxy> list = allResourcesByUrl.get(uri);
          if (list != null) {
            for (ResourceProxy r : list) {
              if (uri.equals(r.getUrl())) {
                res.add((T) r.getResource());
              }
            }            
          }  
        }
        if (class_ == ImplementationGuide.class || class_ == Resource.class || class_ == null) {
          for (ImplementationGuide cr : guides.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == CapabilityStatement.class || class_ == Resource.class || class_ == null) {
          for (CapabilityStatement cr : capstmts.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == Measure.class || class_ == Resource.class || class_ == null) {
          for (Measure cr : measures.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == Library.class || class_ == Resource.class || class_ == null) {
          for (Library cr : libraries.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == StructureDefinition.class || class_ == Resource.class || class_ == null) {
          for (StructureDefinition cr : structures.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == StructureMap.class || class_ == Resource.class || class_ == null) {
          for (StructureMap cr : transforms.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == NamingSystem.class || class_ == Resource.class || class_ == null) {
          for (NamingSystem cr : systems.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == ValueSet.class || class_ == Resource.class || class_ == null) {
          for (ValueSet cr : valueSets.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == CodeSystem.class || class_ == Resource.class || class_ == null) {
          for (CodeSystem cr : codeSystems.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == ConceptMap.class || class_ == Resource.class || class_ == null) {
          for (ConceptMap cr : maps.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == ActorDefinition.class || class_ == Resource.class || class_ == null) {
          for (ActorDefinition cr : actors.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == Requirements.class || class_ == Resource.class || class_ == null) {
          for (Requirements cr : requirements.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == PlanDefinition.class || class_ == Resource.class || class_ == null) {
          for (PlanDefinition cr : plans.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == OperationDefinition.class || class_ == Resource.class || class_ == null) {
          for (OperationDefinition cr : operations.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == Questionnaire.class || class_ == Resource.class || class_ == null) {
          for (Questionnaire cr : questionnaires.getForUrl(uri)) {
            res.add((T) cr);
          } 
        } else if (class_ == SearchParameter.class || class_ == Resource.class || class_ == null) {
          for (SearchParameter cr : searchParameters.getForUrl(uri)) {
            res.add((T) cr);
          } 
        }
      }
    }
    return res;
  }
  
  public void setLocale(Locale locale) {
    super.setLocale(locale);
    if (locale != null) {
      String lt = locale.toLanguageTag();
      if ("und".equals(lt)) {
        throw new FHIRException("The locale "+locale.toString()+" is not valid");
      }
      if (expParameters != null) {
        for (ParametersParameterComponent p : expParameters.getParameter()) {
          if ("displayLanguage".equals(p.getName())) {
            if (p.hasUserData(UserDataNames.auto_added_parameter)) {
              p.setValue(new CodeType(lt));
              return;
            } else {
              // user supplied, we leave it alone
              return ;
            }
          }
        }
        ParametersParameterComponent p = expParameters.addParameter();
        p.setName("defaultDisplayLanguage");
        p.setValue(new CodeType(lt));
        p.setUserData(UserDataNames.auto_added_parameter, true);
      }
    }
  }
  
  @Override
  public OperationOutcome validateTxResource(ValidationOptions options, Resource resource) {
    if (resource instanceof ValueSet) {
      ValueSet vs = (ValueSet) resource;
      Set<String> systems = findRelevantSystems(vs);
      TerminologyClientContext tc = terminologyClientManager.chooseServer(vs, systems, false);
      if (tc == null) {
        throw new FHIRException(formatMessage(I18nConstants.ATTEMPT_TO_USE_TERMINOLOGY_SERVER_WHEN_NO_TERMINOLOGY_SERVER_IS_AVAILABLE));
      }
      for (ConceptSetComponent inc : vs.getCompose().getInclude()) {
        codeSystemsUsed.add(inc.getSystem());
      }
      for (ConceptSetComponent inc : vs.getCompose().getExclude()) {
        codeSystemsUsed.add(inc.getSystem());
      }

      txLog("$validate ValueSet on "+tc.getAddress());
      if (txLog != null) {
        txLog.clearLastId();
      }
      return tc.getClient().validateResource(vs);
    }
    return null;
  }


  public List<String> getSuppressedMappings() {
    return suppressedMappings;
  }

  public void setSuppressedMappings(List<String> suppressedMappings) {
    this.suppressedMappings = suppressedMappings;
    this.cutils.setSuppressedMappings(suppressedMappings);
  }

  public ContextUtilities getCutils() {
    return cutils;
  }


  public String txCacheReport() {
    return txCache.getReport();
  }


  public static boolean isAllowedToIterateTerminologyResources() {
    return allowedToIterateTerminologyResources;
  }

  public static void setAllowedToIterateTerminologyResources(boolean allowedToIterateTerminologyResources) {
    BaseWorkerContext.allowedToIterateTerminologyResources = allowedToIterateTerminologyResources;
  }
}
